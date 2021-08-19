package com.xzh.pictureselectoruni;

import android.app.Activity;
import android.util.Log;

import com.alibaba.fastjson.JSONObject;
import com.luck.picture.lib.PictureSelectionModel;
import com.luck.picture.lib.PictureSelector;
import com.luck.picture.lib.config.PictureConfig;
import com.luck.picture.lib.config.PictureMimeType;
import com.luck.picture.lib.entity.LocalMedia;
import com.luck.picture.lib.language.LanguageConfig;
import com.luck.picture.lib.listener.OnResultCallbackListener;
import com.luck.picture.lib.style.PictureWindowAnimationStyle;

import java.util.List;

import io.dcloud.feature.uniapp.annotation.UniJSMethod;
import io.dcloud.feature.uniapp.bridge.UniJSCallback;
import io.dcloud.feature.uniapp.common.UniModule;

public class PictureSelectorModule extends UniModule {
    private static final String TAG = PictureSelectorModule.class.getSimpleName();

    @UniJSMethod
    public void openPictureSelector(JSONObject date, UniJSCallback callback) {
        Log.d(TAG, "openPictureSelector: " + date);
        if (mUniSDKInstance.getContext() instanceof Activity) {
            PictureSelectionModel model = PictureSelector.create((Activity) mUniSDKInstance.getContext())
                    .openGallery(this.switchType(date.getString("type")))
                    .imageEngine(GlideEngine.createGlideEngine())
                    .isAutomaticTitleRecyclerTop(true)
                    .isWithVideoImage(true)
                    .isPreviewImage(true)
                    .isPreviewVideo(true)
                    .isPageStrategy(true, 50);

            if (date.getBooleanValue("single")) {
                model.selectionMode(PictureConfig.SINGLE);
                model.isSingleDirectReturn(true);
            } else {
                model.selectionMode(PictureConfig.MULTIPLE);

                if (date.getInteger("max") != null && date.getInteger("max") > 0) {
                    model.maxSelectNum(date.getInteger("max"));
                    model.maxVideoSelectNum(date.getInteger("max"));
                }

                if (date.getInteger("min") != null && date.getInteger("min") > 0) {
                    model.minSelectNum(date.getInteger("min"));
                    model.minVideoSelectNum(date.getInteger("min"));
                }
            }

            if (date.getInteger("videoMaxSecond") != null && date.getInteger("videoMaxSecond") > 0) {
                model.videoMaxSecond(date.getInteger("videoMaxSecond"));
            }

            if (date.getInteger("videoMinSecond") != null && date.getInteger("videoMinSecond") > 0) {
                model.videoMinSecond(date.getInteger("videoMinSecond"));
            }

            if (date.getInteger("spanCount") != null && date.getInteger("spanCount") > 0) {
                model.imageSpanCount(date.getInteger("spanCount"));
            }

            if (date.getString("anim") != null) {
                model.setPictureWindowAnimationStyle(this.switchAnimation(date.getString("anim")));
            }

            model.setLanguage(this.switchLanguage(date.getString("language")));

            model.isGif(date.getBooleanValue("gif"));

            model.isOriginalImageControl(date.getBooleanValue("original"));

            model.forResult(new OnResultCallbackListener<LocalMedia>() {
                @Override
                public void onResult(List<LocalMedia> result) {
                    // 结果回调
                    Log.d(TAG, "onResult: " + (callback != null));
                    if (callback != null) {
                        Log.d(TAG, "onResult: " + result.size());
                        callback.invoke(result);
                    }
                }

                @Override
                public void onCancel() {
                    // 取消
                }
            });
        }
    }

    public int switchType(String type) {
        switch (type.toLowerCase()) {
            case "image":
                return PictureMimeType.ofImage();
            case "video":
                return PictureMimeType.ofVideo();
            default:
                return PictureMimeType.ofAll();
        }
    }

    public int switchLanguage(String type) {
        switch (type.toUpperCase()) {
            case "TRADITIONAL_CHINESE":
                return LanguageConfig.TRADITIONAL_CHINESE;
            case "ENGLISH":
                return LanguageConfig.ENGLISH;
            case "KOREA":
                return LanguageConfig.KOREA;
            case "GERMANY":
                return LanguageConfig.GERMANY;
            case "FRANCE":
                return LanguageConfig.FRANCE;
            case "JAPAN":
                return LanguageConfig.JAPAN;
            case "VIETNAM":
                return LanguageConfig.VIETNAM;
            default:
                return LanguageConfig.CHINESE;
        }
    }

    private PictureWindowAnimationStyle switchAnimation(String type) {
        PictureWindowAnimationStyle animationStyle = new PictureWindowAnimationStyle();
        switch (type) {
            case "slide-bottom":
                animationStyle.ofAllAnimation(R.anim.picture_anim_up_in, R.anim.picture_anim_down_out);
                break;
        }
        return animationStyle;
    }
}
