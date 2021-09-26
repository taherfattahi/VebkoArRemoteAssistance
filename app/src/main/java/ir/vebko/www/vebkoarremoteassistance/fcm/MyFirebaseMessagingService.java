package ir.vebko.www.vebkoarremoteassistance.fcm;

import android.app.PendingIntent;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import ir.vebko.www.vebkoarremoteassistance.MyApplication;
import ir.vebko.www.vebkoarremoteassistance.R;

public class MyFirebaseMessagingService extends FirebaseMessagingService {
    private static final String TAG = "MyFirebaseMessaging";

    @Override
    public void onNewToken(@NonNull String s) {
        super.onNewToken(s);
        Log.i(TAG, "onNewToken: " + s);
    }

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        Log.i(TAG, "onMessageReceived: Message received: " + remoteMessage.getData().get("title"));

        /*((MyApplication)getApplication()).triggerNotificationWithBackStack(NotificationDetailsActivity.class,
                getString(R.string.NEWS_CHANNEL_ID),
                //remoteMessage.getNotification().getTitle(),
                remoteMessage.getData().get("title"),
                remoteMessage.getNotification().getBody(),
                "This is from FCM console",
                NotificationCompat.PRIORITY_HIGH,
                true,
                getResources().getInteger(R.integer.notificationId),
                PendingIntent.FLAG_UPDATE_CURRENT);*/
        Log.e(TAG, "onMessageReceived: " + remoteMessage.getData().get("title"));
        Log.e(TAG, "onMessageReceived: " + remoteMessage.getData().get("content"));
        Log.e(TAG, "onMessageReceived: " + remoteMessage.getData().get("button"));

        ((MyApplication)getApplication()).triggerNotificationWithBackStack(NotificationDetailsActivity.class,
                getString(R.string.NEWS_CHANNEL_ID),
                //remoteMessage.getNotification().getTitle(),
                remoteMessage.getData().get("title"),
                remoteMessage.getData().get("content"),
                remoteMessage.getData().get("content"),
                NotificationCompat.PRIORITY_HIGH,
                true,
                getResources().getInteger(R.integer.notificationId),
                PendingIntent.FLAG_UPDATE_CURRENT);

    }
}

