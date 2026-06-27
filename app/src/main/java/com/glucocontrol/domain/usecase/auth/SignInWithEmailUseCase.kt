package com.glucocontrol.domain.usecase.auth

import com.glucocontrol.domain.repository.AuthRepository
import javax.inject.Inject

class SignInWithEmailUseCase @Inject constructor(private val authRepository: AuthRepository) {
    suspend operator fun invoke(email: String, password: String) = authRepository.signInWithEmail(email, password)
}
