package com.poping520.dyxposed.framework;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.poping520.dyxposed.BuildConfig;
import com.poping520.dyxposed.R;
import com.poping520.dyxposed.system.AndroidSystem;
import com.poping520.open.mdialog.MDialog;

import java.util.ArrayList;
import java.util.List;

/**
 * @author WangKZ
 * @version 1.0.0
 * create on 2018/11/8 17:20
 */
public abstract class BaseMainActivity extends AppCompatActivity {

    private static final String[] MUST_PERMISSIONS = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    private static final int REQUEST_CODE = 0;

    private MDialog mPermissionDialog;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        DyXContext.getInstance().init(this);

        if (waitHook() && !BuildConfig.DEBUG) {
            MDialog mDialog = new MDialog.Builder(this)
                    .setHeaderBgColor(getResources().getColor(R.color.Teal_500))
                    .setTitle("!!!")
                    .setMessage("似乎Xposed没有正常工作，您所做的一切修改都将没有效果！！！")
                    .create();
            mDialog.show();
        } else {
            checkPermission();
        }
    }


    // 检查必须权限
    private void checkPermission() {
        if (AndroidSystem.API_LEVEL < Build.VERSION_CODES.M) {
            onCheckPermissionResult(true);
            return;
        }

        List<String> list = new ArrayList<>();

        for (String permission : MUST_PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(this, permission)
                    != PackageManager.PERMISSION_GRANTED) {
                list.add(permission);
            }
        }

        if (list.size() > 0) {
            mPermissionDialog = new MDialog.Builder(this)
                    .setHeaderBgColor(Color.BLACK)
                    .setTitle(R.string.permission_dialog_title)
                    .setMessage("")
                    .setPositiveButton(R.string.go_on, true, (mDialog, mDialogAction) -> {
                        ActivityCompat.requestPermissions(this, list.toArray(new String[0]), REQUEST_CODE);
                    })
                    .setCancelable(false)
                    .create();
            mPermissionDialog.getNegativeButton().setVisibility(View.INVISIBLE);
            mPermissionDialog.show();
        } else {
            onCheckPermissionResult(true);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        boolean isAllGranted = true;

        for (int grant : grantResults) {
            if (grant != PackageManager.PERMISSION_GRANTED) {
                isAllGranted = false;
                break;
            }
        }

        if (isAllGranted) {
            if (mPermissionDialog != null)
                mPermissionDialog.dismiss();
        } else {

        }

        onCheckPermissionResult(isAllGranted);
    }

    private void onCheckPermissionResult(boolean isGranted) {
        if (!isGranted) {
            return;
        }
        Env.init(this);

    }

    private boolean waitHook() {
        return true;
    }
}
