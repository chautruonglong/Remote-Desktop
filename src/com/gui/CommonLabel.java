package com.gui;

import java.awt.Color;
import java.awt.Font;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.JLabel;

public class CommonLabel extends JLabel {
    public final static String FOREGROUND = "0x99A7FF";
    public final static int BIG_FONT_SIZE = 35;
    public final static int SMALL_FONT_SIZE = 18;

    public CommonLabel() {
        // TODO: small font is default
        this.setSmallFont();
        this.setForeground(Color.decode(CommonLabel.FOREGROUND));

        // TODO: add events when mouse entered or exited
        this.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                tabLabelMouseEntered(e);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                tabLabelMouseExited(e);
            }
        });
    }

    public void setSmallFont() {
        this.setFont(new Font("segoe ui", Font.PLAIN, CommonLabel.SMALL_FONT_SIZE));
    }

    public void setBigFont() {
        this.setFont(new Font("segoe ui", Font.PLAIN, CommonLabel.BIG_FONT_SIZE));
    }

    public void resetFont() {
        int font_size = this.getFont().getSize();
        this.setFont(new Font("segoe ui", Font.PLAIN, font_size));
    }

    private void tabLabelMouseEntered(MouseEvent e) {
        if(this.isEnabled()) {
            int font_size = this.getFont().getSize();
            this.setFont(new Font("segoe ui", Font.BOLD, font_size));
        }
    }

    private void tabLabelMouseExited(MouseEvent e) {
        if(this.isEnabled()) {
            int font_size = this.getFont().getSize();
            this.setFont(new Font("segoe ui", Font.PLAIN, font_size));
        }
    }
}
