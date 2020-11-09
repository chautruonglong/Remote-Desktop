package test;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.NetworkInterface;
import java.util.Enumeration;

public class TestClient {
    public static void main(String[] args) throws IOException {
        Enumeration e = NetworkInterface.getNetworkInterfaces();
        while(e.hasMoreElements())
        {
            NetworkInterface n = (NetworkInterface) e.nextElement();
            Enumeration ee = n.getInetAddresses();
            while (ee.hasMoreElements())
            {
                try {
                    Inet4Address i = (Inet4Address)ee.nextElement();

                    System.out.println(i.getHostName());
                }
                catch(Exception ex) {
                    //TODO: drop ipv6
                }
            }
        }
    }

}
