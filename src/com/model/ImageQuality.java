package com.model;

/**
 * Represents the image quality and format used for screen capture during a remote desktop session.
 *
 * <p>Choosing {@link #LOW} sends JPEG frames which are smaller and faster to transmit but lossy.
 * Choosing {@link #HIGH} sends PNG frames which are lossless and larger.</p>
 */
public enum ImageQuality {

    /** JPEG format — lower quality, smaller payload, better real-time performance. */
    LOW("jpg"),

    /** PNG format — lossless quality, larger payload, higher visual fidelity. */
    HIGH("png");

    private final String formatName;

    ImageQuality(String formatName) {
        this.formatName = formatName;
    }

    /**
     * Returns the {@link javax.imageio.ImageIO}-compatible format name (e.g., {@code "jpg"} or {@code "png"}).
     *
     * @return the format name string
     */
    public String getFormatName() {
        return formatName;
    }
}
