package btcore.co.kr.hatsheal.view.setting;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import btcore.co.kr.hatsheal.MainActivity;
import btcore.co.kr.hatsheal.R;
import btcore.co.kr.hatsheal.bus.BusProvider;
import btcore.co.kr.hatsheal.bus.BusProviderPhoneToDevice;
import btcore.co.kr.hatsheal.databinding.ActivitySettingBinding;
import btcore.co.kr.hatsheal.view.Bluetooth.BluetoothActivity;
import btcore.co.kr.hatsheal.view.dfu.DfuActivity;
import butterknife.OnClick;

/**
 * Created by leehaneul on 2018-04-21.
 */

public class SettingActivity extends AppCompatActivity {

    ActivitySettingBinding settingBinding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        settingBinding = DataBindingUtil.setContentView(this, R.layout.activity_setting);
        settingBinding.setSettingActivity(this);

    }

    @OnClick(R.id.btn_ble)
    public void onBle(View view){
        Intent intent = new Intent(getApplicationContext(), BluetoothActivity.class);
        startActivity(intent);
        finish();
    }
    @OnClick(R.id.btn_firmware)
    public void onFirmware(View view){
        Intent intent = new Intent(getApplicationContext(), DfuActivity.class);
        startActivity(intent);
        finish();
    }
    @OnClick(R.id.btn_question)
    public void onQuestion(View view){

    }
    @OnClick(R.id.btn_service)
    public void onService(View view){

    }
    @OnClick(R.id.btn_back)
    public void onBack(View view){
        startActivity(new Intent(getApplicationContext(), MainActivity.class));
        overridePendingTransition(R.anim.pull_in_right, R.anim.push_out_left);
        finish();
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        startActivity(intent);
        finish();
    }
}
