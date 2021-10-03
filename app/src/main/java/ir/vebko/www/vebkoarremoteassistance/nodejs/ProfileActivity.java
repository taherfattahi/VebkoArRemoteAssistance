package ir.vebko.www.vebkoarremoteassistance.nodejs;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;

import org.json.JSONException;
import org.json.JSONObject;

import ir.vebko.www.vebkoarremoteassistance.R;

public class ProfileActivity extends AppCompatActivity {

    private Toolbar toolbar_top_profile;
    private TextView toolbar_title_profile;
    private EditText edtFirstName, edtLastName, edtPhoneNumber;
    private Button btnUpdateProfile;
    private ImageView imgBackArrow;

    public String randomUniqueId = "";
    public String imeiUniqueID = "";
    public String firstName = "";
    public String lastName = "";
    public String phoneNumber = "ش";

    private SharedPreferences sharedPrefs;
    private static final String PREF_IMEI_UNIQUE_ID = "PREF_IMEI_UNIQUE_ID";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        sharedPrefs = this.getSharedPreferences(PREF_IMEI_UNIQUE_ID, Context.MODE_PRIVATE);

        toolbar_top_profile = (Toolbar) findViewById(R.id.toolbar_top_profile);
        toolbar_title_profile = (TextView) toolbar_top_profile.findViewById(R.id.toolbar_title_profile);
        imgBackArrow = findViewById(R.id.imgBackArrow);
        edtFirstName = findViewById(R.id.edtFirstName);
        edtLastName = findViewById(R.id.edtLastName);
        edtPhoneNumber = findViewById(R.id.edtPhoneNumber);
        btnUpdateProfile = findViewById(R.id.btnUpdateProfile);

        Intent intent = getIntent();
        randomUniqueId = intent.getStringExtra("randomUniqueId");
        imeiUniqueID = intent.getStringExtra("imeiUniqueID");
        firstName = intent.getStringExtra("firstName");
        lastName = intent.getStringExtra("lastName");
        phoneNumber = intent.getStringExtra("phoneNumber");

        if (!randomUniqueId.equals("null")){
            toolbar_title_profile.setText("ID: " + randomUniqueId);
        }
        if (!firstName.equals("null")){
            edtFirstName.setText(firstName);
        }
        if (!lastName.equals("null")){
            edtLastName.setText(lastName);
        }
        if (!phoneNumber.equals("null")){
            edtPhoneNumber.setText(phoneNumber);
        }

        btnUpdateProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btnUpdateProfile.setEnabled(false);
                JSONObject jsonObjectProfile = new JSONObject();

                try {
                    jsonObjectProfile.put("randomUniqueId", "");
                    jsonObjectProfile.put("imei", imeiUniqueID);
                    jsonObjectProfile.put("tokenRegistrationFCM", "");
                    jsonObjectProfile.put("firstName", edtFirstName.getText().toString());
                    jsonObjectProfile.put("lastName", edtLastName.getText().toString());
                    jsonObjectProfile.put("phoneNumber", edtPhoneNumber.getText().toString());
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                AndroidNetworking.patch("http://172.20.10.4:3000/api/Profile/updateprofile")
                        .addJSONObjectBody(jsonObjectProfile)
                        .setTag("updateprofile")
                        .setPriority(Priority.HIGH)
                        .build()
                        .getAsJSONObject(new JSONObjectRequestListener() {

                            @Override
                            public void onResponse(JSONObject jsonObject) {
                                try {
                                    firstName = jsonObject.getString("firstName");
                                    lastName = jsonObject.getString("lastName");
                                    phoneNumber = jsonObject.getString("phoneNumber");

                                    SharedPreferences.Editor editor = sharedPrefs.edit();
                                    editor.putString("firstName", firstName);
                                    editor.putString("lastName", lastName);
                                    editor.putString("phoneNumber", phoneNumber);
                                    editor.commit();

                                    toolbar_title_profile.setText("ID: " + randomUniqueId);
                                    edtFirstName.setText(firstName);
                                    edtLastName.setText(lastName);
                                    edtPhoneNumber.setText(phoneNumber);
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }

                                Toast.makeText(getApplicationContext(), "Profile Updated", Toast.LENGTH_SHORT).show();
                                btnUpdateProfile.setEnabled(true);
                            }

                            @Override
                            public void onError(ANError anError) {
                                anError.printStackTrace();
                                Toast.makeText(getApplicationContext(), "please check your connection", Toast.LENGTH_SHORT).show();
                                btnUpdateProfile.setEnabled(true);
                            }
                        });
            }
        });

        imgBackArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });


    }

}