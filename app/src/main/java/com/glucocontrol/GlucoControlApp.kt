package com.glucocontrol

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class GlucoControlApp :
    Application(),
    Configuration.Provider {

    // HiltWorkerFactory inyectada por Hilt para habilitar @HiltWorker en los workers
    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    // Provee la configuración personalizada de WorkManager con la factory de Hilt.
    // La inicialización automática se desactiva en el Manifest para usar esta.
    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        // NotificationChannel requiere API 26+ que coincide con minSdk — sin guard de versión
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Recordatorios de glucosa",
            NotificationManager.IMPORTANCE_DEFAULT,
        ).apply {
            description = "Recuerda medir tu glucosa en sangre"
        }
        getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
    }

    companion object {
        const val CHANNEL_ID = "glucocontrol_reminders"
    }
}
