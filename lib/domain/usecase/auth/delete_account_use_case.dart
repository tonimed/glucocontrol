import 'package:glucocontrol/domain/repository/auth_repository.dart';

class DeleteAccountUseCase {
  final AuthRepository _repo;
  DeleteAccountUseCase(this._repo);
  Future<void> call() => _repo.deleteAccount();
}
