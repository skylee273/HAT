package btcore.co.kr.hatsheal.view.lampani;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.databinding.DataBindingUtil;
import android.graphics.Color;
import android.os.Bundle;
import android.os.IBinder;
import android.support.design.widget.Snackbar;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import com.christophesmet.android.views.colorpicker.ColorPickerView;

import btcore.co.kr.hatsheal.MainActivity;
import btcore.co.kr.hatsheal.R;
import btcore.co.kr.hatsheal.bus.BusEventPhoneToDevice;
import btcore.co.kr.hatsheal.bus.BusProviderPhoneToDevice;
import btcore.co.kr.hatsheal.databinding.ActivityLampBinding;
import btcore.co.kr.hatsheal.databinding.ActivityLampaniBinding;
import btcore.co.kr.hatsheal.service.BluetoothLeService;
import btcore.co.kr.hatsheal.service.HatService;
import btcore.co.kr.hatsheal.util.TimePickerFragment;
import btcore.co.kr.hatsheal.view.Bluetooth.BluetoothActivity;
import btcore.co.kr.hatsheal.view.lamp.Lamp;
import btcore.co.kr.hatsheal.view.lamp.LampActivity;
import btcore.co.kr.hatsheal.view.lamp.presenter.LampPresenter;
import butterknife.OnClick;

import static btcore.co.kr.hatsheal.service.BluetoothLeService.STATE;

public class LampAniActivity extends AppCompatActivity {

    private final String TAG = getClass().getSimpleName();
    private SharedPreferences.Editor editor;
    private SharedPreferences pref = null;
    HatService hatService;
    private boolean isService = false;
    private BluetoothLeService mService = null;
    private static final int UART_PROFILE_DISCONNECTED = 21;
    private static final int UART_PROFILE_CONNECTED = 20;
    private static final int REQUEST_SELECT_DEVICE = 1;
    private static final int REQUEST_ENABLE_BT = 2;
    private static final int UART_PROFILE_READY = 10;
    private int mState = UART_PROFILE_DISCONNECTED;
    String batteryType;

    ActivityLampaniBinding activityLampaniBinding;

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

        activityLampaniBinding = DataBindingUtil.setContentView(this, R.layout.activity_lampani);
        activityLampaniBinding.setLampAniActivity(this);

        pref = getSharedPreferences("HAT", Activity.MODE_PRIVATE);
        editor = pref.edit();

        BusProviderPhoneToDevice.getInstance().register(this);

        service_init();

        Intent intent = getIntent();
        batteryType = intent.getStringExtra("BATTERY");
        batteryUpload(Integer.parseInt(batteryType));


    }
    private void batteryUpload(int type){
        switch (type){
            case 0:
                activityLampaniBinding.btnBattery1.setText("5%");
                activityLampaniBinding.btnBattery1.setBackgroundResource(R.color.colorBattery);
                activityLampaniBinding.btnBattery2.setBackgroundResource(0);
                activityLampaniBinding.btnBattery3.setBackgroundResource(0);
                activityLampaniBinding.btnBattery4.setBackgroundResource(0);
                activityLampaniBinding.btnBattery5.setBackgroundResource(0);
                break;
            case 1:
                activityLampaniBinding.btnBattery1.setText("25%");
                activityLampaniBinding.btnBattery1.setBackgroundResource(R.color.colorBattery);
                activityLampaniBinding.btnBattery2.setBackgroundResource(R.color.colorBattery);
                activityLampaniBinding.btnBattery3.setBackgroundResource(0);
                activityLampaniBinding.btnBattery4.setBackgroundResource(0);
                activityLampaniBinding.btnBattery5.setBackgroundResource(0);
                break;
            case 2:
                activityLampaniBinding.btnBattery1.setText("50%");
                activityLampaniBinding.btnBattery1.setBackgroundResource(R.color.colorBattery);
                activityLampaniBinding.btnBattery2.setBackgroundResource(R.color.colorBattery);
                activityLampaniBinding.btnBattery3.setBackgroundResource(R.color.colorBattery);
                activityLampaniBinding.btnBattery4.setBackgroundResource(0);
                activityLampaniBinding.btnBattery5.setBackgroundResource(0);
                break;
            case 3:
                activityLampaniBinding.btnBattery1.setText("75%");
                activityLampaniBinding.btnBattery1.setBackgroundResource(R.color.colorBattery);
                activityLampaniBinding.btnBattery2.setBackgroundResource(R.color.colorBattery);
                activityLampaniBinding.btnBattery3.setBackgroundResource(R.color.colorBattery);
                activityLampaniBinding.btnBattery4.setBackgroundResource(R.color.colorBattery);
                activityLampaniBinding.btnBattery5.setBackgroundResource(0);
                break;
            case 4:
                activityLampaniBinding.btnBattery1.setText("100%");
                activityLampaniBinding.btnBattery1.setBackgroundResource(R.color.colorBattery);
                activityLampaniBinding.btnBattery2.setBackgroundResource(R.color.colorBattery);
                activityLampaniBinding.btnBattery3.setBackgroundResource(R.color.colorBattery);
                activityLampaniBinding.btnBattery4.setBackgroundResource(R.color.colorBattery);
                activityLampaniBinding.btnBattery5.setBackgroundResource(R.color.colorBattery);
                break;
        }
    }

    @OnClick(R.id.btn_back)
    public void onBack(View view) {
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
    @OnClick(R.id.image_title)
    public void onTest(View view) {
        Intent intent = new Intent(getApplicationContext(), PattenActivity.class);
        startActivity(intent);
        finish();
    }


    public void FinishLoad(BusEventPhoneToDevice eventPhoneToDevice) {
        if(STATE) {
            if(eventPhoneToDevice.getEventType() == 0) { send(eventPhoneToDevice.getEventData()); }
            if(eventPhoneToDevice.getEventType() == 1) { send(eventPhoneToDevice.getEventData()); }
        }
    }

    @Override
    public void onResume(){
        super.onResume();
        if(isService == false){
            Intent intent = new Intent(
                    LampAniActivity.this, // 현재 화면
                    HatService.class); // 다음넘어갈 컴퍼넌트
            bindService(intent, // intent 객체
                    connection, // 서비스와 연결에 대한 정의
                    Context.BIND_AUTO_CREATE);
        }

    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        BusProviderPhoneToDevice.getInstance().unregister(this);

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
    }
    @Override
    public void onBackPressed() {
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        startActivity(intent);
        finish();
    }
    public void send(byte [] data) {
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
                                activityLampaniBinding.btnBattery1.setText("5%");
                                activityLampaniBinding.btnBattery1.setBackgroundResource(R.color.colorBattery);
                                activityLampaniBinding.btnBattery2.setBackgroundResource(0);
                                activityLampaniBinding.btnBattery3.setBackgroundResource(0);
                                activityLampaniBinding.btnBattery4.setBackgroundResource(0);
                                activityLampaniBinding.btnBattery5.setBackgroundResource(0);
                                break;
                            case 1:
                                activityLampaniBinding.btnBattery1.setText("25%");
                                activityLampaniBinding.btnBattery1.setBackgroundResource(R.color.colorBattery);
                                activityLampaniBinding.btnBattery2.setBackgroundResource(R.color.colorBattery);
                                activityLampaniBinding.btnBattery3.setBackgroundResource(0);
                                activityLampaniBinding.btnBattery4.setBackgroundResource(0);
                                activityLampaniBinding.btnBattery5.setBackgroundResource(0);
                                break;
                            case 2:
                                activityLampaniBinding.btnBattery1.setText("50%");
                                activityLampaniBinding.btnBattery1.setBackgroundResource(R.color.colorBattery);
                                activityLampaniBinding.btnBattery2.setBackgroundResource(R.color.colorBattery);
                                activityLampaniBinding.btnBattery3.setBackgroundResource(R.color.colorBattery);
                                activityLampaniBinding.btnBattery4.setBackgroundResource(0);
                                activityLampaniBinding.btnBattery5.setBackgroundResource(0);
                                break;
                            case 3:
                                activityLampaniBinding.btnBattery1.setText("75%");
                                activityLampaniBinding.btnBattery1.setBackgroundResource(R.color.colorBattery);
                                activityLampaniBinding.btnBattery2.setBackgroundResource(R.color.colorBattery);
                                activityLampaniBinding.btnBattery3.setBackgroundResource(R.color.colorBattery);
                                activityLampaniBinding.btnBattery4.setBackgroundResource(R.color.colorBattery);
                                activityLampaniBinding.btnBattery5.setBackgroundResource(0);
                                break;
                            case 4:
                                activityLampaniBinding.btnBattery1.setText("100%");
                                activityLampaniBinding.btnBattery1.setBackgroundResource(R.color.colorBattery);
                                activityLampaniBinding.btnBattery2.setBackgroundResource(R.color.colorBattery);
                                activityLampaniBinding.btnBattery3.setBackgroundResource(R.color.colorBattery);
                                activityLampaniBinding.btnBattery4.setBackgroundResource(R.color.colorBattery);
                                activityLampaniBinding.btnBattery5.setBackgroundResource(R.color.colorBattery);
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


}