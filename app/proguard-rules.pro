# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

# OkHttp / Retrofit optional dependencies
-dontwarn org.bouncycastle.jsse.BCSSLParameters
-dontwarn org.bouncycastle.jsse.BCSSLSocket
-dontwarn org.bouncycastle.jsse.provider.BouncyCastleJsseProvider
-dontwarn org.conscrypt.Conscrypt$Version
-dontwarn org.conscrypt.Conscrypt
-dontwarn org.conscrypt.ConscryptHostnameVerifier
-dontwarn org.openjsse.javax.net.ssl.SSLParameters
-dontwarn org.openjsse.javax.net.ssl.SSLSocket
-dontwarn org.openjsse.net.ssl.OpenJSSE

# Preserve line numbers and signatures for crash reports
-keepattributes Signature, InnerClasses, EnclosingMethod, *Annotation*
-keepattributes SourceFile, LineNumberTable

# Preserve enum constant names and fields for Moshi / reflection
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
    <fields>;
}

# Moshi rules: Keep generated JsonAdapters and Moshi data models
-keep class * extends com.squareup.moshi.JsonAdapter
-keep @com.squareup.moshi.JsonClass class * { *; }
-keepclassmembers @com.squareup.moshi.JsonClass class * {
    <init>(...);
}
-keepclassmembers class * {
    @com.squareup.moshi.Json *;
}

# Keep Adhan library models and enums (CalculationMethod, Madhab, etc.)
-keep class com.batoulapps.adhan.** { *; }
-keepclassmembers class com.batoulapps.adhan.** { *; }

# Keep App data models & DTOs
-keep class com.prayertimesApp.data.** { *; }
-keepclassmembers class com.prayertimesApp.data.** { *; }


