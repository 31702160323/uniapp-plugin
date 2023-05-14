package com.xzh.calendar;

import android.Manifest;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.net.Uri;
import android.provider.CalendarContract;
import android.util.ArrayMap;
import android.util.Log;
import android.view.View;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import java.util.Map;
import java.util.Set;
import java.util.TimeZone;

import io.dcloud.feature.uniapp.annotation.UniJSMethod;
import io.dcloud.feature.uniapp.bridge.UniJSCallback;
import io.dcloud.feature.uniapp.common.UniModule;

public class CalendarModule extends UniModule {
    private String CALENDARS_ACCOUNT_NAME;

    public void requestPermissions(Context context, IPermissions iPermissions) {
        try {
            ApplicationInfo info = context.getPackageManager().getApplicationInfo(context.getPackageName(), PackageManager.GET_META_DATA);

            if (info != null) {
                CALENDARS_ACCOUNT_NAME = info.metaData.getString("xzh_account_name");
            }

            String[] permissions = new String[]{Manifest.permission.WRITE_CALENDAR, Manifest.permission.READ_CALENDAR};
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_CALENDAR) == PackageManager.PERMISSION_DENIED) {
                if (context instanceof Activity) {
                    ActivityCompat.requestPermissions((Activity) context, permissions, 1);
                }
            } else {
                iPermissions.onRequestPermissionsResult();
            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
    }

    interface IPermissions {
        void onRequestPermissionsResult();
    }

    @UniJSMethod(uiThread = false)
    public void addCalendarEvent(JSONObject option) {
        Context context = mUniSDKInstance.getContext();

//        requestPermissions(context, () -> addEvent(option, context));
        if (context instanceof Activity) {
            context.startActivity(new Intent(context, FloatingCmdAct.class));
        }
    }

    @UniJSMethod(uiThread = false)
    public void queryCalendarEvent(UniJSCallback callback) {
        Context context = mUniSDKInstance.getContext();

        requestPermissions(context, () -> {
            ContentResolver resolver = context.getContentResolver();
            JSONArray accountList = queryCalendar(CALENDARS_ACCOUNT_NAME, CalendarContract.ACCOUNT_TYPE_LOCAL, resolver);
            long calendarId;

            if (accountList.size() <= 0) {
                calendarId = addCalendar(context, resolver);
            } else {
                calendarId = accountList.getJSONObject(0).getLong("account_id");
            }

            if (calendarId >= 0) {
                Map<String, String> selectionList = new ArrayMap<>();
                selectionList.put(CalendarContract.Events.CALENDAR_ID, String.valueOf(calendarId));

                callback.invoke(queryEvent(selectionList, resolver));
            } else {
                callback.invoke(new JSONArray());
            }
        });
    }

    @UniJSMethod(uiThread = false)
    public void deleteCalendarEvent(int id) {
        Context context = mUniSDKInstance.getContext();

        requestPermissions(context, () -> deleteEvent(id, context));
    }

    public long addCalendar(Context context, ContentResolver resolver) {
        String CALENDARS_NAME = context.getResources().getString(context.getApplicationInfo().labelRes);
        String CALENDARS_DISPLAY_NAME = context.getResources().getString(context.getApplicationInfo().labelRes);

        queryCalendar(CALENDARS_ACCOUNT_NAME, CalendarContract.ACCOUNT_TYPE_LOCAL, resolver);

        ContentValues contentValues = new ContentValues();
        contentValues.put(CalendarContract.Calendars.NAME, CALENDARS_NAME);
        contentValues.put(CalendarContract.Calendars.ACCOUNT_NAME, CALENDARS_ACCOUNT_NAME);
        contentValues.put(CalendarContract.Calendars.ACCOUNT_TYPE, CalendarContract.ACCOUNT_TYPE_LOCAL);
        contentValues.put(CalendarContract.Calendars.CALENDAR_DISPLAY_NAME, CALENDARS_DISPLAY_NAME);
        contentValues.put(CalendarContract.Calendars.VISIBLE, 1);
        contentValues.put(CalendarContract.Calendars.CALENDAR_COLOR, Color.BLUE);
        contentValues.put(CalendarContract.Calendars.CALENDAR_ACCESS_LEVEL, CalendarContract.Calendars.CAL_ACCESS_OWNER);
        contentValues.put(CalendarContract.Calendars.SYNC_EVENTS, 1);
        contentValues.put(CalendarContract.Calendars.CALENDAR_TIME_ZONE, TimeZone.getDefault().getID());
        contentValues.put(CalendarContract.Calendars.OWNER_ACCOUNT, CALENDARS_ACCOUNT_NAME);
        contentValues.put(CalendarContract.Calendars.CAN_ORGANIZER_RESPOND, 0);

        Uri uri = CalendarContract.Calendars.CONTENT_URI.buildUpon()
                .appendQueryParameter(CalendarContract.CALLER_IS_SYNCADAPTER, "true")
                .appendQueryParameter(CalendarContract.Calendars.ACCOUNT_NAME, CALENDARS_NAME)
                .appendQueryParameter(CalendarContract.Calendars.ACCOUNT_TYPE, CalendarContract.ACCOUNT_TYPE_LOCAL)
                .build();

        Uri ret = resolver.insert(uri, contentValues);
        return ret != null ? ContentUris.parseId(ret) : -102L;
    }

    public JSONArray queryCalendar(String accountName, String accountType, ContentResolver resolver) {
        String selection = "((" +
                CalendarContract.Calendars.ACCOUNT_NAME + " = ?) AND (" +
                CalendarContract.Calendars.ACCOUNT_TYPE + " = ?))";
        String[] selectionArgs = new String[]{accountName, accountType};

        Cursor cursor = resolver.query(CalendarContract.Calendars.CONTENT_URI, null, selection, selectionArgs, null);

        JSONArray list = new JSONArray();
        // 不存在日历账户
        if (cursor != null) {
            while (cursor.moveToNext()) {
                JSONObject object = new JSONObject();
                object.put("account_id", cursor.getLong(cursor.getColumnIndex(CalendarContract.Calendars._ID)));
                object.put("owner_account", cursor.getString(cursor.getColumnIndex(CalendarContract.Calendars.OWNER_ACCOUNT)));
                object.put("account_type", cursor.getString(cursor.getColumnIndex(CalendarContract.Calendars.ACCOUNT_TYPE)));
                object.put("mutators", cursor.getString(cursor.getColumnIndex(CalendarContract.Calendars.MUTATORS)));
                object.put("calendar_display_name", cursor.getString(cursor.getColumnIndex(CalendarContract.Calendars.CALENDAR_DISPLAY_NAME)));
                object.put("calendar_location", cursor.getString(cursor.getColumnIndex(CalendarContract.Calendars.CALENDAR_LOCATION)));
                object.put("calendar_time_zone", cursor.getString(cursor.getColumnIndex(CalendarContract.Calendars.CALENDAR_TIME_ZONE)));
                object.put("account_name", cursor.getString(cursor.getColumnIndex(CalendarContract.Calendars.ACCOUNT_NAME)));
                object.put("allowed_reminders", cursor.getString(cursor.getColumnIndex(CalendarContract.Calendars.ALLOWED_REMINDERS)));
                object.put("allowed_availability", cursor.getString(cursor.getColumnIndex(CalendarContract.Calendars.ALLOWED_AVAILABILITY)));
                object.put("allowed_attendee_types", cursor.getString(cursor.getColumnIndex(CalendarContract.Calendars.ALLOWED_ATTENDEE_TYPES)));
                object.put("dirty", cursor.getInt(cursor.getColumnIndex(CalendarContract.Calendars.DIRTY)));
                object.put("calendar_color", cursor.getInt(cursor.getColumnIndex(CalendarContract.Calendars.CALENDAR_COLOR)));
                object.put("max_reminders", cursor.getInt(cursor.getColumnIndex(CalendarContract.Calendars.MAX_REMINDERS)));
                list.add(object);
            }
            // 释放资源
            cursor.close();
        }
        return list;
    }

    public void addEvent(JSONObject option, Context context) {
        ContentResolver resolver = context.getContentResolver();
        JSONArray accountList = queryCalendar(CALENDARS_ACCOUNT_NAME, CalendarContract.ACCOUNT_TYPE_LOCAL, resolver);
        long calendarId;

        if (accountList.size() <= 0) {
            calendarId = addCalendar(context, resolver);
        } else {
            calendarId = accountList.getJSONObject(0).getLong("account_id");
        }

        // 标题
        final String title = option.getString("title");
        // 简述
        final String description = option.getString("description");
        // 开始时间
        final long startTime = option.getLong("startTime");
        // 结束时间
        final long endTime = option.getLong("endTime");
        // 提前时间
        final int minutes = option.getIntValue("minutes");

        if (calendarId >= 0) {
            long eventId;

            Map<String, String> selectionList = new ArrayMap<>();
            selectionList.put(CalendarContract.Events.CALENDAR_ID, String.valueOf(calendarId));
            selectionList.put(CalendarContract.Events.TITLE, title);
            selectionList.put(CalendarContract.Events.DESCRIPTION, description);
            selectionList.put(CalendarContract.Events.DTSTART, String.valueOf(startTime));
            selectionList.put(CalendarContract.Events.DTEND, String.valueOf(endTime));

            JSONArray eventList = queryEvent(selectionList, resolver);
            if (eventList.size() > 0) {
                eventId = eventList.getJSONObject(0).getLong("ID");
            } else {
                ContentValues contentValues = new ContentValues();
                contentValues.put(CalendarContract.Events.CALENDAR_ID, calendarId);
                contentValues.put(CalendarContract.Events.TITLE, title);
                contentValues.put(CalendarContract.Events.DESCRIPTION, description);
                contentValues.put(CalendarContract.Events.STATUS, CalendarContract.Events.STATUS_TENTATIVE);
                contentValues.put(CalendarContract.Events.DTSTART, startTime);
                contentValues.put(CalendarContract.Events.DTEND, endTime);
                contentValues.put(CalendarContract.Events.EVENT_TIMEZONE, TimeZone.getDefault().getID());
                contentValues.put(CalendarContract.Events.ACCESS_LEVEL, CalendarContract.Events.ACCESS_DEFAULT);
                contentValues.put(CalendarContract.Events.AVAILABILITY, CalendarContract.Events.AVAILABILITY_BUSY);
                contentValues.put(CalendarContract.Events.HAS_ALARM, 1);
                //        contentValues.put(CalendarContract.Events.EVENT_LOCATION, event.EVENT_LOCATION);
                //        contentValues.put(CalendarContract.Events.EVENT_COLOR, event.EVENT_COLOR);
                //        contentValues.put(CalendarContract.Events.SELF_ATTENDEE_STATUS, event.SELF_ATTENDEE_STATUS);
                //        contentValues.put(CalendarContract.Events.ALL_DAY, event.ALL_DAY);
                //        contentValues.put(CalendarContract.Events.HAS_EXTENDED_PROPERTIES, event.HAS_EXTENDED_PROPERTIES);
                //        contentValues.put(CalendarContract.Events.RRULE, event.RRULE);
                //        contentValues.put(CalendarContract.Events.RDATE, event.RDATE);
                //        contentValues.put(CalendarContract.Events.EXRULE, event.EXRULE);
                //        contentValues.put(CalendarContract.Events.EXDATE, event.EXDATE);
                //        contentValues.put(CalendarContract.Events.ORIGINAL_ID, event.ORIGINAL_ID);
                //        contentValues.put(CalendarContract.Events.LAST_DATE, event.LAST_DATE);
                //        contentValues.put(CalendarContract.Events.HAS_ATTENDEE_DATA, event.HAS_ATTENDEE_DATA);
                //        contentValues.put(CalendarContract.Events.GUESTS_CAN_MODIFY, event.GUESTS_CAN_MODIFY);
                //        contentValues.put(CalendarContract.Events.GUESTS_CAN_INVITE_OTHERS, event.GUESTS_CAN_INVITE_OTHERS);
                //        contentValues.put(CalendarContract.Events.GUESTS_CAN_SEE_GUESTS, event.GUESTS_CAN_SEE_GUESTS);
                //        contentValues.put(CalendarContract.Events.ORGANIZER, event.ORGANIZER);
                //        contentValues.put(CalendarContract.Events.DELETED, event.DELETED);
                //        contentValues.put(CalendarContract.Events.EVENT_END_TIMEZONE, event.EVENT_END_TIMEZONE);

                Uri ret = resolver.insert(CalendarContract.Events.CONTENT_URI, contentValues);
                eventId =ret != null ? ContentUris.parseId(ret) : -102L;
            }
            Log.d("TAG", "addEvent: " + eventId);

            if (eventId >= 0) {
                addReminder(eventId, CalendarContract.Reminders.METHOD_ALERT, minutes, resolver);
            }
        }
    }

    public int deleteEvent(int id, Context context) {
        ContentResolver resolver = context.getContentResolver();
        String selection = "(" + CalendarContract.Events._ID + " = ?)";
        String[] selectionArgs = new String[]{String.valueOf(id)};
        return resolver.delete(CalendarContract.Events.CONTENT_URI, selection, selectionArgs);
    }

    public JSONArray queryEvent(Map<String, String> selectionList, ContentResolver resolver) {
        int i = 0;
        Set<String> keys = selectionList.keySet();
        StringBuilder selection = new StringBuilder("((");
        String[] selectionArgs = new String[keys.size()];
        for (String key: keys) {
            selectionArgs[i] = selectionList.get(key);
            selection.append(key);
            if (i++ < keys.size() - 1) {
                selection.append(" = ?) AND (");
            } else {
                selection.append(" = ?))");
            }
        }

        Cursor cursor = resolver.query(CalendarContract.Events.CONTENT_URI, null, selection.toString(), selectionArgs, null);

        JSONArray list = new JSONArray();
        // 不存在日历账户
        if (cursor != null) {
            while (cursor.moveToNext()) {
                JSONObject object = new JSONObject();
                object.put("id", cursor.getLong(cursor.getColumnIndex(CalendarContract.Events._ID)));
                object.put("calendar_id", cursor.getLong(cursor.getColumnIndex(CalendarContract.Events.CALENDAR_ID)));
                object.put("title", cursor.getString(cursor.getColumnIndex(CalendarContract.Events.TITLE)));
                object.put("event_location", cursor.getString(cursor.getColumnIndex(CalendarContract.Events.EVENT_LOCATION)));
                object.put("description", cursor.getString(cursor.getColumnIndex(CalendarContract.Events.DESCRIPTION)));
                object.put("event_timezone", cursor.getString(cursor.getColumnIndex(CalendarContract.Events.EVENT_TIMEZONE)));
                object.put("rrule", cursor.getString(cursor.getColumnIndex(CalendarContract.Events.RRULE)));
                object.put("rdate", cursor.getString(cursor.getColumnIndex(CalendarContract.Events.RDATE)));
                object.put("exrule", cursor.getString(cursor.getColumnIndex(CalendarContract.Events.EXRULE)));
                object.put("exdate", cursor.getString(cursor.getColumnIndex(CalendarContract.Events.EXDATE)));
                object.put("organizer", cursor.getString(cursor.getColumnIndex(CalendarContract.Events.ORGANIZER)));
                object.put("event_end_timezone", cursor.getString(cursor.getColumnIndex(CalendarContract.Events.EVENT_END_TIMEZONE)));
                object.put("event_color", cursor.getInt(cursor.getColumnIndex(CalendarContract.Events.EVENT_COLOR)));
                object.put("status", cursor.getInt(cursor.getColumnIndex(CalendarContract.Events.STATUS)));
                object.put("self_attendee_status", cursor.getInt(cursor.getColumnIndex(CalendarContract.Events.SELF_ATTENDEE_STATUS)));
                object.put("dtstart", cursor.getLong(cursor.getColumnIndex(CalendarContract.Events.DTSTART)));
                object.put("dtend", cursor.getLong(cursor.getColumnIndex(CalendarContract.Events.DTEND)));
                object.put("all_day", cursor.getInt(cursor.getColumnIndex(CalendarContract.Events.ALL_DAY)));
                object.put("access_level", cursor.getInt(cursor.getColumnIndex(CalendarContract.Events.ACCESS_LEVEL)));
                object.put("availability", cursor.getInt(cursor.getColumnIndex(CalendarContract.Events.AVAILABILITY)));
                object.put("has_alarm", cursor.getInt(cursor.getColumnIndex(CalendarContract.Events.HAS_ALARM)));
                object.put("has_extended_properties", cursor.getInt(cursor.getColumnIndex(CalendarContract.Events.HAS_EXTENDED_PROPERTIES)));
                object.put("original_id", cursor.getInt(cursor.getColumnIndex(CalendarContract.Events.ORIGINAL_ID)));
                object.put("last_date", cursor.getInt(cursor.getColumnIndex(CalendarContract.Events.LAST_DATE)));
                object.put("has_attendee_data", cursor.getInt(cursor.getColumnIndex(CalendarContract.Events.HAS_ATTENDEE_DATA)));
                object.put("guests_can_modify", cursor.getInt(cursor.getColumnIndex(CalendarContract.Events.GUESTS_CAN_MODIFY)));
                object.put("guests_can_invite_others", cursor.getInt(cursor.getColumnIndex(CalendarContract.Events.GUESTS_CAN_INVITE_OTHERS)));
                object.put("guests_can_see_guests", cursor.getInt(cursor.getColumnIndex(CalendarContract.Events.GUESTS_CAN_SEE_GUESTS)));
                object.put("deleted", cursor.getInt(cursor.getColumnIndex(CalendarContract.Events.DELETED)));
                list.add(object);
            }
            // 释放资源
            cursor.close();
        }
        return list;
    }

    public long addReminder(long EVENT_ID, long METHOD, int MINUTES, ContentResolver resolver) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(CalendarContract.Reminders.EVENT_ID, EVENT_ID);
        contentValues.put(CalendarContract.Reminders.METHOD, METHOD);
        contentValues.put(CalendarContract.Reminders.MINUTES, MINUTES);
        Uri ret = resolver.insert(CalendarContract.Reminders.CONTENT_URI, contentValues);
        return ret != null ? ContentUris.parseId(ret) : -102L;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {
            JSONObject data = new JSONObject();
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                data.put("state", true);
                mUniSDKInstance.fireModuleEvent("requestPermissionsResult", this, data);
            } else {
                data.put("state", false);
                mUniSDKInstance.fireModuleEvent("requestPermissionsResult", this, data);
            }
        }
    }
}
