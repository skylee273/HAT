package btcore.co.kr.hatsheal.view.lampani.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

import btcore.co.kr.hatsheal.R;
import btcore.co.kr.hatsheal.view.lampani.item.PatternItem;

public class PatternAdapter extends BaseAdapter {

    private TextView mRed, mGreen, mBlue, mLight, mInterval;
    public ArrayList<PatternItem> listViewItemList = new ArrayList<PatternItem>();

    @Override
    public int getCount() {
        return listViewItemList.size();
    }

    @Override
    public Object getItem(int position) {
        return listViewItemList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        int pos = position;
        Context context = parent.getContext();

        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.list_lamp, parent, false);
        }

        mRed = convertView.findViewById(R.id.text_red);
        mGreen = convertView.findViewById(R.id.text_green);
        mBlue = convertView.findViewById(R.id.text_blue);
        mLight = convertView.findViewById(R.id.text_light);
        mInterval = convertView.findViewById(R.id.text_interval);

        PatternItem listViewItem = (PatternItem) getItem(pos);

        mRed.setText(listViewItem.getRed());
        mGreen.setText(listViewItem.getGreen());
        mBlue.setText(listViewItem.getBlue());
        mLight.setText(listViewItem.getLightTime());
        mInterval.setText(listViewItem.getIntervalTime());

        return convertView;
    }

    public void addItem(String red, String green, String blue, String light, String interval){
        PatternItem item = new PatternItem();
        item.setRed(red);
        item.setGreen(green);
        item.setBlue(blue);
        item.setLightTime(light);
        item.setIntervalTime(interval);

        listViewItemList.add(item);
    }

    public void clearItem() {
        listViewItemList.clear();
    }
}