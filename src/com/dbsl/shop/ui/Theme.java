package com.dbsl.shop.ui;
import java.awt.Color;
import java.awt.Font;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.border.Border;

public final class Theme {
    public static final Color BACKGROUND  = new Color(232, 245, 255);
    public static final Color PANEL       = new Color(213, 235, 250);
    public static final Color CARD        = new Color(244, 250, 255);
    public static final Color ACCENT      = new Color(44, 127, 184);
    public static final Color ACCENT_DARK = new Color(12, 30, 48);
    public static final Color TEXT        = new Color(10, 18, 24);
    public static final Color GRID        = new Color(169, 203, 227);

    public static final Font TITLE_FONT  = new Font("SansSerif", Font.BOLD, 28);
    public static final Font HEADER_FONT = new Font("SansSerif", Font.BOLD, 18);
    public static final Font BODY_FONT   = new Font("SansSerif", Font.PLAIN, 14);

    public static final Border CARD_BORDER = BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(GRID, 1),
            BorderFactory.createEmptyBorder(16, 16, 16, 16)
    );

    private Theme() {}

    public static void applyLookAndFeelDefaults() {
        // Force cross-platform L&F so Linux GTK doesn't override colors
        try {
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Panels and general backgrounds
        UIManager.put("Panel.background",              BACKGROUND);
        UIManager.put("Panel.foreground",              TEXT);
        UIManager.put("ContentPane.background",        BACKGROUND);

        // Labels
        UIManager.put("Label.foreground",              TEXT);
        UIManager.put("Label.background",              BACKGROUND);

        // Buttons
        UIManager.put("Button.background",             ACCENT);
        UIManager.put("Button.foreground",             Color.WHITE);
        UIManager.put("Button.font",                   BODY_FONT);
        UIManager.put("Button.select",                 ACCENT_DARK);
        UIManager.put("Button.focus",                  ACCENT);

        // Text fields
        UIManager.put("TextField.background",          Color.WHITE);
        UIManager.put("TextField.foreground",          TEXT);
        UIManager.put("TextField.caretForeground",     TEXT);
        UIManager.put("TextField.font",                BODY_FONT);
        UIManager.put("TextField.border",              BorderFactory.createLineBorder(GRID, 1));

        // Password fields
        UIManager.put("PasswordField.background",      Color.WHITE);
        UIManager.put("PasswordField.foreground",      TEXT);
        UIManager.put("PasswordField.caretForeground", TEXT);
        UIManager.put("PasswordField.font",            BODY_FONT);

        // Tables
        UIManager.put("Table.background",              Color.WHITE);
        UIManager.put("Table.foreground",              TEXT);
        UIManager.put("Table.font",                    BODY_FONT);
        UIManager.put("Table.gridColor",               GRID);
        UIManager.put("Table.selectionBackground",     ACCENT);
        UIManager.put("Table.selectionForeground",     Color.WHITE);
        UIManager.put("TableHeader.background",        ACCENT_DARK);
        UIManager.put("TableHeader.foreground",        Color.WHITE);
        UIManager.put("TableHeader.font",              BODY_FONT);

        // Tabs
        UIManager.put("TabbedPane.background",         BACKGROUND);
        UIManager.put("TabbedPane.foreground",         TEXT);
        UIManager.put("TabbedPane.selected",           CARD);
        UIManager.put("TabbedPane.selectedForeground", ACCENT_DARK);
        UIManager.put("TabbedPane.unselectedBackground", PANEL);
        UIManager.put("TabbedPane.font",               BODY_FONT);

        // Scroll panes
        UIManager.put("ScrollPane.background",         BACKGROUND);
        UIManager.put("Viewport.background",           BACKGROUND);

        // Combo boxes
        UIManager.put("ComboBox.background",           Color.WHITE);
        UIManager.put("ComboBox.foreground",           TEXT);
        UIManager.put("ComboBox.font",                 BODY_FONT);
        UIManager.put("ComboBox.selectionBackground",  ACCENT);
        UIManager.put("ComboBox.selectionForeground",  Color.WHITE);

        // Option panes (popups/dialogs)
        UIManager.put("OptionPane.background",         BACKGROUND);
        UIManager.put("OptionPane.messageForeground",  TEXT);

        // Menu
        UIManager.put("MenuBar.background",            ACCENT_DARK);
        UIManager.put("MenuBar.foreground",            Color.WHITE);
        UIManager.put("MenuItem.background",           BACKGROUND);
        UIManager.put("MenuItem.foreground",           TEXT);
    }

    public static JLabel titleLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(TITLE_FONT);
        label.setForeground(ACCENT_DARK);
        return label;
    }

    public static JButton primaryButton(String text) {
        JButton button = new JButton(text);
        button.setFont(HEADER_FONT);
        button.setBackground(ACCENT);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setOpaque(true);
        button.setBorderPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(10, 18, 10, 18));
        return button;
    }

    public static JButton secondaryButton(String text) {
        JButton button = new JButton(text);
        button.setFont(BODY_FONT);
        button.setBackground(ACCENT_DARK);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setOpaque(true);
        button.setBorderPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(8, 14, 8, 14));
        return button;
    }

    public static void styleField(JTextField field) {
        field.setFont(BODY_FONT);
        field.setForeground(TEXT);
        field.setBackground(Color.WHITE);
        field.setOpaque(true);
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(GRID, 1),
                BorderFactory.createEmptyBorder(8, 10, 8, 10)
        ));
    }

    public static void styleCard(JComponent component) {
        component.setBackground(CARD);
        component.setOpaque(true);
        component.setBorder(CARD_BORDER);
    }
}
