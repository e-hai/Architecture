plugins {
    alias(libs.plugins.myproject.android.library)
}

android {
    namespace = "xxx.yyy.zzz.core.video"
}

dependencies {
    // api：透出 VideoKit 核心库，业务模块与 Application 直接调用 VideoKit 门面与 Composable
    api(libs.videokit.core)
}
