package com.glucocontrol.domain.usecase

import com.glucocontrol.domain.repository.GlucoseRepository
import com.glucocontrol.domain.usecase.reading.GetReadingsByDateRangeUseCase
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.time.LocalDate

class GetReadingsByDateRangeUseCaseTest {
    private lateinit var repository: GlucoseRepository
    private lateinit var useCase: GetReadingsByDateRangeUseCase

    @BeforeEach
    fun setUp() {
        repository = mockk()
        useCase = GetReadingsByDateRangeUseCase(repository)
    }

    @Test
    fun `invoke returns flow from repository for a valid range`() = runTest {
        val from = LocalDate.of(2024, 1, 1)
        val to = LocalDate.of(2024, 1, 7)
        every { repository.getReadingsByDateRange(from, to) } returns flowOf(emptyList())

        val result = useCase(from, to).first()

        assertTrue(result.isEmpty())
        verify(exactly = 1) { repository.getReadingsByDateRange(from, to) }
    }

    @Test
    fun `invoke accepts same date for from and to`() = runTest {
        val date = LocalDate.of(2024, 3, 15)
        every { repository.getReadingsByDateRange(date, date) } returns flowOf(emptyList())

        useCase(date, date) // must not throw
    }

    @Test
    fun `invoke throws when from is after to`() {
        val from = LocalDate.of(2024, 1, 10)
        val to = LocalDate.of(2024, 1, 1)

        assertThrows<IllegalArgumentException> { useCase(from, to) }
    }
}
