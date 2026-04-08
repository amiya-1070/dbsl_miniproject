package com.dbsl.shop.ui;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

public class ShopApplication {
    public static void main(String[] args) {
        Theme.applyLookAndFeelDefaults();
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
        }

        SwingUtilities.invokeLater(() -> new LoginFrame().setVisible(true));
    }
}
