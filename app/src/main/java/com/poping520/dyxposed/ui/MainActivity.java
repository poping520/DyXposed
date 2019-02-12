package com.poping520.dyxposed.ui;

import android.app.Activity;
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
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.poping520.dyxposed.R;
import com.poping520.dyxposed.adapter.ModuleAdapter;
import com.poping520.dyxposed.framework.BaseMainActivity;
import com.poping520.dyxposed.framework.DyXContext;
import com.poping520.dyxposed.framework.DyXEnv;
import com.poping520.dyxposed.model.Module;
import com.poping520.dyxposed.os.AndroidOS;
import com.poping520.dyxposed.os.Shell;
import com.poping520.dyxposed.util.Objects;
import com.poping520.open.mdialog.MDialog;
import com.poping520.open.mdialog.MDialogAction;

import org.json.JSONException;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.poping520.dyxposed.framework.ModuleHelper.*;


public class MainActivity extends BaseMainActivity {

    private static final String TAG = "MainActivity";

    // 选择模块请求码
    private final static int REQ_CODE_SELECT_MODULE = 0x0;

    private final static int MSG_EXEC_SU = 0x1;

    private FloatingActionButton mFAB;
    private TextView mTvHint;
    private DyXEnv mEnv;
    private ModuleAdapter mAdapter;

    /* 所有模块对象 */
    private List<Module> mAllModule;

    private Handler mHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {

                case MSG_EXEC_SU:
                    if ((boolean) msg.obj) {

                        mEnv.setWorkMode(DyXEnv.MODE_ROOT);
                    } else {
                        snackBar(R.string.device_not_root);
                        mEnv.setWorkMode(DyXEnv.MODE_NORMAL);
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

        mAllModule = mDBHelper.queryAllModule();
        mEnv = DyXEnv.getInstance();

        mTvHint = findViewById(R.id.tv_hint);
        if (mEnv.isWorkModeNotConfigure()) {
            mTvHint.setVisibility(View.VISIBLE);
            mTvHint.setText(R.string.hint_config_work_mode);
        } else if (Objects.isEmptyList(mAllModule)) {
            mTvHint.setVisibility(View.VISIBLE);
            mTvHint.setText(R.string.hint_add_module);
        }

        initFAB();
        initRecyclerView();
    }

    private void initFAB() {
        mFAB = findViewById(R.id.fab);

        final DyXEnv env = DyXEnv.getInstance();

        if (env.isWorkModeNotConfigure()) {
            mFAB.setImageResource(R.drawable.ic_build_white_24dp);
        }

        mFAB.setOnClickListener(v -> {
            // 选择工作模式
            if (env.isWorkModeNotConfigure()) {
                final MDialog mDialog = new MDialog.Builder(this)
                        .setHeaderBgColorRes(R.color.colorPrimary)
                        .setHeaderPic(R.drawable.ic_build_white_24dp)
                        .setTitle(R.string.dialog_title_select_work_mode)
                        .setCancelable(false)
                        .create();

                final Button posBtn = mDialog.getPositiveButton();
                final Button negBtn = mDialog.getNegativeButton();

                if (AndroidOS.isDeviceRooted()) { // 设备已ROOT
                    mDialog.setHTMLMessage(R.string.dialog_msg_work_mode_root);

                    posBtn.setText(R.string.work_mode_root);
                    negBtn.setText(R.string.work_mode_normal);

                    mDialog.setOnClickListener((dialog, mDialogAction) -> {
                        env.setWorkMode(
                                mDialogAction == MDialogAction.NEGATIVE
                                        ? DyXEnv.MODE_NORMAL : DyXEnv.MODE_ROOT
                        );
                        mFAB.setImageResource(R.drawable.ic_add_white_24dp);
                    });

                } else { // 设备未ROOT
                    mDialog.setHTMLMessage(R.string.dialog_msg_work_mode_normal);

                    posBtn.setText(R.string.understand);
                    negBtn.setText(R.string.device_root_already);

                    mDialog.setOnClickListener((dialog, mDialogAction) -> {
                        switch (mDialogAction) {
                            case POSITIVE:
                                env.setWorkMode(DyXEnv.MODE_NORMAL);
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

    private void initRecyclerView() {
        final RecyclerView rvMain = findViewById(R.id.rv_main);

        LinearLayoutManager llm = new LinearLayoutManager(this);
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        rvMain.setLayoutManager(llm);

        mAdapter = new ModuleAdapter(this, mAllModule);
        mAdapter.setMultiListener(new ModuleAdapter.MultiListener() {

            @Override
            public void onInsertModule(Module module) {
                if (mTvHint.getVisibility() == View.VISIBLE) {
                    mTvHint.setVisibility(View.GONE);
                }
            }

            @Override
            public void onModuleSwitchChanged(boolean isCheck, Module module) {
                String moduleId = module.id;
                mDBHelper.updateModule(moduleId, isCheck);
                if (isCheck) {
                    try {
                        mEnv.openModule(module, mDBHelper.queryModuleDexBytes(moduleId));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else {
                    mEnv.closeModule(module);
                }
            }

            @Override
            public void onDeleteModuleClick(int position, Module module) {
                new MDialog.Builder(MainActivity.this)
                        .setHeaderBgColorRes(R.color.colorPrimary)
                        .setHeaderPic(R.drawable.ic_delete_white_24dp)
                        .setTitle(R.string.delete_module)
                        .setHTMLMessage(R.string.dialog_msg_delete_module, getShowName(module))
                        .setPositiveButton(R.string.ok, (mDialog, mDialogAction) -> {
                            deleteModuleWithUndo(position, module);
                        })
                        .setNegativeButton(R.string.cancel, null)
                        .show();
            }
        });

        rvMain.setAdapter(mAdapter);
    }

    // 从数据库删除模块
    private void deleteModuleWithUndo(int position, Module module) {
        mAdapter.removeItem(position);

        final String msg = getString(R.string.snackbar_delete_module_confirm, getShowName(module));
        AtomicBoolean confirm = new AtomicBoolean(true);
        Snackbar.make(mFAB, msg, Snackbar.LENGTH_LONG)
                .setAction(R.string.undo, v -> {
                    confirm.set(false);
                    mAdapter.undoRemove(position, module);
                })
                .addCallback(new Snackbar.Callback() {
                    @Override
                    public void onDismissed(Snackbar transientBottomBar, int event) {
                        if (confirm.get())
                            mDBHelper.deleteModule(module.id);
                    }
                })
                .show();
    }

    private void snackBar(@StringRes int resId) {
        Snackbar.make(mFAB, resId, Snackbar.LENGTH_SHORT).show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case REQ_CODE_SELECT_MODULE:
                if (resultCode == RESULT_OK) {
                    final String moduleId = data.getStringExtra(ModulePickerActivity.EXTRA_KEY_MODULE_ID);
                    final int action = data.getIntExtra(ModulePickerActivity.EXTRA_KEY_ACTION, 0);
                    if (TextUtils.isEmpty(moduleId)) {
                        return;
                    }

                    if (action == ModulePickerActivity.ACTION_INSERT_MODULE) {
                        // 新增模块
                        mAdapter.insertItem(mDBHelper.queryModule(moduleId));
                    } else if (action == ModulePickerActivity.ACTION_UPDATE_MODULE) {
                        // 更新模块
                        mAdapter.updateItem(moduleId);
                    }
                }
                break;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                jumpActivity(SettingsActivity.class);
                break;
            case R.id.action_log:
                jumpActivity(LoggerActivity.class);
                break;
            case R.id.action_exit:
                DyXContext.safeExitApp();
                break;
        }
        return true;
    }

    private void jumpActivity(Class<? extends Activity> activityClz) {
        startActivity(new Intent(this, activityClz));
    }

    @Override
    public void onWorkModeConfigured(int mode) {
        if (mTvHint == null || mDBHelper == null)
            return;
        if (mTvHint.getVisibility() == View.VISIBLE) {
            if (Objects.isEmptyList(mAllModule)) {
                mTvHint.setText(R.string.hint_add_module);
            }
        }
    }

    @Override
    public void onRootStateChanged(boolean isRooted) {

    }
}
