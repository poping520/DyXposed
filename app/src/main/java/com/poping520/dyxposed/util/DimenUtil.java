package com.poping520.dyxposed.util;

import android.content.Context;
import android.util.DisplayMetrics;
import android.util.TypedValue;

/**
 * @author WangKZ
 * @version 1.0.0
 * create on 2018/11/13 11:48
 */
public class DimenUtil {

    /**
     * dp 转 px
     */
    public static float dp2px(Context context, float dpValue) {
        DisplayMetrics dm = context.getResources().getDisplayMetrics();
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dpValue, dm);
    }
}
