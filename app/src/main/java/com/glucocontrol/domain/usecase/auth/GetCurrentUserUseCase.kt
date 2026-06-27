package com.glucocontrol.domain.usecase.auth

import com.glucocontrol.domain.model.User
import com.glucocontrol.domain.repository.AuthRepository
import javax.inject.Inject

class GetCurrentUserUseCase @Inject constructor(private val authRepository: AuthRepository) {
    suspend operator fun invoke(): User? = authRepository.getCurrentUser()
}
