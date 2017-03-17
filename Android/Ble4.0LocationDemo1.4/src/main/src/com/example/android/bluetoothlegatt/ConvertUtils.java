package com.example.android.bluetoothlegatt;

/**
 * 坐标转换
 * @author Administrator
 *
 */
public class ConvertUtils {
	public static final int TYPE_WIDHT = 1;
	public static final int TYPE_HEIGHT = 2;
	public static double geo2Screen(double geo, int type) {
		if (type == TYPE_WIDHT) {
			return geo * App.W_PX_P_M;
		} else {
			return geo * App.H_PX_P_M;
		}
	}
}
