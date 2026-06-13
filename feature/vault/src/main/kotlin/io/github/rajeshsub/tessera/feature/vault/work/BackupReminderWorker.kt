package io.github.rajeshsub.tessera.feature.vault.work

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import io.github.rajeshsub.tessera.data.vault.PrefsRepository
import kotlinx.coroutines.flow.first

@HiltWorker
class BackupReminderWorker
    @AssistedInject
    constructor(
        @Assisted appContext: Context,
        @Assisted params: WorkerParameters,
        private val prefsRepository: PrefsRepository,
    ) : CoroutineWorker(appContext, params) {
        override suspend fun doWork(): Result {
            val lastBackup = prefsRepository.lastBackupTimestamp.first()
            val sinceLastBackup = System.currentTimeMillis() - lastBackup
            if (lastBackup == 0L || sinceLastBackup >= SEVEN_DAYS_MS) {
                showReminderNotification(applicationContext)
            }
            return Result.success()
        }

        private fun showReminderNotification(context: Context) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val channel =
                    NotificationChannel(
                        CHANNEL_ID,
                        "Backup Reminders",
                        NotificationManager.IMPORTANCE_DEFAULT,
                    )
                context.getSystemService(NotificationManager::class.java)?.createNotificationChannel(channel)
            }
            val notification =
                NotificationCompat
                    .Builder(context, CHANNEL_ID)
                    .setSmallIcon(android.R.drawable.ic_dialog_info)
                    .setContentTitle("Back up Tessera")
                    .setContentText("You haven't backed up your vault recently.")
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                    .setAutoCancel(true)
                    .build()
            runCatching {
                NotificationManagerCompat.from(context).notify(NOTIFICATION_ID, notification)
            }
        }

        companion object {
            const val CHANNEL_ID = "tessera_backup_reminder"
            private const val NOTIFICATION_ID = 1001
            private const val SEVEN_DAYS_MS = 7L * 24 * 60 * 60 * 1_000
            const val WORK_NAME = "tessera_backup_reminder"
        }
    }
