package ir.vebko.www.vebkoarremoteassistance;

import android.content.Intent;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;

import androidx.appcompat.app.AppCompatActivity;

import in.codeshuffle.typewriterview.TypeWriterView;
import ir.vebko.www.vebkoarremoteassistance.nodejs.NodejsActivity;


public class SplashScreen extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.splash_activity);

        TypeWriterView typeWriterView = (TypeWriterView) findViewById(R.id.typeWriterView);
        typeWriterView.setDelay(140);
        typeWriterView.setWithMusic(false);
        typeWriterView.animateText("vebko.ir");

        Thread myThread = new Thread() {
            @Override
            public void run() {
                try {
//                    if (!checkPermission()) {
//                        requestPermission();
//                    }else {
                    sleep(1800);
                    Intent intent = new Intent(getApplicationContext(), NodejsActivity.class);
                    startActivity(intent);
                    finish();
//                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        };
        myThread.start();


//        sharedPrefs = this.getSharedPreferences(PREF_IMEI_UNIQUE_ID, Context.MODE_PRIVATE);
//
//        imeiUniqueID = sharedPrefs.getString(PREF_IMEI_UNIQUE_ID, null);
//
//        if (imeiUniqueID == null) {
//            addProfileToServer(getUniqueID());
//        } else {
//            getProfileFromServer(imeiUniqueID);
//        }

    }



}
