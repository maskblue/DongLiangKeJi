/*
 *  Copyright (C) 2015  U-beacon
 *
 *       Filename:          calc_relative_location.c
 *        Author :          Hewu Hai
 *         Email :          zhanhai163@163.com
 * Last modified :          2015-3-24 10:01
 *   Description :			根据角度和距离计算相对于节点的位置
 *
 */

#include <stdio.h>
#include <string.h>
#include <stdlib.h>
#include "common.hpp"
#include "math.h"
#include "calc_relative_location.h"

#define					PI 3.1415926535

#define ToR(x)  		((x) * PI / 180.0)		//转换为弧度
#define ToA(x) 			((x)*180/3.1415926)			//转换为角度
#define MAP_A(x,y) 		( ToA( acos((y)/sqrt( (x)*(x) + (y)*(y))) ) )

/**
 * fn       move_calc_
 * brief    将得到的步数和指南针角度转化为相对于灯的角度和半径
 * prama    dev_radius - 到节点的半径
 *          dev_theta  - 上一位置相对于北方的顺时针偏角
 *          offset_radius - 相对于上一个位置得移动半径
 *          offset_theta - 当前位置相对于北方的顺时针偏角
 *          new_radius - 当前位置到灯的半径
 *          new_theta - 相对于北方的顺时针偏角
 * return   转换完成返回1
 */
int move_calc_relative_location(double dev_radius, double dev_theta, \
		double offset_radius, double offset_theta, double *new_radius, double *new_theta)
{
    float new_x, new_y;
    new_x = dev_radius * sin(ToR(dev_theta));
    new_y = dev_radius * cos(ToR(dev_theta));

    new_x += offset_radius * sin(ToR(offset_theta));
    new_y += offset_radius * cos(ToR(offset_theta));

    *new_radius= sqrt( new_x*new_x + new_y*new_y);
    *new_theta = MAP_A(new_x, new_y);
    if(new_x < 0)
        *new_theta = 360.0f - *new_theta;

   // DBG_LOG("new_radius = %.3f" , new_radius);
    //DBG_LOG("new_radius = %.3f" , new_theta);
    return 1;
}
