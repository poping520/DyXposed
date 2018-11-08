package com.poping520.dyxposed.ui;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.poping520.dyxposed.R;
import com.poping520.dyxposed.adapter.ModuleRecyclerViewAdapter;
import com.poping520.dyxposed.framework.BaseActivity;
import com.poping520.dyxposed.framework.Module;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends BaseActivity {

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
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(v -> {

        });
    }

    private void initRecyclerViews() {
        final RecyclerView rvMain = findViewById(R.id.rv_main);

        LinearLayoutManager llm = new LinearLayoutManager(this);
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        rvMain.setLayoutManager(llm);

        List<Module> list = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            final Module module = new Module();
            module.name = "Test";
            module.version = "1.0.0";
            module.description = "description";
            list.add(module);
        }

        final ModuleRecyclerViewAdapter adapter = new ModuleRecyclerViewAdapter(this, list);

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
