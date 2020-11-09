package com.bus;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class ChatBus {
    Socket socket;

    public ChatBus() {
        this.socket = null;
    }

    public void setSocket(Socket socket) {
        this.socket = socket;
    }

    public void sendMessage(Message obj_message) throws IOException {
        if(this.socket != null && !this.socket.isClosed()) {
            ObjectOutputStream dos = new ObjectOutputStream(this.socket.getOutputStream());
            dos.writeObject(obj_message);
        }
    }

    public Message recvMessage() throws IOException, ClassNotFoundException {
        Message obj_message = null;
        if(this.socket != null && !this.socket.isClosed()) {
            ObjectInputStream dis = new ObjectInputStream(this.socket.getInputStream());
            obj_message = (Message) dis.readObject();
        }
        return obj_message;
    }
}
