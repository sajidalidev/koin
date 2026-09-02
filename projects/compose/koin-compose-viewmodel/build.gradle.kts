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

    tvosArm64()
    tvosSimulatorArm64()

    sourceSets {
        commonMain.dependencies {
            api(project(":compose:koin-compose"))
            // io.insert-koin:koin-core-viewmodel ships official tvOS klibs since 4.2.2 - reference
            // the stock coordinate (not project(":core:koin-core-viewmodel")) so this module's tvOS
            // publications don't declare a dev.sajidali.koin:koin-core-viewmodel artifact the fork
            // no longer publishes (see koin-compose for the duplicate-klib rationale).
            api("io.insert-koin:koin-core-viewmodel:$koinVersion")
            api(libs.jb.composeViewmodel)
        }
        androidMain.dependencies {
            api(libs.android.activity.compose)
        }
    }
}

val androidCompileSDK: String by project
val androidMinSDK : String by project

android {
    namespace = "org.koin.compose.viewmodel"
    compileSdk = androidCompileSDK.toInt()
    defaultConfig {
        minSdk = androidMinSDK.toInt()
        consumerProguardFiles("consumer-rules.pro")
    }
}

tasks.withType<org.jetbrains.kotlin.gradle.targets.js.npm.tasks.KotlinNpmInstallTask>().configureEach {
    args.add("--ignore-engines")
}

apply(from = file("../../gradle/publish.gradle.kts"))
