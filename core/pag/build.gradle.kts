plugins {
    alias(libs.plugins.myproject.kmp.library)
    alias(libs.plugins.myproject.kmp.compose)
}

android {
    namespace = "xxx.yyy.zzz.core.pag"
}

val xcframeworkPath = rootProject.file("iosApp/Frameworks/libpag.xcframework")

kotlin {
    compilerOptions {
        freeCompilerArgs.add("-Xexpect-actual-classes")
    }

    targets.withType<org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget> {
        val sliceDir =
            if (name.contains("Simulator", ignoreCase = true)) {
                "ios-arm64_x86_64-simulator"
            } else {
                "ios-arm64"
            }
        val frameworkDir = File(xcframeworkPath, sliceDir)
        binaries.all {
            linkerOpts(
                "-F${frameworkDir.absolutePath}",
                "-framework",
                "libpag",
                "-lc++",
                "-rpath",
                frameworkDir.absolutePath,
            )
        }
    }

    sourceSets {
        commonMain.dependencies {
            api(libs.pag.kit)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.kotlinx.coroutines.test)
        }
    }
}
