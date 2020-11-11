package test;

import java.io.File;
import java.io.IOException;
import javax.swing.filechooser.FileSystemView;

public class Test {
    public static void main(String[] args) throws IOException {
        for(File file : File.listRoots()) {
            System.out.println(FileSystemView.getFileSystemView().getSystemDisplayName(File.listRoots()[0]));
        }
    }
}
