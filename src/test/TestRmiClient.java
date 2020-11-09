package test;

import com.bus.IRemoteDesktop;
import com.bus.RmiClient;
import java.awt.AWTException;
import java.awt.EventQueue;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.*;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;

public class TestRmiClient extends JFrame implements Runnable {
    private IRemoteDesktop p;
    private JLabel label;

    public TestRmiClient() throws IOException, NotBoundException, InterruptedException, AWTException {
        this.setSize(500, 500);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setLocationRelativeTo(null);
        this.setVisible(true);

        //p = (IRemoteDesktop) Naming.lookup("rmi://192.168.0.110:9999/remote");

        RmiClient rmiClient = new RmiClient();
        rmiClient.startConnectingToRmiServer("192.168.0.110", 9999);

        p = rmiClient.getRemoteObject();
        label = new JLabel();
        label.setBounds(0, 0, 1388, 768);
        this.add(label);

        new Thread(this).start();



    }

    @Override
    public void run() {
        while(true) {
            try {
                byte[] dgram = p.takeScreenshotServer("jpg");
                ByteArrayInputStream bis = new ByteArrayInputStream(dgram);
                BufferedImage bufferedImage = ImageIO.read(bis);
                label.setIcon(new ImageIcon(bufferedImage));
                Thread.sleep(100);
            }
            catch(Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) throws UnknownHostException, SocketException {
        EventQueue.invokeLater(() -> {
            try {
                new TestRmiClient();
            }
            catch(RemoteException | InterruptedException e) {
                e.printStackTrace();
            }
            catch(NotBoundException e) {
                e.printStackTrace();
            }
            catch(IOException | AWTException e) {
                e.printStackTrace();
            }
        });

    }
}
