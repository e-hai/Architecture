plugins {
    alias(libs.plugins.shortvideo.android.library)
}

android {
    namespace = "com.shortvideo.app.core.crashreport"
}

dependencies {
    // api：业务与 Application 直接使用 CrashReport 门面
    api(libs.crashreport.kit)
}
