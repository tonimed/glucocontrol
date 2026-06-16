package com.glucocontrol.domain.usecase

import com.glucocontrol.domain.model.GlucoseReading
import com.glucocontrol.domain.model.ReadingTag
import com.glucocontrol.domain.repository.GlucoseRepository
import com.glucocontrol.domain.usecase.reading.GetReadingByIdUseCase
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.time.LocalDate

class GetReadingByIdUseCaseTest {
    private val repository = mockk<GlucoseRepository>()
    private val useCase = GetReadingByIdUseCase(repository)

    private val sampleReading =
        GlucoseReading(
            id = 1L,
            date = LocalDate.of(2024, 3, 15),
            valueMgDl = 120,
            tag = ReadingTag.POST_COMIDA,
        )

    @Test
    fun `retorna la lectura cuando el id existe`() = runTest {
        coEvery { repository.getReadingById(1L) } returns sampleReading

        val result = useCase(1L)

        assertEquals(sampleReading, result)
    }

    @Test
    fun `retorna null cuando el id no existe`() = runTest {
        coEvery { repository.getReadingById(99L) } returns null

        val result = useCase(99L)

        assertNull(result)
    }

    @Test
    fun `lanza excepcion cuando el id es cero`() = runTest {
        assertThrows<IllegalArgumentException> { useCase(0L) }
    }

    @Test
    fun `lanza excepcion cuando el id es negativo`() = runTest {
        assertThrows<IllegalArgumentException> { useCase(-1L) }
    }
}
