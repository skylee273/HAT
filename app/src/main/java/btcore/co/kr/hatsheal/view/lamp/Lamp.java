package btcore.co.kr.hatsheal.view.lamp;

/**
 * Created by leehaneul on 2018-04-19.
 */

public interface Lamp {

    interface View{
        void NextActivity();
        void showErrorMessage(String msg);

    }

    interface Presenter{
        void setLamp(String r, String g, String b, String sTime, String eTime, boolean state);
    }


}
