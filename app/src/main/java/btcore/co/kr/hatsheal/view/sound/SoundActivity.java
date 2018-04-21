package btcore.co.kr.hatsheal.view.sound;

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
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;


import com.squareup.otto.Subscribe;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

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
import btcore.co.kr.hatsheal.view.Bluetooth.BluetoothActivity;
import butterknife.OnClick;

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
    private int SAMPLING_RATE = 44100;
    private int FFT_SIZE = 4096;
    private double dB_baseline = Math.pow(2, 15) * FFT_SIZE * Math.sqrt(2);
    private double resol = ((SAMPLING_RATE / (double) FFT_SIZE));
    private AudioRecord audioRec = null;
    private boolean bIsRecording = false;
    private int bufSize;
    private Thread fft;
    HatService hatService;
    private boolean isService = false;
    ActivitySoundBinding soundBinding;

    ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            HatService.HatBinder mb = (HatService.HatBinder)service;
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

        bufSize = AudioRecord.getMinBufferSize(SAMPLING_RATE,
                AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT);

        BusProvider.getInstance().register(this);
        BusProviderPhoneToDevice.getInstance().register(this);

        service_init();

    }


    @OnClick(R.id.image_equalizer)
    public void onSoundEqaul(View view){
        audioRec = new AudioRecord(MediaRecorder.AudioSource.MIC,
                SAMPLING_RATE, AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT, bufSize * 2);
        audioRec.startRecording();
        bIsRecording = true;

        if(bIsRecording == true){
            fft = new Thread(new Runnable() {
                @Override
                public void run() {
                    byte buf[] = new byte[bufSize * 2];
                    while (bIsRecording) {
                        audioRec.read(buf, 0, buf.length);

                        ByteBuffer bf = ByteBuffer.wrap(buf);
                        bf.order(ByteOrder.LITTLE_ENDIAN);
                        short[] s = new short[(int) FFT_SIZE];
                        for (int i = bf.position(); i < bf.capacity() / 2; i++) {
                            s[i] = bf.getShort();
                        }

                        FFT4g fft = new FFT4g(FFT_SIZE);
                        double[] FFTdata = new double[FFT_SIZE];
                        for (int i = 0; i < FFT_SIZE; i++) {
                            FFTdata[i] = (double) s[i];
                        }
                        fft.rdft(1, FFTdata);

                        double[] dbfs = new double[FFT_SIZE / 2];
                        double max_db = -120d;
                        int max_i = 0;
                        for (int i = 0; i < FFT_SIZE; i += 2) {
                            dbfs[i / 2] = (int) (20 * Math.log10(Math.sqrt(Math
                                    .pow(FFTdata[i], 2)
                                    + Math.pow(FFTdata[i + 1], 2)) / dB_baseline));
                            if (max_db < dbfs[i / 2]) {
                                max_db = dbfs[i / 2];
                                max_i = i / 2;
                            }
                        }

                        Log.d("fft","주파수："+ resol * max_i+" [Hz] 음량：" +  max_db+" [dB]");
                        int rgb[] = waveLengthToRGB(resol * max_i);

                        for(int color : rgb)
                            Log.d("Color", String.valueOf(color));
                    }
                    audioRec.stop();
                    audioRec.release();
                }
            });

            fft.start();
        }else{
            fft.interrupt();
        }

    }
    @OnClick(R.id.btn_back)
    public void onSoundBack(View view) {
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        startActivity(intent);
        finish();
    }
    @Subscribe
    public void FinishLoad(BusEventPhoneToDevice eventPhoneToDevice) {
        Log.d(TAG, "데이터 수신" + String.valueOf(mService.getState()));
        //if(mService.getState() == 2 && eventPhoneToDevice.getEventType() == 0) { sendCommand(eventPhoneToDevice.getEventData()); }
        //if(mService.getState() == 2 && eventPhoneToDevice.getEventType() == 1) { sendCommand(eventPhoneToDevice.getEventData()); }
    }

    @Subscribe
    public void FinishLoad(BusEvent mBusEvent) {
        int mBattery = mBusEvent.getEventData();
        switch (mBattery) {
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

    @Override
    public void onDestroy() {
        super.onDestroy();
        BusProvider.getInstance().unregister(this);
        BusProviderPhoneToDevice.getInstance().unregister(this);
        android.util.Log.d(TAG, "onDestroy()");

        try {
            LocalBroadcastManager.getInstance(this).unregisterReceiver(UARTStatusChangeReceiver);
        } catch (Exception ignore) {
            android.util.Log.e(TAG, ignore.toString());
        }
        if(isService == true){
            unbindService(connection);
            isService = false;
        }

        if (mService != null) {
            unbindService(mServiceConnection);
        }

        bIsRecording = false;

    }

    @Override
    public void onBackPressed() {
        bIsRecording = false;
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        startActivity(intent);
        finish();
    }

    private int[] waveLengthToRGB(double Wavelength){
        double factor;
        int Red,Green,Blue;

        if((Wavelength >= 100) && (Wavelength<349.2)){
            Red = 82;
            Green = 0;
            Blue = 0;
        }else if((Wavelength >= 349.2) && (Wavelength<370)){
            Red = 116;
            Green = 0;
            Blue = 0;
        }else if((Wavelength >= 370) && (Wavelength<392.0)){
            Red = 179;
            Green = 0;
            Blue = 0;
        }else if((Wavelength >= 392.0) && (Wavelength<415.3)){
            Red = 238;
            Green = 0;
            Blue = 0;
        }else if((Wavelength >= 415.3) && (Wavelength<440.0)){
            Red = 255;
            Green = 99;
            Blue = 0;
        }else if((Wavelength >= 440.0) && (Wavelength<466.2)){
            Red = 255;
            Green = 236;
            Blue = 0;
        }else if((Wavelength >= 466.2) && (Wavelength<493.9)){
            Red = 153;
            Green = 255;
            Blue = 0;
        }else if((Wavelength >= 493.9) && (Wavelength<523.2)){
            Red = 40;
            Green = 255;
            Blue = 0;
        }else if((Wavelength >= 523.2) && (Wavelength<554.4)){
            Red = 0;
            Green = 255;
            Blue = 232;
        }else if((Wavelength >= 554.4) && (Wavelength<587.3)){
            Red = 0;
            Green = 124;
            Blue = 255;
        }else if((Wavelength >= 587.3) && (Wavelength<622.2)){
            Red = 5;
            Green = 0;
            Blue = 255;
        }else if((Wavelength >= 622.2) && (Wavelength<659.3)){
            Red = 69;
            Green = 0;
            Blue = 234;
        }else if((Wavelength >= 659.3) && (Wavelength<698.5)){
            Red = 87;
            Green = 0;
            Blue = 158;
        }else{
            Red = 0;
            Green = 0;
            Blue = 0;
        };

        int[] rgb = new int[3];

        rgb[0] = Red;
        rgb[1] = Green;
        rgb[2] = Blue;

        return rgb;
    }
    public void sendCommand(String data) {
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
        intentFilter.addAction(BluetoothLeService.ACTION_DATA_AVAILABLE);
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


            //*********************//
            if (action.equals(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED)) {
                mService.enableTXNotification();
            }
            //*********************//
            if (action.equals(BluetoothLeService.ACTION_DATA_AVAILABLE)) {
                runOnUiThread(new Runnable() {
                    public void run() {
                        try {

                        } catch (Exception e) {
                            android.util.Log.e(TAG, e.toString());
                        }
                    }
                });
            }
            //*********************//
            if (action.equals(BluetoothLeService.DEVICE_DOES_NOT_SUPPORT_UART)) {
                mService.disconnect();
            }
        }
    };

}
