package com.poping520.dyxposed.ui;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;

import com.poping520.dyxposed.R;
import com.poping520.dyxposed.adapter.CompileEnvAdapter;
import com.poping520.dyxposed.framework.DyXContext;
import com.poping520.dyxposed.framework.DyXDBHelper;
import com.poping520.dyxposed.model.Library;
import com.poping520.open.mdialog.MDialog;

import java.util.List;

public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fl_settings, new SettingsPreferenceFragment())
                .commit();
    }

    public static class SettingsPreferenceFragment extends PreferenceFragmentCompat {

        private DyXDBHelper mDBHelper;

        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            addPreferencesFromResource(R.xml.pref_settings);

            mDBHelper = DyXDBHelper.getInstance();
            final Context context = getContext();

            final Preference prefCompileEnv = findPreference(R.string.pref_key_compile_env);

            prefCompileEnv.setOnPreferenceClickListener(preference -> {

                final RecyclerView recyclerView = new RecyclerView(context);
                recyclerView.setLayoutParams(
                        new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
                recyclerView.setLayoutManager(new LinearLayoutManager(context));

                List<Library> list = mDBHelper.queryAllLib();
                recyclerView.setAdapter(new CompileEnvAdapter(context, list));

                new MDialog.Builder(context)
                        .setHeaderBgColorRes(R.color.colorPrimary)
                        .setTitle(R.string.pref_title_compile_env)
                        .setContentView(recyclerView)
                        .setPositiveButton(R.string.ok, null)
                        .show();
                return false;
            });
        }

        public Preference findPreference(@StringRes int stringResId) {
            return super.findPreference(getString(stringResId));
        }

        @NonNull
        @Override
        public Context getContext() {
            final Context context = super.getContext();
            if (context == null) {
                return DyXContext.getApplicationContext();
            }
            return context;
        }
    }
}
