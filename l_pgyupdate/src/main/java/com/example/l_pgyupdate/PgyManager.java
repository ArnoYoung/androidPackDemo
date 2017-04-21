package com.example.l_pgyupdate;



import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;

import com.pgyersdk.javabean.AppBean;
import com.pgyersdk.update.PgyUpdateManager;
import com.pgyersdk.update.UpdateManagerListener;

/**
 * Created by arno-young on 16/3/15.
 */
public class PgyManager {
    public static void registerUpdate(final Activity context){


        //软件更新注册
        PgyUpdateManager.register(context,"/", new UpdateManagerListener() {

            @Override
            public void onUpdateAvailable(String arg0) {
                final AppBean appBean = getAppBeanFromString(arg0);
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                if (appBean.getVersionName().contains("force")) {
                    builder
                            .setCancelable(false)
                            .setTitle("请更新到新版本，否则会影响使用！")
                            .setMessage("更新内容：\n" + appBean.getReleaseNote())
                            .setPositiveButton("更新", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    startDownloadTask(context,appBean.getDownloadURL());
                                }
                            }).show();
                }else {
                    builder
                            .setCancelable(true)
                            .setTitle("检测到新版本")
                            .setMessage("更新内容：\n" + appBean.getReleaseNote())
                            .setPositiveButton("更新", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    startDownloadTask(context,appBean.getDownloadURL());
                                }
                            })
                            .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            }).show();
                }
            }

            @Override
            public void onNoUpdateAvailable() {

            }
        });
    }

}
