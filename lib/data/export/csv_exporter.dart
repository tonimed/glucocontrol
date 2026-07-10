import 'package:csv/csv.dart';
import 'package:glucocontrol/domain/model/glucose_reading.dart';
import 'package:intl/intl.dart';

/// Genera un archivo CSV RFC 4180 con las lecturas indicadas.
class CsvExporter {
  static const _headers = [
    'Fecha',
    'Hora',
    'Glucosa (mg/dL)',
    'Etiqueta',
    'Notas',
  ];

  String export(List<GlucoseReading> readings) {
    final dateFmt = DateFormat('dd/MM/yyyy', 'es');
    final timeFmt = DateFormat('HH:mm', 'es');

    final rows = <List<dynamic>>[_headers];

    for (final r in readings) {
      rows.add([
        dateFmt.format(r.date),
        r.time != null ? timeFmt.format(r.time!) : '',
        r.valueMgDl,
        r.tag.label,
        r.notes ?? '',
      ]);
    }

    return const ListToCsvConverter().convert(rows);
  }
}
