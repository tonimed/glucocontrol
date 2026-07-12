# Flutter
-keep class io.flutter.** { *; }
-keep class io.flutter.plugins.** { *; }

# Play Core (deferred components) — no se usa en esta app, pero el motor de
# Flutter referencia estas clases condicionalmente; sin esta regla R8 falla
# con "Missing classes" en el build release.
-dontwarn com.google.android.play.core.**

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
# El propio "-keep class com.dexterous.**" NO basta: el plugin persiste las
# notificaciones programadas usando Gson con TypeToken genérico
# (new TypeToken<List<...>>(){}), y R8 en release recorta la información de
# tipo genérico de esa subclase anónima. Sin las reglas de abajo, cualquier
# llamada que toque la caché (cancel(), y la propia programación) lanza
# "java.lang.RuntimeException: Missing type parameter" en
# FlutterLocalNotificationsPlugin.loadScheduledNotifications — reproducido en
# dispositivo físico: el interruptor "Recordatorio diario" fallaba al
# desactivarse y la notificación nunca llegaba a programarse de verdad.
-keep class com.dexterous.** { *; }
-keep class com.google.gson.reflect.TypeToken { *; }
-keep class * extends com.google.gson.reflect.TypeToken
-keep,allowobfuscation,allowshrinking class com.google.gson.reflect.TypeToken
-keep,allowobfuscation,allowshrinking class * extends com.google.gson.reflect.TypeToken

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
