import 'package:glucocontrol/domain/model/user.dart';

abstract class AuthRepository {
  Future<void> signInWithEmail(String email, String password);
  Future<void> signUpWithEmail(String email, String password);
  Future<void> signInWithGoogle();
  Future<void> signOut();
  Future<AppUser?> getCurrentUser();
  Future<void> deleteAccount();
}
