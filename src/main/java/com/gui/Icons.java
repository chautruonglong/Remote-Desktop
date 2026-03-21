package com.gui;

import javax.swing.ImageIcon;

/**
 * Central registry of all application icon resources.
 *
 * <p>All {@link ImageIcon}s are loaded once from the classpath when this class is first
 * referenced, avoiding repeated resource lookups scattered throughout the codebase.
 * Use the public constants directly — no instantiation required.</p>
 */
public final class Icons {

    /** Window/frame icon (used in title bars). */
    public static final ImageIcon WINDOW = load("window_icon.png");

    /** Dialog icon (used in the hardware info dialog title bar). */
    public static final ImageIcon DIALOG = load("dialog_icon.png");

    /** Hard disk drive icon (used in the drives info panel). */
    public static final ImageIcon HDD = load("hdd_icon.png");

    /** "Start listening" server icon. */
    public static final ImageIcon LISTEN = load("listen_icon.png");

    /** "Stop listening" server icon. */
    public static final ImageIcon STOP = load("stop_icon.png");

    /** Online/connected status icon (used in connection menu items). */
    public static final ImageIcon ONLINE = load("online.png");

    /** "Connect now" client icon. */
    public static final ImageIcon CONNECT = load("connect_icon.png");

    /** Large loading spinner (used while connecting). */
    public static final ImageIcon LOADER = load("loader_icon.gif");

    /** Small loading spinner (used while sending a file). */
    public static final ImageIcon LOADER_SMALL = load("loader_icon2.gif");

    /** "Send message" icon. */
    public static final ImageIcon SEND = load("send_icon.png");

    /** "Send file" icon. */
    public static final ImageIcon FILE = load("file_icon.png");

    private Icons() {}

    private static ImageIcon load(String resourceName) {
        return new ImageIcon(Icons.class.getClassLoader().getResource(resourceName));
    }
}
