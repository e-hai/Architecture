plugins {
    alias(libs.plugins.myproject.android.library)
}

android {
    namespace = "xxx.yyy.zzz.core.crashreport"
}

dependencies {
    // api：业务与 Application 直接使用 CrashReport 门面
    api(libs.crashreport.kit)
}
