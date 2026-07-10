import 'dart:async';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:glucocontrol/domain/model/glucose_range.dart';
import 'package:glucocontrol/domain/model/glucose_reading.dart';
import 'package:glucocontrol/presentation/provider/providers.dart';

enum ChartPeriod { weekly, monthly }

class ChartPoint {
  final DateTime date;
  final double avgMgDl;
  const ChartPoint(this.date, this.avgMgDl);
}

class ChartState {
  final List<ChartPoint> points;
  final ChartPeriod period;
  final DateTime referenceDate;
  final GlucoseRange glucoseRange;
  final bool isLoading;
  final String? error;

  const ChartState({
    this.points = const [],
    this.period = ChartPeriod.weekly,
    required this.referenceDate,
    this.glucoseRange = const GlucoseRange(),
    this.isLoading = true,
    this.error,
  });

  ChartState copyWith({
    List<ChartPoint>? points,
    ChartPeriod? period,
    DateTime? referenceDate,
    GlucoseRange? glucoseRange,
    bool? isLoading,
    String? error,
    bool clearError = false,
  }) =>
      ChartState(
        points: points ?? this.points,
        period: period ?? this.period,
        referenceDate: referenceDate ?? this.referenceDate,
        glucoseRange: glucoseRange ?? this.glucoseRange,
        isLoading: isLoading ?? this.isLoading,
        error: clearError ? null : (error ?? this.error),
      );

  double? get minVal =>
      points.isEmpty ? null : points.map((p) => p.avgMgDl).reduce((a, b) => a < b ? a : b);
  double? get maxVal =>
      points.isEmpty ? null : points.map((p) => p.avgMgDl).reduce((a, b) => a > b ? a : b);
  double? get avgVal => points.isEmpty
      ? null
      : points.map((p) => p.avgMgDl).reduce((a, b) => a + b) / points.length;
  double? get inRangePct {
    if (points.isEmpty) return null;
    final inRange = points
        .where((p) =>
            p.avgMgDl >= glucoseRange.minTarget &&
            p.avgMgDl <= glucoseRange.maxTarget)
        .length;
    return inRange / points.length * 100;
  }
}

class ChartNotifier extends StateNotifier<ChartState> {
  final Ref _ref;
  StreamSubscription<List<GlucoseReading>>? _sub;

  ChartNotifier(this._ref, ChartPeriod initialPeriod)
      : super(ChartState(
          referenceDate: DateTime.now(),
          period: initialPeriod,
        )) {
    _subscribe();
  }

  void _subscribe() {
    _sub?.cancel();
    final Stream<List<GlucoseReading>> stream;
    final ref = state.referenceDate;

    if (state.period == ChartPeriod.weekly) {
      stream = _ref.read(getWeeklyReadingsProvider).call(ref);
    } else {
      stream = _ref
          .read(getMonthlyReadingsProvider)
          .call(ref.year, ref.month);
    }

    _sub = stream.listen(
      (readings) {
        // Agrupar por día y calcular media diaria
        final Map<DateTime, List<int>> byDay = {};
        for (final r in readings) {
          final day = DateTime(r.date.year, r.date.month, r.date.day);
          byDay.putIfAbsent(day, () => []).add(r.valueMgDl);
        }
        final points = byDay.entries
            .map((e) {
              final avg = e.value.reduce((a, b) => a + b) / e.value.length;
              return ChartPoint(e.key, avg);
            })
            .toList()
          ..sort((a, b) => a.date.compareTo(b.date));

        state = state.copyWith(
          points: points,
          glucoseRange: _ref.read(glucoseRangeProvider),
          isLoading: false,
          clearError: true,
        );
      },
      onError: (_) => state = state.copyWith(
        isLoading: false,
        error: 'Error al cargar la gráfica.',
      ),
    );
  }

  void setPeriod(ChartPeriod p) {
    state = state.copyWith(period: p, isLoading: true);
    _subscribe();
  }

  void goBack() {
    final ref = state.referenceDate;
    final next = state.period == ChartPeriod.weekly
        ? ref.subtract(const Duration(days: 7))
        : DateTime(ref.year, ref.month - 1, 1);
    state = state.copyWith(referenceDate: next, isLoading: true);
    _subscribe();
  }

  void goForward() {
    final ref = state.referenceDate;
    final next = state.period == ChartPeriod.weekly
        ? ref.add(const Duration(days: 7))
        : DateTime(ref.year, ref.month + 1, 1);
    state = state.copyWith(referenceDate: next, isLoading: true);
    _subscribe();
  }

  @override
  void dispose() {
    _sub?.cancel();
    super.dispose();
  }
}

final chartNotifierProvider = StateNotifierProvider.autoDispose
    .family<ChartNotifier, ChartState, ChartPeriod>(
  (ref, period) => ChartNotifier(ref, period),
);
