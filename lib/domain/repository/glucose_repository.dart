import 'package:glucocontrol/domain/model/glucose_reading.dart';

abstract class GlucoseRepository {
  Stream<List<GlucoseReading>> getReadingsByDate(DateTime date);
  Stream<List<GlucoseReading>> getReadingsByDateRange(DateTime from, DateTime to);
  Future<int> insertReading(GlucoseReading reading);
  Future<void> updateReading(GlucoseReading reading);
  Future<void> deleteReading(int id);
  Future<GlucoseReading?> getReadingById(int id);
}
