package com.babelkey.estepais1.player;

import com.babelkey.estepais1.service.BabelPlayerService;

import android.app.Application;
import android.content.Intent;
import android.content.res.Configuration;
import android.util.Log;

public class BabelApplication extends Application {
	private boolean isLargeScreen = false;
	private int textSizeDefault = 22;
	private int textSizeMax = 40;

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		Log.e(getClass().getName(), "BabelApplication onConfigurationChanged");
		super.onConfigurationChanged(newConfig);
	}

	@Override
	public void onCreate() {
		Log.e(getClass().getName(), "BabelApplication onCreate");

		// TODO Auto-generated method stub
		super.onCreate();
	}

	@Override
	public void onTerminate() {
		Log.e(getClass().getName(), "BabelApplication onTerminate");
		BabelPlayerService.getInstance().onDestroy();
		new Thread(new Runnable() {
			public void run() {
				stopService(new Intent("PLAY"));
			}
		}).start();
		
		super.onTerminate();
	}

	public boolean isLargeScreen() {
		return isLargeScreen;
	}

	public void setLargeScreen(boolean isLargeScreen) {
		this.isLargeScreen = isLargeScreen;
	}

	public int getTextSizeDefault() {
		return textSizeDefault;
	}

	public void setTextSizeDefault(int textSizeDefault) {
		this.textSizeDefault = textSizeDefault;
	}

	public int getTextSizeMax() {
		return textSizeMax;
	}

	public void setTextSizeMax(int textSizeMax) {
		this.textSizeMax = textSizeMax;
	}


}
