package com.bus.chat;

import java.io.Serial;
import java.util.Objects;

/**
 * A {@link Message} that carries a plain-text string payload.
 *
 * <p>Used to send chat text from one peer to another during a remote desktop session.</p>
 */
public record StringMessage(String sender, String content) implements Message {
    @Serial
    private static final long serialVersionUID = 1L;

    public StringMessage {
        Objects.requireNonNull(sender, "sender");
        Objects.requireNonNull(content, "content");
    }
}
