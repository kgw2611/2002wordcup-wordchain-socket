package client.resource;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;

public class Styles {

    // 둥근 Border
    public static Border roundedBorder(int radius) {
        return BorderFactory.createLineBorder(Colors.BORDER, 2, true);
    }

    public static void styleButton(JButton btn) {
        btn.setBackground(Colors.ACCENT);
        btn.setForeground(Color.WHITE);
        btn.setFont(Fonts.BUTTON);
        btn.setFocusPainted(false);
        btn.setBorder(roundedBorder(20));
        btn.setPreferredSize(new Dimension(200, 45));
    }

    public static void styleTextField(JTextField field) {
        field.setFont(Fonts.NORMAL);
        field.setBorder(roundedBorder(15));
        field.setOpaque(false);
        field.setPreferredSize(new Dimension(220, 40));
    }
}
