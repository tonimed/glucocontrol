import 'package:glucocontrol/domain/model/glucose_reading.dart';
import 'package:glucocontrol/domain/repository/glucose_repository.dart';

class GetMonthlyReadingsUseCase {
  final GlucoseRepository _repo;
  GetMonthlyReadingsUseCase(this._repo);

  Stream<List<GlucoseReading>> call(int year, int month) {
    final from = DateTime(year, month, 1);
    final to = DateTime(year, month + 1, 0); // último día del mes
    return _repo.getReadingsByDateRange(from, to);
  }
}
