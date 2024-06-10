package ch.heigvd.dma.bookreturnreminder.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationCompat
import ch.heigvd.dma.bookreturnreminder.R



class BookReminderBroadcastReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val bookTitle = intent.getStringExtra("bookTitle")
        Log.d("BookReminderBroadcastReceiver", "Received broadcast for book: $bookTitle")
        sendNotification(context, "Reminder", "Return the book: $bookTitle")
    }

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
