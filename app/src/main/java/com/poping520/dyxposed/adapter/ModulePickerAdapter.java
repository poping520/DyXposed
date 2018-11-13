package com.poping520.dyxposed.adapter;

import android.animation.ObjectAnimator;
import android.app.Activity;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.poping520.dyxposed.R;
import com.poping520.dyxposed.model.FileItem;
import com.poping520.dyxposed.model.FileType;
import com.poping520.dyxposed.util.DimenUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by WangKZ on 18/11/11.
 *
 * @author poping520
 * @version 1.0.0
 */
public class ModulePickerAdapter extends RecyclerView.Adapter<ModulePickerAdapter.ViewHolder> {

    private static final String TAG = "ModulePickerAdapter";

    private Activity mActivity;

    private final float m20dp;

    @NonNull
    private List<FileItem> mList;

    // 模拟一个栈结构
    @NonNull
    private final LinkedList<List<FileItem>> mStack;

    /**
     * 栈为空，当此时点击返回按钮，应该退出界面
     */
    public static final int NULL_STACK = 0x0;

    private OnMultiClickListener mListener;

    // 被选择的条目
    private FileItem mSelectedItem;

    private View mSelectedItemView;

    public ModulePickerAdapter(Activity activity, OnMultiClickListener onItemSelectedListener) {
        if (onItemSelectedListener == null) {
            throw new IllegalArgumentException("onItemSelectedListener is NULL");
        }

        mActivity = activity;
        mListener = onItemSelectedListener;

        m20dp = DimenUtil.dp2px(mActivity, 20f);

        mList = getFileItemList(
                new File(Environment.getExternalStorageDirectory(), "DyXposed")
        );
        mStack = new LinkedList<>();
        // 第一个元素压栈
        mStack.push(mList);

        if (mList.isEmpty()) {
            makeNullFolderSnackbar(mActivity.findViewById(android.R.id.content));
        }
    }

    /**
     * 点击返回键 模拟栈操作
     *
     * @return 是否为 {@link #NULL_STACK}
     */
    public int onBackPressed() {
        // 栈内至少有一个元素 否则状态异常
        if (mStack.size() == NULL_STACK)
            throw new IllegalStateException("this stack must has one element at least");

        // 弹出栈顶元素
        mStack.pop();

        // 栈已经为空
        if (mStack.isEmpty()) {
            return NULL_STACK;
        } else {
            // 获取当前栈顶元素
            mList = mStack.getFirst();
            notifyDataSetChanged();
            return ~NULL_STACK;
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        final View view = LayoutInflater
                .from(mActivity)
                .inflate(R.layout.item_recycler_file_picker, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        final FileItem item = mList.get(position);

        holder.mIcon.setImageResource(item.type.getResId());
        holder.mName.setText(item.name);

        // 平移动画
        ObjectAnimator.ofFloat(holder.itemView, "translationY", m20dp, 0f)
                .setDuration(300)
                .start();

        // 长按操作
        holder.itemView.setLongClickable(true);
        holder.itemView.setOnLongClickListener(v -> {
            if (hasSelectedItem()) {
                releaseSelected(false);
            }
            onItemSelected(holder.itemView, item);

            return true;
        });

        // 点击操作
        holder.itemView.setOnClickListener(v -> {
            if (hasSelectedItem()) {
                releaseSelected(true);
            }

            switch (item.type) {
                case ZIP:
                    mListener.onSingleFileClicked(item);
                    break;

                case FOLDER:
                    final List<FileItem> list = getFileItemList(item.file);
                    if (list.size() == 0) {
                        makeNullFolderSnackbar(holder.itemView);
                    } else {
                        mList = list;
                        notifyDataSetChanged();

                        //压栈
                        mStack.push(mList);
                    }
                    break;

                case JAVA_SOURCE:
                    break;
            }
        });
    }

    @Override
    public int getItemCount() {
        return mList.isEmpty() ? 0 : mList.size();
    }

    // 选中条目
    private void onItemSelected(View itemView, FileItem item) {
        mSelectedItemView = itemView;
        mSelectedItem = item;
        mSelectedItemView.setBackgroundColor(mActivity.getResources().getColor(R.color.item_selected_bg));
        mListener.onItemSelected(mSelectedItem);
    }

    // 是否有被选中的条目
    private boolean hasSelectedItem() {
        return mSelectedItem != null;
    }

    /**
     * 重置选中
     *
     * @param notify 是否通知监听回调
     */
    private void releaseSelected(boolean notify) {
        mSelectedItemView.setBackground(null);
        mSelectedItem = null;
        mSelectedItemView = null;
        if (notify) {
            mListener.onReleaseSelected();
        }
    }

    private void makeNullFolderSnackbar(View view) {
        Snackbar.make(view, R.string.folder_is_empty, Snackbar.LENGTH_SHORT).show();
    }

    // 条件性遍历目录
    @NonNull
    private List<FileItem> getFileItemList(File dir) {
        if (dir == null || !dir.isDirectory()) return Collections.emptyList();

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

    /**
     * {@link ModulePickerAdapter} 监听器
     */
    public interface OnMultiClickListener {

        /**
         * 点击单个文件
         */
        void onSingleFileClicked(FileItem item);

        /**
         * 选中文件 or 文件夹
         */
        void onItemSelected(FileItem item);

        /**
         * 取消选中
         */
        void onReleaseSelected();
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
