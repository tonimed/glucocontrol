import 'dart:async';

import 'package:glucocontrol/data/dto/glucose_reading_dto.dart';
import 'package:glucocontrol/data/remote/supabase_client_provider.dart';
import 'package:glucocontrol/domain/model/glucose_reading.dart';
import 'package:glucocontrol/domain/repository/glucose_repository.dart';

class GlucoseRepositoryImpl implements GlucoseRepository {
  // refreshTrigger invalida todos los Streams activos tras cualquier mutación
  final _refresh = StreamController<void>.broadcast();

  void _invalidate() => _refresh.add(null);

  String get _userId =>
      supabaseClient.auth.currentUser?.id ?? (throw Exception('No autenticado'));

  Stream<List<GlucoseReading>> _query(
    int fromEpochDay,
    int toEpochDay,
  ) async* {
    yield await _fetch(fromEpochDay, toEpochDay);
    await for (final _ in _refresh.stream) {
      yield await _fetch(fromEpochDay, toEpochDay);
    }
  }

  Future<List<GlucoseReading>> _fetch(int fromDay, int toDay) async {
    try {
      final rows = await supabaseClient
          .from(kTableGlucoseReadings)
          .select()
          .eq('user_id', _userId)
          .gte('date_epoch_day', fromDay)
          .lte('date_epoch_day', toDay)
          .order('date_epoch_day', ascending: true)
          .order('time_seconds_of_day', ascending: true);

      return (rows as List)
          .map((r) => GlucoseReadingDto.fromJson(r as Map<String, dynamic>).toDomain())
          .toList();
    } catch (_) {
      return [];
    }
  }

  // Usa año/mes/día tal cual (sin .toUtc()) — ver nota en DriftGlucoseRepository.
  int _toEpochDay(DateTime date) =>
      DateTime.utc(date.year, date.month, date.day).millisecondsSinceEpoch ~/
      86400000;

  @override
  Stream<List<GlucoseReading>> getReadingsByDate(DateTime date) {
    final day = _toEpochDay(date);
    return _query(day, day);
  }

  @override
  Stream<List<GlucoseReading>> getReadingsByDateRange(DateTime from, DateTime to) =>
      _query(_toEpochDay(from), _toEpochDay(to));

  @override
  Future<int> insertReading(GlucoseReading reading) async {
    final dto = GlucoseReadingCreateDto.fromDomain(reading, _userId);
    await supabaseClient.from(kTableGlucoseReadings).insert(dto.toJson());
    _invalidate();
    return 0; // Supabase sin Prefer:return=representation no devuelve el id
  }

  @override
  Future<void> updateReading(GlucoseReading reading) async {
    final dto = GlucoseReadingUpdateDto.fromDomain(reading, _userId);
    await supabaseClient
        .from(kTableGlucoseReadings)
        .update(dto.toJson())
        .eq('id', reading.id)
        .eq('user_id', _userId);
    _invalidate();
  }

  @override
  Future<void> deleteReading(int id) async {
    await supabaseClient
        .from(kTableGlucoseReadings)
        .delete()
        .eq('id', id)
        .eq('user_id', _userId);
    _invalidate();
  }

  @override
  Future<GlucoseReading?> getReadingById(int id) async {
    try {
      final rows = await supabaseClient
          .from(kTableGlucoseReadings)
          .select()
          .eq('id', id)
          .eq('user_id', _userId)
          .limit(1);

      final list = rows as List;
      if (list.isEmpty) return null;
      return GlucoseReadingDto.fromJson(list.first as Map<String, dynamic>)
          .toDomain();
    } catch (_) {
      return null;
    }
  }
}
