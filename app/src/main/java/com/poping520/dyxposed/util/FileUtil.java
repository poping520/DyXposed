package com.poping520.dyxposed.util;

import android.content.Context;
import android.support.annotation.Nullable;

import com.poping520.dyxposed.os.Shell;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;

/**
 * @author WangKZ
 * @version 1.0.0
 * create on 2018/11/9 16:37
 */
public final class FileUtil {

    /**
     * 解压 zip 文件
     *
     * @param src
     * @param dst
     * @param force
     * @return
     */
    public static void unZip(String src, String dst, boolean force) throws IOException {

    }

    public static boolean writeStringToFile(String dstPath, String str, boolean force)
            throws IOException {
        return writeStringToFile(new File(dstPath), str, force);
    }

    /**
     * 字符串对象保存到文件
     *
     * @param dst   输出文件
     * @param str   要写入字符串
     * @param force 存在同名文件/夹是否删除
     * @return 是否成功
     */
    public static boolean writeStringToFile(File dst, String str, boolean force) throws IOException {
        if (dst.exists()) {
            if (force) {
                if (remove(dst))
                    return writeStringToFile(dst, str, true);
                else
                    throw new IOException("已存在的同名文件/夹无法删除");
            } else return false;
        } else {
            final File parentDir = dst.getParentFile();
            if (!parentDir.exists() && !mkDirIfNotExists(parentDir, force))
                throw new IOException("无法创建父级目录");

            BufferedWriter bw = new BufferedWriter(new FileWriter(dst));
            bw.write(str);
            bw.flush();
            bw.close();
            return true;
        }
    }

    public static boolean writeBytes(String dstFile, byte[] bytes, boolean force)
            throws IOException {
        return writeBytes(new File(dstFile), bytes, force);
    }

    /**
     * 字节数组写到文件
     *
     * @param dst   输出文件
     * @param bytes 字节数组
     * @param force 是否覆盖写入
     * @return 是否成功
     */
    public static boolean writeBytes(File dst, byte[] bytes, boolean force) throws IOException {
        if (dst.exists()) {
            if (force) {
                if (remove(dst))
                    return writeBytes(dst, bytes, true);
                else
                    throw new IOException("已存在的同名文件/夹无法删除");
            } else return false;
        } else {
            final File parentDir = dst.getParentFile();
            if (!parentDir.exists() && !mkDirIfNotExists(parentDir, force))
                throw new IOException("无法创建父级目录");

            BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(dst));
            bos.write(bytes);
            bos.flush();
            bos.close();
            return true;
        }
    }

    /**
     * 读取文本文件
     *
     * @param path 源文件路径
     * @return 文本字符串
     */
    @Nullable
    public static String readTextFile(String path) {
        return readTextFile(new File(path));
    }

    /**
     * 读取文本文件
     *
     * @param src 源文件
     * @return 文本字符串
     */
    @Nullable
    public static String readTextFile(File src) {
        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader(src));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line).append("\n");
            }
            return sb.toString();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

    /**
     * 文件转字节数组
     *
     * @param path   文件路径
     * @param delete 是否删除
     * @return byte 数组
     */
    public static byte[] readFileToBytes(String path, boolean delete) throws IOException {
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
                remove(file);
            }
        }
        return baos.toByteArray();
    }

    /**
     * 验证文件是否存在和 md5 是否一致
     *
     * @param file 目标文件
     * @param md5  md5值
     * @return 是否验证成功
     */
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


    public static boolean remove(File file) {
        return remove(file.getAbsolutePath());
    }

    public static boolean mkDirIfNotExists(File dir, boolean force) {
        return mkDirIfNotExists(dir.getAbsolutePath(), force);
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

        if (dir.exists())
            if (dir.isDirectory())
                return true;
            else if (force && remove(path))
                return mkDirIfNotExists(path, true);
            else
                return false;
        else if (dir.mkdirs())
            return true;
        else
            return Shell.exec(false, false,
                    String.format("mkdir -p %s", path)).success;
    }

    /**
     * 从 APK LibraryAssets 中释放文件
     *
     * @param context Context
     * @param asset   输入文件 Asset相对路径
     * @param dst     输出文件路径
     * @param force   输出文件已存在，是否覆盖
     * @throws IOException
     */
    public static void unZipAsset(Context context, String asset, String dst, boolean force) throws
            IOException {
        final File dstFile = new File(dst);

        if (!mkDirIfNotExists(dstFile.getParent(), force)) {
            throw new IOException("创建文件夹失败");
        }

        if (dstFile.exists()) {
            if (force) {
                if (remove(dstFile))
                    unZipAsset(context, asset, dst, true);
                else
                    throw new IOException("已存在的同名文件/夹无法删除");
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
