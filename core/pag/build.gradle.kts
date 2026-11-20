import project.LIBPAG_IOS_VERSION
import project.configureLibpagNativeLinking

plugins {
    alias(libs.plugins.myproject.kmp.library)
    alias(libs.plugins.myproject.kmp.compose)
}

val libpagCacheDir = rootProject.file(".gradle/libpag-ios/$LIBPAG_IOS_VERSION")
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
    android {
        namespace = "xxx.yyy.zzz.core.pag"
    }

    compilerOptions {
        freeCompilerArgs.add("-Xexpect-actual-classes")
    }

    configureLibpagNativeLinking(
        project = project,
        includeLinkLibraries = true,
        ensureTask = ensureLibpagXcframework,
    )

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
