# Flutter
-keep class io.flutter.** { *; }
-keep class io.flutter.plugins.** { *; }

# Supabase / Ktor (serialización JSON en runtime)
-keep class io.github.jan.supabase.** { *; }
-keepattributes *Annotation*
-keepattributes Signature
-keepclassmembers class * {
    @kotlinx.serialization.SerialName <fields>;
}

# Drift (reflexión de tablas)
-keep class com.glucocontrol.** { *; }

# flutter_local_notifications
-keep class com.dexterous.** { *; }

# google_sign_in
-keep class com.google.android.gms.** { *; }

# share_plus
-keep class dev.fluttercommunity.plus.share.** { *; }

# Kotlin corrutinas
-keepclassmembernames class kotlinx.** {
    volatile <fields>;
}

# Eliminar logs de debug en release
-assumenosideeffects class android.util.Log {
    public static *** d(...);
    public static *** v(...);
}
