package com.glucocontrol.domain.usecase

import com.glucocontrol.domain.model.GlucoseReading
import com.glucocontrol.domain.model.ReadingTag
import com.glucocontrol.domain.repository.GlucoseRepository
import com.glucocontrol.domain.usecase.reading.AddReadingUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.time.LocalDate

class AddReadingUseCaseTest {
    private lateinit var repository: GlucoseRepository
    private lateinit var useCase: AddReadingUseCase

    @BeforeEach
    fun setUp() {
        repository = mockk()
        useCase = AddReadingUseCase(repository)
    }

    @Test
    fun `invoke inserts a valid reading and returns its generated id`() = runTest {
        val reading = buildReading(valueMgDl = 100)
        coEvery { repository.insertReading(reading) } returns 1L

        val result = useCase(reading)

        assertEquals(1L, result)
        coVerify(exactly = 1) { repository.insertReading(reading) }
    }

    @Test
    fun `invoke throws when glucose value is zero`() = runTest {
        assertThrows<IllegalArgumentException> { useCase(buildReading(valueMgDl = 0)) }
    }

    @Test
    fun `invoke throws when glucose value is negative`() = runTest {
        assertThrows<IllegalArgumentException> { useCase(buildReading(valueMgDl = -1)) }
    }

    @Test
    fun `invoke throws when glucose value exceeds 600`() = runTest {
        assertThrows<IllegalArgumentException> { useCase(buildReading(valueMgDl = 601)) }
    }

    @Test
    fun `invoke accepts boundary glucose value of 600`() = runTest {
        val reading = buildReading(valueMgDl = 600)
        coEvery { repository.insertReading(reading) } returns 2L

        useCase(reading)

        coVerify(exactly = 1) { repository.insertReading(reading) }
    }

    @Test
    fun `invoke throws when date is before year 2020`() = runTest {
        assertThrows<IllegalArgumentException> {
            useCase(buildReading(date = LocalDate.of(2019, 12, 31)))
        }
    }

    @Test
    fun `invoke throws when date is in the future`() = runTest {
        assertThrows<IllegalArgumentException> {
            useCase(buildReading(date = LocalDate.now().plusDays(1)))
        }
    }

    @Test
    fun `invoke accepts today as a valid date`() = runTest {
        val reading = buildReading(date = LocalDate.now())
        coEvery { repository.insertReading(reading) } returns 3L

        useCase(reading)

        coVerify(exactly = 1) { repository.insertReading(reading) }
    }

    private fun buildReading(date: LocalDate = LocalDate.now(), valueMgDl: Int = 100) = GlucoseReading(
        date = date,
        valueMgDl = valueMgDl,
        tag = ReadingTag.AYUNAS,
    )
}
