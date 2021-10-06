package ir.vebko.www.vebkoarremoteassistance.nodejs;

import android.app.Activity;
import android.content.Context;
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

import ir.vebko.www.vebkoarremoteassistance.MyApplication;


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
    public static void callSingle(NodejsActivity activity, String wss, String roomId, boolean videoEnable,
                                  String randomUniqueId, String imei, String destinationTokenRegistrationFCM, String destinationCustomName, String myCustomName, String secondCustomName, String lastName, String phoneNumber) {
        if (TextUtils.isEmpty(wss)) {
            wss = WSS;
        }
        WebRTCManager.getInstance().init(wss, iceServers, new IConnectEvent() {
            @Override
            public void onSuccess() {

                JSONObject jsonObjectContact = new JSONObject();

                try {
                    jsonObjectContact.put("myRandomUniqueIdProfile", randomUniqueId);
                    jsonObjectContact.put("destinationRandomUniqueIdProfile", roomId);
                    jsonObjectContact.put("myCustomName", myCustomName);
                    jsonObjectContact.put("destinationCustomName", destinationCustomName);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                AndroidNetworking.post(MyApplication.WebApiURL + "/api/Contact")
                        .addJSONObjectBody(jsonObjectContact) // posting json
                        .setTag("AddContact")
                        .setPriority(Priority.HIGH)
                        .build()
                        .getAsJSONObject(new JSONObjectRequestListener() {
                            @Override
                            public void onResponse(JSONObject jsonObject) {

                                JSONObject jsonObjectProfile = new JSONObject();
                                JSONObject jsonObjectData = new JSONObject();

                                try {
                                    activity.getContact(jsonObject.getString("destinationCustomName"));

                                    jsonObjectProfile.put("to", destinationTokenRegistrationFCM);
                                    jsonObjectProfile.put("collapse_key", "news");

                                    if (secondCustomName != null) {
                                        if (!secondCustomName.equals("") && !secondCustomName.equals("null")){
                                            jsonObjectData.put("body", "From: " + secondCustomName);
                                        }else{
                                            jsonObjectData.put("body", "");
                                        }
                                    }else{
                                        jsonObjectData.put("body", "");
                                    }
                                    jsonObjectData.put("title", "Incoming Video Call");
                                    jsonObjectData.put("sender_random_unique_id", randomUniqueId);
//                                    jsonObjectData.put("key_2", "Value for key_2");

                                    jsonObjectProfile.put("data", jsonObjectData);
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }

                                AndroidNetworking.post("https://fcm.googleapis.com/fcm/send")
                                        .addJSONObjectBody(jsonObjectProfile) // posting json
                                        .addHeaders("Authorization", "key=AAAAdCVkpQ4:APA91bHxw6Et20DOdLLEl5vcx5byKS-AjzR6_7sam-zrsdVIbD21fdsfvMBDREA5AegY8uEYKnPP2uxkutSjDIUZfDripu7jQs4bsYyE0njip_mjD-OArWWp38h3H0aL_GKZYNmcF1v0")
                                        .setTag("AddProfile")
                                        .setPriority(Priority.HIGH)
                                        .build()
                                        .getAsJSONObject(new JSONObjectRequestListener() {
                                            @Override
                                            public void onResponse(JSONObject response) {
                                                ChatSingleActivity.openActivity(activity, videoEnable, roomId, randomUniqueId, imei, destinationTokenRegistrationFCM, myCustomName, lastName, phoneNumber);
                                            }
                                            @Override
                                            public void onError(ANError error) {
                                                error.printStackTrace();
                                                Toast.makeText(activity, "please check your connection", Toast.LENGTH_SHORT).show();
                                            }
                                        });
                            }

                            @Override
                            public void onError(ANError anError) {
                                anError.printStackTrace();
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

    public static void callSingle1(Context context, NodejsActivity nodejsActivity, String wss, String roomId, boolean videoEnable,
                                   String randomUniqueId, String imei, String tokenRegistrationFCM, String firstName, String lastName, String phoneNumber) {
        if (TextUtils.isEmpty(wss)) {
            wss = WSS;
        }
        WebRTCManager.getInstance().init(wss, iceServers, new IConnectEvent() {
            @Override
            public void onSuccess() {
                    ChatSingleReceiveActivity.openActivity(context, nodejsActivity, videoEnable, roomId, randomUniqueId);
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
