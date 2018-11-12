package com.poping520.dyxposed.util;

import android.content.Context;

import com.poping520.dyxposed.exception.DyXRuntimeException;
import com.poping520.dyxposed.system.Shell;

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
     * 创建文件夹 如果不存在
     *
     * @param path 绝对路径
     * @return 是否成功
     */
    public static boolean mkDirIfNotExists(String path) {
        final File dir = new File(path);

        if (!dir.isAbsolute())
            throw new IllegalArgumentException("not absolute path");

        if (dir.exists()) {
            if (dir.isFile())
                throw new DyXRuntimeException("已存在同名文件");
            else
                return dir.isDirectory();
        } else {
            if (dir.mkdirs()) {
                return true;
            } else {
                final String cmd = String.format("mkdir -p %s", path);
                return Shell.exec(false, false, cmd).isSuccess;
            }
        }
    }


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

        if (!mkDirIfNotExists(dstFile.getAbsolutePath())) {
            throw new IOException("创建文件夹失败");
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
