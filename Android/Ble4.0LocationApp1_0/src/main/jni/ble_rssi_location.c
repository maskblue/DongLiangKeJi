/*
 *  Copyright (C) 2015  U-beacon
 *
 *       Filename:          ble_rssi_location.c
 *        Author :          Hewu Hai
 *         Email :          zhanhai163@163.com
 * Last modified :          2015-3-23 14:20
 *   Description :			基于蓝牙的定位引擎jni封装库
 *
 */

#include <stdlib.h>
#include <string.h>
#include <stdio.h>
#include <assert.h>
#include <math.h>

#include <jni.h>
#include "ble_rssi_location.h"
#include "calc_relative_location.h"
#include "rssi_deal.h"
#include "common.hpp"

#define JNIREG_CLASS "com/example/android/bluetoothlegatt/iBeaconClass"

/**
 * fn :		Java_com_ble_rssi_location
 * brief :	android内嵌c接口函数，实时采集基站节点的rssi的信号强度，并对其进行滤波,根据强度拟合曲线，得出距离
 * prama : 	rssi - ble4.0节点信号强度和对应id号
 * 			measurePower - 需要定位的设备到ble4.0节点距离1m - 2m时的信号强度均值
 * 			n - rssi相对与距离拟合对数曲线衰减因子
 * return : 计算出的并且经过误差修正的距离
 */

static jdouble Java_com_ble_rssi_ranging( JNIEnv*  env, jobject  thiz, jdouble  rssi, jint measurePower, jdouble n)
{
	double distance = -1.0;
	//滤波处理
	//dealEndRssi = meansDoRssi(bleNodeInfo, collectNum);
	//dealEndRssi = gaussDoRssi(bleNodeInfo, collectNum);

	//对数拟合曲线测距离
	distance = calcDistance(rssi, measurePower, n);
	return distance;
}

/**
 * fn :		Java_com_calc_relative_position
 * brief :	android内嵌c接口函数，根据磁力计计算的角度和加速计计算的距离来计算相对位置
 * prama    dev_radius - 到节点的半径
 *          dev_theta  - 上一位置相对于北方的顺时针偏角
 *          offset_radius - 相对于上一个位置得移动半径
 *          offset_theta - 当前位置相对于北方的顺时针偏角
 *          new_radius - 当前位置到灯的半径
 *          new_theta - 相对于北方的顺时针偏角
 * return : 1 完成相对位置计算
 */

static jboolean Java_com_calc_relative_position(JNIEnv* env, jobject thiz, jdouble dev_radius, jdouble dev_theta, jdouble offset_radius, \
		jdouble offset_theta, jdoubleArray position_info)
{
	double *pPosition_info = (*env)->GetDoubleArrayElements(env, position_info, NULL);
	move_calc_relative_location(dev_radius, dev_theta, offset_radius, offset_theta, &pPosition_info[0], &pPosition_info[1]);
	(*env)->ReleaseDoubleArrayElements(env, position_info, pPosition_info, 0);
	return 1;
}

/**
 * fn:		Java_com_ble_rssi_kalmanFiltering
 * brief:	android内嵌c接口函数，对采集的rssi进行卡尔曼滤波处理
 * prama:	rssi - 采集到的rssi数据
 * return:	滤波完成的信号强度
 */

/*static jdouble Java_com_ble_rssi_kalmanFiltering( JNIEnv*  env, jobject  thiz, jdouble  rssi)
{
	double rssi_deal_result = 0.0;
	//卡尔曼滤波处理
	rssi_deal_result = kalmanFilteringDoRssi(rssi);
	return rssi_deal_result;
}*/

static JNINativeMethod method_table[] =
{
    { "bleRssiRanging", "(DID)D", (void*)Java_com_ble_rssi_ranging},
	{ "calcRelativePosition", "(DDDD[D)Z", (void*)Java_com_calc_relative_position},
	/*{ "bleRssiKalmanFiltering", "(D)D", (void*)Java_com_ble_rssi_kalmanFiltering},*/
};


static int registerNativeMethods(JNIEnv* env, const char* className,
        JNINativeMethod* gMethods, int numMethods)
{
    jclass clazz;
    clazz = (*env)->FindClass(env, className);
    if (clazz == NULL) {
        return JNI_FALSE;
    }
    if ((*env)->RegisterNatives(env, clazz, gMethods, numMethods) < 0) {
        return JNI_FALSE;
    }

    return JNI_TRUE;
}

static int register_ndk_load(JNIEnv *env)
{

    return registerNativeMethods(env, JNIREG_CLASS,
            method_table, sizeof(method_table)/sizeof(method_table[0]));
}

JNIEXPORT jint JNI_OnLoad(JavaVM* vm, void* reserved)
{
    JNIEnv* env = NULL;
    jint result = -1;

    if ((*vm)->GetEnv(vm, (void**) &env, JNI_VERSION_1_4) != JNI_OK)
	{
        return result;
    }

    register_ndk_load(env);

    return JNI_VERSION_1_4;
}
