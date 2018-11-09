package com.poping520.dyxposed.debug;

import android.content.Context;
import android.os.Build;
import android.os.Environment;
import android.os.Looper;
import android.os.Process;
import android.os.SystemClock;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.reflect.Field;

import static com.poping520.dyxposed.util.DateUtil.getCurrentTime;

/**
 * @author WangKZ
 * @version 1.0.0
 * create on 2018/10/11 13:59
 */
public class CrashHandler implements Thread.UncaughtExceptionHandler {

    private static final InstanceHolder sInstanceHolder = new InstanceHolder();

    private static class InstanceHolder {
        CrashHandler instance = new CrashHandler();
    }

    private CrashHandler() {
    }

    public static CrashHandler getInstance() {
        return sInstanceHolder.instance;
    }

    private Context mContext;
    private Thread.UncaughtExceptionHandler mDefaultHandler;
    private StringBuilder mSb;

    public void init(Context context) {
        mContext = context;
        mDefaultHandler = Thread.getDefaultUncaughtExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler(this);
    }

    @Override
    public void uncaughtException(Thread t, Throwable th) {
        if (!handleException(th) && mDefaultHandler != null) {
            mDefaultHandler.uncaughtException(t, th);
        } else {
            SystemClock.sleep(3000);
            Process.killProcess(Process.myPid());
            System.exit(1);
        }
    }

    private boolean handleException(Throwable th) {
        if (th == null) return false;

        try {
            new Thread(() -> {
                Looper.prepare();
                Toast.makeText(mContext, "程序出现异常 即将关闭", Toast.LENGTH_LONG).show();
                Looper.loop();
            }).start();

            mSb = new StringBuilder();
            mSb.append("            this crash start at ").append(getCurrentTime("yyy-MM-dd HH:mm:ss")).append("\n\n");

            addDeviceInfo();
            saveCrashLogToFile(th);

            SystemClock.sleep(3000);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }

    private void addDeviceInfo() {
        Field[] fields = Build.class.getDeclaredFields();

        mSb.append("=====> DeviceInfo start <=====\n");
        for (Field field : fields) {
            try {
                field.setAccessible(true);
                mSb.append(field.getName()).append("=").append(field.get(null)).append("\n");
            } catch (Exception e) {
                mSb.append("collect device info error");
            }
        }
        mSb.append("=====> DeviceInfo end <=====\n\n");
    }

    private void saveCrashLogToFile(Throwable th) throws IOException {
        mSb.append("=====> crash log start <=====\n");

        Writer writer = new StringWriter();
        PrintWriter printWriter = new PrintWriter(writer);
        th.printStackTrace(printWriter);
        Throwable cause = th.getCause();
        while (cause != null) {
            cause.printStackTrace(printWriter);
            cause = cause.getCause();
        }
        printWriter.flush();
        printWriter.close();
        String result = writer.toString();
        mSb.append(result);

        mSb.append("=====> crash log end <=====\n\n");

        String fileName = "dyxposed_crash-" + getCurrentTime("MM-dd-HH-mm") + ".log";
        final File file = new File(Environment.getExternalStorageDirectory(), fileName);

        stringToFile(file, mSb.toString());
    }

    private void stringToFile(File file, String str) throws IOException {
        final OutputStreamWriter osw = new OutputStreamWriter(
                new FileOutputStream(file, true), "UTF-8");
        osw.write(str);
        osw.flush();
        osw.close();
    }
}
