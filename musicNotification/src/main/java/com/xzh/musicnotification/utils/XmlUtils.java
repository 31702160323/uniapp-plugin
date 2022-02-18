package com.xzh.musicnotification.utils;

import android.content.Context;
import android.util.Xml;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;

public class XmlUtils {
    public static void readXml(Context context, String fileName, IReadXmlListener listener) {
        XmlPullParser parser = Xml.newPullParser();
        try {
            InputStream is = context.getAssets().open(fileName);
            parser.setInput(is, "UTF-8");
            for (int i = parser.getEventType(); i != XmlPullParser.END_DOCUMENT; i = parser.next()) {
                if (listener.onCallBack(i, parser)) return;
            }
        } catch (XmlPullParserException | IOException e) {
            e.printStackTrace();
        }
    }

    public interface IReadXmlListener{
        boolean onCallBack(int event,XmlPullParser parser);
    }
}
