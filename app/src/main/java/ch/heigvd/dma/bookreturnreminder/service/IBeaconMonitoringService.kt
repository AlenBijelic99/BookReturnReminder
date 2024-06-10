package ch.heigvd.dma.bookreturnreminder.service

import android.app.Application
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.RemoteException
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.lifecycleScope
import ch.heigvd.dma.bookreturnreminder.MainActivity
import ch.heigvd.dma.bookreturnreminder.R
import ch.heigvd.dma.bookreturnreminder.models.Book
import ch.heigvd.dma.bookreturnreminder.repositories.BookRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.altbeacon.beacon.*


class IBeaconMonitoringService : LifecycleService() {

    private lateinit var beaconManager: BeaconManager
    private lateinit var bookRepository: BookRepository
    private val region = Region(
        "libraryRegion",
        Identifier.parse("ebefd083-70a2-47c8-9837-e7b5634df670"),
        Identifier.parse("1"),
        Identifier.parse("69")
    )

    override fun onCreate() {
        super.onCreate()
        Log.d("IBeaconMonitoringService", "Service created")
        bookRepository = BookRepository(applicationContext as Application)
        startForegroundService()
        setupBeaconManager()

        Log.d("IBeaconMonitoringService", "Service created and foreground service started")

    }

    private fun startForegroundService() {
        Log.d("IBeaconMonitoringService", "Starting foreground service")
        val channelId = "foreground_service_channel"
        val channelName = "Foreground Service"
        val importance = NotificationManager.IMPORTANCE_HIGH
        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val channel = NotificationChannel(channelId, channelName, importance)
        notificationManager.createNotificationChannel(channel)

        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, notificationIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle("iBeacon Monitoring Service")
            .setContentText("Monitoring for library iBeacons")
            .setSmallIcon(R.drawable.ic_notification)
            .setContentIntent(pendingIntent)
            .build()

        startForeground(1, notification)
    }

    private fun setupBeaconManager() {
        Log.d("IBeaconMonitoringService", "Setting up Beacon Manager")
        beaconManager = BeaconManager.getInstanceForApplication(this)
        beaconManager.beaconParsers.add(
            BeaconParser().setBeaconLayout("m:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24")
        )
        // Monitor the region for entering and exiting
        beaconManager.addMonitorNotifier(object : MonitorNotifier {
            override fun didEnterRegion(region: Region) {
                Log.d("IBeaconMonitoringService", "Entered region: ${region.id1}")
                sendNotification()
            }

            override fun didExitRegion(region: Region) {
                Log.d("IBeaconMonitoringService", "Exited region: ${region.id1}")
            }

            override fun didDetermineStateForRegion(state: Int, region: Region) {
                if (state == MonitorNotifier.INSIDE) {
                    Log.d("IBeaconMonitoringService", "Inside region: ${region.id1}")
                    sendNotification()
                } else {
                    Log.d("IBeaconMonitoringService", "Outside region: ${region.id1}")
                }
            }
        })

        // Range the beacons in the region to get updates about proximity
        beaconManager.addRangeNotifier { beacons, _ ->
            if (beacons.isNotEmpty()) {
                Log.d("IBeaconMonitoringService", "Beacons detected: ${beacons.first().id1}")
                sendNotification()
            } else {
                Log.d("IBeaconMonitoringService", "No beacons detected.")
            }
        }

        try {
            beaconManager.startMonitoring(region)
            beaconManager.startRangingBeacons(region)
            Log.d("IBeaconMonitoringService", "Started monitoring and ranging beacons")
        } catch (e: RemoteException) {
            Log.e("IBeaconMonitoringService", "Error starting monitoring or ranging", e)
            e.printStackTrace()
        }
    }

    private fun sendNotification() {
        Log.d("IBeaconMonitoringService", "Started monitoring and ranging beacons")
        lifecycleScope.launch(Dispatchers.IO) {
            val dueBooks = getDueBooks()
            if (dueBooks.isNotEmpty()) {
                val booksList = dueBooks.joinToString(", ") { it.title }
                Log.d("IBeaconMonitoringService", "Due books found: $booksList")
                val channelId = "library_channel_id"
                val channelName = "Library Notification"
                val importance = NotificationManager.IMPORTANCE_HIGH

                val notificationManager =
                    getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                val channel = NotificationChannel(channelId, channelName, importance)
                notificationManager.createNotificationChannel(channel)

                val notificationIntent =
                    Intent(this@IBeaconMonitoringService, MainActivity::class.java)
                val pendingIntent = PendingIntent.getActivity(
                    this@IBeaconMonitoringService,
                    0,
                    notificationIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )

                // Generate a unique notification ID
                val notificationId = System.currentTimeMillis().toInt()

                val notification: Notification =
                    NotificationCompat.Builder(this@IBeaconMonitoringService, channelId)
                        .setContentTitle("iBeacons Books Due Reminder")
                        .setContentText("Books due: $booksList")
                        .setSmallIcon(R.drawable.ic_notification)
                        .setContentIntent(pendingIntent)
                        .build()

                notificationManager.notify(notificationId, notification)
                Log.d("IBeaconMonitoringService", "Notification sent: $booksList")
            } else {
                Log.d("IBeaconMonitoringService", "No due books found")
            }
        }
    }


    private suspend fun getDueBooks(): List<Book> {
        return bookRepository.getBooksListToReturn()
    }


    override fun onDestroy() {
        super.onDestroy()
        try {
            beaconManager.stopMonitoring(region)
            beaconManager.stopRangingBeacons(region)
            Log.d("IBeaconMonitoringService", "Stopped monitoring and ranging beacons")
        } catch (e: RemoteException) {
            Log.e("IBeaconMonitoringService", "Error stopping monitoring or ranging", e)
            e.printStackTrace()
        }
    }


}
