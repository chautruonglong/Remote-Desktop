package com.model;

/**
 * Represents the available navigation tabs in the main application window.
 *
 * <p>Each tab corresponds to one functional panel (client connect, server listen, or active chats).
 * The {@link #index} is used for ordering (lower index is to the left).</p>
 */
public enum Tab {

    /** The client tab lets the user connect to a remote server. */
    CLIENT(1),

    /** The server tab lets the user start listening for incoming connections. */
    SERVER(2),

    /** The chat tab shows all active connection sessions. */
    CHAT(3);

    private final int index;

    Tab(int index) {
        this.index = index;
    }

    /**
     * Returns the positional index of this tab (1-based, left to right).
     *
     * @return the tab index
     */
    public int getIndex() {
        return index;
    }

    /**
     * Returns the {@code Tab} matching the given index.
     *
     * @param index the 1-based positional index
     * @return the matching {@code Tab}, or {@code null} if none matches
     */
    public static Tab fromIndex(int index) {
        return switch (index) {
            case 1 -> CLIENT;
            case 2 -> SERVER;
            case 3 -> CHAT;
            default -> null;
        };
    }
}
