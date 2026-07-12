import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:glucocontrol/domain/model/reading_tag.dart';
import 'package:glucocontrol/presentation/component/app_top_bar.dart';
import 'package:glucocontrol/presentation/screen/add_edit_reading/add_edit_reading_notifier.dart';
import 'package:intl/intl.dart';

class AddEditReadingScreen extends ConsumerStatefulWidget {
  final int readingId; // -1 = nueva, >0 = edición

  const AddEditReadingScreen({super.key, required this.readingId});

  @override
  ConsumerState<AddEditReadingScreen> createState() =>
      _AddEditReadingScreenState();
}

class _AddEditReadingScreenState extends ConsumerState<AddEditReadingScreen> {
  // Controladores persistentes: crear uno nuevo en cada build() (a partir de
  // state.valueMgDl) desincroniza el texto visible del estado real del
  // notifier — cualquier rebuild a mitad de escritura (p.ej. al fijar
  // state.error) sustituye el controlador por uno con el valor ANTERIOR,
  // perdiendo lo que el usuario acaba de teclear sin que onChanged lo note.
  late final _valueController = TextEditingController(
    text: ref.read(addEditNotifierProvider(widget.readingId)).valueMgDl,
  );
  late final _notesController = TextEditingController(
    text: ref.read(addEditNotifierProvider(widget.readingId)).notes,
  );

  @override
  void dispose() {
    _valueController.dispose();
    _notesController.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    final readingId = widget.readingId;
    final state    = ref.watch(addEditNotifierProvider(readingId));
    final notifier = ref.read(addEditNotifierProvider(readingId).notifier);

    // Volver automáticamente tras guardar
    ref.listen<AddEditState>(addEditNotifierProvider(readingId), (previous, next) {
      if (next.savedSuccessfully && context.mounted) Navigator.of(context).pop();

      // Sincroniza los controladores solo cuando el valor cambia por una vía
      // que no sea el propio TextField (p.ej. al cargar una lectura existente
      // para editar), nunca en cada rebuild.
      if (previous?.valueMgDl != next.valueMgDl &&
          _valueController.text != next.valueMgDl) {
        _valueController.text = next.valueMgDl;
        _valueController.selection =
            TextSelection.collapsed(offset: _valueController.text.length);
      }
      if (previous?.notes != next.notes && _notesController.text != next.notes) {
        _notesController.text = next.notes;
        _notesController.selection =
            TextSelection.collapsed(offset: _notesController.text.length);
      }
    });

    return Scaffold(
      appBar: AppTopBar(
        title: state.isEditing ? 'Editar lectura' : 'Nueva lectura',
        onBack: () => Navigator.of(context).pop(),
      ),
      body: SingleChildScrollView(
        padding: const EdgeInsets.all(16),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.stretch,
          children: [
            // Valor de glucosa
            TextField(
              decoration: const InputDecoration(
                labelText: 'Glucosa (mg/dL)',
                prefixIcon: Icon(Icons.water_drop_outlined),
              ),
              keyboardType: TextInputType.number,
              controller: _valueController,
              onChanged: notifier.setValue,
            ),
            const SizedBox(height: 14),

            // Fecha
            TextField(
              readOnly: true,
              decoration: InputDecoration(
                labelText: 'Fecha',
                prefixIcon: const Icon(Icons.calendar_today_outlined),
                suffixText: DateFormat('dd/MM/yyyy').format(state.date),
              ),
              onTap: () async {
                final picked = await showDatePicker(
                  context: context,
                  initialDate: state.date,
                  firstDate: DateTime(2020),
                  lastDate: DateTime.now(),
                );
                if (picked != null) notifier.setDate(picked);
              },
            ),
            const SizedBox(height: 14),

            // Hora (opcional)
            TextField(
              readOnly: true,
              decoration: InputDecoration(
                labelText: 'Hora (opcional)',
                prefixIcon: const Icon(Icons.access_time_outlined),
                suffixText: state.time != null
                    ? DateFormat('HH:mm').format(state.time!)
                    : 'Sin hora',
                suffixIcon: state.time != null
                    ? IconButton(
                        icon: const Icon(Icons.clear, size: 18),
                        onPressed: () => notifier.setTime(null),
                        tooltip: 'Eliminar hora',
                      )
                    : null,
              ),
              onTap: () async {
                final picked = await showTimePicker(
                  context: context,
                  initialTime: state.time != null
                      ? TimeOfDay.fromDateTime(state.time!)
                      : TimeOfDay.now(),
                  builder: (ctx, child) => MediaQuery(
                    data: MediaQuery.of(ctx)
                        .copyWith(alwaysUse24HourFormat: true),
                    child: child!,
                  ),
                );
                if (picked != null) {
                  final now = DateTime.now();
                  notifier.setTime(DateTime(
                    now.year,
                    now.month,
                    now.day,
                    picked.hour,
                    picked.minute,
                  ));
                }
              },
            ),
            const SizedBox(height: 16),

            // Etiquetas
            Text(
              'Etiqueta',
              style: TextStyle(
                fontSize: 13,
                color: Theme.of(context).colorScheme.onSurfaceVariant,
              ),
            ),
            const SizedBox(height: 8),
            Wrap(
              spacing: 8,
              runSpacing: 4,
              children: ReadingTag.values.map((tag) {
                final selected = state.tag == tag;
                return FilterChip(
                  label: Text(tag.label),
                  selected: selected,
                  onSelected: (_) => notifier.setTag(tag),
                );
              }).toList(),
            ),
            const SizedBox(height: 14),

            // Notas
            TextField(
              decoration: const InputDecoration(
                labelText: 'Notas (opcional)',
                prefixIcon: Icon(Icons.notes_outlined),
                alignLabelWithHint: true,
              ),
              maxLines: 3,
              controller: _notesController,
              onChanged: notifier.setNotes,
            ),
            const SizedBox(height: 20),

            // Error
            if (state.error != null)
              Padding(
                padding: const EdgeInsets.only(bottom: 12),
                child: Text(
                  state.error!,
                  style: TextStyle(
                    color: Theme.of(context).colorScheme.error,
                    fontSize: 13,
                  ),
                ),
              ),

            // Botón guardar
            FilledButton(
              onPressed: state.isSaving ? null : notifier.save,
              child: state.isSaving
                  ? const SizedBox(
                      height: 20,
                      width: 20,
                      child: CircularProgressIndicator(strokeWidth: 2),
                    )
                  : const Text('Guardar'),
            ),
          ],
        ),
      ),
    );
  }
}
