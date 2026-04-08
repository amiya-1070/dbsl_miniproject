package com.dbsl.shop.ui;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

public class ShopApplication {
    public static void main(String[] args) {
        try {
            com.formdev.flatlaf.FlatLightLaf.setup();
        } catch (Exception e) {
            e.printStackTrace();
        }
        Theme.applyLookAndFeelDefaults();
        SwingUtilities.invokeLater(() -> new LoginFrame().setVisible(true));
    }
}
