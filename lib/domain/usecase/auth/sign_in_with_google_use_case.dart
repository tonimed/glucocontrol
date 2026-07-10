import 'package:glucocontrol/domain/repository/auth_repository.dart';

class SignInWithGoogleUseCase {
  final AuthRepository _repo;
  SignInWithGoogleUseCase(this._repo);
  Future<void> call() => _repo.signInWithGoogle();
}
