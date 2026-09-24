/*
 * Copyright (c) 2026 Javier Fernández. All rights reserved.
 */

import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    alias(libs.plugins.kotlinJvm)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
}

// Same source BuildInfo.GIT_DESCRIBE (shared/build.gradle.kts) is generated from — providers.exec
// rather than a raw process call so this stays configuration-cache-safe. Never fails the build if
// git is unavailable, matching GenerateGitInfoTask's own fallback there.
fun gitOutput(vararg args: String): String? {
    val execOutput = providers.exec {
        commandLine("git", *args)
        isIgnoreExitValue = true
    }
    return runCatching { execOutput.standardOutput.asText.get().trim() }.getOrNull()?.takeIf { it.isNotBlank() }
}

// packageVersion has to be a strict major.minor.patch — unlike Android's free-form versionName,
// there's no format here to just drop the raw git describe string (with its commit hash suffix)
// into. Derived entirely from the nearest "vMAJOR.MINOR.0"-style tag: that tag's own
// major/minor, plus how many commits HEAD is ahead of it as the patch digit. Bumping the version
// is now just `git tag vX.Y.0` on the commit that should read as X.Y.0 — the patch digit resets
// to 0 right there and counts up from that point, rather than a hand-edited major.minor prefix
// glued to a total-repo-commit-count patch that never reset across a bump (which is how "1.1.229"
// happened the first time this was bumped by hand).
val tagDescribeRegex = Regex("""^v(\d+)\.(\d+)\.\d+-(\d+)-g[0-9a-f]+$""")
val gitPackageVersion = gitOutput("describe", "--tags", "--long", "--match", "v[0-9]*.[0-9]*.[0-9]*")
    ?.let { tagDescribeRegex.find(it)?.destructured }
    ?.let { (major, minor, distance) -> "$major.$minor.$distance" }
    ?: "0.0.0"

dependencies {
    implementation(project(":shared"))

    implementation(compose.desktop.currentOs)
    implementation(libs.kotlinx.coroutinesSwing)
    implementation(libs.navigation.compose)

    implementation(libs.compose.uiToolingPreview)
}

// Plain ProGuard (what compose desktop's release.proguard task runs) doesn't auto-merge
// META-INF/proguard/*.pro consumer rules bundled inside dependency jars the way Android's
// R8/AGP does — so libraries relying on reflection (Room's generated *_Impl lookup, kotlinx
// .serialization, Moshi/Kotshi codegen used by the MCP SDK, bundled sqlite) silently lose those
// rules here even though the exact same jars work fine on Android. Pull the rules out of the
// runtime classpath jars ourselves rather than hand-copying and maintaining our own duplicate of
// every library's consumer rules.
val extractConsumerProguardRules = tasks.register<Copy>("extractConsumerProguardRules") {
    // The Provider wrapping configurations.getByName(...).map { zipTree(it) } doesn't carry the
    // configuration's own build dependencies (resolving a runtime classpath entry like
    // sync-core-jvm.jar depends on :sync-core:jvmJar actually having run) — an implicit,
    // unvalidated dependency Gradle now flags. Declare it explicitly instead.
    dependsOn(configurations.named("runtimeClasspath"))
    from(provider { configurations.getByName("runtimeClasspath").map { zipTree(it) } }) {
        include("META-INF/proguard/*.pro")
        // kotlin-reflect.pro's "-keep class kotlin.Metadata { *; }" + RuntimeVisible*Annotations
        // trigger ProGuard 7.7.0's Kotlin-module-mapping pass, which reads every .kotlin_module
        // file on the classpath including this project's own — and that pass hard-fails on
        // metadata format 2.4.0 (Kotlin 2.4.10) since bundled kotlin-metadata-jvm only supports up
        // to 2.2.0. kotlin-reflect itself is only a transitive dependency here, not something this
        // app calls into reflectively, so its rule isn't needed and is excluded rather than
        // worked around.
        exclude("**/kotlin-reflect.pro")
    }
    into(layout.buildDirectory.dir("consumerProguardRules"))
    duplicatesStrategy = DuplicatesStrategy.INCLUDE
}

compose.desktop {
    application {
        mainClass = "org.cr.pipeline.MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "org.cr.pipeline"
            packageVersion = gitPackageVersion

            macOS { iconFile.set(project.file("icon/icon.icns")) }
            windows { iconFile.set(project.file("icon/icon.ico")) }
            linux { iconFile.set(project.file("icon/icon.png")) }
        }
        buildTypes.release.proguard {
            // Point this directly to your android module's proguard file path
            configurationFiles.from(project.file("compose-desktop.pro"))
            // -include (what configurationFiles feeds ProGuard) takes individual files, not a
            // directory, so hand it the extracted *.pro files themselves rather than the Copy
            // task's output directory; .builtBy wires the task dependency since a FileTree built
            // from a directory doesn't otherwise know it depends on whatever populates it.
            configurationFiles.from(
                fileTree(layout.buildDirectory.dir("consumerProguardRules")) { include("**/*.pro") }
                    .builtBy(extractConsumerProguardRules),
            )
        }
    }
}

project.afterEvaluate {

    // PL-039-001: on this machine, neither Skiko/AWT's Window(icon = ...) call nor jpackage's own
    // --icon flag gets the running window's icon into _NET_WM_ICON on Linux (confirmed against a
    // real running instance via `xprop -id <window> _NET_WM_ICON` — the property is simply
    // absent), so GNOME Shell's task switcher/Alt+Tab falls back to desktop-file-based icon
    // lookup instead of reading the window's own icon. That lookup fails too: jpackage's
    // generated .desktop file has no StartupWMClass, and the running window's actual WM_CLASS
    // ("Org.cr.pipeline", also confirmed via xprop) doesn't match the .desktop file's own
    // oddly-doubled name ("org.cr.pipeline-org.cr.pipeline.desktop") closely enough for GNOME's
    // matching heuristic — so it shows a generic icon instead. The Compose Desktop Gradle plugin
    // has no hook to customize jpackage's generated .desktop file (no --resource-dir passthrough
    // the way jpackage itself supports), so this patches the packaged .deb directly as the task's
    // very last action — anything depending on packageReleaseDeb (copyFinalInstaller included)
    // only sees the patched .deb, since a doLast action runs before the task is considered
    // complete. Registered inside afterEvaluate: the compose plugin only creates
    // packageReleaseDeb once nativeDistributions' target formats are processed.
    //
    // debDir/workDir are resolved to plain Files here, at configuration time (safe — this is
    // ordinary project configuration, not a stored task action), so doLast below only ever
    // captures serializable File values, never layout/providers/Project itself.
    val debDir = layout.buildDirectory.dir("compose/binaries/main-release/deb").get().asFile
    val debPatchWorkDir = layout.buildDirectory.dir("compose/tmp/debPatch").get().asFile
    tasks.named("packageReleaseDeb") {
        doLast {
            // A local function declared right here, not a top-level script function — calling a
            // top-level function from inside doLast still captures a reference to the enclosing
            // script object itself ("cannot serialize Gradle script object references"), even
            // though the function body only touches plain JDK APIs. A function local to this
            // lambda, referencing only its own parameters, captures nothing.
            fun runCommand(vararg args: String) {
                val process = ProcessBuilder(*args).redirectErrorStream(true).start()
                val output = process.inputStream.bufferedReader().readText()
                val exitCode = process.waitFor()
                check(exitCode == 0) { "${args.joinToString(" ")} failed ($exitCode): $output" }
            }

            val deb = debDir.listFiles { f -> f.extension == "deb" }?.singleOrNull()
                ?: error("Expected exactly one .deb in $debDir")
            debPatchWorkDir.deleteRecursively()
            debPatchWorkDir.mkdirs()
            runCommand("dpkg-deb", "-R", deb.absolutePath, debPatchWorkDir.absolutePath)
            // isFile matters: the bundled JDK runtime's own legal-notices tree contains a
            // directory literally named "legal/java.desktop" (the java.desktop JDK module's
            // license folder) — File.extension only parses the name string, so without isFile
            // that directory matches "desktop" too, and the later readText() on it as if it were
            // a file blows up with "Is a directory".
            val desktopFiles = debPatchWorkDir.walkTopDown().filter { it.isFile && it.extension == "desktop" }.toList()
            check(desktopFiles.isNotEmpty()) { "No .desktop file found unpacking $deb" }
            desktopFiles.forEach { f ->
                val text = f.readText()
                if (!text.contains("StartupWMClass")) {
                    f.writeText(text.trimEnd() + "\nStartupWMClass=Org.cr.pipeline\n")
                }
            }
            deb.delete()
            runCommand("dpkg-deb", "-b", debPatchWorkDir.absolutePath, deb.absolutePath)
        }
    }

    val packageTasks = tasks.withType<org.jetbrains.compose.desktop.application.tasks.AbstractJPackageTask>()
        .matching { it.name.startsWith("packageRelease") && !it.name.contains("Distributable") }

    val uberTasks = tasks.named("packageReleaseUberJarForCurrentOS")
    println("uberTasks: $uberTasks")

    tasks.register<Copy>("copyFinalInstaller") {
        dependsOn(packageTasks)
        dependsOn(uberTasks)
        group = "_isss"
        from(packageTasks.map { task -> task.destinationDir })
        from(uberTasks.map { task -> task.outputs.files })

        include("**/*.deb", "**/*.msi", "**/*.dmg", "**/*.pkg", "**/*.jar", "**/*.apk")

        into(rootProject.layout.projectDirectory.dir("release-artifacts"))
    }
}