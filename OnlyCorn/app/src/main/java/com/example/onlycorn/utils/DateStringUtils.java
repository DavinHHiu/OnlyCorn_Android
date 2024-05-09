package com.example.onlycorn.utils;

import android.annotation.SuppressLint;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class DateStringUtils {
    @SuppressLint("SimpleDateFormat")
    public static String format(String timestamp) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(Long.parseLong(timestamp));
        return new SimpleDateFormat("dd/MM/yyyy hh:mm aa").format(calendar.getTime());
    }
}
