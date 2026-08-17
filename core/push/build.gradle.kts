plugins {
    alias(libs.plugins.shortvideo.android.library)
}

android {
    namespace = "com.shortvideo.app.core.push"
}

dependencies {
    // api：业务直接使用 PushKitManager / notifyPush DSL
    api(libs.push.kit)
}
