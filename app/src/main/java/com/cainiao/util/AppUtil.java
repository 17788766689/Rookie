package com.cainiao.util;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.support.v4.content.FileProvider;
import android.text.TextUtils;

import com.cainiao.R;
import com.cainiao.base.MyApp;
import com.okhttplib.HttpInfo;
import com.okhttplib.callback.ProgressCallback;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class AppUtil {

    private static String APK_PATH;
    private static final String APK_NAME = "cainiao.apk";


    /**根据包名判断apk是否已安装*/
    public static boolean isInstalled(String packagename) {
        PackageManager localPackageManager = MyApp.getContext().getPackageManager();
        try {
            localPackageManager.getPackageInfo(packagename, PackageManager.GET_UNINSTALLED_PACKAGES);
            return true;
        } catch (PackageManager.NameNotFoundException localNameNotFoundException) {
            return false;
        }
    }

    /**
     * 开启一个应用程序，开启功能
     */
    public static void startApp(String packname) {
        PackageManager pm = MyApp.getContext().getPackageManager();
        Intent intent = pm.getLaunchIntentForPackage(packname);
        if (intent != null) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            MyApp.getContext().startActivity(intent);
        }
    }

    /**
     * 使用外部浏览器打开url
     * @param url
     */
    public static void openUrlInOuter(String url){
        if(TextUtils.isEmpty(url)) return;
        MyApp.getContext().startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
    }

    /**
     * 获取当前版本号
     *
     * @param context
     * @return
     */
    public static int getVersionCode(Context context) {
        int version = 0;
        try {
            PackageManager pm = context.getPackageManager();
            PackageInfo packageInfo = pm.getPackageInfo(context.getPackageName(), 0);
            version = packageInfo.versionCode;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return version;
    }


    /**
     * 下载APK
     * @param url
     * @param cancelable
     */
    public static void downloadApk(final Activity aty, String url, boolean cancelable){
        APK_PATH = Environment.getExternalStorageDirectory() + "/";
//        APK_PATH = aty.getFilesDir().getAbsolutePath() + "/";
        File dir = new File(APK_PATH);
        if(!dir.exists()) dir.mkdirs();
        deleteDownloadApk();
        DialogUtil.get().showUpdateDialog(aty, "");
        HttpUtil.download(aty, url, APK_PATH, APK_NAME, new ProgressCallback(){
            @Override
            public void onProgressMain(int percent, long bytesWritten, long contentLength, boolean done) {
                super.onProgressMain(percent, bytesWritten, contentLength, done);
                DialogUtil.get().setDownloadProgress(percent);
            }

            @Override
            public void onResponseMain(String filePath, HttpInfo info) {
                super.onResponseMain(filePath, info);
                if(info.isSuccessful()){  //下载成功
                    DialogUtil.get().closeLoadDialog();
                    installApk(aty, new File(APK_PATH + APK_NAME), cancelable);
                }
            }
        });
    }

    /**
     * 通过隐式意图调用系统安装程序安装APK
     */
    public static void installApk(Activity activity, File file, boolean cancelable) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        // 由于没有在Activity环境下启动Activity,设置下面的标签
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        //判读版本是否在7.0以上
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            //参数1 上下文, 参数2 Provider主机地址 和配置文件中保持一致   参数3  共享的文件
            Uri apkUri = FileProvider.getUriForFile(activity, "com.cainiao.provider.FileProvider", file);
            //添加这一句表示对目标应用临时授权该Uri所代表的文件
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            intent.setDataAndType(apkUri, "application/vnd.android.package-archive");
        } else {
            intent.setDataAndType(Uri.fromFile(file),
                    "application/vnd.android.package-archive");
        }
        activity.startActivity(intent);
        if(!cancelable) activity.finish();
    }


    /**
     * 删除已下载的apk
     */
    private static void deleteDownloadApk(){
        File file = new File(APK_PATH + APK_NAME);
        if(file.exists()) file.delete();
    }

    /**
     * 获取进程号对应的进程名
     *
     * @param pid 进程号
     * @return 进程名
     */
    public static String getProcessName(int pid) {
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader("/proc/" + pid + "/cmdline"));
            String processName = reader.readLine();
            if (!TextUtils.isEmpty(processName)) {
                processName = processName.trim();
            }
            return processName;
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        } finally {
            try {
                if (reader != null) {
                    reader.close();
                }
            } catch (IOException exception) {
                exception.printStackTrace();
            }
        }
        return null;
    }

}
