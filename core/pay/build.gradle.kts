plugins {
    alias(libs.plugins.shortvideo.android.library)
}

android {
    namespace = "com.shortvideo.app.core.pay"
}

dependencies {
    // api：业务直接使用 PayKit 门面
    api(libs.pay.kit)
}
