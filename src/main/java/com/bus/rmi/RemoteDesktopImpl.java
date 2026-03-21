package com.bus.rmi;

import com.model.ComputerInfo;
import com.model.DriveInfo;
import com.model.ImageQuality;
import com.sun.management.OperatingSystemMXBean;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.lang.management.ManagementFactory;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import javax.imageio.ImageIO;
import javax.swing.filechooser.FileSystemView;

/**
 * Server-side implementation of {@link IRemoteDesktop} that exposes the local machine's
 * screen, mouse, keyboard, and hardware metrics to remote clients via Java RMI.
 *
 * <p>{@link Robot} is used for screen capture and input injection. The
 * {@link OperatingSystemMXBean} provides CPU and memory statistics.</p>
 *
 * <p>Image I/O disk caching is disabled on the server side ({@code ImageIO.setUseCache(false)})
 * to avoid unnecessary disk writes during high-frequency screen captures.</p>
 */
public class RemoteDesktopImpl extends UnicastRemoteObject implements IRemoteDesktop {

    /**
     * Number of bytes in one gigabyte, used to convert byte counts to GB.
     * Named {@code BYTES_PER_GB} to clarify the unit.
     */
    public static final long BYTES_PER_GB = 1024L * 1024 * 1024;

    private final Robot robot;
    private final OperatingSystemMXBean osBean;

    /**
     * Constructs a new {@code RemoteDesktopImpl}.
     *
     * <p>Disables ImageIO disk caching so that screen captures are encoded entirely
     * in memory, avoiding latency from disk access.</p>
     *
     * @throws RemoteException if RMI registration fails
     * @throws AWTException    if a {@link Robot} cannot be created (headless environment)
     */
    public RemoteDesktopImpl() throws RemoteException, AWTException {
        super();
        ImageIO.setUseCache(false);
        this.robot = new Robot();
        this.osBean = (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
    }

    // -------------------------------------------------------------------------
    // Screen capture
    // -------------------------------------------------------------------------

    /**
     * {@inheritDoc}
     *
     * <p>Captures the entire screen bounds and encodes the result using the format
     * specified by {@code quality} (JPEG for low, PNG for high).</p>
     */
    @Override
    public byte[] captureScreen(ImageQuality quality) throws Exception {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        Rectangle screenBounds = new Rectangle(screenSize);
        BufferedImage screenshot = this.robot.createScreenCapture(screenBounds);
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        ImageIO.write(screenshot, quality.getFormatName(), buffer);
        return buffer.toByteArray();
    }

    // -------------------------------------------------------------------------
    // Mouse control
    // -------------------------------------------------------------------------

    /** {@inheritDoc} */
    @Override
    public void moveMouse(int x, int y) throws RemoteException {
        this.robot.mouseMove(x, y);
    }

    /** {@inheritDoc} */
    @Override
    public void pressMouse(int buttonMask) throws RemoteException {
        this.robot.mousePress(buttonMask);
    }

    /** {@inheritDoc} */
    @Override
    public void releaseMouse(int buttonMask) throws RemoteException {
        this.robot.mouseRelease(buttonMask);
    }

    /** {@inheritDoc} */
    @Override
    public void scrollMouseWheel(int wheelAmount) throws RemoteException {
        this.robot.mouseWheel(wheelAmount);
    }

    // -------------------------------------------------------------------------
    // Keyboard control
    // -------------------------------------------------------------------------

    /** {@inheritDoc} */
    @Override
    public void pressKey(int keyCode) throws RemoteException {
        this.robot.keyPress(keyCode);
    }

    /** {@inheritDoc} */
    @Override
    public void releaseKey(int keyCode) throws RemoteException {
        this.robot.keyRelease(keyCode);
    }

    // -------------------------------------------------------------------------
    // Hardware monitoring
    // -------------------------------------------------------------------------

    /** {@inheritDoc} */
    @Override
    public double getCpuLoad() throws RemoteException {
        return this.osBean.getCpuLoad();
    }

    /**
     * {@inheritDoc}
     *
     * <p>Calculated as {@code (total - free) / total}.</p>
     */
    @Override
    public double getRamUsage() throws RemoteException {
        long total = this.osBean.getTotalMemorySize();
        long free = this.osBean.getFreeMemorySize();
        return (double) (total - free) / total;
    }

    /**
     * {@inheritDoc}
     *
     * <p>Values are rounded up to the nearest GB using integer ceiling arithmetic
     * ({@code 1 + bytes / BYTES_PER_GB}).</p>
     */
    @Override
    public long[] getRamCapacities() throws RemoteException {
        long totalRamGb  = 1 + this.osBean.getTotalMemorySize()    / BYTES_PER_GB;
        long totalSwapGb = 1 + this.osBean.getTotalSwapSpaceSize()  / BYTES_PER_GB;
        return new long[]{ totalRamGb, totalSwapGb };
    }

    /** {@inheritDoc} */
    @Override
    public int getProcessorCount() throws RemoteException {
        return this.osBean.getAvailableProcessors();
    }

    /**
     * {@inheritDoc}
     *
     * <p>Iterates over all file system roots reported by the JVM and collects
     * drive name, free space, and total space (all in GB).</p>
     */
    @Override
    public ComputerInfo getComputerInfo() throws RemoteException {
        var drives = new ArrayList<DriveInfo>();
        for (File root : File.listRoots()) {
            String displayName = FileSystemView.getFileSystemView().getSystemDisplayName(root);
            long freeGb  = root.getFreeSpace()  / BYTES_PER_GB;
            long totalGb = root.getTotalSpace()  / BYTES_PER_GB;
            drives.add(new DriveInfo(displayName, freeGb, totalGb));
        }
        return new ComputerInfo(this.osBean.getName(), drives);
    }
}
