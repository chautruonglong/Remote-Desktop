package com.gui.remote;

import java.io.Serializable;

public class DriveInfo implements Serializable {
    private String name;
    private long free;
    private long total;

    public DriveInfo(String name, long free, long total) {
        this.name = name;
        this.free = free;
        this.total = total;
    }

    public String getName() {
        return this.name;
    }

    public long getFreeSpace() {
        return this.free;
    }

    public long getTotalSpace() {
        return this.total;
    }
}
