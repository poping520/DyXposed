package com.poping520.dyxposed.framework;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.Button;

import com.poping520.dyxposed.BuildConfig;
import com.poping520.dyxposed.R;
import com.poping520.dyxposed.os.AndroidOS;
import com.poping520.dyxposed.util.Objects;
import com.poping520.open.mdialog.MDialog;

import java.util.ArrayList;
import java.util.List;

/**
 * @author WangKZ
 * @version 1.0.0
 * create on 2018/11/8 17:20
 */
public abstract class BaseMainActivity extends BaseActivity implements DyXEnv.EnvStateListener {

    private static final int REQUEST_CODE = 0;

    private static final String[] MUST_PERMISSIONS = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    private MDialog mPmsDialog;

    protected DyXDBHelper mDBHelper;

    private static final String TAG = "BaseMainActivity";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mDBHelper = DyXDBHelper.getInstance();

        if (waitHook() && !BuildConfig.DEBUG) {
            /* xposed 状态异常 */
            handleXposedState();
            return;
        }

        startCheck();
    }

    private void startCheck() {
        if (!AndroidOS.isRooted()) {
            /* 未检测到设备 root */
            DyXContext
                    .buildMDialog(R.drawable.ic_build_white_24dp, R.string.dtitle_not_root)
                    .setCancelable(false)
                    .setMessage(R.string.dmsg_not_root)
                    .setPositiveButton(R.string.dbtn_root_already, true, (mDialog, mDialogAction) -> {

                    })
                    .setNegativeButton(R.string.exit_app, (mDialog, mDialogAction) -> AndroidOS.killSelf())
                    .show();
        } else {
            checkPermission();
        }
    }

    private void handleXposedState() {
        MDialog mDialog = new MDialog.Builder(this)
                .setHeaderBgColorRes(R.color.colorPrimary)
                .setHeaderPic(R.drawable.ic_warning_white_24dp)
                .setTitle(R.string.warning)
                .setNegativeButton(R.string.exit_app, (dialog, mDialogAction) -> AndroidOS.killSelf())
                .setCancelable(false)
                .create();

        // xposed 状态正常 但未激活模块
        final Button posBtn = mDialog.getPositiveButton();
        if (AndroidOS.isXposedFrameworkInstalled()) {
            mDialog.setMessage(R.string.dialog_msg_xposed_module_not_active);
            posBtn.setText(R.string.enable_module);
            posBtn.setOnClickListener(v -> AndroidOS.jump2XposedInstaller());
        } else {
            mDialog.setMessage(R.string.dmsg_xposed_framework_not_installed);
            posBtn.setText(R.string.understand);
        }

        mDialog.show();
    }


    // 检查必须权限
    private void checkPermission() {

        /* 未获得权限的集合 */
        List<String> list = new ArrayList<>();

        if (AndroidOS.isDynamicPermission()) {
            /* 向集合添加未获得的权限 */
            for (String permission : MUST_PERMISSIONS) {
                if (ContextCompat.checkSelfPermission(this, permission)
                        != PackageManager.PERMISSION_GRANTED) {
                    list.add(permission);
                }
            }
        }

        if (list.isEmpty()) {
            onPermissionsGranted();
        } else {
            mPmsDialog = DyXContext.buildMDialog(R.drawable.ic_security_white_24dp, R.string.dtilte_request_permission)
                    .setHTMLMessage(R.string.dmsg_request_permission)
                    .setPositiveButton(R.string.go_on, true, (mDialog, mDialogAction) ->
                            ActivityCompat.requestPermissions(this, list.toArray(new String[0]), REQUEST_CODE)
                    )
                    .setCancelable(false)
                    .create();
            mPmsDialog.show();
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

        /* 此处 mPmsDialog 不可能为空 */
        Objects.requireNonNull(mPmsDialog, "");

        if (isAllGranted) {
            Snackbar.make(
                    findViewById(android.R.id.content), R.string.toast_permission_granted, Snackbar.LENGTH_SHORT
            ).show();
            onPermissionsGranted();
        } else {
            final Button negBtn = mPmsDialog.getNegativeButton();
            final Button posBtn = mPmsDialog.getPositiveButton();
            mPmsDialog.setMessage(R.string.dmsg_refuse_permission);
            negBtn.setVisibility(View.VISIBLE);
            negBtn.setText(R.string.exit_app);
            negBtn.setOnClickListener(v -> AndroidOS.killSelf());
            posBtn.setText(R.string.retry_request_permission);
        }
    }

    private void onPermissionsGranted() {

        if (DyXContext.isRootAuthGranted()) {
            if (mPmsDialog != null && mPmsDialog.isShowing())
                mPmsDialog.dismiss();

            DyXEnv.getInstance().setEnvStateListener(this);
            final int result = DyXEnv.getInstance().initDyXEnv();
        } else {
            if (mPmsDialog == null) {
                mPmsDialog = DyXContext.buildMDialog(
                        R.drawable.ic_security_white_24dp, R.string.dtilte_request_permission
                ).create();
            }
            mPmsDialog.setMessage("NEED ROOT");
            mPmsDialog.getNegativeButton().setVisibility(View.INVISIBLE);
            final Button posBtn = mPmsDialog.getPositiveButton();
            posBtn.setText(R.string.understand);
            posBtn.setOnClickListener(v -> {
                mPmsDialog.dismiss();

                DyXEnv.getInstance().setEnvStateListener(this);
                final int result = DyXEnv.getInstance().initDyXEnv();
            });
            if (!mPmsDialog.isShowing()) {
                mPmsDialog.show();
            }
        }
    }

    /**
     * 在 {@link XposedChecker} 拦截后, 修改返回值为 false
     *
     * @return true => xposed 未生效; false => xposed 生效
     */
    private boolean waitHook() {
        return true;
    }
}
