package btcore.co.kr.hatsheal.service;

import android.app.Activity;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.util.Log;

import java.util.Calendar;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import btcore.co.kr.hatsheal.bus.BusEvent;
import btcore.co.kr.hatsheal.bus.BusEventPhoneToDevice;
import btcore.co.kr.hatsheal.bus.BusProvider;
import btcore.co.kr.hatsheal.bus.BusProviderPhoneToDevice;
import btcore.co.kr.hatsheal.util.Protocol;

/**
 * Created by leehaneul on 2018-04-20.
 */

public class HatService extends Service {

    private final String TAG = getClass().getSimpleName();
    private SharedPreferences.Editor editor;
    private SharedPreferences pref = null;
    private Timer mTimer;
    Protocol protocol;
    IBinder mBinder = new HatBinder();


    public class HatBinder extends Binder {
        public HatService getService() { // 서비스 객체를 리턴
            return HatService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        // 액티비티에서 bindService() 를 실행하면 호출됨
        // 리턴한 IBinder 객체는 서비스와 클라이언트 사이의 인터페이스 정의한다
        return mBinder; // 서비스 객체를 리턴
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate Start");

        protocol = new Protocol();

        pref = getSharedPreferences("HAT", Activity.MODE_PRIVATE);
        editor = pref.edit();

        if (mTimer == null) {
            MainTimerTask timerTask = new MainTimerTask();
            mTimer = new Timer();
            mTimer.schedule(timerTask, 0, 5000);
        }


    }

    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mTimer != null) {
            mTimer.cancel();
        }

    }

    private Handler mHandler = new Handler();
    private Runnable mUpdateTimeTask = new Runnable() {
        @RequiresApi(api = Build.VERSION_CODES.N)
        public void run() {

            String strHour = String.format("%02d", hour());
            String strMin = String.format("%02d", min());

            boolean alarmState = pref.getBoolean("ALARMSTATE", false);
            boolean lampState = pref.getBoolean("LAMPSTATE", false);

            if(alarmState) { Alarm(strHour, strMin); }
            if(lampState) { LED(strHour, strMin); }

        }
    };

    class MainTimerTask extends TimerTask {
        public void run() {
            mHandler.post(mUpdateTimeTask);
        }
    }

    private void LED(String strHour, String strMin) {
        try {
            String LampTime[] = pref.getString("TIME", "").split("-");
            String RGB[] = pref.getString("RGB", "").split("-");
            if (LampTime.length > 0 && RGB.length > 0) {
                Log.d(TAG, "cHour = " + strHour + " cMin = " + strMin + "   LampTimeStart = " + LampTime[0] + "   LampTimeEnd = " + LampTime[1]);
                if (LampTime[0].equals(strHour + " : " + strMin)) {
                    BusProviderPhoneToDevice.getInstance().post(new BusEventPhoneToDevice(protocol.RGBDataStreaming(Integer.parseInt(RGB[0]),Integer.parseInt(RGB[1]),Integer.parseInt(RGB[2])), 1));
                }
                if (LampTime[1].equals(strHour + " : " + strMin)) {
                    BusProviderPhoneToDevice.getInstance().post(new BusEventPhoneToDevice(protocol.RGBOffControl(), 1));
                }
            }
        } catch (Exception e) {
            Log.d(TAG, e.toString());
        }
    }

    private void Alarm(String strHour, String strMin) {
        try {
            String alarm[] = pref.getString("ALARMTIME", "").split("-");
            if ( alarm.length > 0) {
                Log.d(TAG, " cHour = " + strHour + " cMin = " + strMin + "   UvHour = " + alarm[0] + "   UvMin = " + alarm[1]);
                if (alarm[0].contains(strHour) && alarm[1].contains(strMin)) {
                    BusProviderPhoneToDevice.getInstance().post(new BusEventPhoneToDevice(protocol.uvControl(), 0));
                }
            }
        } catch (Exception e) {
            Log.d(TAG, e.toString());
        }
    }

    private int hour() {
        Calendar cal = Calendar.getInstance();

        int year = cal.get(cal.HOUR_OF_DAY);
        return year;
    }

    private int min() {
        Calendar cal = Calendar.getInstance();

        int year = cal.get(cal.MINUTE);
        return year;
    }
}
