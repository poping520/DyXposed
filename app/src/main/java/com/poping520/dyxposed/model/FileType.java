package com.poping520.dyxposed.model;

import android.support.annotation.DrawableRes;

import com.poping520.dyxposed.R;

/**
 * Created by WangKZ on 18/11/11.
 *
 * @author poping520
 * @version 1.0.0
 */
public enum FileType {

    FOLDER(R.drawable.ic_folder_primary_42dp),

    ZIP(R.drawable.ic_zip_file_primary_42dp),

    JAVA_SOURCE(R.mipmap.ic_launcher);


    private int mResId;

    FileType(@DrawableRes int resId) {
        mResId = resId;
    }

    public int getResId() {
        return mResId;
    }
}
