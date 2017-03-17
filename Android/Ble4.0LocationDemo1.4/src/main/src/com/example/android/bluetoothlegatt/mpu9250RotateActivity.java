package com.example.android.bluetoothlegatt;

import com.example.android.bluetoothlegatt.LocationPeriodicTask.OnTaskFinishCallback;
import com.example.android.bluetoothlegatt.iBeaconClass.iBeacon;
import com.example.android.bluetoothlegatt.mpu6050.GlSurfaceView;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;



public class mpu9250RotateActivity extends Activity implements OnTaskFinishCallback {
    
    GlSurfaceView mGLSurfaceView;
    
    /**搜索BLE终端*/
    private BluetoothAdapter mBluetoothAdapter;

    private boolean mScanning;
    private Handler mHandler;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
         mGLSurfaceView = new GlSurfaceView(this);
		 setContentView(mGLSurfaceView);
		 
		 //LocationPeriodicTask task = new LocationPeriodicTask();
		 //task.start(this);
		 
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
	        
	        locationPeriodicTask = new LocationPeriodicTask();
			locationPeriodicTask.start(this);
	        
    }
    
    @Override
    protected void onDestroy() {
    	super.onDestroy();
    	if (locationPeriodicTask != null) {
			locationPeriodicTask.stop();
		}

    }
    
	@Override
    protected void onResume() {
        super.onResume();
        mGLSurfaceView.onResume();
        scanLeDevice(true);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mGLSurfaceView.onPause();
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
            	
            	float yaw = Float.intBitsToFloat(iBeaconClass.advYaw);
            	float pitch = Float.intBitsToFloat(iBeaconClass.advPitch);
            	float roll = Float.intBitsToFloat(iBeaconClass.advRoll);
            	
            	Log.e("yawwwww", "yaw = " + yaw);
            	
            	mGLSurfaceView.onMpu6050Sensor(pitch, roll, yaw);
            }
        });
	}
	
}
