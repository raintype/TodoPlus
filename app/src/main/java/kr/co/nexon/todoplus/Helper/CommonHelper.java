package kr.co.nexon.todoplus.Helper;


import android.content.*;

import java.text.DateFormatSymbols;
import java.util.*;
import kr.co.nexon.todoplus.Entity.*;

/**
 * Created by raintype on 2015-01-23.
 */
public class CommonHelper {
    public static String LINE_FEED = "\r\n";
    public static SettingInfo settingInfo;
    private static String SHARED_PREFERENCES_KEY = "com.nexon.todo";
    private static String IS_LOCK_SCREEN = "IsLockScreen";
    private static String IS_DAY_TIME_DISPLAY = "IsDayTimeDisplay";
    private static String IS_DUE_DATE_DISPLAY = "IsDueDateDisplay";

    public static long getDateDiff(Calendar cal1, Calendar cal2) {
        long cal1Day = cal1.getTimeInMillis() / 1000 / (60*60*24);
        long cal2Day = cal2.getTimeInMillis() / 1000 / (60*60*24);

        long diffDay = cal1Day - cal2Day;

        return  diffDay;
    }

    public static SettingInfo getSettingInfo(Context context) {
        if (settingInfo == null) {
            settingInfo = new SettingInfo();
            SharedPreferences pref =  context.getSharedPreferences(SHARED_PREFERENCES_KEY, Context.MODE_PRIVATE);

            settingInfo.setIsLockScreen(pref.getBoolean(IS_LOCK_SCREEN, false));
            settingInfo.setIsDayTimeDisplay(pref.getBoolean(IS_DAY_TIME_DISPLAY, false));
            settingInfo.setIsDueDateDisplay(pref.getBoolean(IS_DUE_DATE_DISPLAY, false));
        }

        return settingInfo;
    }

    public static void setSettingInfo(Context context, SettingInfo value) {
        settingInfo = value;

        SharedPreferences pref =  context.getSharedPreferences(SHARED_PREFERENCES_KEY, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();

        editor.putBoolean(IS_LOCK_SCREEN, value.getIsLockScree());
        editor.putBoolean(IS_DAY_TIME_DISPLAY, value.getIsDayTimeDisplay());
        editor.putBoolean(IS_DUE_DATE_DISPLAY, value.getIsDueDateDisplay());

        editor.commit();
    }

    public static String getCurrentDate() {
        final Calendar c = Calendar.getInstance();
        String month = new DateFormatSymbols(Locale.US).getShortMonths()[c.get(Calendar.MONTH)];
        String week = new DateFormatSymbols(Locale.US).getShortWeekdays()[c.get(Calendar.DAY_OF_WEEK)];
        int day = c.get(Calendar.DAY_OF_MONTH);

        return String.format("%s, %s %d", week, month, day);
    }
}
