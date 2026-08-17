plugins {
    alias(libs.plugins.shortvideo.android.library)
}

android {
    namespace = "com.shortvideo.app.core.video"
}

dependencies {
    // api：透出 VideoKit 核心库，业务模块与 Application 直接调用 VideoKit 门面与 Composable
    api(libs.videokit.core)
}
