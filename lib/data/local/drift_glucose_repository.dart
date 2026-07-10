import 'package:drift/drift.dart';
import 'package:glucocontrol/data/local/app_database.dart';
import 'package:glucocontrol/domain/model/glucose_reading.dart';
import 'package:glucocontrol/domain/model/reading_tag.dart';
import 'package:glucocontrol/domain/repository/glucose_repository.dart';

/// Implementación local del repositorio usando Drift (SQLite).
/// Escribe siempre primero en local; el SyncManager sube los cambios.
class DriftGlucoseRepository implements GlucoseRepository {
  final AppDatabase _db;

  DriftGlucoseRepository(this._db);

  // ---------------------------------------------------------------------------
  // Queries
  // ---------------------------------------------------------------------------

  @override
  Stream<List<GlucoseReading>> getReadingsByDate(DateTime date) {
    final epochDay = _toEpochDay(date);
    return _db
        .watchByDateEpochDay(epochDay)
        .map((rows) => rows.map(_toDomain).toList());
  }

  @override
  Stream<List<GlucoseReading>> getReadingsByDateRange(
    DateTime from,
    DateTime to,
  ) {
    return _db
        .watchByDateRange(_toEpochDay(from), _toEpochDay(to))
        .map((rows) => rows.map(_toDomain).toList());
  }

  @override
  Future<GlucoseReading?> getReadingById(int id) async {
    final row = await _db.getById(id);
    return row != null ? _toDomain(row) : null;
  }

  // ---------------------------------------------------------------------------
  // Mutaciones (isSynced = false → pendiente de subir)
  // ---------------------------------------------------------------------------

  @override
  Future<int> insertReading(GlucoseReading reading) =>
      _db.insertReading(_toCompanion(reading, isSynced: false));

  @override
  Future<void> updateReading(GlucoseReading reading) =>
      _db.updateReading(_toCompanion(reading, isSynced: false));

  @override
  Future<void> deleteReading(int id) => _db.deleteReading(id);

  // ---------------------------------------------------------------------------
  // Conversiones domain ↔ Drift
  // ---------------------------------------------------------------------------

  static int _toEpochDay(DateTime date) =>
      date.toUtc().millisecondsSinceEpoch ~/ 86400000;

  static DateTime _fromEpochDay(int day) =>
      DateTime.utc(1970).add(Duration(days: day));

  static GlucoseReading _toDomain(GlucoseReadingsTableData row) {
    DateTime? time;
    if (row.timeSecondsOfDay != null) {
      final base = _fromEpochDay(row.dateEpochDay);
      time = base.add(Duration(seconds: row.timeSecondsOfDay!));
    }
    return GlucoseReading(
      id: row.id,
      date: _fromEpochDay(row.dateEpochDay),
      time: time,
      valueMgDl: row.valueMgDl,
      tag: ReadingTag.fromString(row.tag),
      notes: row.notes,
    );
  }

  static GlucoseReadingsTableCompanion _toCompanion(
    GlucoseReading r, {
    required bool isSynced,
    String? remoteId,
  }) {
    final epochDay = _toEpochDay(r.date);
    final timeSeconds = r.time != null
        ? r.time!.hour * 3600 + r.time!.minute * 60 + r.time!.second
        : null;

    return GlucoseReadingsTableCompanion(
      id: r.id > 0 ? Value(r.id) : const Value.absent(),
      remoteId: remoteId != null ? Value(remoteId) : const Value.absent(),
      valueMgDl: Value(r.valueMgDl),
      dateEpochDay: Value(epochDay),
      timeSecondsOfDay:
          timeSeconds != null ? Value(timeSeconds) : const Value.absent(),
      tag: Value(r.tag.name.toUpperCase()),
      notes: r.notes != null ? Value(r.notes!) : const Value.absent(),
      isSynced: Value(isSynced),
      updatedAt: Value(DateTime.now().toUtc()),
    );
  }
}
