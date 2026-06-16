package com.glucocontrol.notification

import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import dagger.hilt.android.qualifiers.ApplicationContext
import java.time.LocalTime
import java.time.temporal.ChronoUnit
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

// Gestiona el trabajo periódico de recordatorio de glucosa.
// WorkManager garantiza la ejecución aunque la app esté cerrada.
@Singleton
class ReminderScheduler @Inject constructor(@ApplicationContext private val context: Context) {
    private val workManager = WorkManager.getInstance(context)

    /**
     * Programa el recordatorio diario a la hora indicada.
     * Si ya existía una tarea previa, la reemplaza (UPDATE).
     */
    fun schedule(hourOfDay: Int, minute: Int) {
        val now = LocalTime.now()
        val target = LocalTime.of(hourOfDay, minute)
        // Minutos hasta el próximo disparo — si la hora ya pasó, el delay apunta a mañana
        var delay = ChronoUnit.MINUTES.between(now, target)
        if (delay < 0) delay += TimeUnit.DAYS.toMinutes(1)

        val request =
            PeriodicWorkRequestBuilder<GlucoseReminderWorker>(1, TimeUnit.DAYS)
                .setInitialDelay(delay, TimeUnit.MINUTES)
                .build()

        workManager.enqueueUniquePeriodicWork(WORK_NAME, ExistingPeriodicWorkPolicy.UPDATE, request)
    }

    /** Cancela el recordatorio si estaba programado. */
    fun cancel() {
        workManager.cancelUniqueWork(WORK_NAME)
    }

    companion object {
        const val WORK_NAME = "glucose_daily_reminder"
    }
}
