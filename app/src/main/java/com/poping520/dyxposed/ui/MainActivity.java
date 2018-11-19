package com.poping520.dyxposed.ui;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.StringRes;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.poping520.dyxposed.R;
import com.poping520.dyxposed.adapter.ModuleAdapter;
import com.poping520.dyxposed.framework.BaseMainActivity;
import com.poping520.dyxposed.framework.Env;
import com.poping520.dyxposed.framework.ModuleDBHelper;
import com.poping520.dyxposed.model.Module;
import com.poping520.dyxposed.system.AndroidSystem;
import com.poping520.dyxposed.system.Shell;
import com.poping520.dyxposed.util.Objects;
import com.poping520.open.mdialog.MDialog;
import com.poping520.open.mdialog.MDialogAction;

import org.json.JSONException;

import java.io.IOException;
import java.util.List;


public class MainActivity extends BaseMainActivity {

    private static final String TAG = "MainActivity";

    // 选择模块请求码
    private final static int REQ_CODE_SELECT_MODULE = 0x0;

    private final static int MSG_EXEC_SU = 0x1;

    private TextView mTvHint;
    private ModuleDBHelper mDBHelper;
    private Env mEnv;

    private Handler mHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {

                case MSG_EXEC_SU:
                    if ((boolean) msg.obj) {

                        mEnv.setWorkMode(Env.MODE_ROOT);
                    } else {
                        snackBar(R.string.device_not_root);
                        mEnv.setWorkMode(Env.MODE_NORMAL);
                    }

                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mDBHelper = ModuleDBHelper.getInstance();
        mEnv = Env.getInstance();

        mTvHint = findViewById(R.id.tv_hint);
        initFAB();
        initRecyclerViews();
    }

    private void initFAB() {
        final FloatingActionButton fab = findViewById(R.id.fab);

        final Env env = Env.getInstance();

        if (env.isWorkModeNotConfigure()) {
            fab.setImageResource(R.drawable.ic_build_white_24dp);
        }

        fab.setOnClickListener(v -> {
            // 选择工作模式
            if (env.isWorkModeNotConfigure()) {
                final MDialog mDialog = new MDialog.Builder(this)
                        .setHeaderBgColor(getResources().getColor(R.color.colorPrimary))
                        .setHeaderPic(R.drawable.ic_build_white_24dp)
                        .setTitle(R.string.dialog_title_select_work_mode)
                        .setCancelable(false)
                        .create();

                final Button posBtn = mDialog.getPositiveButton();
                final Button negBtn = mDialog.getNegativeButton();

                if (AndroidSystem.isRootedDevice()) { // 设备已ROOT
                    mDialog.setHTMLMessage(R.string.dialog_msg_work_mode_root);

                    posBtn.setText(R.string.work_mode_root);
                    negBtn.setText(R.string.work_mode_normal);

                    mDialog.setOnClickListener((dialog, mDialogAction) -> {
                        env.setWorkMode(
                                mDialogAction == MDialogAction.NEGATIVE
                                        ? Env.MODE_NORMAL : Env.MODE_ROOT
                        );
                        fab.setImageResource(R.drawable.ic_add_white_24dp);
                    });

                } else { // 设备未ROOT
                    mDialog.setHTMLMessage(R.string.dialog_msg_work_mode_normal);

                    posBtn.setText(R.string.understand);
                    negBtn.setText(R.string.device_root_already);

                    mDialog.setOnClickListener((dialog, mDialogAction) -> {
                        switch (mDialogAction) {
                            case POSITIVE:
                                env.setWorkMode(Env.MODE_NORMAL);
                                break;

                            case NEGATIVE: //执行 su 命令
                                tryExecRootCmd();
                                break;
                        }
                    });
                }

                mDialog.show();

            } else {
                final Intent intent = new Intent(this, ModulePickerActivity.class);
                startActivityForResult(intent, REQ_CODE_SELECT_MODULE);
            }
        });
    }

    // 执行su命令
    private void tryExecRootCmd() {
        new Thread(() -> {
            final Shell.Result ret = Shell.exec(true, true, "\n");
            final Message msg = mHandler.obtainMessage(MSG_EXEC_SU,
                    ret.resultCode == 0 || ret.resultCode == 1 || ret.toString().contains("denied"));
            mHandler.sendMessage(msg);
        }).start();
    }

    private void initRecyclerViews() {
        final RecyclerView rvMain = findViewById(R.id.rv_main);

        LinearLayoutManager llm = new LinearLayoutManager(this);
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        rvMain.setLayoutManager(llm);

        final List<Module> list = mDBHelper.queryAll();

        if (mEnv.isWorkModeNotConfigure()) {
            mTvHint.setVisibility(View.VISIBLE);
            mTvHint.setText(R.string.hint_config_work_mode);
            return;
        } else if (Objects.isEmptyList(list)) {
            mTvHint.setVisibility(View.VISIBLE);
            mTvHint.setText(R.string.hint_add_module);
            return;
        }
        final ModuleAdapter adapter = new ModuleAdapter(this, list);
        adapter.setMultiListener(new ModuleAdapter.MultiListener() {
            @Override
            public void onModuleSwitchChanged(Module module, boolean isCheck) {
                String moduleId = module.id;
                mDBHelper.update(moduleId, isCheck);
                if (isCheck) {
                    try {
                        mEnv.openModule(module, mDBHelper.queryDexBytes(moduleId));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else {
                    mEnv.closeModule(moduleId);
                }
            }
        });

        rvMain.setAdapter(adapter);
    }

    private void snackBar(@StringRes int resId) {
        Snackbar.make(findViewById(android.R.id.content), resId, Snackbar.LENGTH_SHORT).show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onWorkModeConfigured(int mode) {
        if (mTvHint == null || mDBHelper == null)
            return;
        if (mTvHint.getVisibility() == View.VISIBLE) {
            if (Objects.isEmptyList(mDBHelper.queryAll())) {
                mTvHint.setText(R.string.hint_add_module);
            }
        }
    }

    @Override
    public void onRootStateChanged(boolean isRooted) {

    }
}
