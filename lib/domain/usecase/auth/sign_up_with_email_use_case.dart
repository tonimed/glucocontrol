import 'package:glucocontrol/domain/repository/auth_repository.dart';

class SignUpWithEmailUseCase {
  final AuthRepository _repo;
  SignUpWithEmailUseCase(this._repo);
  Future<void> call(String email, String password) =>
      _repo.signUpWithEmail(email, password);
}
