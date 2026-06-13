package io.github.rajeshsub.tessera

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import android.util.Log
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import dagger.hilt.android.HiltAndroidApp
import io.github.rajeshsub.tessera.feature.vault.work.BackupReminderWorker
import timber.log.Timber
import javax.inject.Inject

@HiltAndroidApp
class TesseraApp :
    Application(),
    Configuration.Provider {
    @Inject lateinit var workerFactory: HiltWorkerFactory

    override val workManagerConfiguration: Configuration
        get() =
            Configuration
                .Builder()
                .setWorkerFactory(workerFactory)
                .build()

    override fun onCreate() {
        super.onCreate()
        plantTimberTree()
        createNotificationChannels()
        Timber.tag("Tessera").i("Started, dist flavor: %s", DIST_FLAVOR)
    }

    private fun plantTimberTree() {
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        } else {
            Timber.plant(RedactingReleaseTree())
        }
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel =
                NotificationChannel(
                    BackupReminderWorker.CHANNEL_ID,
                    "Backup Reminders",
                    NotificationManager.IMPORTANCE_DEFAULT,
                )
            getSystemService(NotificationManager::class.java)?.createNotificationChannel(channel)
        }
    }
}

private class RedactingReleaseTree : Timber.Tree() {
    override fun log(
        priority: Int,
        tag: String?,
        message: String,
        t: Throwable?,
    ) {
        if (priority < Log.ERROR) return
        Log.e(tag ?: "Tessera", message, t)
    }
}
