package com.glucocontrol.domain.usecase

import com.glucocontrol.domain.repository.GlucoseRepository
import com.glucocontrol.domain.usecase.reading.DeleteReadingUseCase
import io.mockk.coJustRun
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class DeleteReadingUseCaseTest {
    private lateinit var repository: GlucoseRepository
    private lateinit var useCase: DeleteReadingUseCase

    @BeforeEach
    fun setUp() {
        repository = mockk()
        useCase = DeleteReadingUseCase(repository)
    }

    @Test
    fun `invoke calls repository with a valid id`() = runTest {
        coJustRun { repository.deleteReading(5L) }

        useCase(5L)

        coVerify(exactly = 1) { repository.deleteReading(5L) }
    }

    @Test
    fun `invoke throws when id is zero`() = runTest {
        assertThrows<IllegalArgumentException> { useCase(0L) }
    }

    @Test
    fun `invoke throws when id is negative`() = runTest {
        assertThrows<IllegalArgumentException> { useCase(-1L) }
    }
}
