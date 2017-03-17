package com.example.android.bluetoothlegatt;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.math.BigDecimal;
import java.math.RoundingMode;

import com.example.android.bluetoothlegatt.sensor.Acceleration;
import com.example.android.bluetoothlegatt.sensor.PedometerMediator;
import com.example.android.bluetoothlegatt.sensor.Orientation;

import android.bluetooth.BluetoothDevice;
import android.util.Log;

//import matlabplot.*;
/*
 * 	@author 		hewu
 * 	@FN 			iBeaconClass
 *	@Description	实时采集各个信标节点的rssi，对rssi进行高斯滤波和加权均值滤波，将得到的rssi
 *					进行曲线拟合，计算盲节点到目标节点的距离distance，然后通过和运动传感器结合的方式来定位
 */

/*
 *	获取信标节点的相关信息，如rssi和信标节点的设备mac地址和id号 
 */

public class iBeaconClass {
	
    static public  class iBeacon{
    	public String name;
    	public int major;
    	public int minor;
    	public String proximityUuid;
    	public String bluetoothAddress;
    	public int txPower;
    	public int rssi;
    	public double distance;
    }
  
    public static class locationData {
    	double new_radius = 0;				//解算出来的新的到节点的距离
    	double new_theta = 0;				//解算出来相对于北方的顺时针偏角
    	double dev_last_radius = 0.01;		//上一位置相对于节点的半径
    	double dev_last_theta = 0.01;		//上一位置相对于节点的偏角
    	int step_distance_count;   			//每次走动的距离
    }
    
    public static class sprotsUnitCollectData {
    	double [] accel = {0.01f, 0.01f, 0.01f};
    	double [] gyro = {0.01f, 0.01f, 0.01f};
    	double [] mag = {0.01f, 0.01f, 0.01f};
    	int jump;
    	int dash;
    	int veer;
    	int adcValue;
    	int advCount;
    	
    }
    
    public static sprotsUnitCollectData sportsUnitCollectData = new sprotsUnitCollectData();
    
    public static locationData locationData = new locationData();
    //计算完成的相对位置信息0 保存相对半径，1保存相对角度
    public static double [] position_info= {0.01, 0.01};
	/*	ble4.0信标节点的物理坐标，南北向为x轴，东西方向为y轴，原点是西南角
	 */
	public static final double [][] nodeCoordinate= {
		{0.0, 0.0}, {5.5f, 14.0f}, {1.5f, 9.5f}, {9.5f, 9.5f}, {17.5f, 9.5f}, {3.3, 0.3}, {3.3, 10.0}, {3.3, 6.2}, {3.3, 0.6}, {3.3, 10.0}
	};
	
	/*	小于1m时记录当前是在那个节点下面
	 */
	public static int nodeFlags = -1, preNodeFlags = -1;
	public static int step = 0;
	/* 根据rssi的值计算的距离
	 */
	public static double distance = 0.0;
	public static double maxRssi = 0.0;
	
    /**
     * fn :		Java_com_ble_rssi_location
     * brief :	android内嵌c接口函数，实时采集基站节点的rssi的信号强度，并对其进行滤波,根据强度拟合曲线，得出距离
     * prama : 	rssi - ble4.0节点信号强度
     * 			measurePower - 需要定位的设备到ble4.0节点距离1m - 2m时的信号强度均值
     * 			n - rssi相对与距离拟合对数曲线衰减因子
     * return : 计算出的并且经过误差修正的距离
     */
    public static native double bleRssiRanging(double rssi, int measurePower, double attFactor);
    
    /**
     * fn :		Java_com_calc_relative_position
     * brief :	android内嵌c接口函数，根据磁力计计算的角度和加速计计算的距离来计算相对位置
     * prama    dev_radius - 到节点的半径
     *          dev_theta  - 上一位置相对于北方的顺时针偏角
     *          offset_radius - 相对于上一个位置得移动半径
     *          offset_theta - 当前位置相对于北方的顺时针偏角
     *          position_info[2] - {new_radius 当前位置到灯的半径
     *          					new_theta - 相对于北方的顺时针偏角}
     * return : 1 完成相对位置计算
     */
	public static native boolean calcRelativePosition(double dev_radius, double dev_theta, double offset_radius, double offset_theta, double [] position_info);
	
	/**
	 * fn:		Java_com_ble_rssi_kalmanFiltering
	 * brief:	android内嵌c接口函数，对采集的rssi进行卡尔曼滤波处理
	 * prama:	rssi - 采集到的rssi数据
	 * return:	滤波完成的信号强度
	 */
	//public static native double bleRssiKalmanFiltering(double rssi);
	
	/* 每秒钟采集次数
	 */
	private static final int COLLECTNUMBNER = 17;
	
	/* measurePower : 1m时的不同方向的rssi值
	 */
	private static final int MEAUREPOWER = -60;
	
	/* decayFactor衰减因子 
	 * (-57,25.32)、(-57,16.25)、(-58, 18.14)、(-58,22.98)
	 */
	private static final double DECAYFACTOR = 20.0;
	
	/* 	基站之间布置的距离
	 */
	private static final double NODESDISTANCE = 10.0;
	
	public static Map<Integer, List<Integer>> majorRssi1 = new HashMap<Integer, List<Integer>>();
	public static Map<Integer, Integer> majorRssiSingle = new HashMap<Integer, Integer>();
	public static Map<Integer, Double> RssiResults = new HashMap<Integer, Double>();
	private static IdRssiData data = new IdRssiData(0, 0);
	
	/*	经过滤波处理后rssi 
	 */
	public static double [] averageRssi = new double[10];
	
	/* 记录处理完成后计算坐标用到的距离
	 */
	public static double[] majorDistance = new double[10];
  	
    private static long currentTime = 0;
    
    
    //////
    static boolean isPlaying = false;
    
    static int streamId = -1;
    /*
     *	扫描获得ibeacon相应数据 
     *	参数1：BLE设备管理类
     *	参数2：读取到设备的rssi值
     *	参数3：其他扫描数据（广播包里的数据）
     */
    
    private static int preMajor = 0;
    private static Map<Integer, IdRssiData> kanmanFliterMap = new HashMap<Integer, IdRssiData>();
    
    public static iBeacon fromScanData(BluetoothDevice device, int rssi,byte[] scanData) {
    	
    	int startByte = 0;
		boolean patternFound = false;
		int tmpRssi = 0;
		
		if (((int)scanData[startByte+5] & 0xff) == 0x52 &&
			((int)scanData[startByte+6] & 0xff) == 0xA8 &&
			((int)scanData[startByte+7] & 0xff) == 0x91 &&
			((int)scanData[startByte+8] & 0xff) == 0xCF) 
		{	
			Log.e("advinfo", "dongliangkeji" + (int)scanData[startByte]); //7b
			patternFound = true;
		}

		if (patternFound == false) {
			// This is not an iBeacon
			return null;
		}
		
		sportsUnitCollectData.jump = (int)scanData[startByte+21];
		Log.e("sportsUnit", "jump" + sportsUnitCollectData.jump);
		sportsUnitCollectData.dash = (int)scanData[startByte+22];
		Log.e("sportsUnit", "dash" + sportsUnitCollectData.dash);
		sportsUnitCollectData.veer = (int)scanData[startByte+23];
		Log.e("sportsUnit", "veer" + sportsUnitCollectData.veer);
		sportsUnitCollectData.adcValue = (int)scanData[startByte+24];
		sportsUnitCollectData.advCount = (int)scanData[startByte+25];
		Log.e("sportsUnit", "advCount" + sportsUnitCollectData.advCount);
		iBeacon iBeacon = new iBeacon();
//
//		iBeacon.major = (scanData[startByte+20] & 0xff) * 0x100 + (scanData[startByte+21] & 0xff);
//		iBeacon.minor = (scanData[startByte+22] & 0xff) * 0x100 + (scanData[startByte+23] & 0xff);
//		iBeacon.txPower = (int)scanData[startByte+24]; // this one is signed
//    	data = new IdRssiData(0, rssi);
//		Log.e("rssiprint", "rssi" + iBeacon.rssi);
//		kanmanFliterMap.put(iBeacon.major, data);
//		
//		kalmanFilteringDoRssi(iBeacon.major, rssi);
//	    
//		//iBeacon.rssi = (int) (0xAF);
//		
//		iBeacon.rssi = (int) data.rssi;
//		
//		Log.e("rssiprint", "rssi" + iBeacon.rssi);
		
		
//		PedometerMediator pedometerMediator = PedometerMediator.getInstanceWithoutContext();
//		if (pedometerMediator != null) {
//			//step = pedometerMediator.getTotal();
//		}
//		step = Acceleration.Radians;
    	
		/* 	AirLocate:
		  * 02 01 1a 1a ff 4c 00 02 15  # Apple's fixed iBeacon advertising prefix
		  * e2 c5 6d b5 df fb 48 d2 b0 60 d0 f5 a7 10 96 e0 # iBeacon profile uuid
		  * 00 00 # major 
	  	  * 00 00 # minor 
	   	  * c5 # The 2's complement of the calibrated Tx Power

		  * Estimote:		
		  * 02 01 1a 11 07 2d 24 bf 16 
		  * 394b31ba3f486415ab376e5c0f09457374696d6f7465426561636f6e
	     */

		byte[] proximityUuidBytes = new byte[16];
		System.arraycopy(scanData, startByte+4, proximityUuidBytes, 0, 16); 
		String hexString = bytesToHexString(proximityUuidBytes);
		StringBuilder sb = new StringBuilder();
		sb.append(hexString.substring(0,8));
		sb.append("-");
		sb.append(hexString.substring(8,12));
		sb.append("-");
		sb.append(hexString.substring(12,16));
		sb.append("-");
		sb.append(hexString.substring(16,20));
		sb.append("-");
		sb.append(hexString.substring(20,32));
		iBeacon.proximityUuid = sb.toString();

		if(device != null)
		{
			iBeacon.bluetoothAddress = device.getAddress();
			iBeacon.name = device.getName();
		}
		
		Log.e("rssi_deal_end", "major111111 : " + iBeacon.major + " preRssi : " + data.preRssi + " rssi : " + iBeacon.rssi);
		
//		if(iBeacon.rssi < 0.0)
//		{	
//			//已简单适配手机型号：三星note3 （-63dbm）、三星i9502（-60dbm）、三星note2（-74dbm）
//			if (iBeacon.rssi >= -88) {
//				
//				if (!majorRssi1.containsKey(iBeacon.major)) {
//					majorRssi1.put(iBeacon.major, new ArrayList<Integer>());
//				} else {
//					majorRssi1.get(iBeacon.major).add(iBeacon.rssi);
//				}
//				//Log.e("kalmanLLL", " major" + iBeacon.major + "rssi" + iBeacon.rssi);
//				if (majorRssi1.get(iBeacon.major).size() >= 5) {
//					
//					nodeFlags = iBeacon.major;
//					step = 0;
//					locationData.dev_last_radius = 0.01;
//					locationData.dev_last_theta = 0.0;
//					locationData.new_radius = 0.01;
//					locationData.new_theta = 0.0;
//					locationData.step_distance_count = 0;			
//					Acceleration.clean();
//					majorRssi1.get(iBeacon.major).clear();
//							
//				}
//			} else {
//				majorRssi1.get(iBeacon.major).clear();
//			}
//		}
		
		return iBeacon;
	}
    
    public static double radius = 0;
    public static double theta = 0;
    
    private static String bytesToHexString(byte[] src){  
        StringBuilder stringBuilder = new StringBuilder("");  
        if (src == null || src.length <= 0) {  
            return null;  
        }  
        for (int i = 0; i < src.length; i++) {  
            int v = src[i] & 0xFF;  
            String hv = Integer.toHexString(v);  
            if (hv.length() < 2) {  
                stringBuilder.append(0);  
            }  
            stringBuilder.append(hv);  
        }  
        return stringBuilder.toString();  
    } 
    
    /**
     * fn		kalmanFiltingDealRssi
     * brief	对rssi进行卡尔曼滤波
     * @param major
     * 
     */
    
    static double Q = 0.001f;        // 过程方差，反应两个时刻加速度方差
    static double R = 0.1f;         // 测量方差，反应加速度的测量精度

    static double xhatminus;     // 加速度的先验估计，在k-1时刻，对k时刻加速的做出估计
    static double rssi_pre = 0;      // k-1时刻的采样值
    static double rssi_deal_end;
    static double Pminus;        // 先验估计的方差
    static double P_pre;         // 上一时刻加速度最优估计值的方差
    static double K;             // 卡尔曼增益,反应加速计测量结果与过程模型（即当前时刻与下一时刻加速计相同这一模型）的可信程度
    static double PP;            // 误差方差
	

    /**
     * fn:		kalmanFilteringDoRssi
     * brief:	对采集的rssi信号进行卡尔曼滤波处理
     * prama:	rssi - 采集到的信号强度
     * return:	rssi_deal_end - 卡尔曼滤波完成的信号强度
     */

    public static void kalmanFilteringDoRssi(double major, double rssi)
    {
    	data.rssi = rssi;
    	Log.e("rssi_deal_end", "major000000" + major + "preRssi" + data.preRssi + "rssi" + data.rssi);
    	xhatminus = data.rssi;
    	// 预测的方差为上一时刻加速度最优估计值得方差与过程方差之和
    	Pminus = P_pre + Q;
    	//卡尔曼增益,反应加速计测量结果与过程模型（即当前时刻与下一时刻加速计相同这一模型）的可信程度
    	K = Pminus / (Pminus + R);
    	//结合当前的测量值，对上一时刻的预测进行校正，得到校正后的最优估计，该估计具有最小均方差
    	data.rssi = xhatminus + K * (data.rssi - xhatminus);
    	//计算最终估计值得方差
    	PP = (1 - K) * Pminus;
    	data.preRssi = data.rssi;
    	P_pre = PP;
    }
    
    /* 对rssi进行加权均值滤波处理
     * 低功耗广播设备广播频率在20ms，每秒中可以获取近50次的rssi值，这里作为测试demo
     * 还未加入携带广播信息代码，不过影响不大
     * 参数：设备id
     */
    
    private static void MeansDoRssi(int major)
    {
    	if (majorRssi1.get(major).size() >= COLLECTNUMBNER) {
    		int sum = 0;
			List<Integer> datas = majorRssi1.get(major);
			Collections.sort(datas);
			
			for (int i = 2; i < datas.size() - 2; i++) {
				sum += datas.get(i);
			}
			double rst = (double)sum / (datas.size() - 4);
			RssiResults.put(major, rst);
		    	
	    	Set<Integer> keySet = RssiResults.keySet();
	    	for (Integer i: keySet) {
	    		//Log.i("kkkk", i + " -> " + RssiResults.get(i) + "");
	    	}
	    	majorRssi1.clear();
    	}
    }
    
   /**	对rssi进行高斯滤波处理 
    *	参数：设备id
    */   
    private static void GaussDoRssi(int major)
    {
    	int i = 0, sumRssi = 0, countGauss = 0;
    	double rst = 0.0, gaussTmp = 0.0;
    	if (majorRssi1.get(major).size() > COLLECTNUMBNER) {
    		sumRssi = 0;
			List<Integer> datas = majorRssi1.get(major);
			Collections.sort(datas);
			
			for (i = 0; i < datas.size(); i++) {
				sumRssi += datas.get(i);
			}
			rst = (double)sumRssi / ((datas.size() - 4) * 1.0);
			
			for (i = 0; i < datas.size(); i++) {
				sumRssi += Math.pow(datas.get(i) - rst, 2.0);
			}
			sumRssi = (int)Math.sqrt((1.0/(datas.size() * 1.0 - 1.0)) * sumRssi);
			
			for (i = 0; i < datas.size(); i++) {
				//概率分布在0.6到1
    			if( ((rst + 0.15 * sumRssi) <= datas.get(i)) && (datas.get(i) <= (rst + 3.09 * sumRssi)))
    			{
    				gaussTmp *= datas.get(i) * (-1.0);
    				++countGauss;
    			}
			}
			
			/** 求高斯滤波后的几何平均数
			 */
			if(countGauss != 0 && gaussTmp > 1.0)
			{
				rst = - Math.pow(gaussTmp, 1.0 / countGauss);
			}
			countGauss = 0;
    		gaussTmp = 1.0;
    		
			RssiResults.put(major, rst);
		    	
	    	Set<Integer> keySet = RssiResults.keySet();
	    	for (Integer j: keySet) {
	    		//Log.i("kkkk", i + " -> " + RssiResults.get(i) + "");
	    	}
	    	majorRssi1.clear();
    	}
    }
    
    /*	对rssi范围经验数据的匹配 
     *	参数1和2:采集后需要匹配的两个rssi
     *	参数3:范围匹配数据数组
     */
    private static int RangeMatchingRssi(double averageRssiOne, double averageRssiTwo,int [] rangeRssi)
    {
    	int rangeFlags = 0;
    	if(averageRssiOne >= rangeRssi[0] || (averageRssiTwo >= rangeRssi[3] && averageRssiTwo < rangeRssi[2]))
    		rangeFlags = 1;
    	else if((averageRssiOne >= rangeRssi[1] && averageRssiOne < rangeRssi[0]) || (averageRssiTwo >= rangeRssi[2] && averageRssiTwo < rangeRssi[1]))
    		rangeFlags = 2;
    	else if((averageRssiOne >= rangeRssi[2] && averageRssiOne < rangeRssi[1]) || (averageRssiTwo >= rangeRssi[1] && averageRssiTwo < rangeRssi[0]))
    		rangeFlags = 3;
    	else if((averageRssiOne >= rangeRssi[3] && averageRssiOne < rangeRssi[2]) || averageRssiTwo >= rangeRssi[0])
    		rangeFlags = 4;
    	return rangeFlags;
    }
    
    /*	 正太分布标准差计算，参数一是均值，参数2是个数,参数3是采集数据的数组
     */
    public double StdValue(double averValue, int n, double [][] rssiBuf)
    {
    	double tmpNum = 0.0;
    	for(int i = 0; i < n; i++)
    	{
    		tmpNum  += Math.pow((rssiBuf[i][i] - averValue), 2);
    	}
    	tmpNum = Math.sqrt((1/(n-1)) * tmpNum);
    	return tmpNum;
    }
    
    /*  java.util.Randeom 类下有个nextGaussian()方法，用于产生服从正态分布，但是nextGaussian不可重写
     *	利用中心极限定理 生成符合正太分布的随机量,
     */
    public static double NormRand(double miu, double sigma, int n)
    {
    	double x = 0, tmp = n;
		for(int i = 0; i < n; i++)
		{
			x = x + Math.random();
			x = (x - tmp/2)/(Math.sqrt(tmp / n));
			x = miu + x*Math.sqrt(sigma);
		}
    	return x;
    }
    
    /* 求两点之间的距离
     */
    public static double DistanceTwoPos(double [] pointOne, double [] pointTwo)
    {
    	return Math.sqrt((pointOne[0] - pointTwo[0]) * (pointOne[0] - pointTwo[0]) + (pointOne[1] - pointTwo[1]) * (pointOne[1] - pointTwo[1]));
    }
    
    /* 	求得高斯随机数
     */
    public void norm(int major, double rssi, double normrandMean)
    {
    	//加入高斯随机变量
    	double [] normrandXn = new double[11];	
    	double mean = 0.0;
    	int i = 0;
    	normrandXn[10] = 0.0;	
    	for(i = 0; i < 10; i++)
    	{
    		normrandXn[i] = 0.0;
    		normrandXn[i] = NormRand(0, 11.8, 10);
    		//Log.e("----------------------", "normrandXn1111111111   : "+ normrandXn[i]);
    		normrandXn[11] += normrandXn[i];
    	}
    	mean = normrandXn[10] / 10; 
    	
    //	majorRssi[major][count[major]++] = Math.pow(10, (rssi + normrandMean));
    }
    
    /* 差分修正系数确定
     * 参数1 ： 参考节点到其他信标节点的实际距离，如果正三角形布置的话这个只是固定的
     * 参数2 ： 参考节点到信标节点的计算距离，由于这个值随rssi变化且不稳定所以算多个值得平均
     * 参数3 ： 参与定位的信标节点的个数
     */
    private static double differentialCorrectionFactor(final double actualDistance, final double calculateDistance, int nodeNumber)
    {
    	int i = 0;
    	double actualDistanceCount = 1.0,  calculateDistanceCount = 1.0;
    	for(i = 0; i < nodeNumber; i++)
    	{
    		actualDistanceCount *= actualDistance;
    	}
    	
    	for(i = 0; i < nodeNumber; i++)
    	{
    		calculateDistanceCount *= calculateDistance;
    	}
    	
    	return (calculateDistanceCount / actualDistanceCount);
    }
    
    /* 	差分修正后的计算距离
     *	参数1：修正系数
     *	参数2：目标节点到信标节点的计算距离
     *	参数3：所选测距所用到的信标节点个数
     *	参数4：rssi在固定环境下的衰减因子
     */
    private static double differentialCorrectionDistance(double correctionFactor, double calculateDistance, int nodeNumber, double decayFactor)
    {
    	double correctionDistance = -1.0;
    	double componentN = -1.0;
    	componentN = (10.0 * decayFactor * Math.log10(correctionFactor)) / (nodeNumber * 1.0);
    	correctionDistance = calculateDistance * Math.pow(10.0, (-componentN) / (10.0 * decayFactor));
    	return correctionDistance;
    }
    
    /* 	非视距误差修正
     */
    private static double nlosCorrectionDistance(double calculateDistance)
    {
    	double correctionDistance = -1.0;
    	
    	return correctionDistance;
    }
    
    /* fn : 	formatDouble2
     * brief : 	保留double数小数点后num位
     * prama :	dValue - 要四舍五入的值
     * 			num - 需要保留几位小数点
     * return: 	完成后的数
     */ 
    public static double formatDouble(double dValue, int num) {
        // 旧方法，已经不再推荐使用
    	// BigDecimal bg = new BigDecimal(d).setScale(2, BigDecimal.ROUND_HALF_UP);

        
        // 新方法，如果不需要四舍五入，可以使用RoundingMode.DOWN
        BigDecimal bg = new BigDecimal(dValue).setScale(num, RoundingMode.UP);
        return bg.doubleValue();
    }
    
    public static double ToR(double x)
    {
    	return x * Math.PI / 180;
    }
    
    public static double ToA(double x)
    {
    	return x * 180 / Math.PI;
    }
    
    private static double MAP_A(double x, double y)
    {
    	return (ToA(Math.acos((y)/ Math.sqrt(x*x + y*y))));
    }

}
