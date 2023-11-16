// IMusicServiceCallbackAidlInterface.aidl
package com.xzh.musicnotification;

// Declare any non-default types here with import statements

interface IMusicServiceCallbackAidlInterface {
    void sendMessage(String eventName,in Map params);
}