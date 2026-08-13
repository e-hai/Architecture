plugins {
    alias(libs.plugins.myproject.android.library)
}

android {
    namespace = "xxx.yyy.zzz.core.push"
}

dependencies {
    // api：业务直接使用 PushKitManager / notifyPush DSL
    api(libs.push.kit)
}
