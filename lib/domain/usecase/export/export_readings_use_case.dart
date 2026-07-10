import 'package:glucocontrol/domain/model/glucose_reading.dart';
import 'package:glucocontrol/domain/repository/glucose_repository.dart';

class ExportReadingsUseCase {
  final GlucoseRepository _repo;
  ExportReadingsUseCase(this._repo);

  Future<List<GlucoseReading>> call(DateTime from, DateTime to) async {
    if (from.isAfter(to)) {
      throw ArgumentError('La fecha de inicio no puede ser posterior a la de fin');
    }
    return await _repo.getReadingsByDateRange(from, to).first;
  }
}
