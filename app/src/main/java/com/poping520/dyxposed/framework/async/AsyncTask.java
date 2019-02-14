package com.poping520.dyxposed.framework.async;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.UiThread;
import android.support.annotation.WorkerThread;

import com.poping520.dyxposed.os.AndroidOS;

/**
 * 实现此接口即是一个异步任务<p>
 *
 * @author WangKZ
 * @version 1.0.0
 * create on 2019/2/13 16:48
 */
public abstract class AsyncTask {

    private Handler mUiHandler = new Handler(Looper.getMainLooper()) {

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            onReceive(msg);
        }
    };

    public void start() {
        new Thread(this::run).start();
    }

    public void sendMessage(Message msg) {
        if (AndroidOS.isMainThread())
            onReceive(msg);
        else
            mUiHandler.sendMessage(msg);
    }

    /**
     * <p>这个方法会运行在新开的子线程里, 将异步任务的耗时逻辑写在这里</p>
     */
    @WorkerThread
    protected abstract void run();

    /**
     * <p>收到 Message 时会回调这个方法，将异步任务的相关响应写在这里</p>
     *
     * @param msg 收到的消息
     */
    @UiThread
    protected abstract void onReceive(Message msg);
}
