import 'dart:async';

import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';
import 'package:glucocontrol/presentation/navigation/app_routes.dart';
import 'package:glucocontrol/presentation/screen/auth/auth_screen.dart';
import 'package:glucocontrol/presentation/screen/home/home_screen.dart';
import 'package:glucocontrol/presentation/screen/history/history_screen.dart';
import 'package:glucocontrol/presentation/screen/chart/chart_screen.dart';
import 'package:glucocontrol/presentation/screen/add_edit_reading/add_edit_reading_screen.dart';
import 'package:glucocontrol/presentation/screen/reading_detail/reading_detail_screen.dart';
import 'package:glucocontrol/presentation/navigation/app_shell.dart';
import 'package:glucocontrol/presentation/screen/settings/settings_screen.dart';
import 'package:supabase_flutter/supabase_flutter.dart';

/// Listenable que notifica al router cada vez que cambia el estado de auth
/// en Supabase, forzando la re-evaluación del redirect sin esperar a una
/// navegación manual (evita bucle auth→home→auth con estado inconsistente).
class _GoRouterRefreshStream extends ChangeNotifier {
  late final StreamSubscription<AuthState> _sub;

  _GoRouterRefreshStream() {
    _sub = Supabase.instance.client.auth.onAuthStateChange
        .listen((_) => notifyListeners());
  }

  @override
  void dispose() {
    _sub.cancel();
    super.dispose();
  }
}

final routerProvider = Provider<GoRouter>((ref) {
  final refresh = _GoRouterRefreshStream();
  ref.onDispose(refresh.dispose);

  return GoRouter(
    initialLocation: AppRoutes.auth,
    refreshListenable: refresh,
    redirect: (context, state) {
      final isAuthenticated =
          Supabase.instance.client.auth.currentUser != null;
      final isGoingToAuth = state.matchedLocation == AppRoutes.auth;

      if (!isAuthenticated && !isGoingToAuth) return AppRoutes.auth;
      if (isAuthenticated && isGoingToAuth) return AppRoutes.home;
      return null;
    },
    routes: [
      GoRoute(
        path: AppRoutes.auth,
        builder: (_, _) => const AuthScreen(),
      ),
      ShellRoute(
        builder: (context, state, child) => AppShell(child: child),
        routes: [
          GoRoute(
            path: AppRoutes.home,
            builder: (_, _) => const HomeScreen(),
          ),
          GoRoute(
            path: AppRoutes.history,
            builder: (_, _) => const HistoryScreen(),
          ),
          GoRoute(
            path: AppRoutes.chart,
            builder: (context, state) {
              final period = state.pathParameters['period'] ?? 'WEEKLY';
              return ChartScreen(initialPeriod: period);
            },
          ),
        ],
      ),
      GoRoute(
        path: AppRoutes.addEditReading,
        builder: (context, state) {
          final id = int.tryParse(
                state.pathParameters['readingId'] ?? '-1',
              ) ??
              -1;
          return AddEditReadingScreen(readingId: id);
        },
      ),
      GoRoute(
        path: AppRoutes.readingDetail,
        builder: (context, state) {
          final id = int.parse(state.pathParameters['readingId']!);
          return ReadingDetailScreen(readingId: id);
        },
      ),
      GoRoute(
        path: AppRoutes.settings,
        builder: (_, _) => const SettingsScreen(),
      ),
    ],
    errorBuilder: (context, state) => Scaffold(
      body: Center(child: Text('Ruta no encontrada: ${state.error}')),
    ),
  );
});
