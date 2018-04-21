package btcore.co.kr.hatsheal.util;

import android.app.Dialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.text.format.DateFormat;
import android.widget.TextView;

import java.util.Calendar;

import btcore.co.kr.hatsheal.R;

/**
 * Created by leehaneul on 2018-04-19.
 */

public class TimePickerFragment extends DialogFragment implements TimePickerDialog.OnTimeSetListener {

    int state;
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState){
        Calendar mCalendar = Calendar.getInstance();
        int hour = mCalendar.get(Calendar.HOUR_OF_DAY);
        int min = mCalendar.get(Calendar.MINUTE);

        TimePickerDialog mTimePickerDialog = new TimePickerDialog(getActivity(), this, hour, min, DateFormat.is24HourFormat(getActivity()));
        return mTimePickerDialog;
    }
    @Override
    public void onTimeSet(android.widget.TimePicker view, int hourOfDay, int minute) {
        if(state == 0){
            TextView text_start = getActivity().findViewById(R.id.text_start);
            text_start.setText(String.format("%02d",hourOfDay)+ " : " + String.format("%02d",minute));
        }else{
            TextView text_end = getActivity().findViewById(R.id.text_end);
            text_end.setText(String.format("%02d",hourOfDay)+ " : " + String.format("%02d",minute));
        }

    }

    public void onState(int s){
        this.state = s;
    }

}
