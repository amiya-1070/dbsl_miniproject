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
    public static final Color BACKGROUND = new Color(232, 245, 255);
    public static final Color PANEL = new Color(213, 235, 250);
    public static final Color CARD = new Color(244, 250, 255);
    public static final Color ACCENT = new Color(44, 127, 184);
    public static final Color ACCENT_DARK = new Color(12, 30, 48);
    public static final Color TEXT = new Color(10, 18, 24);
    public static final Color GRID = new Color(169, 203, 227);
    public static final Font TITLE_FONT = new Font("SansSerif", Font.BOLD, 28);
    public static final Font HEADER_FONT = new Font("SansSerif", Font.BOLD, 18);
    public static final Font BODY_FONT = new Font("SansSerif", Font.PLAIN, 14);
    public static final Border CARD_BORDER = BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(GRID, 1),
            BorderFactory.createEmptyBorder(16, 16, 16, 16)
    );

    private Theme() {
    }

    public static void applyLookAndFeelDefaults() {
        UIManager.put("Panel.background", BACKGROUND);
        UIManager.put("OptionPane.background", BACKGROUND);
        UIManager.put("OptionPane.messageForeground", TEXT);
        UIManager.put("Table.gridColor", GRID);
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
        button.setBorder(BorderFactory.createEmptyBorder(10, 18, 10, 18));
        return button;
    }

    public static JButton secondaryButton(String text) {
        JButton button = new JButton(text);
        button.setFont(BODY_FONT);
        button.setBackground(ACCENT_DARK);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(8, 14, 8, 14));
        return button;
    }

    public static void styleField(JTextField field) {
        field.setFont(BODY_FONT);
        field.setForeground(TEXT);
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(GRID, 1),
                BorderFactory.createEmptyBorder(8, 10, 8, 10)
        ));
    }

    public static void styleCard(JComponent component) {
        component.setBackground(CARD);
        component.setBorder(CARD_BORDER);
    }
}
