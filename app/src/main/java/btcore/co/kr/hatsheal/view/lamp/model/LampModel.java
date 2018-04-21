package btcore.co.kr.hatsheal.view.lamp.model;

/**
 * Created by leehaneul on 2018-04-19.
 */

public class LampModel {

    private String Red, Green, Blue, startTime, endTime;
    private boolean state = false;

    public void setkLamp(String r, String g, String b, String sTime, String eTime, boolean s){
        this.Red = r;
        this.Green = g;
        this.Blue = b;
        this.startTime = sTime;
        this.endTime = eTime;
        this.state = s;
    }

    public int checkLamp(){
        if (state == false) return 2;
        else if(startTime.equals("00 : 00") && endTime.equals("00 : 00")) return 0;
        else return 2;
    }
}
