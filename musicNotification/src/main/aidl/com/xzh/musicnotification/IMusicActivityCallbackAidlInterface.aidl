// IMusicServiceCallbackAidlInterface.aidl
package com.xzh.musicnotification;

// Declare any non-default types here with import statements

interface IMusicActivityCallbackAidlInterface {
    void update(in Map option);
    void setFavour(boolean favour);
    void playOrPause(boolean playing);
}