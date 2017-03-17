package com.amo.keyfob.ui.mpu6050;

import java.util.UUID;

import com.amo.keyfob.Constans;
import com.amo.keyfob.R;
import com.amo.keyfob.logs.MyLog;
import com.amo.keyfob.service.BluetoothLeService;

import android.app.Activity;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.media.AudioManager;
import android.media.SoundPool;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

public class Mpu3DActivity extends Activity {
	public static final String SERVERID ="SERVERID";
	public static final String CHARAID ="CHARAID";
	private static final String TAG = "Mpu3DActivity";

	private final String ACTION_NAME_RSSI = "AMOMCU_RSSI"; // 其他文件广播的定义必须一致
	private final String ACTION_CONNECT = "AMOMCU_CONNECT"; // 其他文件广播的定义必须一致
	
	// 根据rssi 值计算距离， 只是参考作用， 不准确---amomcu
	static final int rssibufferSize = 10;
	int[] rssibuffer = new int[rssibufferSize];
	int rssibufferIndex = 0;
	boolean rssiUsedFalg = false;
	
	
	GlSurfaceView mGLSurfaceView;
	BluetoothGattCharacteristic characteristic;
	boolean flag = true;
		
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		//requestWindowFeature(Window.FEATURE_NO_TITLE);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_NOSENSOR);
		super.onCreate(savedInstanceState);
		getActionBar().setTitle("串口助手  版本V1.02(2015.08.31)");
		registerBoradcastReceiver();		
		
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON); 		
		
		mGLSurfaceView = new GlSurfaceView(this);
		setContentView(mGLSurfaceView);
		
		//setContentView(mGLSurfaceView);
		Intent intent = getIntent();
		int servidx = intent.getIntExtra(SERVERID, -1);
		String uuidString = intent.getStringExtra(CHARAID);
		MyLog.i(TAG, "servid="+servidx + " uuid="+uuidString);
		UUID uuid = UUID.fromString(uuidString);


		BluetoothGattService  gattService = Constans.gattServiceObject.get(servidx);  
		characteristic = gattService.getCharacteristic(uuid) ;
		if (characteristic == null) {
			Toast.makeText(this, getString(R.string.mpu6050_sensor_fail), Toast.LENGTH_LONG).show();
			finish();
		}

		//registerReceiver(mGattUpdateReceiver, new IntentFilter(BluetoothLeService.ACTION_DATA_AVAILABLE));

		
		Constans.mBluetoothLeService.readCharacteristic(characteristic);
	}

	// 	接收 rssi 的广播
	private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();

			if (action.equals(ACTION_NAME_RSSI)) {
				int rssi = intent.getIntExtra("RSSI", 0);

				// 以下这些参数我 amomcu 自己设置的， 不太具有参考意义，
				// 实际上我的本意就是根据rssi的信号前度计算以下距离，
				// 以便达到定位目的， 但这个方法并不准 ---amomcu---------20150411

				int rssi_avg = 0;
				int distance_cm_min = 10; // 距离cm -30dbm
				int distance_cm_max_near = 1500; // 距离cm -90dbm
				int distance_cm_max_middle = 5000; // 距离cm -90dbm
				int distance_cm_max_far = 10000; // 距离cm -90dbm
				int near = -72;
				int middle = -80;
				int far = -88;
				double distance = 0.0f;

				if (true) {
					rssibuffer[rssibufferIndex] = rssi;
					rssibufferIndex++;

					if (rssibufferIndex == rssibufferSize)
						rssiUsedFalg = true;

					rssibufferIndex = rssibufferIndex % rssibufferSize;

					if (rssiUsedFalg == true) {
						int rssi_sum = 0;
						for (int i = 0; i < rssibufferSize; i++) {
							rssi_sum += rssibuffer[i];
						}

						rssi_avg = rssi_sum / rssibufferSize;

						if (-rssi_avg < 35)
							rssi_avg = -35;

						if (-rssi_avg < -near) {
							distance = distance_cm_min
									+ ((-rssi_avg - 35) / (double) (-near - 35))
									* distance_cm_max_near;
						} else if (-rssi_avg < -middle) {
							distance = distance_cm_min
									+ ((-rssi_avg - 35) / (double) (-middle - 35))
									* distance_cm_max_middle;
						} else {
							distance = distance_cm_min
									+ ((-rssi_avg - 35) / (double) (-far - 35))
									* distance_cm_max_far;
						}
					}
				}

				getActionBar().setTitle(
						"RSSI: " + rssi_avg + " dbm" + ", " + "距离: "
								+ (int) distance + " cm");
			} else if (action.equals(ACTION_CONNECT)) {
				int status = intent.getIntExtra("CONNECT_STATUC", 0);
				if (status == 0) {
					getActionBar().setTitle("已断开连接");
					finish();
				} else {
					getActionBar().setTitle("已连接");
				}
			}
		}
	};

	// 注册广播
	public void registerBoradcastReceiver() {
		IntentFilter myIntentFilter = new IntentFilter();
		myIntentFilter.addAction(ACTION_NAME_RSSI);
		myIntentFilter.addAction(ACTION_CONNECT);
		// 注册广播
		registerReceiver(mBroadcastReceiver, myIntentFilter);
	}
	
	@Override
	protected void onResume() { 
		// Ideally a game should implement onResume() and onPause()
		// to take appropriate action when the activity looses focus
		super.onResume();
		mGLSurfaceView.onResume();
		registerReceiver(mGattUpdateReceiver, new IntentFilter(BluetoothLeService.ACTION_DATA_AVAILABLE));
	}

	@Override
	protected void onPause() {
		// Ideally a game should implement onResume() and onPause()
		// to take appropriate action when the activity looses focus
		super.onPause();
		unregisterReceiver(mGattUpdateReceiver);
		mGLSurfaceView.onPause();
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		  if (keyCode == KeyEvent.KEYCODE_BACK) {
			  MyLog.i(TAG, "keyback@");
	            finish();
	            return false;
	        } else {
	            return super.onKeyDown(keyCode, event);
	        }
	}
	
	private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			final String action = intent.getAction();
			System.out.println("action = " + action);
			if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)) {
                byte []data = intent.getByteArrayExtra(BluetoothLeService.EXTRA_DATA);
                int len = data.length;
                getblePacket(data);
                
                Constans.mBluetoothLeService.readCharacteristic(characteristic);                
			}
		}
	};
	

	private void getblePacket(byte[] packet){
	//	Log.i(TAG, "data.len="+data.length());
	    float[] q = new float [4];
	    for (int ii=0, i=13; i<20; i+=2) {//13 15 17 19
	        q[ii++] =  (short)(((packet[i-1]&255) | (packet[i]&255) <<8)) / 16384.0f;
	    }
	    
	    StringBuffer buffer = new StringBuffer();
	    for (int i=0; i<q.length; i++)
	        buffer.append("  q["+i+"]="+q[i]);
	    //	Log.d(TAG, buffer.toString());
	
	    float []data = new float[3];
	    
	    // 四元数变欧拉角
	    
	    data[0] = (float) Math.atan2(2*q[1]*q[2] - 2*q[0]*q[3], 2*q[0]*q[0] + 2*q[1]*q[1] - 1);   // psi
	    data[1] = (float) -Math.asin(2*q[1]*q[3] + 2*q[0]*q[2]);                              		// theta
	    data[2] = (float) Math.atan2(2*q[2]*q[3] - 2*q[0]*q[1], 2*q[0]*q[0] + 2*q[3]*q[3] - 1);   // phi
	    //
	    data[0] = data[0] * 180.0f / 3.14f;
	    data[1] = data[1] * 180.0f / 3.14f;
	    data[2] = data[2] * 180.0f / 3.14f;
	    
	    MyLog.i(TAG, "RealTag [0]="+ data[0] + "  data[1]"+ data[1] +"   data[2]=" + data[2]);
	    
	    mGLSurfaceView.onMpu6050Sensor(data[2], data[1], data[0]);
	//	mGLSurfaceView.onMpu6050Sensor(Angel_accX, Angel_accY, Angel_accZ);	    
					
	}	
}
