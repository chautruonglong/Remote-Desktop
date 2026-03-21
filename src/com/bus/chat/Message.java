package com.bus.chat;

import java.io.Serializable;

/**
 * Abstract base class for all messages exchanged between peers over a chat session.
 *
 * <p>Each concrete subclass represents a different kind of payload (e.g., text or file).
 * Messages are serialized over the network via Java object streams and therefore implement
 * {@link Serializable}.</p>
 *
 * <p>Use {@code instanceof} checks (rather than the type constants) to determine the
 * concrete subtype at the receiving end:</p>
 * <pre>
 *   if (message instanceof StringMessage stringMsg) { ... }
 *   else if (message instanceof FileMessage fileMsg) { ... }
 * </pre>
 */
public abstract class Message implements Serializable {

    private static final long serialVersionUID = 1L;

    /** Type constant indicating a plain-text message. Prefer {@code instanceof StringMessage}. */
    public static final int STRING_MESSAGE = 0x1;

    /** Type constant indicating a binary file transfer message. Prefer {@code instanceof FileMessage}. */
    public static final int FILE_MESSAGE = 0x2;

    private final int type;
    private String sender;

    /**
     * Constructs a {@code Message} with an undefined type (type {@code 0x0}).
     * Subclasses should always supply a specific type.
     */
    protected Message() {
        this.type = 0x0;
    }

    /**
     * Constructs a {@code Message} with the given type constant.
     *
     * @param type one of {@link #STRING_MESSAGE} or {@link #FILE_MESSAGE}
     */
    protected Message(int type) {
        this.type = type;
    }

    /**
     * Returns the type constant that identifies the concrete subclass.
     *
     * @return the message type constant
     */
    public int getType() {
        return type;
    }

    /**
     * Sets the display name of the sender (typically the sender's hostname).
     *
     * @param sender the sender's hostname or display name
     */
    public void setSender(String sender) {
        this.sender = sender;
    }

    /**
     * Returns the display name of the sender.
     *
     * @return the sender's hostname or display name
     */
    public String getSender() {
        return sender;
    }
}
