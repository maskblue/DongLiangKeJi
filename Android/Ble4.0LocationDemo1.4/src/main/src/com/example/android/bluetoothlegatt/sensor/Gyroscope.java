package com.example.android.bluetoothlegatt.sensor;

import java.util.List;

import android.util.Log;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

public class Gyroscope{
	//private static final double INVERSE_PRECISION = 80.0;
	private static final double INVERSE_PRECISION = 50.0;
	//private static final double INVERSE_PRECISION = 20.0;
	//private static final double INVERSE_PRECISION = 1.0/0.0003;
	//private static final double INVERSE_PRECISION = 1000.0;

	public static float cur_x_save = (float)0.0;
	public static float cur_y_save = (float)0.0;;
	public static float cur_z_save = (float)0.0;;
	public static float pose_save[] = new float[3];;
	
	public static float cur_x = (float)0.0;
	public static float cur_y = (float)0.0;;
	public static float cur_z = (float)0.0;;
	
	public static boolean positive_x=true;
	public static boolean positive_y=true;
	public static boolean positive_z=true;
	
	public static float error_x=(float)0.0;
	public static float error_y=(float)0.0;
	public static float error_z=(float)0.0;

	public static float read_x_save;
	public static float read_y_save;
	public static float read_z_save;
	
	public static float ebcel_x=(float)0.0;
	public static float ebcel_y=(float)0.0;
	public static float ebcel_z=(float)0.0;
	
	public static float sum_dx =(float)0.0;
	public static float sum_dy =(float)0.0;
	public static float sum_dz =(float)0.0;

    public static double radius;
    public static double theta;
	
    private static boolean isRegisteredSensor = false;
    private static SensorManager sensorManager = null;
	private static long timestamp=0;
    private static boolean isConfigGyroscrope = false;
	
//    public static native boolean gyroscopeInit(double a_x,	 double a_y,	double a_z );
//    public static native boolean gyroscopeOrientation(double w_x,	 double w_y,	double w_z, double	a_xyz[]);
	
    public static void start(SensorManager sm) {
		
//	    System.loadLibrary("gyroscope_orientation");
		
		sensorManager = sm;
		
		List<Sensor> sensors = sensorManager.getSensorList(Sensor.TYPE_GYROSCOPE);
		
		if (sensors != null && sensors.size() > 0) 
		{
			Sensor sensor = sensors.get(0);
			isRegisteredSensor = sensorManager.registerListener(myListener, sensor,SensorManager.SENSOR_DELAY_GAME/*SENSOR_DELAY_NORMAL*//*SENSOR_DELAY_GAME SENSOR_DELAY_FASTEST*/);
			 
		}
    }

	public static void stop() {
		if (isRegisteredSensor) {
			sensorManager.unregisterListener(myListener);
			isRegisteredSensor = false;
		}
    }
	
	public static void config(float x,float y,float z) {
		//if(isConfigGyroscrope)
		//	return;
		
		//gyroscopeInit(0,0,0 );
		
		//cur_x = x;
		//cur_y = y;
		//cur_z = z;

		isConfigGyroscrope = true;
    }

    //-------------------------------------------
    //��������������ط���
	final private static SensorEventListener myListener = new SensorEventListener() { 
	
	    @Override
	    public void onAccuracyChanged(Sensor sensor, int accuracy) {
			//Log.e("Gyroscope", "-----------------------accuracy=" + accuracy);

	    }
		///*
	    @Override
	    public void onSensorChanged(SensorEvent event) {
			synchronized (this) 
			{
				double read_x;
				double read_y;
				double read_z;
				
			    read_x = event.values[SensorManager.DATA_X];
			    read_y = event.values[SensorManager.DATA_Y];
			    read_z = event.values[SensorManager.DATA_Z];
				double	a_xyz[] = new double[3];
				
				//gyroscopeOrientation(read_x,	 read_y,	read_z, a_xyz);
				
				cur_x = event.values[SensorManager.DATA_X];
				cur_y = event.values[SensorManager.DATA_Y];
				cur_z = event.values[SensorManager.DATA_Z];
/*
			    float pose[]=new float[3];
				if(timestamp == 0)
				{
				    pose_save = Orientation.get_pose();
					timestamp = event.timestamp; 
					return;
				}
				

				long dT = (event.timestamp - timestamp)/1000000;//ms
				float toAngle = (float)(180/3.1415926);
				toAngle = (float)( toAngle*(event.timestamp - timestamp)/1000000.0/1000);//11
                //toAngle = (float)1.0;
				//dT = (float)1.0;
				float read_x;
				float read_y;
				float read_z;
				
				float read_r;
				float read_alpha;
				float read_beta;
				float read_gama;
				float read_angle;
				
			    pose = Orientation.get_pose();


			    read_x = event.values[SensorManager.DATA_X];
			    read_y = event.values[SensorManager.DATA_Y];
			    read_z = event.values[SensorManager.DATA_Z];
				double	a_xyz[] = new double[3];
				
				gyroscopeOrientation(read_x,	 read_y,	read_z, a_xyz);
				
				if(Math.abs(pose[2]) >20 || Math.abs(pose[1]) >20)
				{
					read_x = 0;
					read_y = 0;
					read_z = 0;
					//Log.e("Gyroscope", "not support-----------------------"+",y=" + pose[1] + ", z=" + pose[2]);
				}
				
				read_x =  (float)( ((int)(read_x*INVERSE_PRECISION))/INVERSE_PRECISION );
				read_y =  (float)( ((int)(read_y*INVERSE_PRECISION))/INVERSE_PRECISION );
				read_z =  (float)( ((int)(read_z*INVERSE_PRECISION))/INVERSE_PRECISION );
				
				read_r     = (float)Math.sqrt(read_x*read_x + read_y*read_y + read_z*read_z );
				
			    read_x = (float) ( (pose[2]-pose_save[2])/(event.timestamp - timestamp)/1000000.0/1000 );
			    read_y = (float) ( (pose[1]-pose_save[1])/(event.timestamp - timestamp)/1000000.0/1000 );
			    //read_z = (float) ( (pose[0]-pose_save[0])/(event.timestamp - timestamp)/1000000.0/1000 );
			    read_z = (float)( Math.sqrt(read_r*read_r - read_x*read_x - read_y*read_y )*(read_z>0?1.0:-1.0) );

				if(Math.abs(pose[2]) >20 || Math.abs(pose[1]) >20)
				{
					read_x = 0;
					read_y = 0;
					read_z = 0;
				}
				
			    //read_z = (float)( Math.sqrt(read_r*read_r - read_x*read_x - read_y*read_y ) );
				//Log.e("Gyroscope", "-----------------------"+"read_r="+ read_r+",x=" + read_z + ", y=" + read_y+",z="+read_x);

				
				//read_x =  (float)( ((int)(read_x*INVERSE_PRECISION))/INVERSE_PRECISION );
				//read_y =  (float)( ((int)(read_y*INVERSE_PRECISION))/INVERSE_PRECISION );
				//read_z =  (float)( ((int)(read_z*INVERSE_PRECISION))/INVERSE_PRECISION );

				read_r     = (float)Math.sqrt(read_x*read_x + read_y*read_y + read_z*read_z );
				read_alpha = (float)Math.asin(read_z/read_r)*toAngle;
				read_beta  = (float)Math.asin(read_y/read_r)*toAngle;
				read_gama  = (float)Math.asin(read_x/read_r)*toAngle;
				read_angle = read_alpha+read_beta+read_gama;
				
                positive_x = read_x>=0?true:false;
                positive_y = read_y>=0?true:false;
                positive_z = read_z>=0?true:false;
				
				ebcel_x =(float)( (read_x-read_x_save)/(event.timestamp - timestamp)/1000000.0/1000);
				ebcel_y =(float)( (read_y-read_y_save)/(event.timestamp - timestamp)/1000000.0/1000);
				ebcel_z =(float)( (read_z-read_z_save)/(event.timestamp - timestamp)/1000000.0/1000);
				float resolution = event.sensor.getResolution();
				
				if(( // true
					  Math.abs(read_r)>=1e-2 
					  // Math.abs(ebcel_z)>=1e-18 
					  //&&Math.abs(ebcel_z)<=1e-16
					//&&Math.abs(ebcel_y)>=1e-20 
					//&&Math.abs(ebcel_x)>=1e-20
					)&&( true//Math.abs(read_z)>=10e-3 
					   //&&Math.abs(read_z)<=9
					    )
					)
				{	
					read_x =  (float)( ((int)(read_x*INVERSE_PRECISION))/INVERSE_PRECISION );
					read_y =  (float)( ((int)(read_y*INVERSE_PRECISION))/INVERSE_PRECISION );
					read_z =  (float)( ((int)(read_z*INVERSE_PRECISION))/INVERSE_PRECISION );
					
					cur_x += -read_z*toAngle;
					cur_y += -read_y*toAngle;
					cur_z += -read_x*toAngle;
					cur_x = cur_x>0? cur_x-(int)(cur_x/360)*360 :(360+cur_x)-(int)((360+cur_x)/360)*360;
					cur_y = cur_y>0? cur_y-(int)(cur_y/360)*360 :(360+cur_y)-(int)((360+cur_y)/360)*360;
					cur_z = cur_z>0? cur_z-(int)(cur_z/360)*360 :(360+cur_z)-(int)((360+cur_z)/360)*360;
					
	                error_x = 0;//(float)(5/90.0*cur_x)*dT/1000;
	                error_y = 0;//(float)(5/90.0*cur_y)*dT/1000;
	                error_z = 0;//(float)(5/90.0*cur_z)*dT/1000;

	                cur_x += positive_x==true?-error_x:error_x;
	                cur_y += positive_y==true?-error_y:error_y;
	                cur_z += positive_z==true?-error_z:error_z;
					
					float dx = cur_x-cur_x_save;
					float dy = cur_y-cur_y_save;
					float dz = cur_z-cur_z_save;
					sum_dx +=dx;
					sum_dy +=dy;
					sum_dz +=dz;
				}
				//cur_y -= (int)(cur_y/360)*360;
				//cur_z -= (int)(cur_z/360)*360;
				//Log.e("Gyroscope", "-----------------------resolution="+resolution+",x=" + ebcel_x + ", y=" + ebcel_y+",z="+ebcel_z);
				//Log.e("Gyroscope", "-----------------------"+",x=" + read_z + ", y=" + read_y+",z="+read_x);


				Log.e("Gyroscope", "-----------------------"+",x=" + cur_x + ", y=" + cur_y+",z="+cur_z);


				//Log.e("Gyroscope", "-----------------------"+",read_r="+ read_r+",read_angle="+ read_angle+",x=" + read_alpha + ", y=" + read_beta+",z="+read_gama);
				cur_x_save = cur_x;
				cur_y_save = cur_y;
				cur_z_save = cur_z;
				
				read_x_save = read_x;
				read_y_save = read_y;
				read_z_save = read_z;
				pose_save   = pose;
 				timestamp   = event.timestamp; 
 */
			}
	    }
    }; 
	
}


/*
��������һ��ϵͳ��
��������������ʱ���¶Ȳ�ֵ�ı�׼��Ϊ0.02��
�¶ȼƵĲ���ֵ���ı�׼��Ϊ0.5��
�����¶ȵ���ʵֵΪ24��
���¶ȵĳ�ʼ����ֵΪ23.5�ȣ����ķ���Ϊ1
MatLab����Ĵ������£�
% Kalman filter example of temperature measurement in Matlab
% This M code is modified from Xuchen Yao's matlab on 2013/4/18
%���䵱ǰ�¶���ʵֵΪ24�ȣ���Ϊ��һʱ���뵱ǰʱ���¶���ͬ�����Ϊ0.02�ȣ�����Ϊ����������ʱ�����仯0.02�ȣ���
%�¶ȼƵĲ������Ϊ0.5�ȡ�
%��ʼʱ�������¶ȵĹ���Ϊ23.5�ȣ����Ϊ1�ȡ�
% Kalman filter example demo in Matlab
% This M code is modified from Andrew D. Straw's Python
% implementation of Kalman filter algorithm.
% The original code is from the link in references[2] 
% Below is the Python version's comments:
% Kalman filter example demo in Python
% A Python implementation of the example given in pages 11-15 of "An
% Introduction to the Kalman Filter" by Greg Welch and Gary Bishop,
% University of North Carolina at Chapel Hill, Department of Computer
% Science, TR 95-041,
[3] % by Andrew D. Straw
% by Xuchen Yao
% by Lin Wu
clear all;
close all;
% intial parameters
n_iter = 100; %��������n_iter��ʱ��
sz = [n_iter, 1]; % size of array. n_iter�У�1��
x = 24; % �¶ȵ���ʵֵ
Q = 4e-4; % ���̷�� ��Ӧ��������ʱ���¶ȷ�����Ĳ鿴Ч��
R = 0.25; % ���������Ӧ�¶ȼƵĲ������ȡ����Ĳ鿴Ч��
z = x + sqrt(R)*randn(sz); % z���¶ȼƵĲ������������ʵֵ�Ļ����ϼ����˷���Ϊ0.25�ĸ�˹������
% ��������г�ʼ��
xhat=zeros(sz); % ���¶ȵĺ�����ơ�����kʱ�̣�����¶ȼƵ�ǰ����ֵ��k-1ʱ��������ƣ��õ������չ���ֵ
P=zeros(sz); % ������Ƶķ���
xhatminus=zeros(sz); % �¶ȵ�������ơ�����k-1ʱ�̣���kʱ���¶������Ĺ���
Pminus=zeros(sz); % ������Ƶķ���
K=zeros(sz); % ���������棬��Ӧ���¶ȼƲ�����������ģ�ͣ�����ǰʱ������һʱ���¶���ͬ��һģ�ͣ��Ŀ��ų̶�
% intial guesses
xhat(1) = 23.5; %�¶ȳ�ʼ����ֵΪ23.5��
P(1) =1; %����Ϊ1
for k = 2:n_iter
% ʱ����£�Ԥ�⣩
xhatminus(k) = xhat(k-1); %����һʱ�̵����Ź���ֵ����Ϊ�Ե�ǰʱ�̵��¶ȵ�Ԥ��
Pminus(k) = P(k-1)+Q; %Ԥ��ķ���Ϊ��һʱ���¶����Ź���ֵ�ķ�������̷���֮��
% �������£�У����
K(k) = Pminus(k)/( Pminus(k)+R ); %���㿨��������
xhat(k) = xhatminus(k)+K(k)*(z(k)-xhatminus(k)); %��ϵ�ǰʱ���¶ȼƵĲ���ֵ������һʱ�̵�Ԥ�����У�����õ�У��������Ź��ơ��ù��ƾ�����С������
P(k) = (1-K(k))*Pminus(k); %�������չ���ֵ�ķ���
end
FontSize=14;
LineWidth=3;
figure();
plot(z,'k+'); %�����¶ȼƵĲ���ֵ
hold on;
plot(xhat,'b-','LineWidth',LineWidth) %�������Ź���ֵ
hold on;
plot(x*ones(sz),'g-','LineWidth',LineWidth); %������ʵֵ
legend('�¶ȼƵĲ������', '�������', '��ʵֵ');
xl=xlabel('ʱ��(����)');
yl=ylabel('�¶�');
set(xl,'fontsize',FontSize);
set(yl,'fontsize',FontSize);
hold off;
set(gca,'FontSize',FontSize);
figure();
valid_iter = [2:n_iter]; % Pminus not valid at step 1
plot(valid_iter,P([valid_iter]),'LineWidth',LineWidth); %�������Ź���ֵ�ķ���
legend('������Ƶ�������');
xl=xlabel('ʱ��(����)');
yl=ylabel('��^2');
set(xl,'fontsize',FontSize);
set(yl,'fontsize',FontSize);
set(gca,'FontSize',FontSize);
*/

