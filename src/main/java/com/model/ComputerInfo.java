package com.model;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;
import java.util.Objects;

/**
 * Data-transfer object that holds information about a remote computer.
 *
 * <p>Instances of this class are serialized over the network via Java RMI and therefore
 * implement {@link Serializable}. Both the server and client JVMs must have this class
 * on their classpath.</p>
 *
 * <p>Contains the operating system name and an immutable snapshot of detected disk drives.</p>
 */
public record ComputerInfo(String osName, List<DriveInfo> drives) implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    public ComputerInfo {
        Objects.requireNonNull(osName, "osName");
        drives = List.copyOf(Objects.requireNonNull(drives, "drives"));
    }
}
