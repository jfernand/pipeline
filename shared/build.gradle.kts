import org.gradle.process.ExecOperations
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.io.ByteArrayOutputStream
import javax.inject.Inject

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidMultiplatformLibrary)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.kotlinSerialization)
    alias(libs.plugins.ksp)
}

/**
 * Generates a single commonMain Kotlin file exposing the current `git describe` output, so the
 * running app can show which commit it was built from. Always re-runs (git HEAD/dirty state can
 * change without any other Gradle input changing) and never fails the build if git is unavailable.
 */
abstract class GenerateGitInfoTask : DefaultTask() {
    @get:Inject
    abstract val execOps: ExecOperations

    @get:OutputDirectory
    abstract val outputDir: DirectoryProperty

    @TaskAction
    fun generate() {
        val describe = runCatching {
            val out = ByteArrayOutputStream()
            val result = execOps.exec {
                commandLine("git", "describe", "--tags", "--always", "--dirty")
                standardOutput = out
                isIgnoreExitValue = true
            }
            if (result.exitValue == 0) out.toString(Charsets.UTF_8).trim() else null
        }.getOrNull()?.takeIf { it.isNotBlank() } ?: "unknown"

        val pkgDir = outputDir.get().asFile.resolve("org/cr/pipeline")
        pkgDir.mkdirs()
        pkgDir.resolve("BuildInfo.kt").writeText(
            """
            |package org.cr.pipeline
            |
            |object BuildInfo {
            |    const val GIT_DESCRIBE: String = "$describe"
            |}
            |""".trimMargin(),
        )
    }
}

val generateGitInfo = tasks.register<GenerateGitInfoTask>("generateGitInfo") {
    outputDir.set(layout.buildDirectory.dir("generated/gitInfo/kotlin"))
    outputs.upToDateWhen { false }
}

kotlin {
    // Room's KMP database-constructor pattern (expect object AppDatabaseConstructor) relies
    // on expect/actual classes, still Beta in the compiler.
    compilerOptions {
        freeCompilerArgs.add("-Xexpect-actual-classes")
    }

    listOf(
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "Shared"
            isStatic = true
        }
    }

    jvm()

    js {
        browser()
    }

    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        browser()
    }

    android {
        namespace = "org.cr.pipeline.shared"
        compileSdk = libs.versions.android.compileSdk.get().toInt()
        minSdk = libs.versions.android.minSdk.get().toInt()

        compilerOptions {
            jvmTarget = JvmTarget.JVM_11
        }
        androidResources {
            enable = true
        }
        withHostTest {
            isIncludeAndroidResources = true
        }
        withDeviceTestBuilder {
            sourceSetTreeName = "test"
        }.configure {
            instrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        }
    }

    sourceSets {
        commonMain.get().kotlin.srcDir(generateGitInfo.map { it.outputDir })

        // Room's KMP support covers Android/JVM/Native but not js/wasmJs, so the
        // Room-backed data layer lives here instead of commonMain, shared only by the
        // targets that can use it. js/wasmJs fall back to an in-memory repository.
        val roomMain by creating {
            dependsOn(commonMain.get())
        }
        androidMain.get().dependsOn(roomMain)
        jvmMain.get().dependsOn(roomMain)
        iosMain.get().dependsOn(roomMain)

        // js and wasmJs share browser storage access (localStorage) but need different bindings
        // for it: js gets kotlinx.browser for free from kotlin-stdlib-js, wasmJs needs the
        // separate kotlinx-browser library (wasmJs-only — it doesn't publish a js variant).
        val webMain by creating {
            dependsOn(commonMain.get())
        }
        jsMain.get().dependsOn(webMain)
        wasmJsMain.get().dependsOn(webMain)

        androidMain.dependencies {
            implementation(libs.compose.uiToolingPreview)
            implementation(libs.compose.uiTooling)
        }
        commonMain.dependencies {
            implementation(project(":sync-core"))
            implementation(libs.compose.runtime)
            implementation(libs.compose.foundation)
            implementation(libs.compose.material3)
            implementation(libs.compose.materialIconsExtended)
            implementation(libs.compose.ui)
            implementation(libs.compose.components.resources)
            implementation(libs.compose.uiToolingPreview)
            implementation(libs.androidx.lifecycle.viewmodelCompose)
            implementation(libs.androidx.lifecycle.runtimeCompose)
            implementation(libs.koin.core)
            implementation(libs.koin.compose)
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.kotlinx.datetime)
            implementation(libs.kotlinx.serialization.core)
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.navigation.compose)
            implementation(libs.multiplatform.settings)
        }
        roomMain.dependencies {
            implementation(libs.room.runtime)
            implementation(libs.sqlite.bundled)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.kotlinx.coroutines.test)
        }
        jsMain.dependencies {
            implementation(libs.wrappers.browser)
        }
        wasmJsMain.dependencies {
            implementation(libs.kotlinx.browser)
        }
        jvmMain.dependencies {
            implementation(libs.http4k.core)
            implementation(libs.http4k.ai.mcp.sdk)
        }
        jvmTest.dependencies {
            implementation(libs.forkhandles.result4k)
        }
    }
}

dependencies {
    add("kspAndroid", libs.room.compiler)
    add("kspJvm", libs.room.compiler)
    add("kspIosArm64", libs.room.compiler)
    add("kspIosSimulatorArm64", libs.room.compiler)
    androidRuntimeClasspath(libs.compose.uiTooling)
}