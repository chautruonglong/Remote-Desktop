package com.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Data-transfer object that holds information about a remote computer.
 *
 * <p>Instances of this class are serialized over the network via Java RMI and therefore
 * implement {@link Serializable}. Both the server and client JVMs must have this class
 * on their classpath.</p>
 *
 * <p>Contains the operating system name and a list of detected disk drives.</p>
 */
public class ComputerInfo implements Serializable {

    private static final long serialVersionUID = 1L;

    private final String osName;
    private final List<DriveInfo> drives;

    /**
     * Creates a new {@code ComputerInfo} for the given operating system name.
     *
     * @param osName the display name of the operating system (e.g., {@code "Windows 10"})
     */
    public ComputerInfo(String osName) {
        this.osName = osName;
        this.drives = new ArrayList<>();
    }

    /**
     * Returns the name of the operating system running on the remote computer.
     *
     * @return the OS name string
     */
    public String getOsName() {
        return osName;
    }

    /**
     * Returns the mutable list of disk drives detected on the remote computer.
     *
     * <p>Callers may add {@link DriveInfo} entries to this list to populate drive data.</p>
     *
     * @return the list of drives
     */
    public List<DriveInfo> getDrives() {
        return drives;
    }
}
