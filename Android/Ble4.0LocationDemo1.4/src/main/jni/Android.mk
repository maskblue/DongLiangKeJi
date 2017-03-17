# Copyright (C) 2009 The Android Open Source Project
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#

# the purpose of this sample is to demonstrate how one can
# generate two distinct shared libraries and have them both
# uploaded in
#

LOCAL_PATH:= $(call my-dir)

include $(CLEAR_VARS)
#编译目标对象，以标示在Android.mk文件中描述的每个模块
LOCAL_MODULE    := bleRssiRanging 
#必须包含要编译打包进模块的c或c++源代码文件
LOCAL_SRC_FILES := ble_rssi_location.c rssi_deal.c calc_relative_location.c 
LOCAL_STATIC_LIBRARIES := bleRssiLocation
LOCAL_LDLIBS += -L$(SYSROOT)/usr/lib -llog 
#编译生成的共享库
include $(BUILD_SHARED_LIBRARY)

