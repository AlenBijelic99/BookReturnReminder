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
import kotlinx.coroutines.runBlocking
import org.altbeacon.beacon.*

/**
 * DMA project - Managing reminder for borrowed Books at the Library - scan book barcode
 * and detection of iBeacons in a foreground service.
 * @author Bijelic Alen & Bogale Tegest
 * @Date 10.06.2024
 * Service that monitors iBeacons & send notifications for book returns.
 * Notification are sent based on the following conditions:
 *
 * - when the user is within 5 meters of the beacon.
 * - every hour if the user remains in the region and the message content hasn't changed.
 * - when the user re-enters the region, regardless of the message content has not change.
 * - when message content has changed regardless of the time interval.
 *
 * The service is started in the foreground to ensure it continues running even when the app is in the background.
 */
class IBeaconMonitoringService : LifecycleService() {

    private lateinit var beaconManager: BeaconManager
    private lateinit var bookRepository: BookRepository

    // Change the UUID, major and minor to match the iBeacon used
    private val region = Region(
        "libraryRegion",
        Identifier.parse("ebefd083-70a2-47c8-9837-e7b5634df670"),// UUID of the ibeacon used
        Identifier.parse("1"), // Major of the ibeacon used
        Identifier.parse("69") // Minor of the ibeacon used
    )
    private var isInRegion = false
    private var lastNotificationTime: Long = 0
    private val notificationInterval = 3600000 // 1 hour in milliseconds
    private var lastNotificationMsg : String? = null

    override fun onCreate() {
        super.onCreate()
        Log.d("IBeaconMonitoringService", "Service created")
        bookRepository = BookRepository(applicationContext as Application)
        startForegroundService()
        setupBeaconManager()

        Log.d("IBeaconMonitoringService", "Service created and foreground service started")

    }

    /**
     * Starts the service in the foreground with a notification.
     */
    private fun startForegroundService() {
        Log.d("IBeaconMonitoringService", "Starting foreground service")
        val channelId = "foreground_service_channel"
        val channelName = "Foreground Service"
        val importance = NotificationManager.IMPORTANCE_LOW
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
            .setSmallIcon(R.drawable.ic_ibeacon_notification)
            .setContentIntent(pendingIntent)
            .build()

        startForeground(1, notification)
    }

    /**
     * Sets up the beacon manager to monitor and range the beacons.
     */
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
                isInRegion = true
                startRangingBeacons()
                sendNotification() // Send a notification when re-entering the region
            }

            override fun didExitRegion(region: Region) {
                Log.d("IBeaconMonitoringService", "Exited region: ${region.id1}")
                isInRegion = false
                stopRangingBeacons()
            }

            override fun didDetermineStateForRegion(state: Int, region: Region) {
                if (state == MonitorNotifier.INSIDE) {
                    Log.d("IBeaconMonitoringService", "Inside region: ${region.id1}")
                    if (!isInRegion) {
                        isInRegion = true
                        startRangingBeacons()
                        sendNotification()
                    }
                } else {
                    Log.d("IBeaconMonitoringService", "Outside region: ${region.id1}")
                    isInRegion = false
                }
            }
        })


        try {
            beaconManager.startMonitoring(region)
            beaconManager.startRangingBeacons(region)
            Log.d("IBeaconMonitoringService", "Started monitoring and ranging beacons")
        } catch (e: RemoteException) {
            Log.e("IBeaconMonitoringService", "Error starting monitoring or ranging", e)
            e.printStackTrace()
        }
    }

    /**
     * Starts ranging the beacons.
     */
    private fun startRangingBeacons() {
        try {
            beaconManager.startRangingBeacons(region)
            Log.d("IBeaconMonitoringService", "Started ranging beacons")
        } catch (e: RemoteException) {
            Log.e("IBeaconMonitoringService", "Error starting ranging", e)
            e.printStackTrace()
        }

        beaconManager.addRangeNotifier { beacons, _ ->
            if (beacons.isNotEmpty()) {
                val nearestBeacon = beacons.minByOrNull { it.distance }
                nearestBeacon?.let {
                    Log.d("IBeaconMonitoringService", "Nearest beacon: ${it.id1}, Distance: ${it.distance} meters")
                    if (it.distance < 5.0) { // Check if the beacon is within 5 meters
                        val currentTime = System.currentTimeMillis()
                        if (shouldSendNotification(currentTime)) {
                            sendNotification()
                            lastNotificationTime = currentTime
                        }
                    }
                }
            }
        }
    }

    /**
     * Stops ranging beacons in the region.
     */
    private fun stopRangingBeacons() {
        try {
            beaconManager.stopRangingBeacons(region)
            Log.d("IBeaconMonitoringService", "Stopped ranging beacons")
        } catch (e: RemoteException) {
            Log.e("IBeaconMonitoringService", "Error stopping ranging", e)
            e.printStackTrace()
        }
    }
    /**
     * Determines whether a notification should be sent based on the time and message content.
     * @param currentTime The current system time in milliseconds.
     * @return True if a notification should be sent, false otherwise.
     */
    private fun shouldSendNotification(currentTime: Long): Boolean {
        val dueBooks = getDueBooksSync()
        val booksList = dueBooks.joinToString(", ") { it.title }

        return if (booksList != lastNotificationMsg) {
            lastNotificationMsg = booksList
            true
        } else {
            currentTime - lastNotificationTime > notificationInterval
        }
    }

    /**
     * Sends a notification if there are books due to be returned.
     */
    private fun sendNotification() {
        Log.d("IBeaconMonitoringService", "Sending notification")
        lifecycleScope.launch(Dispatchers.IO) {
            val dueBooks = getDueBooks()
            if (dueBooks.isNotEmpty()) {
                val booksList = dueBooks.joinToString(", ") { it.title }
                Log.d("IBeaconMonitoringService", "Due books found: $booksList")

                val channelId = "library_channel_id"
                val channelName = "Library Notification"
                val importance = NotificationManager.IMPORTANCE_HIGH

                val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                val channel = NotificationChannel(channelId, channelName, importance)
                notificationManager.createNotificationChannel(channel)

                val notificationIntent = Intent(this@IBeaconMonitoringService, MainActivity::class.java)
                val pendingIntent = PendingIntent.getActivity(
                    this@IBeaconMonitoringService, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )

                // Generate a unique notification ID
                val notificationId = System.currentTimeMillis().toInt()

                val notification: Notification = NotificationCompat.Builder(this@IBeaconMonitoringService, channelId)
                    .setContentTitle("iBeacons Books Due Reminder")
                    .setContentText("Books due: $booksList")
                    .setSmallIcon(R.drawable.ic_ibeacon_notification)
                    .setContentIntent(pendingIntent)
                    .build()

                notificationManager.notify(notificationId, notification)
                Log.d("IBeaconMonitoringService", "Notification sent: $booksList")

                // Update the last notification message and time
                lastNotificationMsg = booksList
                lastNotificationTime = System.currentTimeMillis()
            } else {
                Log.d("IBeaconMonitoringService", "No due books found")
            }
        }
    }


    /**
     * Gets the list of books that are due to be returned.
     */
    private suspend fun getDueBooks(): List<Book> {
        return bookRepository.getBooksListToReturn()
    }

    /**
     * Retrieves the list of books that are due for return.
     * This is a synchronous version used within the notification check logic.
     */
    private fun getDueBooksSync(): List<Book> {
        return runBlocking {
            bookRepository.getBooksListToReturn()
        }
    }


    /**
     * Stops monitoring and ranging the beacons when the service is destroyed.
     */
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
