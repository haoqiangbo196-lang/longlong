# Add project specific ProGuard rules here.
# Keep Room entity classes
-keep class com.elegant.reader.data.** { *; }
-keepattributes *Annotation*

# Bmob SDK
-keep class cn.bmob.v3.** { *; }
-keep class cn.bmob.v3.helper.** { *; }
-keep class io.reactivex.rxjava3.** { *; }
-dontwarn cn.bmob.v3.**
-dontwarn io.reactivex.rxjava3.**
-dontwarn okhttp3.**
-dontwarn okio.**
