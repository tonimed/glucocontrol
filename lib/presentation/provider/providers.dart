import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:glucocontrol/data/export/export_service.dart';
import 'package:glucocontrol/data/local/app_database.dart';
import 'package:glucocontrol/data/local/drift_glucose_repository.dart';
import 'package:glucocontrol/data/notification/notification_service.dart';
import 'package:glucocontrol/data/repository/auth_repository_impl.dart';
import 'package:glucocontrol/data/sync/sync_manager.dart';
import 'package:glucocontrol/domain/model/glucose_range.dart';
import 'package:glucocontrol/domain/repository/auth_repository.dart';
import 'package:glucocontrol/domain/repository/glucose_repository.dart';
import 'package:supabase_flutter/supabase_flutter.dart';
import 'package:glucocontrol/domain/usecase/auth/delete_account_use_case.dart';
import 'package:glucocontrol/domain/usecase/auth/get_current_user_use_case.dart';
import 'package:glucocontrol/domain/usecase/auth/sign_in_with_email_use_case.dart';
import 'package:glucocontrol/domain/usecase/auth/sign_in_with_google_use_case.dart';
import 'package:glucocontrol/domain/usecase/auth/sign_out_use_case.dart';
import 'package:glucocontrol/domain/usecase/auth/sign_up_with_email_use_case.dart';
import 'package:glucocontrol/domain/usecase/export/export_readings_use_case.dart';
import 'package:glucocontrol/domain/usecase/query/get_daily_readings_use_case.dart';
import 'package:glucocontrol/domain/usecase/query/get_monthly_readings_use_case.dart';
import 'package:glucocontrol/domain/usecase/query/get_readings_by_date_range_use_case.dart';
import 'package:glucocontrol/domain/usecase/query/get_weekly_readings_use_case.dart';
import 'package:glucocontrol/domain/usecase/reading/add_reading_use_case.dart';
import 'package:glucocontrol/domain/usecase/reading/delete_reading_use_case.dart';
import 'package:glucocontrol/domain/usecase/reading/get_reading_by_id_use_case.dart';
import 'package:glucocontrol/domain/usecase/reading/update_reading_use_case.dart';

// ── Infraestructura ──
final appDatabaseProvider = Provider<AppDatabase>(
  (_) => AppDatabase(),
);

final syncManagerProvider = Provider<SyncManager>((ref) {
  final db = ref.read(appDatabaseProvider);
  final supabase = Supabase.instance.client;
  return SyncManager(db, supabase);
});

// ── Repositorios ──
final authRepositoryProvider = Provider<AuthRepository>(
  (_) => AuthRepositoryImpl(),
);

// Repositorio offline-first: lee y escribe en SQLite local.
// SyncManager sube los cambios a Supabase de forma transparente.
final glucoseRepositoryProvider = Provider<GlucoseRepository>(
  (ref) => DriftGlucoseRepository(ref.read(appDatabaseProvider)),
);

// ── Rango objetivo (fijo por ahora; se personalizará en Settings) ──
final glucoseRangeProvider = Provider<GlucoseRange>(
  (_) => const GlucoseRange(),
);

// ── Casos de uso — Auth ──
final signInWithEmailProvider = Provider(
  (ref) => SignInWithEmailUseCase(ref.read(authRepositoryProvider)),
);
final signUpWithEmailProvider = Provider(
  (ref) => SignUpWithEmailUseCase(ref.read(authRepositoryProvider)),
);
final signInWithGoogleProvider = Provider(
  (ref) => SignInWithGoogleUseCase(ref.read(authRepositoryProvider)),
);
final signOutProvider = Provider(
  (ref) => SignOutUseCase(ref.read(authRepositoryProvider)),
);
final getCurrentUserProvider = Provider(
  (ref) => GetCurrentUserUseCase(ref.read(authRepositoryProvider)),
);
final deleteAccountProvider = Provider(
  (ref) => DeleteAccountUseCase(ref.read(authRepositoryProvider)),
);

// ── Casos de uso — Lecturas ──
final addReadingProvider = Provider(
  (ref) => AddReadingUseCase(ref.read(glucoseRepositoryProvider)),
);
final updateReadingProvider = Provider(
  (ref) => UpdateReadingUseCase(ref.read(glucoseRepositoryProvider)),
);
final deleteReadingProvider = Provider(
  (ref) => DeleteReadingUseCase(ref.read(glucoseRepositoryProvider)),
);
final getReadingByIdProvider = Provider(
  (ref) => GetReadingByIdUseCase(ref.read(glucoseRepositoryProvider)),
);

// ── Casos de uso — Queries ──
final getDailyReadingsProvider = Provider(
  (ref) => GetDailyReadingsUseCase(ref.read(glucoseRepositoryProvider)),
);
final getWeeklyReadingsProvider = Provider(
  (ref) => GetWeeklyReadingsUseCase(ref.read(glucoseRepositoryProvider)),
);
final getMonthlyReadingsProvider = Provider(
  (ref) => GetMonthlyReadingsUseCase(ref.read(glucoseRepositoryProvider)),
);
final getReadingsByDateRangeProvider = Provider(
  (ref) => GetReadingsByDateRangeUseCase(ref.read(glucoseRepositoryProvider)),
);
final exportReadingsProvider = Provider(
  (ref) => ExportReadingsUseCase(ref.read(glucoseRepositoryProvider)),
);
final exportServiceProvider = Provider<ExportService>((_) => ExportService());

final notificationServiceProvider = Provider<NotificationService>(
  (_) => NotificationService(),
);
