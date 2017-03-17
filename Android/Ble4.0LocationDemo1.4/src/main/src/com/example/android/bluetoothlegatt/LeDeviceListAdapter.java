package com.example.android.bluetoothlegatt;

import java.util.ArrayList;
import java.util.List;

import com.example.android.bluetoothlegatt.iBeaconClass.iBeacon;
import com.example.android.bluetoothlegatt.iBeaconClass;
import com.example.android.bluetoothlegatt.iBeaconClass.sprotsUnitCollectData;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import java.text.DecimalFormat;

public class LeDeviceListAdapter extends BaseAdapter {

	// Adapter for holding devices found through scanning.

	private ArrayList<iBeacon> mLeDevices;
	private LayoutInflater mInflator;
	private Activity mContext;
	private static int count = 0, j = 0;
	
	public LeDeviceListAdapter(Activity c) {
		super();
		mContext = c;
		mLeDevices = new ArrayList<iBeacon>();
		mInflator = mContext.getLayoutInflater();
	} 
	public void addDevice(iBeacon device) {
		if(device==null)
			return;
		//根据bluetoothAddress来添加集合
		for(int i=0;i<mLeDevices.size();i++){
			String btAddress = mLeDevices.get(i).bluetoothAddress;
			if(btAddress.equals(device.bluetoothAddress)){
				mLeDevices.add(i+1, device);
				mLeDevices.remove(i);
				return;
			}
		}
		mLeDevices.add(device);
		
	}
	
	public ArrayList<iBeacon> get(){
		return mLeDevices;
	}

	public iBeacon getDevice(int position) {
		return mLeDevices.get(position);
	}

	public void clear() {
		mLeDevices.clear();
	}

	@Override
	public int getCount() {
		return mLeDevices.size();
	}

	@Override
	public Object getItem(int i) {
		return mLeDevices.get(i);
	}

	@Override
	public long getItemId(int i) {
		return i;
	}

	public View getView(int i, View view, ViewGroup viewGroup) {
		ViewHolder viewHolder;
		// General ListView optimization code.
		if (view == null) {
			view = mInflator.inflate(R.layout.listitem_device_f, null);
			viewHolder = new ViewHolder();
			viewHolder.deviceAddress = (TextView) view.findViewById(R.id.device_address);
			viewHolder.deviceName = (TextView) view.findViewById(R.id.device_name);
			viewHolder.deviceUUID= (TextView)view.findViewById(R.id.device_beacon_uuid);
			viewHolder.deviceMajor_Minor=(TextView)view.findViewById(R.id.device_major_minor);
			viewHolder.devicetxPower_RSSI=(TextView)view.findViewById(R.id.device_txPower_rssi);
			viewHolder.devicePos_x_y=(TextView)view.findViewById(R.id.devicePos_x_y);
			viewHolder.deviceDistance=(TextView)view.findViewById(R.id.deviceDistance);
			view.setTag(viewHolder);
		} else {
			viewHolder = (ViewHolder) view.getTag();
		}

		iBeacon device = mLeDevices.get(i);
		final String deviceName = device.name;
		//double distance = device.distance;
		DecimalFormat df = new DecimalFormat("#.00");
		if (deviceName != null && deviceName.length() > 0)
			viewHolder.deviceName.setText(deviceName);
		else
			viewHolder.deviceName.setText(R.string.unknown_device);
		
		viewHolder.deviceAddress.setText(device.bluetoothAddress);
		viewHolder.deviceUUID.setText(device.proximityUuid);
		viewHolder.deviceMajor_Minor.setText("jump:" + iBeaconClass.sportsUnitCollectData.jump);
		viewHolder.devicetxPower_RSSI.setText("dash:" + iBeaconClass.sportsUnitCollectData.dash);
		//viewHolder.devicetxPower_RSSI.setText("txPower:"+device.txPower+",rssi:"+ df.format(device.rssi));

		viewHolder.deviceDistance.setText("veer:" + iBeaconClass.sportsUnitCollectData.veer);
		
//		if(iBeaconClass.majorDistance[device.major] < 1.0)
//			viewHolder.deviceDistance.setText("Distance : 0"+df.format(iBeaconClass.majorDistance[device.major]));
//		else
//			viewHolder.deviceDistance.setText("Distance : "+df.format(iBeaconClass.majorDistance[device.major]));
		viewHolder.devicePos_x_y.setText("Pos_radius:"+df.format(iBeaconClass.position_info[0])+",Pos_theta:"+df.format(iBeaconClass.position_info[1]));
		return view;
	}
	class ViewHolder {
		TextView deviceName;
		TextView deviceAddress;
		TextView deviceUUID;
		TextView deviceMajor_Minor;
		TextView devicetxPower_RSSI;
		TextView deviceDistance;
		TextView devicePos_x_y;
	}
	

}
