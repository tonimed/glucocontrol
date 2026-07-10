import 'package:glucocontrol/domain/model/glucose_reading.dart';
import 'package:glucocontrol/domain/repository/glucose_repository.dart';

class UpdateReadingUseCase {
  final GlucoseRepository _repo;
  UpdateReadingUseCase(this._repo);

  static final _minDate = DateTime(2020, 1, 1);

  Future<void> call(GlucoseReading reading) {
    if (reading.id <= 0) throw ArgumentError('ID inválido');
    if (reading.valueMgDl < 1 || reading.valueMgDl > 600) {
      throw ArgumentError('La glucosa debe estar entre 1 y 600 mg/dL');
    }
    final date = DateTime(reading.date.year, reading.date.month, reading.date.day);
    if (date.isBefore(_minDate)) {
      throw ArgumentError('La fecha no puede ser anterior a 2020-01-01');
    }
    return _repo.updateReading(reading);
  }
}
