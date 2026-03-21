package com.model;

/**
 * Represents the image quality preset used for screen capture during a remote desktop session.
 *
 * <p>Both presets use JPEG for real-time streaming because lower latency matters more than
 * lossless fidelity in a remote desktop session. The presets differ in compression level
 * and target frame rate.</p>
 */
public enum ImageQuality {

    /** Lower bitrate stream — smaller payloads and a higher target frame rate. */
    LOW("jpg", 0.45f, 18),

    /** Higher fidelity stream — larger payloads with less compression. */
    HIGH("jpg", 0.8f, 12);

    private final String formatName;
    private final float compressionQuality;
    private final int targetFps;

    ImageQuality(String formatName, float compressionQuality, int targetFps) {
        this.formatName = formatName;
        this.compressionQuality = compressionQuality;
        this.targetFps = targetFps;
    }

    /**
     * Returns the {@link javax.imageio.ImageIO}-compatible format name (e.g., {@code "jpg"} or {@code "png"}).
     *
     * @return the format name string
     */
    public String getFormatName() {
        return formatName;
    }

    /**
     * Returns the JPEG compression quality in the range {@code 0.0f..1.0f}.
     *
     * @return the compression quality factor
     */
    public float getCompressionQuality() {
        return compressionQuality;
    }

    /**
     * Returns the preferred streaming frame rate for this preset.
     *
     * @return the target frames per second
     */
    public int getTargetFps() {
        return targetFps;
    }
}
