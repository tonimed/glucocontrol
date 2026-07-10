import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:glucocontrol/presentation/component/app_top_bar.dart';
import 'package:glucocontrol/presentation/screen/auth/auth_notifier.dart';
import 'package:glucocontrol/presentation/screen/settings/settings_notifier.dart';

class SettingsScreen extends ConsumerWidget {
  const SettingsScreen({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final cs       = Theme.of(context).colorScheme;
    final settings = ref.watch(settingsNotifierProvider);
    final notifier = ref.read(settingsNotifierProvider.notifier);

    return Scaffold(
      appBar: AppTopBar(
        title: 'Ajustes',
        onBack: () => Navigator.of(context).pop(),
      ),
      body: ListView(
        children: [
          // ── Notificaciones ──────────────────────────────────────────────
          _SectionHeader(label: 'Notificaciones'),

          SwitchListTile(
            secondary: const Icon(Icons.notifications_outlined),
            title: const Text('Recordatorio diario'),
            subtitle: const Text('Recordarte registrar tu glucosa cada día'),
            value: settings.reminderEnabled,
            onChanged: settings.isLoading
                ? null
                : (v) => notifier.toggleReminder(v),
          ),

          if (settings.reminderEnabled)
            ListTile(
              leading: const Icon(Icons.schedule_outlined),
              title: const Text('Hora del recordatorio'),
              trailing: Text(
                settings.reminderTime.format(context),
                style: TextStyle(
                  color: cs.primary,
                  fontWeight: FontWeight.w600,
                  fontSize: 15,
                ),
              ),
              onTap: () => _pickTime(context, settings.reminderTime, notifier),
            ),

          if (settings.error != null)
            Padding(
              padding: const EdgeInsets.fromLTRB(16, 0, 16, 8),
              child: Text(
                settings.error!,
                style: TextStyle(color: cs.error, fontSize: 13),
              ),
            ),

          const Divider(),

          // ── Exportación ─────────────────────────────────────────────────
          _SectionHeader(label: 'Exportación'),
          ListTile(
            leading: const Icon(Icons.file_download_outlined),
            title: const Text('Exportar lecturas'),
            subtitle: const Text('CSV o PDF desde la pantalla Historial'),
            onTap: null,
            enabled: false,
          ),

          const Divider(),

          // ── Cuenta ──────────────────────────────────────────────────────
          _SectionHeader(label: 'Cuenta'),
          ListTile(
            leading: const Icon(Icons.logout),
            title: const Text('Cerrar sesión'),
            onTap: () => ref.read(authNotifierProvider.notifier).signOut(),
          ),
          ListTile(
            leading: Icon(Icons.delete_forever_outlined, color: cs.error),
            title: Text('Eliminar cuenta', style: TextStyle(color: cs.error)),
            subtitle: const Text(
              'Borra tu cuenta y todas tus lecturas permanentemente',
            ),
            onTap: () => _confirmDeleteAccount(context, ref),
          ),

          const Divider(),

          // ── Legal ────────────────────────────────────────────────────────
          _SectionHeader(label: 'Legal'),
          ListTile(
            leading: const Icon(Icons.privacy_tip_outlined),
            title: const Text('Política de privacidad'),
            onTap: () {
              // TODO: abrir URL de GitHub Pages con la política de privacidad
            },
          ),
          ListTile(
            leading: const Icon(Icons.info_outline),
            title: const Text('Versión'),
            trailing: const Text('2.5.0', style: TextStyle(fontSize: 13)),
          ),
        ],
      ),
    );
  }

  Future<void> _pickTime(
    BuildContext context,
    TimeOfDay current,
    SettingsNotifier notifier,
  ) async {
    final picked = await showTimePicker(
      context: context,
      initialTime: current,
      helpText: 'Hora del recordatorio',
    );
    if (picked != null) {
      await notifier.setReminderTime(picked);
    }
  }

  Future<void> _confirmDeleteAccount(BuildContext context, WidgetRef ref) async {
    final confirmed = await showDialog<bool>(
      context: context,
      builder: (_) => AlertDialog(
        title: const Text('Eliminar cuenta'),
        content: const Text(
          'Se borrarán tu cuenta y TODAS tus lecturas de forma permanente. '
          'Esta acción no se puede deshacer.',
        ),
        actions: [
          TextButton(
            onPressed: () => Navigator.pop(context, false),
            child: const Text('Cancelar'),
          ),
          FilledButton(
            style: FilledButton.styleFrom(
              backgroundColor: Theme.of(context).colorScheme.error,
            ),
            onPressed: () => Navigator.pop(context, true),
            child: const Text('Eliminar cuenta'),
          ),
        ],
      ),
    );

    if (confirmed == true && context.mounted) {
      // Pendiente: conectar con DeleteAccountUseCase cuando la Edge Function
      // delete-user esté desplegada en Supabase
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(
          content: Text(
            'Eliminación de cuenta pendiente de configurar en Supabase.',
          ),
        ),
      );
    }
  }
}

class _SectionHeader extends StatelessWidget {
  final String label;
  const _SectionHeader({required this.label});

  @override
  Widget build(BuildContext context) {
    return Padding(
      padding: const EdgeInsets.fromLTRB(16, 16, 16, 4),
      child: Text(
        label.toUpperCase(),
        style: TextStyle(
          fontSize: 11,
          fontWeight: FontWeight.w700,
          letterSpacing: 0.8,
          color: Theme.of(context).colorScheme.primary,
        ),
      ),
    );
  }
}
