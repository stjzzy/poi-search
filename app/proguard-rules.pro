# ProGuard rules for POI Search App

# Keep model classes
-keep class com.example.poisearch.model.** { *; }

# Keep Volley
-keep class com.android.volley.** { *; }
-keep class org.apache.commons.logging.**

# Keep Gson
-keepattributes Signature
-keepattributes *Annotation*
-keep class sun.misc.Unsafe { *; }
-keep class com.google.gson.** { *; }

# Keep Google Play Services
-keep class com.google.android.gms.** { *; }
-dontwarn com.google.android.gms.**
