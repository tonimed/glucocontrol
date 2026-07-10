import 'dart:io';

import 'package:glucocontrol/data/export/csv_exporter.dart';
import 'package:glucocontrol/data/export/pdf_exporter.dart';
import 'package:glucocontrol/domain/model/glucose_reading.dart';
import 'package:intl/intl.dart';
import 'package:path_provider/path_provider.dart';
import 'package:share_plus/share_plus.dart';

enum ExportFormat { csv, pdf }

/// Genera el archivo (CSV o PDF) y abre el diálogo de compartir del sistema.
class ExportService {
  final CsvExporter _csv = CsvExporter();
  final PdfExporter _pdf = PdfExporter();

  Future<void> share({
    required List<GlucoseReading> readings,
    required ExportFormat format,
    required DateTime from,
    required DateTime to,
  }) async {
    final dir = await getTemporaryDirectory();
    final stamp = DateFormat('yyyyMMdd').format(DateTime.now());
    final File file;

    if (format == ExportFormat.csv) {
      final content = _csv.export(readings);
      file = File('${dir.path}/glucosa_$stamp.csv')
        ..writeAsStringSync(content);
    } else {
      final bytes = await _pdf.export(readings, from, to);
      file = File('${dir.path}/glucosa_$stamp.pdf')
        ..writeAsBytesSync(bytes);
    }

    await Share.shareXFiles(
      [XFile(file.path)],
      subject: 'Control de Glucosa — $stamp',
    );
  }
}
