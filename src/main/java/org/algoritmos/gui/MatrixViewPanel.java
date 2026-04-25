package org.algoritmos.gui;

import org.algoritmos.MatrixGenerator;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * MatrixViewPanel - Pestaña para visualizar las matrices A, B y C.
 *
 * Permite seleccionar:
 *   - Caso (1 o 2)
 *   - Tamaño (32, 64, 128, 512, 1024, 2048)
 *   - Matriz (A, B, o resultado C de cualquier algoritmo)
 *
 * Muestra la matriz en una tabla con scroll, con las primeras
 * MAX_PREVIEW filas/columnas para matrices grandes.
 */
public class MatrixViewPanel extends JPanel {

    private static final Color BG       = MainWindow.C_BG;
    private static final Color BG2      = MainWindow.C_BG_PANEL;
    private static final Color ACCENT   = MainWindow.C_ACCENT;
    private static final Color FG       = MainWindow.C_TEXT;
    private static final Color CELL_BG  = MainWindow.C_BG_DARK;
    private static final Color CELL_ALT = new Color(30, 30, 46);
    private static final Color HEADER_BG = new Color(40, 60, 100);

    private static final int MAX_PREVIEW = 16; // máx filas/cols a mostrar

    private JComboBox<String> cbCase;
    private JComboBox<String> cbSize;
    private JComboBox<String> cbMatrix;
    private JTable table;
    private DefaultTableModel tableModel;
    private JLabel lblInfo;
    private JLabel lblStatus;

    // Carpetas de resultados disponibles
    private static final String[] RESULT_DIRS = {
        "NaivOnArray", "NaivLoopUnrollingTwo", "NaivLoopUnrollingFour",
        "WinogradOriginal", "WinogradScaled",
        "StrassenNaiv", "StrassenWinograd",
        "ThreeSequentialBlock", "ThreeParallelBlock", "ThreeEnhancedParallelBlock",
        "FourSequentialBlock", "FourParallelBlock", "FourEnhancedParallelBlock",
        "FiveSequentialBlock", "FiveParallelBlock"
    };

    public MatrixViewPanel() {
        setLayout(new BorderLayout(8, 8));
        setBackground(BG);
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        add(buildControls(), BorderLayout.NORTH);
        add(buildTable(),    BorderLayout.CENTER);
        add(buildStatus(),   BorderLayout.SOUTH);
    }

    // -----------------------------------------------------------------------
    // Panel de controles
    // -----------------------------------------------------------------------
    private JPanel buildControls() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 6));
        panel.setBackground(BG2);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(60, 60, 80)),
                BorderFactory.createEmptyBorder(6, 10, 6, 10)
        ));

        // Caso
        panel.add(makeLabel("Caso:"));
        cbCase = makeCombo("Caso 1", "Caso 2");
        panel.add(cbCase);

        // Tamaño
        panel.add(makeLabel("Tamaño:"));
        cbSize = makeCombo("32×32", "64×64", "128×128", "512×512", "1024×1024", "2048×2048");
        panel.add(cbSize);

        // Matriz
        panel.add(makeLabel("Matriz:"));
        String[] matrixOptions = buildMatrixOptions();
        cbMatrix = makeCombo(matrixOptions);
        panel.add(cbMatrix);

        // Botón cargar
        JButton btnLoad = new JButton("  Cargar  ");
        btnLoad.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnLoad.setBackground(new Color(40, 100, 160));
        btnLoad.setForeground(Color.WHITE);
        btnLoad.setFocusPainted(false);
        btnLoad.setBorderPainted(false);
        btnLoad.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnLoad.addActionListener(e -> loadMatrix());
        panel.add(btnLoad);

        // Info
        lblInfo = new JLabel("  Selecciona una matriz y presiona Cargar");
        lblInfo.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        lblInfo.setForeground(new Color(140, 140, 160));
        panel.add(lblInfo);

        return panel;
    }

    private String[] buildMatrixOptions() {
        java.util.List<String> opts = new java.util.ArrayList<>();
        opts.add("A (entrada)");
        opts.add("B (entrada)");
        for (String dir : RESULT_DIRS) {
            opts.add("C - " + dir);
        }
        return opts.toArray(new String[0]);
    }

    // -----------------------------------------------------------------------
    // Tabla
    // -----------------------------------------------------------------------
    private JScrollPane buildTable() {
        tableModel = new DefaultTableModel() {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        table = new JTable(tableModel);
        table.setBackground(CELL_BG);
        table.setForeground(FG);
        table.setFont(new Font("Consolas", Font.PLAIN, 11));
        table.setRowHeight(22);
        table.setShowGrid(true);
        table.setGridColor(new Color(50, 50, 65));
        table.setSelectionBackground(new Color(60, 100, 160));
        table.setSelectionForeground(Color.WHITE);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        // Filas alternadas
        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(
                    JTable t, Object val, boolean sel, boolean foc, int row, int col) {
                super.getTableCellRendererComponent(t, val, sel, foc, row, col);
                setHorizontalAlignment(SwingConstants.RIGHT);
                if (!sel) {
                    setBackground(row % 2 == 0 ? CELL_BG : CELL_ALT);
                    setForeground(FG);
                }
                setBorder(BorderFactory.createEmptyBorder(0, 6, 0, 6));
                return this;
            }
        });

        // Header
        JTableHeader header = table.getTableHeader();
        header.setBackground(HEADER_BG);
        header.setForeground(Color.WHITE);
        header.setFont(new Font("Segoe UI", Font.BOLD, 11));
        header.setReorderingAllowed(false);

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBackground(CELL_BG);
        scroll.getViewport().setBackground(CELL_BG);
        scroll.setBorder(BorderFactory.createLineBorder(new Color(60, 60, 80)));
        return scroll;
    }

    // -----------------------------------------------------------------------
    // Barra de estado
    // -----------------------------------------------------------------------
    private JPanel buildStatus() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panel.setBackground(BG2);
        panel.setBorder(BorderFactory.createEmptyBorder(4, 10, 4, 10));

        lblStatus = new JLabel("Sin datos cargados");
        lblStatus.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblStatus.setForeground(new Color(140, 140, 160));
        panel.add(lblStatus);

        return panel;
    }

    // -----------------------------------------------------------------------
    // Cargar y mostrar matriz
    // -----------------------------------------------------------------------
    private void loadMatrix() {
        int caseNum = cbCase.getSelectedIndex() + 1;
        int[] sizes = {32, 64, 128, 512, 1024, 2048};
        int n = sizes[cbSize.getSelectedIndex()];
        String matrixOpt = (String) cbMatrix.getSelectedItem();

        String path;
        if (matrixOpt.equals("A (entrada)")) {
            path = MatrixGenerator.pathA(caseNum, n);
        } else if (matrixOpt.equals("B (entrada)")) {
            path = MatrixGenerator.pathB(caseNum, n);
        } else {
            // Resultado C de algún algoritmo
            String algoDir = matrixOpt.replace("C - ", "");
            path = "datos/resultados/" + algoDir +
                   "/caso" + caseNum + "_C_" + n + "x" + n + ".txt";
        }

        if (!Files.exists(Paths.get(path))) {
            lblStatus.setText("⚠ Archivo no encontrado: " + path);
            lblStatus.setForeground(new Color(255, 180, 60));
            tableModel.setRowCount(0);
            tableModel.setColumnCount(0);
            return;
        }

        try {
            long[][] M = MatrixGenerator.load(path);
            displayMatrix(M, n, path);
        } catch (IOException ex) {
            lblStatus.setText("✘ Error al leer: " + ex.getMessage());
            lblStatus.setForeground(Color.RED);
        }
    }

    private void displayMatrix(long[][] M, int n, String path) {
        int rows = Math.min(n, MAX_PREVIEW);
        int cols = Math.min(n, MAX_PREVIEW);

        // Columnas
        tableModel.setColumnCount(0);
        tableModel.setRowCount(0);

        // Agregar columna de índice de fila
        tableModel.addColumn("i \\ j");
        for (int j = 0; j < cols; j++) {
            tableModel.addColumn("" + j);
        }
        if (cols < n) tableModel.addColumn("...");

        // Filas
        for (int i = 0; i < rows; i++) {
            Object[] row = new Object[cols + 1 + (cols < n ? 1 : 0)];
            row[0] = i;
            for (int j = 0; j < cols; j++) {
                row[j + 1] = M[i][j];
            }
            if (cols < n) row[cols + 1] = "...";
            tableModel.addRow(row);
        }
        if (rows < n) {
            Object[] dotRow = new Object[cols + 1 + (cols < n ? 1 : 0)];
            dotRow[0] = "...";
            for (int j = 1; j < dotRow.length; j++) dotRow[j] = "...";
            tableModel.addRow(dotRow);
        }

        // Ajustar ancho de columnas
        for (int c = 0; c < table.getColumnCount(); c++) {
            table.getColumnModel().getColumn(c).setPreferredWidth(c == 0 ? 40 : 90);
        }

        boolean truncated = n > MAX_PREVIEW;
        String info = String.format("Matriz %dx%d  |  Mostrando %dx%d%s  |  %s",
                n, n, rows, cols,
                truncated ? " (primeras " + MAX_PREVIEW + " filas/cols)" : "",
                path);

        lblStatus.setText(info);
        lblStatus.setForeground(new Color(100, 200, 100));
        lblInfo.setText("  ✔ Cargada: " + Paths.get(path).getFileName());
    }

    // -----------------------------------------------------------------------
    // Helpers
    // -----------------------------------------------------------------------
    private JLabel makeLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lbl.setForeground(FG);
        return lbl;
    }

    private JComboBox<String> makeCombo(String... items) {
        JComboBox<String> cb = new JComboBox<>(items);
        cb.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        cb.setBackground(BG2);
        cb.setForeground(FG);
        cb.setFocusable(false);
        return cb;
    }
}
