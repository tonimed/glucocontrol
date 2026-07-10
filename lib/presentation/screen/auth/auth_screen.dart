import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';
import 'package:glucocontrol/presentation/navigation/app_routes.dart';
import 'package:glucocontrol/presentation/screen/auth/auth_notifier.dart';

class AuthScreen extends ConsumerStatefulWidget {
  const AuthScreen({super.key});

  @override
  ConsumerState<AuthScreen> createState() => _AuthScreenState();
}

class _AuthScreenState extends ConsumerState<AuthScreen>
    with SingleTickerProviderStateMixin {
  late final TabController _tabs;
  final _emailCtrl    = TextEditingController();
  final _passwordCtrl = TextEditingController();
  bool _obscure = true;

  @override
  void initState() {
    super.initState();
    _tabs = TabController(length: 2, vsync: this);
  }

  @override
  void dispose() {
    _tabs.dispose();
    _emailCtrl.dispose();
    _passwordCtrl.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    final authState = ref.watch(authNotifierProvider);
    final notifier  = ref.read(authNotifierProvider.notifier);
    final cs        = Theme.of(context).colorScheme;

    // Navegar a Home cuando el usuario se autentica
    ref.listen<AuthState>(authNotifierProvider, (_, next) {
      if (next.isAuthenticated) context.go(AppRoutes.home);
    });

    return Scaffold(
      body: SafeArea(
        child: SingleChildScrollView(
          padding: const EdgeInsets.symmetric(horizontal: 24, vertical: 32),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.stretch,
            children: [
              const SizedBox(height: 16),

              // Logo + título
              Icon(Icons.water_drop_rounded, size: 72, color: cs.primary),
              const SizedBox(height: 12),
              Text(
                'Control de Glucosa',
                textAlign: TextAlign.center,
                style: TextStyle(
                  fontSize: 26,
                  fontWeight: FontWeight.w800,
                  color: cs.onSurface,
                ),
              ),
              const SizedBox(height: 8),
              Text(
                'Registra y monitoriza tu glucosa en sangre',
                textAlign: TextAlign.center,
                style: TextStyle(color: cs.onSurfaceVariant, fontSize: 14),
              ),
              const SizedBox(height: 32),

              // Tabs
              TabBar(
                controller: _tabs,
                tabs: const [
                  Tab(text: 'Iniciar sesión'),
                  Tab(text: 'Registrarse'),
                ],
              ),
              const SizedBox(height: 24),

              // Campos
              TextField(
                controller: _emailCtrl,
                decoration: const InputDecoration(
                  labelText: 'Email',
                  prefixIcon: Icon(Icons.email_outlined),
                ),
                keyboardType: TextInputType.emailAddress,
                textInputAction: TextInputAction.next,
              ),
              const SizedBox(height: 12),
              TextField(
                controller: _passwordCtrl,
                decoration: InputDecoration(
                  labelText: 'Contraseña',
                  prefixIcon: const Icon(Icons.lock_outline),
                  suffixIcon: IconButton(
                    icon: Icon(
                      _obscure ? Icons.visibility_outlined : Icons.visibility_off_outlined,
                    ),
                    onPressed: () => setState(() => _obscure = !_obscure),
                  ),
                ),
                obscureText: _obscure,
                textInputAction: TextInputAction.done,
                onSubmitted: (_) => _submit(notifier),
              ),
              const SizedBox(height: 20),

              // Botón principal
              AnimatedBuilder(
                animation: _tabs,
                builder: (_, _) => FilledButton(
                  onPressed: authState.isLoading ? null : () => _submit(notifier),
                  child: authState.isLoading
                      ? const SizedBox(
                          height: 20,
                          width: 20,
                          child: CircularProgressIndicator(strokeWidth: 2),
                        )
                      : Text(_tabs.index == 0 ? 'Entrar' : 'Crear cuenta'),
                ),
              ),
              const SizedBox(height: 12),

              // Google Sign-In — preparado, no operativo aún
              OutlinedButton.icon(
                onPressed: () => notifier.signInWithGoogle(),
                icon: const Icon(Icons.g_mobiledata, size: 22),
                label: const Text('Continuar con Google'),
              ),

              // Mensajes de error / info
              if (authState.error != null) ...[
                const SizedBox(height: 16),
                _MessageBanner(
                  message: authState.error!,
                  isError: true,
                  colorScheme: cs,
                ),
              ],
              if (authState.info != null) ...[
                const SizedBox(height: 16),
                _MessageBanner(
                  message: authState.info!,
                  isError: false,
                  colorScheme: cs,
                ),
              ],
            ],
          ),
        ),
      ),
    );
  }

  void _submit(AuthNotifier notifier) {
    final email    = _emailCtrl.text.trim();
    final password = _passwordCtrl.text;
    if (_tabs.index == 0) {
      notifier.signIn(email, password);
    } else {
      notifier.signUp(email, password);
    }
  }
}

class _MessageBanner extends StatelessWidget {
  final String message;
  final bool isError;
  final ColorScheme colorScheme;

  const _MessageBanner({
    required this.message,
    required this.isError,
    required this.colorScheme,
  });

  @override
  Widget build(BuildContext context) {
    return Container(
      padding: const EdgeInsets.symmetric(horizontal: 14, vertical: 10),
      decoration: BoxDecoration(
        color: isError
            ? colorScheme.errorContainer
            : colorScheme.primaryContainer,
        borderRadius: BorderRadius.circular(8),
      ),
      child: Text(
        message,
        style: TextStyle(
          color: isError
              ? colorScheme.onErrorContainer
              : colorScheme.onPrimaryContainer,
          fontSize: 13,
        ),
      ),
    );
  }
}
