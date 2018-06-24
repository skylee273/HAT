package btcore.co.kr.hatsheal.view.lampani;

public interface Pattern {

    interface View{
        void showErrorMessage(String msg);
        void updateListView(String red, String green, String blue, String light, String interval);
        void clearView();
    }

    interface Presenter{
        void plusPattern(String red, String green, String blue, String light, String interval);
        void cleanPattern();
    }
}
