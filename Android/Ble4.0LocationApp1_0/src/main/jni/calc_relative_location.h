/*
 *  Copyright (C) 2015  U-beacon
 *
 *       Filename:          calc_relative_location.h
 *        Author :          Hewu Hai
 *         Email :          zhanhai163@163.com
 * Last modified :          2015-3-24 10:01
 *   Description :			根据角度和距离计算相对于节点的位置
 *
 */

#ifndef _CALC_RELATIVE_LCOATION_H_
#define _CALC_RELATIVE_LCOATION_H_

extern int move_calc_relative_location(double dev_radius, double dev_theta, \
		double offset_radius, double offset_theta, double *new_radius, double *new_theta);

#endif
