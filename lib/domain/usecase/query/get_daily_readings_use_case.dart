import 'package:glucocontrol/domain/model/glucose_reading.dart';
import 'package:glucocontrol/domain/repository/glucose_repository.dart';

class GetDailyReadingsUseCase {
  final GlucoseRepository _repo;
  GetDailyReadingsUseCase(this._repo);

  Stream<List<GlucoseReading>> call(DateTime date) =>
      _repo.getReadingsByDate(date);
}
