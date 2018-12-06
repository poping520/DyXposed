package com.poping520.dyxposed.framework;

import android.app.Application;

import com.poping520.dyxposed.BuildConfig;
import com.poping520.dyxposed.debug.CrashHandler;

/**
 * Created by WangKZ on 18/11/07.
 *
 * @author poping520
 * @version 1.0.0
 */


public class DyXApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        DyXContext.getInstance().onCreate(this);

        if (!BuildConfig.DEBUG) {
            CrashHandler.getInstance().init(this);
        }
    }
}
