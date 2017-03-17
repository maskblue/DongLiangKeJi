#if !defined(COMMON_HPP)
#define COMMON_HPP

#define DEBUG_OS_ANDROID 1
#define DEBUG_OS_IOS 0
#define DEBUG_OS_WIN 0

#include <stdio.h>
#include <string.h>
#include <stdlib.h>
#include <math.h>
#include <jni.h>
#if DEBUG_OS_ANDROID
#include <android/log.h>
#endif



#define SIZE_A(a) (sizeof(a)/sizeof(a[0]))

#define PA(a,w,h,width) ((a)+(width)*(h)+(w))
#define PAV(a,w,h,width) (*((a)+(width)*(h)+(w)))

#define MEM_ALLOC(size,type) (type *)calloc(1,(size)*sizeof(type))
#define MEM_FREE2(ptr) do{if(ptr){ /*(DBG_LOG("\n");*/free((void*)(ptr));(ptr) = 0;}else {/*DBG_WAR("\n");*/} }while(0)
#define MEM_CPY(a,b,size) memcpy(a,b,size)

#if ( DEBUG_OS_WIN ==1 ) || ( DEBUG_OS_IOS == 1)

#define DBG_LOG(format,arg...) printf("[LOG][%s:%d]" format,__FUNCTION__,__LINE__,##arg)
#define DBG_WAR(format,arg...) printf("[WAR][%s:%d]" format,__FUNCTION__,__LINE__,##arg)
#define DBG_ERR(format,arg...) printf("[ERR][%s:%d]" format,__FUNCTION__,__LINE__,##arg)
#define DBG_FAT(format,arg...) printf("[FAT][%s:%d]" format,__FUNCTION__,__LINE__,##arg)

#elif ( DEBUG_OS_ANDROID ==1 )

#define DBG_LOG(format,arg...) do{\
	char tmp_buffer[512]={0,};\
	sprintf(tmp_buffer,"[%s:%d]"format,__FUNCTION__,__LINE__,##arg);\
	__android_log_write(ANDROID_LOG_INFO,"LIGHTID",tmp_buffer);\
	}while(0)

#define DBG_WAR(format,arg...) do{\
	char tmp_buffer[512]={0,};\
	sprintf(tmp_buffer,"[%s:%d]"format,__FUNCTION__,__LINE__,##arg);\
	__android_log_write(ANDROID_LOG_WARN,"LIGHTID",tmp_buffer);\
	}while(0)
	
#define DBG_ERR(format,arg...) do{\
		char log_buffer[512]={0,};\
		sprintf(tmp_buffer,"[%s:%d]"format,__FUNCTION__,__LINE__,##arg);\
		__android_log_write(ANDROID_LOG_ERROR,"LIGHTID",log_buffer);\
		}while(0)
		
#define DBG_FAT(format,arg...) do{\
		char tmp_buffer[512]={0,};\
		sprintf(tmp_buffer,"[%s:%d]"format,__FUNCTION__,__LINE__,##arg);\
		__android_log_write(ANDROID_LOG_FATAL,"LIGHTID",tmp_buffer);\
		}while(0)

#endif


#define MAX(a,b) ((a)>=(b)?(a):(b))
#define MIN(a,b) ((a)<=(b)?(a):(b))
#endif
