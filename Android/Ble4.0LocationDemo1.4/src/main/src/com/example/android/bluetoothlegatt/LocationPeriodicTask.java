package com.example.android.bluetoothlegatt;

import java.util.Timer;
import java.util.TimerTask;
import static com.example.android.bluetoothlegatt.iBeaconClass.*;

import android.location.Location;
import android.util.Log;

import com.example.android.bluetoothlegatt.sensor.Acceleration;
import com.example.android.bluetoothlegatt.sensor.Orientation;


public class LocationPeriodicTask {
	
	public interface OnTaskFinishCallback {
		void onFinish();
	}
	
	private OnTaskFinishCallback onTaskFinishCallback;
	
	private boolean isStarted = false;
	
	private Timer timer;
	
	public void start(OnTaskFinishCallback onTaskFinishCallback) {
		this.onTaskFinishCallback = onTaskFinishCallback;
		if (!isStarted) {
			isStarted = true;
			timer = new Timer();
			timer.scheduleAtFixedRate(new TimerTask() {
				
				@Override
				public void run() {
					step = Acceleration.Radians;
					if(locationData.step_distance_count != step)
					{
						if(calcRelativePosition(locationData.dev_last_radius, locationData.dev_last_theta, (step - locationData.step_distance_count) * 0.60,  Orientation.get_pose()[0], position_info))
						{
							locationData.new_radius = position_info[0];
							locationData.new_theta = position_info[1];
							position_info[0] = 0.01;
							position_info[1] = 0.0;
							locationData.dev_last_radius = locationData.new_radius;
							locationData.dev_last_theta = locationData.new_theta;
						}
						locationData.step_distance_count = step;			
					}
					
					radius = locationData.dev_last_radius;
					theta = locationData.dev_last_theta;
					Log.i("tonghu3", "radius : " + position_info[0] + "  theta : " + position_info[1]);
					if (LocationPeriodicTask.this.onTaskFinishCallback != null) {
						LocationPeriodicTask.this.onTaskFinishCallback.onFinish();
					}
				}
			}, 0, 100);
		}
	}
	
	public void stop() {
		if (isStarted) {
			if (timer != null) {
				timer.cancel();
				timer = null;
			}
		}
	}
	
}
