package com.poping520.dyxposed.util;

import android.content.Context;

import com.poping520.dyxposed.system.Shell;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * @author WangKZ
 * @version 1.0.0
 * create on 2018/11/9 16:37
 */
public final class FileUtil {

    /**
     * 文件转字节数组
     *
     * @param path   文件路径
     * @param delete 是否删除
     * @return byte 数组
     */
    public static byte[] readBytes(String path, boolean delete) throws IOException {
        final File file = new File(path);
        BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file));
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        byte[] buf = new byte[100 * 1024];
        int len;
        while ((len = bis.read(buf)) != -1) {
            baos.write(buf, 0, len);
        }

        if (delete) {
            if (!file.delete()) {
                Shell.exec(false, false, "rm -rf " + path);
            }
        }
        return baos.toByteArray();
    }

    public static boolean verifyMD5(File file, String md5) {
        if (!file.exists()) return false;
        return md5.equalsIgnoreCase(CryptoUtil.getFileMD5(file));
    }

    /**
     * 删除文件夹
     *
     * @param path 绝对路径
     * @return 是否成功
     */
    public static boolean remove(String path) {
        String cmd = "rm -rf " + path;
        return Shell.exec(false, false, cmd).success;
    }


    public static boolean remove(File dir) {
        return remove(dir.getAbsolutePath());
    }

    /**
     * 创建文件夹 如果不存在
     *
     * @param path  绝对路径
     * @param force 存在同名文件，是否覆盖
     * @return 是否成功
     */
    public static boolean mkDirIfNotExists(String path, boolean force) {
        final File dir = new File(path);

        if (!dir.isAbsolute())
            throw new IllegalArgumentException("not absolute path");

        if (dir.exists()) {
            if (dir.isDirectory())
                return true;
            else {
                if (remove(path))
                    return mkDirIfNotExists(path, force);
                else
                    return false;
            }
        } else {
            if (dir.mkdirs()) {
                return true;
            } else {
                final String cmd = String.format("mkdir -p %s", path);
                return Shell.exec(false, false, cmd).success;
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

        if (!mkDirIfNotExists(dstFile.getAbsolutePath(), force)) {
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

        byte[] buf = new byte[100 * 1024];
        int len;
        while ((len = is.read(buf)) != -1) {
            bos.write(buf, 0, len);
        }
        bos.flush();
        is.close();
        bos.close();
    }
}
