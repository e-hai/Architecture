# Retrofit & Serialization ProGuard Rules
-keepattributes Signature, InnerClasses, EnclosingMethod, AnnotationDefault
-dontwarn rx.**
-dontwarn retrofit2.**
-keepclassmembers,allowobfuscation class * {
    @kotlinx.serialization.* <fields>;
}
-keep class kotlinx.serialization.json.** { *; }
