package ir.vebko.www.vebkoarremoteassistance.nodejs;

import android.Manifest;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
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
import com.dds.webrtclib.ui.ChatSingleActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.ar.core.ArCoreApk;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.HttpsURLConnection;

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

    public String randomUniqueId = "";
    public String imeiUniqueID = "";
    public String tokenRegistrationFCM = "";
    public String firstName = "";
    public String lastName = "";
    public String phoneNumber = "";


    private String signalIp = "ws://185.208.172.104:3000/ws";

    private Button btnArCore;
    private Button btnVuforia;
    private Button btnRetry, btnProfile;

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

        if (isConnected()) {
            btnRetry.setVisibility(View.GONE);
            progressBar.setVisibility(View.VISIBLE);
            btnProfile.setVisibility(View.VISIBLE);
            progressBar.showProgressBar();
            if (imeiUniqueID == null) {
                addProfileToServer(getUniqueID());
            } else {
                getProfileFromServer(imeiUniqueID);
//                getContactFromServer(randomUniqueId);
            }
        } else {
            btnRetry.setVisibility(View.VISIBLE);
            progressBar.setVisibility(View.GONE);
            btnProfile.setVisibility(View.GONE);
            progressBar.hideProgressBar();
            Toast.makeText(getApplicationContext(), "please check your connection", Toast.LENGTH_SHORT).show();
        }


    }

    private void initView() {

        toolbar = (Toolbar) findViewById(R.id.toolbar_top);
        mTitleRandomUniqueId = (TextView) toolbar.findViewById(R.id.toolbar_title);
        progressBar = findViewById(R.id.prgToolbar);
        btnArCore = findViewById(R.id.btnArCore);
        btnVuforia = findViewById(R.id.btnVuforia);
        btnRetry = findViewById(R.id.btnRetry);
        btnProfile = findViewById(R.id.btnProfile);
        edtRemoteID = findViewById(R.id.edtRemoteID);
        rvDestinationUniqueId = (RecyclerView) findViewById(R.id.rvDestinationUniqueId);

        randomUniqueId = sharedPrefs.getString("randomUniqueId", null);
        imeiUniqueID = sharedPrefs.getString(PREF_IMEI_UNIQUE_ID, null);
        tokenRegistrationFCM = sharedPrefs.getString("tokenRegistrationFCM", null);
        firstName = sharedPrefs.getString("firstName", null);
        lastName = sharedPrefs.getString("lastName", null);
        phoneNumber = sharedPrefs.getString("phoneNumber", null);

        adapter = new ContactsAdapter(contacts, this);
        rvDestinationUniqueId.setAdapter(adapter);
        rvDestinationUniqueId.setLayoutManager(new LinearLayoutManager(this));

//        rvDestinationUniqueId.addOnItemTouchListener(new RecyclerTouchListener(getApplicationContext(), rvDestinationUniqueId, new RecyclerTouchListener.ClickListener() {
//            @Override
//            public void onClick(View view, int position) {
////                speech(countries_list_code[position]);
//                edtRemoteID.setText(contacts.get(position).getName());
//            }
//
//            @Override
//            public void onLongClick(View view, int position) {
//
//            }
//        }));

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
                } else {
                    Intent intent = new Intent(getApplicationContext(), ImagePlayback.class);
                    startActivity(intent);
                }
            }
        });

        btnRetry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btnRetry.setEnabled(false);
                if (isConnected()) {
                    btnRetry.setVisibility(View.GONE);
                    btnProfile.setVisibility(View.VISIBLE);
                    progressBar.setVisibility(View.VISIBLE);
                    progressBar.showProgressBar();
                    if (imeiUniqueID == null) {
                        addProfileToServer(getUniqueID());
                    } else {
                        getProfileFromServer(imeiUniqueID);
//                        getContactFromServer(randomUniqueId);
                    }
                } else {
                    btnRetry.setVisibility(View.VISIBLE);
                    btnProfile.setVisibility(View.GONE);
                    progressBar.setVisibility(View.GONE);
                    progressBar.hideProgressBar();
                    Toast.makeText(getApplicationContext(), "please check your connection", Toast.LENGTH_SHORT).show();
                }
                btnRetry.setEnabled(true);
            }
        });

        btnProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btnProfile.setEnabled(false);
                if (isConnected()) {
                    randomUniqueId = sharedPrefs.getString("randomUniqueId", null);
                    imeiUniqueID = sharedPrefs.getString(PREF_IMEI_UNIQUE_ID, null);
                    tokenRegistrationFCM = sharedPrefs.getString("tokenRegistrationFCM", null);
                    firstName = sharedPrefs.getString("firstName", null);
                    lastName = sharedPrefs.getString("lastName", null);
                    phoneNumber = sharedPrefs.getString("phoneNumber", null);

                    Intent intent = new Intent(getApplicationContext(), ProfileActivity.class);
                    intent.putExtra("randomUniqueId", randomUniqueId);
                    intent.putExtra("imeiUniqueID", imeiUniqueID);
                    intent.putExtra("firstName", firstName);
                    intent.putExtra("lastName", lastName);
                    intent.putExtra("phoneNumber", phoneNumber);
                    startActivity(intent);
                } else {
                    Toast.makeText(getApplicationContext(), "please check your connection", Toast.LENGTH_SHORT).show();
                }
                btnProfile.setEnabled(true);
            }
        });


//        // Create the Handler object (on the main thread by default)
//        Handler handler = new Handler();
//        // Define the code block to be executed
//        Runnable runnableCode = new Runnable() {
//            @Override
//            public void run() {
//                // Do something here on the main thread
//                Log.d("Handlers", "Called on main thread");
//                // Repeat this the same runnable code block again another 2 seconds
//                // 'this' is referencing the Runnable object
//                handler.postDelayed(this, 10000);
//            }
//        };
//        // Start the initial runnable task by posting through the handler
//        handler.post(runnableCode);


    }

    public boolean isConnected() {
        String command = "ping -c 1 google.com";
        try {
            return Runtime.getRuntime().exec(command).waitFor() == 0;
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
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

    public void addDestinationUniueIdToTextView(int position) {
        edtRemoteID.setText(contacts.get(position).getName());
    }

    public void addContactCustomName(String customName, int position) {

        JSONObject jsonObjectProfile = new JSONObject();
        try {
            jsonObjectProfile.put("myRandomUniqueIdProfile", randomUniqueId);
            jsonObjectProfile.put("destinationRandomUniqueIdProfile", contacts.get(position).getName());
            jsonObjectProfile.put("myCustomName", customName);
            jsonObjectProfile.put("destinationCustomName", "");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        AndroidNetworking.patch("http://172.20.10.4:3000/api/Contact/updatecontactcustomname")
                .addJSONObjectBody(jsonObjectProfile) // posting json
                .setTag("UpdateContactCustomName")
                .setPriority(Priority.HIGH)
                .build()
                .getAsJSONObject(new JSONObjectRequestListener() {
                    @Override
                    public void onResponse(JSONObject jsonObject) {
                        try {
                            if (jsonObject.getString("myRandomUniqueIdProfile").equals(randomUniqueId)) {
                                contacts.get(position).setMyCustomName(jsonObject.getString("destinationCustomName"));
                            } else {
                                contacts.get(position).setMyCustomName(jsonObject.getString("myCustomName"));
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        adapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onError(ANError anError) {

                    }
                });
    }

    public void getContact(String destinationCustomName) {
        boolean isContains = true;
        for (int i = 0; i < contacts.size(); i++) {
            if (contacts.get(i).getName().equals(edtRemoteID.getText().toString().trim())) {
                isContains = false;
            }
        }
        if (isContains) {
            Contact contact = new Contact();
            contact.setName(edtRemoteID.getText().toString().trim());
            contact.setMyCustomName(destinationCustomName);
            contacts.add(contact);
            adapter.notifyDataSetChanged();
        }
    }

    public void JoinRoomSingleVideo() {
//        WebrtcUtil.callSingle(NodejsActivity.this,
//                signalIp,
//                "232343",
//                true, edtRemoteID.getText().toString().trim(), imeiUniqueID, "jsonObject.getString(\"tokenRegistrationFCM\")", firstName, lastName, phoneNumber);

        try {
            switch (ArCoreApk.getInstance().requestInstall(this, true)) {
                case INSTALL_REQUESTED:
                    installRequested = true;
                    Toast.makeText(getApplicationContext(), "please install ArCore dependency", Toast.LENGTH_SHORT).show();
                    return;
                case INSTALLED:
                    break;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        if (isGetDataFromServer) {
            if (edtRemoteID.getText().toString().trim().length() != 0 && !edtRemoteID.getText().toString().trim().equals(randomUniqueId)) {
                AndroidNetworking.post("http://172.20.10.4:3000/api/Profile/callingprofile")
                        .addQueryParameter("randomUniqueId", randomUniqueId)
                        .addQueryParameter("destinationRandomUniqueId", edtRemoteID.getText().toString().trim())
                        .addQueryParameter("calling", "true")
                        .setTag("CallingProfile")
                        .setPriority(Priority.HIGH)
                        .build()
                        .getAsJSONObject(new JSONObjectRequestListener() {
                            @Override
                            public void onResponse(JSONObject jsonObject) {
                                try {
                                    JSONObject jsonObject1;
                                    JSONArray jsonArray = jsonObject.getJSONArray("contactData");
                                    if (jsonArray.length() != 0) {
                                        for (int i = 0; i < jsonArray.length(); i++) {
                                            jsonObject1 = jsonArray.getJSONObject(i);
                                            if (jsonObject1.getString("destinationRandomUniqueIdProfile").equals(edtRemoteID.getText().toString().trim()) && jsonObject1.getString("myRandomUniqueIdProfile").equals(randomUniqueId)) {
                                                WebrtcUtil.callSingle(NodejsActivity.this, NodejsActivity.this, signalIp,
                                                        edtRemoteID.getText().toString().trim(),
                                                        true, randomUniqueId, imeiUniqueID, jsonObject.getString("tokenRegistrationFCM"),
                                                        jsonObject.getString("firstName"), firstName, jsonObject1.getString("myCustomName"), lastName, phoneNumber);
                                                break;
                                            } else if (jsonObject1.getString("destinationRandomUniqueIdProfile").equals(randomUniqueId) && jsonObject1.getString("myRandomUniqueIdProfile").equals(edtRemoteID.getText().toString().trim())) {
                                                WebrtcUtil.callSingle(NodejsActivity.this, NodejsActivity.this, signalIp,
                                                        edtRemoteID.getText().toString().trim(),
                                                        true, randomUniqueId, imeiUniqueID, jsonObject.getString("tokenRegistrationFCM"),
                                                        jsonObject.getString("firstName"), firstName, jsonObject1.getString("destinationCustomName"), lastName, phoneNumber);
                                                break;
                                            }
                                        }
                                    } else {
                                        WebrtcUtil.callSingle(NodejsActivity.this, NodejsActivity.this, signalIp,
                                                edtRemoteID.getText().toString().trim(),
                                                true, randomUniqueId, imeiUniqueID, jsonObject.getString("tokenRegistrationFCM"),
                                                jsonObject.getString("firstName"), firstName, firstName, lastName, phoneNumber);
                                    }
                                } catch (Exception ex) {
                                    try {
                                        WebrtcUtil.callSingle(NodejsActivity.this, NodejsActivity.this, signalIp,
                                                edtRemoteID.getText().toString().trim(),
                                                true, randomUniqueId, imeiUniqueID, jsonObject.getString("tokenRegistrationFCM"),
                                                jsonObject.getString("firstName"), firstName, firstName, lastName, phoneNumber);
                                    } catch (JSONException e) {
                                        Toast.makeText(getApplicationContext(), "please check your connection", Toast.LENGTH_SHORT).show();
                                    }
                                }

                            }

                            @Override
                            public void onError(ANError anError) {
                                anError.printStackTrace();
                                if (edtRemoteID.getText().toString().trim().length() == 0) {
                                    Toast.makeText(getApplicationContext(), "please add correct ID", Toast.LENGTH_SHORT).show();
                                } else if (anError.getErrorBody().contains("Is Calling!!!!!!")) {
                                    Toast.makeText(getApplicationContext(), "this user is calling", Toast.LENGTH_SHORT).show();
                                } else if (anError.getErrorBody().contains("Cant Find User!!!!!!")) {
                                    Toast.makeText(getApplicationContext(), "Cant Find User!!", Toast.LENGTH_SHORT).show();
                                }else {
                                    Toast.makeText(getApplicationContext(), "please check your connection", Toast.LENGTH_SHORT).show();
                                }

                            }
                        });
            } else {
                Toast.makeText(getApplicationContext(), "please insert remote ID", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(getApplicationContext(), "please check your connection", Toast.LENGTH_SHORT).show();
        }

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

                AndroidNetworking.post("http://172.20.10.4:3000/api/Profile/getprofileimei")
                        .addQueryParameter("imei", myImeiUniqueID)
                        .addQueryParameter("tokenRegistrationFCM", task.getResult().getToken())
                        .addQueryParameter("calling", "false")
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

                                    JSONArray jsonArray = response.getJSONArray("contactData");

                                    for (int i = 0; i < jsonArray.length(); i++) {
                                        try {
                                            JSONObject jsonObject = jsonArray.getJSONObject(i);

                                            if (jsonObject.getString("myRandomUniqueIdProfile").equals(randomUniqueId)) {
                                                Contact contact = new Contact();
                                                contact.setName(jsonObject.getString("destinationRandomUniqueIdProfile"));
                                                if (jsonObject.getString("destinationCustomName").equals("null")) {
                                                    AndroidNetworking.post("http://172.20.10.4:3000/api/Profile/getprofileuniqueid")
                                                            .addQueryParameter("randomUniqueId", jsonObject.getString("destinationRandomUniqueIdProfile"))
                                                            .setTag("getProfileUniqueId")
                                                            .setPriority(Priority.HIGH)
                                                            .build()
                                                            .getAsJSONObject(new JSONObjectRequestListener() {
                                                                @Override
                                                                public void onResponse(JSONObject jsonObject) {
                                                                    try {
                                                                        contact.setMyCustomName(jsonObject.getString("firstName"));
                                                                        contacts.add(contact);
                                                                        adapter.notifyDataSetChanged();
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
                                                    contact.setMyCustomName(jsonObject.getString("destinationCustomName"));
                                                    contacts.add(contact);
                                                    adapter.notifyDataSetChanged();
                                                }
                                            } else {
                                                Contact contact = new Contact();
                                                contact.setName(jsonObject.getString("myRandomUniqueIdProfile"));
                                                if (jsonObject.getString("myCustomName").equals("null")) {
                                                    AndroidNetworking.post("http://172.20.10.4:3000/api/Profile/getprofileuniqueid")
                                                            .addQueryParameter("randomUniqueId", jsonObject.getString("myRandomUniqueIdProfile"))
                                                            .setTag("getProfileUniqueId")
                                                            .setPriority(Priority.HIGH)
                                                            .build()
                                                            .getAsJSONObject(new JSONObjectRequestListener() {
                                                                @Override
                                                                public void onResponse(JSONObject jsonObject) {
                                                                    try {
                                                                        contact.setMyCustomName(jsonObject.getString("firstName"));
                                                                        contacts.add(contact);
                                                                        adapter.notifyDataSetChanged();
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
                                                    contact.setMyCustomName(jsonObject.getString("myCustomName"));
                                                    contacts.add(contact);
                                                    adapter.notifyDataSetChanged();
                                                }
                                            }

                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }
                                    }

//                        Collections.reverse(contacts);

                                    progressBar.hideProgressBar();
                                    progressBar.setVisibility(View.GONE);
                                    btnProfile.setVisibility(View.VISIBLE);
                                    mTitleRandomUniqueId.setText("ID: " + randomUniqueId);

                                    isGetDataFromServer = true;

                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }

                            @Override
                            public void onError(ANError anError) {
                                if (anError.getErrorBody().contains("please add new user")) {
                                    addProfileToServer(imeiUniqueID);
                                } else {
                                    anError.printStackTrace();
                                    Toast.makeText(getApplicationContext(), "please check your connection", Toast.LENGTH_SHORT).show();
                                }
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
                    jsonObjectProfile.put("randomUniqueId", randomUniqueId);
                    jsonObjectProfile.put("imei", myImeiUniqueID);
                    jsonObjectProfile.put("tokenRegistrationFCM", FCM_TOKEN);
                    jsonObjectProfile.put("firstName", firstName);
                    jsonObjectProfile.put("lastName", lastName);
                    jsonObjectProfile.put("phoneNumber", phoneNumber);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                AndroidNetworking.post("http://172.20.10.4:3000/api/Profile")
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

                                    JSONArray jsonArray = response.getJSONArray("contactData");

                                    for (int i = 0; i < jsonArray.length(); i++) {
                                        try {
                                            JSONObject jsonObject = jsonArray.getJSONObject(i);

                                            if (jsonObject.getString("myRandomUniqueIdProfile").equals(randomUniqueId)) {
                                                Contact contact = new Contact();
                                                contact.setName(jsonObject.getString("destinationRandomUniqueIdProfile"));
                                                if (jsonObject.getString("destinationCustomName").equals("null")) {
                                                    AndroidNetworking.post("http://172.20.10.4:3000/api/Profile/getprofileuniqueid")
                                                            .addQueryParameter("randomUniqueId", jsonObject.getString("destinationRandomUniqueIdProfile"))
                                                            .setTag("getProfileUniqueId")
                                                            .setPriority(Priority.HIGH)
                                                            .build()
                                                            .getAsJSONObject(new JSONObjectRequestListener() {
                                                                @Override
                                                                public void onResponse(JSONObject jsonObject) {
                                                                    try {
                                                                        contact.setMyCustomName(jsonObject.getString("firstName"));
                                                                        contacts.add(contact);
                                                                        adapter.notifyDataSetChanged();
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
                                                    contact.setMyCustomName(jsonObject.getString("destinationCustomName"));
                                                    contacts.add(contact);
                                                    adapter.notifyDataSetChanged();
                                                }
                                            } else {
                                                Contact contact = new Contact();
                                                contact.setName(jsonObject.getString("myRandomUniqueIdProfile"));
                                                if (jsonObject.getString("myCustomName").equals("null")) {
                                                    AndroidNetworking.post("http://172.20.10.4:3000/api/Profile/getprofileuniqueid")
                                                            .addQueryParameter("randomUniqueId", jsonObject.getString("myRandomUniqueIdProfile"))
                                                            .setTag("getProfileUniqueId")
                                                            .setPriority(Priority.HIGH)
                                                            .build()
                                                            .getAsJSONObject(new JSONObjectRequestListener() {
                                                                @Override
                                                                public void onResponse(JSONObject jsonObject) {
                                                                    try {
                                                                        contact.setMyCustomName(jsonObject.getString("firstName"));
                                                                        contacts.add(contact);
                                                                        adapter.notifyDataSetChanged();
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
                                                    contact.setMyCustomName(jsonObject.getString("myCustomName"));
                                                    contacts.add(contact);
                                                    adapter.notifyDataSetChanged();
                                                }
                                            }

                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }
                                    }

                                    progressBar.hideProgressBar();
                                    progressBar.setVisibility(View.GONE);
                                    btnProfile.setVisibility(View.VISIBLE);
                                    mTitleRandomUniqueId.setText("ID: " + randomUniqueId);

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
