package com.gui;

import java.io.File;
import java.io.Serializable;

public class ComputerInfo implements Serializable {
    private String os_name;
    private File[] disks;

    public ComputerInfo(String os_name, File[] disks) {
        this.os_name = os_name;
        this.disks = disks;
    }

    public String getOsName() {
        return this.os_name;
    }

    public File[] getDisks() {
        return this.disks;
    }
}
