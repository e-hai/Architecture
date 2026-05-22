# Retrofit & Serialization ProGuard Rules
-keepattributes Signature, InnerClasses, EnclosingMethod, AnnotationDefault
-dontwarn rx.**
-dontwarn retrofit2.**
-keepclassmembers,allowobfuscation class * {
    @com.diffplug.spotless.* <fields>;
}
-keep class kotlinx.serialization.json.** { *; }
