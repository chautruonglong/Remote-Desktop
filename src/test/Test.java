package test;

import com.sun.management.OperatingSystemMXBean;
import java.io.IOException;
import java.lang.management.ManagementFactory;

public class Test {
    public static void main(String[] args) throws IOException {
        OperatingSystemMXBean os = (OperatingSystemMXBean)ManagementFactory.getOperatingSystemMXBean();
        System.out.println(os.getTotalMemorySize() / 1000 / 1000 / 1000);
        System.out.println(os.getTotalSwapSpaceSize() / 1000 / 1000 / 1000);
        System.out.println(os.getCommittedVirtualMemorySize() / 1000 / 1000 / 1000);
        System.out.println(os.getArch());
        System.out.println(os.getAvailableProcessors());
    }
}
