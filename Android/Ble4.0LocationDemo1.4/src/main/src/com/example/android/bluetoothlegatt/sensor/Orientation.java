package com.example.android.bluetoothlegatt.sensor;

import java.util.ArrayList;
import java.util.List;

import android.util.Log;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

public class Orientation{
	public static float cur_x = (float)0.0;
	public static float cur_y = (float)0.0;;
	public static float cur_z = (float)0.0;;

    public static double radius;
    public static double theta;
    private static int countConfigGyroscrope = 0;
	
    private static boolean isRegisteredSensor = false;
    private static SensorManager sensorManager = null;
    
    public static interface SensorListener {
    	void onChange(float x, float y, float z);
    }
    
    public static List<SensorListener> listeners = new ArrayList<SensorListener>();
    
    public void setListeners(List<SensorListener> listeners) {
		this.listeners = listeners;
	}
	
    public static void start(SensorManager sm) {
		sensorManager = sm;
		
		List<Sensor> sensors = sensorManager.getSensorList(Sensor.TYPE_ORIENTATION);
		
		if (sensors != null && sensors.size() > 0) 
		{
			Sensor sensor = sensors.get(0);
//			isRegisteredSensor = sensorManager.registerListener(myListener, sensor,SensorManager.SENSOR_DELAY_GAME);
			isRegisteredSensor = sensorManager.registerListener(myListener, sensor, 250000);
			 
		}
    }

	public static void stop() {
		if (isRegisteredSensor) {
			sensorManager.unregisterListener(myListener);
			isRegisteredSensor = false;
		}
    }
	
	public static float [] get_pose() {
		float pose[] = new float[3];
		pose[0] = cur_x;
		pose[1] = cur_y;
		pose[2] = cur_z;

		return pose;
    }
	
	final private static SensorEventListener myListener = new SensorEventListener() { 
	
	    @Override
	    public void onAccuracyChanged(Sensor sensor, int accuracy) {
//			Log.e("Orientation", "-----------------------accuracy=" + accuracy);

	    }
		///*
	    @Override
	    public void onSensorChanged(SensorEvent event) {
			synchronized (this) 
			{
				cur_x = event.values[SensorManager.DATA_X];;
				cur_y = event.values[SensorManager.DATA_Y];
				cur_z = event.values[SensorManager.DATA_Z];
				//Log.e("Orientation", "-----------------------x=" + cur_x + ", y=" + cur_y+",z="+cur_z);
				//Gyroscope.config(Orientation.cur_x,Orientation.cur_y,Orientation.cur_z);
//				Log.i("tonghu", "orientaion: " + cur_x + ", " + cur_y + ", " + cur_z);
				for (int i = 0; i < Orientation.listeners.size(); i++) {
					Orientation.listeners.get(i).onChange(cur_x, cur_y, cur_z);
				}
				if(countConfigGyroscrope>=400)
				{
					//Gyroscope.config(cur_x,cur_y,cur_z);
					//countConfigGyroscrope = countConfigGyroscrope-400;
					//isConfigGyroscrope = true;
				}
				countConfigGyroscrope++;
			}
	    }
    }; 
	
}

