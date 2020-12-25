package com.bus.chat;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class ChatBus {
    private Socket socket;

    public ChatBus(Socket socket) {
        this.socket = socket;
    }

    public void sendMessage(Message obj_message) throws IOException {
        ObjectOutputStream dos = new ObjectOutputStream(this.socket.getOutputStream());
        dos.writeObject(obj_message);
    }

    public Message recvMessage() throws IOException, ClassNotFoundException {
        Message obj_message = null;
        ObjectInputStream dis = new ObjectInputStream(this.socket.getInputStream());
        obj_message = (Message) dis.readObject();
        return obj_message;
    }

    public Socket getSocket() {
        return this.socket;
    }
}
