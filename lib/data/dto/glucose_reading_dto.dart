import 'package:glucocontrol/domain/model/glucose_reading.dart';
import 'package:glucocontrol/domain/model/reading_tag.dart';

// Columnas de la tabla Supabase — deben coincidir exactamente con el schema
class GlucoseReadingDto {
  final int id;
  final String userId;
  final int valueMgDl;
  final int dateEpochDay;
  final int? timeSecondsOfDay;
  final String tag;
  final String? notes;

  const GlucoseReadingDto({
    required this.id,
    required this.userId,
    required this.valueMgDl,
    required this.dateEpochDay,
    this.timeSecondsOfDay,
    required this.tag,
    this.notes,
  });

  factory GlucoseReadingDto.fromJson(Map<String, dynamic> json) =>
      GlucoseReadingDto(
        id: json['id'] as int,
        userId: json['user_id'] as String,
        valueMgDl: json['value_mg_dl'] as int,
        dateEpochDay: json['date_epoch_day'] as int,
        timeSecondsOfDay: json['time_seconds_of_day'] as int?,
        tag: (json['tag'] as String?) ?? 'OTRO',
        notes: json['notes'] as String?,
      );

  GlucoseReading toDomain() => GlucoseReading(
        id: id,
        date: DateTime.fromMillisecondsSinceEpoch(
          dateEpochDay * 86400 * 1000,
          isUtc: true,
        ),
        time: timeSecondsOfDay != null
            ? DateTime.fromMillisecondsSinceEpoch(0, isUtc: true).add(
                Duration(seconds: timeSecondsOfDay!),
              )
            : null,
        valueMgDl: valueMgDl,
        tag: ReadingTag.fromString(tag),
        notes: notes,
      );
}

class GlucoseReadingCreateDto {
  final String userId;
  final int valueMgDl;
  final int dateEpochDay;
  final int? timeSecondsOfDay;
  final String tag;
  final String? notes;

  const GlucoseReadingCreateDto({
    required this.userId,
    required this.valueMgDl,
    required this.dateEpochDay,
    this.timeSecondsOfDay,
    required this.tag,
    this.notes,
  });

  factory GlucoseReadingCreateDto.fromDomain(
    GlucoseReading r,
    String userId,
  ) {
    final epochDay = r.date
            .toUtc()
            .millisecondsSinceEpoch ~/
        86400000;
    final timeSeconds = r.time != null
        ? r.time!.hour * 3600 + r.time!.minute * 60 + r.time!.second
        : null;
    return GlucoseReadingCreateDto(
      userId: userId,
      valueMgDl: r.valueMgDl,
      dateEpochDay: epochDay,
      timeSecondsOfDay: timeSeconds,
      tag: r.tag.name.toUpperCase(),
      notes: r.notes,
    );
  }

  Map<String, dynamic> toJson() => {
        'user_id': userId,
        'value_mg_dl': valueMgDl,
        'date_epoch_day': dateEpochDay,
        if (timeSecondsOfDay != null) 'time_seconds_of_day': timeSecondsOfDay,
        'tag': tag,
        if (notes != null) 'notes': notes,
      };
}

class GlucoseReadingUpdateDto extends GlucoseReadingCreateDto {
  final int id;

  const GlucoseReadingUpdateDto({
    required this.id,
    required super.userId,
    required super.valueMgDl,
    required super.dateEpochDay,
    super.timeSecondsOfDay,
    required super.tag,
    super.notes,
  });

  factory GlucoseReadingUpdateDto.fromDomain(
    GlucoseReading r,
    String userId,
  ) {
    final base = GlucoseReadingCreateDto.fromDomain(r, userId);
    return GlucoseReadingUpdateDto(
      id: r.id,
      userId: base.userId,
      valueMgDl: base.valueMgDl,
      dateEpochDay: base.dateEpochDay,
      timeSecondsOfDay: base.timeSecondsOfDay,
      tag: base.tag,
      notes: base.notes,
    );
  }

  @override
  Map<String, dynamic> toJson() => {
        ...super.toJson(),
        'id': id,
      };
}
