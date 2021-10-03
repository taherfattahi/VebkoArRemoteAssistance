package com.dds.webrtclib.ui;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;
import com.dds.webrtclib.IViewCallback;
import com.dds.webrtclib.PeerConnectionHelper;
import com.dds.webrtclib.ProxyVideoSink;
import com.dds.webrtclib.R;
import com.dds.webrtclib.WebRTCManager;
import com.dds.webrtclib.ar.Stroke;
import com.dds.webrtclib.bean.MediaType;
import com.dds.webrtclib.bean.MyIceServer;
import com.dds.webrtclib.utils.PermissionUtil;
import com.dds.webrtclib.ws.IConnectEvent;
import com.google.ar.core.Pose;
import com.google.ar.core.TrackingState;
import com.google.ar.sceneform.AnchorNode;
import com.google.ar.sceneform.Camera;
import com.google.ar.sceneform.collision.Ray;
import com.google.ar.sceneform.math.Vector3;

import org.json.JSONException;
import org.json.JSONObject;
import org.webrtc.EglBase;
import org.webrtc.MediaStream;
import org.webrtc.RendererCommon;
import org.webrtc.SurfaceViewRenderer;

import java.io.IOException;
import java.net.URISyntaxException;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

public class ChatSingleReceiveActivity extends AppCompatActivity {

    //    private SurfaceViewRenderer local_view;
    private SurfaceViewRenderer remote_view;
    //    private ProxyVideoSink localRender;
    private ProxyVideoSink remoteRender;

    private WebRTCManager manager;

    private boolean videoEnable;
    private boolean isSwappedFeeds;

    private EglBase rootEglBase;

    private NotificationManager notificationManager;

    public String myRandomUniqueId;

//    private boolean isAlive = true;
//    private boolean isAliveTimerFlag = false;
//    private boolean reCallFlag = false;
    private static boolean active = false;

    private static Socket socketIO = null;
//    private Handler handlerHeartBeatChecker;
//    private Runnable runnableCodeHeartBeatChecker;

//    private Socket mSocket;
//    {
//        try {
////            mSocket = IO.socket("http://185.208.172.104:3001");
//            mSocket = IO.socket("http://192.168.0.13:3001");
//        } catch (URISyntaxException e) {
//            e.printStackTrace();
//        }
//    }

    public static final String HOST = "136.243.172.245";

    // signalling
    private String signalIp = "ws://185.208.172.104:3000/ws";

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

    public static void openActivity(Context activity, boolean videoEnable, String roomId) {
        Intent intent = new Intent(activity, ChatSingleReceiveActivity.class);
        intent.putExtra("videoEnable", videoEnable);
        intent.putExtra("myRandomUniqueId", roomId);
//        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        activity.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
                | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.wr_activity_chat_single_receive);

        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(1);

        initVar();

//        local_view.setVisibility(View.GONE);
    }

    public void setDistanceFromSickbarReceiver(float value){
        try{
            socketIO.emit("onDistanceFromSickbarReceiver", myRandomUniqueId + "-" + value);
        }catch (Exception ex){
        }
    }

    public void setStrokFromSickbarReceiver(float value){
        try{
            socketIO.emit("onStrokFromSickbarReceiver", myRandomUniqueId + "-" + value);
        }catch (Exception ex){
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        active = true;
    }

    @Override
    public void onStop() {
        super.onStop();
        active = false;
    }


    int zz = 0;
//    CountDownTimer aaa;

    private void initVar() {
        Intent intent = getIntent();
        videoEnable = intent.getBooleanExtra("videoEnable", false);
        myRandomUniqueId = intent.getStringExtra("myRandomUniqueId");

        ChatSingleReceiveFragment chatSingleReceiveFragment = new ChatSingleReceiveFragment();
        replaceFragment(chatSingleReceiveFragment, videoEnable);
        rootEglBase = EglBase.create();
        if (videoEnable) {
//            local_view = findViewById(R.id.local_view_render);
            remote_view = findViewById(R.id.remote_view_render);

            // 本地图像初始化
//            local_view.init(rootEglBase.getEglBaseContext(), null);
//            local_view.setScalingType(RendererCommon.ScalingType.SCALE_ASPECT_FIT);
//            local_view.setZOrderMediaOverlay(true);
//            local_view.setMirror(true);
//            localRender = new ProxyVideoSink();
            //远端图像初始化
            remote_view.init(rootEglBase.getEglBaseContext(), null);
            remote_view.setScalingType(RendererCommon.ScalingType.SCALE_ASPECT_BALANCED);
//            remote_view.setEnableHardwareScaler(true);
//            remote_view.setMirror(true);
            remoteRender = new ProxyVideoSink();
            setSwappedFeeds(true);

//            local_view.setOnClickListener(v -> setSwappedFeeds(!isSwappedFeeds));
        }

        try {
            socketIO = IO.socket("http://172.20.10.4:3001");
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }

        socketIO.on(Socket.EVENT_CONNECT, onConnect);
//        socketIO.on("heartBeatPusher", onHeartBeatMessage);
//        socketIO.on("reCallPusher", onReCallMessage);

        socketIO.connect();

        remote_view.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {

                DisplayMetrics metrics = new DisplayMetrics();
                getWindowManager().getDefaultDisplay().getMetrics(metrics);

                int height = metrics.heightPixels;
                int width = metrics.widthPixels;

                int x = (int) event.getX();
                int y = (int) event.getY();

//                Log.d("test123", "onTouchEvent: " + x + "  :  " + y);

                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        Log.i("TAG", "touched down");

                        int xMove1 = (int) event.getX();
                        int yMove1 = (int) event.getY();

//
                        socketIO.emit("positionPlayer", myRandomUniqueId + "-" + xMove1 + "-" + yMove1 + "-" + "down" + "-" + width + "-" + height);
                        break;
                    case MotionEvent.ACTION_MOVE:
                        Log.i("TAG", "moving: (" + x + ", " + y + ")");

                        int xMove = (int) event.getX();
                        int yMove = (int) event.getY();

                        if (zz % 2 == 0) {
                            socketIO.emit("positionPlayer", myRandomUniqueId + "-" + xMove + "-" + yMove + "-" + "move" + "-" + width + "-" + height);
                        }
                        zz++;
                        break;
                    case MotionEvent.ACTION_UP:
                        zz = 0;
                        Log.i("TAG", "touched up");
                        break;
                }

                return true;
            }
        });

        startCall();

//        aaa = new CountDownTimer(4000, 1000) {
//            public void onTick(long millisUntilFinished) {
////                                        mTextField.setText("seconds remaining: " + millisUntilFinished / 1000);
//            }
//
//            public void onFinish() {
////                                        mTextField.setText("done!");
//                isAliveTimer();
//            }
//        };
//
//        handlerHeartBeatChecker = new Handler();
//        // Define the code block to be executed
//        runnableCodeHeartBeatChecker = new Runnable() {
//            @Override
//            public void run() {
//                // Do something here on the main thread
//                Log.d("Handlers", "Called on main thread: Receiving  " + socketIO.isActive());
//
//                if (active) {
////                    if (!isAlive) {
////                        if (!isAliveTimerFlag) {
////                            isAliveTimerFlag = true;
////                            isAliveTimer();
////                        }
////                    }
//                    if (isConnected()) {
//                        if (isAlive) {
//
//                            if (reCallFlag) {
//
////                                hangUp();
//                                isAliveTimer();
//
////                                hangUp();
//
////                                WebRTCManager.getInstance().init(signalIp, iceServers, new IConnectEvent() {
////                                    @Override
////                                    public void onSuccess() {
//////                                    ChatSingleReceiveActivity.openActivity(ChatSingleReceiveActivity.this, videoEnable, myRandomUniqueId);
////                                        finish();
////                                        overridePendingTransition(0, 0);
////                                        startActivity(getIntent());
////                                        overridePendingTransition(0, 0);
////                                    }
////
////                                    @Override
////                                    public void onFailed(String msg) {
////
////                                    }
////                                });
////
////                                //todo randomUniqueId
////                                WebRTCManager.getInstance().connect(videoEnable ? MediaType.TYPE_VIDEO : MediaType.TYPE_AUDIO, myRandomUniqueId);
//                            }
//
//                            if (isAliveTimerFlag) {
//                                isAliveTimerFlag = false;
//                                //recalling
//
////                                hangUp();
//
////                                WebRTCManager.getInstance().init(signalIp, iceServers, new IConnectEvent() {
////                                    @Override
////                                    public void onSuccess() {
////                                        ChatSingleReceiveActivity.openActivity(ChatSingleReceiveActivity.this, videoEnable, myRandomUniqueId);
////                                    }
////
////                                    @Override
////                                    public void onFailed(String msg) {
////
////                                    }
////                                });
////
////                                //todo randomUniqueId
////                                WebRTCManager.getInstance().connect(videoEnable ? MediaType.TYPE_VIDEO : MediaType.TYPE_AUDIO, myRandomUniqueId);
//                            }
//                        }
//                    } else {
//                        isAlive = false;
//                        if (!isAliveTimerFlag) {
////                        isDestroyConnection = true;
//                            isAliveTimerFlag = true;
//                            isAliveTimer();
//                        }
//                    }
//                }
//
//                // Repeat this the same runnable code block again another 2 seconds
//                // 'this' is referencing the Runnable object
//                handlerHeartBeatChecker.postDelayed(this, 2000);
//            }
//        };
//        // Start the initial runnable task by posting through the handler
//        handlerHeartBeatChecker.post(runnableCodeHeartBeatChecker);

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

    //todo
//    public void isAliveTimer() {
//
////        WebRTCManager.getInstance().init(signalIp, iceServers, new IConnectEvent() {
////            @Override
////            public void onSuccess() {
//////                ChatSingleReceiveActivity.openActivity(ChatSingleReceiveActivity.this, videoEnable, myRandomUniqueId);
////                handlerHeartBeatChecker.removeCallbacksAndMessages(runnableCodeHeartBeatChecker);
////                recreate();
////            }
////
////            @Override
////            public void onFailed(String msg) {
////
////            }
////        });
////
////        //todo randomUniqueId
////        WebRTCManager.getInstance().connect(videoEnable ? MediaType.TYPE_VIDEO : MediaType.TYPE_AUDIO, "destinationRandomUniqueId");
//
//        Intent intent = new Intent(ChatSingleReceiveActivity.this, ReconnectActivity1.class);
//        intent.putExtra("videoEnable", videoEnable);
//        intent.putExtra("destinationRandomUniqueId", myRandomUniqueId);
//        intent.putExtra("myRandomUniqueId", myRandomUniqueId);
//        intent.putExtra("imei", "");
//        intent.putExtra("destinationTokenRegistrationFCM", "");
//        intent.putExtra("firstName", "");
//        intent.putExtra("lastName", "");
//        intent.putExtra("phoneNumber", "");
//        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
//        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
//        //        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//        startActivity(intent);
//        this.finish();
//
////        final int interval = 20000; // 1 Second
////        Handler handler = new Handler();
////        Runnable runnable = new Runnable() {
////            public void run() {
////                if (isAliveTimerFlag) {
////                    //end
////                    isAliveTimerFlag = false;
////                    hangUp();
////                }
////            }
////        };
////        handler.postAtTime(runnable, System.currentTimeMillis() + interval);
////        handler.postDelayed(runnable, interval);
//    }


    private Emitter.Listener onConnect = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            try {
                Log.d("TAG", "SocketIO Connected");
//                JSONObject jsonObj = new JSONObject();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };


    //todo
//    private Emitter.Listener onHeartBeatMessage = new Emitter.Listener() {
//        @Override
//        public void call(final Object... args) {
//            runOnUiThread(new Runnable() {
//                @Override
//                public void run() {
//                    String a = (String) args[0];
//                    String[] b = a.split("-");
//
//
//                    if (b[0].equals(myRandomUniqueId)) {
//                        if (b[2].equals("alive")) {
//                            if (socketIO.isActive()) {
//                                socketIO.emit("heartBeat", b[1] + "-" + myRandomUniqueId + "-" + "alive");
//                                isAlive = true;
//
////                                new android.os.Handler(Looper.getMainLooper()).postDelayed(
////                                        new Runnable() {
////                                            public void run() {
//////                                                Log.i("tag", "This'll run 300 milliseconds later");
////                                            }
////                                        },
////                                        2000);
//                            aaa.cancel();
//                            aaa.start();
//
//                            }
//                        }
//                    }
//
//                }
//            });
//        }
//    };

//    private Emitter.Listener onReCallMessage = new Emitter.Listener() {
//        @Override
//        public void call(final Object... args) {
//            runOnUiThread(new Runnable() {
//                @Override
//                public void run() {
//                    String a = (String) args[0];
//                    String[] b = a.split("-");
//
//                    if (b[0].equals(myRandomUniqueId)) {
//                        reCallFlag = true;
//                    }
//
//                }
//            });
//        }
//    };


    private void setSwappedFeeds(boolean isSwappedFeeds) {
        this.isSwappedFeeds = isSwappedFeeds;
//        localRender.setTarget(isSwappedFeeds ? remote_view : local_view);
//        remoteRender.setTarget(isSwappedFeeds ? local_view : remote_view);
        remoteRender.setTarget(remote_view);
    }

    private void startCall() {
        manager = WebRTCManager.getInstance();
        manager.setCallback(new IViewCallback() {
            @Override
            public void onSetLocalStream(MediaStream stream, String socketId) {
//                if (stream.videoTracks.size() > 0) {
//                    stream.videoTracks.get(0).addSink(localRender);
//                }

                if (videoEnable) {
                    stream.videoTracks.get(0).setEnabled(true);
                }

                AndroidNetworking.post("http://172.20.10.4:3000/api/Profile/callingprofile")
                        .addQueryParameter("randomUniqueId", myRandomUniqueId)
                        .addQueryParameter("destinationRandomUniqueId", "")
                        .addQueryParameter("calling", "true")
                        .setTag("CallingProfile")
                        .setPriority(Priority.HIGH)
                        .build()
                        .getAsJSONObject(new JSONObjectRequestListener() {
                            @Override
                            public void onResponse(JSONObject jsonObject) {
//                                try {
////                                    WebrtcUtil.callSingle(NodejsActivity.this, NodejsActivity.this, signalIp,
////                                            edtRemoteID.getText().toString().trim(),
////                                            true, randomUniqueId, imeiUniqueID, jsonObject.getString("tokenRegistrationFCM"),
////                                            jsonObject.getString("firstName"), firstName, lastName, phoneNumber);
//                                } catch (JSONException e) {
//                                    e.printStackTrace();
//                                }
                            }

                            @Override
                            public void onError(ANError anError) {
                                anError.printStackTrace();
//                                if (edtRemoteID.getText().toString().trim().length() == 0){
//                                    Toast.makeText(getApplicationContext(), "please add correct ID", Toast.LENGTH_SHORT).show();
//                                }else
                                if (anError.getErrorBody().contains("Is Calling!!!!!!")) {
                                    Toast.makeText(getApplicationContext(), "this user is calling", Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(getApplicationContext(), "please check your connection", Toast.LENGTH_SHORT).show();
                                }


                            }
                        });
            }

            @Override
            public void onAddRemoteStream(MediaStream stream, String socketId) {
                if (stream.videoTracks.size() > 0) {
                    stream.videoTracks.get(0).addSink(remoteRender);
                }
                if (videoEnable) {
                    stream.videoTracks.get(0).setEnabled(true);

                    runOnUiThread(() -> setSwappedFeeds(false));
                }
            }

            @Override
            public void onCloseWithId(String socketId) {

                runOnUiThread(() -> {
                    disConnect();
                    ChatSingleReceiveActivity.this.finish();
                });

            }
        });
        if (!PermissionUtil.isNeedRequestPermission(ChatSingleReceiveActivity.this)) {
            manager.joinRoom(getApplicationContext(), rootEglBase);
        }
    }

    private void replaceFragment(Fragment fragment, boolean videoEnable) {
        Bundle bundle = new Bundle();
        bundle.putBoolean("videoEnable", videoEnable);
        fragment.setArguments(bundle);
        FragmentManager manager = getSupportFragmentManager();
        manager.beginTransaction()
                .replace(R.id.wr_container, fragment)
                .commit();

    }


    //    @Override
//    public boolean onKeyDown(int keyCode, KeyEvent event) {
//        return keyCode == KeyEvent.KEYCODE_BACK || super.onKeyDown(keyCode, event);
//    }

    @Override
    public void onBackPressed() {
        //        super.onBackPressed();
        hangUp();
    }


    public void switchCamera() {
        manager.switchCamera();
    }

    public void clearDraw() {
        socketIO.emit("clearDrawFunc", myRandomUniqueId + "-" + "clearDraw");
    }

    public void changeColorDraw(int colorHex) {
        socketIO.emit("changeColorDrawFunc", myRandomUniqueId + "-" + colorHex);
    }

    public void hangUp() {
        disConnect();
        finish();
    }

    public void toggleMic(boolean enable) {
        manager.toggleMute(enable);
    }

    public void toggleSpeaker(boolean enable) {
        manager.toggleSpeaker(enable);

    }

    public void toggleBlueToothOn(boolean enable) {
        if (enable) {
            if (manager._peerHelper.mAudioManager.isBluetoothA2dpOn()) {
                manager._peerHelper.mAudioManager.startBluetoothSco();
                manager._peerHelper.mAudioManager.setMode(AudioManager.MODE_IN_COMMUNICATION);
            }
        } else {
//            if (manager._peerHelper.mAudioManager.isBluetoothScoOn()){
            manager._peerHelper.mAudioManager.stopBluetoothSco();
            manager.toggleSpeaker(true);
            //                manager._peerHelper.mAudioManager.setMode(AudioManager.MODE_IN_COMMUNICATION);
//            }
        }
    }

    @Override
    protected void onDestroy() {
        AndroidNetworking.post("http://172.20.10.4:3000/api/Profile/profileendcall")
                .addQueryParameter("randomUniqueId", myRandomUniqueId)
                .addQueryParameter("calling", "false")
                .setTag("ProfileEndCall")
                .setPriority(Priority.HIGH)
                .build()
                .getAsJSONObject(new JSONObjectRequestListener() {
                    @Override
                    public void onResponse(JSONObject jsonObject) {
                    }

                    @Override
                    public void onError(ANError anError) {
                        Toast.makeText(getApplicationContext(), "please check your connection", Toast.LENGTH_SHORT).show();
                    }
                });
        disConnect();
        super.onDestroy();

    }

    private void disConnect() {

//        handlerHeartBeatChecker.removeCallbacksAndMessages(runnableCodeHeartBeatChecker);

        manager.exitRoom();
//        if (localRender != null) {
//            localRender.setTarget(null);
//            localRender = null;
//        }
        if (remoteRender != null) {
            remoteRender.setTarget(null);
            remoteRender = null;
        }

//        if (local_view != null) {
//            local_view.release();
//            local_view = null;
//        }
        if (remote_view != null) {
            remote_view.release();
            remote_view = null;
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        for (int i = 0; i < permissions.length; i++) {
            Log.i(PeerConnectionHelper.TAG, "[Permission] " + permissions[i] + " is " + (grantResults[i] == PackageManager.PERMISSION_GRANTED ? "granted" : "denied"));
            if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                finish();
                break;
            }
        }
        manager.joinRoom(getApplicationContext(), rootEglBase);

    }
}
