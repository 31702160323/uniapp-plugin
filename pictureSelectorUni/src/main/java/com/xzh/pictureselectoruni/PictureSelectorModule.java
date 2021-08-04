package com.xzh.pictureselectoruni;

import android.app.Activity;
import android.content.Intent;

import com.luck.picture.lib.PictureSelector;
import com.luck.picture.lib.config.PictureConfig;
import com.luck.picture.lib.config.PictureMimeType;

import io.dcloud.feature.uniapp.annotation.UniJSMethod;
import io.dcloud.feature.uniapp.common.UniModule;

public class PictureSelectorModule extends UniModule {

    @UniJSMethod(uiThread = false)
    public void openPictureSelector() {
        if (mUniSDKInstance.getContext() instanceof Activity) {
            PictureSelector.create((Activity) mUniSDKInstance.getContext())
                    .openGallery(PictureMimeType.ofImage())
                    .imageEngine(GlideEngine.createGlideEngine()) // 请参考Demo GlideEngine.java
                    .maxSelectNum(9999)
                    .forResult(PictureConfig.CHOOSE_REQUEST);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }
}
