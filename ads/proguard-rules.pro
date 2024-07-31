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

-keep class com.amazic.ads.billing.**{ *; }
-keep class com.amazic.ads.callback.**{ *; }
-keep class com.amazic.ads.dialog.**{ *; }
-keep class com.amazic.ads.event.**{ *; }
-keep class com.amazic.ads.iap.**{ *; }
-keep class com.amazic.ads.service.**{ *; }
-keep class com.amazic.ads.util.**{ *; }
-keep class com.amazic.ads.organic.AdjustOutputModel.**{ *; }
-keep class com.amazic.ads.organic.ApiServiceAdjust.**{ *; }
-keep class com.amazic.ads.organic.RetrofitClientAdjust.**{ *; }
-keep class com.amazic.ads.organic.Constant.**{ *; }
-keep class com.amazic.ads.organic.TechManager {
    public *;
}
-keep class retrofit2.**{ *; }