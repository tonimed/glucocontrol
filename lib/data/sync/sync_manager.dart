import 'dart:async';

import 'package:connectivity_plus/connectivity_plus.dart';
import 'package:drift/drift.dart';
import 'package:glucocontrol/data/dto/glucose_reading_dto.dart';
import 'package:glucocontrol/data/local/app_database.dart';
import 'package:glucocontrol/data/remote/supabase_client_provider.dart';
import 'package:supabase_flutter/supabase_flutter.dart' show SupabaseClient;

/// Sincroniza la base de datos local (Drift) con Supabase.
///
/// Estrategia:
///   Push — sube filas con isSynced=false al reconectar.
///   Pull — descarga registros remotos modificados desde la última sync.
///   Conflicto — last-write-wins por updated_at.
class SyncManager {
  final AppDatabase _db;
  final SupabaseClient _supabase;

  StreamSubscription<List<ConnectivityResult>>? _connectivitySub;

  // Timestamp de la última sincronización exitosa (persiste solo en memoria;
  // en producción se podría guardar en SharedPreferences)
  DateTime? _lastSyncAt;

  SyncManager(this._db, this._supabase);

  // ---------------------------------------------------------------------------
  // Arranque / parada
  // ---------------------------------------------------------------------------

  /// Inicia la escucha de conectividad. Llamar en el arranque de la app.
  /// La sincronización tras login se dispara explícitamente con [syncNow]
  /// desde AuthNotifier para evitar triggers por refresh de token.
  void start() {
    _connectivitySub = Connectivity()
        .onConnectivityChanged
        .listen(_onConnectivityChanged);

    // Intento inicial en caso de que ya haya sesión activa al arrancar
    _trySync();
  }

  void dispose() {
    _connectivitySub?.cancel();
  }

  /// Sincronización explícita — llamar tras login exitoso.
  Future<void> syncNow() {
    _lastSyncAt = null; // forzar pull completo
    return _trySync();
  }

  void _onConnectivityChanged(List<ConnectivityResult> results) {
    final hasNetwork = results.any((r) => r != ConnectivityResult.none);
    if (hasNetwork) _trySync();
  }

  // ---------------------------------------------------------------------------
  // Sync principal
  // ---------------------------------------------------------------------------

  bool _syncing = false;

  Future<void> _trySync() async {
    if (_syncing) return;
    if (_supabase.auth.currentUser == null) return;

    _syncing = true;
    try {
      await _push();
      await _pull();
      _lastSyncAt = DateTime.now().toUtc();
    } catch (_) {
      // Fallo de red — se reintentará en la próxima reconexión
    } finally {
      _syncing = false;
    }
  }

  // ---------------------------------------------------------------------------
  // Push: local → Supabase
  // ---------------------------------------------------------------------------

  Future<void> _push() async {
    final unsynced = await _db.getUnsynced();
    if (unsynced.isEmpty) return;

    final userId = _supabase.auth.currentUser!.id;

    for (final row in unsynced) {
      try {
        if (row.remoteId == null) {
          // INSERT nuevo en Supabase
          final response = await _supabase
              .from(kTableGlucoseReadings)
              .insert(_toRemoteJson(row, userId))
              .select('id')
              .single();

          final remoteId = response['id'].toString();
          await _db.markSynced(row.id, remoteId);
        } else {
          // UPDATE en Supabase
          await _supabase
              .from(kTableGlucoseReadings)
              .update(_toRemoteJson(row, userId))
              .eq('id', int.parse(row.remoteId!))
              .eq('user_id', userId);

          await _db.markSynced(row.id, row.remoteId!);
        }
      } catch (_) {
        // Si falla un registro individual, continúa con los demás
      }
    }
  }

  // ---------------------------------------------------------------------------
  // Pull: Supabase → local
  // ---------------------------------------------------------------------------

  Future<void> _pull() async {
    final userId = _supabase.auth.currentUser!.id;

    // Si es la primera vez, descarga todos; si no, solo los más recientes
    dynamic query = _supabase
        .from(kTableGlucoseReadings)
        .select()
        .eq('user_id', userId);

    if (_lastSyncAt != null) {
      query = query.gte('updated_at', _lastSyncAt!.toIso8601String());
    }

    final rows = await query as List<dynamic>;

    for (final raw in rows) {
      final json = raw as Map<String, dynamic>;
      final dto = GlucoseReadingDto.fromJson(json);
      final remoteUpdatedAt = json['updated_at'] != null
          ? DateTime.parse(json['updated_at'] as String)
          : DateTime.now().toUtc();

      await _db.upsertFromRemote(
        GlucoseReadingsTableCompanion(
          remoteId: Value(dto.id.toString()),
          valueMgDl: Value(dto.valueMgDl),
          dateEpochDay: Value(dto.dateEpochDay),
          timeSecondsOfDay: dto.timeSecondsOfDay != null
              ? Value(dto.timeSecondsOfDay!)
              : const Value.absent(),
          tag: Value(dto.tag),
          notes: dto.notes != null ? Value(dto.notes!) : const Value.absent(),
          isSynced: const Value(true),
          updatedAt: Value(remoteUpdatedAt),
        ),
      );
    }
  }

  // ---------------------------------------------------------------------------
  // Helpers
  // ---------------------------------------------------------------------------

  static Map<String, dynamic> _toRemoteJson(
    GlucoseReadingsTableData row,
    String userId,
  ) =>
      {
        'user_id': userId,
        'value_mg_dl': row.valueMgDl,
        'date_epoch_day': row.dateEpochDay,
        if (row.timeSecondsOfDay != null)
          'time_seconds_of_day': row.timeSecondsOfDay,
        'tag': row.tag,
        if (row.notes != null) 'notes': row.notes,
        'updated_at': row.updatedAt.toIso8601String(),
      };
}
