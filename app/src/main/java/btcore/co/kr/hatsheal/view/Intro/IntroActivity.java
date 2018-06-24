package btcore.co.kr.hatsheal.view.Intro;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;

import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.TedPermission;

import java.util.ArrayList;

import btcore.co.kr.hatsheal.MainActivity;
import btcore.co.kr.hatsheal.R;
import btcore.co.kr.hatsheal.view.Bluetooth.BluetoothActivity;

/**
 * Created by leehaneul on 2018-04-10.
 */

public class IntroActivity extends AppCompatActivity {

    private static int SPLASH_TIME_OUT = 3000;
    Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_intro);

        mContext = this;
        
        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                // 테드 퍼미션 라이브러리 상용
                TedPermission.with(mContext)
                        .setPermissionListener(permissionlistener)
                        .setDeniedMessage("만약 서비스를 허용하지 않으시면 앱 이용시에 제한이 있습니다.\n\n 권한을 설정 해주세요 [설정] > [권한]")
                        .setPermissions(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.RECORD_AUDIO, Manifest.permission.CALL_PHONE,
                                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        .check();
            }
        }, SPLASH_TIME_OUT);
    }

    PermissionListener permissionlistener = new PermissionListener() {
        @Override
        public void onPermissionGranted() {

            // MainActivity.class 자리에 다음에 넘어갈 액티비티를 넣어주기
            Intent intent = new Intent(mContext, BluetoothActivity.class);
            startActivity(intent);
            finish();
        }

        @Override
        public void onPermissionDenied(ArrayList<String> deniedPermissions) {
            finish();
        }
    };

}
