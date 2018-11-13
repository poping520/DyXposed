package com.poping520.dyxposed.ui;

import android.animation.ObjectAnimator;
import android.content.Intent;
import android.support.annotation.Nullable;
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
import com.poping520.dyxposed.framework.ModuleManager;
import com.poping520.dyxposed.model.FileItem;
import com.poping520.dyxposed.model.ModuleSource;

import static android.animation.ObjectAnimator.ofFloat;

/**
 * 选择模块导入
 */
public class ModulePickerActivity extends AppCompatActivity {

    /**
     * 选择的文件/夹
     */
    public static final String EXTRA_NAME_SELECTED_FILE = "SelectedFile";

    @Nullable
    private FileItem mSelectedFile;

    private FloatingActionButton mFab;
    private boolean mFabState;
    private ModulePickerAdapter mAdapter;

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

        mFab = findViewById(R.id.fab);

        initFAB();
        initRecyclerView();
    }

    private void initFAB() {
        mFab.setOnClickListener(v -> {
            if (mSelectedFile == null) {
                return;
            }

            switch (mSelectedFile.type) {
                case FOLDER:
                    final ModuleSource src = new ModuleSource();
                    src.path = mSelectedFile.file.getAbsolutePath();
                    new ModuleManager().parseModule(src);
                    break;

                case ZIP:
                    break;

                case JAVA_SOURCE:
                    break;
            }

            final Intent intent = new Intent();
            intent.putExtra(EXTRA_NAME_SELECTED_FILE, mSelectedFile);
            setResult(RESULT_OK, intent);
        });
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
