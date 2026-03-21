import com.gui.MainFrame;
import java.awt.EventQueue;
import java.io.IOException;
import javax.swing.JOptionPane;

/**
 * Application entry point for the Remote Desktop Software.
 *
 * <p>Bootstraps the Swing GUI on the Event Dispatch Thread. If the main window fails to
 * initialize (e.g., due to a missing icon resource), an error dialog is shown.</p>
 */
public class Application {

    /** Private constructor — this is a utility class with only a {@code main} method. */
    private Application() {}

    /**
     * Launches the application.
     *
     * @param args command-line arguments (not used)
     */
    public static void main(String[] args) {
        EventQueue.invokeLater(() -> {
            try {
                new MainFrame();
            } catch (IOException e) {
                JOptionPane.showMessageDialog(
                    null,
                    "Application failed to start:\n" + e.getMessage(),
                    "Startup Error",
                    JOptionPane.ERROR_MESSAGE
                );
            }
        });
    }
}
