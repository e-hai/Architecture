plugins {
    alias(libs.plugins.shortvideo.android.library)
}

android {
    namespace = "com.shortvideo.app.core.mmp"
}

dependencies {
    // api：业务直接使用 Mmp 门面；默认 AppsFlyer 实现
    api(libs.mmp.kit.appsflyer)
}
