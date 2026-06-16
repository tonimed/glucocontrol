package com.glucocontrol.domain.usecase

import com.glucocontrol.domain.model.GlucoseReading
import com.glucocontrol.domain.model.ReadingTag
import com.glucocontrol.domain.repository.GlucoseRepository
import com.glucocontrol.domain.usecase.export.ExportReadingsUseCase
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.time.LocalDate

class ExportReadingsUseCaseTest {
    private val repository = mockk<GlucoseRepository>()
    private val useCase = ExportReadingsUseCase(repository)

    private val from = LocalDate.of(2024, 3, 1)
    private val to = LocalDate.of(2024, 3, 31)

    private val sampleReadings = listOf(
        GlucoseReading(id = 1L, date = from, valueMgDl = 95, tag = ReadingTag.AYUNAS),
        GlucoseReading(id = 2L, date = to, valueMgDl = 180, tag = ReadingTag.POST_COMIDA),
    )

    @Test
    fun `devuelve las lecturas del rango especificado`() = runTest {
        every { repository.getReadingsByDateRange(from, to) } returns flowOf(sampleReadings)

        val result = useCase(from, to)

        assertEquals(sampleReadings, result)
    }

    @Test
    fun `devuelve lista vacía si no hay lecturas en el rango`() = runTest {
        every { repository.getReadingsByDateRange(from, to) } returns flowOf(emptyList())

        val result = useCase(from, to)

        assertTrue(result.isEmpty())
    }

    @Test
    fun `acepta from igual a to (exportación de un solo día)`() = runTest {
        val date = LocalDate.of(2024, 3, 15)
        every { repository.getReadingsByDateRange(date, date) } returns flowOf(emptyList())

        val result = useCase(date, date)

        assertTrue(result.isEmpty())
    }

    @Test
    fun `lanza excepcion cuando from es posterior a to`() = runTest {
        assertThrows<IllegalArgumentException> {
            useCase(to, from) // fechas invertidas
        }
    }
}
