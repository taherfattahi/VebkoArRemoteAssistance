package ir.vebko.www.vebkoarremoteassistance.fcm;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;

import com.androidnetworking.AndroidNetworking;

import org.w3c.dom.Node;

import ir.vebko.www.vebkoarremoteassistance.nodejs.NodejsActivity;
import ir.vebko.www.vebkoarremoteassistance.nodejs.WebrtcUtil;


public class HandleAnswerReceiver extends BroadcastReceiver {

    private String signalIp = "ws://136.243.172.245:3000/ws";

    private SharedPreferences sharedPrefs;
    private static final String PREF_IMEI_UNIQUE_ID = "PREF_IMEI_UNIQUE_ID";

    public String randomUniqueId;

    @Override
    public void onReceive(Context context, Intent intent) {
//        Toast.makeText(context, "Notification Dialog Closed", Toast.LENGTH_LONG).show();
//        Log.d("Notification:", "Notification Dialog Closed");

        AndroidNetworking.initialize(context);

        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        manager.cancel(1);

//        PendingIntent resultPendingIntent = PendingIntent.getActivity(context, 0, new Intent(), 0);
//        NotificationCompat.Builder mb = new NotificationCompat.Builder(context);
//        mb.setContentIntent(resultPendingIntent);

        sharedPrefs = context.getSharedPreferences(PREF_IMEI_UNIQUE_ID, Context.MODE_PRIVATE);
        randomUniqueId = sharedPrefs.getString("randomUniqueId", null);

//        WebrtcUtil.callSingle1(context, NodejsActivity.nodejsActivity,
        WebrtcUtil.callSingle1(context, null,
                signalIp,
                randomUniqueId,
                true,  sharedPrefs.getString("senderRandomUniqueId", null), "", "", "", "", "");
    }
}
