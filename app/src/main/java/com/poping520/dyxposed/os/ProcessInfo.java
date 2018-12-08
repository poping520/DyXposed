package com.poping520.dyxposed.os;

/**
 * Android Process Model
 *
 * @author WangKZ
 * @version 1.0.0
 * create on 2018/12/8 11:56
 */
public class ProcessInfo {

    /**
     * process id
     */
    public int pid;

    /**
     * user id
     */
    public int uid;

    /**
     * group id
     */
    public int gid;

    /**
     * 进程名
     */
    public String name;

    /**
     * 进程状态
     */
    public State state;

    ProcessInfo(int pid, int uid, int gid, String name, State state) {
        this.pid = pid;
        this.uid = uid;
        this.gid = gid;
        this.name = name;
        this.state = state;
    }

    @Override
    public String toString() {
        return "ProcessInfo{" +
                "pid=" + pid +
                ", uid=" + uid +
                ", gid=" + gid +
                ", name='" + name + '\'' +
                ", state=" + state +
                '}';
    }

    /**
     * 进程目前的状态
     * <li>R (running)</li>
     * <li>S (sleeping)</li>
     * <li>D (disk sleep)</li>
     * <li>T (stopped)</li>
     * <li>t (tracing stop)</li>
     * <li>X (dead)</li>
     * <li>Z (zombie)</li>
     */
    public enum State {

        /**
         * 运行状态
         */
        RUNNING("R"),

        /**
         * 睡眠状态
         */
        SLEEPING("S"),

        /**
         * 磁盘休眠状态
         */
        DISK_SLEEP("D"),

        /**
         * 暂停状态
         */
        STOPPED("T"),

        /**
         * 用于debug调试跟踪
         */
        TRACING_STO("t"),

        /**
         * 死亡状态
         */
        DEAD("X"),

        /**
         * 僵死状态
         */
        ZOMBIE("Z");

        public String start;

        State(String start) {
            this.start = start;
        }
    }
}
