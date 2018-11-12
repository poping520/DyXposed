package com.poping520.dyxposed.ui;

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

import com.poping520.dyxposed.R;
import com.poping520.dyxposed.adapter.FilePickerAdapter;
import com.poping520.dyxposed.model.FileItem;

public class FilePickerActivity extends AppCompatActivity {

    @Nullable
    private FileItem selectedFile;

    /**
     * 选择的文件/夹
     */
    public static final String EXTRA_NAME_SELECTED_FILE = "SelectedFile";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_file_picker);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        final ActionBar supportActionBar = getSupportActionBar();
        if (supportActionBar != null) {
            supportActionBar.setDisplayHomeAsUpEnabled(true);
        }


        final FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(v -> {
            final Intent intent = new Intent();
            intent.putExtra(EXTRA_NAME_SELECTED_FILE, selectedFile);
            setResult(RESULT_OK, intent);
            finish();
        });

        final RecyclerView rv = findViewById(R.id.rv_file_picker);

        LinearLayoutManager llm = new LinearLayoutManager(this);
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        rv.setLayoutManager(llm);

        final FilePickerAdapter adapter = new FilePickerAdapter(this);

        adapter.setOnItemSelectedListener(item -> {
            selectedFile = item;
            fab.setVisibility(View.VISIBLE);
        });

        rv.setAdapter(adapter);
    }
}
