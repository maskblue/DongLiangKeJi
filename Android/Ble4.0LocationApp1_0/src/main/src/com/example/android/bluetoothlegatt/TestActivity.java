/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.android.bluetoothlegatt;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.app.ListActivity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;

import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.Transformation;
import android.view.animation.TranslateAnimation;
import android.view.Window;

import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

import com.estimote.sdk.Utils;
import com.example.android.blelocation.R;
import com.example.android.bluetoothlegatt.LocationPeriodicTask.OnTaskFinishCallback;
import com.example.android.bluetoothlegatt.iBeaconClass.iBeacon;
import com.example.android.bluetoothlegatt.sensor.Acceleration;
import com.example.android.bluetoothlegatt.sensor.Orientation;
import com.example.android.bluetoothlegatt.sensor.PedometerMediator;
import com.example.android.bluetoothlegatt.sensor.Orientation.SensorListener;

public class TestActivity extends Activity implements OnTaskFinishCallback {

	private View secondContainer;
	private View pointView;
	private LineView lineView;
	//private View pointView1;

	private final static String TAG = TestActivity.class.getSimpleName();
	private final static String UUID_KEY_DATA = "0000ffe1-0000-1000-8000-00805f9b34fb";

    /**搜索BLE终端*/
    private BluetoothAdapter mBluetoothAdapter;

    private boolean mScanning;
    private Handler mHandler;
    private TextView tv;
    private ImageView iv1, iv2, iv3, iv4, iv5, iv6;
    private boolean isFirst = true;
    
/* 保存刚显示的相对半径和角度
 */
    private static double posRadius = 0.0;
	private static double posTheta = 0.0;
    // Stops scanning after 10 seconds.
    private static final long SCAN_PERIOD = 600000;

    private static double x = 0.0, y = 0.0;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //getActionBar().setTitle(R.string.title_devices);
//        getWindow().requestFeature(Window.feature_);
        setContentView(R.layout.activity_device_scan);
        secondContainer = findViewById(R.id.second);
        pointView = findViewById(R.id.point);
        //pointView1 = findViewById(R.id.point1);
        mHandler = new Handler();
        tv = (TextView) findViewById(R.id.tv);

        // Use this check to determine whether BLE is supported on the device.  Then you can
        // selectively disable BLE-related features.
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, R.string.ble_not_supported, Toast.LENGTH_SHORT).show();
            finish();
        }

        // Initializes a Bluetooth adapter.  For API level 18 and above, get a reference to
        // BluetoothAdapter through BluetoothManager.
        final BluetoothManager bluetoothManager =
                (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();
        
        // Checks if Bluetooth is supported on the device.
        if (mBluetoothAdapter == null) {
            Toast.makeText(this, R.string.error_bluetooth_not_supported, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        //�?启蓝�?
        mBluetoothAdapter.enable();
        
        timer = new Timer();
        timer.schedule(new TimerTask() {
			
			@Override
			public void run() {
				mHandler.removeCallbacks(null);
				mHandler.post(new Runnable() {
					DecimalFormat df = new DecimalFormat("#.00");
					@Override
					public void run() {
						if(PedometerMediator.getInstance(TestActivity.this).getTotal() * 0.6 < 1.0) {
							//tv.setText(Orientation.get_pose()[0] + ", \ndistance: 0" + df.format(PedometerMediator.getInstance(TestActivity.this).getTotal() * 0.6) + "m" + "     step: " + Acceleration.Radians);
						}else {
							//tv.setText(Orientation.get_pose()[0] + ", \ndistance: " + df.format(PedometerMediator.getInstance(TestActivity.this).getTotal() * 0.6) + "m" + "     step: " + Acceleration.Radians);
						}
						
						if(iBeaconClass.distance < 1.0) {
							//tv.setText(tv.getText() + "\ncalcDistance: 0" + df.format(iBeaconClass.distance));
						}else {
							//tv.setText(tv.getText() + "\ncalcDistance: " + df.format(iBeaconClass.distance));
						}
						
						//tv.setText(tv.getText() + "\nrssi: " + df.format(iBeaconClass.maxRssi) );
						
						//pointView.setRotation(Orientation.get_pose()[0]);
						//pointView1.setRotation(Orientation.get_pose()[0]);
					}
				});
			}
		}, 0, 100);
        
        adjustBeconPostion();
        //adjustLineViewPosition();
        
        PedometerMediator.getInstance(this).start();
        
        xAnimator = new ObjectAnimator();
        xAnimator.setPropertyName("x");
        yAnimator = new ObjectAnimator();
        yAnimator.setPropertyName("y");
        animatorSet = new AnimatorSet();
        animatorSet.playTogether(xAnimator, yAnimator);
        animatorSet.setDuration(1000);
        
        locationPeriodicTask = new LocationPeriodicTask();
		locationPeriodicTask.start(this);
    }
    
    private Timer timer;
    
    @Override
    protected void onDestroy() {
    	super.onDestroy();
    	PedometerMediator.getInstance(this).stop();
    	timer.cancel();
    	Orientation.stop();
    	
    	if (locationPeriodicTask != null) {
			locationPeriodicTask.stop();
		}
    }


    /**
     * 调整四个设备在地图上的位置
     */
    private void adjustBeconPostion() {
    	iv1 = (ImageView) findViewById(R.id.iv1);
    	iv2 = (ImageView) findViewById(R.id.iv2);
    	iv3 = (ImageView) findViewById(R.id.iv3);
    	iv4 = (ImageView) findViewById(R.id.iv4);
    	iv5 = (ImageView) findViewById(R.id.iv5);
    	iv6 = (ImageView) findViewById(R.id.iv6); 
    	
    	setBeaconPos(iv1, C.bEACON_POS[0].x, C.bEACON_POS[0].y);
    	setBeaconPos(iv2, C.bEACON_POS[1].x, C.bEACON_POS[1].y);
    	setBeaconPos(iv3, C.bEACON_POS[2].x, C.bEACON_POS[2].y);
    	setBeaconPos(iv4, C.bEACON_POS[3].x, C.bEACON_POS[3].y);
    	//setBeaconPos(iv5, C.bEACON_POS[4].x, C.bEACON_POS[4].y);
    	//setBeaconPos(iv6, C.bEACON_POS[5].x, C.bEACON_POS[5].y);
	}
    
    private void setBeaconPos(View iv, float x, float y) {
    	iv.setX((float) (x * App.W_PX_P_M - 10));
    	iv.setY((float) (y * App.H_PX_P_M - 10 + 420));
    }
    
    /**
     * 调整划线的位置
     */
    private void adjustLineViewPosition() {
//    	lineView = (LineView) findViewById(R.id.lineview);
//    	LayoutParams layoutParams = lineView.getLayoutParams();
//    	layoutParams.width = (int) (6 * App.W_PX_P_M);
//    	layoutParams.height = (int) (6 * App.H_PX_P_M);
//    	lineView.setX((float) (1.0 * App.W_PX_P_M));
//    	lineView.setY((float) (1.0 * App.H_PX_P_M + 420));
//    	lineView.setLayoutParams(layoutParams);
//    	lineView.invalidate();
    }


	@Override
    protected void onResume() {
        super.onResume();

        // Initializes list view adapter.
        scanLeDevice(true);
    }

    @Override
    protected void onPause() {
        super.onPause();
        scanLeDevice(false);
    }

    private void scanLeDevice(final boolean enable) {
        if (enable) {
            // Stops scanning after a pre-defined scan period.
//            mHandler.postDelayed(new Runnable() {
//                @Override
//                public void run() {
//                    mScanning = false;
//                    mBluetoothAdapter.stopLeScan(mLeScanCallback);
//                    invalidateOptionsMenu();
//                }
//            }, SCAN_PERIOD);

            mScanning = true;
            mBluetoothAdapter.startLeScan(mLeScanCallback);
        } else {
            mScanning = false;
            mBluetoothAdapter.stopLeScan(mLeScanCallback);
        }
        invalidateOptionsMenu();
    }
    
    public final static double  dAgredd = 70;
    private Animation animation = new TranslateAnimation(0, 0, 0, 0);
    private ObjectAnimator xAnimator;
    private ObjectAnimator yAnimator;
    private AnimatorSet animatorSet;
    private double px;
    private double py;

    // Device scan callback.
    private BluetoothAdapter.LeScanCallback mLeScanCallback =
            new BluetoothAdapter.LeScanCallback() {

        @Override
        public void onLeScan(final BluetoothDevice device, int rssi, byte[] scanRecord) {

        	final iBeacon ibeacon = iBeaconClass.fromScanData(device,rssi,scanRecord);
        }
    };
	private LocationPeriodicTask locationPeriodicTask;

	@Override
	public void onFinish() {
		runOnUiThread(new Runnable() {
            @Override
            public void run() {
            	double radius = iBeaconClass.radius;
            	double theta = iBeaconClass.theta;
            	double d = 0.0, v = 0.0;
            	
            	if(iBeaconClass.nodeFlags >= 0)
            	{	
            		if( iBeaconClass.nodeFlags == 1)
            		{
            			x = iBeaconClass.nodeCoordinate[iBeaconClass.nodeFlags][0] + radius * Math.cos(iBeaconClass.ToR(theta));
                		y = iBeaconClass.nodeCoordinate[iBeaconClass.nodeFlags][1] + radius * Math.sin(iBeaconClass.ToR(theta));
            		}
            		else
            		{
            			x = iBeaconClass.nodeCoordinate[iBeaconClass.nodeFlags][0] + radius * Math.cos(iBeaconClass.ToR(theta));
                		//y = iBeaconClass.nodeCoordinate[iBeaconClass.nodeFlags][1] + radius * Math.sin(iBeaconClass.ToR(theta));
                		y = 8.8f;
            		}
            		
            		if(x <= 0.5)
            			x = 0.5;
            		if(x >= 18.0)
            			x = 18.0;
            		
            	}
            	
        	
            	Log.i("tonghu3", "x: " + (float) ConvertUtils.geo2Screen(x, ConvertUtils.TYPE_WIDHT) + ", y: " + (float) ConvertUtils.geo2Screen(y, ConvertUtils.TYPE_HEIGHT));
            	float dx = (float) ConvertUtils.geo2Screen(x, ConvertUtils.TYPE_WIDHT);
            	float dy = (float) ConvertUtils.geo2Screen(y, ConvertUtils.TYPE_HEIGHT);
            	x = ConvertUtils.geo2Screen(x, ConvertUtils.TYPE_WIDHT);
            	y = ConvertUtils.geo2Screen(y, ConvertUtils.TYPE_HEIGHT) + 420;
            	//pointView1.setX((float)x);
            	//pointView1.setY((float)y);
            	
            	if (px != x || py != y) {
            		px = x;
            		py = y;
            		if (animation != null) {
            			animation.cancel();
            			animation = null;
            		}
//            		animation = new TranslateAnimation(Animation.ABSOLUTE, pointView.getX(), 
//            				Animation.ABSOLUTE, (float)x, Animation.ABSOLUTE, pointView.getY(), Animation.ABSOLUTE, (float)y);
//            		animation.setFillAfter(true);
//            		pointView.startAnimation(animation);
            		if (animatorSet.isRunning()) {
						animatorSet.cancel();
					}
            		xAnimator.setFloatValues(pointView.getX(), (float)x - 20);
            		yAnimator.setFloatValues(pointView.getY(), (float)y - 50);
            		animatorSet.setTarget(pointView);
            		animatorSet.start();
//            		pointView.setX((float) x);
//                	pointView.setY((float) y);
            		
            	}
            }
        });
	}
	
}
