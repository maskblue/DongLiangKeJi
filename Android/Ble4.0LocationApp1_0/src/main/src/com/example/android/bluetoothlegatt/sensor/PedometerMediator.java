package com.example.android.bluetoothlegatt.sensor;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;
import android.widget.TextView;

public class PedometerMediator implements SensorEventListener {
	private static final String TAG = "PedometerMediator";
	private SensorManager mSensorManager;
	private Sensor mAccelerometer;
	private Context context;
	
	private static int mStepValue;
	private float mLastValues[] = new float[3 * 2];
	private float mScale[] = new float[2];
	private float mYoffset;					//补偿
	private static long end = 0;			//开始时间和结束时间，时间窗
	private static long start = 0;
	private static float SENSITIVITY = 0;	
	
	//最后加速度方向
	private float mLastDirections[] = new float[3 * 2];
	private float mLastExtremes[][] = { new float[3 * 2], new float[3 * 2] };
	private float mLastDiff[] = new float[3 * 2];
	private int mLastMatch = -1;
	
	private PedometerMediator(Context context) {
		mSensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
		mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);	
		
		int h = 480; 
		mYoffset = h * 0.5f;
		//标准重力加速度
		mScale[0] = -(h * 0.5f * (1.0f / (SensorManager.STANDARD_GRAVITY * 2)));
//		Log.e("hewu", "mScale0:"+ -mScale[0]);
		//地球表面的最大磁场
		mScale[1] = -(h * 0.5f * (1.0f / (SensorManager.MAGNETIC_FIELD_EARTH_MAX)));
//		Log.e("hewu", "mScale1:"+ -mScale[1]);
		//预定义精度，是个经验值
		SENSITIVITY =  3.0f;
//		Log.e("hewu", "SENSITIVITY:"+ SENSITIVITY);
	};
	
	private static PedometerMediator instance;
	public static PedometerMediator getInstance(Context context) {
		if (instance == null) {
			instance =new PedometerMediator(context);
		}
		return instance;
	}
	
	public static PedometerMediator getInstanceWithoutContext() {
		return instance;
	}
	
	private boolean isStarted = false;
	private int total;
	
	public int getTotal() {
		return total;
	}
	
	public void clean() {
		total = 0;
	}
	
	public void start() {
		if (!isStarted) {
			mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
			isStarted = true;
		}
	}
	
	public void stop() {
		mSensorManager.unregisterListener(this);
		isStarted = false;
	}

	@Override
	public void onSensorChanged(SensorEvent event) {
		 FileOutputStream outStream;
		Sensor sensor = event.sensor;
		synchronized (this) {
			if(sensor.getType() == Sensor.TYPE_ORIENTATION ){
				
			}else{
				
				int j = (sensor.getType() == Sensor.TYPE_ACCELEROMETER) ? 1 : 0;
				if( j == 1 )
				{
					
					float vSum = 0;
					/*
					 * 获取加速计三轴数据，并对三轴数据进行补偿后求和再均值
					 */
					for(int i = 0; i < 3; i++)
					{
						final float v = mYoffset + event.values[i] * mScale[j];
						vSum += v;
					}
					int k = 0;
					float v = vSum / 3.0f;
					/*
					 * 将z轴数据和三轴和的平均值保存在txt文本文件中，用来matlab数据分析
					 */
					String s = String.valueOf(v); 
					s = s.concat("\t");
					s = s.concat(String.valueOf(event.values[2]));
					s = s.concat("\r\n");
					/*try {
						Log.i("tonghu", "v: " + v);
//						outStream = this.openFileOutput("accel.txt",Context.MODE_WORLD_READABLE);
						File file = new File("/sdcard/accel.txt");
						outStream = new FileOutputStream(file, true);
						outStream.write(s.getBytes());
			            outStream.close();
					} catch (FileNotFoundException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}*/
		             //如果新采集的均值大于前一个返回1小于则返回-1否返回0
					float direction = (v > mLastValues[k] ? 1 : (v < mLastValues[k] ? -1 : 0));
					//上一次的方向和新的如果相反
					if(direction == -mLastDirections[k]) {
						//方向变化
						
						int extType = (direction > 0 ? 0 : 1);    //判断是最大值还是最小值，极值
						//保存极大极小值
						mLastExtremes[extType][k] = mLastValues[k];
						//差分处理，得出变动的幅度
						float diff = Math.abs(mLastExtremes[extType][k] - mLastExtremes[1 - extType][k]);
//						Log.e("hewu", "diff:"+ diff);
						//幅度符合预定义的敏感变化
						if(diff > SENSITIVITY) {
//							Log.e("hewu", "mStepValue:"+ mStepValue);
							//和之前的差不多一样大
							boolean isAlmostAsLargeAsPrevious = diff > (mLastDiff[k] * 2 / 3);
							//之前的足够大
							boolean isPreviousLargeEnough = mLastDiff[k] > (diff / 3);
							//判断加速度方向是否相反
							boolean isNotContra = (mLastMatch != 1 - extType);
							
							if(isAlmostAsLargeAsPrevious && isPreviousLargeEnough && isNotContra)
							{
								end = System.currentTimeMillis();
								//时间窗，人迈出一步需要大于500ms的时间
								if(end - start > 500)
								{
									//判断为走了一步
//									Log.e("hewu", "mStepValue:"+ mStepValue);
//									mStepValue = Integer.parseInt((String) numberSteps.getText());
//									numberSteps.setText(Integer.toString(++mStepValue));
									total++;
									mLastMatch = extType;
									start = end;
								}
								
							}else
							{
								mLastMatch = -1;
							}
							
						}
						mLastDiff[k] = diff;
					}
					mLastDirections[k] = direction;
					mLastValues[k] = v;
				}
			}
		}
		
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		// TODO Auto-generated method stub
		
	}
	
	/**
	 * float转换byte
	 *
	 * @param bb
	 * @param x
	 * @param index
	 */
	public static void putFloat(byte[] bb, float x, int index) {
	    // byte[] b = new byte[4];
	    int l = Float.floatToIntBits(x);
	    for (int i = 0; i < 4; i++) {
	        bb[index + i] = new Integer(l).byteValue();
	        l = l >> 8;
	    }
	}

	/**
	 * 通过byte数组取得float
	 *
	 * @param bb
	 * @param index
	 * @return
	 */
	public static float getFloat(byte[] b, int index) {
	    int l;
	    l = b[index + 0];
	    l &= 0xff;
	    l |= ((long) b[index + 1] << 8);
	    l &= 0xffff;
	    l |= ((long) b[index + 2] << 16);
	    l &= 0xffffff;
	    l |= ((long) b[index + 3] << 24);
	    return Float.intBitsToFloat(l);
	}
}
