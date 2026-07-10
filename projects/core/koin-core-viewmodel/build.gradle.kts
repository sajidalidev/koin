import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.kotlinMultiplatform)
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

//    // Enable context receivers for all targets
//    targets.all {
//        compilations.all {
//            kotlinOptions {
//                freeCompilerArgs += listOf("-Xcontext-receivers")
//            }
//        }
//    }

    js(IR) {
        nodejs()
        browser()
        binaries.executable()
    }

    wasmJs {
        binaries.executable()
        nodejs()
    }

    iosArm64()
    iosSimulatorArm64()
    macosArm64()

    tvosArm64()
    tvosSimulatorArm64()

    sourceSets {
        commonMain.dependencies {
            // io.insert-koin:koin-core ships official tvOS klibs at this version - reference the
            // external stock coordinate here (not project(":core:koin-core")) so this module's own
            // tvOS publications don't declare a dependency on a dev.sajidali.koin:koin-core artifact
            // we no longer publish (official tvOS coverage means it's out of fork scope), which would
            // otherwise cause consumers to link two koin-core klibs (dev.sajidali + io.insert-koin)
            // for the same symbols.
            api("io.insert-koin:koin-core:$koinVersion")
            api(libs.jb.lifecycleViewmodel)
            api(libs.jb.lifecycleViewmodelSavedState)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.test.junit)
        }
    }
}

val androidCompileSDK : String by project
val androidMinSDK : String by project

android {
    namespace = "org.koin.viewmodel"
    compileSdk = androidCompileSDK.toInt()
    defaultConfig {
        minSdk = androidMinSDK.toInt()
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}

apply(from = file("../../gradle/publish.gradle.kts"))
