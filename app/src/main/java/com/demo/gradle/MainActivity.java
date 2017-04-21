package com.demo.gradle;

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.example.l_pgyupdate.PgyManager;

public class MainActivity extends AppCompatActivity {
    String mAppId;
    String mVersionName;
    int mVersionCode;
    String mResEnv ;
    String mBuildConfigEnv ;
    String mChanel;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
        TextView textAppId = (TextView) findViewById(R.id.applicationId);
        TextView textVersionName = (TextView) findViewById(R.id.versionName);
        TextView textVersionCode = (TextView) findViewById(R.id.versionCode);
        TextView textResEnv = (TextView) findViewById(R.id.res_env);
        TextView textBuildConfigEnv = (TextView) findViewById(R.id.buildconfig_env);
        TextView textChanel = (TextView) findViewById(R.id.chanel);

        textAppId.setText(mAppId);
        textVersionName.setText(mVersionName);
        textVersionCode.setText("" + mVersionCode);
        textResEnv.setText(mResEnv);
        textBuildConfigEnv.setText(mBuildConfigEnv);
        textChanel.setText(mChanel);

    }

    private  void init() {
        initAppInfo();
        mBuildConfigEnv = "env:" + BuildConfig.ENV
                + "\nbuildType:" + BuildConfig.BUILD_TYPE
                + "\nflavor:" + BuildConfig.FLAVOR;
        mResEnv = getResources().getString(R.string.env);
        mChanel = getMetaValue("UMENG_CHANNEL");
    }

    private  void initAppInfo() {
        try {
            mAppId = this.getPackageName();
            mVersionName = this.getPackageManager().getPackageInfo(
                    mAppId, 0).versionName;
            mVersionCode = this.getPackageManager()
                    .getPackageInfo(mAppId, 0).versionCode;
            Log.i("gradle",mAppId + "   " + mVersionName + "  " + mVersionCode);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String getMetaValue(String metaName){
        ApplicationInfo appInfo = null;
        try {
            appInfo = this.getPackageManager()
                    .getApplicationInfo(this.getPackageName(),
                            PackageManager.GET_META_DATA);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        String msg=appInfo.metaData.getString(metaName);
        return msg;
    }




}

