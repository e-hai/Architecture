plugins {
    alias(libs.plugins.myproject.kmp.library)
}

android {
    namespace = "xxx.yyy.zzz.core.log"
}

kotlin {
    listOf(
        iosArm64(),
        iosSimulatorArm64(),
    ).forEach { iosTarget ->
        iosTarget.compilations.getByName("main") {
            cinterops.create("kitlogfacade") {
                definitionFile.set(file("src/nativeInterop/cinterop/kitlogfacade.def"))
                includeDirs("src/nativeInterop/cinterop")
            }
        }
    }

    sourceSets {
        androidMain.dependencies {
            implementation(libs.log.kit)
        }
    }
}
