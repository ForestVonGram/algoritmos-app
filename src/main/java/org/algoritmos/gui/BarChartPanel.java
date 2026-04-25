package org.algoritmos.gui;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;

/**
 * BarChartPanel - Componente de gráfica de barras personalizado.
 *
 * Dibuja barras verticales con etiquetas, valores y leyenda.
 * Soporta valores negativos (no disponibles) mostrando la barra en gris.
 */
public class BarChartPanel extends JPanel {

    private static final Color BG        = MainWindow.C_BG_DARK;
    private static final Color GRID_LINE = new Color(45, 45, 62);
    private static final Color TEXT_FG   = MainWindow.C_TEXT;
    private static final Color NO_DATA   = new Color(55, 55, 72);
    private static final Color TITLE_FG  = MainWindow.C_ACCENT;

    private String[] labels  = new String[0];
    private double[] values  = new double[0];
    private Color[]  colors  = new Color[0];
    private String   title   = "";

    public BarChartPanel() {
        setBackground(BG);
        setPreferredSize(new Dimension(800, 300));
    }

    public void setData(String[] labels, double[] values, Color[] colors, String title) {
        this.labels = labels;
        this.values = values;
        this.colors = colors;
        this.title  = title;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (labels == null || labels.length == 0) return;

        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        int w = getWidth();
        int h = getHeight();

        // Márgenes
        int marginLeft   = 70;
        int marginRight  = 20;
        int marginTop    = 50;
        int marginBottom = 80;

        int chartW = w - marginLeft - marginRight;
        int chartH = h - marginTop - marginBottom;

        // Título
        g2.setFont(new Font("Segoe UI", Font.BOLD, 13));
        g2.setColor(TITLE_FG);
        FontMetrics fm = g2.getFontMetrics();
        g2.drawString(title, (w - fm.stringWidth(title)) / 2, 28);

        // Valor máximo
        double maxVal = 0;
        for (double v : values) if (v > maxVal) maxVal = v;
        if (maxVal == 0) maxVal = 1;

        // Líneas de cuadrícula horizontales
        int gridLines = 5;
        g2.setFont(new Font("Consolas", Font.PLAIN, 10));
        g2.setColor(TEXT_FG);
        for (int i = 0; i <= gridLines; i++) {
            int y = marginTop + chartH - (int)(chartH * i / (double) gridLines);
            g2.setColor(GRID_LINE);
            g2.drawLine(marginLeft, y, marginLeft + chartW, y);
            g2.setColor(TEXT_FG);
            double val = maxVal * i / gridLines;
            String label = val >= 1000 ? String.format("%.0f s", val / 1000)
                         : val >= 1    ? String.format("%.0f ms", val)
                         :               String.format("%.2f ms", val);
            g2.drawString(label, 2, y + 4);
        }

        // Eje Y
        g2.setColor(new Color(80, 80, 100));
        g2.drawLine(marginLeft, marginTop, marginLeft, marginTop + chartH);

        // Eje X
        g2.drawLine(marginLeft, marginTop + chartH, marginLeft + chartW, marginTop + chartH);

        // Barras
        int n = labels.length;
        int barAreaW = chartW / n;
        int barW = Math.max(8, barAreaW - 10);

        for (int i = 0; i < n; i++) {
            int x = marginLeft + i * barAreaW + (barAreaW - barW) / 2;
            double val = values[i];

            Color barColor = (val < 0) ? NO_DATA : colors[i % colors.length];

            int barH = (val < 0) ? 6 : (int)(chartH * val / maxVal);
            int y = marginTop + chartH - barH;

            // Sombra
            g2.setColor(new Color(0, 0, 0, 60));
            g2.fill(new RoundRectangle2D.Float(x + 3, y + 3, barW, barH, 4, 4));

            // Barra
            g2.setColor(barColor);
            g2.fill(new RoundRectangle2D.Float(x, y, barW, barH, 4, 4));

            // Brillo superior
            g2.setColor(new Color(255, 255, 255, 40));
            g2.fill(new RoundRectangle2D.Float(x, y, barW, Math.min(barH, 8), 4, 4));

            // Valor encima de la barra
            if (val >= 0) {
                g2.setFont(new Font("Consolas", Font.PLAIN, 9));
                g2.setColor(TEXT_FG);
                String valStr = val >= 1000 ? String.format("%.1fs", val / 1000)
                              : val >= 1    ? String.format("%.0fms", val)
                              :               String.format("%.2fms", val);
                FontMetrics vfm = g2.getFontMetrics();
                int vx = x + (barW - vfm.stringWidth(valStr)) / 2;
                if (y - 4 > marginTop) {
                    g2.drawString(valStr, vx, y - 4);
                }
            }

            // Etiqueta abajo (nombre corto del algoritmo) — rotada 40°
            String shortName = shortName(labels[i]);
            Graphics2D g2r = (Graphics2D) g2.create();
            g2r.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2r.setFont(new Font("Segoe UI", Font.PLAIN, 9));
            g2r.setColor(TEXT_FG);
            g2r.translate(x + barW / 2, marginTop + chartH + 8);
            g2r.rotate(Math.toRadians(40));
            g2r.drawString(shortName, 0, 0);
            g2r.dispose();
        }

        // Leyenda "Sin datos"
        g2.setFont(new Font("Segoe UI", Font.ITALIC, 10));
        g2.setColor(NO_DATA);
        g2.fillRect(w - 120, h - 20, 10, 10);
        g2.setColor(TEXT_FG);
        g2.drawString("Sin datos", w - 106, h - 11);
    }

    private String shortName(String name) {
        // Acortar nombres largos para que quepan
        return name.replace("Sequential", "Seq")
                   .replace("Parallel", "Par")
                   .replace("Enhanced", "Enh")
                   .replace("Block", "Blk")
                   .replace("Unrolling", "Unr")
                   .replace("Original", "Orig")
                   .replace("Scaled", "Scl")
                   .replace("Winograd", "Wino")
                   .replace("Strassen", "Str");
    }
}
