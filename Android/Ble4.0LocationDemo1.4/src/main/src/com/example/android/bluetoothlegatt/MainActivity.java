package com.example.android.bluetoothlegatt;

import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.app.ActivityGroup;
import android.content.Intent;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.RelativeLayout;
import com.example.android.bluetoothlegatt.iBeaconClass;
import com.example.android.bluetoothlegatt.sensor.Acceleration;
import com.example.android.bluetoothlegatt.sensor.Orientation;

public class MainActivity extends ActivityGroup implements OnClickListener {

	private RelativeLayout act;
	private Timer time;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		DisplayMetrics metrics = getResources().getDisplayMetrics();
		Log.i("tonghu2","w, h: " + metrics.widthPixels + ", " + metrics.heightPixels);
		System.out.println("型号：" + android.os.Build.MODEL);
		
		setContentView(R.layout.activity_main);
		act = (RelativeLayout) findViewById(R.id.act_layout);
		findViewById(R.id.btn_act_f).setOnClickListener(this);
		findViewById(R.id.btn_act_s).setOnClickListener(this);
		findViewById(R.id.btn_act_l).setOnClickListener(this);
		findViewById(R.id.btn_act_e).setOnClickListener(this);
		act.addView(getLocalActivityManager().startActivity(
                "Module1",
                new Intent(MainActivity.this, DeviceScanActivity.class)
                        .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP))
                .getDecorView());
		
		System.loadLibrary("bleRssiRanging");		
		//System.loadLibrary("calcRelativePosition");	
		//get sensor manager
		SensorManager sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
		//du shuju 2 ge
		Acceleration.start(sensorManager);
		com.example.android.bluetoothlegatt.sensor.Orientation.start(sensorManager);
		
		time = new Timer();
		time.scheduleAtFixedRate(
				new TimerTask() {
					
					@Override
					public void run() {
						Log.i("tonghu1", "orientaion: " + Orientation.cur_x + ", " + Orientation.cur_y + ", " + Orientation.cur_z);
						Log.i("tonghu1", "acceleration: " + Acceleration.cur_x + ", " + Acceleration.cur_y + ", " + Acceleration.cur_z);
					}
				}, 0, 50);
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		Acceleration.stop();
		com.example.android.bluetoothlegatt.sensor.Orientation.stop();
		if (time != null) {
			time.cancel();
		}
	}

	@SuppressWarnings("deprecation")
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btn_act_f:
			act.removeAllViews();
			act.addView(getLocalActivityManager().startActivity(
                     "Module1",
                     new Intent(MainActivity.this, DeviceScanActivity.class)
                             .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP))
                     .getDecorView());

			break;
		case R.id.btn_act_s:
			act.removeAllViews();
			act.addView(getLocalActivityManager().startActivity(
                     "Module1",
                     new Intent(MainActivity.this, DeviceScanFActivity.class)
                             .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP))
                     .getDecorView());
			break;
		case R.id.btn_act_l:
//			act.removeAllViews();
//			act.addView(getLocalActivityManager().startActivity(
//                    "Module1",
//                    new Intent(MainActivity.this, TestActivity.class)
//                            .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP))
//                    .getDecorView());
			Intent intent = new Intent(MainActivity.this, TestActivity.class);
			startActivity(intent);
			Log.e("----------------------", ":::::::::::::::::::::::::: ");
			break;
			
		case R.id.btn_act_e:
//			act.removeAllViews();
//			act.addView(getLocalActivityManager().startActivity(
//                    "Module1",
//                    new Intent(MainActivity.this, mpu9250RotateActivity.class)
//                            .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP))
//                    .getDecorView());
			Intent intent1 = new Intent(MainActivity.this, mpu9250RotateActivity.class);
			startActivity(intent1);
			Log.e("----------------------", ":::::::::::::::::::::::::: ");
			break;
			
		default:
			break;
		}
	}

}
