import 'dart:async';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:glucocontrol/domain/model/glucose_range.dart';
import 'package:glucocontrol/domain/model/glucose_reading.dart';
import 'package:glucocontrol/domain/model/glucose_status.dart';
import 'package:glucocontrol/presentation/provider/providers.dart';

enum DetailPeriod { threeDays, weekly, monthly }

class DetailStats {
  final int low;
  final int normal;
  final int high;
  final int total;

  const DetailStats({
    required this.low,
    required this.normal,
    required this.high,
    required this.total,
  });

  double get lowPct    => total == 0 ? 0 : low    / total;
  double get normalPct => total == 0 ? 0 : normal / total;
  double get highPct   => total == 0 ? 0 : high   / total;
}

class ReadingDetailState {
  final GlucoseReading? reading;
  final DetailPeriod period;
  final DetailStats stats;
  final GlucoseRange glucoseRange;
  final bool isLoading;

  const ReadingDetailState({
    this.reading,
    this.period = DetailPeriod.weekly,
    this.stats = const DetailStats(low: 0, normal: 0, high: 0, total: 0),
    this.glucoseRange = const GlucoseRange(),
    this.isLoading = true,
  });

  ReadingDetailState copyWith({
    GlucoseReading? reading,
    DetailPeriod? period,
    DetailStats? stats,
    GlucoseRange? glucoseRange,
    bool? isLoading,
  }) =>
      ReadingDetailState(
        reading: reading ?? this.reading,
        period: period ?? this.period,
        stats: stats ?? this.stats,
        glucoseRange: glucoseRange ?? this.glucoseRange,
        isLoading: isLoading ?? this.isLoading,
      );
}

class ReadingDetailNotifier extends StateNotifier<ReadingDetailState> {
  final Ref _ref;
  final int readingId;
  StreamSubscription<List<GlucoseReading>>? _sub;

  ReadingDetailNotifier(this._ref, this.readingId)
      : super(const ReadingDetailState()) {
    _loadReading();
  }

  Future<void> _loadReading() async {
    final reading = await _ref.read(getReadingByIdProvider).call(readingId);
    final range   = _ref.read(glucoseRangeProvider);
    state = state.copyWith(reading: reading, glucoseRange: range);
    _subscribePeriod();
  }

  void _subscribePeriod() {
    _sub?.cancel();
    final now = DateTime.now();
    final Stream<List<GlucoseReading>> stream;

    switch (state.period) {
      case DetailPeriod.threeDays:
        final from = now.subtract(const Duration(days: 2));
        stream = _ref.read(getReadingsByDateRangeProvider).call(from, now);
      case DetailPeriod.weekly:
        stream = _ref.read(getWeeklyReadingsProvider).call(now);
      case DetailPeriod.monthly:
        stream = _ref.read(getMonthlyReadingsProvider).call(now.year, now.month);
    }

    _sub = stream.listen((readings) {
      final range  = state.glucoseRange;
      int lo = 0, no = 0, hi = 0;
      for (final r in readings) {
        switch (r.glucoseStatus(range)) {
          case GlucoseStatus.low:    lo++; break;
          case GlucoseStatus.normal: no++; break;
          case GlucoseStatus.high:   hi++; break;
        }
      }
      state = state.copyWith(
        stats: DetailStats(
          low: lo,
          normal: no,
          high: hi,
          total: readings.length,
        ),
        isLoading: false,
      );
    });
  }

  void setPeriod(DetailPeriod p) {
    state = state.copyWith(period: p, isLoading: true);
    _subscribePeriod();
  }

  @override
  void dispose() {
    _sub?.cancel();
    super.dispose();
  }
}

final readingDetailNotifierProvider = StateNotifierProvider.autoDispose
    .family<ReadingDetailNotifier, ReadingDetailState, int>(
  (ref, id) => ReadingDetailNotifier(ref, id),
);
