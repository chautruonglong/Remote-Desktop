import com.gui.MainFrame;
import java.awt.EventQueue;
import java.io.IOException;
import javax.swing.JOptionPane;

public class Application {
    public static void main(String[] args) {
        // TODO: point to start new program
        EventQueue.invokeLater(() -> {
            try {
                new MainFrame();
            }
            catch(IOException e) {
                JOptionPane.showMessageDialog(null, "Application error!\n" + e.getMessage());
            }
        });
    }
}
