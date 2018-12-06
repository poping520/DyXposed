package com.poping520.dyxposed.framework;

/**
 * 程序设置
 *
 * @author WangKZ
 * @version 1.0.0
 * create on 2018/12/6 15:08
 */
public final class DyXSettings {

    private static final String SP_NAME = "dyxposed_settings";

    private static final String SPK_KILL_TARGET = "KillTarget";

    private static final String SPK_USING_ROOT = "UsingRoot";

    private static final DyXSharedPrefs SP = DyXContext.getDyXSharedPrefs(SP_NAME);


    public static void setUsingRoot(boolean isUsingRoot) {
        SP.put(SPK_USING_ROOT, isUsingRoot);
    }

    public static boolean isUsingRoot() {
        return SP.get(SPK_USING_ROOT, false);
    }

    public static void setKillTarget(boolean isKillTarget) {
        SP.put(SPK_KILL_TARGET, isKillTarget);
    }

    public static boolean isKillTarget() {
        return SP.get(SPK_KILL_TARGET, false);
    }
}
