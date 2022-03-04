package com.babelkey.estepais1.player;

import android.os.Bundle;
import android.preference.PreferenceActivity;

public class BabelPreferenceActivity extends PreferenceActivity {

	 @Override
	     public void onCreate(Bundle savedInstanceState) {  
	         super.onCreate(savedInstanceState);  
	         addPreferencesFromResource(R.xml.settingsmenu);
	     }

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		finish();
	}

	
}
