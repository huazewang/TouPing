package iHuaze.Yinao.Home;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
//import android.support.v7.widget.RecyclerView;
//import android.support.v7.widget.SwitchCompat;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.activity.CaptureActivity;
import com.hpplay.common.utils.DeviceUtil;
import com.hpplay.common.utils.LeLog;
import com.hpplay.common.utils.NetworkUtil;
import com.hpplay.sdk.source.api.IConnectListener;
import com.hpplay.sdk.source.api.ILelinkPlayerListener;
import com.hpplay.sdk.source.api.LelinkPlayerInfo;
import com.hpplay.sdk.source.api.LelinkSourceSDK;
import com.hpplay.sdk.source.browse.api.IAPI;
import com.hpplay.sdk.source.browse.api.IBrowseListener;
import com.hpplay.sdk.source.browse.api.IParceResultListener;
import com.hpplay.sdk.source.browse.api.LelinkServiceInfo;

import androidx.appcompat.widget.SwitchCompat;
import androidx.recyclerview.widget.RecyclerView;
import iHuaze.Yinao.Home.adapter.BrowseAdapter;
import iHuaze.Yinao.Home.utils.AssetsUtil;
import iHuaze.Yinao.Home.utils.CameraPermissionCompat;

import java.lang.ref.WeakReference;
import java.util.List;

/**
 * jasinCao
 * 19-7-25 下午2:50
 */
public class MainActivity extends Activity implements View.OnClickListener {

    private static final String TAG = "MainActivity";
    private static final String WIFI_AP_STATE_CHANGED_ACTION = "android.net.wifi.WIFI_AP_STATE_CHANGED";
    private static final String NET_VIDEO_URL = "https://v.mifile.cn/b2c-mimall-media/ed921294fb62caf889d40502f5b38147.mp4";
    private static final String NET_MUSIC_URL = "http://music.163.com/song/media/outer/url?id=287248.mp3";
    private static final String NET_PICTURE_URL = "http://news.cri.cn/gb/mmsource/images/2013/06/23/2/2211679758122940818.jpg";
    private static final String LOCAL_MEDIA_PATH = "/hpplay_demo/local_media/";
    private static final String SDCARD_LOCAL_MEDIA_PATH = Environment.getExternalStorageDirectory()
            + LOCAL_MEDIA_PATH;
    private static final String LOCAL_VIDEO_URL = SDCARD_LOCAL_MEDIA_PATH + "test_video.mp4";
    private static final String LOCAL_MUSIC_URL = SDCARD_LOCAL_MEDIA_PATH + "EDC - I Never Told You.mp3";
    private static final String LOCAL_PICTURE_URL = SDCARD_LOCAL_MEDIA_PATH + "I01027343.jpg";

    private static final int REQUEST_CAMERA_PERMISSION = 2;


    private static final int MSG_SEARCH_RESULT = 100;
    private static final int MSG_CONNECT_FAILURE = 101;
    private static final int MSG_CONNECT_SUCCESS = 102;
    private static final int MSG_UPDATE_PROGRESS = 103;

    private TextView mTvVersion, mTvWifi, mTvIp;
    private RecyclerView mBrowseRecyclerView;
    private EditText mEtPinCode;
    private RadioGroup mRadioGroup, mRgResolution, mRgBitRate, mRgMirrorAudio, rg_auto_bitrate;
    private EditText mEtNetVideo, mEtNetMusic, mEtNetPicture;
    private EditText mEtLocalVideo, mEtLocalMusic, mEtLocalPicture, mAppidEdit;
    private SeekBar mProgressBar, seekbarVolume;
    private UIHandler mUiHandler;
    private CheckBox mCheckBox;
    private SwitchCompat mirrorSwtich;

    private BrowseAdapter mBrowseAdapter;
    private NetworkReceiver mNetworkReceiver;
    private LelinkServiceInfo mSelectInfo;
    private boolean isPause;
    private boolean isAutoBitrate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mUiHandler = new UIHandler(this);
        initViews();
        initDatas();
        initSDKStatusListener();
    }

    void initSDKStatusListener() {
        LelinkSourceSDK.getInstance().setBrowseResultListener(iBrowseListener);
        LelinkSourceSDK.getInstance().setConnectListener(iConnectListener);
        LelinkSourceSDK.getInstance().setPlayListener(lelinkPlayerListener);
    }


    @Override
    protected void onResume() {
        super.onResume();
    }

    private void initViews() {
        mTvVersion = registView(R.id.tv_version);
        mTvWifi = registView(R.id.tv_wifi);
        mTvIp = registView(R.id.tv_ip);
        mBrowseRecyclerView = registView(R.id.recycler_browse);
        mEtPinCode = registView(R.id.et_pincode);
        mRadioGroup = registView(R.id.radio_group);
        mEtNetVideo = registView(R.id.et_net_video);
        mEtNetMusic = registView(R.id.et_net_music);
        mEtNetPicture = registView(R.id.et_net_picture);
        mEtLocalVideo = registView(R.id.et_local_video);
        mEtLocalMusic = registView(R.id.et_local_music);
        mEtLocalPicture = registView(R.id.et_local_picture);
        mProgressBar = registView(R.id.seekbar_progress);
        seekbarVolume = registView(R.id.seekbar_volume);
        mRgResolution = registView(R.id.rg_resolution);
        mRgBitRate = registView(R.id.rg_bitrate);
        mRgMirrorAudio = registView(R.id.rg_mirror_audio);
        mCheckBox = registView(R.id.checkbox);
        mAppidEdit = registView(R.id.edit_appid);
        mirrorSwtich = registView(R.id.mirror_switch);
        rg_auto_bitrate = registView(R.id.rg_auto_bitrate);
        registView(R.id.btn_browse);
        registView(R.id.btn_stop_browse);
        registView(R.id.btn_disconnect);
        registView(R.id.btn_qrcode);
        registView(R.id.btn_delete);
        registView(R.id.btn_pincode_connect);
        registView(R.id.rb_net_video);
        registView(R.id.rb_net_music);
        registView(R.id.rb_net_picture);
        registView(R.id.rb_local_video);
        registView(R.id.rb_local_music);
        registView(R.id.rb_local_picture);
        registView(R.id.btn_play);
        registView(R.id.btn_pause);
        registView(R.id.btn_stop);
        registView(R.id.btn_volume_up);
        registView(R.id.btn_volume_down);
        registView(R.id.btn_set_ad_listener);
        registView(R.id.btn_report_ad_show);
        registView(R.id.btn_report_ad_end);
        registView(R.id.btn_send_mediaasset_info);
        registView(R.id.btn_send_error_info);
        registView(R.id.btn_send_passth_info);
        registView(R.id.btn_send_header_info);
        registView(R.id.btn_send_lebo_passth_info);
        registView(R.id.btn_loop_mode);
        registView(R.id.btn_3rd_monitor);
        registView(R.id.btn_pushbtn_click);
        registView(R.id.btn_list_gone);
        registView(R.id.send_danmaku);
        registView(R.id.danmaku_settings);
        registView(R.id.btn_screenshot);

        rg_auto_bitrate.setOnCheckedChangeListener(autoBitrateChangeListener);
        mProgressBar.setOnSeekBarChangeListener(mProgressChangeListener);
        seekbarVolume.setOnSeekBarChangeListener(mProgressChangeListener);
        mirrorSwtich.setOnCheckedChangeListener(onCheckedChangeListener);
    }


    private void initDatas() {
        copyMediaToSDCard();
        mEtNetVideo.setText(NET_VIDEO_URL);
        mEtNetMusic.setText(NET_MUSIC_URL);
        mEtNetPicture.setText(NET_PICTURE_URL);

        mNetworkReceiver = new NetworkReceiver(MainActivity.this);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(WIFI_AP_STATE_CHANGED_ACTION);
        intentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(mNetworkReceiver, intentFilter);

        mTvVersion.setText("SDK:" + com.hpplay.sdk.source.api.BuildConfig.BUILD_TYPE
                + "-" + com.hpplay.sdk.source.api.BuildConfig.VERSION_NAME);
        refreshWifiName();

        mBrowseAdapter = new BrowseAdapter(getApplicationContext());
        mBrowseRecyclerView.setAdapter(mBrowseAdapter);
        mBrowseAdapter.setOnItemClickListener(new BrowseAdapter.OnItemClickListener() {

            @Override
            public void onClick(int position, LelinkServiceInfo info) {
//                connect(info);
//                if (mSelectInfo != null) {
//                    LelinkSourceSDK.getInstance().disConnect(mSelectInfo);
//                }
                mSelectInfo = info;
                mBrowseAdapter.setSelectInfo(info);
                mBrowseAdapter.notifyDataSetChanged();
            }

        });

    }

    CompoundButton.OnCheckedChangeListener onCheckedChangeListener = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
            if (b) {
                startMirror();
            } else {
                LelinkSourceSDK.getInstance().stopPlay();
            }
        }
    };

    private SeekBar.OnSeekBarChangeListener mProgressChangeListener = new SeekBar.OnSeekBarChangeListener() {

        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            // ignore
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
            // ignore
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            int progress = seekBar.getProgress();
            if (seekBar.getId() == R.id.seekbar_progress) {
                LelinkSourceSDK.getInstance().seekTo(progress);
            } else if (seekBar.getId() == R.id.seekbar_volume) {
                LelinkSourceSDK.getInstance().setVolume(progress);
            }
        }
    };


    ILelinkPlayerListener lelinkPlayerListener = new ILelinkPlayerListener() {


        @Override
        public void onLoading() {
            mUiHandler.post(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(getApplicationContext(), "开始加载", Toast.LENGTH_SHORT).show();
                }
            });
        }

        @Override
        public void onStart() {
            isPause = false;
            mUiHandler.post(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(getApplicationContext(), "开始播放", Toast.LENGTH_SHORT).show();
                    mirrorSwitchChange();
                }
            });

        }

        @Override
        public void onPause() {
            isPause = true;
            mUiHandler.post(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(getApplicationContext(), "暂停播放", Toast.LENGTH_SHORT).show();
                }
            });

        }

        @Override
        public void onCompletion() {
            mUiHandler.post(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(getApplicationContext(), "播放完成", Toast.LENGTH_SHORT).show();
                }
            });
        }

        @Override
        public void onStop() {
            mUiHandler.post(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(getApplicationContext(), "播放停止", Toast.LENGTH_SHORT).show();
                    mirrorSwitchChange();
                }
            });
        }

        @Override
        public void onSeekComplete(int i) {

        }

        @Override
        public void onInfo(int i, int i1) {

        }

        String text = null;

        @Override
        public void onError(int what, int extra) {
            Log.d(TAG, "onError what:" + what + " extra:" + extra);
            if (what == ILelinkPlayerListener.PUSH_ERROR_INIT) {
                if (extra == ILelinkPlayerListener.PUSH_ERRROR_FILE_NOT_EXISTED) {
                    text = "文件不存在";
                } else if (extra == ILelinkPlayerListener.PUSH_ERROR_IM_OFFLINE) {
                    text = "IM TV不在线";
                } else if (extra == ILelinkPlayerListener.PUSH_ERROR_IMAGE) {

                } else if (extra == ILelinkPlayerListener.PUSH_ERROR_IM_UNSUPPORTED_MIMETYPE) {
                    text = "IM不支持的媒体类型";
                } else {
                    text = "未知";
                }
            } else if (what == ILelinkPlayerListener.MIRROR_ERROR_INIT) {
                if (extra == ILelinkPlayerListener.MIRROR_ERROR_UNSUPPORTED) {
                    text = "不支持镜像";
                } else if (extra == ILelinkPlayerListener.MIRROR_ERROR_REJECT_PERMISSION) {
                    text = "镜像权限拒绝";
                } else if (extra == ILelinkPlayerListener.MIRROR_ERROR_DEVICE_UNSUPPORTED) {
                    text = "设备不支持镜像";
                } else if (extra == ILelinkPlayerListener.NEED_SCREENCODE) {
                    text = "请输入投屏码";
                }
            } else if (what == ILelinkPlayerListener.MIRROR_ERROR_PREPARE) {
                if (extra == ILelinkPlayerListener.MIRROR_ERROR_GET_INFO) {
                    text = "获取镜像信息出错";
                } else if (extra == ILelinkPlayerListener.MIRROR_ERROR_GET_PORT) {
                    text = "获取镜像端口出错";
                } else if (extra == ILelinkPlayerListener.NEED_SCREENCODE) {
                    text = "请输入投屏码";
                    if (extra == ILelinkPlayerListener.GRAP_UNSUPPORTED) {
                        text = "投屏码模式不支持抢占";
                    }
                } else if (what == ILelinkPlayerListener.PUSH_ERROR_PLAY) {
                    if (extra == ILelinkPlayerListener.PUSH_ERROR_NOT_RESPONSED) {
                        text = "播放无响应";
                    } else if (extra == ILelinkPlayerListener.NEED_SCREENCODE) {
                        text = "请输入投屏码";
                    } else if (extra == ILelinkPlayerListener.RELEVANCE_DATA_UNSUPPORTED) {
                        text = "老乐联不支持数据透传,请升级接收端的版本！";
                    } else if (extra == ILelinkPlayerListener.GRAP_UNSUPPORTED) {
                        text = "投屏码模式不支持抢占";
                    }
                } else if (what == ILelinkPlayerListener.PUSH_ERROR_STOP) {
                    if (extra == ILelinkPlayerListener.PUSH_ERROR_NOT_RESPONSED) {
                        text = "退出 播放无响应";
                    }
                } else if (what == ILelinkPlayerListener.PUSH_ERROR_PAUSE) {
                    if (extra == ILelinkPlayerListener.PUSH_ERROR_NOT_RESPONSED) {
                        text = "暂停无响应";
                    }
                } else if (what == ILelinkPlayerListener.PUSH_ERROR_RESUME) {
                    if (extra == ILelinkPlayerListener.PUSH_ERROR_NOT_RESPONSED) {
                        text = "恢复无响应";
                    }
                }

            } else if (what == ILelinkPlayerListener.MIRROR_ERROR_CODEC) {
                if (extra == ILelinkPlayerListener.MIRROR_ERROR_NETWORK_BROKEN) {
                    text = "镜像网络断开";
                }
            }
            if (null != mUiHandler) {
                mUiHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        mirrorSwitchChange();
                        Toast.makeText(getApplicationContext(), text, Toast.LENGTH_SHORT).show();
                    }
                });
            }

        }

        @Override
        public void onVolumeChanged(float v) {

        }

        @Override
        public void onPositionUpdate(long l, long l1) {
            if (mUiHandler != null) {
                Message msg = new Message();
                msg.what = MSG_UPDATE_PROGRESS;
                msg.arg1 = (int) l;
                msg.arg2 = (int) l1;
                mUiHandler.sendMessage(msg);
            }
        }
    };

    public void mirrorSwitchChange() {
        if (LelinkSourceSDK.getInstance().getOption(IAPI.OPTION_32) == LelinkSourceSDK.MIRROR_PLAYING) {
            mirrorSwtich.setChecked(true);
        } else {
            mirrorSwtich.setChecked(false);
        }
    }


    public void getPincodeDevice() {
        LelinkSourceSDK.getInstance().addPinCodeToLelinkServiceInfo(mEtPinCode.getText().toString(), new IParceResultListener() {
            @Override
            public void onParceResult(int resultCode, LelinkServiceInfo info) {
                if (resultCode == IParceResultListener.PARCE_SUCCESS) {
                    mSelectInfo = info;
                }
            }
        });
    }


    private void startCaptureActivity() {
        CameraPermissionCompat.checkCameraPermission(this, new CameraPermissionCompat.OnCameraPermissionListener() {

            @Override
            public void onGrantResult(boolean granted) {
                Log.d(TAG, "权限--------->" + granted);
                if (granted) {
                    // 允许，打开二维码
                    Intent intent = new Intent(MainActivity.this, CaptureActivity.class);
                    startActivityForResult(intent, REQUEST_CAMERA_PERMISSION);
                } else {
                    // 若没有授权，会弹出一个对话框（这个对话框是系统的，开发者不能自己定制），用户选择是否授权应用使用系统权限
                }
            }

        });

    }

    public void getQRCodeDevice(String qrUrl) {
        Log.d(TAG, qrUrl);
        LelinkSourceSDK.getInstance().addQRCodeToLelinkServiceInfo(qrUrl, new IParceResultListener() {
            @Override
            public void onParceResult(int resultCode, LelinkServiceInfo info) {
                if (resultCode == IParceResultListener.PARCE_SUCCESS) {
                    mSelectInfo = info;
                }
            }
        });
    }

    /**
     * 连接设备
     *
     * @param serviceInfo
     */
    void connect(LelinkServiceInfo serviceInfo) {
        LelinkSourceSDK.getInstance().connect(serviceInfo);
    }

    private static class NetworkReceiver extends BroadcastReceiver {

        private WeakReference<MainActivity> mReference;

        public NetworkReceiver(MainActivity pReference) {
            mReference = new WeakReference<>(pReference);
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            if (null == mReference || null == mReference.get()) {
                return;
            }
            MainActivity mainActivity = mReference.get();
            String action = intent.getAction();
            if (ConnectivityManager.CONNECTIVITY_ACTION.equalsIgnoreCase(action) ||
                    MainActivity.WIFI_AP_STATE_CHANGED_ACTION.equalsIgnoreCase(action)) {
                mainActivity.refreshWifiName();
            }
        }
    }

    public void refreshWifiName() {
        mTvWifi.setText("WiFi:" + NetworkUtil.getNetWorkName(getApplicationContext()));
        mTvIp.setText(DeviceUtil.getIPAddress(getApplicationContext()));
    }

    private void copyMediaToSDCard() {
        AssetsUtil.getInstance(getApplicationContext())
                .copyAssetsToSD("local_media", LOCAL_MEDIA_PATH)
                .setFileOperateCallback(new AssetsUtil.FileOperateCallback() {

                    @Override
                    public void onSuccess() {
                        mEtLocalVideo.setText(LOCAL_VIDEO_URL);
                        mEtLocalMusic.setText(LOCAL_MUSIC_URL);
                        mEtLocalPicture.setText(LOCAL_PICTURE_URL);
                    }

                    @Override
                    public void onFailed(String error) {
                        LeLog.e(TAG, error);
                    }
                });
    }


    IBrowseListener iBrowseListener = new IBrowseListener() {

        @Override
        public void onBrowse(int i, List<LelinkServiceInfo> list) {
            LeLog.i("onBrowsess", "-------------->list size : " + list.size());
            if (i == IBrowseListener.BROWSE_ERROR_AUTH) {
                mUiHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplicationContext(), "授权失败", Toast.LENGTH_SHORT).show();
                    }
                });
                return;
            }
            if (mUiHandler != null) {
                mUiHandler.sendMessage(Message.obtain(null, MSG_SEARCH_RESULT, list));
            }
        }

    };

    IConnectListener iConnectListener = new IConnectListener() {
        @Override
        public void onConnect(LelinkServiceInfo lelinkServiceInfo, int extra) {

            LeLog.d("pincode", "onConnect:" + lelinkServiceInfo.getName());
            if (mUiHandler != null) {
                mUiHandler.sendMessage(Message.obtain(null, MSG_CONNECT_SUCCESS, extra, 0, lelinkServiceInfo));
            }
        }

        @Override
        public void onDisconnect(LelinkServiceInfo lelinkServiceInfo, int what, int extra) {
            LeLog.d("pincode", "onDisconnect:" + lelinkServiceInfo.getName() + " disConnectType:" + what + " extra:" + extra);
            String text = null;
            if (what == IConnectListener.CONNECT_INFO_DISCONNECT) {
                if (null != mUiHandler) {
                    if (TextUtils.isEmpty(lelinkServiceInfo.getName())) {
                        text = "pin码连接断开";
                    } else {
                        text = lelinkServiceInfo.getName() + "连接断开";
                    }
                }
            } else if (what == IConnectListener.CONNECT_ERROR_FAILED) {
                if (extra == IConnectListener.CONNECT_ERROR_IO) {
                    text = lelinkServiceInfo.getName() + "连接失败";
                } else if (extra == IConnectListener.CONNECT_ERROR_IM_WAITTING) {
                    text = lelinkServiceInfo.getName() + "等待确认";
                } else if (extra == IConnectListener.CONNECT_ERROR_IM_REJECT) {
                    text = lelinkServiceInfo.getName() + "连接拒绝";
                } else if (extra == IConnectListener.CONNECT_ERROR_IM_TIMEOUT) {
                    text = lelinkServiceInfo.getName() + "连接超时";
                } else if (extra == IConnectListener.CONNECT_ERROR_IM_BLACKLIST) {
                    text = lelinkServiceInfo.getName() + "连接黑名单";
                }
            }
            if (null != mUiHandler) {
                mUiHandler.sendMessage(Message.obtain(null, MSG_CONNECT_FAILURE, text));
            }
        }
    };

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_browse:
                LelinkSourceSDK.getInstance().startBrowse();
                break;
            case R.id.btn_stop_browse:
                LelinkSourceSDK.getInstance().stopBrowse();
                break;
            case R.id.btn_qrcode:
//                startCaptureActivity();
                getQRCodeDevice("http://sl.hpplay.cn/c4sBJS");
                break;
            case R.id.btn_disconnect:
                LelinkSourceSDK.getInstance().disConnect(mSelectInfo);
                LeLog.i("getpalystate", "" + LelinkSourceSDK.getInstance().getOption(IAPI.OPTION_32));
                break;
            case R.id.btn_pause:
                LelinkSourceSDK.getInstance().pause();
                break;
            case R.id.btn_play:
                startPlayMedia();
                break;
            case R.id.btn_stop:
                LelinkSourceSDK.getInstance().stopPlay();
                break;
            case R.id.btn_volume_up:
                LelinkSourceSDK.getInstance().addVolume();
                break;
            case R.id.btn_volume_down:
                LelinkSourceSDK.getInstance().subVolume();
                break;
            case R.id.btn_delete:
                Intent intent2 = new Intent();
                intent2.setType("image/*");
//                intent2.setType("video/*");
                intent2.setAction(Intent.ACTION_OPEN_DOCUMENT);
                startActivityForResult(intent2, 1);
                break;
        }
    }


    void startPlayMedia() {
        if (null == mSelectInfo) {
            Toast.makeText(getApplicationContext(), "请选择接设备", Toast.LENGTH_SHORT).show();
            return;
        }
        if (isPause) {
            isPause = false;
            // 暂停中
            LelinkSourceSDK.getInstance().resume();
            return;
        }
        int checkedId = mRadioGroup.getCheckedRadioButtonId();
        int mediaType = 0;
        String mediaTypeStr = null;
        boolean isLocalFile = false;
        String url = null;
        switch (checkedId) {
            case R.id.rb_net_video:
                mediaType = LelinkSourceSDK.MEDIA_TYPE_VIDEO;
                url = mEtNetVideo.getText().toString();
                mediaTypeStr = "NetVideo";
                break;
            case R.id.rb_net_music:
                mediaType = LelinkSourceSDK.MEDIA_TYPE_AUDIO;
                url = mEtNetMusic.getText().toString();
                mediaTypeStr = "NetMusic";
                break;
            case R.id.rb_net_picture:
                mediaType = LelinkSourceSDK.MEDIA_TYPE_IMAGE;
                url = mEtNetPicture.getText().toString();
                mediaTypeStr = "NetPicture";
                break;
            case R.id.rb_local_video:
                mediaType = LelinkSourceSDK.MEDIA_TYPE_VIDEO;
                isLocalFile = true;
                url = mEtLocalVideo.getText().toString();
                mediaTypeStr = "LocalVideo";
                break;
            case R.id.rb_local_music:
                mediaType = LelinkSourceSDK.MEDIA_TYPE_AUDIO;
                isLocalFile = true;
                url = mEtLocalMusic.getText().toString();
                mediaTypeStr = "LocalMusic";
                break;
            case R.id.rb_local_picture:
                mediaType = LelinkSourceSDK.MEDIA_TYPE_IMAGE;
                isLocalFile = true;
                url = mEtLocalPicture.getText().toString();
                mediaTypeStr = "LocalPicture";
                break;
        }
        LeLog.i(TAG, "start play url:" + url + " type:" + mediaTypeStr);

        LelinkPlayerInfo lelinkPlayerInfo = new LelinkPlayerInfo();
        if (isLocalFile) {
            lelinkPlayerInfo.setLocalPath(url);
        } else {
            lelinkPlayerInfo.setUrl(url);
        }
        lelinkPlayerInfo.setType(mediaType);
        LelinkSourceSDK.getInstance().startPlayMediaImmed(mSelectInfo, url, mediaType, isLocalFile);
    }



    RadioGroup.OnCheckedChangeListener autoBitrateChangeListener = new RadioGroup.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(RadioGroup group, int checkedId) {

            if (checkedId == R.id.rb_mirror_auto_bitrate_on) {
                isAutoBitrate = true;
                mRgResolution.getChildAt(0).setEnabled(false);
                mRgResolution.getChildAt(1).setEnabled(false);
                mRgResolution.getChildAt(2).setEnabled(false);
                mRgBitRate.getChildAt(0).setEnabled(false);
                mRgBitRate.getChildAt(1).setEnabled(false);
                mRgBitRate.getChildAt(2).setEnabled(false);
            } else {
                isAutoBitrate = false;
                mRgResolution.getChildAt(0).setEnabled(true);
                mRgResolution.getChildAt(1).setEnabled(true);
                mRgResolution.getChildAt(2).setEnabled(true);
                mRgBitRate.getChildAt(0).setEnabled(true);
                mRgBitRate.getChildAt(1).setEnabled(true);
                mRgBitRate.getChildAt(2).setEnabled(false);

            }
        }
    };

    void startMirror() {
        // 分辨率
        int resolutionLevel = 0;
        int resolutionCheckId = mRgResolution.getCheckedRadioButtonId();
        switch (resolutionCheckId) {
            case R.id.rb_resolution_height:
                resolutionLevel = LelinkSourceSDK.RESOLUTION_HEIGHT;
                break;
            case R.id.rb_resolution_middle:
                resolutionLevel = LelinkSourceSDK.RESOLUTION_MIDDLE;
                break;
            case R.id.rb_resolution_low:
                resolutionLevel = LelinkSourceSDK.RESOLUTION_AUTO;
                break;
        }

        // 比特率
        int bitrateLevel = 0;
        int bitrateCheckId = mRgBitRate.getCheckedRadioButtonId();
        switch (bitrateCheckId) {
            case R.id.rb_bitrate_height:
                bitrateLevel = LelinkSourceSDK.BITRATE_HEIGHT;
                break;
            case R.id.rb_bitrate_middle:
                bitrateLevel = LelinkSourceSDK.BITRATE_MIDDLE;
                break;
            case R.id.rb_bitrate_low:
                bitrateLevel = LelinkSourceSDK.BITRATE_LOW;
                break;
        }
        // 音频
        boolean audioEnable = true;
        int audioCheckId = mRgMirrorAudio.getCheckedRadioButtonId();
        switch (audioCheckId) {
            case R.id.rb_mirror_audio_on:
                audioEnable = true;
                break;
            case R.id.rb_mirror_audio_off:
                audioEnable = false;
                break;
        }

         //两种方法都可镜像
//        LelinkPlayerInfo lelinkPlayerInfo = new LelinkPlayerInfo();
//        lelinkPlayerInfo.setOption(IAPI.OPTION_31, isAutoBitrate);
//        lelinkPlayerInfo.setLelinkServiceInfo(mSelectInfo);
//        lelinkPlayerInfo.setBitRateLevel(bitrateLevel);
//        lelinkPlayerInfo.setResolutionLevel(resolutionLevel);
//        lelinkPlayerInfo.setMirrorAudioEnable(audioEnable);
//         LelinkSourceSDK.getInstance().startMirror(lelinkPlayerInfo);
        LelinkSourceSDK.getInstance().startMirror(mSelectInfo, audioEnable, isAutoBitrate);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            if (REQUEST_CAMERA_PERMISSION == requestCode) {
                String scanResult = data.getStringExtra(CaptureActivity.INTENT_EXTRA_KEY_QR_SCAN);
                getQRCodeDevice(scanResult);
            } else {

                Uri uri = data.getData();
                LelinkPlayerInfo lelinkPlayerInfo = new LelinkPlayerInfo();
                lelinkPlayerInfo.setLoaclUri(uri);
                lelinkPlayerInfo.setType(LelinkPlayerInfo.TYPE_IMAGE);
                lelinkPlayerInfo.setLelinkServiceInfo(mSelectInfo);
                LelinkSourceSDK.getInstance().startPlayMedia(lelinkPlayerInfo);
            }
        }

    }

    private static class UIHandler extends Handler {

        private WeakReference<MainActivity> mReference;

        UIHandler(MainActivity reference) {
            mReference = new WeakReference<>(reference);
        }

        @Override
        public void handleMessage(Message msg) {
            MainActivity mainActivity = mReference.get();
            if (mainActivity == null) {
                return;
            }
            switch (msg.what) {
                case MSG_SEARCH_RESULT:
                    try {
                        if (msg.obj != null) {
                            mainActivity.updateBrowseAdapter((List<LelinkServiceInfo>) msg.obj);
                        }
                    } catch (Exception e) {
                        LeLog.w(TAG, e);
                    }
                    break;
                case MSG_CONNECT_SUCCESS:
                    try {
                        if (msg.obj != null) {
                            LelinkServiceInfo serviceInfo = (LelinkServiceInfo) msg.obj;
                            String type = msg.arg1 == IConnectListener.TYPE_LELINK ? "Lelink"
                                    : msg.arg1 == IConnectListener.TYPE_DLNA ? "DLNA"
                                    : msg.arg1 == IConnectListener.TYPE_NEW_LELINK ? "NEW_LELINK" : "IM";
                            Toast.makeText(mainActivity, type + "  " + serviceInfo.getName() + "连接成功", Toast.LENGTH_SHORT).show();
                            mainActivity.mSelectInfo = serviceInfo;
                        }
                    } catch (Exception e) {
                        LeLog.w(TAG, e);
                    }
                    break;
                case MSG_CONNECT_FAILURE:
                    if (msg.obj != null) {
                        Toast.makeText(mainActivity, msg.obj.toString(), Toast.LENGTH_SHORT).show();
                    }
                    break;
                case MSG_UPDATE_PROGRESS:
                    mainActivity.mProgressBar.setMax(msg.arg1);
                    mainActivity.mProgressBar.setProgress(msg.arg2);
                    break;
            }
            super.handleMessage(msg);
        }

    }


    private void updateBrowseAdapter(List<LelinkServiceInfo> infos) {
        mBrowseAdapter.updateDatas(infos);
    }

    public <T extends View> T registView(int id) {
        T v = findViewById(id);
        if (v instanceof Button || v instanceof RadioButton) {
            Log.i(TAG, " btn id " + id);
            v.setOnClickListener(this);
        }
        return v;
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        LelinkSourceSDK.getInstance().stopPlay();
        LelinkSourceSDK.getInstance().unBindSdk();
    }
}
