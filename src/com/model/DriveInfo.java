package com.model;

import java.io.Serializable;

/**
 * Data-transfer object that holds disk drive information for a single drive on a remote computer.
 *
 * <p>Instances are serialized over the network via Java RMI and therefore implement
 * {@link Serializable}. Space values are stored in gigabytes (GB).</p>
 */
public class DriveInfo implements Serializable {

    private static final long serialVersionUID = 1L;

    private final String name;
    private final long freeSpaceGb;
    private final long totalSpaceGb;

    /**
     * Creates a new {@code DriveInfo}.
     *
     * @param name         the human-readable display name of the drive (e.g., {@code "Local Disk (C:)"})
     * @param freeSpaceGb  available free space in gigabytes
     * @param totalSpaceGb total capacity of the drive in gigabytes
     */
    public DriveInfo(String name, long freeSpaceGb, long totalSpaceGb) {
        this.name = name;
        this.freeSpaceGb = freeSpaceGb;
        this.totalSpaceGb = totalSpaceGb;
    }

    /**
     * Returns the human-readable display name of the drive.
     *
     * @return the drive name
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the available free space on this drive in gigabytes.
     *
     * @return free space in GB
     */
    public long getFreeSpaceGb() {
        return freeSpaceGb;
    }

    /**
     * Returns the total storage capacity of this drive in gigabytes.
     *
     * @return total space in GB
     */
    public long getTotalSpaceGb() {
        return totalSpaceGb;
    }
}
