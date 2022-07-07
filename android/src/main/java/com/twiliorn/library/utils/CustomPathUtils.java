package com.twiliorn.library.utils;

import android.content.Context;

import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.uimanager.ThemedReactContext;

import java.io.File;

public class CustomPathUtils {

    private final Context context;

    public CustomPathUtils(Context context) {
        this.context = context;
    }

    public String getStethoscopePipePath() {
        return getCacheDirPath(this.context) + "/stethoscope.wave";
    }

    private static  String getCacheDirPath(Context ctx) {
        File dir = ctx.getCacheDir();
        if (dir != null) return dir.getAbsolutePath();
        return "";
    }
}
