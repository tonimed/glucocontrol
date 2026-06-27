package com.glucocontrol.domain.usecase.auth

import com.glucocontrol.domain.repository.AuthRepository
import javax.inject.Inject

class SignOutUseCase @Inject constructor(private val authRepository: AuthRepository) {
    suspend operator fun invoke() = authRepository.signOut()
}
