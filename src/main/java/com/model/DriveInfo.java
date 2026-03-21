package com.model;

import java.io.Serial;
import java.io.Serializable;

/**
 * Data-transfer object that holds disk drive information for a single drive on a remote computer.
 *
 * <p>Instances are serialized over the network via Java RMI and therefore implement
 * {@link Serializable}. Space values are stored in gigabytes (GB).</p>
 */
public record DriveInfo(String name, long freeSpaceGb, long totalSpaceGb) implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
}
