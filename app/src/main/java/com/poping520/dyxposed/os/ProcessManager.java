package com.poping520.dyxposed.os;

import android.text.TextUtils;

import com.poping520.dyxposed.util.FileUtil;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * 保存 Android 系统进程信息
 * Android 7.0 及以上需要 ROOT 权限 才能获得全部进程信息
 *
 * <p>
 * 本类在程序运行中只有一个实例 {@link AndroidOS#getProcessManager()}
 *
 * @author WangKZ
 * @version 1.0.0
 * create on 2018/12/8 10:20
 */
public final class ProcessManager {

    private final List<ProcessInfo> mList = new ArrayList<>();

    ProcessManager() {
    }

    /**
     * 通过程序包名, 获取该程序的所有进程
     *
     * @param packageName 程序包名
     * @return 进程集合
     */
    public List<ProcessInfo> getProcessInfos(String packageName) {
        update();

        List<ProcessInfo> list = new ArrayList<>();
        for (ProcessInfo ap : mList) {
            if (ap.name.contains(packageName)) {
                list.add(ap);
            }
        }
        return list;
    }

    /**
     * @return 当前系统 所有进程信息
     */
    public List<ProcessInfo> getProcessInfos() {
        update();
        return mList;
    }

    // 更新集合数据
    private void update() {
        mList.clear();
        final File[] files = new File("/proc").listFiles(File::isDirectory);
        for (File file : files) {
            try {
                int pid = Integer.parseInt(file.getName());
                mList.add(parseAndroidProcess(pid));
            } catch (NumberFormatException ignored) {

            }
        }
    }

    private ProcessInfo parseAndroidProcess(int pid) {
        String dir = String.format(Locale.ENGLISH, "/proc/%d", pid);

        String name;
        int uid = -1, gid = -1;
        ProcessInfo.State state = null;

        name = FileUtil.readTextFile(new File(dir, "cmdline"));
        if (TextUtils.isEmpty(name)) {
            name = FileUtil.readTextFile(new File(dir, "comm"));
        }

        final File status = new File(dir, "status");
        BufferedReader br = null;

        try {
            br = new BufferedReader(new FileReader(status));
            String line;
            while ((line = br.readLine()) != null) {
                if (line.startsWith("State:")) {
                    state = parseState(line.split("State:")[1].trim());
                } else if (line.startsWith("Uid:")) {
                    uid = parseId(line.split("Uid:")[1].trim());
                } else if (line.startsWith("Gid:")) {
                    gid = parseId(line.split("Gid:")[1].trim());
                }
            }
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
        return new ProcessInfo(pid, uid, gid, name, state);
    }

    private int parseId(String str) {
        int ret = -1;
        try {
            ret = Integer.parseInt(str.split("\\s+")[0]);
        } catch (NumberFormatException ignored) {

        }
        return ret;
    }

    private ProcessInfo.State parseState(String str) {
        final ProcessInfo.State[] states = ProcessInfo.State.values();
        for (ProcessInfo.State state : states) {
            if (str.startsWith(state.start)) {
                return state;
            }
        }
        return null;
    }
}
