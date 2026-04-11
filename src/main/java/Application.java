import com.gui.MainFrame;
import java.awt.EventQueue;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.imageio.spi.IIORegistry;
import javax.imageio.spi.ImageReaderSpi;
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
        deregisterThirdPartyImageReaders();

        EventQueue.invokeLater(() -> {
            try {
                new MainFrame();
            } catch (IOException e) {
                JOptionPane.showMessageDialog(
                    null,
                    "Application failed to start:\n" + e.getMessage(),
                    "Startup Error LONG",
                    JOptionPane.ERROR_MESSAGE
                );
            }
        });
    }

    /**
     * Removes any third-party {@link ImageReaderSpi} providers from the global
     * {@link IIORegistry}, leaving only the built-in Java readers.
     *
     * <p>Some third-party JPEG libraries (e.g., JDeli) register themselves as ImageIO
     * readers but internally cast the {@code ImageInputStream} to
     * {@code FileImageInputStream}, which fails when reading from in-memory byte arrays.
     * Since this application only ever reads screenshot byte arrays on the client side,
     * the built-in Java readers are sufficient.</p>
     *
     * <p>Third-party {@link javax.imageio.spi.ImageWriterSpi} providers are intentionally
     * kept so that JDeli can still encode screenshots on the server side.</p>
     */
    private static void deregisterThirdPartyImageReaders() {
        IIORegistry registry = IIORegistry.getDefaultInstance();
        Iterator<ImageReaderSpi> it = registry.getServiceProviders(ImageReaderSpi.class, false);

        List<ImageReaderSpi> toRemove = new ArrayList<>();
        while (it.hasNext()) {
            ImageReaderSpi spi = it.next();
            String className = spi.getClass().getName();
            if (!className.startsWith("com.sun.") && !className.startsWith("sun.")) {
                toRemove.add(spi);
            }
        }
        toRemove.forEach(registry::deregisterServiceProvider);
    }
}
