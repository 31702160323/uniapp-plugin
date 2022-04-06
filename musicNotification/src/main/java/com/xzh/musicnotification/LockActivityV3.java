package com.xzh.musicnotification;

import android.content.pm.ApplicationInfo;
import android.os.Build;
import android.os.Bundle;
import android.util.ArrayMap;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;

import androidx.appcompat.app.AppCompatActivity;

import com.taobao.weex.IWXRenderListener;
import com.taobao.weex.WXSDKInstance;
import com.taobao.weex.common.WXRenderStrategy;
import com.taobao.weex.utils.WXFileUtils;
import com.xzh.musicnotification.utils.Utils;

import java.util.Map;

import io.dcloud.EntryProxy;
import io.dcloud.common.DHInterface.ICore;
import io.dcloud.common.DHInterface.IMgr;
import io.dcloud.common.util.BaseInfo;
import io.dcloud.feature.uniapp.UniSDKInstance;

public class LockActivityV3 extends AppCompatActivity implements IWXRenderListener {
    UniSDKInstance mUniSDKInstance;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true);
        }
        Utils.fullScreen(this);
        ApplicationInfo info = Utils.getApplicationInfo(this);
        String page = info.metaData.getString("xzh_page");
        Map<String, Object> options = new ArrayMap<>();
        options.put(WXSDKInstance.BUNDLE_URL, "source");

        String template = WXFileUtils.loadFileOrAsset(BaseInfo.sCacheFsAppsPath + BaseInfo.sDefaultBootApp + "/www/"+ page +".js", this);

        mUniSDKInstance = new UniSDKInstance(this);
        mUniSDKInstance.registerRenderListener(this);
        mUniSDKInstance.render(getPackageName(), template, options, null, WXRenderStrategy.APPEND_ASYNC);

    }
    @Override
    public void onViewCreated(WXSDKInstance instance, View view) {
        setContentView(view);
    }

    @Override
    public void onRenderSuccess(WXSDKInstance instance, int width, int height) {
    }
    @Override
    public void onRefreshSuccess(WXSDKInstance instance, int width, int height) {
    }

    @Override
    public void onException(WXSDKInstance instance, String errCode, String msg) {
        Log.d("TAG", "onException: " + errCode);
        Log.d("TAG", "onException: " + msg);
    }
    @Override
    public void onResume() {
        super.onResume();
        if(mUniSDKInstance !=null){
            mUniSDKInstance.onActivityResume();
        }
    }
    @Override
    public void onPause() {
        super.onPause();
        if(mUniSDKInstance !=null){
            mUniSDKInstance.onActivityPause();
        }
    }
    @Override
    protected void onStop() {
        super.onStop();
        if(mUniSDKInstance !=null){
            mUniSDKInstance.onActivityStop();
        }
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        if(mUniSDKInstance !=null){
            mUniSDKInstance.onActivityDestroy();
        }
    }
}
