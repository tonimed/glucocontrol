import 'package:glucocontrol/data/remote/supabase_client_provider.dart';
import 'package:glucocontrol/domain/model/user.dart';
import 'package:glucocontrol/domain/repository/auth_repository.dart';

class AuthRepositoryImpl implements AuthRepository {
  @override
  Future<void> signInWithEmail(String email, String password) async {
    await supabaseClient.auth.signInWithPassword(
      email: email,
      password: password,
    );
  }

  @override
  Future<void> signUpWithEmail(String email, String password) async {
    await supabaseClient.auth.signUp(
      email: email,
      password: password,
    );
  }

  // Google Sign-In: pendiente de activar cuando el Web Client ID esté configurado
  // en Google Play Console. Ver TODO en auth_screen.dart.
  @override
  Future<void> signInWithGoogle() async {
    throw UnimplementedError(
      'Google Sign-In no está configurado todavía. '
      'Activa esta función cuando tengas el Web Client ID de Google Play Console.',
    );
  }

  @override
  Future<void> signOut() async {
    await supabaseClient.auth.signOut();
  }

  @override
  Future<AppUser?> getCurrentUser() async {
    final user = supabaseClient.auth.currentUser;
    if (user == null) return null;
    final meta = user.userMetadata;
    return AppUser(
      id: user.id,
      email: user.email ?? '',
      displayName: meta?['full_name'] as String? ?? meta?['name'] as String?,
    );
  }

  @override
  Future<void> deleteAccount() async {
    // Llama a una Edge Function Supabase con Admin API — el ON DELETE CASCADE
    // elimina todas las lecturas del usuario automáticamente
    await supabaseClient.functions.invoke('delete-user');
    await supabaseClient.auth.signOut();
  }
}
