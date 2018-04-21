package btcore.co.kr.hatsheal.view.lamp;

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
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.support.design.widget.Snackbar;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import com.christophesmet.android.views.colorpicker.ColorPickerView;
import com.squareup.otto.Subscribe;

import btcore.co.kr.hatsheal.MainActivity;
import btcore.co.kr.hatsheal.R;
import btcore.co.kr.hatsheal.bus.BusEvent;
import btcore.co.kr.hatsheal.bus.BusEventPhoneToDevice;
import btcore.co.kr.hatsheal.bus.BusProvider;
import btcore.co.kr.hatsheal.bus.BusProviderPhoneToDevice;
import btcore.co.kr.hatsheal.databinding.ActivityLampBinding;
import btcore.co.kr.hatsheal.service.BluetoothLeService;
import btcore.co.kr.hatsheal.service.HatService;
import btcore.co.kr.hatsheal.util.TimePickerFragment;
import btcore.co.kr.hatsheal.view.Bluetooth.BluetoothActivity;
import btcore.co.kr.hatsheal.view.lamp.presenter.LampPresenter;
import butterknife.OnClick;

/**
 * Created by leehaneul on 2018-04-19.
 */

public class LampActivity extends AppCompatActivity implements ColorPickerView.ColorListener, Lamp.View {

    private final String TAG = getClass().getSimpleName();
    private String Red = null, Green = null, Blue = null;
    private boolean lampState;
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

    Lamp.Presenter presenter;
    ActivityLampBinding lampBinding;

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

        lampBinding = DataBindingUtil.setContentView(this, R.layout.activity_lamp);
        lampBinding.setLampActivity(this);

        lampBinding.colorpicker.setColorListener(this);
        presenter = new LampPresenter(this);

        pref = getSharedPreferences("pref", Activity.MODE_PRIVATE);
        editor = pref.edit();

        BusProvider.getInstance().register(this);
        BusProviderPhoneToDevice.getInstance().register(this);

        saveView();

        service_init();
    }

    private void saveView(){

        try{
            String Time[] = pref.getString("TIME","").split("-");
            boolean state = pref.getBoolean("LAMPSTATE", false);
            if(Time != null && !Time[0].equals("") && !Time[1].equals("")) { lampBinding.textStart.setText(Time[0]); lampBinding.textEnd.setText(Time[1]); }
            if(state) { lampBinding.imageOn.setImageResource(R.drawable.icon_circle);   lampBinding.imageOff.setImageResource(0); lampState = true; }
            else { lampBinding.imageOn.setImageResource(0);   lampBinding.imageOff.setImageResource(R.drawable.icon_circle); }
        }catch (NullPointerException e ){
            Log.d(TAG,e.toString());
        }catch (NumberFormatException e ){
            Log.d(TAG,e.toString());
        }catch (ArrayIndexOutOfBoundsException e){
            Log.d(TAG, e.toString());
        }

    }

    @OnClick(R.id.btn_back)
    public void onLampBack(View view) {
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        startActivity(intent);
        finish();
    }

    @OnClick(R.id.btn_save)
    public void onLampSave(View view) {
        presenter.setLamp(Red, Green, Blue, lampBinding.textStart.getText().toString(), lampBinding.textEnd.getText().toString(), lampState);
    }

    @OnClick(R.id.text_start)
    public void onStartTime(View view) {
        TimePickerFragment timePickerFragment = new TimePickerFragment();
        timePickerFragment.onState(0);
        timePickerFragment.show(getSupportFragmentManager(), "TIME_TAG");
    }

    @OnClick(R.id.text_end)
    public void onEndTime(View view) {
        TimePickerFragment timePickerFragment = new TimePickerFragment();
        timePickerFragment.onState(1);
        timePickerFragment.show(getSupportFragmentManager(), "TIME_TAG");
    }

    @OnClick(R.id.image_on)
    public void onTimeOn(View view) {
        lampBinding.imageOn.setImageResource(R.drawable.icon_circle);
        lampBinding.imageOff.setImageResource(0);
        lampState = true;
    }

    @OnClick(R.id.image_off)
    public void onTimeOff(View view) {
        lampBinding.imageOff.setImageResource(R.drawable.icon_circle);
        lampBinding.imageOn.setImageResource(0);
        lampState = false;
    }

    @Override
    public void onColorSelected(int i) {
        Red = String.valueOf(Color.red(i));
        Green = String.valueOf(Color.green(i));
        Blue = String.valueOf(Color.blue(i));
    }

    @Override
    public void NextActivity() {

        editor.putString("RGB", Red + "-" + Green + "-" + Blue);
        editor.putString("TIME", lampBinding.textStart.getText().toString() + "-" + lampBinding.textEnd.getText().toString());
        editor.putBoolean("LAMPSTATE",lampState);
        editor.commit();

        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    public void showErrorMessage(String msg) {
        Snackbar.make(getWindow().getDecorView().getRootView(), msg, Snackbar.LENGTH_SHORT).show();
    }
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
                lampBinding.btnBattery1.setText("5%");
                lampBinding.btnBattery1.setBackgroundResource(R.color.colorBattery);
                lampBinding.btnBattery2.setBackgroundResource(0);
                lampBinding.btnBattery3.setBackgroundResource(0);
                lampBinding.btnBattery4.setBackgroundResource(0);
                lampBinding.btnBattery5.setBackgroundResource(0);
                break;
            case 1:
                lampBinding.btnBattery1.setText("25%");
                lampBinding.btnBattery1.setBackgroundResource(R.color.colorBattery);
                lampBinding.btnBattery2.setBackgroundResource(R.color.colorBattery);
                lampBinding.btnBattery3.setBackgroundResource(0);
                lampBinding.btnBattery4.setBackgroundResource(0);
                lampBinding.btnBattery5.setBackgroundResource(0);
                break;
            case 2:
                lampBinding.btnBattery1.setText("50%");
                lampBinding.btnBattery1.setBackgroundResource(R.color.colorBattery);
                lampBinding.btnBattery2.setBackgroundResource(R.color.colorBattery);
                lampBinding.btnBattery3.setBackgroundResource(R.color.colorBattery);
                lampBinding.btnBattery4.setBackgroundResource(0);
                lampBinding.btnBattery5.setBackgroundResource(0);
                break;
            case 3:
                lampBinding.btnBattery1.setText("75%");
                lampBinding.btnBattery1.setBackgroundResource(R.color.colorBattery);
                lampBinding.btnBattery2.setBackgroundResource(R.color.colorBattery);
                lampBinding.btnBattery3.setBackgroundResource(R.color.colorBattery);
                lampBinding.btnBattery4.setBackgroundResource(R.color.colorBattery);
                lampBinding.btnBattery5.setBackgroundResource(0);
                break;
            case 4:
                lampBinding.btnBattery1.setText("100%");
                lampBinding.btnBattery1.setBackgroundResource(R.color.colorBattery);
                lampBinding.btnBattery2.setBackgroundResource(R.color.colorBattery);
                lampBinding.btnBattery3.setBackgroundResource(R.color.colorBattery);
                lampBinding.btnBattery4.setBackgroundResource(R.color.colorBattery);
                lampBinding.btnBattery5.setBackgroundResource(R.color.colorBattery);
                break;

        }
    }

    @Override
    public void onResume(){
        super.onResume();
        if(isService == false){
            Intent intent = new Intent(
                    LampActivity.this, // 현재 화면
                    HatService.class); // 다음넘어갈 컴퍼넌트
            bindService(intent, // intent 객체
                    connection, // 서비스와 연결에 대한 정의
                    Context.BIND_AUTO_CREATE);
        }

    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        BusProvider.getInstance().unregister(this);
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
