import 'package:glucocontrol/domain/repository/auth_repository.dart';

class SignInWithEmailUseCase {
  final AuthRepository _repo;
  SignInWithEmailUseCase(this._repo);
  Future<void> call(String email, String password) =>
      _repo.signInWithEmail(email, password);
}
