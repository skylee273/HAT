package btcore.co.kr.hatsheal.view.Bluetooth;

import android.app.Activity;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import java.text.DateFormat;
import java.util.Date;

import btcore.co.kr.hatsheal.MainActivity;
import btcore.co.kr.hatsheal.R;
import btcore.co.kr.hatsheal.adapter.DeviceListActivity;
import btcore.co.kr.hatsheal.databinding.ActivityBluetoothBinding;
import btcore.co.kr.hatsheal.service.BluetoothLeService;
import butterknife.OnClick;

/**
 * Created by leehaneul on 2018-04-10.
 */

public class BluetoothActivity extends AppCompatActivity {

    public static final String TAG = "BluetoothActivity";
    private static final int UART_PROFILE_DISCONNECTED = 21;
    private static final int UART_PROFILE_CONNECTED = 20;
    private static final int REQUEST_SELECT_DEVICE = 1;
    private static final int REQUEST_ENABLE_BT = 2;
    private static final int UART_PROFILE_READY = 10;
    private BluetoothAdapter mBtAdapter = null;

    private int mState = UART_PROFILE_DISCONNECTED;
    public BluetoothLeService mService = null;
    public BluetoothDevice mDevice = null;
    private boolean connect_flag = false;
    private boolean select_flag = false;
    private ProgressDialog ConnectionLoading;

    private SharedPreferences pref = null;
    private SharedPreferences.Editor editor;

    ActivityBluetoothBinding bluetoothBinding;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        bluetoothBinding = DataBindingUtil.setContentView(this, R.layout.activity_bluetooth);
        bluetoothBinding.setBluetoothActivity(this);

        pref = getSharedPreferences("pref", Activity.MODE_PRIVATE);
        editor = pref.edit();


        checkBle();

        service_init();

    }
    @OnClick(R.id.btn_autoconnect)
    public void onAutoConnect(View view){
        if (!mBtAdapter.isEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
        } else {
            String address = pref.getString("deviceAddress", "");
            if(mService == null && address.length() > 0){
                mService.connect(address);
            }

        }
    }
    @OnClick(R.id.btn_connect)
    public void onConnect(View view){
        if (!mBtAdapter.isEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
        } else {
            if (connect_flag == false) {
                Intent newIntent = new Intent(BluetoothActivity.this, DeviceListActivity.class);
                startActivityForResult(newIntent, REQUEST_SELECT_DEVICE);
            } else {
                if (mDevice != null) {
                    mService.disconnect();
                }
            }
        }
    }

    private void checkBle(){
        mBtAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBtAdapter == null) {
            Toast.makeText(this, "Bluetooth is not available", Toast.LENGTH_LONG).show();
            finish();
            return;
        }
    }

    public void service_init() {
        Intent bindIntent = new Intent(this, BluetoothLeService.class);
        bindService(bindIntent, mServiceConnection, Context.BIND_AUTO_CREATE);
        LocalBroadcastManager.getInstance(this).registerReceiver(UARTStatusChangeReceiver, makeGattUpdateIntentFilter());
    }
    private final BroadcastReceiver UARTStatusChangeReceiver = new BroadcastReceiver() {

        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (action.equals(BluetoothLeService.ACTION_GATT_CONNECTED)) {
                runOnUiThread(new Runnable() {
                    public void run() {
                        if (ConnectionLoading != null) {
                            ConnectionLoading.dismiss();
                        }
                        connect_flag = true;
                        mState = UART_PROFILE_CONNECTED;

                        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                        intent.putExtra("device","Test");
                        startActivity(intent);
                        finish();

                    }
                });
            }

            if (action.equals(BluetoothLeService.ACTION_GATT_DISCONNECTED)) {
                runOnUiThread(new Runnable() {
                    public void run() {

                        connect_flag = false;
                        android.util.Log.d(TAG, "UART_DISCONNECT_MSG");
                        mState = UART_PROFILE_DISCONNECTED;
                        mService.close();
                    }
                });
            }

            if (action.equals(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED)) {
                mService.enableTXNotification();
            }

            if (action.equals(BluetoothLeService.ACTION_DATA_AVAILABLE)) {

                runOnUiThread(new Runnable() {
                    public void run() {
                        try {
                            String currentDateTimeString = DateFormat.getTimeInstance().format(new Date());
                            Log.d(TAG, "ACTION_DATA_AVAILABLE : " + currentDateTimeString);
                        } catch (Exception e) {
                            android.util.Log.e(TAG, e.toString());
                        }
                    }
                });
            }
            if (action.equals(BluetoothLeService.DEVICE_DOES_NOT_SUPPORT_UART)) {
                String currentDateTimeString = DateFormat.getTimeInstance().format(new Date());
                Log.d(TAG, "DEVICE_DOES_NOT_SUPPORT_UART : " + currentDateTimeString);
                mService.disconnect();
            }
        }
    };

    public void sendCommand(String data) {
        mService.writeRXCharacteristic(data);
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

    @Override
    public void onDestroy() {
        super.onDestroy();

        try {
            LocalBroadcastManager.getInstance(this).unregisterReceiver(UARTStatusChangeReceiver);
        } catch (Exception ignore) {
            android.util.Log.e(TAG, ignore.toString());
        }

        if (mService != null) {
            unbindService(mServiceConnection);
            mService.stopSelf();
            mService = null;
        }


    }

    @Override
    protected void onStop() {
        android.util.Log.d(TAG, "onStop");
        super.onStop();
    }

    @Override
    protected void onPause() {
        android.util.Log.d(TAG, "onPause");
        super.onPause();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        android.util.Log.d(TAG, "onRestart");
    }

    @Override
    public void onResume() {
        super.onResume();

        android.util.Log.d(TAG, "onResume");
        if (!mBtAdapter.isEnabled()) {
            android.util.Log.i(TAG, "onResume - BT not enabled yet");
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
        }

    }
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {

            case REQUEST_SELECT_DEVICE:
                //When the DeviceListActivity return, with the selected device address
                if (resultCode == Activity.RESULT_OK && data != null) {
                    ConnectionLoading = ProgressDialog.show(BluetoothActivity.this, "잠시 기다려주세요", "블루투스 연결중입니다.", true, false);
                    String deviceAddress = data.getStringExtra(BluetoothDevice.EXTRA_DEVICE);
                    mDevice = BluetoothAdapter.getDefaultAdapter().getRemoteDevice(deviceAddress);

                    if (deviceAddress != null) {
                        editor.remove("deviceAddress");
                        editor.putString("deviceAddress", deviceAddress);
                        editor.commit();
                    }


                    Log.d(TAG, "... onActivityResultdevice.address==" + mDevice + "mserviceValue" + mService);
                    //((TextView) findViewById(R.id.tag_name)).setText(mDevice.getName() );
                    try {
                        mService.connect(deviceAddress);

                    } catch (NullPointerException e) {
                        e.printStackTrace();
                        Toast.makeText(this, "해당기기가 없습니다.", Toast.LENGTH_SHORT).show();
                    }


                }
                break;
            case REQUEST_ENABLE_BT:
                // When the request to enable Bluetooth returns
                if (resultCode == Activity.RESULT_OK) {
                    Toast.makeText(this, "블루투스를 활성화 했습니다.", Toast.LENGTH_SHORT).show();

                } else {
                    // User did not enable Bluetooth or an error occurred
                    android.util.Log.d(TAG, "BT not enabled");
                    Toast.makeText(this, "블루투스를 활성화 해주세요", Toast.LENGTH_SHORT).show();
                    finish();
                }
                break;
            default:
                android.util.Log.e(TAG, "wrong request code");
                break;
        }
    }


    @Override
    public void onBackPressed() {

        if (mState == UART_PROFILE_CONNECTED) {
            moveTaskToBack(true);
        } else {
            moveTaskToBack(true);
        }
    }
}
