// IMusicServiceAidlInterface.aidl
package com.xzh.musicnotification;

import com.xzh.musicnotification.IMusicServiceCallbackAidlInterface;

interface IMusicServiceAidlInterface {
    void setEventListener(IMusicServiceCallbackAidlInterface listener);
    void switchNotification(boolean style);
    void setPosition(long position);
    boolean getFavour();
    boolean getPlaying();
    Map getSongData();
    void lock(boolean locking);
    void playOrPause(boolean playing);
    void setFavour(boolean favour);
    void update(in Map option);
}