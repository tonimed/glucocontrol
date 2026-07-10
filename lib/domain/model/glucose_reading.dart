import 'package:glucocontrol/domain/model/reading_tag.dart';

class GlucoseReading {
  final int id;
  final DateTime date;
  final DateTime? time;
  final int valueMgDl;
  final ReadingTag tag;
  final String? notes;

  const GlucoseReading({
    this.id = 0,
    required this.date,
    this.time,
    required this.valueMgDl,
    this.tag = ReadingTag.otro,
    this.notes,
  });

  GlucoseReading copyWith({
    int? id,
    DateTime? date,
    DateTime? time,
    bool clearTime = false,
    int? valueMgDl,
    ReadingTag? tag,
    String? notes,
    bool clearNotes = false,
  }) {
    return GlucoseReading(
      id: id ?? this.id,
      date: date ?? this.date,
      time: clearTime ? null : (time ?? this.time),
      valueMgDl: valueMgDl ?? this.valueMgDl,
      tag: tag ?? this.tag,
      notes: clearNotes ? null : (notes ?? this.notes),
    );
  }
}
