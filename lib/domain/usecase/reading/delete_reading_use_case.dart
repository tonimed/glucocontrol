import 'package:glucocontrol/domain/repository/glucose_repository.dart';

class DeleteReadingUseCase {
  final GlucoseRepository _repo;
  DeleteReadingUseCase(this._repo);

  Future<void> call(int id) async {
    if (id <= 0) throw ArgumentError('ID inválido');
    return _repo.deleteReading(id);
  }
}
