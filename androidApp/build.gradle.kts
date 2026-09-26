/*
 * Copyright (c) 2026 Javier Fernández. All rights reserved.
 */

import com.android.build.gradle.internal.tasks.BundleToStandaloneApkTask
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.androidApplication)
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

// No release tags exist yet to derive a real major.minor.patch from — commit count is the
// simplest monotonically-increasing stand-in for the versionCode Play requires until there are.
val gitVersionCode = gitOutput("rev-list", "--count", "HEAD")?.toIntOrNull() ?: 1
val gitVersionName = gitOutput("describe", "--tags", "--always", "--dirty") ?: "unknown"

// Release signing comes from the environment — CI secrets (see .github/workflows/ci.yml's apk
// leg), or a local shell that exports the same four variables — never from a file in the repo.
// With PIPELINE_KEYSTORE_FILE unset, release builds stay unsigned exactly as before, so local and
// `build`-job builds are unaffected. providers.environmentVariable rather than System.getenv so the
// configuration cache knows these are inputs.
fun env(name: String): String? = providers.environmentVariable(name).orNull?.takeIf { it.isNotBlank() }
val releaseKeystore = env("PIPELINE_KEYSTORE_FILE")?.let { file(it) }

kotlin {
    compilerOptions {
        jvmTarget = JvmTarget.JVM_11
    }
}
dependencies {
    implementation(project(":shared"))

    implementation(libs.androidx.activity.compose)
    implementation(libs.navigation.compose)

    implementation(libs.compose.uiToolingPreview)
    debugImplementation(libs.compose.uiTooling)
}

android {
    namespace = "org.cr.pipeline"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        applicationId = "org.cr.pipeline"
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        versionCode = gitVersionCode
        versionName = gitVersionName
    }
    signingConfigs {
        if (releaseKeystore != null) {
            create("release") {
                storeFile = releaseKeystore
                storePassword = env("PIPELINE_KEYSTORE_PASSWORD")
                keyAlias = env("PIPELINE_KEY_ALIAS")
                keyPassword = env("PIPELINE_KEY_PASSWORD")
            }
        }
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    buildTypes {
        release {
            signingConfig = signingConfigs.findByName("release")
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
    }
}

project.afterEvaluate {

    val packageTasks = tasks.withType<BundleToStandaloneApkTask>()
        .matching { it.name.startsWith("package") && !it.name.contains("Distributable") }

    tasks.register<Copy>("copyFinalInstaller") {
        dependsOn(packageTasks)
        group = "_isss"
        from(packageTasks.map { task -> task.outputDirectory })

        include("**/*.deb", "**/*.msi", "**/*.dmg", "**/*.pkg", "**/*.jar", "**/*.apk")

        into(rootProject.layout.projectDirectory.dir("release-artifacts"))
    }
}