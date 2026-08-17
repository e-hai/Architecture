plugins {
    alias(libs.plugins.shortvideo.android.library)
}

android {
    namespace = "com.shortvideo.app.core.ads"
}

dependencies {
    // api：业务直接使用 AdsManager；默认 AdMob（含 mediation 适配器）
    api(libs.ads.kit.admob)
}
