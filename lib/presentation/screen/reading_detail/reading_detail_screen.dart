import 'package:fl_chart/fl_chart.dart';
import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';
import 'package:glucocontrol/core/theme/app_colors.dart';
import 'package:glucocontrol/domain/model/glucose_status.dart';
import 'package:glucocontrol/presentation/component/app_top_bar.dart';
import 'package:glucocontrol/presentation/component/glucose_reading_card.dart';
import 'package:glucocontrol/presentation/navigation/app_routes.dart';
import 'package:glucocontrol/presentation/screen/reading_detail/reading_detail_notifier.dart';

class ReadingDetailScreen extends ConsumerWidget {
  final int readingId;
  const ReadingDetailScreen({super.key, required this.readingId});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final state    = ref.watch(readingDetailNotifierProvider(readingId));
    final notifier = ref.read(readingDetailNotifierProvider(readingId).notifier);
    final cs       = Theme.of(context).colorScheme;

    if (state.isLoading || state.reading == null) {
      return Scaffold(
        appBar: AppTopBar(
          title: 'Detalle',
          onBack: () => Navigator.of(context).pop(),
        ),
        body: const Center(child: CircularProgressIndicator()),
      );
    }

    final reading = state.reading!;
    final status  = reading.glucoseStatus(state.glucoseRange);

    return Scaffold(
      appBar: AppTopBar(
        title: 'Detalle de lectura',
        onBack: () => Navigator.of(context).pop(),
      ),
      body: ListView(
        padding: const EdgeInsets.all(16),
        children: [
          // Tarjeta de la lectura (solo lectura, sin botones)
          GlucoseReadingCard(
            reading: reading,
            glucoseRange: state.glucoseRange,
          ),
          const SizedBox(height: 16),

          // Barra de estado de glucosa
          Card(
            child: Padding(
              padding: const EdgeInsets.all(16),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Text(
                    'Estado',
                    style: TextStyle(
                      fontSize: 13,
                      fontWeight: FontWeight.w700,
                      color: cs.onSurfaceVariant,
                    ),
                  ),
                  const SizedBox(height: 12),
                  _GlucoseStatusBar(
                    value: reading.valueMgDl,
                    minTarget: state.glucoseRange.minTarget,
                    maxTarget: state.glucoseRange.maxTarget,
                  ),
                  const SizedBox(height: 8),
                  Text(
                    switch (status) {
                      GlucoseStatus.low    => 'Por debajo del rango objetivo',
                      GlucoseStatus.normal => 'Dentro del rango objetivo',
                      GlucoseStatus.high   => 'Por encima del rango objetivo',
                    },
                    style: TextStyle(
                      fontSize: 13,
                      color: switch (status) {
                        GlucoseStatus.low    => kGlucoseLow,
                        GlucoseStatus.normal => kGlucoseNormal,
                        GlucoseStatus.high   => kGlucoseHigh,
                      },
                      fontWeight: FontWeight.w600,
                    ),
                  ),
                ],
              ),
            ),
          ),
          const SizedBox(height: 16),

          // Donut de distribución
          Card(
            child: Padding(
              padding: const EdgeInsets.all(16),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Text(
                    'Distribución del periodo',
                    style: TextStyle(
                      fontSize: 13,
                      fontWeight: FontWeight.w700,
                      color: cs.onSurfaceVariant,
                    ),
                  ),
                  const SizedBox(height: 8),
                  // Selector de periodo
                  Wrap(
                    spacing: 8,
                    children: DetailPeriod.values.map((p) {
                      final label = switch (p) {
                        DetailPeriod.threeDays => '3 días',
                        DetailPeriod.weekly    => 'Semana',
                        DetailPeriod.monthly   => 'Mes',
                      };
                      return FilterChip(
                        label: Text(label),
                        selected: state.period == p,
                        onSelected: (_) => notifier.setPeriod(p),
                        visualDensity: VisualDensity.compact,
                      );
                    }).toList(),
                  ),
                  const SizedBox(height: 16),
                  if (state.stats.total == 0)
                    const Center(child: Text('Sin lecturas en este periodo'))
                  else
                    Row(
                      children: [
                        SizedBox(
                          width: 140,
                          height: 140,
                          child: PieChart(
                            PieChartData(
                              sectionsSpace: 2,
                              centerSpaceRadius: 40,
                              sections: [
                                if (state.stats.low > 0)
                                  PieChartSectionData(
                                    value: state.stats.lowPct,
                                    color: kGlucoseLow,
                                    radius: 28,
                                    showTitle: false,
                                  ),
                                if (state.stats.normal > 0)
                                  PieChartSectionData(
                                    value: state.stats.normalPct,
                                    color: kGlucoseNormal,
                                    radius: 28,
                                    showTitle: false,
                                  ),
                                if (state.stats.high > 0)
                                  PieChartSectionData(
                                    value: state.stats.highPct,
                                    color: kGlucoseHigh,
                                    radius: 28,
                                    showTitle: false,
                                  ),
                              ],
                            ),
                          ),
                        ),
                        const SizedBox(width: 20),
                        Expanded(
                          child: Column(
                            crossAxisAlignment: CrossAxisAlignment.start,
                            children: [
                              _LegendRow(
                                color: kGlucoseLow,
                                label: 'Bajo',
                                pct: state.stats.lowPct,
                                count: state.stats.low,
                              ),
                              _LegendRow(
                                color: kGlucoseNormal,
                                label: 'Normal',
                                pct: state.stats.normalPct,
                                count: state.stats.normal,
                              ),
                              _LegendRow(
                                color: kGlucoseHigh,
                                label: 'Alto',
                                pct: state.stats.highPct,
                                count: state.stats.high,
                              ),
                            ],
                          ),
                        ),
                      ],
                    ),
                ],
              ),
            ),
          ),
          const SizedBox(height: 16),

          // Botón editar
          OutlinedButton.icon(
            onPressed: () =>
                context.push(AppRoutes.editReadingPath(reading.id)),
            icon: const Icon(Icons.edit_outlined),
            label: const Text('Editar lectura'),
          ),
        ],
      ),
    );
  }
}

class _GlucoseStatusBar extends StatelessWidget {
  final int value;
  final int minTarget;
  final int maxTarget;

  const _GlucoseStatusBar({
    required this.value,
    required this.minTarget,
    required this.maxTarget,
  });

  @override
  Widget build(BuildContext context) {
    const min = 40.0, max = 350.0;
    final clampedVal = value.clamp(min.toInt(), max.toInt()).toDouble();
    final pct = (clampedVal - min) / (max - min);

    return Column(
      crossAxisAlignment: CrossAxisAlignment.stretch,
      children: [
        // Barra segmentada por posición absoluta (Stack), no por Row+Expanded:
        // en el emulador de pruebas (imagen Android 17 "16k page size"),
        // Row+Expanded con un ColoredBox como único hijo no llega a pintarse
        // (verificado con varias combinaciones); un Stack posicionado por
        // porcentaje evita ese camino de renderizado por completo.
        LayoutBuilder(
          builder: (context, constraints) {
            final width = constraints.maxWidth;
            final lowW = (minTarget - min) / (max - min) * width;
            final normalW = (maxTarget - minTarget) / (max - min) * width;
            final highW = (max - maxTarget) / (max - min) * width;
            const cursorSize = 18.0;
            final cursorLeft =
                (pct * width - cursorSize / 2).clamp(0.0, width - cursorSize);
            const labelStyle = TextStyle(fontSize: 10);
            return SizedBox(
              width: double.infinity,
              height: 12 + cursorSize + 16,
              child: Stack(
                children: [
                  ClipRRect(
                    borderRadius: BorderRadius.circular(4),
                    child: SizedBox(
                      height: 12,
                      width: width,
                      child: Stack(
                        children: [
                          Positioned(
                            left: 0,
                            top: 0,
                            bottom: 0,
                            width: lowW,
                            child: DecoratedBox(
                              decoration: BoxDecoration(color: kGlucoseLow.withValues(alpha: 0.4)),
                            ),
                          ),
                          Positioned(
                            left: lowW,
                            top: 0,
                            bottom: 0,
                            width: normalW,
                            child: DecoratedBox(
                              decoration: BoxDecoration(color: kGlucoseNormal.withValues(alpha: 0.4)),
                            ),
                          ),
                          Positioned(
                            left: lowW + normalW,
                            top: 0,
                            bottom: 0,
                            width: highW,
                            child: DecoratedBox(
                              decoration: BoxDecoration(color: kGlucoseHigh.withValues(alpha: 0.4)),
                            ),
                          ),
                        ],
                      ),
                    ),
                  ),
                  Positioned(
                    left: cursorLeft,
                    top: 12,
                    child: const Icon(Icons.arrow_drop_up, size: cursorSize),
                  ),
                  // Etiquetas — alineadas con el punto real de la barra donde
                  // cambia de color (minTarget/maxTarget), no con los bordes
                  // del contenedor (que representan min/max, no el rango objetivo).
                  Positioned(
                    left: lowW,
                    top: 12 + cursorSize,
                    child: FractionalTranslation(
                      translation: const Offset(-0.5, 0),
                      child: Text('$minTarget', style: labelStyle),
                    ),
                  ),
                  Positioned(
                    left: lowW + normalW,
                    top: 12 + cursorSize,
                    child: FractionalTranslation(
                      translation: const Offset(-0.5, 0),
                      child: Text('$maxTarget', style: labelStyle),
                    ),
                  ),
                ],
              ),
            );
          },
        ),
      ],
    );
  }
}

class _LegendRow extends StatelessWidget {
  final Color color;
  final String label;
  final double pct;
  final int count;

  const _LegendRow({
    required this.color,
    required this.label,
    required this.pct,
    required this.count,
  });

  @override
  Widget build(BuildContext context) {
    return Padding(
      padding: const EdgeInsets.symmetric(vertical: 4),
      child: Row(
        children: [
          Container(
            width: 10,
            height: 10,
            decoration: BoxDecoration(color: color, shape: BoxShape.circle),
          ),
          const SizedBox(width: 8),
          Text(label, style: const TextStyle(fontSize: 13)),
          const Spacer(),
          Text(
            '${(pct * 100).round()}%  ($count)',
            style: const TextStyle(
              fontSize: 12,
              fontFeatures: [FontFeature.tabularFigures()],
            ),
          ),
        ],
      ),
    );
  }
}
