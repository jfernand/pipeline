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
// into. No release tags exist yet to derive a real major.minor from, so 1.0 stays fixed by hand
// and only the patch digit moves, driven by commit count — a real, monotonically-increasing
// number instead of "1.0.0" frozen forever.
val gitPatchVersion = gitOutput("rev-list", "--count", "HEAD")?.toIntOrNull() ?: 0
val gitPackageVersion = "1.0.$gitPatchVersion"

dependencies {
    implementation(project(":shared"))

    implementation(compose.desktop.currentOs)
    implementation(libs.kotlinx.coroutinesSwing)
    implementation(libs.navigation.compose)

    implementation(libs.compose.uiToolingPreview)
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
    }
}