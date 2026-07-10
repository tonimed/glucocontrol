import 'package:fl_chart/fl_chart.dart';
import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:glucocontrol/core/theme/app_colors.dart';
import 'package:glucocontrol/presentation/component/app_top_bar.dart';
import 'package:glucocontrol/presentation/screen/chart/chart_notifier.dart';
import 'package:intl/intl.dart';

class ChartScreen extends ConsumerWidget {
  final String initialPeriod;
  const ChartScreen({super.key, required this.initialPeriod});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final period = initialPeriod == 'MONTHLY'
        ? ChartPeriod.monthly
        : ChartPeriod.weekly;
    final state    = ref.watch(chartNotifierProvider(period));
    final notifier = ref.read(chartNotifierProvider(period).notifier);
    final cs       = Theme.of(context).colorScheme;

    final periodLabel = state.period == ChartPeriod.weekly
        ? () {
            final mon = state.referenceDate.subtract(
              Duration(days: state.referenceDate.weekday - 1),
            );
            final sun = mon.add(const Duration(days: 6));
            return '${DateFormat('d MMM', 'es').format(mon)} – '
                '${DateFormat('d MMM', 'es').format(sun)}';
          }()
        : DateFormat('MMMM yyyy', 'es').format(state.referenceDate);

    return Scaffold(
      appBar: AppTopBar(
        title: 'Gráfica',
        onBack: () => Navigator.of(context).pop(),
      ),
      body: Column(
        children: [
          // Selector de periodo
          Padding(
            padding: const EdgeInsets.fromLTRB(16, 8, 16, 0),
            child: Row(
              children: [
                FilterChip(
                  label: const Text('Semana'),
                  selected: state.period == ChartPeriod.weekly,
                  onSelected: (_) => notifier.setPeriod(ChartPeriod.weekly),
                ),
                const SizedBox(width: 8),
                FilterChip(
                  label: const Text('Mes'),
                  selected: state.period == ChartPeriod.monthly,
                  onSelected: (_) => notifier.setPeriod(ChartPeriod.monthly),
                ),
              ],
            ),
          ),

          // Navegador
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
                    periodLabel,
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

          // Estadísticas
          if (!state.isLoading && state.points.isNotEmpty)
            Padding(
              padding: const EdgeInsets.fromLTRB(16, 0, 16, 8),
              child: Wrap(
                spacing: 8,
                children: [
                  if (state.avgVal != null)
                    Chip(label: Text('Media: ${state.avgVal!.round()} mg/dL')),
                  if (state.minVal != null)
                    Chip(label: Text('Mín: ${state.minVal!.round()}')),
                  if (state.maxVal != null)
                    Chip(label: Text('Máx: ${state.maxVal!.round()}')),
                  if (state.inRangePct != null)
                    Chip(label: Text('En rango: ${state.inRangePct!.round()}%')),
                ],
              ),
            ),

          const Divider(height: 1),

          // Gráfica
          Expanded(
            child: state.isLoading
                ? const Center(child: CircularProgressIndicator())
                : state.points.isEmpty
                    ? const Center(child: Text('Sin datos en este periodo'))
                    : Padding(
                        padding: const EdgeInsets.fromLTRB(16, 16, 24, 16),
                        child: LineChart(
                          LineChartData(
                            minY: 40,
                            maxY: 350,
                            gridData: FlGridData(
                              show: true,
                              drawVerticalLine: false,
                              horizontalInterval: 70,
                              getDrawingHorizontalLine: (_) => FlLine(
                                color: cs.outlineVariant.withValues(alpha: 0.4),
                                strokeWidth: 1,
                              ),
                            ),
                            borderData: FlBorderData(show: false),
                            titlesData: FlTitlesData(
                              leftTitles: AxisTitles(
                                sideTitles: SideTitles(
                                  showTitles: true,
                                  interval: 70,
                                  reservedSize: 36,
                                  getTitlesWidget: (v, _) => Text(
                                    v.toInt().toString(),
                                    style: TextStyle(
                                      fontSize: 10,
                                      color: cs.onSurfaceVariant,
                                    ),
                                  ),
                                ),
                              ),
                              bottomTitles: AxisTitles(
                                sideTitles: SideTitles(
                                  showTitles: true,
                                  reservedSize: 28,
                                  getTitlesWidget: (v, _) {
                                    final idx = v.toInt();
                                    if (idx < 0 || idx >= state.points.length) {
                                      return const SizedBox.shrink();
                                    }
                                    return Padding(
                                      padding: const EdgeInsets.only(top: 4),
                                      child: Text(
                                        DateFormat('d/M').format(
                                          state.points[idx].date,
                                        ),
                                        style: TextStyle(
                                          fontSize: 9,
                                          color: cs.onSurfaceVariant,
                                        ),
                                      ),
                                    );
                                  },
                                ),
                              ),
                              rightTitles: const AxisTitles(
                                sideTitles: SideTitles(showTitles: false),
                              ),
                              topTitles: const AxisTitles(
                                sideTitles: SideTitles(showTitles: false),
                              ),
                            ),
                            // Zonas de color por rango
                            rangeAnnotations: RangeAnnotations(
                              horizontalRangeAnnotations: [
                                HorizontalRangeAnnotation(
                                  y1: 40,
                                  y2: state.glucoseRange.minTarget.toDouble(),
                                  color: kGlucoseLow.withValues(alpha: 0.07),
                                ),
                                HorizontalRangeAnnotation(
                                  y1: state.glucoseRange.minTarget.toDouble(),
                                  y2: state.glucoseRange.maxTarget.toDouble(),
                                  color: kGlucoseNormal.withValues(alpha: 0.07),
                                ),
                                HorizontalRangeAnnotation(
                                  y1: state.glucoseRange.maxTarget.toDouble(),
                                  y2: 350,
                                  color: kGlucoseHigh.withValues(alpha: 0.07),
                                ),
                              ],
                            ),
                            lineBarsData: [
                              LineChartBarData(
                                spots: state.points.asMap().entries.map((e) {
                                  return FlSpot(
                                    e.key.toDouble(),
                                    e.value.avgMgDl,
                                  );
                                }).toList(),
                                isCurved: true,
                                curveSmoothness: 0.3,
                                color: cs.primary,
                                barWidth: 2.5,
                                dotData: FlDotData(
                                  show: true,
                                  getDotPainter: (spot, _, _, _) {
                                    final val = spot.y;
                                    final color = val < state.glucoseRange.minTarget
                                        ? kGlucoseLow
                                        : val > state.glucoseRange.maxTarget
                                            ? kGlucoseHigh
                                            : kGlucoseNormal;
                                    return FlDotCirclePainter(
                                      radius: 4,
                                      color: color,
                                      strokeWidth: 1.5,
                                      strokeColor: Colors.white,
                                    );
                                  },
                                ),
                                belowBarData: BarAreaData(
                                  show: true,
                                  color: cs.primary.withValues(alpha: 0.06),
                                ),
                              ),
                            ],
                          ),
                        ),
                      ),
          ),
        ],
      ),
    );
  }
}
