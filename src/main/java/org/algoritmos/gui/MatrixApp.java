package org.algoritmos.gui;

import javax.swing.*;
import javax.swing.plaf.metal.MetalLookAndFeel;
import javax.swing.plaf.metal.OceanTheme;

/**
 * MatrixApp - Punto de entrada de la interfaz gráfica.
 */
public class MatrixApp {

    public static void main(String[] args) {
        // Usar Metal L&F con tema Ocean — más controlable que el L&F del sistema
        try {
            MetalLookAndFeel.setCurrentTheme(new OceanTheme());
            UIManager.setLookAndFeel(new MetalLookAndFeel());
        } catch (Exception ignored) {}

        SwingUtilities.invokeLater(() -> {
            MainWindow window = new MainWindow();
            window.setVisible(true);
        });
    }
}
