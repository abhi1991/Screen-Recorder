package shashank.com.screenrecorder

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.IBinder
import android.widget.RemoteViews

class RecordService : Service() {

    private var notificationManager: NotificationManager? = null
    private var notification: Notification? = null

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()

        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val expandedView = RemoteViews(packageName, R.layout.notification_expanded)
        val stopIntent = PendingIntent.getBroadcast(this, 0, Intent(NotificationCallbacks.STOP), 0)
        val pendingIntent = PendingIntent.getActivity(this, 0, Intent(this,
                MainActivity::class.java).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP),
                PendingIntent.FLAG_UPDATE_CURRENT)

        expandedView.setOnClickPendingIntent(R.id.stop, stopIntent)

        val notificationBuilder = Notification.Builder(this).setOngoing(true).setContentTitle("Title")
                .setContentText("Text").setAutoCancel(true)
        notification = notificationBuilder.build()
        notification!!.contentIntent = pendingIntent
        notification!!.bigContentView = expandedView
        notification!!.icon = R.drawable.ic_stat_videocam

        val intentFilter = IntentFilter()
        intentFilter.addAction(NotificationCallbacks.PLAY_PAUSE)
        intentFilter.addAction(NotificationCallbacks.STOP)

        registerReceiver(receiver, intentFilter)
        startForeground(FOREGROUND_NOTIFICATION, notification)

        notificationManager!!.notify(FOREGROUND_NOTIFICATION, notification)
    }

    internal var receiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {
                NotificationCallbacks.STOP -> {
                    ScreenRecordHelper.stopRecording()
                    unregisterReceiver(this)
                    stopForeground(true)
                    stopSelf()
                    notificationManager!!.cancel(FOREGROUND_NOTIFICATION)
                }

                NotificationCallbacks.PLAY_PAUSE -> if (ScreenRecordHelper.isRecording()) {
                    ScreenRecordHelper.pauseRecorder()
                } else {
                    ScreenRecordHelper.resumeRecorder()
                }
            }
        }
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        return Service.START_STICKY
    }

    companion object {
        val FOREGROUND_NOTIFICATION = 0

        private val TAG = RecordService::class.java.simpleName
    }
}