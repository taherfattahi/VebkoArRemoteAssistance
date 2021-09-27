package ir.vebko.www.vebkoarremoteassistance.nodejs;

import android.Manifest;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONArrayRequestListener;
import com.androidnetworking.interfaces.JSONObjectRequestListener;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.ar.core.ArCoreApk;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.UUID;

import ir.alirezabdn.wp7progress.WP7ProgressBar;
import ir.vebko.www.vebkoarremoteassistance.R;
import ir.vebko.www.vebkoarremoteassistance.vuforia.ImagePlayback;

public class NodejsActivity extends AppCompatActivity {

    private EditText edtRemoteID;
    private EditText et_room;

    private SharedPreferences sharedPrefs;
    private static final String PREF_IMEI_UNIQUE_ID = "PREF_IMEI_UNIQUE_ID";
    private static final int PERMISSION_REQUEST_CODE = 100;

    public String FCM_TOKEN = null;

    public String randomUniqueId;
    public String imeiUniqueID;
    public String tokenRegistrationFCM;
    public String firstName;
    public String lastName;
    public String phoneNumber;


    private String signalIp = "ws://185.208.172.104:3000/ws";

    private Button btnArCore;
    private Button btnVuforia;

    private Toolbar toolbar;
    private TextView mTitleRandomUniqueId;
    private WP7ProgressBar progressBar;

    private boolean isGetDataFromServer;
    private boolean installRequested;

    private ArrayList<Contact> contacts = new ArrayList<>();
    private RecyclerView rvDestinationUniqueId;
    private ContactsAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nodejs);

        sharedPrefs = this.getSharedPreferences(PREF_IMEI_UNIQUE_ID, Context.MODE_PRIVATE);
        imeiUniqueID = sharedPrefs.getString(PREF_IMEI_UNIQUE_ID, null);

        initView();

        if (imeiUniqueID == null) {
            addProfileToServer(getUniqueID());
        } else {
            getProfileFromServer(imeiUniqueID);
            getContactFromServer(randomUniqueId);
        }

    }

    private void initView() {

        toolbar = (Toolbar) findViewById(R.id.toolbar_top);
        mTitleRandomUniqueId = (TextView) toolbar.findViewById(R.id.toolbar_title);
        progressBar = findViewById(R.id.prgToolbar);
        btnArCore = findViewById(R.id.btnArCore);
        btnVuforia = findViewById(R.id.btnVuforia);
        edtRemoteID = findViewById(R.id.edtRemoteID);
        rvDestinationUniqueId = (RecyclerView) findViewById(R.id.rvDestinationUniqueId);

        progressBar.setVisibility(View.VISIBLE);
        progressBar.showProgressBar();

        randomUniqueId = sharedPrefs.getString("randomUniqueId", null);
        imeiUniqueID = sharedPrefs.getString(PREF_IMEI_UNIQUE_ID, null);
        tokenRegistrationFCM = sharedPrefs.getString("tokenRegistrationFCM", null);
        firstName = sharedPrefs.getString("firstName", null);
        lastName = sharedPrefs.getString("lastName", null);
        phoneNumber = sharedPrefs.getString("phoneNumber", null);

        adapter = new ContactsAdapter(contacts);
        rvDestinationUniqueId.setAdapter(adapter);
        rvDestinationUniqueId.setLayoutManager(new LinearLayoutManager(this));

        rvDestinationUniqueId.addOnItemTouchListener(new RecyclerTouchListener(getApplicationContext(), rvDestinationUniqueId, new RecyclerTouchListener.ClickListener() {
            @Override
            public void onClick(View view, int position) {
//                speech(countries_list_code[position]);
                edtRemoteID.setText(contacts.get(position).getName());
            }

            @Override
            public void onLongClick(View view, int position) {

            }
        }));


//        mTitle.setText(randomUniqueId);


        mTitleRandomUniqueId.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent share = new Intent(Intent.ACTION_SEND);
                share.setType("text/html");
                share.putExtra(Intent.EXTRA_TEXT, randomUniqueId);
                startActivity(Intent.createChooser(share, "share unique ID"));
            }
        });

        mTitleRandomUniqueId.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("label", randomUniqueId);
                clipboard.setPrimaryClip(clip);

                Toast.makeText(getApplicationContext(), "Copied: " + randomUniqueId, Toast.LENGTH_SHORT).show();
                return true;
            }
        });

        btnArCore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                JoinRoomSingleVideo();
            }
        });

        btnVuforia.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!checkPermission()) {
                    requestPermission();
                }else {
                    Intent intent = new Intent(getApplicationContext(), ImagePlayback.class);
                    startActivity(intent);
                }
            }
        });


    }


    public void JoinRoomSingleVideo() {
//        WebrtcUtil.callSingle(NodejsActivity.this,
//                signalIp,
//                "232343",
//                true, edtRemoteID.getText().toString().trim(), imeiUniqueID, "jsonObject.getString(\"tokenRegistrationFCM\")", firstName, lastName, phoneNumber);

        try{
            switch (ArCoreApk.getInstance().requestInstall(this, !installRequested)) {
                case INSTALL_REQUESTED:
                    installRequested = true;
                    return;
                case INSTALLED:
                    break;
            }
        }catch (Exception ex){
            ex.printStackTrace();
        }

        if (isGetDataFromServer) {
            if (edtRemoteID.getText().toString().trim().length() != 0) {
                AndroidNetworking.get("http://192.168.0.13:3000/api/Profile/getprofileuniqueid")
                        .addQueryParameter("randomUniqueId", edtRemoteID.getText().toString().trim())
                        .setTag("getProfileUniqueId")
                        .setPriority(Priority.HIGH)
                        .build()
                        .getAsJSONObject(new JSONObjectRequestListener() {
                            @Override
                            public void onResponse(JSONObject jsonObject) {
                                try {
                                    WebrtcUtil.callSingle(NodejsActivity.this,
                                            signalIp,
                                            edtRemoteID.getText().toString().trim(),
                                            true, randomUniqueId, imeiUniqueID, jsonObject.getString("tokenRegistrationFCM"), firstName, lastName, phoneNumber);
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }

                            @Override
                            public void onError(ANError anError) {
                                anError.printStackTrace();
                                Toast.makeText(getApplicationContext(), "please check your connection", Toast.LENGTH_SHORT).show();
                            }
                        });
            } else {
                Toast.makeText(getApplicationContext(), "please insert remote ID", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(getApplicationContext(), "please check your connection", Toast.LENGTH_SHORT).show();
        }

    }

    public String getUniqueID() {
        imeiUniqueID = Settings.Secure.getString(this.getContentResolver(), Settings.Secure.ANDROID_ID);

        if (imeiUniqueID == null) {
            imeiUniqueID = sharedPrefs.getString(PREF_IMEI_UNIQUE_ID, null);
            if (imeiUniqueID == null) {
                imeiUniqueID = UUID.randomUUID().toString();
            }
        }

        return imeiUniqueID;
    }



    public void getContactFromServer(String myRandomUniqueId) {

//        [
//        {
//            "id": "c237b42e-fe80-49f1-9dc9-59062d3464f7",
//                "myRandomUniqueIdProfile": "4b2a4c12",
//                "destinationRandomUniqueIdProfile": "9044c589"
//        }
//]
        AndroidNetworking.get("http://192.168.0.13:3000/api/Contact")
                .addQueryParameter("randomUniqueIdProfile", myRandomUniqueId) // posting json
                .setTag("GetContact")
                .setPriority(Priority.HIGH)
                .build()
                .getAsJSONArray(new JSONArrayRequestListener() {
                    @Override
                    public void onResponse(JSONArray jsonArray) {

                        for (int i=0; i<jsonArray.length(); i++){
                            try {
                                JSONObject jsonObject = jsonArray.getJSONObject(i);

                                if (jsonObject.getString("myRandomUniqueIdProfile").equals(randomUniqueId)){
                                    Contact contact = new Contact();
                                    contact.setName(jsonObject.getString("destinationRandomUniqueIdProfile"));
                                    contacts.add(contact);
                                }else{
                                    Contact contact = new Contact();
                                    contact.setName(jsonObject.getString("myRandomUniqueIdProfile"));
                                    contacts.add(contact);
                                }

                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }

                        adapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onError(ANError anError) {

                    }
                });
    }

    public void getProfileFromServer(String myImeiUniqueID) {
        FirebaseInstanceId.getInstance().getInstanceId().addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
            @Override
            public void onComplete(@NonNull Task<InstanceIdResult> task) {
                if (!task.isSuccessful()) {
                    Log.i("Token:  ", "onComplete: Task failed");
                    return;
                }
                Log.i("Token:  ", "onComplete: The result: " + task.getResult().getToken());

                AndroidNetworking.post("http://192.168.0.13:3000/api/Profile/getprofileimei")
                        .addQueryParameter("imei", myImeiUniqueID)
                        .addQueryParameter("tokenRegistrationFCM", task.getResult().getToken())
                        .setTag("getProfile")
                        .setPriority(Priority.HIGH)
                        .build()
                        .getAsJSONObject(new JSONObjectRequestListener() {
                            @Override
                            public void onResponse(JSONObject response) {
                                try {
                                    randomUniqueId = response.getString("randomUniqueId");
                                    tokenRegistrationFCM = response.getString("tokenRegistrationFCM");
                                    firstName = response.getString("firstName");
                                    lastName = response.getString("lastName");
                                    phoneNumber = response.getString("phoneNumber");

                                    SharedPreferences.Editor editor = sharedPrefs.edit();
                                    editor.putString("randomUniqueId", randomUniqueId);
                                    editor.putString("tokenRegistrationFCM", tokenRegistrationFCM);
                                    editor.putString("firstName", firstName);
                                    editor.putString("lastName", lastName);
                                    editor.putString("phoneNumber", phoneNumber);
                                    editor.commit();

                                    progressBar.hideProgressBar();
                                    progressBar.setVisibility(View.GONE);
                                    mTitleRandomUniqueId.setText(randomUniqueId);

                                    isGetDataFromServer = true;

                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }

                            @Override
                            public void onError(ANError anError) {
                                anError.printStackTrace();
                                Toast.makeText(getApplicationContext(), "please check your connection", Toast.LENGTH_SHORT).show();
                            }
                        });

            }
        });
    }

    public void addProfileToServer(String myImeiUniqueID) {
        FirebaseInstanceId.getInstance().getInstanceId().addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
            @Override
            public void onComplete(@NonNull Task<InstanceIdResult> task) {
                if (!task.isSuccessful()) {
                    Log.i("Token:  ", "onComplete: Task failed");
                    return;
                }
                FCM_TOKEN = task.getResult().getToken();
                Log.i("Token:  ", "onComplete: The result: " + task.getResult().getToken());

                JSONObject jsonObjectProfile = new JSONObject();

                try {
                    jsonObjectProfile.put("randomUniqueId", "");
                    jsonObjectProfile.put("imei", myImeiUniqueID);
                    jsonObjectProfile.put("tokenRegistrationFCM", FCM_TOKEN);
                    jsonObjectProfile.put("firstName", "");
                    jsonObjectProfile.put("lastName", "");
                    jsonObjectProfile.put("phoneNumber", "");
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                AndroidNetworking.post("http://192.168.0.13:3000/api/Profile")
                        .addJSONObjectBody(jsonObjectProfile) // posting json
                        .setTag("AddProfile")
                        .setPriority(Priority.HIGH)
                        .build()
                        .getAsJSONObject(new JSONObjectRequestListener() {
                            @Override
                            public void onResponse(JSONObject response) {
                                try {
                                    randomUniqueId = response.getString("randomUniqueId");
                                    tokenRegistrationFCM = response.getString("tokenRegistrationFCM");
                                    firstName = response.getString("firstName");
                                    lastName = response.getString("lastName");
                                    phoneNumber = response.getString("phoneNumber");

                                    SharedPreferences.Editor editor = sharedPrefs.edit();
                                    editor.putString(PREF_IMEI_UNIQUE_ID, imeiUniqueID);
                                    editor.putString("randomUniqueId", randomUniqueId);
                                    editor.putString("tokenRegistrationFCM", tokenRegistrationFCM);
                                    editor.putString("firstName", firstName);
                                    editor.putString("lastName", lastName);
                                    editor.putString("phoneNumber", phoneNumber);
                                    editor.commit();

                                    progressBar.hideProgressBar();
                                    progressBar.setVisibility(View.GONE);
                                    mTitleRandomUniqueId.setText(randomUniqueId);

                                    isGetDataFromServer = true;

                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }

                            @Override
                            public void onError(ANError error) {
                                error.printStackTrace();
                                Toast.makeText(getApplicationContext(), "please check your connection", Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        });

    }

    private boolean checkPermission() {
        if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_DENIED) {
            return false;
        }
        return true;
    }

    private void requestPermission() {
        if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA},
                    PERMISSION_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case PERMISSION_REQUEST_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Intent intent = new Intent(this, ImagePlayback.class);
                    startActivity(intent);
                    finish();
                } else {
                    requestPermission();
                }
                break;
        }
    }

}
