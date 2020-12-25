package com.bus.chat;

public class StringMessage extends Message {
    private String content;

    public StringMessage(String sender, String content) {
        this.setCurrentType(Message.STRING_MESSAGE);
        this.setSender(sender);
        this.content = content;
    }

    public String getContent() {
        return this.content;
    }
}
