package com.example.android.bluetoothlegatt;

import android.app.Application;
import android.util.DisplayMetrics;
import android.media.AudioManager;
import android.media.SoundPool;
import android.util.Log;
import com.example.android.blelocation.R;

/**
 * 整个程序的代表类
 * @author Administrator
 *
 */
public class App extends Application{
	public static double W_PX_P_M = -1;			//宽度  像素点/物理长度
	public static double H_PX_P_M = -1;			//高度  像素点/物理长度
	
	public static SoundPool soundPool = null;
	
	public static int soundId = -1;
	private static App app;
	
	/**
	 * 整个程序的入口
	 */
	@Override
	public void onCreate() {
		super.onCreate();
		DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
		W_PX_P_M = displayMetrics.widthPixels / 19.0;
		//H_PX_P_M = displayMetrics.heightPixels / 8.0;
		H_PX_P_M = displayMetrics.widthPixels / 19.0;
		
		soundPool = new SoundPool(10, AudioManager.STREAM_SYSTEM, 5);
		soundId = soundPool.load(this, R.raw.aa, 1);
		app = this;
		Log.i("kkkk", "soundId: " + soundId);
		
	}
}
