package com.bus.rmi;

import com.model.ComputerInfo;
import com.model.DriveInfo;
import com.model.ImageQuality;
import com.sun.management.OperatingSystemMXBean;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Iterator;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.MemoryCacheImageOutputStream;
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
     * <p>Captures the entire screen, scales it down to the requested client viewport when
     * needed, and encodes it using the format specified by {@code quality}.</p>
     */
    @Override
    public byte[] captureScreen(ImageQuality quality, int targetWidth, int targetHeight) throws Exception {
        Rectangle screenBounds = new Rectangle(Toolkit.getDefaultToolkit().getScreenSize());
        BufferedImage screenshot = this.robot.createScreenCapture(screenBounds);
        BufferedImage frame = scaleForStreaming(screenshot, targetWidth, targetHeight);
        return encodeFrame(frame, quality);
    }

    /** {@inheritDoc} */
    @Override
    public int[] getScreenDimensions() throws RemoteException {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        return new int[]{ screenSize.width, screenSize.height };
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

    private static BufferedImage scaleForStreaming(BufferedImage source, int targetWidth, int targetHeight) {
        if (targetWidth <= 0 || targetHeight <= 0) {
            return source;
        }

        double widthScale = (double) targetWidth / source.getWidth();
        double heightScale = (double) targetHeight / source.getHeight();
        double scale = Math.min(Math.min(widthScale, heightScale), 1d);
        if (scale >= 1d) {
            return source;
        }

        int scaledWidth = Math.max(1, (int) Math.round(source.getWidth() * scale));
        int scaledHeight = Math.max(1, (int) Math.round(source.getHeight() * scale));
        BufferedImage scaled = new BufferedImage(scaledWidth, scaledHeight, BufferedImage.TYPE_3BYTE_BGR);
        Graphics2D graphics = scaled.createGraphics();
        try {
            graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            graphics.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_SPEED);
            graphics.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_SPEED);
            graphics.drawImage(source, 0, 0, scaledWidth, scaledHeight, null);
        } finally {
            graphics.dispose();
        }
        return scaled;
    }

    private static byte[] encodeFrame(BufferedImage frame, ImageQuality quality) throws IOException {
        Iterator<ImageWriter> writers = ImageIO.getImageWritersByFormatName(quality.getFormatName());
        if (!writers.hasNext()) {
            throw new IOException("No ImageIO writer found for format " + quality.getFormatName());
        }

        ImageWriter writer = writers.next();
        ImageWriteParam writeParam = writer.getDefaultWriteParam();
        if (writeParam.canWriteCompressed()) {
            writeParam.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
            writeParam.setCompressionQuality(quality.getCompressionQuality());
        }

        int estimatedSize = Math.max(32_768, frame.getWidth() * frame.getHeight() / 3);
        try (ByteArrayOutputStream buffer = new ByteArrayOutputStream(estimatedSize);
             MemoryCacheImageOutputStream imageOutput = new MemoryCacheImageOutputStream(buffer)) {
            writer.setOutput(imageOutput);
            writer.write(null, new javax.imageio.IIOImage(frame, null, null), writeParam);
            imageOutput.flush();
            return buffer.toByteArray();
        } finally {
            writer.dispose();
        }
    }
}
