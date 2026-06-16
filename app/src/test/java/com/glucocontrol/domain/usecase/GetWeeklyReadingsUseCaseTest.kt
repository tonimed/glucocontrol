package com.glucocontrol.domain.usecase

import com.glucocontrol.domain.repository.GlucoseRepository
import com.glucocontrol.domain.usecase.query.GetWeeklyReadingsUseCase
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.flowOf
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.LocalDate

class GetWeeklyReadingsUseCaseTest {
    private lateinit var repository: GlucoseRepository
    private lateinit var useCase: GetWeeklyReadingsUseCase

    @BeforeEach
    fun setUp() {
        repository = mockk()
        useCase = GetWeeklyReadingsUseCase(repository)
    }

    @Test
    fun `invoke queries from Monday to Sunday when given a Wednesday`() {
        // Miércoles 2024-01-10 → semana: lun 2024-01-08 a dom 2024-01-14
        val wednesday = LocalDate.of(2024, 1, 10)
        val expectedStart = LocalDate.of(2024, 1, 8)
        val expectedEnd = LocalDate.of(2024, 1, 14)
        every { repository.getReadingsByDateRange(expectedStart, expectedEnd) } returns flowOf(emptyList())

        useCase(wednesday)

        verify(exactly = 1) { repository.getReadingsByDateRange(expectedStart, expectedEnd) }
    }

    @Test
    fun `invoke uses Monday as start when given a Monday`() {
        val monday = LocalDate.of(2024, 1, 8)
        val expectedEnd = LocalDate.of(2024, 1, 14)
        every { repository.getReadingsByDateRange(monday, expectedEnd) } returns flowOf(emptyList())

        useCase(monday)

        verify(exactly = 1) { repository.getReadingsByDateRange(monday, expectedEnd) }
    }

    @Test
    fun `invoke uses Sunday as end when given a Sunday`() {
        // Domingo 2024-01-14 → la semana sigue siendo lun 08 a dom 14
        val sunday = LocalDate.of(2024, 1, 14)
        val expectedStart = LocalDate.of(2024, 1, 8)
        every { repository.getReadingsByDateRange(expectedStart, sunday) } returns flowOf(emptyList())

        useCase(sunday)

        verify(exactly = 1) { repository.getReadingsByDateRange(expectedStart, sunday) }
    }
}
