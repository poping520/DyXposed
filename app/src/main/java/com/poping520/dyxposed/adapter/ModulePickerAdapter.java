package com.poping520.dyxposed.adapter;

import android.content.Context;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.poping520.dyxposed.R;
import com.poping520.dyxposed.model.FileItem;
import com.poping520.dyxposed.model.FileType;
import com.poping520.dyxposed.util.Arrays;

import java.io.File;
import java.io.FileFilter;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by WangKZ on 18/11/11.
 *
 * @author poping520
 * @version 1.0.0
 */
public class ModulePickerAdapter extends RecyclerView.Adapter<ModulePickerAdapter.ViewHolder> {

    private static final String TAG = "ModulePickerAdapter";

    private Context mContext;
    private List<FileItem> mList;

    @Nullable
    private OnItemSelectedListener mListener;

    public ModulePickerAdapter(Context context) {
        mContext = context;
        mList = getFileItemList(Environment.getExternalStorageDirectory());
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

        holder.mIcon.setImageResource(item.type.getResId());
        holder.mName.setText(item.name);

        switch (item.type) {
            case ZIP:
                holder.itemView.setOnClickListener(v -> {
                    if (mListener != null) {
                        mListener.onItemSelected(item);
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


        holder.itemView.setLongClickable(true);
        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {


                return true;
            }
        });
    }

    @Override
    public int getItemCount() {
        return mList == null || mList.isEmpty() ? 0 : mList.size();
    }

    // 条件性遍历目录
    private List<FileItem> getFileItemList(File dir) {
        if (dir == null || !dir.isDirectory()) return null;

        final File[] files = dir.listFiles(pathname -> {
            final String name = pathname.getName();
            // 过滤 .开头的文件/夹
            if (name.startsWith(".")) {
                return false;
            }

            if (pathname.isFile()) {
                //过滤 扩展名不为 zip
                return name.endsWith(".zip") || name.endsWith(".java");
            }
            return true;
        });

        List<FileItem> list = new ArrayList<>();
        List<FileItem> fileList = new ArrayList<>();

        for (File file : files) {
            final String name = file.getName();
            if (file.isDirectory()) {
                list.add(new FileItem(FileType.FOLDER, name, file));
            } else if (file.isFile()) {
                fileList.add(new FileItem(FileType.ZIP, name, file));
            }
        }

        // 排序
        final CharComparator charComparator = new CharComparator();
        Collections.sort(list, charComparator);
        Collections.sort(fileList, charComparator);
        list.addAll(fileList);
        return list;
    }

    // 文件名 按字母排序 算法
    private static class CharComparator implements Comparator<FileItem> {

        @Override
        public int compare(FileItem o1, FileItem o2) {

            final String str1 = o1.name;
            final String str2 = o2.name;

            if (str1.equals(str2)) return 0;

            final int i = 'a' - 'A';

            int index = 0;
            char c1, c2;
            while (true) {
                if (index >= str1.length())
                    return 1;
                if (index >= str2.length())
                    return -1;

                c1 = str1.charAt(index);
                c2 = str2.charAt(index);

                // 大小写转换
                if (c1 >= 'a' && c1 <= 'z')
                    c1 -= i;
                if (c2 >= 'a' && c2 <= 'z')
                    c2 -= i;

                if (c1 != c2) break;
                ++index;
            }

            return c1 > c2 ? 1 : -1;
        }
    }

    public static void main(String[] args) {
        System.out.println('0' + 0);
        System.out.println('A' + 0);
        System.out.println('Z' + 0);
        System.out.println('a' + 0);
        System.out.println('z' + 0);
    }

    public void setOnItemSelectedListener(OnItemSelectedListener listener) {
        mListener = listener;
    }

    public interface OnItemSelectedListener {
        void onItemSelected(FileItem item);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        private ImageView mIcon;
        private TextView mName;

        private ViewHolder(View itemView) {
            super(itemView);

            mIcon = itemView.findViewById(R.id.file_icon);
            mName = itemView.findViewById(R.id.file_name);
        }
    }
}
