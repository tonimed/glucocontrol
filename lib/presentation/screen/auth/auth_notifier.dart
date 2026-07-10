import 'dart:async';

import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:supabase_flutter/supabase_flutter.dart' as supa;
import 'package:glucocontrol/presentation/provider/providers.dart';

class AuthState {
  final bool isLoading;
  final bool isAuthenticated;
  final String? error;
  final String? info;

  const AuthState({
    this.isLoading = false,
    this.isAuthenticated = false,
    this.error,
    this.info,
  });

  AuthState copyWith({
    bool? isLoading,
    bool? isAuthenticated,
    String? error,
    bool clearError = false,
    String? info,
    bool clearInfo = false,
  }) =>
      AuthState(
        isLoading: isLoading ?? this.isLoading,
        isAuthenticated: isAuthenticated ?? this.isAuthenticated,
        error: clearError ? null : (error ?? this.error),
        info: clearInfo ? null : (info ?? this.info),
      );
}

class AuthNotifier extends StateNotifier<AuthState> {
  final Ref _ref;
  StreamSubscription<supa.AuthState>? _authSub;

  AuthNotifier(this._ref) : super(const AuthState()) {
    _checkCurrentUser();
    // Mantiene Riverpod en sync con Supabase: si la sesión expira o se
    // revoca en otro dispositivo, resetea el estado local sin bucle de
    // redirección (el GoRouter reacciona vía refreshListenable).
    _authSub = supa.Supabase.instance.client.auth.onAuthStateChange.listen(
      (data) {
        if (data.event == supa.AuthChangeEvent.signedOut) {
          state = const AuthState();
        }
      },
    );
  }

  @override
  void dispose() {
    _authSub?.cancel();
    super.dispose();
  }

  Future<void> _checkCurrentUser() async {
    state = state.copyWith(isLoading: true);
    final user = await _ref.read(getCurrentUserProvider).call();
    state = state.copyWith(isLoading: false, isAuthenticated: user != null);
  }

  Future<void> signIn(String email, String password) async {
    state = state.copyWith(isLoading: true, clearError: true, clearInfo: true);
    try {
      await _ref.read(signInWithEmailProvider).call(email, password);
      state = state.copyWith(isLoading: false, isAuthenticated: true);
      // Sincroniza en background sin bloquear la navegación
      _ref.read(syncManagerProvider).syncNow();
    } catch (e) {
      state = state.copyWith(
        isLoading: false,
        error: _translate(e.toString()),
      );
    }
  }

  Future<void> signUp(String email, String password) async {
    state = state.copyWith(isLoading: true, clearError: true, clearInfo: true);
    try {
      await _ref.read(signUpWithEmailProvider).call(email, password);
      final user = await _ref.read(getCurrentUserProvider).call();
      if (user != null) {
        state = state.copyWith(isLoading: false, isAuthenticated: true);
        _ref.read(syncManagerProvider).syncNow();
      } else {
        // Supabase requiere confirmación de email antes de crear sesión
        state = state.copyWith(
          isLoading: false,
          info: 'Revisa tu email para confirmar el registro.',
        );
      }
    } catch (e) {
      state = state.copyWith(
        isLoading: false,
        error: _translate(e.toString()),
      );
    }
  }

  // Google Sign-In preparado — no operativo hasta tener Web Client ID
  Future<void> signInWithGoogle() async {
    state = state.copyWith(
      error: 'Google Sign-In estará disponible próximamente.',
    );
  }

  Future<void> signOut() async {
    await _ref.read(signOutProvider).call();
    state = const AuthState();
  }

  String _translate(String raw) {
    final msg = raw.toLowerCase();
    if (msg.contains('invalid login credentials')) return 'Email o contraseña incorrectos.';
    if (msg.contains('email rate limit')) return 'Demasiados intentos. Espera unos minutos.';
    if (msg.contains('user already registered')) return 'Ya existe una cuenta con ese email.';
    if (msg.contains('network')) return 'Sin conexión. Comprueba tu red.';
    return 'Error inesperado. Inténtalo de nuevo.';
  }
}

final authNotifierProvider =
    StateNotifierProvider<AuthNotifier, AuthState>(AuthNotifier.new);
