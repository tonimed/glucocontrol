package com.glucocontrol.data.repository

import com.glucocontrol.data.local.dao.GlucoseReadingDao
import com.glucocontrol.data.local.entity.GlucoseReadingEntity
import com.glucocontrol.domain.model.GlucoseReading
import com.glucocontrol.domain.repository.GlucoseRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import javax.inject.Inject

/**
 * Implementación concreta del repositorio de glucosa.
 *
 * Actúa como puente entre el DAO (que trabaja con [GlucoseReadingEntity])
 * y la capa de dominio (que trabaja con [GlucoseReading]).
 *
 * El mapeo Entity ↔ Domain ocurre exclusivamente aquí, de forma que
 * ni el DAO ni el dominio conocen la estructura interna del otro.
 *
 * [@Inject] permite a Hilt proveer esta clase automáticamente donde se
 * declare una dependencia de [GlucoseRepository].
 */
class GlucoseRepositoryImpl
@Inject
constructor(private val dao: GlucoseReadingDao) : GlucoseRepository {
    override fun getReadingsByDate(date: LocalDate): Flow<List<GlucoseReading>> = dao
        .getByDate(date.toEpochDay())
        .map { entities -> entities.map(GlucoseReadingEntity::toDomain) }

    override fun getReadingsByDateRange(from: LocalDate, to: LocalDate): Flow<List<GlucoseReading>> = dao
        .getByDateRange(from.toEpochDay(), to.toEpochDay())
        .map { entities -> entities.map(GlucoseReadingEntity::toDomain) }

    override suspend fun insertReading(reading: GlucoseReading): Long =
        dao.insert(GlucoseReadingEntity.fromDomain(reading))

    override suspend fun updateReading(reading: GlucoseReading) {
        dao.update(GlucoseReadingEntity.fromDomain(reading))
    }

    override suspend fun deleteReading(id: Long) {
        dao.deleteById(id)
    }

    override suspend fun getReadingById(id: Long): GlucoseReading? = dao.getById(id)?.toDomain()
}
