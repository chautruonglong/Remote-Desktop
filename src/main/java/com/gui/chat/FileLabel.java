package com.gui.chat;

import com.bus.chat.FileMessage;
import com.gui.AppStyles;
import com.gui.common.UiSupport;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.filechooser.FileSystemView;

/**
 * A clickable {@link JLabel} representing a received file in the chat history.
 *
 * <p>Displays the sender name and file name with underline HTML formatting. When the user
 * clicks the label, a save-file dialog is shown. The file bytes from the {@link FileMessage}
 * are written to the chosen location. Resources are always closed via try-with-resources
 * to prevent stream leaks.</p>
 *
 * <p>The cursor changes to a hand on hover to signal interactivity, and the font bolds
 * slightly for visual feedback.</p>
 */
public class FileLabel extends JLabel {

    private static final int NORMAL_SIZE = 14;
    private static final int HOVER_SIZE  = 15;

    private final FileMessage fileMessage;

    /**
     * Creates a new {@code FileLabel} for the given received file message.
     *
     * @param fileMessage the received file message to display and allow saving
     */
    public FileLabel(FileMessage fileMessage) {
        this.fileMessage = fileMessage;

        this.setText(
            this.fileMessage.sender() + " sent a file: <U>" + this.fileMessage.fileName() + "</U>"
        );
        this.setFont(new Font(AppStyles.FONT_MONO, Font.ITALIC, NORMAL_SIZE));

        this.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                onMouseEntered(e);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                onMouseExited(e);
            }

            @Override
            public void mousePressed(MouseEvent e) {
                onMousePressed(e);
            }
        });
    }

    private void onMouseEntered(MouseEvent e) {
        if (this.isEnabled()) {
            this.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            this.setFont(new Font(AppStyles.FONT_MONO, Font.ITALIC | Font.BOLD, HOVER_SIZE));
        }
    }

    private void onMouseExited(MouseEvent e) {
        if (this.isEnabled()) {
            this.setCursor(Cursor.getDefaultCursor());
            this.setFont(new Font(AppStyles.FONT_MONO, Font.ITALIC, NORMAL_SIZE));
        }
    }

    /**
     * Opens a save-file dialog and writes the file bytes to the user-selected location.
     * Uses try-with-resources to guarantee the output stream is closed even if an error occurs.
     */
    private void onMousePressed(MouseEvent e) {
        if (e.getButton() != MouseEvent.BUTTON1) return;

        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setCurrentDirectory(FileSystemView.getFileSystemView().getHomeDirectory());
        fileChooser.setSelectedFile(new File(this.fileMessage.fileName()));
        fileChooser.showSaveDialog(this);

        File destination = fileChooser.getSelectedFile();
        if (destination == null) return;

        try (FileOutputStream fos = new FileOutputStream(destination)) {
            fos.write(this.fileMessage.fileData());
        } catch (IOException ex) {
            UiSupport.showError(null, "Could not save file", ex);
        }
    }
}
