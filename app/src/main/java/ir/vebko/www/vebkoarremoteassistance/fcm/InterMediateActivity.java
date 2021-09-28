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




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inter_mediate);

        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(1);



    }

    @Override
    protected void onResume() {
        super.onResume();

    }
}