package com.poping520.dyxposed.forcestop;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * @author WangKZ
 * @version 1.0.0
 * create on 2018/12/11 15:59
 */
public class Targets implements Serializable {

    /* packageName <=> target */
    public Map<String, Target> targetMap = new HashMap<>();

    public static class Target implements Serializable {

        /* process name <=> pid */
        public Map<String, Integer> pidMap = new HashMap<>();
    }
}
