package com.glucocontrol.data.dao

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.glucocontrol.data.local.dao.GlucoseReadingDao
import com.glucocontrol.data.local.database.GlucoDatabase
import com.glucocontrol.data.local.entity.GlucoseReadingEntity
import com.glucocontrol.domain.model.ReadingTag
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.time.LocalDate
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Tests de integración sobre la base de datos Room.
 *
 * Usan una base de datos en memoria ([Room.inMemoryDatabaseBuilder]) que se crea y destruye
 * en cada test, garantizando aislamiento total entre pruebas.
 *
 * Requieren un emulador o dispositivo Android (androidTest).
 */
@RunWith(AndroidJUnit4::class)
class GlucoseReadingDaoTest {
    private lateinit var db: GlucoDatabase
    private lateinit var dao: GlucoseReadingDao

    @Before
    fun setUp() {
        db =
            Room
                .inMemoryDatabaseBuilder(
                    ApplicationProvider.getApplicationContext(),
                    GlucoDatabase::class.java,
                ).allowMainThreadQueries()
                .build()
        dao = db.glucoseReadingDao()
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun insertAndRetrieveByDate_returnsCorrectRow() = runBlocking {
        val epochDay = LocalDate.of(2024, 3, 15).toEpochDay()
        val entity =
            GlucoseReadingEntity(
                dateEpochDay = epochDay,
                valueMgDl = 95,
                tag = ReadingTag.AYUNAS.name,
            )

        dao.insert(entity)

        val result = dao.getByDate(epochDay).first()
        assertEquals(1, result.size)
        assertEquals(95, result[0].valueMgDl)
        assertEquals(ReadingTag.AYUNAS.name, result[0].tag)
        assertNull(result[0].timeSecondOfDay)
    }

    @Test
    fun insertAndRetrieveByDateRange_excludesOutsideRows() = runBlocking {
        val date1 = LocalDate.of(2024, 3, 10).toEpochDay()
        val date2 = LocalDate.of(2024, 3, 14).toEpochDay()
        val dateOutside = LocalDate.of(2024, 3, 20).toEpochDay()

        dao.insert(GlucoseReadingEntity(dateEpochDay = date1, valueMgDl = 90, tag = ReadingTag.AYUNAS.name))
        dao.insert(GlucoseReadingEntity(dateEpochDay = date2, valueMgDl = 150, tag = ReadingTag.POST_COMIDA.name))
        dao.insert(GlucoseReadingEntity(dateEpochDay = dateOutside, valueMgDl = 80, tag = ReadingTag.AYUNAS.name))

        val result = dao.getByDateRange(date1, date2).first()

        assertEquals(2, result.size)
        assertTrue(result.none { it.valueMgDl == 80 })
    }

    @Test
    fun deleteById_removesOnlyTargetRow() = runBlocking {
        val epochDay = LocalDate.of(2024, 4, 1).toEpochDay()
        val idToDelete =
            dao.insert(
                GlucoseReadingEntity(dateEpochDay = epochDay, valueMgDl = 100, tag = ReadingTag.PRE_COMIDA.name),
            )
        dao.insert(
            GlucoseReadingEntity(dateEpochDay = epochDay, valueMgDl = 200, tag = ReadingTag.POST_COMIDA.name),
        )

        dao.deleteById(idToDelete)

        val result = dao.getByDate(epochDay).first()
        assertEquals(1, result.size)
        assertEquals(200, result[0].valueMgDl)
    }

    @Test
    fun update_changesFieldsOfExistingRow() = runBlocking {
        val epochDay = LocalDate.of(2024, 5, 1).toEpochDay()
        val id = dao.insert(
            GlucoseReadingEntity(dateEpochDay = epochDay, valueMgDl = 100, tag = ReadingTag.AYUNAS.name),
        )

        dao.update(
            GlucoseReadingEntity(
                id = id,
                dateEpochDay = epochDay,
                valueMgDl = 135,
                tag = ReadingTag.POST_COMIDA.name,
                notes = "Comida copiosa",
            ),
        )

        val result = dao.getByDate(epochDay).first()
        assertEquals(1, result.size)
        assertEquals(135, result[0].valueMgDl)
        assertEquals(ReadingTag.POST_COMIDA.name, result[0].tag)
        assertEquals("Comida copiosa", result[0].notes)
    }

    @Test
    fun readingsWithoutTime_orderedLastWithinSameDay() = runBlocking {
        val epochDay = LocalDate.of(2024, 6, 1).toEpochDay()
        dao.insert(
            GlucoseReadingEntity(
                dateEpochDay = epochDay,
                timeSecondOfDay = null,
                valueMgDl = 80,
                tag = ReadingTag.OTRO.name,
            ),
        )
        dao.insert(
            GlucoseReadingEntity(
                dateEpochDay = epochDay,
                timeSecondOfDay = 43200,
                valueMgDl = 120,
                tag = ReadingTag.PRE_COMIDA.name,
            ),
        )
        dao.insert(
            GlucoseReadingEntity(
                dateEpochDay = epochDay,
                timeSecondOfDay = 28800,
                valueMgDl = 95,
                tag = ReadingTag.AYUNAS.name,
            ),
        )

        val result = dao.getByDate(epochDay).first()

        assertEquals(3, result.size)
        assertEquals(95, result[0].valueMgDl) // 08:00 — primero
        assertEquals(120, result[1].valueMgDl) // 12:00 — segundo
        assertEquals(80, result[2].valueMgDl) // sin hora — último
    }

    @Test
    fun getByDate_returnsEmptyListWhenNothingInserted() = runBlocking {
        val epochDay = LocalDate.of(2024, 1, 1).toEpochDay()

        val result = dao.getByDate(epochDay).first()

        assertTrue(result.isEmpty())
    }
}
