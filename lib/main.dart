import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:glucocontrol/core/theme/app_theme.dart';
import 'package:glucocontrol/data/notification/notification_service.dart';
import 'package:glucocontrol/presentation/navigation/app_router.dart';
import 'package:glucocontrol/presentation/provider/providers.dart';
import 'package:intl/date_symbol_data_local.dart';
import 'package:supabase_flutter/supabase_flutter.dart';

// Credenciales inyectadas con --dart-define en tiempo de compilación.
// Ejemplo: flutter run --dart-define=SUPABASE_URL=https://xxx.supabase.co
//                      --dart-define=SUPABASE_ANON_KEY=eyJxxx
const _supabaseUrl     = String.fromEnvironment('SUPABASE_URL');
const _supabaseAnonKey = String.fromEnvironment('SUPABASE_ANON_KEY');

Future<void> main() async {
  WidgetsFlutterBinding.ensureInitialized();

  // Locale español para intl (fechas)
  await initializeDateFormatting('es', null);

  await Supabase.initialize(
    url: _supabaseUrl,
    anonKey: _supabaseAnonKey, // ignore: deprecated_member_use — publishableKey no existe en esta versión del SDK
  );

  // Inicializar plugin de notificaciones antes de runApp
  await NotificationService().initialize();

  runApp(
    const ProviderScope(
      child: GlucoControlApp(),
    ),
  );
}

class GlucoControlApp extends ConsumerStatefulWidget {
  const GlucoControlApp({super.key});

  @override
  ConsumerState<GlucoControlApp> createState() => _GlucoControlAppState();
}

class _GlucoControlAppState extends ConsumerState<GlucoControlApp> {
  @override
  void initState() {
    super.initState();
    // Arranca el SyncManager: escucha conectividad y sincroniza al reconectar
    ref.read(syncManagerProvider).start();
  }

  @override
  void dispose() {
    ref.read(syncManagerProvider).dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    final router = ref.watch(routerProvider);

    return MaterialApp.router(
      title: 'Control de Glucosa',
      debugShowCheckedModeBanner: false,
      theme: AppTheme.light,
      darkTheme: AppTheme.dark,
      themeMode: ThemeMode.system,
      routerConfig: router,
    );
  }
}
