package me.tbsten.notificationpractice.notify

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import me.tbsten.notificationpractice.R

@Composable
fun NotifyScreen() {
    val context = LocalContext.current
    Column {
        Button(onClick = {
            pushNotification(context)
        }) {
            Text("通知を送信")
        }
    }
}

private const val CHANNEL_ID = "test-notification"

fun pushNotification(
    context: Context,
) {
    val builder = NotificationCompat.Builder(context, CHANNEL_ID)
        .setSmallIcon(R.drawable.ic_launcher_foreground)
        .setContentTitle("notification title")
        .setContentText("notification content")
        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
    val notification = builder.build()

    createNotificationChannel(context)

    val notificationId = 0
    if (ActivityCompat.checkSelfPermission(
            context,
            Manifest.permission.POST_NOTIFICATIONS
        ) != PackageManager.PERMISSION_GRANTED
    ) {
        throw IllegalStateException("none permission . please request POST_NOTIFICATIONS .")
    }
    NotificationManagerCompat.from(context)
        .notify(notificationId, notification)
}

private fun createNotificationChannel(context: Context) {
    // Create the NotificationChannel, but only on API 26+ because
    // the NotificationChannel class is new and not in the support library
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val name = "channel name"
        val descriptionText = "channel description"
        val importance = NotificationManager.IMPORTANCE_DEFAULT
        val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
            description = descriptionText
        }
        val notificationManager: NotificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }
}