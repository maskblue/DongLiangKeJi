package com.example.android.bluetoothlegatt.sensor;

import java.util.List;
import android.util.Log;

public class pedometerArithmetic {
	public static int Radians = 0;
		
	private static int setp_DG = 200;
	private static double[] DGACCELEROMETER = new double[setp_DG];
	private static double Pre_Accelerometer=0;
	private static double Pre_Pre_Accelerometer=0;
    
	private static double xhatminus;
	private static double xhat_pre=0;
	private static double xhat;
	private static double Pminus;
	private static double P_pre;
	private static double K;
	private static double P;
	private static double Q=0.05;     // Gyroscope 0.05
	private static double RR=1;     // Gyroscope 1
	private static double Accelercur = 0.0;
	
	public static int pedometerArithmeticKalman(double accelX, double accelY, double accelZ) {
		
		Accelercur = accelX * accelX + accelY * accelY + accelZ * accelZ;
		Accelercur = Math.sqrt(Accelercur);
//		Log.d("Accelercur", "" +Accelercur);
		xhat = Accelercur;
		
		xhatminus = xhat_pre;
		Pminus = P_pre+Q;
		K = Pminus/(Pminus + RR);
		xhat = xhatminus + K*(xhat-xhatminus);
		P = (1-K)*Pminus;
		
		xhat_pre = xhat;
		P_pre=P;
		
//		Log.d("xhat", "" +xhat);
		

		for (int i=0;i<setp_DG;i++)
		{
//			Log.d("change", "  before 11: " + DGACCELEROMETER[i]);
			if(Math.abs(Pre_Pre_Accelerometer-xhat) < 0.2)   //0.8 0.40
			{
				break;
			}
			if (DGACCELEROMETER[i] == 0)
				{
					DGACCELEROMETER[i] = Pre_Pre_Accelerometer-xhat;
					//Log.d("change", "  before : " + DGACCELEROMETER[i]);
					break;
				}
		}
		
		int DG_step = 0;
		for (int i=0;i<setp_DG;i++)
		{
			if (DGACCELEROMETER[i] < 0)
			{
				for (int j=i+1;j<setp_DG;j++)
				{
					if (DGACCELEROMETER[j] > 0)
					{
						for (int k=j+1;k<setp_DG;k++)
						{
							if (DGACCELEROMETER[k] < 0)
							{
								DG_step=1;
//								Log.e("DG_step", "DG_step" + DG_step);
								DGACCELEROMETER[0]=DGACCELEROMETER[k];
								for(int m=1;m<setp_DG;m++)
								{
									DGACCELEROMETER[m] = 0;
								}
								break;
							}
						}
					}
				}
			}
		}
		
		Radians=Radians+DG_step;
		Pre_Pre_Accelerometer=Pre_Accelerometer;
		Pre_Accelerometer=xhat;
		return Radians;
	}
	
	public static void clean() {
		Radians = 0;
	}
}