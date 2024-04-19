package com.example.onlycorn.utils;

import android.content.Context;
import android.widget.Toast;

public class Pop extends Toast {
    public Pop(Context context) {
        super(context);
    }

    public static void pop(Context context, String message) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }
}
