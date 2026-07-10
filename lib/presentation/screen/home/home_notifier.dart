import 'dart:async';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:glucocontrol/domain/model/glucose_range.dart';
import 'package:glucocontrol/domain/model/glucose_reading.dart';
import 'package:glucocontrol/presentation/provider/providers.dart';

class HomeState {
  final List<GlucoseReading> todayReadings;
  final GlucoseRange glucoseRange;
  final bool isLoading;
  final String? error;

  const HomeState({
    this.todayReadings = const [],
    this.glucoseRange = const GlucoseRange(),
    this.isLoading = true,
    this.error,
  });

  HomeState copyWith({
    List<GlucoseReading>? todayReadings,
    GlucoseRange? glucoseRange,
    bool? isLoading,
    String? error,
    bool clearError = false,
  }) =>
      HomeState(
        todayReadings: todayReadings ?? this.todayReadings,
        glucoseRange: glucoseRange ?? this.glucoseRange,
        isLoading: isLoading ?? this.isLoading,
        error: clearError ? null : (error ?? this.error),
      );

  int get count => todayReadings.length;
  double? get average => count == 0
      ? null
      : todayReadings.map((r) => r.valueMgDl).reduce((a, b) => a + b) / count;
  int? get last => todayReadings.isEmpty ? null : todayReadings.last.valueMgDl;
}

class HomeNotifier extends StateNotifier<HomeState> {
  final Ref _ref;
  StreamSubscription<List<GlucoseReading>>? _sub;

  HomeNotifier(this._ref) : super(const HomeState()) {
    _subscribe();
  }

  void _subscribe() {
    final today = DateTime.now();
    _sub = _ref.read(getDailyReadingsProvider).call(today).listen(
      (readings) {
        state = state.copyWith(
          todayReadings: readings,
          glucoseRange: _ref.read(glucoseRangeProvider),
          isLoading: false,
          clearError: true,
        );
      },
      onError: (_) => state = state.copyWith(
        isLoading: false,
        error: 'No se pudieron cargar las lecturas de hoy.',
      ),
    );
  }

  @override
  void dispose() {
    _sub?.cancel();
    super.dispose();
  }
}

final homeNotifierProvider =
    StateNotifierProvider.autoDispose<HomeNotifier, HomeState>(HomeNotifier.new);
