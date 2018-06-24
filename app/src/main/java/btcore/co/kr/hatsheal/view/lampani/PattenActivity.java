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
import android.os.Bundle;
import android.os.IBinder;
import android.provider.ContactsContract;
import android.support.design.widget.Snackbar;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import btcore.co.kr.hatsheal.MainActivity;
import btcore.co.kr.hatsheal.R;
import btcore.co.kr.hatsheal.bus.BusEventPhoneToDevice;
import btcore.co.kr.hatsheal.bus.BusProviderPhoneToDevice;
import btcore.co.kr.hatsheal.databinding.ActivityLampaniBinding;
import btcore.co.kr.hatsheal.databinding.ActivityPatternBinding;
import btcore.co.kr.hatsheal.service.BluetoothLeService;
import btcore.co.kr.hatsheal.service.HatService;
import btcore.co.kr.hatsheal.util.Protocol;
import btcore.co.kr.hatsheal.view.Bluetooth.BluetoothActivity;
import btcore.co.kr.hatsheal.view.lampani.adapter.PatternAdapter;
import btcore.co.kr.hatsheal.view.lampani.item.PatternItem;
import btcore.co.kr.hatsheal.view.lampani.presenter.PatternPresenter;
import butterknife.OnClick;

import static btcore.co.kr.hatsheal.service.BluetoothLeService.STATE;

public class PattenActivity extends AppCompatActivity implements  Pattern.View{

    private final String TAG = getClass().getSimpleName();
    private BluetoothLeService mService = null;
    private static final int UART_PROFILE_DISCONNECTED = 21;
    private static final int UART_PROFILE_CONNECTED = 20;
    private static final int REQUEST_SELECT_DEVICE = 1;
    private static final int REQUEST_ENABLE_BT = 2;
    private static final int UART_PROFILE_READY = 10;
    private int mState = UART_PROFILE_DISCONNECTED;
    private PatternAdapter patternAdapter;
    private String Red, Green, Blue, Interval, Light;
    private boolean startFlag = true;
    private Timer autoTimer;
    private TimerTask autoTask;
    private int IntervalTime = 1;
    int position = 0;
    int totalCount;
    Protocol protocol;
    Pattern.Presenter presenter;
    ActivityPatternBinding activityPatternBinding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        activityPatternBinding = DataBindingUtil.setContentView(this, R.layout.activity_pattern);
        activityPatternBinding.setPattenActivity(this);

        BusProviderPhoneToDevice.getInstance().register(this);

        service_init();

        protocol = new Protocol();
        patternAdapter = new PatternAdapter();
        activityPatternBinding.listPattern.setAdapter(patternAdapter);
        presenter = new PatternPresenter(this);


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

    @OnClick(R.id.text_plus)
    public void onPlus(View view) {
        if (patternAdapter.getCount() < 15) {
            getPattern();
            if(checkPattern(Red, Green, Blue, Light, Interval)){
                patternAdapter.addItem(Red, Green, Blue, Light, Interval);
                patternAdapter.notifyDataSetChanged();
            }else{
                Toast.makeText(getApplicationContext(), "모두 입력해주세요.", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(getApplicationContext(), "최대 15개 패턴을 추가 할 수 있습니다.", Toast.LENGTH_SHORT).show();
        }
        presenter.cleanPattern();
    }
    @OnClick(R.id.btn_start)
    public void onStart(View view) {
        if( startFlag && patternAdapter.getCount() > 0 ){
            activityPatternBinding.btnStart.setText("중지");
            startFlag = false;
            totalCount  = patternAdapter.getCount();
            aniTask(IntervalTime);
        }else{
            if (autoTimer != null ) { autoTimer.cancel();}
            patternAdapter.clearItem();
            patternAdapter.notifyDataSetChanged();
            activityPatternBinding.listPattern.setAdapter(patternAdapter);
            startFlag = true;
            IntervalTime = 1;
            activityPatternBinding.btnStart.setText("실행");
        }
    }

    private void aniTask(int intervalTime){
        autoTimer = new Timer();
        autoTask = new TimerTask() {
            @Override
            public void run() {
                if (STATE) {
                    if(position == totalCount) { position = 0; }
                    PatternItem patternItem = (PatternItem)patternAdapter.getItem(position);
                    send(protocol.RGBDataStreaming(Integer.parseInt(patternItem.getRed()), Integer.parseInt(patternItem.getGreen()), Integer.parseInt(patternItem.getBlue())));
                    IntervalTime = ( Integer.parseInt(patternItem.getLightTime()) );
                    Log.d(TAG, "RED : " + patternItem.getRed() + " GREEN : " + patternItem.getGreen() + " BLUE  : " + patternItem.getBlue() + " INTERVAL : " + String.valueOf(IntervalTime)
                            + " Position : " + String.valueOf(position) + " Total Count : " + String.valueOf(totalCount));
                    position++;
                    if(autoTimer != null) { autoTimer.cancel();
                    aniTask(intervalTime);
                    }
                }
            }
        };
        autoTimer.schedule(autoTask, IntervalTime * 1000);
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

    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        BusProviderPhoneToDevice.getInstance().unregister(this);
        if(autoTimer != null) { autoTimer.cancel();}
        try {
            LocalBroadcastManager.getInstance(this).unregisterReceiver(UARTStatusChangeReceiver);
        } catch (Exception ignore) {
            android.util.Log.e(TAG, ignore.toString());
        }

        if (mService != null) {
            unbindService(mServiceConnection);
        }
    }
    @Override
    public void onBackPressed() {
        Intent intent = new Intent(getApplicationContext(), LampAniActivity.class);
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
            if (action.equals(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED)) {
                mService.enableTXNotification();
            }
            if (action.equals(BluetoothLeService.DEVICE_DOES_NOT_SUPPORT_UART)) {
                mService.disconnect();
            }
        }
    };


    @Override
    public void showErrorMessage(String msg) {
        Snackbar.make(getWindow().getDecorView().getRootView(),msg , Snackbar.LENGTH_LONG).show();
    }

    @Override
    public void updateListView(String red, String green, String blue, String light, String interval) {
        patternAdapter.addItem(red, green, blue, light, interval);
        activityPatternBinding.listPattern.setAdapter(patternAdapter);
    }

    @Override
    public void clearView() {
        activityPatternBinding.editBlue.setText(null);
        activityPatternBinding.editRed.setText(null);
        activityPatternBinding.editGreen.setText(null);
        activityPatternBinding.editInterval.setText(null);
        activityPatternBinding.editLight.setText(null);
    }

    private void getPattern(){
        this.Red = activityPatternBinding.editRed.getText().toString();
        this.Green = activityPatternBinding.editGreen.getText().toString();
        this.Blue = activityPatternBinding.editBlue.getText().toString();
        this.Interval = activityPatternBinding.editInterval.getText().toString();
        this.Light = activityPatternBinding.editLight.getText().toString();
    }
    public boolean checkPattern(String red, String green, String blue, String light, String interval){
        try{
            if(red.length() > 0 && green.length() > 0 && blue.length() > 0 && interval.length() > 0 && light.length() > 0){
                return true;
            }else {
                return false;
            }
        }catch (NullPointerException e){
            return false;
        }
    }


}