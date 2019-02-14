package com.poping520.dyxposed.framework;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

/**
 * 程序所有 Activity 的基类
 *
 * @author WangKZ
 * @version 1.0.0
 * create on 2019/2/12 16:21
 */
public abstract class BaseActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        DyXContext.getInstance().onCreate(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        DyXContext.getInstance().onDestroy(this);
    }
}
