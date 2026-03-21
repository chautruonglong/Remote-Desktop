package com.gui;

/**
 * Centralized repository for all UI style constants used across the application.
 *
 * <p>Keeping colors and font names here ensures a consistent visual identity and
 * makes global theme changes a single-file edit.</p>
 */
public final class AppStyles {

    // -------------------------------------------------------------------------
    // Colors
    // -------------------------------------------------------------------------

    /** Main panel background — deep green. */
    public static final String COLOR_BACKGROUND       = "0x00A571";

    /** Main panel foreground text — dark green. */
    public static final String COLOR_FOREGROUND       = "0x003927";

    /** Tab bar background — dark navy. */
    public static final String COLOR_TASKBAR          = "0x000942";

    /** Tab label foreground — lavender blue. */
    public static final String COLOR_TAB_LABEL        = "0x99A7FF";

    /** Server status "Stopped" label — red. */
    public static final String COLOR_STATUS_STOPPED   = "0xF50016";

    /** Server status "Listening" label — blue. */
    public static final String COLOR_STATUS_LISTENING = "0x0042A7";

    /** Chat message history background — white. */
    public static final String COLOR_CHAT_CONTENT     = "0xFFFFFF";

    /** Hardware graph grid lines — light blue. */
    public static final String COLOR_GRAPH_GRID       = "0xD9EAF4";

    /** Hardware graph border — medium blue. */
    public static final String COLOR_GRAPH_BORDER     = "0x117DBB";

    /** Drive usage progress bar — sky blue. */
    public static final String COLOR_DRIVE_PROGRESS   = "0x26A0DA";

    /** Remote desktop menu bar background — medium gray. */
    public static final String COLOR_MENU_BAR         = "0x9A9A9A";

    /** Disabled control foreground — light gray. */
    public static final String COLOR_DISABLED         = "0xD3D3D3";

    // -------------------------------------------------------------------------
    // Font families
    // -------------------------------------------------------------------------

    /** Primary UI font used for labels, buttons, and form controls. */
    public static final String FONT_UI   = "segoe ui";

    /** Monospaced font used for chat message text. */
    public static final String FONT_MONO = "consolas";

    private AppStyles() {}
}
