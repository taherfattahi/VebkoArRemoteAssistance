package ir.vebko.www.vebkoarremoteassistance.nodejs;

import android.app.Activity;
import android.text.TextUtils;
import android.widget.Toast;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;
import com.dds.webrtclib.WebRTCManager;
import com.dds.webrtclib.bean.MediaType;
import com.dds.webrtclib.bean.MyIceServer;
import com.dds.webrtclib.ui.ChatRoomActivity;
import com.dds.webrtclib.ui.ChatSingleActivity;
import com.dds.webrtclib.ui.ChatSingleReceiveActivity;
import com.dds.webrtclib.ws.IConnectEvent;

import org.json.JSONException;
import org.json.JSONObject;


public class WebrtcUtil {


    public static final String HOST = "136.243.172.245";

    // turn and stun
    private static MyIceServer[] iceServers = {
            new MyIceServer("stun:stun.l.google.com:19302"),

            new MyIceServer("stun:" + HOST + ":3478?transport=udp"),
            new MyIceServer("turn:" + HOST + ":3478?transport=udp",
                    "test",
                    "test123"),
            new MyIceServer("turn:" + HOST + ":3478?transport=tcp",
                    "test",
                    "test123"),
    };

    // signalling
    private static String WSS = "wss://" + HOST + "/wss";

    // private static String WSS = "ws://192.168.1.138:3000";

    // one to one
    public static void callSingle(Activity activity, String wss, String roomId, boolean videoEnable,
                                  String randomUniqueId, String imei, String tokenRegistrationFCM, String firstName, String lastName, String phoneNumber) {
        if (TextUtils.isEmpty(wss)) {
            wss = WSS;
        }
        WebRTCManager.getInstance().init(wss, iceServers, new IConnectEvent() {
            @Override
            public void onSuccess() {

                JSONObject jsonObjectProfile = new JSONObject();
                JSONObject jsonObjectData = new JSONObject();

                try {
                    jsonObjectProfile.put("to", tokenRegistrationFCM);
                    jsonObjectProfile.put("collapse_key", "news");

                    jsonObjectData.put("body", "Body of Your Notification in Data");
                    jsonObjectData.put("title", "Incoming Video Call");
                    jsonObjectData.put("key_1", "Value for key_1");
                    jsonObjectData.put("key_2", "Value for key_2");

                    jsonObjectProfile.put("data", jsonObjectData);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                AndroidNetworking.post("https://fcm.googleapis.com/fcm/send")
                        .addJSONObjectBody(jsonObjectProfile) // posting json
                        .addHeaders("Authorization", "key=AAAA68JG54A:APA91bGKgYRKIJ9C56ZgQAreD5rzxJ3Tayn1YhXLr3H_pSmcXBGgpJUb2fahiCzGWMTmy-NjBke8E9vZmemfGrkuVxYxt2-3VWOYRPUNNwn-7N8lscETZikuxjtI0UA8aryeD2dHv4BH")
                        .setTag("AddProfile")
                        .setPriority(Priority.HIGH)
                        .build()
                        .getAsJSONObject(new JSONObjectRequestListener() {
                            @Override
                            public void onResponse(JSONObject response) {

                                ChatSingleActivity.openActivity(activity, videoEnable, roomId, imei, tokenRegistrationFCM, firstName, lastName, phoneNumber);

//                                try {
//                                    randomUniqueId = response.getString("randomUniqueId");
//                                    tokenRegistrationFCM = response.getString("tokenRegistrationFCM");
//                                    firstName = response.getString("firstName");
//                                    lastName = response.getString("lastName");
//                                    phoneNumber = response.getString("phoneNumber");

//                                    SharedPreferences.Editor editor = sharedPrefs.edit();
//                                    editor.putString(PREF_IMEI_UNIQUE_ID, imeiUniqueID);
//                                    editor.putString("randomUniqueId", randomUniqueId);
//                                    editor.putString("tokenRegistrationFCM", tokenRegistrationFCM);
//                                    editor.putString("firstName", firstName);
//                                    editor.putString("lastName", lastName);
//                                    editor.putString("phoneNumber", phoneNumber);
//                                    editor.commit();
//
//                                    Thread myThread = new Thread() {
//                                        @Override
//                                        public void run() {
//                                            try {
////                    if (!checkPermission()) {
////                        requestPermission();
////                    }else {
//                                                sleep(1500);
//                                                Intent intent = new Intent(getApplicationContext(), NodejsActivity.class);
//                                                startActivity(intent);
//                                                finish();
////                    }
//                                            } catch (InterruptedException e) {
//                                                e.printStackTrace();
//                                            }
//                                        }
//                                    };
//                                    myThread.start();
//
//                                } catch (JSONException e) {
//                                    e.printStackTrace();
//                                }
                            }

                            @Override
                            public void onError(ANError error) {
                                error.printStackTrace();
                                Toast.makeText(activity, "please check your connection", Toast.LENGTH_SHORT).show();
                            }
                        });


            }

            @Override
            public void onFailed(String msg) {

            }
        });

        //todo randomUniqueId
//        roomId = randomUniqueId;
        WebRTCManager.getInstance().connect(videoEnable ? MediaType.TYPE_VIDEO : MediaType.TYPE_AUDIO, roomId);
    }

    public static void callSingle1(Activity activity, String wss, String roomId, boolean videoEnable,
                                   String randomUniqueId, String imei, String tokenRegistrationFCM, String firstName, String lastName, String phoneNumber) {
        if (TextUtils.isEmpty(wss)) {
            wss = WSS;
        }
        WebRTCManager.getInstance().init(wss, iceServers, new IConnectEvent() {
            @Override
            public void onSuccess() {
                ChatSingleReceiveActivity.openActivity(activity, videoEnable);
            }

            @Override
            public void onFailed(String msg) {

            }
        });

        //todo randomUniqueId
//        roomId = randomUniqueId;
        WebRTCManager.getInstance().connect(videoEnable ? MediaType.TYPE_VIDEO : MediaType.TYPE_AUDIO, roomId);
    }


    // Videoconferencing
    public static void call(Activity activity, String wss, String roomId) {
        if (TextUtils.isEmpty(wss)) {
            wss = WSS;
        }
        WebRTCManager.getInstance().init(wss, iceServers, new IConnectEvent() {
            @Override
            public void onSuccess() {
                ChatRoomActivity.openActivity(activity);
            }

            @Override
            public void onFailed(String msg) {

            }
        });
        WebRTCManager.getInstance().connect(MediaType.TYPE_MEETING, roomId);
    }


}
