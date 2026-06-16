package com.glucocontrol.domain.usecase

import com.glucocontrol.domain.repository.GlucoseRepository
import com.glucocontrol.domain.usecase.query.GetMonthlyReadingsUseCase
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.flowOf
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.time.Month

class GetMonthlyReadingsUseCaseTest {
    private lateinit var repository: GlucoseRepository
    private lateinit var useCase: GetMonthlyReadingsUseCase

    @BeforeEach
    fun setUp() {
        repository = mockk()
        useCase = GetMonthlyReadingsUseCase(repository)
    }

    @Test
    fun `invoke queries full month for January with 31 days`() {
        val expectedStart = LocalDate.of(2024, 1, 1)
        val expectedEnd = LocalDate.of(2024, 1, 31)
        every { repository.getReadingsByDateRange(expectedStart, expectedEnd) } returns flowOf(emptyList())

        useCase(2024, Month.JANUARY)

        verify(exactly = 1) { repository.getReadingsByDateRange(expectedStart, expectedEnd) }
    }

    @Test
    fun `invoke queries 29 days for February in a leap year`() {
        // 2024 es año bisiesto → febrero tiene 29 días
        val expectedStart = LocalDate.of(2024, 2, 1)
        val expectedEnd = LocalDate.of(2024, 2, 29)
        every { repository.getReadingsByDateRange(expectedStart, expectedEnd) } returns flowOf(emptyList())

        useCase(2024, Month.FEBRUARY)

        verify(exactly = 1) { repository.getReadingsByDateRange(expectedStart, expectedEnd) }
    }

    @Test
    fun `invoke queries 28 days for February in a non-leap year`() {
        val expectedStart = LocalDate.of(2023, 2, 1)
        val expectedEnd = LocalDate.of(2023, 2, 28)
        every { repository.getReadingsByDateRange(expectedStart, expectedEnd) } returns flowOf(emptyList())

        useCase(2023, Month.FEBRUARY)

        verify(exactly = 1) { repository.getReadingsByDateRange(expectedStart, expectedEnd) }
    }
}
