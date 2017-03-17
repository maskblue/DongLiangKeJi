/*
 *  Copyright (C) 2015  U-beacon
 *
 *       Filename:          rssi_deal.c
 *        Author :          Hewu Hai
 *         Email :          zhanhai163@163.com
 * Last modified :          2015-3-23 17:49
 *   Description :			rssi滤波处理和基于rssi和距离关系拟合模型的测距
 *
 */

#include <stdio.h>
#include <math.h>
#include <stdlib.h>
#include <string.h>
#include "rssi_deal.h"
#include "common.hpp"

#define DBG_KALMAN_FILTER	1

#if DBG_KALMAN_FILTER

static double Q = 0.001f;        // 过程方差，反应两个时刻加速度方差
static double R = 0.1f;         // 测量方差，反应温度计的测量精度

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

double kalmanFilteringDoRssi(double rssi)
{
	rssi_deal_end = rssi;
	xhatminus = rssi_pre;
	// 预测的方差为上一时刻加速度最优估计值得方差与过程方差之和
	Pminus = P_pre + Q;
	//卡尔曼增益,反应加速计测量结果与过程模型（即当前时刻与下一时刻加速计相同这一模型）的可信程度
	K = Pminus / (Pminus + R);
	//结合当前的测量值，对上一时刻的预测进行校正，得到校正后的最优估计，该估计具有最小均方差
	rssi_deal_end = xhatminus + K * (rssi_deal_end - xhatminus);
	//计算最终估计值得方差
	PP = (1 - K) * Pminus;
	rssi_pre = rssi_deal_end;
	DBG_LOG("rssi_deal_end = %.3f rssi_pre = %.3f" , rssi_deal_end, rssi_pre);
	P_pre = PP;

	return rssi_deal_end;

}

#endif

/**
 * fn:		meansDoRssi
 * brief：	对采集的rssi信号进行加权均值滤波
 * prama：	bleNodeInfo - 采集的id和rssi信息
 * return: 	dealEndRssi - 处理完成的rssi信息
 */

double meansDoRssi(double * bleNodeInfo, int len)
{
	double rssiTmp = 0.0, dealEndRssi = 0.0;;
	int i = 0, actualCollectNumber = 0;

	bubblesort(bleNodeInfo, len);

	for(i = 2; i < len -2; i++)
	{
		rssiTmp += bleNodeInfo[i];
		actualCollectNumber++;
		bleNodeInfo[i] = 0.0;
	}
	dealEndRssi = rssiTmp / (actualCollectNumber * 1.0);

	rssiTmp = 0.0;
	return dealEndRssi;
}

/**
 * fn:		gaussDoRssi
 * brief：	对采集的rssi信号进行加权均值滤波
 * prama：	bleNodeInfo - 采集的id和rssi信息
 * return: 	dealEndRssi - 处理完成的rssi信息
 */

double gaussDoRssi(double * bleNodeInfo, int len)
{
	double rssiTmp = 0.0, dealEndRssi = 0.0, gaussTmp = 1.0;
	int i = 0, actualCollectNumber = 0, countGauss = 0;

	bubblesort(bleNodeInfo, len);
//求均值
	for(i = 0; i < len; i++)
	{
		rssiTmp += bleNodeInfo[i];
		bleNodeInfo[i] = 0.0;
		++actualCollectNumber;
	}

	dealEndRssi = rssiTmp / (actualCollectNumber * 1.0);
	rssiTmp = 0.0;
	//求标准差
	for(i = 0; i < len; i++)
		rssiTmp += pow(bleNodeInfo[i] - dealEndRssi, 2.0);

	rssiTmp = sqrt((1.0 / (len * 1.0 - 1.0)) * rssiTmp);
	for(i = 0; i < len; i++)
	{
		if((dealEndRssi + 0.15 * rssiTmp) <= bleNodeInfo[i]  && bleNodeInfo[i] <= (dealEndRssi + 3.09 * rssiTmp))
		{
			gaussTmp *= bleNodeInfo[i] * (-1.0);
			++countGauss;
		}
		bleNodeInfo[i] = 0.0;
	}
	// 求高斯滤波后的几何平均数
	if(countGauss != 0 && gaussTmp > 1.0)
		dealEndRssi = -pow(gaussTmp, 1.0 / countGauss);
	countGauss = 0;
	gaussTmp = 1.0;
	rssiTmp = 0;
	return dealEndRssi;
}

/**
 * fn:		calcDistance
 * brief：	对滤波后的rssi进行拟合测距
 * prama：	rssi - 进行滤波之后的信号强度
 * 			measurePower - 1m初测试信号的强度，是一个经过处理的校准值
 * 			n - 信号在不同环境下的衰减因子
 * return:  结算完成的距离
 */
double calcDistance(double rssi, double measurePower, double n)
{
	return pow(10.0, (measurePower - rssi*1.0) / n) + 0.02;;
}


/**
 *  fn:			bubblesort
 *  brief:		冒泡法排序
 *  prama:		data - 需要排序的数组地址
 *  			len - 数据的实际长度
 */
void bubblesort(double *data, int len)
{
	int i= 0, j = 0, flag = 1, count;
	double rssi_tmp = 0.0;
	for(i = 0; i < len && flag ==1; i++)
	{
		for(j = 0; j < len - i - 1; j++)
		{
			count++;
			if(data[j] < data[j+1])
			{
				rssi_tmp = data[j];
				data[j] = data[j+1];
				data[j+1] = data[j];
				flag = 1;
			}
		}
	}
}
