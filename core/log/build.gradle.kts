plugins {
    alias(libs.plugins.shortvideo.android.library)
}

android {
    namespace = "com.shortvideo.app.core.log"
}

dependencies {
    // api：业务直接使用 LogKit 门面
    api(libs.log.kit)
}
