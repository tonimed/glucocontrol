import 'package:glucocontrol/domain/model/glucose_reading.dart';
import 'package:glucocontrol/domain/repository/glucose_repository.dart';

class GetReadingsByDateRangeUseCase {
  final GlucoseRepository _repo;
  GetReadingsByDateRangeUseCase(this._repo);

  Stream<List<GlucoseReading>> call(DateTime from, DateTime to) {
    if (from.isAfter(to)) {
      throw ArgumentError('La fecha de inicio no puede ser posterior a la de fin');
    }
    return _repo.getReadingsByDateRange(from, to);
  }
}
