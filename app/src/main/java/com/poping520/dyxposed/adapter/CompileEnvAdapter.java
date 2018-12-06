package com.poping520.dyxposed.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import com.poping520.dyxposed.R;
import com.poping520.dyxposed.model.Library;

import java.util.List;

/**
 * @author WangKZ
 * @version 1.0.0
 * create on 2018/12/5 16:58
 */
public class CompileEnvAdapter extends RecyclerView.Adapter<CompileEnvAdapter.ViewHolder> {

    private Context mContext;
    private List<Library> mList;

    public CompileEnvAdapter(Context context, List<Library> list) {
        mContext = context;
        mList = list;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        final View view = LayoutInflater
                .from(mContext)
                .inflate(R.layout.item_recycler_compile_env, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        final Library lib = mList.get(position);

        holder.name.setText(lib.getName());
        holder.enable.setChecked(lib.enable);
    }

    @Override
    public int getItemCount() {
        return mList == null || mList.isEmpty() ? 0 : mList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        private TextView name;
        private CheckBox enable;

        private ViewHolder(View itemView) {
            super(itemView);

            name = itemView.findViewById(R.id.name);
            enable = itemView.findViewById(R.id.enable);
        }
    }
}
