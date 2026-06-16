package com.glucocontrol.data.export

import com.glucocontrol.domain.model.GlucoseReading
import com.glucocontrol.domain.model.ReadingTag
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.io.ByteArrayOutputStream
import java.time.LocalDate
import java.time.LocalTime

class CsvExporterTest {
    private val exporter = CsvExporter()
    private val baseDate = LocalDate.of(2024, 3, 15)

    @Test
    fun `genera cabecera CSV en la primera línea`() {
        val output = ByteArrayOutputStream()
        exporter.export(emptyList(), output)
        val firstLine = output.toString(Charsets.UTF_8.name()).lines().first()
        assertEquals("id,fecha,hora,glucosa_mg_dl,etiqueta,notas", firstLine)
    }

    @Test
    fun `genera una fila por lectura además de la cabecera`() {
        val readings = listOf(
            GlucoseReading(id = 1L, date = baseDate, valueMgDl = 120, tag = ReadingTag.AYUNAS),
            GlucoseReading(id = 2L, date = baseDate, valueMgDl = 180, tag = ReadingTag.POST_COMIDA),
        )
        val output = ByteArrayOutputStream()
        exporter.export(readings, output)
        val lines = output.toString(Charsets.UTF_8.name()).trim().lines()
        assertEquals(3, lines.size) // cabecera + 2 lecturas
    }

    @Test
    fun `la hora se incluye cuando está presente`() {
        val reading = GlucoseReading(
            id = 1L,
            date = baseDate,
            valueMgDl = 100,
            tag = ReadingTag.AYUNAS,
            time = LocalTime.of(8, 30),
        )
        val output = ByteArrayOutputStream()
        exporter.export(listOf(reading), output)
        val dataLine = output.toString(Charsets.UTF_8.name()).lines()[1]
        assertTrue(dataLine.contains("08:30"))
    }

    @Test
    fun `hora y notas ausentes se representan como campo vacío`() {
        val reading = GlucoseReading(
            id = 1L,
            date = baseDate,
            valueMgDl = 100,
            tag = ReadingTag.AYUNAS,
            time = null,
            notes = null,
        )
        val output = ByteArrayOutputStream()
        exporter.export(listOf(reading), output)
        val fields = output.toString(Charsets.UTF_8.name()).lines()[1].split(",")
        assertEquals("", fields[2]) // hora vacía
        assertEquals("", fields[5]) // notas vacías
    }

    @Test
    fun `las comillas en notas se escapan como dobles comillas (RFC 4180)`() {
        val reading = GlucoseReading(
            id = 1L,
            date = baseDate,
            valueMgDl = 100,
            tag = ReadingTag.AYUNAS,
            notes = "dijo \"hola\"",
        )
        val output = ByteArrayOutputStream()
        exporter.export(listOf(reading), output)
        val content = output.toString(Charsets.UTF_8.name())
        // El contenido debe aparecer envuelto en comillas con las internas duplicadas
        assertTrue(content.contains("\"dijo \"\"hola\"\"\""))
    }
}
