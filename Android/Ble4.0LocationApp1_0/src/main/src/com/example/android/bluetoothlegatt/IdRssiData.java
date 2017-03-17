package com.example.android.bluetoothlegatt;

/**
 * 创建
 * IdRssiData data = new IdRssiData(id, rssi);
 * 拿数据：
 * id： data.id
 * rssi: data.rssi
 * 
 * 存map
 * map.put(key, data);
 * 从map读
 * IdRssiData data = map.get(key);
 * 
 * 创建map
 * Map<Integer, IdRssiData> map = new HashMap<Integer, IdRssiData>();
 * @author Administrator
 *
 */
public class IdRssiData {
	public double preRssi;
	public double rssi;
	public IdRssiData(double preRssi, double rssi) {
		super();
		this.preRssi = preRssi;
		this.rssi = rssi;
	}
}
