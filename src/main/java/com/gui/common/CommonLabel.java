package com.gui.common;

import com.gui.AppStyles;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.JLabel;

/**
 * A styled navigation label used in the application's top tab bar.
 *
 * <p>Provides two font size states — "big" (active/selected tab) and "small" (inactive tab) —
 * and automatically bolds its font on mouse hover to give interactive feedback. The hover
 * behavior is handled internally, so subclasses and callers need not wire their own listeners
 * for this effect.</p>
 *
 * <p>The foreground color ({@link #FOREGROUND_COLOR}) matches the tab bar palette. Labels
 * start in the small (inactive) state.</p>
 */
public class CommonLabel extends JLabel {

    /** Font size for the currently active (selected) tab label. */
    public static final int BIG_FONT_SIZE = 35;

    /** Font size for inactive tab labels. */
    public static final int SMALL_FONT_SIZE = 18;

    /**
     * Creates a new {@code CommonLabel} in the small (inactive) state.
     * Hover listeners for bold/plain toggling are wired automatically.
     */
    public CommonLabel() {
        this.setSmallFont();
        this.setForeground(Color.decode(AppStyles.COLOR_TAB_LABEL));

        this.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                onMouseEntered(e);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                onMouseExited(e);
            }
        });
    }

    /**
     * Switches this label to the big (active) font size in plain weight.
     * Call this when the associated tab becomes selected.
     */
    public void setBigFont() {
        this.setFont(new Font(AppStyles.FONT_UI, Font.PLAIN, BIG_FONT_SIZE));
    }

    /**
     * Switches this label to the small (inactive) font size in plain weight.
     * Call this when the associated tab loses focus.
     */
    public void setSmallFont() {
        this.setFont(new Font(AppStyles.FONT_UI, Font.PLAIN, SMALL_FONT_SIZE));
    }

    /**
     * Resets the font to plain weight while keeping the current size.
     * Useful when re-enabling a label that may have been left in the bold state.
     */
    public void resetFont() {
        int currentSize = this.getFont().getSize();
        this.setFont(new Font(AppStyles.FONT_UI, Font.PLAIN, currentSize));
    }

    // -------------------------------------------------------------------------
    // Internal hover handlers — bold on entrance, plain on exit
    // -------------------------------------------------------------------------

    private void onMouseEntered(MouseEvent e) {
        if (this.isEnabled()) {
            this.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            int currentSize = this.getFont().getSize();
            this.setFont(new Font(AppStyles.FONT_UI, Font.BOLD, currentSize));
        }
    }

    private void onMouseExited(MouseEvent e) {
        if (this.isEnabled()) {
            this.setCursor(Cursor.getDefaultCursor());
            int currentSize = this.getFont().getSize();
            this.setFont(new Font(AppStyles.FONT_UI, Font.PLAIN, currentSize));
        }
    }
}
