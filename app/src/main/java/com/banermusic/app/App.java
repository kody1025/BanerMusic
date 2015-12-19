package com.banermusic.app;

import android.app.Application;
import android.content.Context;

/**
 * Application
 */
public class App extends Application {

	private static Context mContext; // 应用全局context

	@Override
	public void onCreate() {
		super.onCreate();
		mContext = this.getApplicationContext();
	}

	public static Context getContext(){
		return mContext;
	}
}
