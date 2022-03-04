package com.babelkey.estepais1.player;

import java.util.List;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

public class SentenceViewAdapter extends BaseAdapter {
	private Context context;
	private List<SentenceUI> sentenceList;
	private LayoutInflater inflater;
	
	public SentenceViewAdapter(Context context, List<SentenceUI> sentenceList, LayoutInflater inflater){
		this.sentenceList = sentenceList;
		this.context = context;
		this.inflater = inflater;
	}

	public int getCount() {
		return sentenceList.size();
	}

	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return null;
	}

	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}
	
	public View getView(int position, View convertView, ViewGroup parent) {
    	Log.e(getClass().getName(), "getView() at position: "+position);
		SentenceView sentenceView;
		SentenceUI sentenceUI = (SentenceUI)this.sentenceList.get(position);
		sentenceView = new SentenceView(this.context, sentenceUI, this.inflater, false, false);	
		
		if(sentenceUI.isSelectedSentence()){
			sentenceView.setBackgroundColor(Color.DKGRAY);
		}

		return sentenceView;
		
/*		if(convertView == null){
			sentenceView = new SentenceView(this.context, (SentenceUI)this.sentenceList.get(position), this.inflater);			
		} else {
			sentenceView = (SentenceView) convertView;
		}
*/
	}
	
}
