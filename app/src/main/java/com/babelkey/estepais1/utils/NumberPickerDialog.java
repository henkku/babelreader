package com.babelkey.estepais1.utils;

import com.babelkey.estepais1.player.R;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.view.LayoutInflater;
import android.view.View;

public class NumberPickerDialog extends AlertDialog implements OnClickListener {
    private OnNumberSetListener mListener;
    private NumberPicker mNumberPicker;
    
    private int mInitialValue;
    
    public NumberPickerDialog(Context context, int theme, int initialValue) {
        super(context, theme);
        mInitialValue = initialValue;

        setButton(BUTTON_POSITIVE, context.getString(R.string.dialog_set_number), this);
        setButton(BUTTON_NEGATIVE, context.getString(R.string.dialog_cancel), (OnClickListener) null);

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.number_picker_pref, null);
        setView(view);

        mNumberPicker = (NumberPicker) view.findViewById(R.id.pref_num_picker);
        mNumberPicker.setCurrent(mInitialValue);
    }

    public void setOnNumberSetListener(OnNumberSetListener listener) {
        mListener = listener;
    }

    public void onClick(DialogInterface dialog, int which) {
        if (mListener != null) {
            mListener.onNumberSet(mNumberPicker.getCurrent());
        }
    }

    public interface OnNumberSetListener {
        public void onNumberSet(int selectedNumber);
    }
}
