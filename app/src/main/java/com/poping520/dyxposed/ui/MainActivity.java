package com.poping520.dyxposed.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;

import com.poping520.dyxposed.R;
import com.poping520.dyxposed.adapter.ModuleAdapter;
import com.poping520.dyxposed.framework.BaseMainActivity;
import com.poping520.dyxposed.framework.DyXCompiler;
import com.poping520.dyxposed.framework.Env;
import com.poping520.dyxposed.model.Module;
import com.poping520.dyxposed.model.FileItem;
import com.poping520.dyxposed.system.AndroidSystem;
import com.poping520.open.mdialog.MDialog;
import com.poping520.open.mdialog.MDialogAction;

import java.util.ArrayList;
import java.util.List;

import static com.poping520.dyxposed.ui.ModulePickerActivity.EXTRA_NAME_SELECTED_FILE;

public class MainActivity extends BaseMainActivity {

    private static final String TAG = "MainActivity";

    // 选择模块请求码
    private final static int REQ_CODE_SELECT_MODULE = 0x0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

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
                    mDialog.getPositiveButton().setText(R.string.go_on);
                    mDialog.getNegativeButton().setText("设备已ROOT");
                }

                mDialog.show();

            } else {
                final Intent intent = new Intent(this, ModulePickerActivity.class);
                startActivityForResult(intent, REQ_CODE_SELECT_MODULE);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // 处理模块 compile => class => dex
        if (requestCode == REQ_CODE_SELECT_MODULE && resultCode == RESULT_OK) {
            FileItem item = (FileItem) data.getSerializableExtra(EXTRA_NAME_SELECTED_FILE);

            switch (item.type) {
                case ZIP:

                    break;

                case FOLDER:

                    break;

                case JAVA_SOURCE:
                    break;
            }
        }
    }

    private void initRecyclerViews() {
        final RecyclerView rvMain = findViewById(R.id.rv_main);

        LinearLayoutManager llm = new LinearLayoutManager(this);
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        rvMain.setLayoutManager(llm);

        List<Module> list = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            final Module module = new Module();
//            module.name = "Test";
//            module.version = "1.0.0";
//            module.description = "description";
            list.add(module);
        }

        final ModuleAdapter adapter = new ModuleAdapter(this, list);

        rvMain.setAdapter(adapter);
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
}
