import 'package:flutter/material.dart';
import 'package:glucocontrol/core/theme/app_colors.dart';
import 'package:glucocontrol/domain/model/glucose_range.dart';
import 'package:glucocontrol/domain/model/glucose_reading.dart';
import 'package:glucocontrol/domain/model/glucose_status.dart';
import 'package:intl/intl.dart';

class GlucoseReadingCard extends StatelessWidget {
  final GlucoseReading reading;
  final GlucoseRange glucoseRange;
  final VoidCallback? onTap;
  final VoidCallback? onEdit;
  final VoidCallback? onDelete;

  const GlucoseReadingCard({
    super.key,
    required this.reading,
    required this.glucoseRange,
    this.onTap,
    this.onEdit,
    this.onDelete,
  });

  Color _valueColor() {
    return switch (reading.glucoseStatus(glucoseRange)) {
      GlucoseStatus.low    => kGlucoseLow,
      GlucoseStatus.normal => kGlucoseNormal,
      GlucoseStatus.high   => kGlucoseHigh,
    };
  }

  @override
  Widget build(BuildContext context) {
    final timeStr = reading.time != null
        ? DateFormat('HH:mm').format(reading.time!)
        : null;

    return Card(
      child: InkWell(
        onTap: onTap,
        borderRadius: BorderRadius.circular(12),
        child: Padding(
          padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 12),
          child: Row(
            children: [
              // Valor
              Text(
                '${reading.valueMgDl}',
                style: TextStyle(
                  fontSize: 26,
                  fontWeight: FontWeight.w700,
                  color: _valueColor(),
                  fontFeatures: const [FontFeature.tabularFigures()],
                ),
              ),
              const SizedBox(width: 4),
              Text(
                'mg/dL',
                style: TextStyle(
                  fontSize: 12,
                  color: Theme.of(context).colorScheme.onSurfaceVariant,
                ),
              ),
              const SizedBox(width: 12),
              // Etiqueta + hora
              Expanded(
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Chip(
                      label: Text(
                        reading.tag.label,
                        style: const TextStyle(fontSize: 11),
                      ),
                      padding: EdgeInsets.zero,
                      visualDensity: VisualDensity.compact,
                    ),
                    if (timeStr != null)
                      Text(
                        timeStr,
                        style: TextStyle(
                          fontSize: 12,
                          color: Theme.of(context).colorScheme.onSurfaceVariant,
                        ),
                      ),
                    if (reading.notes != null && reading.notes!.isNotEmpty)
                      Text(
                        reading.notes!,
                        style: TextStyle(
                          fontSize: 11,
                          color: Theme.of(context).colorScheme.onSurfaceVariant,
                        ),
                        maxLines: 1,
                        overflow: TextOverflow.ellipsis,
                      ),
                  ],
                ),
              ),
              // Acciones
              if (onEdit != null)
                IconButton(
                  icon: const Icon(Icons.edit_outlined, size: 20),
                  onPressed: onEdit,
                  tooltip: 'Editar',
                ),
              if (onDelete != null)
                IconButton(
                  icon: Icon(
                    Icons.delete_outline,
                    size: 20,
                    color: Theme.of(context).colorScheme.error,
                  ),
                  onPressed: onDelete,
                  tooltip: 'Eliminar',
                ),
            ],
          ),
        ),
      ),
    );
  }
}
