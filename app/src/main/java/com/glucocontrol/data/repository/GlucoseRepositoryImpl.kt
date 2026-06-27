package com.glucocontrol.data.repository

import com.glucocontrol.data.remote.dto.GlucoseReadingCreateDto
import com.glucocontrol.data.remote.dto.GlucoseReadingDto
import com.glucocontrol.data.remote.dto.GlucoseReadingUpdateDto
import com.glucocontrol.domain.model.GlucoseReading
import com.glucocontrol.domain.repository.GlucoseRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import java.time.LocalDate
import javax.inject.Inject

private const val TABLE = "glucose_readings"

/**
 * Implementación remota del repositorio de glucosa usando Supabase PostgREST.
 *
 * Cada Flow usa [refreshTrigger] con flatMapLatest para re-fetchear los datos
 * al producirse cualquier mutación (insert/update/delete). Así los ViewModels
 * reciben datos actualizados sin necesidad de navegar fuera y volver.
 *
 * Los flows envuelven la llamada de red con runCatching: si la red falla,
 * la tabla no existe o no hay sesión activa, emiten lista vacía (sin crash).
 */
@OptIn(ExperimentalCoroutinesApi::class)
class GlucoseRepositoryImpl @Inject constructor(private val supabase: SupabaseClient) : GlucoseRepository {

    // replay = 1 garantiza emisión inmediata al suscribirse; tryEmit nunca bloquea
    private val refreshTrigger = MutableSharedFlow<Unit>(replay = 1).also { it.tryEmit(Unit) }

    private fun invalidate() = refreshTrigger.tryEmit(Unit)

    private suspend fun currentUserId(): String = supabase.auth.currentUserOrNull()?.id
        ?: error("Sin sesión activa")

    override fun getReadingsByDate(date: LocalDate): Flow<List<GlucoseReading>> = refreshTrigger.flatMapLatest {
        flow {
            val result = runCatching {
                val userId = currentUserId()
                supabase.postgrest[TABLE]
                    .select {
                        filter {
                            eq("user_id", userId)
                            eq("date_epoch_day", date.toEpochDay())
                        }
                        order("time_seconds_of_day", Order.ASCENDING)
                    }
                    .decodeList<GlucoseReadingDto>()
                    .map { it.toDomain() }
            }
            emit(result.getOrDefault(emptyList()))
        }
    }

    override fun getReadingsByDateRange(from: LocalDate, to: LocalDate): Flow<List<GlucoseReading>> =
        refreshTrigger.flatMapLatest {
            flow {
                val result = runCatching {
                    val userId = currentUserId()
                    supabase.postgrest[TABLE]
                        .select {
                            filter {
                                eq("user_id", userId)
                                gte("date_epoch_day", from.toEpochDay())
                                lte("date_epoch_day", to.toEpochDay())
                            }
                            order("date_epoch_day", Order.ASCENDING)
                            order("time_seconds_of_day", Order.ASCENDING)
                        }
                        .decodeList<GlucoseReadingDto>()
                        .map { it.toDomain() }
                }
                emit(result.getOrDefault(emptyList()))
            }
        }

    override suspend fun insertReading(reading: GlucoseReading): Long {
        val userId = currentUserId()
        val dto = GlucoseReadingCreateDto.fromDomain(reading, userId)
        // postgrest-kt 2.x: insert ejecuta inmediatamente y retorna body vacío sin
        // Prefer:return=representation (propiedad privada en esta versión del SDK).
        supabase.postgrest[TABLE].insert(dto)
        invalidate()
        return 0L
    }

    override suspend fun updateReading(reading: GlucoseReading) {
        val userId = currentUserId()
        val dto = GlucoseReadingUpdateDto.fromDomain(reading, userId)
        supabase.postgrest[TABLE]
            .update(dto) {
                filter {
                    eq("id", reading.id)
                    eq("user_id", userId)
                }
            }
        invalidate()
    }

    override suspend fun deleteReading(id: Long) {
        val userId = currentUserId()
        supabase.postgrest[TABLE]
            .delete {
                filter {
                    eq("id", id)
                    eq("user_id", userId)
                }
            }
        invalidate()
    }

    override suspend fun getReadingById(id: Long): GlucoseReading? {
        val userId = currentUserId()
        return runCatching {
            supabase.postgrest[TABLE]
                .select {
                    filter {
                        eq("id", id)
                        eq("user_id", userId)
                    }
                }
                .decodeList<GlucoseReadingDto>()
                .firstOrNull()
                ?.toDomain()
        }.getOrNull()
    }
}
