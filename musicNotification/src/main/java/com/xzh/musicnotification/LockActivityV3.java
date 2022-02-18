package com.xzh.musicnotification;

import android.content.pm.ApplicationInfo;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;

import androidx.appcompat.app.AppCompatActivity;

import com.taobao.weex.IWXRenderListener;
import com.taobao.weex.WXSDKInstance;
import com.taobao.weex.common.WXRenderStrategy;
import com.taobao.weex.utils.WXFileUtils;
import com.xzh.musicnotification.utils.Utils;
import com.xzh.musicnotification.utils.XmlUtils;

import org.xmlpull.v1.XmlPullParser;

import java.util.HashMap;
import java.util.Map;

public class LockActivityV3 extends AppCompatActivity implements IWXRenderListener {
    WXSDKInstance mWXSDKInstance;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true);
        }
        Utils.fullScreen(this);

        ApplicationInfo info = Utils.getApplicationInfo(this);
        XmlUtils.readXml(this, "data/dcloud_control.xml", (event, parser) -> {
            if (event == XmlPullParser.START_TAG && parser.getName().equalsIgnoreCase("app")) {
                String appId = parser.getAttributeValue(null, "appid");
                String page = info.metaData.getString("xzh_page");
                Map<String, Object> options = new HashMap<>();
                options.put(WXSDKInstance.BUNDLE_URL, "source");

                mWXSDKInstance = new WXSDKInstance(this);
                mWXSDKInstance.registerRenderListener(this);
                mWXSDKInstance.render(getPackageName(), WXFileUtils.loadAsset("apps/" + appId + "/www/" + page + ".js", this), options, null, WXRenderStrategy.APPEND_ASYNC);
                return true;
            }
            return false;
        });
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
    protected void onResume() {
        super.onResume();
        if(mWXSDKInstance!=null){
            mWXSDKInstance.onActivityResume();
        }
    }
    @Override
    protected void onPause() {
        super.onPause();
        if(mWXSDKInstance!=null){
            mWXSDKInstance.onActivityPause();
        }
    }
    @Override
    protected void onStop() {
        super.onStop();
        if(mWXSDKInstance!=null){
            mWXSDKInstance.onActivityStop();
        }
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(mWXSDKInstance!=null){
            mWXSDKInstance.onActivityDestroy();
        }
    }
}