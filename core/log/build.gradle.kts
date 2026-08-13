plugins {
    alias(libs.plugins.myproject.android.library)
}

android {
    namespace = "xxx.yyy.zzz.core.log"
}

dependencies {
    // api：业务直接使用 LogKit 门面
    api(libs.log.kit)
}
