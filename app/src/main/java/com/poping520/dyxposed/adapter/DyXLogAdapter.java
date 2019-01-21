package com.poping520.dyxposed.adapter;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.poping520.dyxposed.R;
import com.poping520.dyxposed.log.DyXLogHandler;
import com.poping520.dyxposed.log.DyXLogLevel;

import java.util.List;

/**
 * @author WangKZ
 * @version 1.0.0
 * create on 2019/1/21 14:47
 */
public class DyXLogAdapter extends RecyclerView.Adapter<DyXLogAdapter.ViewHolder> {

    private static final int ERROR_COLOR = Color.RED;
    private static final int WARN_COLOR = 0xFFFFB300;

    private Context mContext;
    private List<String> mList;

    public DyXLogAdapter(Context context, List<String> list) {
        mContext = context;
        mList = list;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        final View view = LayoutInflater
                .from(mContext)
                .inflate(R.layout.item_recycler_log, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        final String line = mList.get(position);

        holder.mTvLog.setText(line);

        final DyXLogLevel logLevel = DyXLogHandler.getLogLevel(line);
        switch (logLevel) {
            case WARN:
                holder.mTvLog.setTextColor(WARN_COLOR);
                break;

            case ERROR:
                holder.mTvLog.setTextColor(ERROR_COLOR);
                break;
        }
    }

    @Override
    public int getItemCount() {
        return mList == null || mList.isEmpty() ? 0 : mList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        private TextView mTvLog;

        ViewHolder(View itemView) {
            super(itemView);
            mTvLog = itemView.findViewById(R.id.tv_log_item);
        }
    }
}
