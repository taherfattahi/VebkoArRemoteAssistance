package ir.vebko.www.vebkoarremoteassistance.fcm;

import android.app.NotificationManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import ir.vebko.www.vebkoarremoteassistance.R;
import ir.vebko.www.vebkoarremoteassistance.nodejs.WebrtcUtil;


public class
InterMediateActivity extends AppCompatActivity {

    private NotificationManager notificationManager;

    private String signalIp = "ws://185.208.172.104:3000/ws";

    public String randomUniqueId;

    private SharedPreferences sharedPrefs;
    private static final String PREF_IMEI_UNIQUE_ID = "PREF_IMEI_UNIQUE_ID";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inter_mediate);

        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(1);

        sharedPrefs = this.getSharedPreferences(PREF_IMEI_UNIQUE_ID, Context.MODE_PRIVATE);
        randomUniqueId = sharedPrefs.getString("randomUniqueId", null);

        WebrtcUtil.callSingle1(InterMediateActivity.this,
                signalIp,
                randomUniqueId,
                true, "", "", "", "", "", "");

    }

    @Override
    protected void onResume() {
        super.onResume();

    }
}