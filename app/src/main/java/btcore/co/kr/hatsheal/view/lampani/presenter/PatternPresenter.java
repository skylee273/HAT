package btcore.co.kr.hatsheal.view.lampani.presenter;

import btcore.co.kr.hatsheal.view.lampani.Pattern;
import btcore.co.kr.hatsheal.view.lampani.model.PatternModel;

public class PatternPresenter implements Pattern.Presenter {


    Pattern.View messageView;
    PatternModel messageModel;

    public PatternPresenter(Pattern.View messageView) {
        this.messageView = messageView;
        this.messageModel = new PatternModel();
    }

    public void onCreate() {
        messageModel = new PatternModel();
    }

    public void onPause() {
    }

    public void onResume() {
    }

    public void onDestroy() {
    }

    @Override
    public void plusPattern(String red, String green, String blue, String light, String interval) {
        if(messageModel.checkPattern(red, green, blue, light, interval)){
            messageView.updateListView(red, green, blue, light, interval);
        }else{
            messageView.showErrorMessage("모두 입력해주세요");
        }
    }

    @Override
    public void cleanPattern() {
        messageView.clearView();
    }
}
