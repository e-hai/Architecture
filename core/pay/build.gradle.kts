plugins {
    alias(libs.plugins.myproject.android.library)
}

android {
    namespace = "xxx.yyy.zzz.core.pay"
}

dependencies {
    // api：业务直接使用 PayKit 门面
    api(libs.pay.kit)
}
