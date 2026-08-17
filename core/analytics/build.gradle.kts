plugins {
    alias(libs.plugins.shortvideo.android.library)
}

android {
    namespace = "com.shortvideo.app.core.analytics"
}

dependencies {
    // api：业务与 Application 直接使用 Analytics 门面与 Provider 类型
    api(libs.analytics.kit.firebase)
}
