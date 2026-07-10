import 'package:glucocontrol/domain/model/glucose_reading.dart';
import 'package:glucocontrol/domain/repository/glucose_repository.dart';

class GetReadingByIdUseCase {
  final GlucoseRepository _repo;
  GetReadingByIdUseCase(this._repo);

  Future<GlucoseReading?> call(int id) {
    if (id <= 0) throw ArgumentError('ID inválido');
    return _repo.getReadingById(id);
  }
}
