package com.babelkey.estepais1.player;

import android.content.Context;
import android.view.MotionEvent;
import android.widget.ListView;

public class CustomListView extends ListView {
	
	private Boolean isScrollEnabled = false;
	int lastAction = 0;

	public CustomListView(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}
	
    @Override
    public boolean dispatchTouchEvent(MotionEvent ev)
    {
            int action = ev.getAction();

            if (!isScrollEnabled) {
                    if (action == lastAction) {
                            ev.setAction(MotionEvent.ACTION_CANCEL);
                            super.dispatchTouchEvent(ev);
                            return true;
                    }
                    lastAction = action;
            }

            return super.dispatchTouchEvent(ev);
    }

	public Boolean getIsScrollEnabled() {
		return isScrollEnabled;
	}

	public void setIsScrollEnabled(Boolean isScrollEnabled) {
		this.isScrollEnabled = isScrollEnabled;
	}

	public int getLastAction() {
		return lastAction;
	}

	public void setLastAction(int lastAction) {
		this.lastAction = lastAction;
	}	
    
    

}
