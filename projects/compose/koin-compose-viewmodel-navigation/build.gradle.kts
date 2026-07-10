import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.jetbrainsCompose)
    alias(libs.plugins.compose.compiler)
}

val koinVersion: String by project
version = koinVersion

kotlin {
    androidTarget {
        publishLibraryVariants("release")
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_1_8)
        }
    }

    jvm {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }

    js(IR) {
        nodejs()
        browser()
        binaries.executable()
    }

    wasmJs {
        nodejs()
        binaries.executable()
    }

    iosArm64()
    iosSimulatorArm64()
    macosArm64()

    // tvOS not published for this module yet: org.jetbrains.androidx.navigation:navigation-compose's
    // tvOS redirect resolves at the manifest-mapped version, but commonMain metadata compilation still
    // loses androidx.navigation.NavBackStackEntry/NavController (transitive dependency gap in the
    // fork's tvOS variant) - deferred, koin-compose / koin-compose-viewmodel are the field-critical
    // modules.

    sourceSets {
        commonMain.dependencies {
            api(project(":compose:koin-compose-viewmodel"))
            api(libs.jb.composeNavigation)
        }
    }
}

val androidCompileSDK: String by project
val androidMinSDK : String by project

android {
    namespace = "org.koin.compose.viewmodel.navigation"
    compileSdk = androidCompileSDK.toInt()
    defaultConfig {
        minSdk = androidMinSDK.toInt()
    }
}

tasks.withType<org.jetbrains.kotlin.gradle.targets.js.npm.tasks.KotlinNpmInstallTask>().configureEach {
    args.add("--ignore-engines")
}

apply(from = file("../../gradle/publish.gradle.kts"))
