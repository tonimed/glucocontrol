import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:glucocontrol/data/notification/notification_service.dart';
import 'package:glucocontrol/presentation/provider/providers.dart';
import 'package:shared_preferences/shared_preferences.dart';

const _kReminderEnabled = 'reminder_enabled';
const _kReminderHour    = 'reminder_hour';
const _kReminderMinute  = 'reminder_minute';

class SettingsState {
  final bool reminderEnabled;
  final TimeOfDay reminderTime;
  final bool isLoading;
  final String? error;

  const SettingsState({
    this.reminderEnabled = false,
    this.reminderTime = const TimeOfDay(hour: 8, minute: 0),
    this.isLoading = false,
    this.error,
  });

  SettingsState copyWith({
    bool? reminderEnabled,
    TimeOfDay? reminderTime,
    bool? isLoading,
    String? error,
    bool clearError = false,
  }) =>
      SettingsState(
        reminderEnabled: reminderEnabled ?? this.reminderEnabled,
        reminderTime: reminderTime ?? this.reminderTime,
        isLoading: isLoading ?? this.isLoading,
        error: clearError ? null : (error ?? this.error),
      );
}

class SettingsNotifier extends StateNotifier<SettingsState> {
  final NotificationService _notifications;

  SettingsNotifier(this._notifications) : super(const SettingsState()) {
    _loadPrefs();
  }

  Future<void> _loadPrefs() async {
    final prefs = await SharedPreferences.getInstance();
    state = state.copyWith(
      reminderEnabled: prefs.getBool(_kReminderEnabled) ?? false,
      reminderTime: TimeOfDay(
        hour:   prefs.getInt(_kReminderHour)   ?? 8,
        minute: prefs.getInt(_kReminderMinute) ?? 0,
      ),
    );
  }

  Future<void> toggleReminder(bool enabled) async {
    state = state.copyWith(isLoading: true, clearError: true);
    try {
      if (enabled) {
        final granted = await _notifications.requestPermission();
        if (!granted) {
          state = state.copyWith(
            isLoading: false,
            error: 'Permiso de notificaciones denegado. '
                'Actívalo en los ajustes del sistema.',
          );
          return;
        }
        await _notifications.scheduleDaily(state.reminderTime);
      } else {
        await _notifications.cancel();
      }

      final prefs = await SharedPreferences.getInstance();
      await prefs.setBool(_kReminderEnabled, enabled);
      state = state.copyWith(reminderEnabled: enabled, isLoading: false);
    } catch (_) {
      state = state.copyWith(
        isLoading: false,
        error: 'Error al configurar el recordatorio.',
      );
    }
  }

  Future<void> setReminderTime(TimeOfDay time) async {
    state = state.copyWith(reminderTime: time, clearError: true);

    final prefs = await SharedPreferences.getInstance();
    await prefs.setInt(_kReminderHour, time.hour);
    await prefs.setInt(_kReminderMinute, time.minute);

    // Reprogramar si el recordatorio estaba activo
    if (state.reminderEnabled) {
      await _notifications.scheduleDaily(time);
    }
  }
}

final settingsNotifierProvider =
    StateNotifierProvider.autoDispose<SettingsNotifier, SettingsState>(
  (ref) => SettingsNotifier(ref.read(notificationServiceProvider)),
);
