package btcore.co.kr.hatsheal;

import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.databinding.DataBindingUtil;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.squareup.otto.Subscribe;

import btcore.co.kr.hatsheal.bus.BusEventPhoneToDevice;
import btcore.co.kr.hatsheal.bus.BusProviderPhoneToDevice;
import btcore.co.kr.hatsheal.databinding.ActivityMainBinding;
import btcore.co.kr.hatsheal.service.BluetoothLeService;
import btcore.co.kr.hatsheal.service.HatService;
import btcore.co.kr.hatsheal.view.Bluetooth.BluetoothActivity;
import btcore.co.kr.hatsheal.view.lamp.LampActivity;
import btcore.co.kr.hatsheal.view.lampani.LampAniActivity;
import btcore.co.kr.hatsheal.view.mode.ModeActivity;
import btcore.co.kr.hatsheal.view.setting.SettingActivity;
import btcore.co.kr.hatsheal.view.sound.SoundActivity;
import butterknife.OnClick;

import static btcore.co.kr.hatsheal.service.BluetoothLeService.STATE;

public class MainActivity extends AppCompatActivity {

    private final String TAG = getClass().getSimpleName();
    ActivityMainBinding mainBinding;
    private BluetoothLeService mService = null;
    private static final int UART_PROFILE_DISCONNECTED = 21;
    private static final int UART_PROFILE_CONNECTED = 20;
    private static final int REQUEST_SELECT_DEVICE = 1;
    private static final int REQUEST_ENABLE_BT = 2;
    private static final int UART_PROFILE_READY = 10;
    private int mState = UART_PROFILE_DISCONNECTED;
    private BluetoothAdapter mBtAdapter = null;
    String batteryType;

    HatService hatService;
    private boolean isService = false;

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

        mainBinding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        mainBinding.setMainActivity(this);

        BusProviderPhoneToDevice.getInstance().register(this);

        // 블루투스 서비스 시작
        service_init();
    }

    @OnClick(R.id.btn_mode)
    public void onMode(View view){
        Intent intent = new Intent(getApplicationContext(), ModeActivity.class);
        intent.putExtra("BATTERY", batteryType);
        startActivity(intent);
        finish();
    }
    @OnClick(R.id.btn_lamp)
    public void onLamp(View view){
        Intent intent = new Intent(getApplicationContext(), LampActivity.class);
        intent.putExtra("BATTERY", batteryType);
        startActivity(intent);
        finish();
    }
    @OnClick(R.id.btn_lamp_ani)
    public void onLampAni(View view){
        Intent intent = new Intent(getApplicationContext(), LampAniActivity.class);
        intent.putExtra("BATTERY", batteryType);
        startActivity(intent);
        finish();
    }
    @OnClick(R.id.btn_sound)
    public void onSound(View view){
        Intent intent = new Intent(getApplicationContext(), SoundActivity.class);
        intent.putExtra("BATTERY", batteryType);
        startActivity(intent);
        finish();
    }
    @OnClick(R.id.btn_menu)
    public void onSet(View view){
        startActivity(new Intent(getApplicationContext(), SettingActivity.class));
        overridePendingTransition(R.anim.pull_in_left, R.anim.push_out_right);
        finish();
    }
    @OnClick(R.id.btn_disconnect)
    public void onDisconnect(View view){
       Intent intent = new Intent(getApplicationContext(), BluetoothActivity.class);
       startActivity(intent);
       finish();
    }

    @Subscribe
    public void FinishLoad(BusEventPhoneToDevice eventPhoneToDevice) {
        if(STATE) {
            if(eventPhoneToDevice.getEventType() == 0) { send(eventPhoneToDevice.getEventData()); }
            if(eventPhoneToDevice.getEventType() == 1) { send(eventPhoneToDevice.getEventData()); }
        }
    }
    @Override
    public void onBackPressed() {
            moveTaskToBack(true);
    }
    public void send(byte[] data) {
        mService.writeRXCharacteristic(data);
    }
    public void service_init() {
        mBtAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBtAdapter == null) {
            Toast.makeText(this, "Bluetooth is not available", Toast.LENGTH_LONG).show();
            finish();
            return;
        }
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
                batteryType = intent.getStringExtra(BluetoothLeService.EXTRA_DATA);
                runOnUiThread(new Runnable() {
                    public void run() {
                        int type = Integer.parseInt(batteryType);
                        switch (type) {
                            case 0:
                                mainBinding.btnBattery1.setText("5%");
                                mainBinding.btnBattery1.setBackgroundResource(R.color.colorBattery);
                                mainBinding.btnBattery2.setBackgroundResource(0);
                                mainBinding.btnBattery3.setBackgroundResource(0);
                                mainBinding.btnBattery4.setBackgroundResource(0);
                                mainBinding.btnBattery5.setBackgroundResource(0);
                                break;
                            case 1:
                                mainBinding.btnBattery1.setText("25%");
                                mainBinding.btnBattery1.setBackgroundResource(R.color.colorBattery);
                                mainBinding.btnBattery2.setBackgroundResource(R.color.colorBattery);
                                mainBinding.btnBattery3.setBackgroundResource(0);
                                mainBinding.btnBattery4.setBackgroundResource(0);
                                mainBinding.btnBattery5.setBackgroundResource(0);
                                break;
                            case 2:
                                mainBinding.btnBattery1.setText("50%");
                                mainBinding.btnBattery1.setBackgroundResource(R.color.colorBattery);
                                mainBinding.btnBattery2.setBackgroundResource(R.color.colorBattery);
                                mainBinding.btnBattery3.setBackgroundResource(R.color.colorBattery);
                                mainBinding.btnBattery4.setBackgroundResource(0);
                                mainBinding.btnBattery5.setBackgroundResource(0);
                                break;
                            case 3:
                                mainBinding.btnBattery1.setText("75%");
                                mainBinding.btnBattery1.setBackgroundResource(R.color.colorBattery);
                                mainBinding.btnBattery2.setBackgroundResource(R.color.colorBattery);
                                mainBinding.btnBattery3.setBackgroundResource(R.color.colorBattery);
                                mainBinding.btnBattery4.setBackgroundResource(R.color.colorBattery);
                                mainBinding.btnBattery5.setBackgroundResource(0);
                                break;
                            case 4:
                                mainBinding.btnBattery1.setText("100%");
                                mainBinding.btnBattery1.setBackgroundResource(R.color.colorBattery);
                                mainBinding.btnBattery2.setBackgroundResource(R.color.colorBattery);
                                mainBinding.btnBattery3.setBackgroundResource(R.color.colorBattery);
                                mainBinding.btnBattery4.setBackgroundResource(R.color.colorBattery);
                                mainBinding.btnBattery5.setBackgroundResource(R.color.colorBattery);
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

    @Override
    public void onResume(){
        super.onResume();
        if(isService == false){
            Intent intent = new Intent(
                    MainActivity.this, // 현재 화면
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
        if (mService != null) {
            unbindService(mServiceConnection);
        }
        if(isService == true){
            unbindService(connection);
            isService = false;
        }
    }


}
