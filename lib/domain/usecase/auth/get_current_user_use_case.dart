import 'package:glucocontrol/domain/model/user.dart';
import 'package:glucocontrol/domain/repository/auth_repository.dart';

class GetCurrentUserUseCase {
  final AuthRepository _repo;
  GetCurrentUserUseCase(this._repo);
  Future<AppUser?> call() => _repo.getCurrentUser();
}
