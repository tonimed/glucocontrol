import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:glucocontrol/domain/model/glucose_reading.dart';
import 'package:glucocontrol/domain/model/reading_tag.dart';
import 'package:glucocontrol/presentation/provider/providers.dart';

class AddEditState {
  final DateTime date;
  final DateTime? time;
  final String valueMgDl;
  final ReadingTag tag;
  final String notes;
  final bool isEditing;
  final bool isSaving;
  final bool savedSuccessfully;
  final String? error;

  const AddEditState({
    required this.date,
    this.time,
    this.valueMgDl = '',
    this.tag = ReadingTag.otro,
    this.notes = '',
    this.isEditing = false,
    this.isSaving = false,
    this.savedSuccessfully = false,
    this.error,
  });

  AddEditState copyWith({
    DateTime? date,
    DateTime? time,
    bool clearTime = false,
    String? valueMgDl,
    ReadingTag? tag,
    String? notes,
    bool? isEditing,
    bool? isSaving,
    bool? savedSuccessfully,
    String? error,
    bool clearError = false,
  }) =>
      AddEditState(
        date: date ?? this.date,
        time: clearTime ? null : (time ?? this.time),
        valueMgDl: valueMgDl ?? this.valueMgDl,
        tag: tag ?? this.tag,
        notes: notes ?? this.notes,
        isEditing: isEditing ?? this.isEditing,
        isSaving: isSaving ?? this.isSaving,
        savedSuccessfully: savedSuccessfully ?? this.savedSuccessfully,
        error: clearError ? null : (error ?? this.error),
      );
}

class AddEditNotifier extends StateNotifier<AddEditState> {
  final Ref _ref;
  final int readingId;

  AddEditNotifier(this._ref, this.readingId)
      : super(AddEditState(date: DateTime.now())) {
    if (readingId > 0) _loadReading();
  }

  Future<void> _loadReading() async {
    final reading = await _ref.read(getReadingByIdProvider).call(readingId);
    if (reading == null) return;
    state = state.copyWith(
      date: reading.date,
      time: reading.time,
      valueMgDl: reading.valueMgDl.toString(),
      tag: reading.tag,
      notes: reading.notes ?? '',
      isEditing: true,
    );
  }

  void setDate(DateTime date)     => state = state.copyWith(date: date);
  void setTime(DateTime? time)    => state = time == null
      ? state.copyWith(clearTime: true)
      : state.copyWith(time: time);
  void setValue(String v)         => state = state.copyWith(valueMgDl: v);
  void setTag(ReadingTag tag)     => state = state.copyWith(tag: tag);
  void setNotes(String n)         => state = state.copyWith(notes: n);

  Future<void> save() async {
    state = state.copyWith(isSaving: true, clearError: true);
    final value = int.tryParse(state.valueMgDl);
    if (value == null) {
      state = state.copyWith(isSaving: false, error: 'Introduce un valor numérico.');
      return;
    }

    final reading = GlucoseReading(
      id: readingId > 0 ? readingId : 0,
      date: state.date,
      time: state.time,
      valueMgDl: value,
      tag: state.tag,
      notes: state.notes.isEmpty ? null : state.notes,
    );

    try {
      if (state.isEditing) {
        await _ref.read(updateReadingProvider).call(reading);
      } else {
        await _ref.read(addReadingProvider).call(reading);
      }
      state = state.copyWith(isSaving: false, savedSuccessfully: true);
    } catch (e) {
      state = state.copyWith(isSaving: false, error: e.toString());
    }
  }
}

final addEditNotifierProvider = StateNotifierProvider.autoDispose
    .family<AddEditNotifier, AddEditState, int>(
  (ref, id) => AddEditNotifier(ref, id),
);
