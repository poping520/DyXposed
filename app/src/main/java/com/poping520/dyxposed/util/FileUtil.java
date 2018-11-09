package com.poping520.dyxposed.util;

import android.content.Context;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * @author WangKZ
 * @version 1.0.0
 * create on 2018/11/9 16:37
 */
public class FileUtil {

    /**
     * 从 APK Assets 中释放文件
     *
     * @param context Context
     * @param asset   输入文件 Asset相对路径
     * @param dst     输出文件路径
     * @param force   输出文件已存在，是否覆盖
     * @throws IOException
     */
    public static void unZipAsset(Context context, String asset, String dst, boolean force) throws IOException {
        final File dstFile = new File(dst);

        final File parentDir = dstFile.getParentFile();

        if (!parentDir.exists()) {
            if (!parentDir.mkdirs())
                throw new IOException("");
        }

        if (dstFile.exists()) {

            if (force) {
                if (!dstFile.delete())
                    throw new IOException("");
            } else {
                return;
            }
        }

        final InputStream is = context.getAssets().open(asset);
        final BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(dst));

        byte[] buf = new byte[1024];
        int len;
        while ((len = is.read(buf)) != -1) {
            bos.write(buf, 0, len);
        }
        bos.flush();
        is.close();
        bos.close();
    }
}
