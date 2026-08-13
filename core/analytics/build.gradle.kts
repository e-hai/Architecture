plugins {
    alias(libs.plugins.myproject.android.library)
}

android {
    namespace = "xxx.yyy.zzz.core.analytics"
}

dependencies {
    // api：业务与 Application 直接使用 Analytics 门面与 Provider 类型
    api(libs.analytics.kit.firebase)
}
