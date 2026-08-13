# Keep AdsKit SPI registrars and adapters.
-keep class com.kit.ads.** { *; }
-keep class * implements com.kit.ads.provider.AdsProviderAdapterRegistrar { *; }
-dontwarn com.google.android.gms.ads.**
-dontwarn com.applovin.**
