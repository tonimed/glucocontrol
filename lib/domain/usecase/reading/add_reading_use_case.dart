import 'package:glucocontrol/domain/model/glucose_reading.dart';
import 'package:glucocontrol/domain/repository/glucose_repository.dart';

class AddReadingUseCase {
  final GlucoseRepository _repo;
  AddReadingUseCase(this._repo);

  static final _minDate = DateTime(2020, 1, 1);

  Future<int> call(GlucoseReading reading) {
    if (reading.valueMgDl < 1 || reading.valueMgDl > 600) {
      throw ArgumentError('La glucosa debe estar entre 1 y 600 mg/dL');
    }
    final today = DateTime.now();
    final date = DateTime(reading.date.year, reading.date.month, reading.date.day);
    final todayDate = DateTime(today.year, today.month, today.day);
    if (date.isBefore(_minDate)) {
      throw ArgumentError('La fecha no puede ser anterior a 2020-01-01');
    }
    if (date.isAfter(todayDate)) {
      throw ArgumentError('La fecha no puede ser futura');
    }
    return _repo.insertReading(reading);
  }
}
