/*
 *  Copyright (C) 2015  U-beacon
 *
 *       Filename:          rssi_deal.h
 *        Author :          Hewu Hai
 *         Email :          zhanhai163@163.com
 * Last modified :          2015-3-23 17:49
 *   Description :			rssi滤波处理和基于rssi和距离关系拟合模型的测距
 *
 */

#ifndef _RSSI_DEAL_H_
#define _RSSI_DEAL_H_

double meansDoRssi(double * bleNodeInfo, int len);
double gaussDoRssi(double * bleNodeInfo, int len);
double calcDistance(double rssi, double measurePower, double n);
void bubblesort(double *data, int len);
double kalmanFilteringDoRssi(double rssi);

#endif
