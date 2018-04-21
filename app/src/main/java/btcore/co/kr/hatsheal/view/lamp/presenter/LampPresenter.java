package btcore.co.kr.hatsheal.view.lamp.presenter;

import btcore.co.kr.hatsheal.view.lamp.Lamp;
import btcore.co.kr.hatsheal.view.lamp.model.LampModel;

/**
 * Created by leehaneul on 2018-04-19.
 */

public class LampPresenter implements Lamp.Presenter {

    Lamp.View lampView;
    LampModel lampModel;

    public LampPresenter(Lamp.View lampView){
        this.lampView = lampView;
        this.lampModel = new LampModel();
    }

    @Override
    public void setLamp(String r, String g, String b, String sTime, String eTime, boolean state) {
        lampModel.setkLamp(r, g, b, sTime, eTime, state);
        if(lampModel.checkLamp() == 0) lampView.showErrorMessage("시간을 설정하세요.");
        else lampView.NextActivity();
    }
}
