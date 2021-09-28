package com.dds.webrtclib.ui;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.AudioManager;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.PixelCopy;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.dds.webrtclib.IViewCallback;
import com.dds.webrtclib.PeerConnectionHelper;
import com.dds.webrtclib.ProxyVideoSink;
import com.dds.webrtclib.R;
import com.dds.webrtclib.WebRTCManager;
import com.dds.webrtclib.ar.ArSceneView;
import com.dds.webrtclib.ar.Stroke;
import com.dds.webrtclib.bean.MediaType;
import com.dds.webrtclib.bean.MyIceServer;
import com.dds.webrtclib.utils.PermissionUtil;
import com.dds.webrtclib.ws.IConnectEvent;
import com.google.ar.core.ArCoreApk;
import com.google.ar.core.Config;
import com.google.ar.core.Frame;
import com.google.ar.core.Pose;
import com.google.ar.core.Session;
import com.google.ar.core.TrackingState;
import com.google.ar.core.exceptions.CameraNotAvailableException;
import com.google.ar.core.exceptions.UnavailableApkTooOldException;
import com.google.ar.core.exceptions.UnavailableArcoreNotInstalledException;
import com.google.ar.core.exceptions.UnavailableSdkTooOldException;
import com.google.ar.core.exceptions.UnavailableUserDeclinedInstallationException;
import com.google.ar.sceneform.AnchorNode;
import com.google.ar.sceneform.Camera;
import com.google.ar.sceneform.FrameTime;
import com.google.ar.sceneform.HitTestResult;
import com.google.ar.sceneform.Scene;
import com.google.ar.sceneform.collision.Ray;
import com.google.ar.sceneform.math.Vector3;
import com.google.ar.sceneform.rendering.Material;
import com.google.ar.sceneform.rendering.MaterialFactory;
//import com.google.ar.sceneform.ArSceneView;

import org.json.JSONException;
import org.json.JSONObject;
import org.webrtc.EglBase;
import org.webrtc.MediaStream;
import org.webrtc.NV21Buffer;
import org.webrtc.RendererCommon;
import org.webrtc.SurfaceViewRenderer;
import org.webrtc.VideoFrame;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.concurrent.CompletionException;
import java.util.concurrent.TimeUnit;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;


public class ChatSingleActivity extends AppCompatActivity implements SurfaceHolder.Callback, Scene.OnPeekTouchListener {
    //    private SurfaceViewRenderer local_view;
    private ArSceneView remote_view;
    //    private ProxyVideoSink localRender;
    private ProxyVideoSink remoteRender;

    private WebRTCManager manager;

    private boolean videoEnable;
    private boolean isSwappedFeeds;

    private EglBase rootEglBase;

    private Session session;
//    private final SnackbarHelper messageSnackbarHelper = new SnackbarHelper();

    private boolean shouldConfigureSession = false;
//    private Map<String, AugmentedImageNode> nodes = new HashMap<String, AugmentedImageNode>();

    private static final float DRAW_DISTANCE = 0.28f;
    private Material material;
    private AnchorNode anchorNode;
    private final ArrayList<Stroke> strokes = new ArrayList<>();
    private Stroke currentStroke;

    private static Socket socketIO = null;

    private int widthMain;
    private int heightMain;

    private ChatSingleFragment chatSingleFragment;

    private boolean installRequested;

    public String randomUniqueId;
    public String imeiUniqueID;
    public String firstName;
    public String lastName;
    public String phoneNumber;

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
    private String signalIp = "ws://185.208.172.104:3000/ws";

    public static void openActivity(Activity activity, boolean videoEnable, String randomUniqueId, String imei, String destinationTokenRegistrationFCM, String firstName, String lastName, String phoneNumber) {
        Intent intent = new Intent(activity, ChatSingleActivity.class);
        intent.putExtra("videoEnable", videoEnable);
        intent.putExtra("randomUniqueId", randomUniqueId);
        intent.putExtra("imei", imei);
        intent.putExtra("destinationTokenRegistrationFCM", destinationTokenRegistrationFCM);
        intent.putExtra("firstName", firstName);
        intent.putExtra("lastName", lastName);
        intent.putExtra("phoneNumber", phoneNumber);
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
        setContentView(R.layout.wr_activity_chat_single);
        initVar();
        initListener();
    }

    boolean isTestFlag;

    private void initVar() {
        Intent intent = getIntent();
        videoEnable = intent.getBooleanExtra("videoEnable", false);
        randomUniqueId = intent.getStringExtra("randomUniqueId");
        imeiUniqueID = intent.getStringExtra("imeiUniqueID");
        firstName = intent.getStringExtra("firstName");
        lastName = intent.getStringExtra("lastName");
        phoneNumber = intent.getStringExtra("phoneNumber");

        chatSingleFragment = new ChatSingleFragment();
        replaceFragment(chatSingleFragment, videoEnable);
        rootEglBase = EglBase.create();

        if (videoEnable) {
//            local_view = findViewById(R.id.local_view_render);
            remote_view = findViewById(R.id.remote_view_render);

//            local_view.init(rootEglBase.getEglBaseContext(), null);
//            local_view.setScalingType(RendererCommon.ScalingType.SCALE_ASPECT_FIT);
//            local_view.setZOrderMediaOverlay(true);
//            local_view.setMirror(true);
//            localRender = new ProxyVideoSink();

            remote_view.init(rootEglBase.getEglBaseContext(), null);
//            remote_view.setScalingType(RendererCommon.ScalingType.SCALE_ASPECT_BALANCED);
//            remote_view.setMirror(true);
            remoteRender = new ProxyVideoSink();
            setSwappedFeeds(true);

//            local_view.setOnClickListener(v -> setSwappedFeeds(!isSwappedFeeds));
        }

        startCall();

        //        arSceneView = findViewById(R.id.surfaceView);
//        arSceneView.getPlaneRenderer().setVisible(false);
//        installRequested = false;

        try {
            socketIO = IO.socket("http://185.208.172.104:3001");
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }

        socketIO.on(Socket.EVENT_CONNECT, onConnect);
        socketIO.on("positionPusher", onNewMessage);
        socketIO.on("clearDrawFunc", onNewMessageClearDrawFunc);
        socketIO.on("changeColorDrawFunc", onNewMessageChangeColorDrawFunc);

        socketIO.connect();

        MaterialFactory.makeOpaqueWithColor(this,
                new com.google.ar.sceneform.rendering.Color(Color.RED))
                .thenAccept(material1 -> material = material1.makeCopy())
                .exceptionally(this::handleMaterialError);

        initializeSceneView();

        //todo
        // Create the Handler object (on the main thread by default)
//        Handler handler = new Handler();
//        // Define the code block to be executed
//        Runnable runnableCode = new Runnable() {
//            @Override
//            public void run() {
//                // Do something here on the main thread
//                Log.d("Handlers", "Called on main thread");
//
//                if (isConnected()){
//                    if (isTestFlag) {
//                        WebRTCManager.getInstance().init(signalIp, iceServers, new IConnectEvent() {
//                            @Override
//                            public void onSuccess() {
//                                startCall();
//                            }
//
//                            @Override
//                            public void onFailed(String msg) {
//
//                            }
//                        });
//                        //todo randomUniqueId
//                        WebRTCManager.getInstance().connect(videoEnable ? MediaType.TYPE_VIDEO : MediaType.TYPE_AUDIO, randomUniqueId);
//                        isTestFlag = false;
//                    }
//                }else{
//                    isTestFlag = true;
//                }
//
//                // Repeat this the same runnable code block again another 2 seconds
//                // 'this' is referencing the Runnable object
//                handler.postDelayed(this, 2000);
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

    boolean testFlag = true;


    private Emitter.Listener onNewMessageClearDrawFunc = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    clearDraw();
                }
            });
        }
    };

    private Emitter.Listener onNewMessageChangeColorDrawFunc = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    int a = (int) args[0];
                    changeColorDraw(a);
                }
            });
        }
    };

    private Emitter.Listener onNewMessage = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {

                    if (testFlag) {
                        testFlag = false;
                        String a = (String) args[0];
                        String[] b = a.split("-");

                        DisplayMetrics metrics = new DisplayMetrics();
                        getWindowManager().getDefaultDisplay().getMetrics(metrics);

                        widthMain = metrics.widthPixels;
                        heightMain = metrics.heightPixels;

//                        Log.d("test123", "run: " + b[2]);

                        Camera camera = remote_view.getScene().getCamera();
                        Ray ray = camera.screenPointToRay((Float.parseFloat(b[0]) * widthMain) / Float.parseFloat(b[3]), (Float.parseFloat(b[1]) * heightMain) / Float.parseFloat(b[4]));
                        Vector3 drawPoint = ray.getPoint(DRAW_DISTANCE);

                        if (b[2].equals("down")) {
                            if (anchorNode == null) {
                                com.google.ar.core.Camera coreCamera = remote_view.getArFrame().getCamera();
                                if (coreCamera.getTrackingState() != TrackingState.TRACKING) {
                                    return;
                                }
                                Pose pose = coreCamera.getPose();
                                anchorNode = new AnchorNode(remote_view.getSession().createAnchor(pose));
                                anchorNode.setParent(remote_view.getScene());
                            }

                            currentStroke = new Stroke(anchorNode, material);
                            strokes.add(currentStroke);
                            currentStroke.add(drawPoint);

                        } else if (b[2].equals("move") && currentStroke != null) {
                            currentStroke.add(drawPoint);
                        }
                        testFlag = true;
                    }
                }
            });
        }
    };

    private Void handleMaterialError(Throwable throwable) {
        Toast toast = Toast.makeText(this, "Unable to create material", Toast.LENGTH_LONG);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();
        throw new CompletionException(throwable);
    }

    private void initializeSceneView() {
        remote_view.getPlaneRenderer().setVisible(false);

        remote_view.getScene().addOnUpdateListener((this::onUpdateFrame));
        remote_view.getScene().addOnPeekTouchListener(this);

        remote_view.getHolder().addCallback(this);
    }

    @Override
    public void onPeekTouch(HitTestResult hitTestResult, MotionEvent motionEvent) {

        int action = motionEvent.getAction();
        Camera camera = remote_view.getScene().getCamera();
        Ray ray = camera.screenPointToRay(motionEvent.getX(), motionEvent.getY());

//        Log.d("test123", "onPeekTouch: " +motionEvent.getX() + "    :    " + motionEvent.getY());

        Vector3 drawPoint = ray.getPoint(DRAW_DISTANCE);
        if (action == MotionEvent.ACTION_DOWN) {
//            Log.d("test123", "onPeekTouch: " +"down");
            if (anchorNode == null) {
                com.google.ar.core.Camera coreCamera = remote_view.getArFrame().getCamera();
                if (coreCamera.getTrackingState() != TrackingState.TRACKING) {
                    return;
                }
                Pose pose = coreCamera.getPose();
                anchorNode = new AnchorNode(remote_view.getSession().createAnchor(pose));
                anchorNode.setParent(remote_view.getScene());
            }
            currentStroke = new Stroke(anchorNode, material);
            strokes.add(currentStroke);
            currentStroke.add(drawPoint);
        } else if (action == MotionEvent.ACTION_MOVE && currentStroke != null) {
            Log.d("test123", "onPeekTouch: " + "move");
            currentStroke.add(drawPoint);
        }

    }

    private void onUpdateFrame(FrameTime frameTime) {

        // If there is no frame then don't process anything.
        if (remote_view.getArFrame() == null) {
            return;
        }

        // If ARCore is not tracking yet, then don't process anything.
        if (remote_view.getArFrame().getCamera().getTrackingState() != TrackingState.TRACKING) {
            return;
        }

//        Frame frame = arSceneView.getArFrame();

        Bitmap surfaceBitmap = Bitmap.createBitmap(960, 540, Bitmap.Config.ARGB_8888);
        PixelCopy.request(remote_view, surfaceBitmap, copyResult -> {
        }, new Handler(Looper.getMainLooper()));

        int mWidth = surfaceBitmap.getWidth();
        int mHeight = surfaceBitmap.getHeight();

        try {
            manager._peerHelper.captureAndroid.startCapture1(mWidth, mHeight, 30, bitmapToNv21(surfaceBitmap, mWidth, mHeight));
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static byte[] bitmapToNv21(Bitmap src, int width, int height) {
        if (src != null && src.getWidth() >= width && src.getHeight() >= height) {
            int[] argb = new int[width * height];
            src.getPixels(argb, 0, width, 0, 0, width, height);
            return argbToNv21(argb, width, height);
        } else {
            return null;
        }
    }

    private static byte[] argbToNv21(int[] argb, int width, int height) {
        int frameSize = width * height;
        int yIndex = 0;
        int uvIndex = frameSize;
        int index = 0;
        byte[] nv21 = new byte[width * height * 3 / 2];
        for (int j = 0; j < height; ++j) {
            for (int i = 0; i < width; ++i) {
                int R = (argb[index] & 0xFF0000) >> 16;
                int G = (argb[index] & 0x00FF00) >> 8;
                int B = argb[index] & 0x0000FF;
                int Y = (66 * R + 129 * G + 25 * B + 128 >> 8) + 16;
                int U = (-38 * R - 74 * G + 112 * B + 128 >> 8) + 128;
                int V = (112 * R - 94 * G - 18 * B + 128 >> 8) + 128;
                nv21[yIndex++] = (byte) (Y < 0 ? 0 : (Y > 255 ? 255 : Y));
                if (j % 2 == 0 && index % 2 == 0 && uvIndex < nv21.length - 2) {
                    nv21[uvIndex++] = (byte) (V < 0 ? 0 : (V > 255 ? 255 : V));
                    nv21[uvIndex++] = (byte) (U < 0 ? 0 : (U > 255 ? 255 : U));
                }
                ++index;
            }
        }
        return nv21;
    }

    @Override
    public void onResume() {
        super.onResume();

        if (session == null) {
            Exception exception = null;
            String message = null;
            try {
                //todo
                switch (ArCoreApk.getInstance().requestInstall(this, !installRequested)) {
                    case INSTALL_REQUESTED:
                        installRequested = true;
                        return;
                    case INSTALLED:
                        break;
                }

                // ARCore requires camera permissions to operate. If we did not yet obtain runtime
                // permission on Android M and above, now is a good time to ask the user for it.
//                if (!CameraPermissionHelper.hasCameraPermission(this)) {
//                    CameraPermissionHelper.requestCameraPermission(this);
//                    return;
//                }

                session = new Session(/* context = */ this);
//                session = new Session(/* context = */ this, EnumSet.of(Session.Feature.SHARED_CAMERA));
//                Config config = new Config(session);
//                if (enableAutoFocus) {
//                    config.setFocusMode(Config.FocusMode.AUTO);
//                } else {
//                    config.setFocusMode(Config.FocusMode.FIXED);
//                }
//                session.configure(config);

            } catch (UnavailableArcoreNotInstalledException e) {
                message = "Please install ARCore";
                exception = e;
            } catch (UnavailableApkTooOldException e) {
                message = "Please update ARCore";
                exception = e;
            } catch (UnavailableSdkTooOldException e) {
                message = "Please update this app";
                exception = e;
            } catch (Exception e) {
                message = "This device does not support AR";
                exception = e;
            }

            if (message != null) {
                Log.e("error", "Exception creating session", exception);
                return;
            }

            shouldConfigureSession = true;
        }

        if (shouldConfigureSession) {
            configureSession();
            shouldConfigureSession = false;
            remote_view.setupSession(session);
        }

        // Note that order matters - see the note in onPause(), the reverse applies here.
        try {
            session.resume();
            remote_view.resume();
        } catch (CameraNotAvailableException e) {
            // In some cases (such as another camera app launching) the camera may be given to
            // a different app instead. Handle this properly by showing a message and recreate the
            // session at the next iteration.
//            messageSnackbarHelper.showError(this, "Camera not available. Please restart the app.");
            session = null;
            return;
        }

    }

    @Override
    public void onPause() {
        super.onPause();
        if (session != null) {
            // Note that the order matters - GLSurfaceView is paused first so that it does not try
            // to query the session. If Session is paused before GLSurfaceView, GLSurfaceView may
            // still call session.update() and get a SessionPausedException.
            remote_view.pause();
            session.pause();
        }
    }


    private void configureSession() {
        Config config = new Config(session);

        config.setFocusMode(Config.FocusMode.AUTO);
        config.setUpdateMode(Config.UpdateMode.LATEST_CAMERA_IMAGE);

        session.configure(config);
    }


    @SuppressLint("ClickableViewAccessibility")
    private void initListener() {
        if (videoEnable) {
            // 设置小视频可以移动
//            local_view.setOnTouchListener((view, motionEvent) -> {
//                switch (motionEvent.getAction()) {
//                    case MotionEvent.ACTION_DOWN:
//                        previewX = (int) motionEvent.getX();
//                        previewY = (int) motionEvent.getY();
//                        break;
//                    case MotionEvent.ACTION_MOVE:
//                        int x = (int) motionEvent.getX();
//                        int y = (int) motionEvent.getY();
//                        moveX = (int) motionEvent.getX();
//                        moveY = (int) motionEvent.getY();
//                        RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) local_view.getLayoutParams();
//                        lp.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, 0); // Clears the rule, as there is no removeRule until API 17.
//                        lp.addRule(RelativeLayout.ALIGN_PARENT_END, 0);
//                        lp.addRule(RelativeLayout.ALIGN_PARENT_TOP, 0);
//                        lp.addRule(RelativeLayout.ALIGN_PARENT_START, 0);
//                        int left = lp.leftMargin + (x - previewX);
//                        int top = lp.topMargin + (y - previewY);
//                        lp.leftMargin = left;
//                        lp.topMargin = top;
//                        view.setLayoutParams(lp);
//                        break;
//                    case MotionEvent.ACTION_UP:
//                        if (moveX == 0 && moveY == 0) {
//                            view.performClick();
//                        }
//                        moveX = 0;
//                        moveY = 0;
//                        break;
//                }
//                return true;
//            });
        }
    }

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
                if (stream.videoTracks.size() > 0) {
//                    stream.videoTracks.get(0).addSink(localRender);
                }

                if (videoEnable) {
                    stream.videoTracks.get(0).setEnabled(true);
                }
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
                //todo
                runOnUiThread(() -> {
                    disConnect();
                    ChatSingleActivity.this.finish();
                });

            }
        });
        if (!PermissionUtil.isNeedRequestPermission(ChatSingleActivity.this)) {
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


    // 切换摄像头
    public void clearDraw() {
//        manager.switchCamera();
        for (Stroke stroke : strokes) {
            stroke.clear();
        }
        strokes.clear();
    }

    public void changeColorDraw(int colorHex) {
        chatSingleFragment.changeBackgroundColor(colorHex);
        MaterialFactory.makeOpaqueWithColor(this,
                new com.google.ar.sceneform.rendering.Color(colorHex))
                .thenAccept(material1 -> material = material1.makeCopy())
                .exceptionally(this::handleMaterialError);
    }


    // 挂断
    public void hangUp() {
        disConnect();
        this.finish();
    }

    // 静音
    public void toggleMic(boolean enable) {
        manager.toggleMute(enable);
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

    // 扬声器
    public void toggleSpeaker(boolean enable) {
        manager.toggleSpeaker(enable);

    }

    @Override
    protected void onDestroy() {
        disConnect();
        super.onDestroy();

    }

    private void disConnect() {
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
//        if (remote_view != null) {
        //todo
//            remote_view.release();
//            remote_view = null;
//        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        for (int i = 0; i < permissions.length; i++) {
            Log.i(PeerConnectionHelper.TAG, "[Permission] " + permissions[i] + " is " + (grantResults[i] == PackageManager.PERMISSION_GRANTED ? "granted" : "denied"));
            if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                finish();
                break;
            }
        }
        manager.joinRoom(getApplicationContext(), rootEglBase);

    }

    @Override
    public void surfaceCreated(@NonNull SurfaceHolder holder) {

    }

    @Override
    public void surfaceChanged(@NonNull SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(@NonNull SurfaceHolder holder) {

    }
}
