package com.cainiao.util;


import android.content.Context;
import android.provider.Settings;

public class DeviceUtil {
  //设备号
  private String android;

  private DeviceUtil() {}

  static class DeviceUtilHolder {
    static DeviceUtil INSTANCE = new DeviceUtil();
  }

  public static DeviceUtil getInstance() {
    return DeviceUtilHolder.INSTANCE;
  }

  public void init(Context context) {
    try {

      android= Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
    } catch (Throwable e) {
      e.printStackTrace();
    }
  }


  public String getDeviceId() {
    return android;
  }


}