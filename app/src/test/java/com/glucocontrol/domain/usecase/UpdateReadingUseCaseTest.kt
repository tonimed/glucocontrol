package com.glucocontrol.domain.usecase

import com.glucocontrol.domain.model.GlucoseReading
import com.glucocontrol.domain.model.ReadingTag
import com.glucocontrol.domain.repository.GlucoseRepository
import com.glucocontrol.domain.usecase.reading.UpdateReadingUseCase
import io.mockk.coJustRun
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.time.LocalDate

class UpdateReadingUseCaseTest {
    private lateinit var repository: GlucoseRepository
    private lateinit var useCase: UpdateReadingUseCase

    @BeforeEach
    fun setUp() {
        repository = mockk()
        useCase = UpdateReadingUseCase(repository)
    }

    @Test
    fun `invoke updates a reading with valid data`() = runTest {
        val reading = buildReading(id = 1L, valueMgDl = 120)
        coJustRun { repository.updateReading(reading) }

        useCase(reading)

        coVerify(exactly = 1) { repository.updateReading(reading) }
    }

    @Test
    fun `invoke throws when id is zero`() = runTest {
        assertThrows<IllegalArgumentException> { useCase(buildReading(id = 0L)) }
    }

    @Test
    fun `invoke throws when id is negative`() = runTest {
        assertThrows<IllegalArgumentException> { useCase(buildReading(id = -5L)) }
    }

    @Test
    fun `invoke throws when glucose value is zero`() = runTest {
        assertThrows<IllegalArgumentException> { useCase(buildReading(id = 1L, valueMgDl = 0)) }
    }

    @Test
    fun `invoke throws when glucose value exceeds 600`() = runTest {
        assertThrows<IllegalArgumentException> { useCase(buildReading(id = 1L, valueMgDl = 601)) }
    }

    @Test
    fun `invoke throws when date is before year 2020`() = runTest {
        assertThrows<IllegalArgumentException> {
            useCase(buildReading(id = 1L, date = LocalDate.of(2019, 12, 31)))
        }
    }

    private fun buildReading(id: Long = 1L, date: LocalDate = LocalDate.of(2024, 6, 1), valueMgDl: Int = 100) =
        GlucoseReading(
            id = id,
            date = date,
            valueMgDl = valueMgDl,
            tag = ReadingTag.POST_COMIDA,
        )
}
