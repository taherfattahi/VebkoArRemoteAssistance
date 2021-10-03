package com.dds.webrtclib.ui;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.core.content.ContextCompat;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.dds.webrtclib.R;
import com.dds.webrtclib.utils.Utils;
import com.flask.colorpicker.ColorPickerView;
import com.flask.colorpicker.OnColorChangedListener;
import com.flask.colorpicker.OnColorSelectedListener;
import com.flask.colorpicker.builder.ColorPickerClickListener;
import com.flask.colorpicker.builder.ColorPickerDialogBuilder;
import com.getbase.floatingactionbutton.FloatingActionButton;
import com.getbase.floatingactionbutton.FloatingActionsMenu;

/**
 * 单聊控制界面
 * Created by dds on 2019/1/7.
 * android_shuai@163.com
 */
public class ChatSingleFragment extends Fragment {

    public View rootView;
    private ImageView wr_switch_mute;
    private ImageView wr_switch_hang_up;
    private ImageView wr_switch_bluetooth;
    private TextView textViewStroke, textViewDistance;
    private SeekBar seekBarDistance, seekBarStroke;
    private boolean isBlueTooth = false;
    //    private TextView wr_clear_draw;
//    private TextView wr_hand_free;
    private boolean enableMic = true;
    private boolean enableSpeaker = false;
    private boolean videoEnable;
    private boolean isToogle = true;
    private ChatSingleActivity activity;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        activity = (ChatSingleActivity) getActivity();
        Bundle bundle = getArguments();
        if (bundle != null) {
            videoEnable = bundle.getBoolean("videoEnable");
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (rootView == null) {
            rootView = onInitloadView(inflater, container, savedInstanceState);
            initView(rootView);
            initListener();
        }
        return rootView;
    }

    private View onInitloadView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.wr_fragment_room_control_single, container, false);
    }

//    private int currentBackgroundColor = 0xffffffff;
    private int currentBackgroundColor = 0xFFFF0000;

    private void initView(View rootView) {
        wr_switch_mute = rootView.findViewById(R.id.wr_switch_mute);
        wr_switch_hang_up = rootView.findViewById(R.id.wr_switch_hang_up);
        wr_switch_bluetooth = rootView.findViewById(R.id.wr_switch_bluetooth);
        seekBarDistance = rootView.findViewById(R.id.seekBarDistance);
        seekBarStroke = rootView.findViewById(R.id.seekBarStroke);
        textViewDistance = rootView.findViewById(R.id.textViewDistance);
        textViewStroke = rootView.findViewById(R.id.textViewStroke);

        seekBarStroke.setVisibility(View.GONE);
        seekBarDistance.setVisibility(View.GONE);
        textViewStroke.setVisibility(View.GONE);
        textViewDistance.setVisibility(View.GONE);

//        seekBarDistance.incrementProgressBy(1);
        seekBarDistance.setProgress(28);
        seekBarDistance.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                activity.setDistanceFromSickbarSender(((float)progress)/100);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        seekBarStroke.setProgress(5);
        seekBarStroke.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                activity.setStrokFromSickbarSender(((float)progress)/10000);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        final FloatingActionsMenu famSetting = (FloatingActionsMenu) rootView.findViewById(R.id.famSetting);
        final FloatingActionButton faColorPicker = (FloatingActionButton) rootView.findViewById(R.id.faColorPicker);
        final FloatingActionButton faClearDraw = (FloatingActionButton) rootView.findViewById(R.id.faClearDraw);
        final FloatingActionButton faSettingDraw = (FloatingActionButton) rootView.findViewById(R.id.faSettingDraw);

        faColorPicker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                actionA.setTitle("Action A clicked");

//                final Context context = SampleActivity.this;

                famSetting.toggle();

                ColorPickerDialogBuilder
                        .with(activity)
                        .setTitle(R.string.color_dialog_title)
                        .initialColor(currentBackgroundColor)
                        .wheelType(ColorPickerView.WHEEL_TYPE.FLOWER)
                        .density(12)
                        .setOnColorChangedListener(new OnColorChangedListener() {
                            @Override
                            public void onColorChanged(int selectedColor) {
                                // Handle on color change
                                Log.d("ColorPicker", "onColorChanged: 0x" + Integer.toHexString(selectedColor));
                            }
                        })
                        .setOnColorSelectedListener(new OnColorSelectedListener() {
                            @Override
                            public void onColorSelected(int selectedColor) {
//                                toast("onColorSelected: 0x" + Integer.toHexString(selectedColor));
                            }
                        })
                        .setPositiveButton("ok", new ColorPickerClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int selectedColor, Integer[] allColors) {
                                changeBackgroundColor(selectedColor);
                                if (allColors != null) {
                                    activity.changeColorDrawSender(selectedColor);
                                }
                            }
                        })
                        .setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                            }
                        })
                        .showColorEdit(true)
                        .setColorEditTextColor(ContextCompat.getColor(activity, android.R.color.holo_blue_bright))
                        .build()
                        .show();

            }
        });

        faClearDraw.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                famSetting.toggle();
                activity.clearDrawSender();
            }
        });

        faSettingDraw.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                famSetting.toggle();
                if (isToogle){
                    seekBarStroke.setVisibility(View.VISIBLE);
                    seekBarDistance.setVisibility(View.VISIBLE);
                    textViewStroke.setVisibility(View.VISIBLE);
                    textViewDistance.setVisibility(View.VISIBLE);
                }else{
                    seekBarStroke.setVisibility(View.GONE);
                    seekBarDistance.setVisibility(View.GONE);
                    textViewStroke.setVisibility(View.GONE);
                    textViewDistance.setVisibility(View.GONE);
                }
                isToogle = !isToogle;
            }
        });

//        wr_clear_draw = rootView.findViewById(R.id.wr_switch_camera);
//        wr_hand_free = rootView.findViewById(R.id.wr_hand_free);
//        if (videoEnable) {
//            wr_hand_free.setVisibility(View.GONE);
//            wr_switch_camera.setVisibility(View.VISIBLE);
//        } else {
//            wr_hand_free.setVisibility(View.VISIBLE);
//            wr_switch_camera.setVisibility(View.GONE);
//        }
    }

    public void changeBackgroundColor(int selectedColor) {
        currentBackgroundColor = selectedColor;
//        root.setBackgroundColor(selectedColor);
    }


    private void initListener() {
        wr_switch_mute.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                enableMic = !enableMic;
                if (enableMic) {
                    Drawable drawable = ContextCompat.getDrawable(activity, R.drawable.webrtc_mute_default);
                    if (drawable != null) {
                        drawable.setBounds(0, 0, Utils.dip2px(activity, 60), Utils.dip2px(activity, 60));
                    }
                    wr_switch_mute.setImageDrawable(drawable);
                } else {
                    Drawable drawable = ContextCompat.getDrawable(activity, R.drawable.webrtc_mute);
                    if (drawable != null) {
                        drawable.setBounds(0, 0, Utils.dip2px(activity, 60), Utils.dip2px(activity, 60));
                    }
                    wr_switch_mute.setImageDrawable(drawable);
                }
                activity.toggleMic(enableMic);

            }
        });
        wr_switch_hang_up.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                activity.hangUp();
            }
        });

        wr_switch_bluetooth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                activity.toggleBlueToothOn(!isBlueTooth);
                isBlueTooth = !isBlueTooth;
                if (isBlueTooth)
                    wr_switch_bluetooth.setImageResource(R.drawable.bluetooth_circle_round_press);
                else{
                    wr_switch_bluetooth.setImageResource(R.drawable.bluetooth_circle_round_default);
                }
            }
        });
//        wr_clear_draw.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                activity.clearDraw();
//            }
//        });

//        wr_hand_free.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                enableSpeaker = !enableSpeaker;
//                if (enableSpeaker) {
//                    Drawable drawable = ContextCompat.getDrawable(activity, R.drawable.webrtc_hands_free);
//                    if (drawable != null) {
//                        drawable.setBounds(0, 0, Utils.dip2px(activity, 60), Utils.dip2px(activity, 60));
//                    }
//                    wr_hand_free.setCompoundDrawables(null, drawable, null, null);
//                } else {
//                    Drawable drawable = ContextCompat.getDrawable(activity, R.drawable.webrtc_hands_free_default);
//                    if (drawable != null) {
//                        drawable.setBounds(0, 0, Utils.dip2px(activity, 60), Utils.dip2px(activity, 60));
//                    }
//                    wr_hand_free.setCompoundDrawables(null, drawable, null, null);
//                }
//                activity.toggleSpeaker(enableSpeaker);
//            }
//        });
    }

}
