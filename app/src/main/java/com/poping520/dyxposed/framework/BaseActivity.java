package com.poping520.dyxposed.framework;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.poping520.dyxposed.BuildConfig;
import com.poping520.dyxposed.R;
import com.poping520.open.mdialog.MDialog;

/**
 * @author WangKZ
 * @version 1.0.0
 * create on 2018/11/8 17:20
 */
public abstract class BaseActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (waitHook() && !BuildConfig.DEBUG) {
            MDialog mDialog = new MDialog.Builder(this)
                    .setHeaderBgColor(getResources().getColor(R.color.Teal_500))
                    .setTitle("!!!")
                    .setMessage("似乎Xposed没有正常工作，您所做的一切修改都将没有效果！！！")
                    .create();
            mDialog.show();
        }
    }

    private boolean waitHook() {
        return true;
    }
}
