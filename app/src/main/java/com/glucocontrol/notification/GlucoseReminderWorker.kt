package com.glucocontrol.notification

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.glucocontrol.GlucoControlApp
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

// Worker que muestra un recordatorio para medir la glucosa.
// La frecuencia y el horario se configuran desde ReminderScheduler.
@HiltWorker
class GlucoseReminderWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        if (hasNotificationPermission()) {
            sendReminder()
        }
        return Result.success()
    }

    private fun hasNotificationPermission(): Boolean {
        // POST_NOTIFICATIONS es obligatoria solo en Android 13+ (API 33)
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return true
        return ContextCompat.checkSelfPermission(
            applicationContext,
            Manifest.permission.POST_NOTIFICATIONS,
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun sendReminder() {
        val notification =
            NotificationCompat.Builder(applicationContext, GlucoControlApp.CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle("Hora de medir tu glucosa")
                .setContentText("Registra tu lectura de glucosa en sangre")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true)
                .build()

        NotificationManagerCompat.from(applicationContext).notify(NOTIFICATION_ID, notification)
    }

    companion object {
        const val NOTIFICATION_ID = 1
    }
}
