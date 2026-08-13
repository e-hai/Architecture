plugins {
    alias(libs.plugins.myproject.android.library)
}

android {
    namespace = "xxx.yyy.zzz.core.ads"
}

dependencies {
    // api：业务直接使用 AdsManager；默认 AdMob（含 mediation 适配器）
    api(libs.ads.kit.admob)
}
