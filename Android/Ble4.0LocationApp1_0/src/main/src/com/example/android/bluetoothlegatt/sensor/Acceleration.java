package com.example.android.bluetoothlegatt.sensor;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import com.example.android.bluetoothlegatt.sensor.Acceleration;
import com.example.android.bluetoothlegatt.sensor.Acceleration.OnStepChangeListener;

import android.util.Log;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

public class Acceleration{
	private static final double INVERSE_PRECISION = 20.0;
	
	public static float cur_x = (float)0.000;
	public static float cur_y = (float)0.000;
	public static float cur_z = (float)0.000;
	
	public static int Radians = 0;
    
    private static boolean isRegisteredSensor = false;
    private static SensorManager sensorManager = null;
    private static Timer timer;
    
    public static int getTotal() {
		return Radians;
	}
    
    public static void start(SensorManager sm) {
		sensorManager = sm;
		
		List<Sensor> sensors = sensorManager.getSensorList(Sensor.TYPE_LINEAR_ACCELERATION);
		
		if (sensors != null && sensors.size() > 0) 
		{
			Sensor sensor = sensors.get(0);
			isRegisteredSensor = sensorManager.registerListener(myListener, sensor,SensorManager.SENSOR_DELAY_GAME);
			 
		}
		
		timer = new Timer();
		timer.scheduleAtFixedRate(new TimerTask() {
			
			@Override
			public void run() {
				Radians = pedometerArithmetic.pedometerArithmeticKalman(cur_x, cur_y, cur_z);
				//Log.e("stepsssss", "step" + Radians);
			}
		}, 0, 60);
    }

	public static void stop() {
		if (isRegisteredSensor) {
			sensorManager.unregisterListener(myListener);
			isRegisteredSensor = false;
		}
    }
	
	public static void clean() {
		Radians = 0;
		pedometerArithmetic.clean();
	}
	
	public static interface OnStepChangeListener {
		void onStepChanage();
	}
	
	private static OnStepChangeListener onStepChangeListener;
	
	public static void setOnStepChagneListener(OnStepChangeListener onStepChangeListener) {
		Acceleration.onStepChangeListener = onStepChangeListener;
	}
	
	final private static SensorEventListener myListener = new SensorEventListener() { 
	
	    @Override
	    public void onAccuracyChanged(Sensor sensor, int accuracy) {
			//Log.e("Gyroscope", "-----------------------accuracy=" + accuracy);

	    }
		///*
	    @Override
	    public void onSensorChanged(SensorEvent event) {
			synchronized (this) 
			{
			
				float read_x;
				float read_y;
				float read_z;
			
			    read_x = event.values[SensorManager.DATA_X];
			    read_y = event.values[SensorManager.DATA_Y];
			    read_z = event.values[SensorManager.DATA_Z];
			    
				cur_x =read_x;
				cur_y =read_y;
				cur_z =read_z;

//				read_x =  (float)( (int)(read_x*INVERSE_PRECISION)/INVERSE_PRECISION );
//				read_y =  (float)( (int)(read_y*INVERSE_PRECISION)/INVERSE_PRECISION );
//				read_z =  (float)( (int)(read_z*INVERSE_PRECISION)/INVERSE_PRECISION );
//				
//				cur_x += read_x;
//				cur_y += read_y;
//				cur_z += read_z;
//
//				cur_x -= (int)(cur_x/360)*360;
//				cur_y -= (int)(cur_y/360)*360;
//				cur_z -= (int)(cur_z/360)*360;
//				Log.e("Gyroscope", "-----------------------x=" + cur_x + ", y=" + cur_y+",z="+cur_z);
//				int tep = 0;
//				if(cur_x * cur_y * cur_z != 0)
//				{
//					tep = pedometerArithmetic.pedometerArithmeticKalman(cur_x, cur_y, cur_z);
//					Log.e("stepsssss", "step" + tep);
//				}
					 
				
			}
	    }
    };
	
}

