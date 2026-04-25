package org.algoritmos.gui;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;

/**
 * ComplexityPanel - Pestaña con la tabla de órdenes de complejidad
 * de cada algoritmo, según el documento del seguimiento.
 */
public class ComplexityPanel extends JPanel {

    private static final Color BG       = MainWindow.C_BG;
    private static final Color BG2      = MainWindow.C_BG_PANEL;
    private static final Color ACCENT   = MainWindow.C_ACCENT;
    private static final Color FG       = MainWindow.C_TEXT;
    private static final Color CELL_BG  = MainWindow.C_BG_DARK;
    private static final Color CELL_ALT = new Color(30, 30, 46);
    private static final Color GREEN    = MainWindow.C_ACCENT2;
    private static final Color YELLOW   = new Color(255, 210, 80);
    private static final Color ORANGE   = new Color(255, 160, 60);

    // Datos: { #, Nombre, Complejidad temporal, Tipo, Descripción }
    private static final Object[][] DATA = {
        { 1,  "NaivOnArray",                "O(n³)",        "Iterativo",   "Triple bucle clásico fila×columna" },
        { 2,  "NaivLoopUnrollingTwo",        "O(n³)",        "Iterativo",   "Desenrollado de bucle ×2 — reduce overhead de control" },
        { 3,  "NaivLoopUnrollingFour",       "O(n³)",        "Iterativo",   "Desenrollado de bucle ×4 — mejor uso del pipeline" },
        { 4,  "WinogradOriginal",            "O(n³)",        "Iterativo",   "~n³/2 multiplicaciones con pre-cómputo de factores" },
        { 5,  "WinogradScaled",              "O(n³)",        "Iterativo",   "Winograd + escalado para estabilidad numérica" },
        { 6,  "StrassenNaiv",                "O(n^2.807)",   "D&V",         "7 productos recursivos — Strassen 1969" },
        { 7,  "StrassenWinograd",            "O(n^2.807)",   "D&V",         "Strassen con optimización Winograd (15 sumas vs 18)" },
        { 8,  "III.3 ThreeSequentialBlock",  "O(n³)",        "Bloques",     "Row×Col por bloques — mejor localidad de caché" },
        { 9,  "III.4 ThreeParallelBlock",    "O(n³/p)",      "Paralelo",    "Bloques Row×Col paralelos con IntStream.parallel()" },
        { 10, "III.5 ThreeEnhancedParallel", "O(n³/2)",      "Paralelo",    "2 hilos: mitad superior e inferior de filas" },
        { 11, "IV.3 FourSequentialBlock",    "O(n³)",        "Bloques",     "Row×Row por bloques — mejor localidad de caché" },
        { 12, "IV.4 FourParallelBlock",      "O(n³/p)",      "Paralelo",    "Bloques Row×Row paralelos con IntStream.parallel()" },
        { 13, "IV.5 FourEnhancedParallel",   "O(n³/2)",      "Paralelo",    "2 hilos: mitad superior e inferior de filas" },
        { 14, "V.3 FiveSequentialBlock",     "O(n³)",        "Bloques",     "Col×Col por bloques — mejor localidad de caché" },
        { 15, "V.4 FiveParallelBlock",       "O(n³/p)",      "Paralelo",    "Bloques Col×Col paralelos con IntStream.parallel()" },
    };

    public ComplexityPanel() {
        setLayout(new BorderLayout(8, 8));
        setBackground(BG);
        setBorder(BorderFactory.createEmptyBorder(14, 14, 14, 14));

        add(buildTitle(),  BorderLayout.NORTH);
        add(buildTable(),  BorderLayout.CENTER);
        add(buildLegend(), BorderLayout.SOUTH);
    }

    // -----------------------------------------------------------------------
    // Título
    // -----------------------------------------------------------------------
    private JPanel buildTitle() {
        JPanel panel = new JPanel(new GridLayout(2, 1, 0, 4));
        panel.setBackground(BG);
        panel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));

        JLabel t1 = new JLabel("Órdenes de Complejidad — Algoritmos de Multiplicación de Matrices");
        t1.setFont(new Font("Segoe UI", Font.BOLD, 16));
        t1.setForeground(ACCENT);

        JLabel t2 = new JLabel("Seguimiento 2  ·  Universidad del Quindío  ·  Ingeniería de Sistemas y Computación");
        t2.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        t2.setForeground(new Color(140, 140, 160));

        panel.add(t1);
        panel.add(t2);
        return panel;
    }

    // -----------------------------------------------------------------------
    // Tabla
    // -----------------------------------------------------------------------
    private JScrollPane buildTable() {
        String[] cols = { "#", "Algoritmo", "Complejidad temporal", "Tipo", "Descripción" };

        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        for (Object[] row : DATA) model.addRow(row);

        JTable table = new JTable(model);
        table.setBackground(CELL_BG);
        table.setForeground(FG);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        table.setRowHeight(28);
        table.setShowGrid(true);
        table.setGridColor(new Color(50, 50, 65));
        table.setSelectionBackground(new Color(60, 100, 160));
        table.setSelectionForeground(Color.WHITE);
        table.setIntercellSpacing(new Dimension(0, 1));

        // Renderer personalizado por columna
        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(
                    JTable t, Object val, boolean sel, boolean foc, int row, int col) {
                super.getTableCellRendererComponent(t, val, sel, foc, row, col);
                setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));

                if (!sel) {
                    setBackground(row % 2 == 0 ? CELL_BG : CELL_ALT);

                    switch (col) {
                        case 0 -> { // #
                            setForeground(new Color(140, 140, 160));
                            setHorizontalAlignment(CENTER);
                            setFont(new Font("Segoe UI", Font.PLAIN, 12));
                        }
                        case 1 -> { // Nombre
                            setForeground(ACCENT);
                            setHorizontalAlignment(LEFT);
                            setFont(new Font("Consolas", Font.BOLD, 13));
                        }
                        case 2 -> { // Complejidad
                            String v = val == null ? "" : val.toString();
                            if (v.contains("2.807"))      setForeground(GREEN);
                            else if (v.contains("n³/p"))  setForeground(YELLOW);
                            else if (v.contains("n³/2"))  setForeground(ORANGE);
                            else                          setForeground(new Color(200, 200, 200));
                            setHorizontalAlignment(CENTER);
                            setFont(new Font("Consolas", Font.BOLD, 14));
                        }
                        case 3 -> { // Tipo
                            String v = val == null ? "" : val.toString();
                            setForeground(switch (v) {
                                case "D&V"      -> GREEN;
                                case "Paralelo" -> YELLOW;
                                case "Bloques"  -> ORANGE;
                                default         -> new Color(180, 180, 200);
                            });
                            setHorizontalAlignment(CENTER);
                            setFont(new Font("Segoe UI", Font.BOLD, 12));
                        }
                        default -> { // Descripción
                            setForeground(new Color(180, 180, 200));
                            setHorizontalAlignment(LEFT);
                            setFont(new Font("Segoe UI", Font.PLAIN, 12));
                        }
                    }
                }
                return this;
            }
        });

        // Anchos de columna
        int[] widths = { 35, 230, 160, 90, 400 };
        for (int i = 0; i < widths.length; i++) {
            table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
        }

        JTableHeader header = table.getTableHeader();
        header.setBackground(new Color(50, 80, 120));
        header.setForeground(Color.WHITE);
        header.setFont(new Font("Segoe UI", Font.BOLD, 13));
        header.setPreferredSize(new Dimension(0, 32));
        header.setReorderingAllowed(false);

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBackground(CELL_BG);
        scroll.getViewport().setBackground(CELL_BG);
        scroll.setBorder(BorderFactory.createLineBorder(new Color(60, 60, 80)));
        return scroll;
    }

    // -----------------------------------------------------------------------
    // Leyenda de colores
    // -----------------------------------------------------------------------
    private JPanel buildLegend() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 8));
        panel.setBackground(BG2);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(60, 60, 80)),
                BorderFactory.createEmptyBorder(4, 12, 4, 12)
        ));

        panel.add(makeLegendItem(GREEN,  "O(n^2.807) — Divide y Vencerás (Strassen)"));
        panel.add(makeLegendItem(YELLOW, "O(n³/p)    — Paralelo (p hilos)"));
        panel.add(makeLegendItem(ORANGE, "O(n³/2)    — Enhanced Parallel (2 hilos)"));
        panel.add(makeLegendItem(new Color(200, 200, 200), "O(n³)      — Iterativo / Bloques secuencial"));

        return panel;
    }

    private JPanel makeLegendItem(Color color, String text) {
        JPanel item = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        item.setBackground(BG2);

        JLabel dot = new JLabel("●");
        dot.setFont(new Font("Segoe UI", Font.BOLD, 16));
        dot.setForeground(color);

        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("Consolas", Font.PLAIN, 11));
        lbl.setForeground(FG);

        item.add(dot);
        item.add(lbl);
        return item;
    }
}
