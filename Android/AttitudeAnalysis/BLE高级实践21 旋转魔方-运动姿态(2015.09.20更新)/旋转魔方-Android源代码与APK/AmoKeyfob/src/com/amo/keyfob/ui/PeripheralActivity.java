package com.amo.keyfob.ui;

import java.util.ArrayList;
import java.util.HashMap;

import com.amo.keyfob.BaseApp;
import com.amo.keyfob.Constans;
import com.amo.keyfob.R;
import com.amo.keyfob.util.StringUtil;


import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

public class PeripheralActivity extends Activity implements OnItemClickListener, OnClickListener {
	private final String TAG = "PeripheralActivity";
	private static final long SCAN_PERIOD = 100000000;
	private Button mScanButton;
	private ListView mDeviceListView;
	private boolean isScan = true;
	private Handler mHandler = new Handler();
	private BluetoothAdapter mBluetoothAdapter;
	//锟斤拷锟斤拷锟借备锟斤拷锟斤拷
	private ArrayList<BluetoothDevice> mLeDevices = new ArrayList<BluetoothDevice>();
	private ArrayList<HashMap<String, Object>> listItem; // ListView锟斤拷锟斤拷锟皆达拷锟斤拷锟斤拷锟绞撅拷诮锟斤拷妫拷锟斤拷锟斤拷锟揭伙拷锟紿ashMap锟斤拷锟叫憋拷
	private SimpleAdapter listItemAdapter; // ListView锟斤拷锟斤拷锟斤拷锟斤拷
	private static final int REQUEST_ENABLE_BT = 1;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
//		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_NOSENSOR);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.peripheral);
		BaseApp.getInstance().addActivity(this);

		initView();
		initBlue();
		setupView();
		
		
//		  SoundPool soundPool;
//		  soundPool= new SoundPool(10,AudioManager.STREAM_SYSTEM,5);
//		  soundPool.load(this,R.raw.music5,1);
//		  soundPool.play(1,1, 1, 0, 0, 1);				
		//scanLeDevice(isScan);
	}

	/**
	 * 锟斤拷锟斤拷只锟斤拷锟斤拷锟�
	 */
	private void initBlue() {

		// 锟斤拷榈鼻帮拷只锟斤拷欠锟街э拷锟絙le 锟斤拷锟斤拷,锟斤拷锟街э拷锟斤拷顺锟斤拷锟斤拷锟�
		if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
			Toast.makeText(this, R.string.ble_not_supported, Toast.LENGTH_SHORT).show();
			finish();
		}

		// 锟斤拷始锟斤拷 Bluetooth adapter, 通锟斤拷锟斤拷锟斤拷锟斤拷锟斤拷锟斤拷锟矫碉拷一锟斤拷锟轿匡拷锟斤拷锟斤拷锟斤拷锟斤拷锟斤拷(API锟斤拷锟斤拷锟斤拷锟斤拷锟斤拷android4.3锟斤拷锟斤拷锟较和版本)
		final BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
		mBluetoothAdapter = bluetoothManager.getAdapter();
		// 锟斤拷锟斤拷璞革拷锟斤拷欠锟街э拷锟斤拷锟斤拷锟�
		if (mBluetoothAdapter == null) {
			Toast.makeText(this, R.string.error_bluetooth_not_supported, Toast.LENGTH_SHORT).show();
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		// 为锟斤拷确锟斤拷锟借备锟斤拷锟斤拷锟斤拷锟斤拷使锟斤拷, 锟斤拷锟角帮拷锟斤拷锟斤拷璞该伙拷锟斤拷锟�,锟斤拷锟斤拷锟皆伙拷锟斤拷锟斤拷锟矫伙拷要锟斤拷锟斤拷锟斤拷权锟斤拷锟斤拷锟斤拷锟斤拷
		//锟斤拷锟斤拷onActivityResult锟叫伙拷取锟斤拷锟斤拷值
		if (!mBluetoothAdapter.isEnabled()) {
			Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
			startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
		}
		
		isScan = true;
		scanLeDevice(isScan);
	}

//	@Override
//	protected void onStop() {
//		super.onStop();			
//		isScan = false;
//		scanLeDevice(isScan);
//		finish();
//	}	
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, data);
		//锟杰撅拷锟斤拷锟斤拷锟斤拷,锟剿筹拷应锟斤拷
		if (requestCode == REQUEST_ENABLE_BT && resultCode == RESULT_CANCELED) {
			finish();
		}
	}
	/**
	 * 锟截硷拷锟斤拷始锟斤拷锟斤拷锟斤拷, 锟斤拷锟斤拷
	 */
	private void setupView() {
		listItem = new ArrayList<HashMap<String, Object>>();
		listItemAdapter = new SimpleAdapter(this, listItem, R.layout.listview,
				new String[]{"image", "title", "text"},
				new int[]{R.id.ItemImage, R.id.ItemTitle, R.id.ItemText});
		mDeviceListView.setAdapter(listItemAdapter);

		mDeviceListView.setOnItemClickListener(this);
		mScanButton.setOnClickListener(this);
	}


	/**
	 * 寻锟揭控硷拷
	 */
	private void initView() {
		mScanButton = (Button) findViewById(R.id.bt_scan);
		mDeviceListView = (ListView) findViewById(R.id.lv_device);
	}


	@Override
	public void onClick(View view) {
		switch (view.getId()) {
		case R.id.bt_scan:
			scanLeDevice(!isScan);
			break;

		default:
			break;
		}

	}

	private void scanLeDevice(final boolean enable) {
		if (enable) {	//true 锟斤拷始扫锟斤拷
			// Stops scanning after a pre-defined scan period.
			mHandler.postDelayed(new Runnable() {
				@Override
				public void run() {
					mBluetoothAdapter.stopLeScan(mLeScanCallback);
					mScanButton.setText(R.string.start_scan);
					isScan = false;
				}
			}, SCAN_PERIOD);	//锟接筹拷10s 执锟斤拷 -> 扫锟斤拷10s 锟斤拷停止扫锟斤拷
			mScanButton.setText(R.string.stop_scan);
			isScan = true;
			deleteItem();
			mLeDevices.clear();
			mBluetoothAdapter.startLeScan(mLeScanCallback);
		} else {
			mBluetoothAdapter.stopLeScan(mLeScanCallback);
			mScanButton.setText(R.string.start_scan);
			isScan = false;
		}
	}


	/**
	 * 扫锟斤拷锟斤拷锟斤拷 mLeDevices.add
	 * addItem
	 */
	private BluetoothAdapter.LeScanCallback mLeScanCallback = new BluetoothAdapter.LeScanCallback() {

		@Override
		public void onLeScan(final BluetoothDevice device, final int rssi, final byte[] scanRecord) {
			runOnUiThread(new Runnable() {

				@Override
				public void run() {                	
					int majorid,minorid;
					majorid = 0;//scanRecord[25]*256 + scanRecord[26];
					minorid = 0;//scanRecord[27]*256 + scanRecord[28];
					
					addItem(device, device.getName(),device.getAddress()+"\r\n" + StringUtil.bytesToHex(scanRecord, 0, scanRecord.length-1)+ "\r\n" + "MajorID:"+majorid+"    MinorID:"+minorid+"    RSSI:"+rssi);
//					mLeDevices.add(device);
					//	MyLog.i(TAG, device.getName());
					// 发现小米3必须加以下的这3个语句，否则不更新数据，而三星的机子s3则没有这个问题
					if (isScan == true) {
						mBluetoothAdapter.stopLeScan(mLeScanCallback);
						mBluetoothAdapter.startLeScan(mLeScanCallback);
					}					
//					Handler mHandler = new Handler();
//					mHandler.post(new Runnable() {
//						@Override
//						public synchronized void run() {
//							if (isScan == true) {
//								mBluetoothAdapter.stopLeScan(mLeScanCallback);
//								mBluetoothAdapter.startLeScan(mLeScanCallback);
//							}
//						}
//					});	
				}
			});
		}
	};

	private void addItem(BluetoothDevice device, String devname,String address)
	{
		boolean flag = false;
		int i = 0;
		for(i = 0;i < listItem.size(); i++)
		{
			HashMap<String, Object> map;
			map = listItem.get(i);			
			String str = (String)(map.get("text"));
			
			String cur = address.substring(0, 17);
			String tmp = str.substring(0, 17);
			if(cur.endsWith(tmp))
			{				
				flag = true;
				break;
			}
			
//            System.out.println(listItem.get(i));
            
//            Log.d(TAG, "i : " + i + " device.getAddress()" + address + "  devname" + devname);
        }
		
		if(flag == true)
		{
			// 修改即可
			HashMap<String, Object> map;
			map = listItem.get(i);		
			map.put("title", devname);
			map.put("text",address);			
			listItem.set(i, map);
		}
		else
		{
			HashMap<String, Object> map = new HashMap<String, Object>();
//			map.put("image", R.drawable.icon);
			map.put("title", devname);
			map.put("text", address);
			
			listItem.add(map);
			if (i % 2 == 0)// set color
			{
				mDeviceListView.setBackgroundColor(Color.argb(25, 255, 0, 0));
			} else {
				mDeviceListView.setBackgroundColor(Color.argb(25, 0, 255, 0));
			}
			
			mLeDevices.add(device);
		}
		
		listItemAdapter.notifyDataSetChanged();
	}

	private void deleteItem()
	{
		listItem.clear();
		listItemAdapter.notifyDataSetChanged();
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id ) {
		final BluetoothDevice device = mLeDevices.get(position);
		if (device == null) return;
		final Intent intent = new Intent();
		intent.setClass(PeripheralActivity.this, MainActivity.class);
		intent.putExtra(MainActivity.EXTRAS_DEVICE_NAME, device.getName());
		intent.putExtra(MainActivity.EXTRAS_DEVICE_ADDRESS, device.getAddress());
		if (isScan) {
			scanLeDevice(false);
		}
		startActivity(intent);
		
		Log.d(TAG, "position : " + position + "device.getAddress()" + device.getAddress());
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		if (keyCode == KeyEvent.KEYCODE_BACK ) {			
			Constans.exit_ask(this);
		}
		return super.onKeyDown(keyCode, event);
	}
}
