package com.poping520.dyxposed.framework;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Process;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import com.poping520.dyxposed.BuildConfig;
import com.poping520.dyxposed.R;
import com.poping520.dyxposed.system.AndroidSystem;
import com.poping520.open.mdialog.MDialog;
import com.poping520.open.mdialog.MDialogAction;

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

    private static final String TAG = "BaseMainActivity";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        DyXContext.getInstance().init(this);

        // xposed 状态异常
        if (waitHook() && !BuildConfig.DEBUG) {
            handleXposedState();
        } else {
            checkPermission();
        }
    }

    private void handleXposedState() {
        MDialog mDialog = new MDialog.Builder(this)
                .setHeaderBgColor(getResources().getColor(R.color.colorPrimary))
                .setTitle(R.string.warning)
                .setNegativeButton(R.string.exit_app, (dialog, mDialogAction) -> killSelf())
                .setCancelable(false)
                .create();

        // xposed 状态正常 但未激活模块
        final Button posBtn = mDialog.getPositiveButton();
        if (AndroidSystem.isXposedFrameworkInstalled()
                && AndroidSystem.isXposedManagerInstalled()) {
            mDialog.setMessage(R.string.dialog_msg_xposed_module_not_active);
            posBtn.setText(R.string.enable_module);
            posBtn.setOnClickListener(v -> {
                AndroidSystem.jump2XposedManager();
            });
        } else {
            mDialog.setMessage(R.string.dialog_msg_xposed_not_install);
            posBtn.setText(R.string.understand);
        }

        mDialog.show();
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
                    .setHeaderBgColor(getResources().getColor(R.color.colorPrimary))
                    .setHeaderPic(R.drawable.ic_security_white_24dp)
                    .setTitle(R.string.permission_dialog_title)
                    .setMessage(R.string.dialog_msg_request_permission)
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

        if (mPermissionDialog != null) {
            if (isAllGranted) {
                Snackbar.make(
                        findViewById(android.R.id.content), R.string.toast_permission_granted, Snackbar.LENGTH_SHORT
                ).show();
                mPermissionDialog.dismiss();
            } else {
                final Button negBtn = mPermissionDialog.getNegativeButton();
                final Button posBtn = mPermissionDialog.getPositiveButton();
                mPermissionDialog.setMessage(R.string.dialog_msg_refuse_permission);
                negBtn.setVisibility(View.VISIBLE);
                negBtn.setText(R.string.exit_app);
                negBtn.setOnClickListener(v -> killSelf());
                posBtn.setText(R.string.retry_request_permission);
            }
        }

        onCheckPermissionResult(isAllGranted);
    }

    private void onCheckPermissionResult(boolean isGranted) {
        if (!isGranted) {
            return;
        }
    }

    protected void killSelf() {
        finish();
        Process.killProcess(Process.myPid());
    }

    private boolean waitHook() {
        return true;
    }
}
