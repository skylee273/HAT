package btcore.co.kr.hatsheal.view.sound;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.databinding.DataBindingUtil;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.DrawableImageViewTarget;
import com.squareup.otto.Subscribe;

import java.io.File;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.text.DecimalFormat;
import java.util.Random;

import btcore.co.kr.hatsheal.bus.BusEventPhoneToDevice;
import btcore.co.kr.hatsheal.bus.BusProviderPhoneToDevice;
import btcore.co.kr.hatsheal.service.BluetoothLeService;
import btcore.co.kr.hatsheal.service.HatService;
import btcore.co.kr.hatsheal.util.FFT4g;
import btcore.co.kr.hatsheal.MainActivity;
import btcore.co.kr.hatsheal.R;
import btcore.co.kr.hatsheal.bus.BusEvent;
import btcore.co.kr.hatsheal.bus.BusProvider;
import btcore.co.kr.hatsheal.databinding.ActivitySoundBinding;
import btcore.co.kr.hatsheal.util.FileUtil;
import btcore.co.kr.hatsheal.util.MyMediaRecorder;
import btcore.co.kr.hatsheal.util.Protocol;
import btcore.co.kr.hatsheal.util.World;
import btcore.co.kr.hatsheal.view.Bluetooth.BluetoothActivity;
import butterknife.OnClick;

import static btcore.co.kr.hatsheal.service.BluetoothLeService.STATE;

/**
 * Created by leehaneul on 2018-04-18.
 */

public class SoundActivity extends AppCompatActivity {

    private final String TAG = getClass().getSimpleName();
    private BluetoothLeService mService = null;
    private static final int UART_PROFILE_DISCONNECTED = 21;
    private static final int UART_PROFILE_CONNECTED = 20;
    private static final int REQUEST_SELECT_DEVICE = 1;
    private static final int REQUEST_ENABLE_BT = 2;
    private static final int UART_PROFILE_READY = 10;
    private int mState = UART_PROFILE_DISCONNECTED;

    private boolean audioFlag = false;
    private MyMediaRecorder mRecorder ;
    private boolean bListener = true;
    private boolean isThreadRun = true;
    private Thread thread;
    float volume = 10000;
    Context mContext;
    HatService hatService;
    private boolean isService = false;
    ActivitySoundBinding soundBinding;
    private int level = 1;
    Protocol protocol;
    Random rnd;
    File file;
    String batteryType;

    @SuppressLint("HandlerLeak")
    final Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg){
            super.handleMessage(msg);
            DecimalFormat df1 = new DecimalFormat("####.0");
            if(msg.what == 1){
                double decibel = World.dbCount;
                Log.d(TAG,df1.format(World.dbCount));
                if(decibel < 50){
                    level = 0;
                }
                else if (decibel >= 50 && decibel < 60) {
                    level = 1;
                }else if(decibel >= 60 && decibel < 65 ){
                    level = 2;
                }else if(decibel >= 65 && decibel < 70){
                    level = 3;
                }else if(decibel >= 70 && decibel < 75){
                    level = 4;
                }else{
                    level = 5;
                }
            }
        }
    };
    ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            HatService.HatBinder mb = (HatService.HatBinder) service;
            hatService = mb.getService();
            isService = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            isService = false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        soundBinding = DataBindingUtil.setContentView(this, R.layout.activity_sound);
        soundBinding.setSoundActivity(this);

        BusProviderPhoneToDevice.getInstance().register(this);

        service_init();

        protocol = new Protocol();

        rnd = new Random();


        Intent intent = getIntent();
        batteryType = intent.getStringExtra("BATTERY");
        batteryUpload(Integer.parseInt(batteryType));
    }
    private void batteryUpload(int type){
        switch (type){
            case 0:
                soundBinding.btnLevel1.setText("5%");
                soundBinding.btnLevel1.setBackgroundResource(R.color.colorBattery);
                soundBinding.btnLevel2.setBackgroundResource(0);
                soundBinding.btnLevel3.setBackgroundResource(0);
                soundBinding.btnLevel4.setBackgroundResource(0);
                soundBinding.btnLevel5.setBackgroundResource(0);
                break;
            case 1:
                soundBinding.btnLevel1.setText("25%");
                soundBinding.btnLevel1.setBackgroundResource(R.color.colorBattery);
                soundBinding.btnLevel2.setBackgroundResource(R.color.colorBattery);
                soundBinding.btnLevel3.setBackgroundResource(0);
                soundBinding.btnLevel4.setBackgroundResource(0);
                soundBinding.btnLevel5.setBackgroundResource(0);
                break;
            case 2:
                soundBinding.btnLevel1.setText("50%");
                soundBinding.btnLevel1.setBackgroundResource(R.color.colorBattery);
                soundBinding.btnLevel2.setBackgroundResource(R.color.colorBattery);
                soundBinding.btnLevel3.setBackgroundResource(R.color.colorBattery);
                soundBinding.btnLevel4.setBackgroundResource(0);
                soundBinding.btnLevel5.setBackgroundResource(0);
                break;
            case 3:
                soundBinding.btnLevel1.setText("75%");
                soundBinding.btnLevel1.setBackgroundResource(R.color.colorBattery);
                soundBinding.btnLevel2.setBackgroundResource(R.color.colorBattery);
                soundBinding.btnLevel3.setBackgroundResource(R.color.colorBattery);
                soundBinding.btnLevel4.setBackgroundResource(R.color.colorBattery);
                soundBinding.btnLevel5.setBackgroundResource(0);
                break;
            case 4:
                soundBinding.btnLevel1.setText("100%");
                soundBinding.btnLevel1.setBackgroundResource(R.color.colorBattery);
                soundBinding.btnLevel2.setBackgroundResource(R.color.colorBattery);
                soundBinding.btnLevel3.setBackgroundResource(R.color.colorBattery);
                soundBinding.btnLevel4.setBackgroundResource(R.color.colorBattery);
                soundBinding.btnLevel5.setBackgroundResource(R.color.colorBattery);
                break;
        }
    }

    @OnClick(R.id.image_equalizer)
    public void onSoundEqaul(View view) {

        if(!audioFlag){
            DrawableImageViewTarget  gifImage = new DrawableImageViewTarget(soundBinding.imageEqualizer);
            Glide.with(this).load(R.raw.icon_equl_stream).into(gifImage);
            mRecorder = new MyMediaRecorder();
            file = FileUtil.createFile("temp.amr");
            if (file != null) {
                startRecord(file);
            } else {
                // Toast.makeText(getApplicationContext(), getString(R.string.activity_recFileErr), Toast.LENGTH_LONG).show();
            }
            bListener = true;
            audioFlag = true;
            isThreadRun = true;
        }
       else{
            soundBinding.imageEqualizer.setImageResource(R.drawable.icon_equal);
            Log.d(TAG, "CANCLE");
            if(thread != null) { thread.interrupt(); }
            thread = null;
            mRecorder.delete(); //Stop recording and delete the recording file
            mRecorder = null;
            bListener = false;
            audioFlag = false;
            isThreadRun = false;
            file = null;
        }
    }

    @OnClick(R.id.btn_back)
    public void onSoundBack(View view) {
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        startActivity(intent);
        finish();
    }
    @OnClick(R.id.btn_disconnect)
    public void onDisconnect(View view) {
        Intent intent = new Intent(getApplicationContext(), BluetoothActivity.class);
        startActivity(intent);
        finish();
    }
    @Subscribe
    public void FinishLoad(BusEventPhoneToDevice eventPhoneToDevice) {
        Log.d(TAG, "데이터 수신" + String.valueOf(mService.getState()));
        //if(mService.getState() == 2 && eventPhoneToDevice.getEventType() == 0) { send(eventPhoneToDevice.getEventData()); }
        //if(mService.getState() == 2 && eventPhoneToDevice.getEventType() == 1) { send(eventPhoneToDevice.getEventData()); }
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        if (thread != null) {
            thread.interrupt();
            isThreadRun = false;
            thread = null;
        }
        if(mRecorder != null) { mRecorder.delete(); }

            BusProviderPhoneToDevice.getInstance().unregister(this);
        try {
            LocalBroadcastManager.getInstance(this).unregisterReceiver(UARTStatusChangeReceiver);
        } catch (Exception ignore) {
            android.util.Log.e(TAG, ignore.toString());
        }
        if (isService == true) {
            unbindService(connection);
            isService = false;
        }

        if (mService != null) {
            unbindService(mServiceConnection);
        }
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        startActivity(intent);
        finish();
    }

    public void send(byte[] data) {
        mService.writeRXCharacteristic(data);
    }


    public void service_init() {
        Intent bindIntent = new Intent(this, BluetoothLeService.class);
        bindService(bindIntent, mServiceConnection, Context.BIND_AUTO_CREATE);
        LocalBroadcastManager.getInstance(this).registerReceiver(UARTStatusChangeReceiver, makeGattUpdateIntentFilter());
    }

    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(BluetoothLeService.ACTION_BATTERY_VALUE);
        intentFilter.addAction(BluetoothLeService.DEVICE_DOES_NOT_SUPPORT_UART);
        return intentFilter;
    }

    private ServiceConnection mServiceConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder rawBinder) {
            mService = ((BluetoothLeService.LocalBinder) rawBinder).getService();
            Log.d(TAG, "onServiceConnected mService= " + mService);
            if (!mService.initialize()) {
                Log.e(TAG, "Unable to initialize Bluetooth");
                finish();
            }
        }

        public void onServiceDisconnected(ComponentName classname) {
            mService = null;
        }
    };
    private final BroadcastReceiver UARTStatusChangeReceiver = new BroadcastReceiver() {

        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(BluetoothLeService.ACTION_GATT_CONNECTED)) {
                runOnUiThread(new Runnable() {
                    public void run() {
                        android.util.Log.d(TAG, "UART_CONNECT_MSG");
                        mState = UART_PROFILE_CONNECTED;
                    }
                });
            }

            //*********************//
            if (action.equals(BluetoothLeService.ACTION_GATT_DISCONNECTED)) {
                runOnUiThread(new Runnable() {
                    public void run() {
                        android.util.Log.d(TAG, "UART_DISCONNECT_MSG");
                        mState = UART_PROFILE_DISCONNECTED;
                        mService.close();

                        Intent intent1 = new Intent(getApplicationContext(), BluetoothActivity.class);
                        startActivity(intent1);
                        finish();

                    }
                });
            }
            if (action.equals(BluetoothLeService.ACTION_BATTERY_VALUE)) {
                final String Type = intent.getStringExtra(BluetoothLeService.EXTRA_DATA);
                runOnUiThread(new Runnable() {
                    public void run() {
                        int type = Integer.parseInt(Type);
                        switch (type) {
                            case 0:
                                soundBinding.btnLevel1.setText("5%");
                                soundBinding.btnLevel1.setBackgroundResource(R.color.colorBattery);
                                soundBinding.btnLevel2.setBackgroundResource(0);
                                soundBinding.btnLevel3.setBackgroundResource(0);
                                soundBinding.btnLevel4.setBackgroundResource(0);
                                soundBinding.btnLevel5.setBackgroundResource(0);
                                break;
                            case 1:
                                soundBinding.btnLevel1.setText("25%");
                                soundBinding.btnLevel1.setBackgroundResource(R.color.colorBattery);
                                soundBinding.btnLevel2.setBackgroundResource(R.color.colorBattery);
                                soundBinding.btnLevel3.setBackgroundResource(0);
                                soundBinding.btnLevel4.setBackgroundResource(0);
                                soundBinding.btnLevel5.setBackgroundResource(0);
                                break;
                            case 2:
                                soundBinding.btnLevel1.setText("50%");
                                soundBinding.btnLevel1.setBackgroundResource(R.color.colorBattery);
                                soundBinding.btnLevel2.setBackgroundResource(R.color.colorBattery);
                                soundBinding.btnLevel3.setBackgroundResource(R.color.colorBattery);
                                soundBinding.btnLevel4.setBackgroundResource(0);
                                soundBinding.btnLevel5.setBackgroundResource(0);
                                break;
                            case 3:
                                soundBinding.btnLevel1.setText("75%");
                                soundBinding.btnLevel1.setBackgroundResource(R.color.colorBattery);
                                soundBinding.btnLevel2.setBackgroundResource(R.color.colorBattery);
                                soundBinding.btnLevel3.setBackgroundResource(R.color.colorBattery);
                                soundBinding.btnLevel4.setBackgroundResource(R.color.colorBattery);
                                soundBinding.btnLevel5.setBackgroundResource(0);
                                break;
                            case 4:
                                soundBinding.btnLevel1.setText("100%");
                                soundBinding.btnLevel1.setBackgroundResource(R.color.colorBattery);
                                soundBinding.btnLevel2.setBackgroundResource(R.color.colorBattery);
                                soundBinding.btnLevel3.setBackgroundResource(R.color.colorBattery);
                                soundBinding.btnLevel4.setBackgroundResource(R.color.colorBattery);
                                soundBinding.btnLevel5.setBackgroundResource(R.color.colorBattery);
                                break;
                        }
                    }
                });
            }


            if (action.equals(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED)) {
                mService.enableTXNotification();
            }
            if (action.equals(BluetoothLeService.DEVICE_DOES_NOT_SUPPORT_UART)) {
                mService.disconnect();
            }
        }
    };

    private void startListenAudio() {
        thread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (isThreadRun) {
                    try {
                        if(bListener) {
                            volume = mRecorder.getMaxAmplitude();  //Get the sound pressure value
                            if(volume > 0 && volume < 1000000) {
                                World.setDbCount(20 * (float)(Math.log10(volume)));  //Change the sound pressure value to the decibel value
                                // Update with thread
                                Message message = new Message();
                                message.what = 1;
                                handler.sendMessage(message);
                            }
                        }
                        if (level == 0) { Thread.sleep(5000);}
                        else if(level == 1){ Thread.sleep(3000); }
                        else if ( level == 2 ) {  Thread.sleep(1000);}
                        else if ( level == 3 ) {  Thread.sleep(750); }
                        else if ( level == 4 ) {  Thread.sleep(250);}
                        else { Thread.sleep(100); }
                        if(STATE) { send(protocol.RGBDataStreaming(rnd.nextInt(256), rnd.nextInt(256), rnd.nextInt(256))); }
                    } catch (Exception e) {
                        Log.d(TAG, e.toString());
                        bListener = false;
                    }
                }
            }
        });
        thread.start();
    }
    /**
     * Start recording
     * @param fFile
     */
    public void startRecord(File fFile){
        try{
            mRecorder.setMyRecAudioFile(fFile);
            if (mRecorder.startRecorder()) {
                startListenAudio();
            }else{
                //Toast.makeText(this, getString(R.string.activity_recStartErr), Toast.LENGTH_SHORT).show();
            }
        }catch(Exception e){
            //Toast.makeText(this, getString(R.string.activity_recBusyErr), Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

}
