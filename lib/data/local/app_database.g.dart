// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'app_database.dart';

// ignore_for_file: type=lint
class $GlucoseReadingsTableTable extends GlucoseReadingsTable
    with TableInfo<$GlucoseReadingsTableTable, GlucoseReadingsTableData> {
  @override
  final GeneratedDatabase attachedDatabase;
  final String? _alias;
  $GlucoseReadingsTableTable(this.attachedDatabase, [this._alias]);
  static const VerificationMeta _idMeta = const VerificationMeta('id');
  @override
  late final GeneratedColumn<int> id = GeneratedColumn<int>(
    'id',
    aliasedName,
    false,
    hasAutoIncrement: true,
    type: DriftSqlType.int,
    requiredDuringInsert: false,
    defaultConstraints: GeneratedColumn.constraintIsAlways(
      'PRIMARY KEY AUTOINCREMENT',
    ),
  );
  static const VerificationMeta _remoteIdMeta = const VerificationMeta(
    'remoteId',
  );
  @override
  late final GeneratedColumn<String> remoteId = GeneratedColumn<String>(
    'remote_id',
    aliasedName,
    true,
    type: DriftSqlType.string,
    requiredDuringInsert: false,
  );
  static const VerificationMeta _valueMgDlMeta = const VerificationMeta(
    'valueMgDl',
  );
  @override
  late final GeneratedColumn<int> valueMgDl = GeneratedColumn<int>(
    'value_mg_dl',
    aliasedName,
    false,
    type: DriftSqlType.int,
    requiredDuringInsert: true,
  );
  static const VerificationMeta _dateEpochDayMeta = const VerificationMeta(
    'dateEpochDay',
  );
  @override
  late final GeneratedColumn<int> dateEpochDay = GeneratedColumn<int>(
    'date_epoch_day',
    aliasedName,
    false,
    type: DriftSqlType.int,
    requiredDuringInsert: true,
  );
  static const VerificationMeta _timeSecondsOfDayMeta = const VerificationMeta(
    'timeSecondsOfDay',
  );
  @override
  late final GeneratedColumn<int> timeSecondsOfDay = GeneratedColumn<int>(
    'time_seconds_of_day',
    aliasedName,
    true,
    type: DriftSqlType.int,
    requiredDuringInsert: false,
  );
  static const VerificationMeta _tagMeta = const VerificationMeta('tag');
  @override
  late final GeneratedColumn<String> tag = GeneratedColumn<String>(
    'tag',
    aliasedName,
    false,
    type: DriftSqlType.string,
    requiredDuringInsert: false,
    defaultValue: const Constant('OTRO'),
  );
  static const VerificationMeta _notesMeta = const VerificationMeta('notes');
  @override
  late final GeneratedColumn<String> notes = GeneratedColumn<String>(
    'notes',
    aliasedName,
    true,
    type: DriftSqlType.string,
    requiredDuringInsert: false,
  );
  static const VerificationMeta _isSyncedMeta = const VerificationMeta(
    'isSynced',
  );
  @override
  late final GeneratedColumn<bool> isSynced = GeneratedColumn<bool>(
    'is_synced',
    aliasedName,
    false,
    type: DriftSqlType.bool,
    requiredDuringInsert: false,
    defaultConstraints: GeneratedColumn.constraintIsAlways(
      'CHECK ("is_synced" IN (0, 1))',
    ),
    defaultValue: const Constant(false),
  );
  static const VerificationMeta _updatedAtMeta = const VerificationMeta(
    'updatedAt',
  );
  @override
  late final GeneratedColumn<DateTime> updatedAt = GeneratedColumn<DateTime>(
    'updated_at',
    aliasedName,
    false,
    type: DriftSqlType.dateTime,
    requiredDuringInsert: true,
  );
  @override
  List<GeneratedColumn> get $columns => [
    id,
    remoteId,
    valueMgDl,
    dateEpochDay,
    timeSecondsOfDay,
    tag,
    notes,
    isSynced,
    updatedAt,
  ];
  @override
  String get aliasedName => _alias ?? actualTableName;
  @override
  String get actualTableName => $name;
  static const String $name = 'glucose_readings';
  @override
  VerificationContext validateIntegrity(
    Insertable<GlucoseReadingsTableData> instance, {
    bool isInserting = false,
  }) {
    final context = VerificationContext();
    final data = instance.toColumns(true);
    if (data.containsKey('id')) {
      context.handle(_idMeta, id.isAcceptableOrUnknown(data['id']!, _idMeta));
    }
    if (data.containsKey('remote_id')) {
      context.handle(
        _remoteIdMeta,
        remoteId.isAcceptableOrUnknown(data['remote_id']!, _remoteIdMeta),
      );
    }
    if (data.containsKey('value_mg_dl')) {
      context.handle(
        _valueMgDlMeta,
        valueMgDl.isAcceptableOrUnknown(data['value_mg_dl']!, _valueMgDlMeta),
      );
    } else if (isInserting) {
      context.missing(_valueMgDlMeta);
    }
    if (data.containsKey('date_epoch_day')) {
      context.handle(
        _dateEpochDayMeta,
        dateEpochDay.isAcceptableOrUnknown(
          data['date_epoch_day']!,
          _dateEpochDayMeta,
        ),
      );
    } else if (isInserting) {
      context.missing(_dateEpochDayMeta);
    }
    if (data.containsKey('time_seconds_of_day')) {
      context.handle(
        _timeSecondsOfDayMeta,
        timeSecondsOfDay.isAcceptableOrUnknown(
          data['time_seconds_of_day']!,
          _timeSecondsOfDayMeta,
        ),
      );
    }
    if (data.containsKey('tag')) {
      context.handle(
        _tagMeta,
        tag.isAcceptableOrUnknown(data['tag']!, _tagMeta),
      );
    }
    if (data.containsKey('notes')) {
      context.handle(
        _notesMeta,
        notes.isAcceptableOrUnknown(data['notes']!, _notesMeta),
      );
    }
    if (data.containsKey('is_synced')) {
      context.handle(
        _isSyncedMeta,
        isSynced.isAcceptableOrUnknown(data['is_synced']!, _isSyncedMeta),
      );
    }
    if (data.containsKey('updated_at')) {
      context.handle(
        _updatedAtMeta,
        updatedAt.isAcceptableOrUnknown(data['updated_at']!, _updatedAtMeta),
      );
    } else if (isInserting) {
      context.missing(_updatedAtMeta);
    }
    return context;
  }

  @override
  Set<GeneratedColumn> get $primaryKey => {id};
  @override
  GlucoseReadingsTableData map(
    Map<String, dynamic> data, {
    String? tablePrefix,
  }) {
    final effectivePrefix = tablePrefix != null ? '$tablePrefix.' : '';
    return GlucoseReadingsTableData(
      id: attachedDatabase.typeMapping.read(
        DriftSqlType.int,
        data['${effectivePrefix}id'],
      )!,
      remoteId: attachedDatabase.typeMapping.read(
        DriftSqlType.string,
        data['${effectivePrefix}remote_id'],
      ),
      valueMgDl: attachedDatabase.typeMapping.read(
        DriftSqlType.int,
        data['${effectivePrefix}value_mg_dl'],
      )!,
      dateEpochDay: attachedDatabase.typeMapping.read(
        DriftSqlType.int,
        data['${effectivePrefix}date_epoch_day'],
      )!,
      timeSecondsOfDay: attachedDatabase.typeMapping.read(
        DriftSqlType.int,
        data['${effectivePrefix}time_seconds_of_day'],
      ),
      tag: attachedDatabase.typeMapping.read(
        DriftSqlType.string,
        data['${effectivePrefix}tag'],
      )!,
      notes: attachedDatabase.typeMapping.read(
        DriftSqlType.string,
        data['${effectivePrefix}notes'],
      ),
      isSynced: attachedDatabase.typeMapping.read(
        DriftSqlType.bool,
        data['${effectivePrefix}is_synced'],
      )!,
      updatedAt: attachedDatabase.typeMapping.read(
        DriftSqlType.dateTime,
        data['${effectivePrefix}updated_at'],
      )!,
    );
  }

  @override
  $GlucoseReadingsTableTable createAlias(String alias) {
    return $GlucoseReadingsTableTable(attachedDatabase, alias);
  }
}

class GlucoseReadingsTableData extends DataClass
    implements Insertable<GlucoseReadingsTableData> {
  final int id;
  final String? remoteId;
  final int valueMgDl;
  final int dateEpochDay;
  final int? timeSecondsOfDay;
  final String tag;
  final String? notes;
  final bool isSynced;
  final DateTime updatedAt;
  const GlucoseReadingsTableData({
    required this.id,
    this.remoteId,
    required this.valueMgDl,
    required this.dateEpochDay,
    this.timeSecondsOfDay,
    required this.tag,
    this.notes,
    required this.isSynced,
    required this.updatedAt,
  });
  @override
  Map<String, Expression> toColumns(bool nullToAbsent) {
    final map = <String, Expression>{};
    map['id'] = Variable<int>(id);
    if (!nullToAbsent || remoteId != null) {
      map['remote_id'] = Variable<String>(remoteId);
    }
    map['value_mg_dl'] = Variable<int>(valueMgDl);
    map['date_epoch_day'] = Variable<int>(dateEpochDay);
    if (!nullToAbsent || timeSecondsOfDay != null) {
      map['time_seconds_of_day'] = Variable<int>(timeSecondsOfDay);
    }
    map['tag'] = Variable<String>(tag);
    if (!nullToAbsent || notes != null) {
      map['notes'] = Variable<String>(notes);
    }
    map['is_synced'] = Variable<bool>(isSynced);
    map['updated_at'] = Variable<DateTime>(updatedAt);
    return map;
  }

  GlucoseReadingsTableCompanion toCompanion(bool nullToAbsent) {
    return GlucoseReadingsTableCompanion(
      id: Value(id),
      remoteId: remoteId == null && nullToAbsent
          ? const Value.absent()
          : Value(remoteId),
      valueMgDl: Value(valueMgDl),
      dateEpochDay: Value(dateEpochDay),
      timeSecondsOfDay: timeSecondsOfDay == null && nullToAbsent
          ? const Value.absent()
          : Value(timeSecondsOfDay),
      tag: Value(tag),
      notes: notes == null && nullToAbsent
          ? const Value.absent()
          : Value(notes),
      isSynced: Value(isSynced),
      updatedAt: Value(updatedAt),
    );
  }

  factory GlucoseReadingsTableData.fromJson(
    Map<String, dynamic> json, {
    ValueSerializer? serializer,
  }) {
    serializer ??= driftRuntimeOptions.defaultSerializer;
    return GlucoseReadingsTableData(
      id: serializer.fromJson<int>(json['id']),
      remoteId: serializer.fromJson<String?>(json['remoteId']),
      valueMgDl: serializer.fromJson<int>(json['valueMgDl']),
      dateEpochDay: serializer.fromJson<int>(json['dateEpochDay']),
      timeSecondsOfDay: serializer.fromJson<int?>(json['timeSecondsOfDay']),
      tag: serializer.fromJson<String>(json['tag']),
      notes: serializer.fromJson<String?>(json['notes']),
      isSynced: serializer.fromJson<bool>(json['isSynced']),
      updatedAt: serializer.fromJson<DateTime>(json['updatedAt']),
    );
  }
  @override
  Map<String, dynamic> toJson({ValueSerializer? serializer}) {
    serializer ??= driftRuntimeOptions.defaultSerializer;
    return <String, dynamic>{
      'id': serializer.toJson<int>(id),
      'remoteId': serializer.toJson<String?>(remoteId),
      'valueMgDl': serializer.toJson<int>(valueMgDl),
      'dateEpochDay': serializer.toJson<int>(dateEpochDay),
      'timeSecondsOfDay': serializer.toJson<int?>(timeSecondsOfDay),
      'tag': serializer.toJson<String>(tag),
      'notes': serializer.toJson<String?>(notes),
      'isSynced': serializer.toJson<bool>(isSynced),
      'updatedAt': serializer.toJson<DateTime>(updatedAt),
    };
  }

  GlucoseReadingsTableData copyWith({
    int? id,
    Value<String?> remoteId = const Value.absent(),
    int? valueMgDl,
    int? dateEpochDay,
    Value<int?> timeSecondsOfDay = const Value.absent(),
    String? tag,
    Value<String?> notes = const Value.absent(),
    bool? isSynced,
    DateTime? updatedAt,
  }) => GlucoseReadingsTableData(
    id: id ?? this.id,
    remoteId: remoteId.present ? remoteId.value : this.remoteId,
    valueMgDl: valueMgDl ?? this.valueMgDl,
    dateEpochDay: dateEpochDay ?? this.dateEpochDay,
    timeSecondsOfDay: timeSecondsOfDay.present
        ? timeSecondsOfDay.value
        : this.timeSecondsOfDay,
    tag: tag ?? this.tag,
    notes: notes.present ? notes.value : this.notes,
    isSynced: isSynced ?? this.isSynced,
    updatedAt: updatedAt ?? this.updatedAt,
  );
  GlucoseReadingsTableData copyWithCompanion(
    GlucoseReadingsTableCompanion data,
  ) {
    return GlucoseReadingsTableData(
      id: data.id.present ? data.id.value : this.id,
      remoteId: data.remoteId.present ? data.remoteId.value : this.remoteId,
      valueMgDl: data.valueMgDl.present ? data.valueMgDl.value : this.valueMgDl,
      dateEpochDay: data.dateEpochDay.present
          ? data.dateEpochDay.value
          : this.dateEpochDay,
      timeSecondsOfDay: data.timeSecondsOfDay.present
          ? data.timeSecondsOfDay.value
          : this.timeSecondsOfDay,
      tag: data.tag.present ? data.tag.value : this.tag,
      notes: data.notes.present ? data.notes.value : this.notes,
      isSynced: data.isSynced.present ? data.isSynced.value : this.isSynced,
      updatedAt: data.updatedAt.present ? data.updatedAt.value : this.updatedAt,
    );
  }

  @override
  String toString() {
    return (StringBuffer('GlucoseReadingsTableData(')
          ..write('id: $id, ')
          ..write('remoteId: $remoteId, ')
          ..write('valueMgDl: $valueMgDl, ')
          ..write('dateEpochDay: $dateEpochDay, ')
          ..write('timeSecondsOfDay: $timeSecondsOfDay, ')
          ..write('tag: $tag, ')
          ..write('notes: $notes, ')
          ..write('isSynced: $isSynced, ')
          ..write('updatedAt: $updatedAt')
          ..write(')'))
        .toString();
  }

  @override
  int get hashCode => Object.hash(
    id,
    remoteId,
    valueMgDl,
    dateEpochDay,
    timeSecondsOfDay,
    tag,
    notes,
    isSynced,
    updatedAt,
  );
  @override
  bool operator ==(Object other) =>
      identical(this, other) ||
      (other is GlucoseReadingsTableData &&
          other.id == this.id &&
          other.remoteId == this.remoteId &&
          other.valueMgDl == this.valueMgDl &&
          other.dateEpochDay == this.dateEpochDay &&
          other.timeSecondsOfDay == this.timeSecondsOfDay &&
          other.tag == this.tag &&
          other.notes == this.notes &&
          other.isSynced == this.isSynced &&
          other.updatedAt == this.updatedAt);
}

class GlucoseReadingsTableCompanion
    extends UpdateCompanion<GlucoseReadingsTableData> {
  final Value<int> id;
  final Value<String?> remoteId;
  final Value<int> valueMgDl;
  final Value<int> dateEpochDay;
  final Value<int?> timeSecondsOfDay;
  final Value<String> tag;
  final Value<String?> notes;
  final Value<bool> isSynced;
  final Value<DateTime> updatedAt;
  const GlucoseReadingsTableCompanion({
    this.id = const Value.absent(),
    this.remoteId = const Value.absent(),
    this.valueMgDl = const Value.absent(),
    this.dateEpochDay = const Value.absent(),
    this.timeSecondsOfDay = const Value.absent(),
    this.tag = const Value.absent(),
    this.notes = const Value.absent(),
    this.isSynced = const Value.absent(),
    this.updatedAt = const Value.absent(),
  });
  GlucoseReadingsTableCompanion.insert({
    this.id = const Value.absent(),
    this.remoteId = const Value.absent(),
    required int valueMgDl,
    required int dateEpochDay,
    this.timeSecondsOfDay = const Value.absent(),
    this.tag = const Value.absent(),
    this.notes = const Value.absent(),
    this.isSynced = const Value.absent(),
    required DateTime updatedAt,
  }) : valueMgDl = Value(valueMgDl),
       dateEpochDay = Value(dateEpochDay),
       updatedAt = Value(updatedAt);
  static Insertable<GlucoseReadingsTableData> custom({
    Expression<int>? id,
    Expression<String>? remoteId,
    Expression<int>? valueMgDl,
    Expression<int>? dateEpochDay,
    Expression<int>? timeSecondsOfDay,
    Expression<String>? tag,
    Expression<String>? notes,
    Expression<bool>? isSynced,
    Expression<DateTime>? updatedAt,
  }) {
    return RawValuesInsertable({
      if (id != null) 'id': id,
      if (remoteId != null) 'remote_id': remoteId,
      if (valueMgDl != null) 'value_mg_dl': valueMgDl,
      if (dateEpochDay != null) 'date_epoch_day': dateEpochDay,
      if (timeSecondsOfDay != null) 'time_seconds_of_day': timeSecondsOfDay,
      if (tag != null) 'tag': tag,
      if (notes != null) 'notes': notes,
      if (isSynced != null) 'is_synced': isSynced,
      if (updatedAt != null) 'updated_at': updatedAt,
    });
  }

  GlucoseReadingsTableCompanion copyWith({
    Value<int>? id,
    Value<String?>? remoteId,
    Value<int>? valueMgDl,
    Value<int>? dateEpochDay,
    Value<int?>? timeSecondsOfDay,
    Value<String>? tag,
    Value<String?>? notes,
    Value<bool>? isSynced,
    Value<DateTime>? updatedAt,
  }) {
    return GlucoseReadingsTableCompanion(
      id: id ?? this.id,
      remoteId: remoteId ?? this.remoteId,
      valueMgDl: valueMgDl ?? this.valueMgDl,
      dateEpochDay: dateEpochDay ?? this.dateEpochDay,
      timeSecondsOfDay: timeSecondsOfDay ?? this.timeSecondsOfDay,
      tag: tag ?? this.tag,
      notes: notes ?? this.notes,
      isSynced: isSynced ?? this.isSynced,
      updatedAt: updatedAt ?? this.updatedAt,
    );
  }

  @override
  Map<String, Expression> toColumns(bool nullToAbsent) {
    final map = <String, Expression>{};
    if (id.present) {
      map['id'] = Variable<int>(id.value);
    }
    if (remoteId.present) {
      map['remote_id'] = Variable<String>(remoteId.value);
    }
    if (valueMgDl.present) {
      map['value_mg_dl'] = Variable<int>(valueMgDl.value);
    }
    if (dateEpochDay.present) {
      map['date_epoch_day'] = Variable<int>(dateEpochDay.value);
    }
    if (timeSecondsOfDay.present) {
      map['time_seconds_of_day'] = Variable<int>(timeSecondsOfDay.value);
    }
    if (tag.present) {
      map['tag'] = Variable<String>(tag.value);
    }
    if (notes.present) {
      map['notes'] = Variable<String>(notes.value);
    }
    if (isSynced.present) {
      map['is_synced'] = Variable<bool>(isSynced.value);
    }
    if (updatedAt.present) {
      map['updated_at'] = Variable<DateTime>(updatedAt.value);
    }
    return map;
  }

  @override
  String toString() {
    return (StringBuffer('GlucoseReadingsTableCompanion(')
          ..write('id: $id, ')
          ..write('remoteId: $remoteId, ')
          ..write('valueMgDl: $valueMgDl, ')
          ..write('dateEpochDay: $dateEpochDay, ')
          ..write('timeSecondsOfDay: $timeSecondsOfDay, ')
          ..write('tag: $tag, ')
          ..write('notes: $notes, ')
          ..write('isSynced: $isSynced, ')
          ..write('updatedAt: $updatedAt')
          ..write(')'))
        .toString();
  }
}

abstract class _$AppDatabase extends GeneratedDatabase {
  _$AppDatabase(QueryExecutor e) : super(e);
  $AppDatabaseManager get managers => $AppDatabaseManager(this);
  late final $GlucoseReadingsTableTable glucoseReadingsTable =
      $GlucoseReadingsTableTable(this);
  @override
  Iterable<TableInfo<Table, Object?>> get allTables =>
      allSchemaEntities.whereType<TableInfo<Table, Object?>>();
  @override
  List<DatabaseSchemaEntity> get allSchemaEntities => [glucoseReadingsTable];
}

typedef $$GlucoseReadingsTableTableCreateCompanionBuilder =
    GlucoseReadingsTableCompanion Function({
      Value<int> id,
      Value<String?> remoteId,
      required int valueMgDl,
      required int dateEpochDay,
      Value<int?> timeSecondsOfDay,
      Value<String> tag,
      Value<String?> notes,
      Value<bool> isSynced,
      required DateTime updatedAt,
    });
typedef $$GlucoseReadingsTableTableUpdateCompanionBuilder =
    GlucoseReadingsTableCompanion Function({
      Value<int> id,
      Value<String?> remoteId,
      Value<int> valueMgDl,
      Value<int> dateEpochDay,
      Value<int?> timeSecondsOfDay,
      Value<String> tag,
      Value<String?> notes,
      Value<bool> isSynced,
      Value<DateTime> updatedAt,
    });

class $$GlucoseReadingsTableTableFilterComposer
    extends Composer<_$AppDatabase, $GlucoseReadingsTableTable> {
  $$GlucoseReadingsTableTableFilterComposer({
    required super.$db,
    required super.$table,
    super.joinBuilder,
    super.$addJoinBuilderToRootComposer,
    super.$removeJoinBuilderFromRootComposer,
  });
  ColumnFilters<int> get id => $composableBuilder(
    column: $table.id,
    builder: (column) => ColumnFilters(column),
  );

  ColumnFilters<String> get remoteId => $composableBuilder(
    column: $table.remoteId,
    builder: (column) => ColumnFilters(column),
  );

  ColumnFilters<int> get valueMgDl => $composableBuilder(
    column: $table.valueMgDl,
    builder: (column) => ColumnFilters(column),
  );

  ColumnFilters<int> get dateEpochDay => $composableBuilder(
    column: $table.dateEpochDay,
    builder: (column) => ColumnFilters(column),
  );

  ColumnFilters<int> get timeSecondsOfDay => $composableBuilder(
    column: $table.timeSecondsOfDay,
    builder: (column) => ColumnFilters(column),
  );

  ColumnFilters<String> get tag => $composableBuilder(
    column: $table.tag,
    builder: (column) => ColumnFilters(column),
  );

  ColumnFilters<String> get notes => $composableBuilder(
    column: $table.notes,
    builder: (column) => ColumnFilters(column),
  );

  ColumnFilters<bool> get isSynced => $composableBuilder(
    column: $table.isSynced,
    builder: (column) => ColumnFilters(column),
  );

  ColumnFilters<DateTime> get updatedAt => $composableBuilder(
    column: $table.updatedAt,
    builder: (column) => ColumnFilters(column),
  );
}

class $$GlucoseReadingsTableTableOrderingComposer
    extends Composer<_$AppDatabase, $GlucoseReadingsTableTable> {
  $$GlucoseReadingsTableTableOrderingComposer({
    required super.$db,
    required super.$table,
    super.joinBuilder,
    super.$addJoinBuilderToRootComposer,
    super.$removeJoinBuilderFromRootComposer,
  });
  ColumnOrderings<int> get id => $composableBuilder(
    column: $table.id,
    builder: (column) => ColumnOrderings(column),
  );

  ColumnOrderings<String> get remoteId => $composableBuilder(
    column: $table.remoteId,
    builder: (column) => ColumnOrderings(column),
  );

  ColumnOrderings<int> get valueMgDl => $composableBuilder(
    column: $table.valueMgDl,
    builder: (column) => ColumnOrderings(column),
  );

  ColumnOrderings<int> get dateEpochDay => $composableBuilder(
    column: $table.dateEpochDay,
    builder: (column) => ColumnOrderings(column),
  );

  ColumnOrderings<int> get timeSecondsOfDay => $composableBuilder(
    column: $table.timeSecondsOfDay,
    builder: (column) => ColumnOrderings(column),
  );

  ColumnOrderings<String> get tag => $composableBuilder(
    column: $table.tag,
    builder: (column) => ColumnOrderings(column),
  );

  ColumnOrderings<String> get notes => $composableBuilder(
    column: $table.notes,
    builder: (column) => ColumnOrderings(column),
  );

  ColumnOrderings<bool> get isSynced => $composableBuilder(
    column: $table.isSynced,
    builder: (column) => ColumnOrderings(column),
  );

  ColumnOrderings<DateTime> get updatedAt => $composableBuilder(
    column: $table.updatedAt,
    builder: (column) => ColumnOrderings(column),
  );
}

class $$GlucoseReadingsTableTableAnnotationComposer
    extends Composer<_$AppDatabase, $GlucoseReadingsTableTable> {
  $$GlucoseReadingsTableTableAnnotationComposer({
    required super.$db,
    required super.$table,
    super.joinBuilder,
    super.$addJoinBuilderToRootComposer,
    super.$removeJoinBuilderFromRootComposer,
  });
  GeneratedColumn<int> get id =>
      $composableBuilder(column: $table.id, builder: (column) => column);

  GeneratedColumn<String> get remoteId =>
      $composableBuilder(column: $table.remoteId, builder: (column) => column);

  GeneratedColumn<int> get valueMgDl =>
      $composableBuilder(column: $table.valueMgDl, builder: (column) => column);

  GeneratedColumn<int> get dateEpochDay => $composableBuilder(
    column: $table.dateEpochDay,
    builder: (column) => column,
  );

  GeneratedColumn<int> get timeSecondsOfDay => $composableBuilder(
    column: $table.timeSecondsOfDay,
    builder: (column) => column,
  );

  GeneratedColumn<String> get tag =>
      $composableBuilder(column: $table.tag, builder: (column) => column);

  GeneratedColumn<String> get notes =>
      $composableBuilder(column: $table.notes, builder: (column) => column);

  GeneratedColumn<bool> get isSynced =>
      $composableBuilder(column: $table.isSynced, builder: (column) => column);

  GeneratedColumn<DateTime> get updatedAt =>
      $composableBuilder(column: $table.updatedAt, builder: (column) => column);
}

class $$GlucoseReadingsTableTableTableManager
    extends
        RootTableManager<
          _$AppDatabase,
          $GlucoseReadingsTableTable,
          GlucoseReadingsTableData,
          $$GlucoseReadingsTableTableFilterComposer,
          $$GlucoseReadingsTableTableOrderingComposer,
          $$GlucoseReadingsTableTableAnnotationComposer,
          $$GlucoseReadingsTableTableCreateCompanionBuilder,
          $$GlucoseReadingsTableTableUpdateCompanionBuilder,
          (
            GlucoseReadingsTableData,
            BaseReferences<
              _$AppDatabase,
              $GlucoseReadingsTableTable,
              GlucoseReadingsTableData
            >,
          ),
          GlucoseReadingsTableData,
          PrefetchHooks Function()
        > {
  $$GlucoseReadingsTableTableTableManager(
    _$AppDatabase db,
    $GlucoseReadingsTableTable table,
  ) : super(
        TableManagerState(
          db: db,
          table: table,
          createFilteringComposer: () =>
              $$GlucoseReadingsTableTableFilterComposer($db: db, $table: table),
          createOrderingComposer: () =>
              $$GlucoseReadingsTableTableOrderingComposer(
                $db: db,
                $table: table,
              ),
          createComputedFieldComposer: () =>
              $$GlucoseReadingsTableTableAnnotationComposer(
                $db: db,
                $table: table,
              ),
          updateCompanionCallback:
              ({
                Value<int> id = const Value.absent(),
                Value<String?> remoteId = const Value.absent(),
                Value<int> valueMgDl = const Value.absent(),
                Value<int> dateEpochDay = const Value.absent(),
                Value<int?> timeSecondsOfDay = const Value.absent(),
                Value<String> tag = const Value.absent(),
                Value<String?> notes = const Value.absent(),
                Value<bool> isSynced = const Value.absent(),
                Value<DateTime> updatedAt = const Value.absent(),
              }) => GlucoseReadingsTableCompanion(
                id: id,
                remoteId: remoteId,
                valueMgDl: valueMgDl,
                dateEpochDay: dateEpochDay,
                timeSecondsOfDay: timeSecondsOfDay,
                tag: tag,
                notes: notes,
                isSynced: isSynced,
                updatedAt: updatedAt,
              ),
          createCompanionCallback:
              ({
                Value<int> id = const Value.absent(),
                Value<String?> remoteId = const Value.absent(),
                required int valueMgDl,
                required int dateEpochDay,
                Value<int?> timeSecondsOfDay = const Value.absent(),
                Value<String> tag = const Value.absent(),
                Value<String?> notes = const Value.absent(),
                Value<bool> isSynced = const Value.absent(),
                required DateTime updatedAt,
              }) => GlucoseReadingsTableCompanion.insert(
                id: id,
                remoteId: remoteId,
                valueMgDl: valueMgDl,
                dateEpochDay: dateEpochDay,
                timeSecondsOfDay: timeSecondsOfDay,
                tag: tag,
                notes: notes,
                isSynced: isSynced,
                updatedAt: updatedAt,
              ),
          withReferenceMapper: (p0) => p0
              .map((e) => (e.readTable(table), BaseReferences(db, table, e)))
              .toList(),
          prefetchHooksCallback: null,
        ),
      );
}

typedef $$GlucoseReadingsTableTableProcessedTableManager =
    ProcessedTableManager<
      _$AppDatabase,
      $GlucoseReadingsTableTable,
      GlucoseReadingsTableData,
      $$GlucoseReadingsTableTableFilterComposer,
      $$GlucoseReadingsTableTableOrderingComposer,
      $$GlucoseReadingsTableTableAnnotationComposer,
      $$GlucoseReadingsTableTableCreateCompanionBuilder,
      $$GlucoseReadingsTableTableUpdateCompanionBuilder,
      (
        GlucoseReadingsTableData,
        BaseReferences<
          _$AppDatabase,
          $GlucoseReadingsTableTable,
          GlucoseReadingsTableData
        >,
      ),
      GlucoseReadingsTableData,
      PrefetchHooks Function()
    >;

class $AppDatabaseManager {
  final _$AppDatabase _db;
  $AppDatabaseManager(this._db);
  $$GlucoseReadingsTableTableTableManager get glucoseReadingsTable =>
      $$GlucoseReadingsTableTableTableManager(_db, _db.glucoseReadingsTable);
}
