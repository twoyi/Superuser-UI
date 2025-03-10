package com.koushikdutta.superuser.util;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.annotation.AttrRes;
import android.support.annotation.NonNull;

/**
 * @author Aidan Follestad (afollestad)
 */
public final class ATHUtil {

    public static int resolveColor(Context context, @AttrRes int attr) {
        return resolveColor(context, attr, 0);
    }

    public static int resolveColor(Context context, @AttrRes int attr, int fallback) {
        TypedArray a = context.getTheme().obtainStyledAttributes(new int[]{attr});

        try {
            return a.getColor(0, fallback);

        } finally {
            a.recycle();
        }
    }

    public static boolean isInClassPath(@NonNull String clsName) {
        try {
            return inClassPath(clsName) != null;
        } catch (Throwable t) {
            return false;
        }
    }

    public static Class<?> inClassPath(@NonNull String clsName) {
        try {
            return Class.forName(clsName);
        } catch (Throwable t) {
            throw new IllegalStateException(String.format("%s is not in your class path! You must include the associated library.", clsName));
        }
    }

    private ATHUtil() {
    }
}