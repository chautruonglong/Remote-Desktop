package test;

import com.bus.IRemoteDesktop;
import com.bus.RmiServer;
import java.awt.AWTException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.rmi.AlreadyBoundException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;

public class TestRmiServer {
    public static void main(String[] args) throws Exception {
        RmiServer rmiServer = new RmiServer();
        System.out.println(rmiServer.isBinding());
        rmiServer.startBindingOnRmiServer("0.0.0.0", 9999);
        System.out.println(rmiServer.isBinding());

        IRemoteDesktop remote_obj = (IRemoteDesktop)Naming.lookup("rmi://0.0.0.0:9999/RemoteDesktop");
        System.out.println(remote_obj.takeScreenshotServer().length);

        rmiServer.stopBindingOnRmiServer();

    }
}