import 'package:flutter/material.dart';
import 'package:flutter_local_notifications/flutter_local_notifications.dart';
import 'package:flutter_timezone/flutter_timezone.dart';
import 'package:timezone/data/latest.dart' as tz;
import 'package:timezone/timezone.dart' as tz;

const _channelId   = 'glucose_reminder';
const _channelName = 'Recordatorio de glucosa';
const _notifId     = 1;

/// Gestiona el recordatorio diario de registro de glucosa.
class NotificationService {
  final FlutterLocalNotificationsPlugin _plugin =
      FlutterLocalNotificationsPlugin();

  bool _initialized = false;

  Future<void> initialize() async {
    if (_initialized) return;

    tz.initializeTimeZones();
    // Sin esto, tz.local se queda en UTC por defecto: un recordatorio
    // configurado a las 8:00 se programaba a las 8:00 UTC (10:00 en Madrid
    // en verano), no a las 8:00 hora local — el usuario no lo veía a la
    // hora esperada.
    final localTimeZone = await FlutterTimezone.getLocalTimezone();
    tz.setLocalLocation(tz.getLocation(localTimeZone.identifier));

    const android = AndroidInitializationSettings('@mipmap/ic_launcher');
    const ios     = DarwinInitializationSettings();
    await _plugin.initialize(
      const InitializationSettings(android: android, iOS: ios),
    );

    _initialized = true;
  }

  /// Solicita permiso de notificaciones (Android 13+ / iOS).
  /// Devuelve true si se concedió.
  Future<bool> requestPermission() async {
    final impl = _plugin
        .resolvePlatformSpecificImplementation<
            AndroidFlutterLocalNotificationsPlugin>();
    if (impl == null) return true; // iOS u otra plataforma
    return await impl.requestNotificationsPermission() ?? false;
  }

  /// Programa una notificación diaria repetitiva a la hora indicada.
  Future<void> scheduleDaily(TimeOfDay time) async {
    await initialize();

    final now     = tz.TZDateTime.now(tz.local);
    var scheduled = tz.TZDateTime(
      tz.local,
      now.year,
      now.month,
      now.day,
      time.hour,
      time.minute,
    );

    // Si la hora ya pasó hoy, programar para mañana
    if (scheduled.isBefore(now)) {
      scheduled = scheduled.add(const Duration(days: 1));
    }

    const androidDetails = AndroidNotificationDetails(
      _channelId,
      _channelName,
      importance: Importance.defaultImportance,
      priority: Priority.defaultPriority,
      icon: '@mipmap/ic_launcher',
    );

    await _plugin.zonedSchedule(
      _notifId,
      'Control de Glucosa',
      '¿Ya has registrado tu glucosa hoy?',
      scheduled,
      const NotificationDetails(android: androidDetails),
      // inexact: un recordatorio diario no necesita precisión al minuto, y
      // exactAllowWhileIdle requiere el permiso especial "Alarmas y
      // recordatorios" (SCHEDULE_EXACT_ALARM) que Android 12+ no concede
      // por defecto — con inexact evitamos ese permiso adicional.
      androidScheduleMode: AndroidScheduleMode.inexactAllowWhileIdle,
      uiLocalNotificationDateInterpretation:
          UILocalNotificationDateInterpretation.absoluteTime,
      matchDateTimeComponents: DateTimeComponents.time, // se repite cada día
    );
  }

  /// Cancela el recordatorio diario.
  Future<void> cancel() async {
    await _plugin.cancel(_notifId);
  }
}
