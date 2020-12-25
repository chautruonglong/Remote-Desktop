package com.bus.chat;

import java.io.Serializable;

public abstract class Message implements Serializable {
    public final static int STRING_MESSAGE = 0x1;
    public final static int FILE_MESSAGE = 0x2;

    private int current_type;
    private String sender;

    public Message() {
        // TODO: undefined message
        this.current_type = 0x0;
    }

    public void setCurrentType(int current_type) {
        this.current_type = current_type;
    }

    public int getCurrentType() {
        return this.current_type;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getSender() {
        return this.sender;
    }
}