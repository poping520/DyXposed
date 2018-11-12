package com.poping520.dyxposed.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;

import com.poping520.dyxposed.R;
import com.poping520.dyxposed.model.Module;


import java.util.List;


/**
 * Created by WangKZ on 18/11/08.
 *
 * @author poping520
 * @version 1.0.0
 */
public class ModuleAdapter extends RecyclerView.Adapter<ModuleAdapter.ViewHolder> {

    private Context mContext;
    private List<Module> mList;

    public ModuleAdapter(Context context, List<Module> list) {
        mContext = context;
        mList = list;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        final View view = LayoutInflater
                .from(mContext)
                .inflate(R.layout.item_recycler_module, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        final Module module = mList.get(position);

        holder.mName.setText(module.name);
        holder.mDesc.setText(module.description);
        holder.mVersion.setText(module.version);
        holder.mSwitch.setChecked(module.enable);
    }

    @Override
    public int getItemCount() {
        return mList == null || mList.isEmpty() ? 0 : mList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        private ImageView mIcon;
        private TextView mName;
        private TextView mDesc;
        private TextView mVersion;
        private Switch mSwitch;


        public ViewHolder(View itemView) {
            super(itemView);

            mIcon = itemView.findViewById(R.id.module_icon);
            mName = itemView.findViewById(R.id.module_name);
            mDesc = itemView.findViewById(R.id.module_desc);
            mVersion = itemView.findViewById(R.id.module_version);
            mSwitch = itemView.findViewById(R.id.module_switch);

        }
    }
}
