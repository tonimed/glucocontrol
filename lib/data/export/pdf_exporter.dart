import 'package:glucocontrol/domain/model/glucose_reading.dart';
import 'package:intl/intl.dart';
import 'package:pdf/pdf.dart';
import 'package:pdf/widgets.dart' as pw;

/// Genera un PDF A4 con las lecturas en formato tabla.
class PdfExporter {
  Future<List<int>> export(
    List<GlucoseReading> readings,
    DateTime from,
    DateTime to,
  ) async {
    final pdf = pw.Document();
    final dateFmt = DateFormat('dd/MM/yyyy', 'es');
    final timeFmt = DateFormat('HH:mm', 'es');
    final headerFmt = DateFormat('d MMM yyyy', 'es');

    // Dividir en páginas de 30 filas para no desbordar
    const pageSize = 30;
    final pages = <List<GlucoseReading>>[];
    for (var i = 0; i < readings.length; i += pageSize) {
      pages.add(
        readings.sublist(
          i,
          (i + pageSize).clamp(0, readings.length),
        ),
      );
    }

    for (var p = 0; p < pages.length; p++) {
      pdf.addPage(
        pw.Page(
          pageFormat: PdfPageFormat.a4,
          margin: const pw.EdgeInsets.all(36),
          build: (ctx) => pw.Column(
            crossAxisAlignment: pw.CrossAxisAlignment.start,
            children: [
              // Cabecera
              pw.Text(
                'Control de Glucosa',
                style: pw.TextStyle(
                  fontSize: 18,
                  fontWeight: pw.FontWeight.bold,
                ),
              ),
              pw.SizedBox(height: 4),
              pw.Text(
                'Periodo: ${headerFmt.format(from)} – ${headerFmt.format(to)}',
                style: const pw.TextStyle(fontSize: 10, color: PdfColors.grey700),
              ),
              pw.SizedBox(height: 4),
              pw.Text(
                'Total: ${readings.length} lecturas   '
                '· Página ${p + 1} de ${pages.length}',
                style: const pw.TextStyle(fontSize: 10, color: PdfColors.grey700),
              ),
              pw.SizedBox(height: 12),

              // Tabla
              pw.Table(
                border: pw.TableBorder.all(color: PdfColors.grey400, width: 0.5),
                columnWidths: {
                  0: const pw.FixedColumnWidth(72),
                  1: const pw.FixedColumnWidth(48),
                  2: const pw.FixedColumnWidth(72),
                  3: const pw.FixedColumnWidth(96),
                  4: const pw.FlexColumnWidth(),
                },
                children: [
                  // Fila de cabecera
                  pw.TableRow(
                    decoration: const pw.BoxDecoration(color: PdfColors.grey200),
                    children: [
                      'Fecha',
                      'Hora',
                      'mg/dL',
                      'Etiqueta',
                      'Notas',
                    ]
                        .map(
                          (h) => pw.Padding(
                            padding: const pw.EdgeInsets.symmetric(
                              horizontal: 6,
                              vertical: 4,
                            ),
                            child: pw.Text(
                              h,
                              style: pw.TextStyle(fontWeight: pw.FontWeight.bold, fontSize: 9),
                            ),
                          ),
                        )
                        .toList(),
                  ),

                  // Filas de datos
                  ...pages[p].map(
                    (r) => pw.TableRow(
                      children: [
                        dateFmt.format(r.date),
                        r.time != null ? timeFmt.format(r.time!) : '—',
                        '${r.valueMgDl}',
                        r.tag.label,
                        r.notes ?? '',
                      ]
                          .map(
                            (cell) => pw.Padding(
                              padding: const pw.EdgeInsets.symmetric(
                                horizontal: 6,
                                vertical: 3,
                              ),
                              child: pw.Text(cell, style: const pw.TextStyle(fontSize: 9)),
                            ),
                          )
                          .toList(),
                    ),
                  ),
                ],
              ),
            ],
          ),
        ),
      );
    }

    return pdf.save();
  }
}
