import 'package:glucocontrol/domain/repository/auth_repository.dart';

class SignOutUseCase {
  final AuthRepository _repo;
  SignOutUseCase(this._repo);
  Future<void> call() => _repo.signOut();
}
