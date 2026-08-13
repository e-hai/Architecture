plugins {
    alias(libs.plugins.myproject.android.library)
}

android {
    namespace = "xxx.yyy.zzz.core.mmp"
}

dependencies {
    // api：业务直接使用 Mmp 门面；默认 AppsFlyer 实现
    api(libs.mmp.kit.appsflyer)
}
