# Firebase Realtime Database deserialises these models reflectively.
-keepclassmembers class com.sothikdor.app.models.** {
    *;
}
-keepattributes Signature
-keepattributes *Annotation*

# MPAndroidChart
-keep class com.github.mikephil.charting.** { *; }

# osmdroid
-keep class org.osmdroid.** { *; }

# Strip logging from release builds
-assumenosideeffects class android.util.Log {
    public static *** d(...);
    public static *** v(...);
    public static *** i(...);
}
