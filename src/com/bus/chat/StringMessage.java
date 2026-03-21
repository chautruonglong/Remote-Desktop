package com.bus.chat;

import java.io.Serial;

/**
 * A {@link Message} that carries a plain-text string payload.
 *
 * <p>Used to send chat text from one peer to another during a remote desktop session.</p>
 */
public class StringMessage extends Message {
    @Serial
    private static final long serialVersionUID = 1L;

    private final String content;

    /**
     * Creates a new text message.
     *
     * @param sender  the hostname or display name of the sender
     * @param content the text content to deliver
     */
    public StringMessage(String sender, String content) {
        super(Message.STRING_MESSAGE);
        this.setSender(sender);
        this.content = content;
    }

    /**
     * Returns the plain-text content of this message.
     *
     * @return the message text
     */
    public String getContent() {
        return content;
    }
}
