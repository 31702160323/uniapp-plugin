// IMusicServiceAidlInterface.aidl
package com.xzh.musicnotification;

import com.xzh.musicnotification.IMusicServiceCallbackAidlInterface;
import com.xzh.musicnotification.IMusicActivityCallbackAidlInterface;

interface IMusicServiceAidlInterface {
    void initConfig(in Map config);
    void setServiceEventListener(IMusicServiceCallbackAidlInterface listener);
    void setActivityEventListener(IMusicActivityCallbackAidlInterface listener);
    void switchNotification(boolean style);
    void setPosition(long position);
    boolean getFavour();
    boolean getPlaying();
    Map getSongData();
    void lock(boolean locking);
    void playOrPause(boolean playing);
    void setFavour(boolean favour);
    void update(in Map option);
    void sendMessage(String eventName,in Map params);
}