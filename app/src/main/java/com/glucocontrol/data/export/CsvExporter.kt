package com.glucocontrol.data.export

import com.glucocontrol.domain.model.GlucoseReading
import java.io.OutputStream
import javax.inject.Inject
import javax.inject.Singleton

// El separador es coma; las notas se envuelven entre comillas y las comillas
// internas se duplican según RFC 4180.
@Singleton
class CsvExporter @Inject constructor() {
    fun export(readings: List<GlucoseReading>, stream: OutputStream) {
        stream.bufferedWriter(Charsets.UTF_8).use { writer ->
            writer.write("id,fecha,hora,glucosa_mg_dl,etiqueta,notas\n")
            readings.forEach { r ->
                val hora = r.time?.toString() ?: ""
                val notas = r.notes
                    ?.replace("\"", "\"\"") // escapar comillas internas
                    ?.let { "\"$it\"" }
                    ?: ""
                writer.write("${r.id},${r.date},$hora,${r.valueMgDl},${r.tag.name},$notas\n")
            }
        }
    }
}
