package com.poping520.dyxposed.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.ListPopupWindow;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;

import com.poping520.dyxposed.R;
import com.poping520.dyxposed.model.Module;
import com.poping520.dyxposed.util.DimenUtil;
import com.poping520.dyxposed.util.ModuleUtil;
import com.poping520.dyxposed.util.Objects;

import java.util.ArrayList;
import java.util.Collections;
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
    private MultiListener mListener;
    private ArrayAdapter<String> mPopAdapter;

    public ModuleAdapter(Context context, List<Module> list) {
        mContext = context;
        mList = list;
        mPopAdapter = new ArrayAdapter<>(
                mContext,
                android.R.layout.simple_list_item_1,
                mContext.getResources().getStringArray(R.array.module_long_click_select_list)
        );
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
    public void onBindViewHolder(@NonNull final ViewHolder holder, final int position) {
        final Module module = mList.get(position);

        holder.mName.setText(ModuleUtil.getShowName(module));
        holder.mDesc.setText(ModuleUtil.getShowDesc(module));
        holder.mVersion.setText(module.version);
        holder.mSwitch.setChecked(module.enable);
        holder.mSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            mListener.onModuleSwitchChanged(isChecked, module);
        });

        holder.itemView.setLongClickable(true);
        holder.itemView.setOnLongClickListener(v -> {
            onItemLongClick(holder.mDesc, position, module);
            return true;
        });
    }

    private void onItemLongClick(View anchorView, int moduleItemPosition, Module module) {
        final ListPopupWindow pop = new ListPopupWindow(mContext);
        pop.setAdapter(mPopAdapter);
        pop.setAnchorView(anchorView);
        pop.setModal(true); // 设置为 true 响应物理键(点返回键 ListPopupWindow 消失)
        pop.setHeight(ListPopupWindow.WRAP_CONTENT);
        pop.setWidth(ListPopupWindow.WRAP_CONTENT);
        final int minWidth = (int) DimenUtil.dp2px(mContext, 120f);
        pop.setContentWidth(DimenUtil.measureAdapterContentWidth(mContext, mPopAdapter, minWidth));
        pop.setOnItemClickListener((parent, view, popItemPosition, id) -> {
            switch (popItemPosition) {
                case 0:
                    mListener.onDeleteModuleClick(moduleItemPosition, module);
                    break;
            }
            pop.dismiss();
        });
        pop.show();
    }

    /**
     * 增加一个条目
     */
    public void insertItem(Module module) {
        mList.add(module);
        notifyItemInserted(mList.size());
    }

    /**
     * 删除一个条目
     */
    public void removeItem(int position) {
        mList.remove(position);
        notifyItemRemoved(position);
    }

    /**
     * 删除撤销
     */
    public void undoRemove(int position, Module module) {
        mList.add(position, module);
        notifyItemInserted(position);
    }

    @Override
    public int getItemCount() {
        return mList == null || mList.isEmpty() ? 0 : mList.size();
    }

    public void setMultiListener(MultiListener listener) {
        this.mListener = Objects.requireNonNull(listener, "the listener is NULL");
    }

    public interface MultiListener {

        /**
         * 模块开关变化
         */
        void onModuleSwitchChanged(boolean isCheck, Module module);

        /**
         * 删除模块回调
         */
        void onDeleteModuleClick(int position, Module module);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        private ImageView mIcon;
        private TextView mName;
        private TextView mDesc;
        private TextView mVersion;
        private Switch mSwitch;

        ViewHolder(View itemView) {
            super(itemView);

            mIcon = itemView.findViewById(R.id.module_icon);
            mName = itemView.findViewById(R.id.module_name);
            mDesc = itemView.findViewById(R.id.module_desc);
            mVersion = itemView.findViewById(R.id.module_version);
            mSwitch = itemView.findViewById(R.id.module_switch);
        }
    }
}
