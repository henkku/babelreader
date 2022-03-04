package com.babelkey.estepais1.player;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.preference.PreferenceManager;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class SentenceView extends LinearLayout{
	public SentenceView(Context context, SentenceUI sentence, LayoutInflater inflater, boolean hideOriginal, boolean hideTranslation){
		super(context);
	   	Log.e(getClass().getName(), "entering SentenceView with sentenceID: "+sentence.getSentenceID());
		LinearLayout.LayoutParams linearLayoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
		this.setPadding(2, 5, 1, 5);  //left, top, right, bottom
		this.setOrientation(VERTICAL);
		this.setEnabled(true);
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        String alignmentSetting = sharedPrefs.getString("layout_alignment", "LEFT_ALIGN");
        BabelApplication app = ((BabelApplication)context.getApplicationContext());
        float fontSizeSetting = new Float(sharedPrefs.getInt("fontSize", app.getTextSizeDefault()));
        
    	String[] tempOriginalWords = sentence.getOriginalWords();
    	String[] tempTranslatedWords = sentence.getTranslatedWords();
    	for(int i=0;i<tempOriginalWords.length;i++){
    		if(alignmentSetting.equals("CENTER_RELATIVE")){
        		RelativeLayout relativeLayout = (RelativeLayout) inflater.inflate(R.layout.center_relative_row, null, true);
        		TextView originalWordTV = (TextView)relativeLayout.findViewById(R.id.originalword);
                originalWordTV.setText(tempOriginalWords[i]);    
                originalWordTV.setTextSize(fontSizeSetting);
                originalWordTV.setTextColor((hideOriginal) ? Color.parseColor("#000000") : Color.parseColor("#00ccff"));
                TextView translatedWordTV = (TextView)relativeLayout.findViewById(R.id.translatedword);
                translatedWordTV.setText(tempTranslatedWords[i]); 
                translatedWordTV.setTextSize(fontSizeSetting);
                translatedWordTV.setTextColor((hideTranslation) ? Color.parseColor("#000000") : Color.parseColor("#cccccc"));
                this.addView(relativeLayout, linearLayoutParams);    			
    		}
    		else if(alignmentSetting.equals("LEFT_ALIGN")) {
    			String color_original = (hideOriginal) ? "color='#000000'" : "color='#00ccff'";
    			String color_translation = (hideTranslation) ? "color='#000000'" : "color='#bbbbbb'";
    			TextView sentenceRow = new TextView(context);
    			sentenceRow.setText(Html.fromHtml("<b><font "+color_original+">" + tempOriginalWords[i] + "</font></b>" + "  " + "<font "+color_translation+">" + tempTranslatedWords[i] + "</font>"));   			
    			sentenceRow.setTextSize(fontSizeSetting);
    			this.addView(sentenceRow);
    		}
    	}
    	this.setId(sentence.getSentenceID());
	   	Log.e(getClass().getName(), "exiting SentenceView ");
    	
	}

}
