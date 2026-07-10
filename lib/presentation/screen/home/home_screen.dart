import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';
import 'package:glucocontrol/presentation/component/app_top_bar.dart';
import 'package:glucocontrol/presentation/navigation/app_routes.dart';
import 'package:glucocontrol/presentation/screen/auth/auth_notifier.dart';
import 'package:glucocontrol/presentation/screen/home/home_notifier.dart';
import 'package:intl/intl.dart';

class HomeScreen extends ConsumerWidget {
  const HomeScreen({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final state  = ref.watch(homeNotifierProvider);
    final cs     = Theme.of(context).colorScheme;
    final today  = DateFormat('EEEE, d MMMM', 'es').format(DateTime.now());

    return Scaffold(
      appBar: AppTopBar(
        title: 'Control de Glucosa',
        actions: [
          IconButton(
            icon: const Icon(Icons.settings_outlined),
            tooltip: 'Ajustes',
            onPressed: () => context.push(AppRoutes.settings),
          ),
          IconButton(
            icon: const Icon(Icons.logout),
            tooltip: 'Cerrar sesión',
            onPressed: () => ref.read(authNotifierProvider.notifier).signOut(),
          ),
        ],
      ),
      floatingActionButton: FloatingActionButton.extended(
        onPressed: () => context.push(AppRoutes.addReadingPath()),
        icon: const Icon(Icons.add),
        label: const Text('Nueva lectura'),
      ),
      body: state.isLoading
          ? const Center(child: CircularProgressIndicator())
          : ListView(
              padding: const EdgeInsets.all(16),
              children: [
                // Tarjeta resumen del día
                Card(
                  color: cs.primaryContainer,
                  child: Padding(
                    padding: const EdgeInsets.all(20),
                    child: Column(
                      crossAxisAlignment: CrossAxisAlignment.start,
                      children: [
                        Text(
                          today,
                          style: TextStyle(
                            color: cs.onPrimaryContainer,
                            fontSize: 13,
                            fontWeight: FontWeight.w600,
                          ),
                        ),
                        const SizedBox(height: 12),
                        if (state.count == 0)
                          Text(
                            'Sin lecturas hoy',
                            style: TextStyle(
                              color: cs.onPrimaryContainer,
                              fontSize: 16,
                            ),
                          )
                        else
                          Row(
                            children: [
                              _StatPill(
                                label: 'Lecturas',
                                value: '${state.count}',
                                cs: cs,
                              ),
                              const SizedBox(width: 10),
                              _StatPill(
                                label: 'Media',
                                value: '${state.average!.round()} mg/dL',
                                cs: cs,
                              ),
                              const SizedBox(width: 10),
                              _StatPill(
                                label: 'Última',
                                value: '${state.last} mg/dL',
                                cs: cs,
                              ),
                            ],
                          ),
                        if (state.error != null) ...[
                          const SizedBox(height: 8),
                          Text(
                            state.error!,
                            style: TextStyle(
                              color: cs.error,
                              fontSize: 12,
                            ),
                          ),
                        ],
                      ],
                    ),
                  ),
                ),
                const SizedBox(height: 20),

                Text(
                  '¿Qué quieres hacer?',
                  style: TextStyle(
                    fontSize: 16,
                    fontWeight: FontWeight.w700,
                    color: cs.onSurface,
                  ),
                ),
                const SizedBox(height: 12),

                // Acciones rápidas
                Row(
                  children: [
                    Expanded(
                      child: _ActionCard(
                        icon: Icons.edit_note,
                        label: 'Historial',
                        onTap: () => context.go(AppRoutes.history),
                      ),
                    ),
                    const SizedBox(width: 12),
                    Expanded(
                      child: _ActionCard(
                        icon: Icons.show_chart,
                        label: 'Gráfica',
                        onTap: () => context.push(AppRoutes.chartPath('WEEKLY')),
                      ),
                    ),
                  ],
                ),
                const SizedBox(height: 80), // espacio para el FAB
              ],
            ),
    );
  }
}

class _StatPill extends StatelessWidget {
  final String label;
  final String value;
  final ColorScheme cs;

  const _StatPill({required this.label, required this.value, required this.cs});

  @override
  Widget build(BuildContext context) {
    return Container(
      padding: const EdgeInsets.symmetric(horizontal: 10, vertical: 6),
      decoration: BoxDecoration(
        color: cs.onPrimaryContainer.withValues(alpha: 0.12),
        borderRadius: BorderRadius.circular(8),
      ),
      child: Column(
        children: [
          Text(
            value,
            style: TextStyle(
              color: cs.onPrimaryContainer,
              fontWeight: FontWeight.w700,
              fontSize: 14,
              fontFeatures: const [FontFeature.tabularFigures()],
            ),
          ),
          Text(
            label,
            style: TextStyle(
              color: cs.onPrimaryContainer.withValues(alpha: 0.7),
              fontSize: 10,
            ),
          ),
        ],
      ),
    );
  }
}

class _ActionCard extends StatelessWidget {
  final IconData icon;
  final String label;
  final VoidCallback onTap;

  const _ActionCard({
    required this.icon,
    required this.label,
    required this.onTap,
  });

  @override
  Widget build(BuildContext context) {
    return Card(
      child: InkWell(
        onTap: onTap,
        borderRadius: BorderRadius.circular(12),
        child: Padding(
          padding: const EdgeInsets.symmetric(vertical: 20, horizontal: 16),
          child: Row(
            children: [
              Icon(icon, color: Theme.of(context).colorScheme.primary),
              const SizedBox(width: 10),
              Text(
                label,
                style: const TextStyle(
                  fontWeight: FontWeight.w600,
                  fontSize: 15,
                ),
              ),
            ],
          ),
        ),
      ),
    );
  }
}
