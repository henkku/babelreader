package com.babelkey.estepais1.utils;

import android.content.Context;
import android.content.DialogInterface;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.view.View;

import com.babelkey.estepais1.player.BabelApplication;
import com.babelkey.estepais1.player.R;

public class NumberPickerPreference extends DialogPreference {
    private NumberPicker mPicker;
    private int mStartRange=0;
    private int mEndRange=50;
    private int mDefault=0;
    
    public NumberPickerPreference(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        
        if (attrs == null) {
            return;
        }
        
/*        TypedArray arr = context.obtainStyledAttributes(attrs, R.styleable.numberpicker);
        mStartRange = arr.getInteger(R.styleable.numberpicker_startRange, 0);
        mEndRange = arr.getInteger(R.styleable.numberpicker_endRange, 200);
        mDefault = arr.getInteger(R.styleable.numberpicker_defaultValue, 0);
        
        arr.recycle();
*/
        BabelApplication app = ((BabelApplication)context.getApplicationContext());
        mStartRange = 0;
        mEndRange = app.getTextSizeMax();
        mDefault = app.getTextSizeDefault();
        setDialogLayoutResource(R.layout.number_picker_pref);
    }
    
    public NumberPickerPreference(Context context, AttributeSet attrs) {
        this(context, attrs, android.R.attr.dialogPreferenceStyle);
    }
    
    public NumberPickerPreference(Context context) {
        this(context, null);
    }
    
    @Override
    protected void onBindDialogView(View view) {
        super.onBindDialogView(view);
        mPicker = (NumberPicker) view.findViewById(R.id.pref_num_picker);
        mPicker.setRange(mStartRange, mEndRange);
        mPicker.setCurrent(getValue());
    }
    
    public void onClick(DialogInterface dialog, int which) {
        switch (which) {
        case DialogInterface.BUTTON_POSITIVE:
            saveValue(mPicker.getCurrent());
            break;
        default:
            break;
        }
    }
    
    public void setRange(int start, int end) {
        mPicker.setRange(start, end);
    }
    
    private void saveValue(int val) {
        getEditor().putInt(getKey(), val).commit();
        notifyChanged();
    }
    
    private int getValue() {
        return getSharedPreferences().getInt(getKey(), mDefault);
    }
}
