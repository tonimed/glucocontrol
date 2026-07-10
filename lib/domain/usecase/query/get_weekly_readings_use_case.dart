import 'package:glucocontrol/domain/model/glucose_reading.dart';
import 'package:glucocontrol/domain/repository/glucose_repository.dart';

class GetWeeklyReadingsUseCase {
  final GlucoseRepository _repo;
  GetWeeklyReadingsUseCase(this._repo);

  // Devuelve lecturas de lunes a domingo de la semana que contiene [dateInWeek]
  Stream<List<GlucoseReading>> call(DateTime dateInWeek) {
    final monday = dateInWeek.subtract(
      Duration(days: dateInWeek.weekday - 1),
    );
    final from = DateTime(monday.year, monday.month, monday.day);
    final to = from.add(const Duration(days: 6));
    return _repo.getReadingsByDateRange(from, to);
  }
}
