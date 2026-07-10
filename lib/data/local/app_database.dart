import 'dart:io';

import 'package:drift/drift.dart';
import 'package:drift/native.dart';
import 'package:path/path.dart' as p;
import 'package:path_provider/path_provider.dart';

part 'app_database.g.dart';

// ---------------------------------------------------------------------------
// Tabla de lecturas locales — espejo del schema Supabase + campos de sync
// ---------------------------------------------------------------------------
class GlucoseReadingsTable extends Table {
  @override
  String get tableName => 'glucose_readings';

  IntColumn get id => integer().autoIncrement()();

  // ID remoto de Supabase (null mientras no se ha sincronizado)
  TextColumn get remoteId => text().nullable()();

  IntColumn get valueMgDl => integer()();

  // Días desde epoch Unix (misma codificación que el backend)
  IntColumn get dateEpochDay => integer()();

  IntColumn get timeSecondsOfDay => integer().nullable()();

  TextColumn get tag => text().withDefault(const Constant('OTRO'))();

  TextColumn get notes => text().nullable()();

  // false = pendiente de subir a Supabase
  BoolColumn get isSynced => boolean().withDefault(const Constant(false))();

  // Timestamp local de última modificación (para resolver conflictos)
  DateTimeColumn get updatedAt => dateTime()();
}

// ---------------------------------------------------------------------------
// Base de datos principal
// ---------------------------------------------------------------------------
@DriftDatabase(tables: [GlucoseReadingsTable])
class AppDatabase extends _$AppDatabase {
  AppDatabase() : super(_openConnection());

  @override
  int get schemaVersion => 1;

  // ---------------------------------------------------------------------------
  // Queries de lectura
  // ---------------------------------------------------------------------------

  Stream<List<GlucoseReadingsTableData>> watchByDateEpochDay(int epochDay) =>
      (select(glucoseReadingsTable)
            ..where((t) => t.dateEpochDay.equals(epochDay))
            ..orderBy([(t) => OrderingTerm.asc(t.timeSecondsOfDay)]))
          .watch();

  Stream<List<GlucoseReadingsTableData>> watchByDateRange(
    int fromEpochDay,
    int toEpochDay,
  ) =>
      (select(glucoseReadingsTable)
            ..where(
              (t) =>
                  t.dateEpochDay.isBiggerOrEqualValue(fromEpochDay) &
                  t.dateEpochDay.isSmallerOrEqualValue(toEpochDay),
            )
            ..orderBy([
              (t) => OrderingTerm.asc(t.dateEpochDay),
              (t) => OrderingTerm.asc(t.timeSecondsOfDay),
            ]))
          .watch();

  Future<GlucoseReadingsTableData?> getById(int id) =>
      (select(glucoseReadingsTable)..where((t) => t.id.equals(id)))
          .getSingleOrNull();

  Future<GlucoseReadingsTableData?> getByRemoteId(String remoteId) =>
      (select(glucoseReadingsTable)
            ..where((t) => t.remoteId.equals(remoteId)))
          .getSingleOrNull();

  /// Filas pendientes de sincronizar con Supabase
  Future<List<GlucoseReadingsTableData>> getUnsynced() =>
      (select(glucoseReadingsTable)
            ..where((t) => t.isSynced.equals(false)))
          .get();

  // ---------------------------------------------------------------------------
  // Mutaciones
  // ---------------------------------------------------------------------------

  Future<int> insertReading(GlucoseReadingsTableCompanion row) =>
      into(glucoseReadingsTable).insert(row);

  Future<bool> updateReading(GlucoseReadingsTableCompanion row) =>
      update(glucoseReadingsTable).replace(row);

  Future<int> deleteReading(int id) =>
      (delete(glucoseReadingsTable)..where((t) => t.id.equals(id))).go();

  /// Marca una fila como sincronizada y guarda el remoteId asignado por Supabase
  Future<void> markSynced(int localId, String remoteId) =>
      (update(glucoseReadingsTable)..where((t) => t.id.equals(localId))).write(
        GlucoseReadingsTableCompanion(
          remoteId: Value(remoteId),
          isSynced: const Value(true),
        ),
      );

  /// Inserta o actualiza un registro remoto recibido durante el pull de sync.
  /// Usa el remoteId como clave de deduplicación.
  Future<void> upsertFromRemote(GlucoseReadingsTableCompanion row) async {
    final existing = await getByRemoteId(row.remoteId.value!);
    if (existing == null) {
      await into(glucoseReadingsTable).insert(row);
    } else {
      // Solo sobreescribe si el remoto es más reciente (last-write-wins)
      if (row.updatedAt.value.isAfter(existing.updatedAt)) {
        await (update(glucoseReadingsTable)
              ..where((t) => t.id.equals(existing.id)))
            .write(row);
      }
    }
  }
}

// ---------------------------------------------------------------------------
// Apertura de la base de datos SQLite
// ---------------------------------------------------------------------------
LazyDatabase _openConnection() {
  return LazyDatabase(() async {
    final dir = await getApplicationDocumentsDirectory();
    final file = File(p.join(dir.path, 'glucocontrol.db'));
    return NativeDatabase.createInBackground(file);
  });
}
