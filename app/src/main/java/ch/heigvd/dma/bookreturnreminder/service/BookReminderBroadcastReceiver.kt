package ch.heigvd.dma.bookreturnreminder.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationCompat
import ch.heigvd.dma.bookreturnreminder.R

/**
 * DMA project - Managing reminder for borrowed Books at the Library - scan book barcode
 * and detection of iBeacons in a foreground service.
 * @author Bijelic Alen & Bogale Tegest
 * @Date 10.06.2024
 * Broadcast receiver that listens for book reminder broadcasts and sends a notification.
 */
class BookReminderBroadcastReceiver : BroadcastReceiver() {
    /**
     * Called when a broadcast is received.
     * @param context The context in which the receiver is running.
     * @param intent The intent being received.
     */
    override fun onReceive(context: Context, intent: Intent) {
        val bookTitle = intent.getStringExtra("bookTitle")
        Log.d("BookReminderBroadcastReceiver", "Received broadcast for book: $bookTitle")
        sendNotification(context, "Reminder", "Return the book: $bookTitle")
    }

    /**
     * Sends a notification to remind the user to return a book.
     * @param context The context in which the receiver is running.
     * @param title The title of the notification.
     * @param message The message of the notification.
     */
    private fun sendNotification(context: Context, title: String, message: String) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val channelId = "reminder_channel"
        val channel = NotificationChannel(channelId, "Reminder Channel", NotificationManager.IMPORTANCE_HIGH)
        notificationManager.createNotificationChannel(channel)

        val notification = NotificationCompat.Builder(context, channelId)
            .setContentTitle(title)
            .setContentText(message)
            .setSmallIcon(R.drawable.ic_notification)
            .build()

        // Generate a unique notification ID
        val notificationId = System.currentTimeMillis().toInt()

        notificationManager.notify(notificationId, notification)
        Log.d("BookReminderBroadcastReceiver", "Notification sent: $title")
    }
}
