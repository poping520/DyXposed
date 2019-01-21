package com.poping520.dyxposed.ui;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;

import com.poping520.dyxposed.R;
import com.poping520.dyxposed.adapter.DyXLogAdapter;
import com.poping520.dyxposed.util.FileUtil;

import java.io.File;
import java.util.List;

public class LoggerActivity extends AppCompatActivity {

    private static final String TAG = "LoggerActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_logger);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        initViews();
    }

    private void initViews() {
        final RecyclerView rv = findViewById(R.id.rv_log);
        rv.setLayoutManager(new LinearLayoutManager(this));

        final List<String> logList = FileUtil.readTextFileToList(new File("/data/data/com.poping520.dyxposed/shared/dyxposed.log"));
        rv.setAdapter(new DyXLogAdapter(this, logList));
    }
}
