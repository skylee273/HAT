package btcore.co.kr.hatsheal.view.Bluetooth;

import android.bluetooth.BluetoothAdapter;

/**
 * Created by leehaneul on 2018-04-15.
 */

public interface Ble {

    interface  View{
        void showErrorMessage(String msg);
        void showSuccesMessage(String msg);
    }

    interface  Presenter{

    }

}
