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
// into. No release tags exist yet to derive a real major.minor from, so major.minor stays fixed
// by hand (bumped manually when it's worth signaling — 1.1 marks the first ProGuard-release-build
// cycle, PL-031 through the MCP JSON-RPC -32600 fix) and only the patch digit moves, driven by
// commit count — a real, monotonically-increasing number instead of "1.1.0" frozen forever.
val gitPatchVersion = gitOutput("rev-list", "--count", "HEAD")?.toIntOrNull() ?: 0
val gitPackageVersion = "1.1.$gitPatchVersion"

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