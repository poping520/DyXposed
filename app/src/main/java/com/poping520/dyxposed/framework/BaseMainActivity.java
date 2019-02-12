package com.poping520.dyxposed.framework;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
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
import com.poping520.open.mdialog.MDialog;

import java.util.ArrayList;
import java.util.List;

/**
 * @author WangKZ
 * @version 1.0.0
 * create on 2018/11/8 17:20
 */
public abstract class BaseMainActivity extends BaseActivity implements DyXEnv.EnvStateListener {

    private static final String[] MUST_PERMISSIONS = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    private static final int REQUEST_CODE = 0;

    private MDialog mPermissionDialog;

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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        DyXContext.getInstance().onDestroy();
    }

    private void startCheck() {

        if (!AndroidOS.isDeviceRooted()) {
            DyXContext
                    .mkBaseMDialog(R.drawable.ic_build_white_24dp, R.string.dtitle_check_root)
                    .setMessage(R.string.dmsg_has_root)
                    .setPositiveButton(R.string.device_root_already, true, (mDialog, mDialogAction) -> {

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
        if (AndroidOS.API_LEVEL < Build.VERSION_CODES.M) {
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
                    .setHeaderBgColorRes(R.color.colorPrimary)
                    .setHeaderPic(R.drawable.ic_security_white_24dp)
                    .setTitle(R.string.permission_dialog_title)
                    .setMessage(R.string.dialog_msg_request_permission)
                    .setPositiveButton(R.string.go_on, true, (mDialog, mDialogAction) ->
                            ActivityCompat.requestPermissions(this, list.toArray(new String[0]), REQUEST_CODE)
                    )
                    .setCancelable(false)
                    .create();
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
                mPermissionDialog.setMessage(R.string.dmsg_refuse_permission);
                negBtn.setVisibility(View.VISIBLE);
                negBtn.setText(R.string.exit_app);
                negBtn.setOnClickListener(v -> AndroidOS.killSelf());
                posBtn.setText(R.string.retry_request_permission);
            }
        }

        onCheckPermissionResult(isAllGranted);
    }

    private void onCheckPermissionResult(boolean isGranted) {
        if (!isGranted) {
            return;
        }

        DyXEnv.getInstance().setEnvStateListener(this);

        final int result = DyXEnv.getInstance().initDyXEnv();
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
