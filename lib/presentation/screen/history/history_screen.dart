import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';
import 'package:glucocontrol/data/export/export_service.dart';
import 'package:glucocontrol/presentation/component/app_top_bar.dart';
import 'package:glucocontrol/presentation/component/glucose_reading_card.dart';
import 'package:glucocontrol/presentation/navigation/app_routes.dart';
import 'package:glucocontrol/presentation/screen/history/history_notifier.dart';
import 'package:glucocontrol/presentation/provider/providers.dart';
import 'package:intl/intl.dart';

class HistoryScreen extends ConsumerWidget {
  const HistoryScreen({super.key});

  void _showExportSheet(BuildContext context, HistoryNotifier notifier) {
    showModalBottomSheet<void>(
      context: context,
      builder: (_) => SafeArea(
        child: Padding(
          padding: const EdgeInsets.fromLTRB(24, 20, 24, 24),
          child: Column(
            mainAxisSize: MainAxisSize.min,
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              const Text(
                'Exportar periodo actual',
                style: TextStyle(fontSize: 16, fontWeight: FontWeight.w700),
              ),
              const SizedBox(height: 4),
              Text(
                'Se exportarán las lecturas del periodo seleccionado.',
                style: TextStyle(
                  fontSize: 13,
                  color: Theme.of(context).colorScheme.onSurfaceVariant,
                ),
              ),
              const SizedBox(height: 20),
              Row(
                children: [
                  Expanded(
                    child: OutlinedButton.icon(
                      icon: const Icon(Icons.table_chart_outlined),
                      label: const Text('CSV'),
                      onPressed: () {
                        Navigator.pop(context);
                        notifier.export(ExportFormat.csv);
                      },
                    ),
                  ),
                  const SizedBox(width: 12),
                  Expanded(
                    child: FilledButton.icon(
                      icon: const Icon(Icons.picture_as_pdf_outlined),
                      label: const Text('PDF'),
                      onPressed: () {
                        Navigator.pop(context);
                        notifier.export(ExportFormat.pdf);
                      },
                    ),
                  ),
                ],
              ),
            ],
          ),
        ),
      ),
    );
  }

  String _periodLabel(HistoryState s) {
    final ref = s.referenceDate;
    return switch (s.period) {
      HistoryPeriod.daily =>
        DateFormat('d MMM yyyy', 'es').format(ref),
      HistoryPeriod.weekly => () {
          final mon = ref.subtract(Duration(days: ref.weekday - 1));
          final sun = mon.add(const Duration(days: 6));
          return '${DateFormat('d MMM', 'es').format(mon)} – '
              '${DateFormat('d MMM', 'es').format(sun)}';
        }(),
      HistoryPeriod.monthly =>
        DateFormat('MMMM yyyy', 'es').format(ref),
    };
  }

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final state    = ref.watch(historyNotifierProvider);
    final notifier = ref.read(historyNotifierProvider.notifier);
    final delUC    = ref.read(deleteReadingProvider);

    return Scaffold(
      appBar: AppTopBar(
        title: 'Historial',
        actions: [
          if (state.isExporting)
            const Padding(
              padding: EdgeInsets.all(14),
              child: SizedBox(
                width: 20,
                height: 20,
                child: CircularProgressIndicator(strokeWidth: 2),
              ),
            )
          else
            IconButton(
              icon: const Icon(Icons.share_outlined),
              tooltip: 'Exportar',
              onPressed: () => _showExportSheet(context, notifier),
            ),
        ],
      ),
      body: Column(
        children: [
          // Filtros de periodo
          Padding(
            padding: const EdgeInsets.fromLTRB(16, 8, 16, 0),
            child: Row(
              children: HistoryPeriod.values.map((p) {
                final label = switch (p) {
                  HistoryPeriod.daily   => 'Día',
                  HistoryPeriod.weekly  => 'Semana',
                  HistoryPeriod.monthly => 'Mes',
                };
                return Padding(
                  padding: const EdgeInsets.only(right: 8),
                  child: FilterChip(
                    label: Text(label),
                    selected: state.period == p,
                    onSelected: (_) => notifier.setPeriod(p),
                  ),
                );
              }).toList(),
            ),
          ),

          // Navegador de periodo
          Padding(
            padding: const EdgeInsets.symmetric(horizontal: 8),
            child: Row(
              children: [
                IconButton(
                  icon: const Icon(Icons.chevron_left),
                  onPressed: notifier.goBack,
                ),
                Expanded(
                  child: Text(
                    _periodLabel(state),
                    textAlign: TextAlign.center,
                    style: const TextStyle(fontWeight: FontWeight.w600),
                  ),
                ),
                IconButton(
                  icon: const Icon(Icons.chevron_right),
                  onPressed: notifier.goForward,
                ),
              ],
            ),
          ),

          // Estadísticas del periodo
          if (!state.isLoading && state.totalReadings > 0)
            Padding(
              padding: const EdgeInsets.fromLTRB(16, 0, 16, 8),
              child: Wrap(
                spacing: 8,
                children: [
                  Chip(label: Text('${state.totalReadings} lecturas')),
                  if (state.average != null)
                    Chip(label: Text('Media: ${state.average!.round()} mg/dL')),
                  if (state.inRangePct != null)
                    Chip(label: Text('En rango: ${state.inRangePct!.round()}%')),
                ],
              ),
            ),

          // Banner de error de exportación
          if (state.error != null)
            Padding(
              padding: const EdgeInsets.fromLTRB(16, 4, 16, 0),
              child: MaterialBanner(
                content: Text(state.error!),
                actions: [
                  TextButton(
                    onPressed: () => notifier.clearError(),
                    child: const Text('Cerrar'),
                  ),
                ],
              ),
            ),

          const Divider(height: 1),

          // Lista agrupada por fecha
          Expanded(
            child: state.isLoading
                ? const Center(child: CircularProgressIndicator())
                : state.groupedReadings.isEmpty
                    ? const Center(child: Text('Sin lecturas en este periodo'))
                    : ListView.builder(
                        padding: const EdgeInsets.only(top: 8, bottom: 16),
                        itemCount: state.groupedReadings.length,
                        itemBuilder: (context, i) {
                          final entry    = state.groupedReadings.entries.elementAt(i);
                          final date     = entry.key;
                          final readings = entry.value;
                          final dayAvg   = readings
                                  .map((r) => r.valueMgDl)
                                  .reduce((a, b) => a + b) /
                              readings.length;

                          return Column(
                            crossAxisAlignment: CrossAxisAlignment.start,
                            children: [
                              Padding(
                                padding: const EdgeInsets.fromLTRB(16, 10, 16, 4),
                                child: Row(
                                  children: [
                                    Text(
                                      DateFormat('EEEE, d MMM', 'es').format(date),
                                      style: const TextStyle(
                                        fontWeight: FontWeight.w700,
                                        fontSize: 13,
                                      ),
                                    ),
                                    const Spacer(),
                                    Text(
                                      'Media: ${dayAvg.round()} mg/dL',
                                      style: TextStyle(
                                        fontSize: 12,
                                        color: Theme.of(context)
                                            .colorScheme
                                            .onSurfaceVariant,
                                      ),
                                    ),
                                  ],
                                ),
                              ),
                              ...readings.map(
                                (r) => Padding(
                                  padding: const EdgeInsets.symmetric(
                                    horizontal: 12,
                                    vertical: 2,
                                  ),
                                  child: GlucoseReadingCard(
                                    reading: r,
                                    glucoseRange: state.glucoseRange,
                                    onTap: () => context.push(
                                      AppRoutes.readingDetailPath(r.id),
                                    ),
                                    onEdit: () => context.push(
                                      AppRoutes.editReadingPath(r.id),
                                    ),
                                    onDelete: () async {
                                      final ok = await showDialog<bool>(
                                        context: context,
                                        builder: (dialogContext) => AlertDialog(
                                          title: const Text('Eliminar lectura'),
                                          content: const Text(
                                            '¿Eliminar esta lectura? No se puede deshacer.',
                                          ),
                                          actions: [
                                            TextButton(
                                              onPressed: () => Navigator.pop(
                                                  dialogContext, false),
                                              child: const Text('Cancelar'),
                                            ),
                                            FilledButton(
                                              onPressed: () => Navigator.pop(
                                                  dialogContext, true),
                                              child: const Text('Eliminar'),
                                            ),
                                          ],
                                        ),
                                      );
                                      if (ok != true) return;
                                      try {
                                        await delUC.call(r.id);
                                      } catch (e) {
                                        if (!context.mounted) return;
                                        ScaffoldMessenger.of(context)
                                            .showSnackBar(
                                          SnackBar(
                                            content: Text(
                                              'Error al eliminar: $e',
                                            ),
                                          ),
                                        );
                                      }
                                    },
                                  ),
                                ),
                              ),
                              const Divider(indent: 16, endIndent: 16),
                            ],
                          );
                        },
                      ),
          ),
        ],
      ),
    );
  }
}
