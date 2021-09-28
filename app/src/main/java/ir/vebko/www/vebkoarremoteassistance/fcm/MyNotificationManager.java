package ir.vebko.www.vebkoarremoteassistance.fcm;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.widget.RemoteViews;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import ir.vebko.www.vebkoarremoteassistance.R;

import static android.content.Context.NOTIFICATION_SERVICE;


public class MyNotificationManager {
    private static final String TAG = "MyNotificationManager";

    private Context context;
    private static MyNotificationManager instance;
    private NotificationManagerCompat notificationManagerCompat;
    private NotificationManager notificationManager;

    private MyNotificationManager(Context context){
        this.context = context;
        notificationManagerCompat = NotificationManagerCompat.from(context);
        notificationManager = (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);
    }

    public static MyNotificationManager getInstance(Context context){
        if (instance == null){
            instance = new MyNotificationManager(context);
        }
        return instance;
    }

    public void registerNotificationChannel(String channelID, String channelName, String channelDescription){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            NotificationChannel notificationChannel = new NotificationChannel(channelID, channelName, NotificationManager.IMPORTANCE_HIGH);
            notificationChannel.setDescription(channelDescription);
            NotificationManager manager = context.getSystemService(NotificationManager.class);
            manager.createNotificationChannel(notificationChannel);
        }
    }

    public void triggerNotification(Class targetNotificationActivity, String channelID, String title, String text, String bigText, int priority, boolean autoCancel, int notificationID){
        Intent intent = new Intent(context, targetNotificationActivity);
        intent.putExtra("count", title);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, channelID)
                .setSmallIcon(R.mipmap.ic_launcher_round)
                .setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.mipmap.ic_launcher_round))
                .setContentTitle(title)
                .setContentText(text)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(bigText))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setChannelId(channelID)
                .setAutoCancel(autoCancel);

        NotificationManagerCompat compat = NotificationManagerCompat.from(context);
        compat.notify(notificationID, builder.build());

    }

    public void triggerNotification(Class targetNotificationActivity, String channelId, String title, String text, String bigText, int priority, boolean autoCancel, int notificationId, int pendingIntentFlag){

        Intent intent = new Intent(context, targetNotificationActivity);
        intent.putExtra("count", title);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK |Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, pendingIntentFlag);


        NotificationCompat.Builder builder = new NotificationCompat.Builder(context,channelId)
                .setSmallIcon(R.mipmap.ic_launcher_round)
                .setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.mipmap.ic_launcher_round))
                .setContentTitle(title)
                .setContentText(text)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(bigText))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setChannelId(channelId)
                .setAutoCancel(true);

        notificationManagerCompat.notify(notificationId,builder.build());
    }

    public void triggerNotificationWithBackStack(Class targetNotificationActivity, String channelId, String title, String text, String bigText, int priority, boolean autoCancel, int notificationId, int pendingIntentFlag){


//        RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.notification_expanded);
//        String strtitle = "salam";
//        String strtext = "saaaaaaaaaaaaaaaaaaalam";
//        Intent intent = new Intent(context, InterMediateActivity.class);
//        intent.putExtra("title", strtitle);
//        intent.putExtra("text", strtext);
//        PendingIntent pIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
//        NotificationCompat.Builder builder = new NotificationCompat.Builder(context)
//                .setSmallIcon(R.drawable.vebko3)
//                .setTicker("getString(R.string.customnotificationticker)")
//                .setAutoCancel(true)
//                .setContentIntent(pIntent)
//                .setContent(remoteViews);
////        remoteViews.setImageViewResource(R.id.imagenotileft,R.drawable.ic_launcher);
////        remoteViews.setImageViewResource(R.id.imagenotiright,R.drawable.androidhappy);
////        remoteViews.setTextViewText(R.id.title,getString(R.string.customnotificationtitle));
////        remoteViews.setTextViewText(R.id.text,getString(R.string.customnotificationtext));
//        // Create Notification Manager
//        NotificationManager notificationmanager = (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);
//        // Build Notification with Notification Manager
//        notificationmanager.notify(0, builder.build());

//        RemoteViews collapsedView = new RemoteViews(context.getPackageName(),
//                R.layout.notification_collapsed);
//        RemoteViews expandedView = new RemoteViews(context.getPackageName(),
//                R.layout.notification_expanded);
//        Intent clickIntent = new Intent(context, InterMediateActivity.class);
//        PendingIntent clickPendingIntent = PendingIntent.getBroadcast(context,
//                0, clickIntent, 0);
//        collapsedView.setTextViewText(R.id.text_view_collapsed_1, "Hello World!");
//        expandedView.setImageViewResource(R.id.image_view_expanded, R.drawable.vebko3);
//        expandedView.setOnClickPendingIntent(R.id.image_view_expanded, clickPendingIntent);
//        Notification notification = new NotificationCompat.Builder(context, channelId)
//                .setSmallIcon(R.drawable.vebko3)
//                .setCustomContentView(collapsedView)
//                .setCustomBigContentView(expandedView)
//                //.setStyle(new NotificationCompat.DecoratedCustomViewStyle())
//                .build();
//        notificationManager.notify(1, notification);


        Intent intent = new Intent(context, targetNotificationActivity);
        TaskStackBuilder taskStackBuilder = TaskStackBuilder.create(context);
        taskStackBuilder.addNextIntentWithParentStack(intent);
        intent.putExtra("count", title);
        PendingIntent pendingIntent = taskStackBuilder.getPendingIntent(0, pendingIntentFlag);


        //todo
        Intent answerCallAction = new Intent(context, InterMediateActivity.class);
        answerCallAction.putExtra("ConstantApp.CALL_RESPONSE_ACTION_KEY", "ConstantApp.CALL_CANCEL_ACTION");
        answerCallAction.putExtra("ACTION_TYPE", "CANCEL_CALL");
//        cancelCallAction.putExtra("NOTIFICATION_ID",NOTIFICATION_ID);
        answerCallAction.putExtra("NOTIFICATION_ID", channelId);
        answerCallAction.putExtra("videoEnable", true);
        answerCallAction.setAction("CANCEL_CALL");

        Intent receiveCallAction = new Intent(context, InterMediateActivity.class);
        receiveCallAction.putExtra("ConstantApp.CALL_RESPONSE_ACTION_KEY", "ConstantApp.CALL_RECEIVE_ACTION");
        receiveCallAction.putExtra("ACTION_TYPE", "RECEIVE_CALL");
//        receiveCallAction.putExtra("NOTIFICATION_ID",NOTIFICATION_ID);
        receiveCallAction.putExtra("NOTIFICATION_ID",2);
        receiveCallAction.setAction("RECEIVE_CALL");


        PendingIntent answerCallPendingIntent = PendingIntent.getActivity(context, 1201, answerCallAction, PendingIntent.FLAG_UPDATE_CURRENT);

//        Intent intent1 = new Intent(context, cancelCallAction.);
        TaskStackBuilder taskStackBuilder1 = TaskStackBuilder.create(context);
        taskStackBuilder1.addNextIntentWithParentStack(answerCallAction);
//        intent.putExtra("count", title);
        PendingIntent pendingIntent1 = taskStackBuilder1.getPendingIntent(0, pendingIntentFlag);

        PendingIntent receiveCallPendingIntent = PendingIntent.getBroadcast(context, 1200, receiveCallAction, PendingIntent.FLAG_UPDATE_CURRENT);

//        NotificationManager manager = (NotificationManager) PendingIntent.getService(Context.NOTIFICATION_SERVICE);

        //Create an Intent for the BroadcastReceiver
        Intent intentDismiss = new Intent(context, HandleBroadcastReceiver.class);
        //Create the PendingIntent
        PendingIntent pIntentDismiss = PendingIntent.getBroadcast(context, 0, intentDismiss, 0);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, channelId)
                .setSmallIcon(R.mipmap.ic_launcher_round)
                .setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.mipmap.ic_launcher_round))
                .setContentTitle(title)
                .setContentText(text)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(bigText))
//                .setPriority(NotificationCompat.PRIORITY_MAX)
//                .setCategory(NotificationCompat.CATEGORY_CALL)
//                .addAction(R.mipmap.ic_launcher_round, "Decline", receiveCallPendingIntent)
                .addAction(android.R.drawable.ic_menu_close_clear_cancel, "Decline", pIntentDismiss)
                .addAction(R.mipmap.ic_launcher_round, "Answer", answerCallPendingIntent)
                .setContentIntent(pendingIntent)
                .setChannelId(channelId)
                .setAutoCancel(false)
                .setOngoing(true)
                .setShowWhen(true);
//                .setFullScreenIntent(cancelCallPendingIntent, true);



        notificationManagerCompat.notify(notificationId,builder.build());
    }

    public void updateWithPicture(Class targetNotificationActivity,String title,String text, String channelId, int notificationId, String bigpictureString, int pendingIntentflag) {

        Intent intent = new Intent(context, targetNotificationActivity);
        intent.putExtra("count", title);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK |Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, pendingIntentflag);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context,channelId)
                .setSmallIcon(R.mipmap.ic_launcher_round)
                .setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.mipmap.ic_launcher_round))
                .setContentTitle(title)
                .setContentText(text)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setChannelId(channelId)
                .setOngoing(true)
                .setAutoCancel(true);

        Bitmap androidImage = BitmapFactory.decodeResource(context.getResources(), R.mipmap.ic_launcher_round);
        builder.setStyle(new NotificationCompat.BigPictureStyle().bigPicture(androidImage).setBigContentTitle(bigpictureString));
        notificationManager.notify(notificationId, builder.build());
    }

    public void cancelNotification(int notificationId){
        notificationManager.cancel(notificationId);
    }

}
