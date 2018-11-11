package com.poping520.dyxposed.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.poping520.dyxposed.R;
import com.poping520.dyxposed.model.FileItem;
import com.poping520.dyxposed.model.FileType;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by WangKZ on 18/11/11.
 *
 * @author poping520
 * @version 1.0.0
 */
public class FilePickerAdapter extends RecyclerView.Adapter<FilePickerAdapter.ViewHolder> {

    private Context mContext;
    private List<FileItem> mList;

    @Nullable
    private OnItemSelectedListener mListener;

    public FilePickerAdapter(Context context, File sdcard) {
        mContext = context;
        mList = getFileItemList(sdcard);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        final View view = LayoutInflater
                .from(mContext)
                .inflate(R.layout.item_recycler_file_picker, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        final FileItem item = mList.get(position);

        switch (item.type) {
            case ZIP:
                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        holder.mCheckBox.setChecked(!holder.mCheckBox.isChecked());
                    }
                });
                break;

            case FOLDER:
                holder.itemView.setOnClickListener(v -> {
                    final List<FileItem> list = getFileItemList(item.file);
                    if (list.size() == 0) {
                        Snackbar.make(holder.itemView, R.string.folder_is_empty,
                                Snackbar.LENGTH_SHORT).show();
                    } else {
                        mList = list;
                        notifyDataSetChanged();
                    }
                });
                break;

            case JAVA_SOURCE:
                break;
        }

        holder.mIcon.setImageResource(item.type.getResId());
        holder.mName.setText(item.name);

        holder.mCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked && mListener != null) {
                mListener.onItemSelected(item);
            }
        });
    }


    @Override
    public int getItemCount() {
        return mList == null || mList.isEmpty() ? 0 : mList.size();
    }

    private List<FileItem> getFileItemList(File dir) {
        if (dir == null || !dir.isDirectory()) return null;

        List<FileItem> list = new ArrayList<>();
        final File[] files = dir.listFiles();

        for (File file : files) {
            final String name = file.getName();

            if (file.isDirectory()) {
                list.add(new FileItem(FileType.FOLDER, name, file));
            } else if (file.isFile() && name.endsWith(".zip")) {
                list.add(new FileItem(FileType.ZIP, name, file));
            }
        }
        return list;
    }

    public void setOnItemSelectedListener(OnItemSelectedListener listener) {
        mListener = listener;
    }

    public interface OnItemSelectedListener {
        void onItemSelected(FileItem item);
    }

    static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private ImageView mIcon;
        private TextView mName;
        private CheckBox mCheckBox;

        private ViewHolder(View itemView) {
            super(itemView);

            mIcon = itemView.findViewById(R.id.file_icon);
            mName = itemView.findViewById(R.id.file_name);
            mCheckBox = itemView.findViewById(R.id.file_check_box);

            itemView.setOnClickListener(this);
        }


        @Override
        public void onClick(View v) {

        }
    }
}
