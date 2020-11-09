package test;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class TestTcpClient {
    public static void main(String[] args) throws IOException {
        Socket client = new Socket("192.168.0.103", 9999);
        DataOutputStream dos = new DataOutputStream(client.getOutputStream());
        DataInputStream dis = new DataInputStream(client.getInputStream());

        boolean isConnected = true;
        while(isConnected) {
            try {
                dos.writeUTF("cc");
            }
            catch(Exception e) {
                System.out.println("Client closed");
                isConnected = false;
            }
        }
    }

}
