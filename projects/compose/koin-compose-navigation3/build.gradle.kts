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

    // tvOS not published for this module yet: navigation add-ons (this module and
    // koin-compose-viewmodel-navigation) are cut from the tvOS release scope for now - deferred,
    // koin-compose / koin-compose-viewmodel are the field-critical modules.

    sourceSets {
        androidMain.dependencies {
            api(project(":android:koin-android"))
        }
        commonMain.dependencies {
            api(project(":compose:koin-compose"))
            implementation(libs.androidx.navigation3.runtime)
        }
    }
}

val androidCompileSDK: String by project
val androidMinSDK : String by project

android {
    namespace = "org.koin.compose.navigation3"
    compileSdk = androidCompileSDK.toInt()
    defaultConfig {
        minSdk = androidMinSDK.toInt()
    }
}

tasks.withType<org.jetbrains.kotlin.gradle.targets.js.npm.tasks.KotlinNpmInstallTask>().configureEach {
    args.add("--ignore-engines")
}

apply(from = file("../../gradle/publish.gradle.kts"))
