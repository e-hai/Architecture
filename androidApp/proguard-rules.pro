# ==============================================================================
# Android App ProGuard / R8 Rules for Kotlin Multiplatform
# ==============================================================================

# 1. 业务与基础模型保活
-keep class xxx.yyy.zzz.** { *; }

# 2. Kotlinx Serialization
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.SerializationKt
-keepclassmembers class * {
    *** Companion;
}
-keepclasseswithmembers class * {
    kotlinx.serialization.KSerializer serializer(...);
}

# 3. Ktor Client
-keep class io.ktor.** { *; }
-dontwarn io.ktor.**

# 4. Room KMP & SQLite Bundled
-keep class androidx.room.** { *; }
-dontwarn androidx.room.**
-keep class androidx.sqlite.** { *; }
-dontwarn androidx.sqlite.**

# 5. Koin 依赖注入
-keep class io.insert.koin.** { *; }
-dontwarn io.insert.koin.**

# 6. DataStore
-keep class androidx.datastore.** { *; }
-dontwarn androidx.datastore.**

# 7. 原生商业化 Kit AAR 保活
-keep class com.kit.** { *; }
-dontwarn com.kit.**
-keep class com.github.e_hai.** { *; }
-dontwarn com.github.e_hai.**

# 8. Media3 & ExoPlayer (VideoKit)
-keep class androidx.media3.** { *; }

# 9. libpag & PagKit
-keep class org.libpag.** { *; }
-keep class io.github.pagkit.** { *; }
-keep class androidx.exifinterface.** { *; }
