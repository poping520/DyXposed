package com.poping520.dyxposed.ui;

import android.animation.ObjectAnimator;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.annotation.UiThread;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.animation.DecelerateInterpolator;

import com.poping520.dyxposed.R;
import com.poping520.dyxposed.adapter.ModulePickerAdapter;
import com.poping520.dyxposed.api.AnnotationProcessor;
import com.poping520.dyxposed.framework.DyXCompiler;
import com.poping520.dyxposed.framework.ModuleDBHelper;
import com.poping520.dyxposed.model.FileItem;
import com.poping520.dyxposed.model.Module;
import com.poping520.dyxposed.model.Result;
import com.poping520.dyxposed.util.FileUtil;
import com.poping520.dyxposed.util.ModuleUtil;
import com.poping520.open.mdialog.MDialog;

import java.io.IOException;

import static android.animation.ObjectAnimator.ofFloat;

/**
 * 选择模块导入
 */
public class ModulePickerActivity extends AppCompatActivity {

    /**
     * 选择的文件/夹
     */
    public static final String EXTRA_NAME_SELECTED_FILE = "SelectedFile";

    // 错误码: java 代码编译失败
    private static final int ERR_CODE_COMPILE_FAIL = -0x1;

    // 错误码: 模块不合法
    private static final int ERR_CODE_ILLEGAL_MODULE = -0x2;

    // 错误码: 模块添加到数据库失败
    private static final int ERR_CODE_MODULE_ADD_FAIL = -0x3;

    // 错误码: 模块添加成功
    private static final int ERR_CODE_MODULE_ADD_SUCC = 0x1;

    public static final String EXTRA_KEY_MODULE_ID = "ModuleId";

    @Nullable
    private FileItem mSelectedFile;

    private FloatingActionButton mFab;
    private boolean mFabState;
    private ModulePickerAdapter mAdapter;
    private ModuleDBHelper mDBHelper;


    private Handler mUiHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            switch (msg.what) {
                case ERR_CODE_COMPILE_FAIL:
                case ERR_CODE_ILLEGAL_MODULE:
                    final int strResId = msg.what
                            == ERR_CODE_COMPILE_FAIL
                            ? R.string.dialog_title_compile_failed
                            : R.string.dialog_title_illegal_module;
                    makeDialog(strResId, (String) msg.obj);
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_module_picker);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        final ActionBar supportActionBar = getSupportActionBar();
        if (supportActionBar != null) {
            supportActionBar.setDisplayHomeAsUpEnabled(true);
        }

        mDBHelper = ModuleDBHelper.getInstance();
        mFab = findViewById(R.id.fab);

        initFAB();
        initRecyclerView();
    }

    private void onResult(String moduleId) {
        final Intent intent = new Intent();
        intent.putExtra(EXTRA_KEY_MODULE_ID, moduleId);
        setResult(RESULT_OK, intent);
        finish();
    }

    private void initFAB() {
        mFab.setOnClickListener(v -> {
            if (mSelectedFile == null) {
                return;
            }

            switch (mSelectedFile.type) {
                case FOLDER:
                    handleModuleSrc(mSelectedFile.file.getAbsolutePath(), false);
                    break;

                case ZIP:

                    break;

                case JAVA_SOURCE:
                    break;
            }
        });
    }

    /*
     * 编译模块
     * force 是否强制更新
     */
    private void handleModuleSrc(String srcPath, boolean force) {
        new Thread(() -> {
            // 编译
            final Result<String> compile = DyXCompiler.compile(srcPath);
            if (!compile.succ) {
                mUiHandler.sendMessage(
                        mUiHandler.obtainMessage(ERR_CODE_COMPILE_FAIL, compile.errMsg)
                );
                return;
            }

            // 注解处理
            String dexPath = compile.obj;
            final Result<Module> process = AnnotationProcessor.processDyXApi(dexPath);
            if (!process.succ) {
                mUiHandler.sendMessage(
                        mUiHandler.obtainMessage(ERR_CODE_ILLEGAL_MODULE, process.errMsg)
                );
                FileUtil.remove(dexPath);
                return;
            }

            // 添加到数据库
            final Module module = process.obj;
            final Module last = mDBHelper.query(module.id);
            if (last == null) {
                mUiHandler.post(() -> onInsertModule(module, dexPath));
            } else {
                // 已存在 ID 相同的模块
                mUiHandler.post(() -> onUpdateModule(module, dexPath, last.version, force));
            }
        }).start();
    }

    // 添加模块
    @UiThread
    private void onInsertModule(Module module, String dexPath) {
        try {
            mDBHelper.insert(module, FileUtil.readBytes(dexPath, true));
            final MDialog dialog = new MDialog.Builder(this)
                    .setHeaderBgColorRes(R.color.colorPrimary)
                    .setHeaderPic(R.drawable.ic_success_white_24dp)
                    .setTitle(R.string.success)
                    .setMessage(R.string.dialog_msg_module_add_succ, ModuleUtil.getShowName(module))
                    .setPositiveButton(R.string.ok, (mDialog, mDialogAction) -> {
                        onResult(module.id);
                    })
                    .create();
            dialog.getNegativeButton().setVisibility(View.INVISIBLE);
            dialog.show();
        } catch (IOException e) {
            FileUtil.remove(dexPath);
        }
    }

    // 更新模块
    @UiThread
    private void onUpdateModule(Module newModule, String newDexPath, String oldVer, boolean force) {
        if (force) {
            try {
                mDBHelper.update(newModule, FileUtil.readBytes(newDexPath, true));
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                FileUtil.remove(newDexPath);
                onResult(newModule.id);
            }
        } else {
            new MDialog.Builder(this)
                    .setHeaderBgColorRes(R.color.colorPrimary)
                    .setHeaderPic(R.drawable.ic_upgrade_white_24dp)
                    .setTitle(R.string.dialog_title_upgrade_module)
                    .setHTMLMessage(R.string.dialog_msg_upgrade_module,
                            ModuleUtil.getShowName(newModule), oldVer, newModule.version)
                    .setPositiveButton(R.string.ok, (mDialog, mDialogAction) -> {
                        try {
                            mDBHelper.update(newModule, FileUtil.readBytes(newDexPath, true));
                        } catch (IOException e) {
                            e.printStackTrace();
                        } finally {
                            FileUtil.remove(newDexPath);
                            onResult(newModule.id);
                        }
                    })
                    .setNegativeButton(R.string.cancel, null)
                    .show();
        }
    }


    private void makeDialog(@StringRes int title, String msg) {
        new MDialog.Builder(this)
                .setHeaderBgColorRes(R.color.colorPrimary)
                .setHeaderPic(R.drawable.ic_error_white_24dp)
                .setTitle(title)
                .setMessage(msg)
                .setPositiveButton(R.string.ok, null)
                .show();
    }

    private void initRecyclerView() {
        final RecyclerView rv = findViewById(R.id.rv_module_picker);

        LinearLayoutManager llm = new LinearLayoutManager(this);
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        rv.setLayoutManager(llm);

        mAdapter = new ModulePickerAdapter(this, new ModulePickerAdapter.OnMultiClickListener() {

            @Override
            public void onSingleFileClicked(FileItem item) {

            }

            @Override
            public void onItemSelected(FileItem item) {
                mSelectedFile = item;
                fabScaleAnim(true);
            }

            @Override
            public void onReleaseSelected() {
                fabScaleAnim(false);
            }
        });

        rv.setAdapter(mAdapter);
    }

    // fab 缩放动画
    private void fabScaleAnim(boolean show) {
        if (mFabState == show) {
            return;
        }
        mFabState = show;

        if (mFab.getVisibility() != View.VISIBLE) {
            mFab.setVisibility(View.VISIBLE);
        }
        float from, to;

        from = show ? 0f : 1f;
        to = show ? 1f : 0f;

        final ObjectAnimator scaleX = ofFloat(mFab, "scaleX", from, to)
                .setDuration(150);
        scaleX.setInterpolator(new DecelerateInterpolator());
        scaleX.start();

        final ObjectAnimator scaleY = ofFloat(mFab, "scaleY", from, to)
                .setDuration(150);
        scaleY.setInterpolator(new DecelerateInterpolator());
        scaleY.start();
    }

    @Override
    public void onBackPressed() {
        if (mAdapter.onBackPressed() == ModulePickerAdapter.NULL_STACK) {
            super.onBackPressed();
        }
    }
}
