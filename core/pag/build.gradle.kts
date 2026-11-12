plugins {
    alias(libs.plugins.myproject.kmp.library)
    alias(libs.plugins.myproject.kmp.compose)
}

android {
    namespace = "xxx.yyy.zzz.core.pag"
}

val libpagCacheDir = rootProject.file(".gradle/libpag-ios/4.5.102")
val xcframeworkPath = libpagCacheDir.resolve("libpag.xcframework")
val ensureScript = rootProject.file("scripts/ensure_libpag_xcframework.sh")

val ensureLibpagXcframework =
    tasks.register<Exec>("ensureLibpagXcframework") {
        group = "build setup"
        description = "确保 Kotlin/Native 链接所需的 libpag.xcframework 已下载并校验 SHA256"
        commandLine("bash", ensureScript.absolutePath, libpagCacheDir.absolutePath)
        outputs.dir(xcframeworkPath)
    }

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
        val frameworkDir = xcframeworkPath.resolve(sliceDir)
        binaries.all {
            linkTaskProvider.configure {
                dependsOn(ensureLibpagXcframework)
            }
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
