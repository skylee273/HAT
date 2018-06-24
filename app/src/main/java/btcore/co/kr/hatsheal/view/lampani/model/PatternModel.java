package btcore.co.kr.hatsheal.view.lampani.model;

public class PatternModel {
    private String Red, Green, Blue, Interval, Light;

    public boolean checkPattern(String red, String green, String blue, String light, String interval){
        this.Red = red;
        this.Green = green;
        this.Blue = blue;
        this.Light = light;
        this.Interval = interval;
        try{
            if(Red.length() > 0 && Green.length() > 0 && Blue.length() > 0 && Interval.length() > 0 && Light.length() > 0){
                return true;
            }else {
                return false;
            }
        }catch (NullPointerException e){
            return false;
        }
    }
}
