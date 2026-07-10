import 'dart:async';
import 'package:collection/collection.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:glucocontrol/data/export/export_service.dart';
import 'package:glucocontrol/domain/model/glucose_range.dart';
import 'package:glucocontrol/domain/model/glucose_reading.dart';
import 'package:glucocontrol/presentation/provider/providers.dart';

enum HistoryPeriod { daily, weekly, monthly }

class HistoryState {
  final Map<DateTime, List<GlucoseReading>> groupedReadings;
  final HistoryPeriod period;
  final DateTime referenceDate;
  final GlucoseRange glucoseRange;
  final bool isLoading;
  final bool isExporting;
  final String? error;

  const HistoryState({
    this.groupedReadings = const {},
    this.period = HistoryPeriod.daily,
    required this.referenceDate,
    this.glucoseRange = const GlucoseRange(),
    this.isLoading = true,
    this.isExporting = false,
    this.error,
  });

  HistoryState copyWith({
    Map<DateTime, List<GlucoseReading>>? groupedReadings,
    HistoryPeriod? period,
    DateTime? referenceDate,
    GlucoseRange? glucoseRange,
    bool? isLoading,
    bool? isExporting,
    String? error,
    bool clearError = false,
  }) =>
      HistoryState(
        groupedReadings: groupedReadings ?? this.groupedReadings,
        period: period ?? this.period,
        referenceDate: referenceDate ?? this.referenceDate,
        glucoseRange: glucoseRange ?? this.glucoseRange,
        isLoading: isLoading ?? this.isLoading,
        isExporting: isExporting ?? this.isExporting,
        error: clearError ? null : (error ?? this.error),
      );

  int get totalReadings =>
      groupedReadings.values.fold(0, (s, l) => s + l.length);

  double? get average {
    final all = groupedReadings.values.expand((l) => l).toList();
    if (all.isEmpty) return null;
    return all.map((r) => r.valueMgDl).reduce((a, b) => a + b) / all.length;
  }

  double? get inRangePct {
    final all = groupedReadings.values.expand((l) => l).toList();
    if (all.isEmpty) return null;
    final inRange = all
        .where((r) =>
            r.valueMgDl >= glucoseRange.minTarget &&
            r.valueMgDl <= glucoseRange.maxTarget)
        .length;
    return inRange / all.length * 100;
  }
}

class HistoryNotifier extends StateNotifier<HistoryState> {
  final Ref _ref;
  StreamSubscription<List<GlucoseReading>>? _sub;

  HistoryNotifier(this._ref)
      : super(HistoryState(referenceDate: DateTime.now())) {
    _subscribe();
  }

  void _subscribe() {
    _sub?.cancel();
    final range = _dateRange();
    final stream = switch (state.period) {
      HistoryPeriod.daily =>
        _ref.read(getDailyReadingsProvider).call(state.referenceDate),
      HistoryPeriod.weekly =>
        _ref.read(getWeeklyReadingsProvider).call(state.referenceDate),
      HistoryPeriod.monthly => _ref
          .read(getMonthlyReadingsProvider)
          .call(range.$1.year, range.$1.month),
    };

    _sub = stream.listen(
      (readings) {
        // Agrupar por fecha descendente
        final grouped = groupBy<GlucoseReading, DateTime>(
          readings,
          (r) => DateTime(r.date.year, r.date.month, r.date.day),
        );
        final sorted = Map.fromEntries(
          grouped.entries.toList()
            ..sort((a, b) => b.key.compareTo(a.key)),
        );
        state = state.copyWith(
          groupedReadings: sorted,
          glucoseRange: _ref.read(glucoseRangeProvider),
          isLoading: false,
          clearError: true,
        );
      },
      onError: (_) => state = state.copyWith(
        isLoading: false,
        error: 'Error al cargar el historial.',
      ),
    );
  }

  (DateTime, DateTime) _dateRange() {
    final ref = state.referenceDate;
    return switch (state.period) {
      HistoryPeriod.daily   => (ref, ref),
      HistoryPeriod.weekly  => (
          ref.subtract(Duration(days: ref.weekday - 1)),
          ref.add(Duration(days: 7 - ref.weekday)),
        ),
      HistoryPeriod.monthly => (
          DateTime(ref.year, ref.month, 1),
          DateTime(ref.year, ref.month + 1, 0),
        ),
    };
  }

  void setPeriod(HistoryPeriod p) {
    state = state.copyWith(period: p, isLoading: true);
    _subscribe();
  }

  void goBack() {
    final ref = state.referenceDate;
    final next = switch (state.period) {
      HistoryPeriod.daily   => ref.subtract(const Duration(days: 1)),
      HistoryPeriod.weekly  => ref.subtract(const Duration(days: 7)),
      HistoryPeriod.monthly => DateTime(ref.year, ref.month - 1, 1),
    };
    state = state.copyWith(referenceDate: next, isLoading: true);
    _subscribe();
  }

  void goForward() {
    final ref = state.referenceDate;
    final next = switch (state.period) {
      HistoryPeriod.daily   => ref.add(const Duration(days: 1)),
      HistoryPeriod.weekly  => ref.add(const Duration(days: 7)),
      HistoryPeriod.monthly => DateTime(ref.year, ref.month + 1, 1),
    };
    state = state.copyWith(referenceDate: next, isLoading: true);
    _subscribe();
  }

  void clearError() => state = state.copyWith(clearError: true);

  Future<void> export(ExportFormat format) async {
    final range = _dateRange();
    final from = range.$1;
    final to = range.$2;

    state = state.copyWith(isExporting: true, clearError: true);
    try {
      final readings = await _ref
          .read(exportReadingsProvider)
          .call(from, to);

      if (readings.isEmpty) {
        state = state.copyWith(
          isExporting: false,
          error: 'No hay lecturas en este periodo para exportar.',
        );
        return;
      }

      await _ref.read(exportServiceProvider).share(
            readings: readings,
            format: format,
            from: from,
            to: to,
          );
    } catch (_) {
      state = state.copyWith(
        error: 'Error al generar el archivo de exportación.',
      );
    } finally {
      state = state.copyWith(isExporting: false);
    }
  }

  @override
  void dispose() {
    _sub?.cancel();
    super.dispose();
  }
}

final historyNotifierProvider =
    StateNotifierProvider.autoDispose<HistoryNotifier, HistoryState>(
  HistoryNotifier.new,
);
