package com.gui;

import java.io.Serializable;

public class DriveInfo implements Serializable {
    public final static int GB = 1024 * 1024 * 1024;

    private String name;
    private int free;
    private int total;

    public DriveInfo(String name, long free, long total) {
        this.name = name;
        this.free = (int) (free / DriveInfo.GB);
        this.total = (int) (total / DriveInfo.GB);
    }

    public String getName() {
        return name;
    }

    public int getFreeSpace() {
        return free;
    }

    public int getTotalSpace() {
        return total;
    }
}
