plugins {
    alias(libs.plugins.myproject.android.application)
}

android {
    namespace = "xxx.yyy.zzz.android"

    defaultConfig {
        applicationId = "xxx.yyy.zzz"
        versionCode = 1
        versionName = "1.0.0"
    }
}

dependencies {
    implementation(project(":shared"))
    implementation(project(":core:log"))
    implementation(project(":core:crashreport"))
    implementation(project(":core:analytics"))
    implementation(project(":core:abtesting"))
    implementation(project(":core:mmp"))
    implementation(project(":core:ads"))
    implementation(project(":core:pay"))
    implementation(project(":core:push"))
    implementation(libs.log.kit)
    implementation(libs.crashreport.kit)
    implementation(libs.analytics.kit.firebase)
    implementation(libs.abtesting.kit.firebase)
    implementation(libs.mmp.kit.appsflyer)
    implementation(libs.ads.kit.admob)
    implementation(libs.pay.kit)
    implementation(libs.push.kit)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.core.ktx)
}
