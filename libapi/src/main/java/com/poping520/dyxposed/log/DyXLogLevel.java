package com.poping520.dyxposed.log;

/**
 * 日志等级
 *
 * @author WangKZ
 * @version 1.0.0
 * create on 2019/1/21 14:00
 */
public enum DyXLogLevel {

    INFO("I"),

    WARN("W"),

    ERROR("E"),

    UNKNOWN("UNKNOWN");

    private String mKwd;

    DyXLogLevel(String kwd) {
        mKwd = kwd;
    }

    public String getLevel() {
        return mKwd;
    }

    /**
     * @param kwd key word
     */
    static DyXLogLevel parseLevel(String kwd) {
        final DyXLogLevel[] values = DyXLogLevel.values();

        for (DyXLogLevel value : values) {
            if (value.mKwd.equals(kwd)) {
                return value;
            }
        }
        return UNKNOWN;
    }
}
