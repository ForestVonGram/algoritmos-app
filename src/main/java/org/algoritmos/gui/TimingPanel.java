package org.algoritmos.gui;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.List;

/**
 * TimingPanel - Pestaña de comparación de tiempos de ejecución.
 *
 * Lee los archivos de log generados por cada algoritmo y muestra:
 *   - Tabla resumen con todos los tiempos
 *   - Gráfica de barras comparativa por tamaño de matriz
 */
public class TimingPanel extends JPanel {

    private static final Color BG      = MainWindow.C_BG;
    private static final Color BG2     = MainWindow.C_BG_PANEL;
    private static final Color ACCENT  = MainWindow.C_ACCENT;
    private static final Color FG      = MainWindow.C_TEXT;
    private static final Color CELL_BG = MainWindow.C_BG_DARK;
    private static final Color CELL_ALT= new Color(30, 30, 46);

    // Colores para las barras de cada algoritmo
    private static final Color[] BAR_COLORS = {
        new Color(100, 180, 255), new Color(100, 220, 140), new Color(255, 180, 80),
        new Color(255, 100, 100), new Color(180, 100, 255), new Color(100, 220, 220),
        new Color(255, 140, 60),  new Color(140, 255, 140), new Color(255, 100, 200),
        new Color(200, 200, 100), new Color(80, 160, 255),  new Color(255, 200, 100),
        new Color(160, 255, 160), new Color(255, 160, 160), new Color(160, 160, 255)
    };

    private static final String[] ALGO_NAMES = {
        "NaivOnArray", "NaivLoopUnrollingTwo", "NaivLoopUnrollingFour",
        "WinogradOriginal", "WinogradScaled",
        "StrassenNaiv", "StrassenWinograd",
        "ThreeSequentialBlock", "ThreeParallelBlock", "ThreeEnhancedParallelBlock",
        "FourSequentialBlock", "FourParallelBlock", "FourEnhancedParallelBlock",
        "FiveSequentialBlock", "FiveParallelBlock"
    };

    private static final int[] SIZES = {32, 64, 128, 512, 1024, 2048};

    private JComboBox<String> cbCase;
    private JComboBox<String> cbSize;
    private JTable table;
    private DefaultTableModel tableModel;
    private BarChartPanel chartPanel;
    private JLabel lblStatus;

    // datos[algo][size] = tiempo en ms (-1 si no disponible)
    private double[][] data;

    public TimingPanel() {
        setLayout(new BorderLayout(8, 8));
        setBackground(BG);
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        add(buildControls(), BorderLayout.NORTH);

        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
                buildTable(), buildChart());
        split.setDividerLocation(220);
        split.setBackground(BG);
        split.setBorder(null);
        split.setDividerSize(6);
        add(split, BorderLayout.CENTER);

        add(buildStatus(), BorderLayout.SOUTH);
    }

    // -----------------------------------------------------------------------
    // Controles
    // -----------------------------------------------------------------------
    private JPanel buildControls() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 6));
        panel.setBackground(BG2);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(60, 60, 80)),
                BorderFactory.createEmptyBorder(6, 10, 6, 10)
        ));

        panel.add(makeLabel("Caso:"));
        cbCase = makeCombo("Caso 1", "Caso 2");
        panel.add(cbCase);

        panel.add(makeLabel("Tamaño para gráfica:"));
        cbSize = makeCombo("32×32", "64×64", "128×128", "512×512", "1024×1024", "2048×2048");
        cbSize.setSelectedIndex(2); // 128 por defecto
        panel.add(cbSize);

        JButton btnLoad = new JButton("  Cargar Tiempos  ");
        btnLoad.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnLoad.setBackground(new Color(40, 100, 160));
        btnLoad.setForeground(Color.WHITE);
        btnLoad.setFocusPainted(false);
        btnLoad.setBorderPainted(false);
        btnLoad.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnLoad.addActionListener(e -> loadTimings());
        panel.add(btnLoad);

        JButton btnRefresh = new JButton("↻ Actualizar");
        btnRefresh.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btnRefresh.setBackground(new Color(60, 80, 60));
        btnRefresh.setForeground(Color.WHITE);
        btnRefresh.setFocusPainted(false);
        btnRefresh.setBorderPainted(false);
        btnRefresh.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnRefresh.addActionListener(e -> loadTimings());
        panel.add(btnRefresh);

        cbCase.addActionListener(e -> { if (data != null) updateChart(); });
        cbSize.addActionListener(e -> { if (data != null) updateChart(); });

        return panel;
    }

    // -----------------------------------------------------------------------
    // Tabla resumen
    // -----------------------------------------------------------------------
    private JScrollPane buildTable() {
        // Columnas: Algoritmo | 32 | 64 | 128 | 512 | 1024 | 2048
        String[] cols = new String[SIZES.length + 1];
        cols[0] = "Algoritmo";
        for (int i = 0; i < SIZES.length; i++) cols[i + 1] = SIZES[i] + "×" + SIZES[i];

        tableModel = new DefaultTableModel(cols, 0) {
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

        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(
                    JTable t, Object val, boolean sel, boolean foc, int row, int col) {
                super.getTableCellRendererComponent(t, val, sel, foc, row, col);
                if (!sel) {
                    setBackground(row % 2 == 0 ? CELL_BG : CELL_ALT);
                    setForeground(col == 0 ? ACCENT : FG);
                }
                setHorizontalAlignment(col == 0 ? SwingConstants.LEFT : SwingConstants.RIGHT);
                setBorder(BorderFactory.createEmptyBorder(0, 6, 0, 6));
                return this;
            }
        });

        JTableHeader header = table.getTableHeader();
        header.setBackground(new Color(50, 80, 120));
        header.setForeground(Color.WHITE);
        header.setFont(new Font("Segoe UI", Font.BOLD, 11));

        // Ancho de columnas
        table.getColumnModel().getColumn(0).setPreferredWidth(200);
        for (int i = 1; i <= SIZES.length; i++) {
            table.getColumnModel().getColumn(i).setPreferredWidth(110);
        }

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBackground(CELL_BG);
        scroll.getViewport().setBackground(CELL_BG);
        scroll.setBorder(BorderFactory.createLineBorder(new Color(60, 60, 80)));
        return scroll;
    }

    // -----------------------------------------------------------------------
    // Gráfica de barras
    // -----------------------------------------------------------------------
    private JPanel buildChart() {
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setBackground(BG);

        JLabel lbl = new JLabel("  Gráfica de barras — Tiempo de ejecución (ms)");
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lbl.setForeground(ACCENT);
        wrapper.add(lbl, BorderLayout.NORTH);

        chartPanel = new BarChartPanel();
        wrapper.add(chartPanel, BorderLayout.CENTER);

        return wrapper;
    }

    // -----------------------------------------------------------------------
    // Barra de estado
    // -----------------------------------------------------------------------
    private JPanel buildStatus() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panel.setBackground(BG2);
        panel.setBorder(BorderFactory.createEmptyBorder(4, 10, 4, 10));
        lblStatus = new JLabel("Presiona 'Cargar Tiempos' para leer los logs.");
        lblStatus.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblStatus.setForeground(new Color(140, 140, 160));
        panel.add(lblStatus);
        return panel;
    }

    // -----------------------------------------------------------------------
    // Cargar tiempos desde los archivos de log (público para llamada externa)
    // -----------------------------------------------------------------------
    public void loadTimings() {
        int caseNum = cbCase.getSelectedIndex() + 1;

        // data[algoIdx][sizeIdx] = tiempo promedio en ms
        data = new double[ALGO_NAMES.length][SIZES.length];
        for (double[] row : data) Arrays.fill(row, -1.0);

        int loaded = 0;

        for (int a = 0; a < ALGO_NAMES.length; a++) {
            String logFile = "datos/logs/tiempos_" + ALGO_NAMES[a] + ".txt";
            if (!Files.exists(Paths.get(logFile))) continue;

            try (BufferedReader br = new BufferedReader(new FileReader(logFile))) {
                String line;
                // Acumular tiempos por tamaño para promediar si hay varias ejecuciones
                Map<Integer, List<Double>> times = new HashMap<>();
                for (int s : SIZES) times.put(s, new ArrayList<>());

                while ((line = br.readLine()) != null) {
                    // Formato: Algoritmo=X | Caso=Y | n=Z | n*n=W | TE=T ms
                    if (!line.contains("Caso=" + caseNum)) continue;
                    try {
                        int nVal = Integer.parseInt(
                                line.split("n=")[1].split(" ")[0].split("\\|")[0].trim());
                        double te = Double.parseDouble(
                                line.split("TE=")[1].split(" ")[0].trim());
                        if (times.containsKey(nVal)) times.get(nVal).add(te);
                    } catch (Exception ignored) {}
                }

                for (int s = 0; s < SIZES.length; s++) {
                    List<Double> ts = times.get(SIZES[s]);
                    if (!ts.isEmpty()) {
                        data[a][s] = ts.stream().mapToDouble(Double::doubleValue).average().orElse(-1);
                        loaded++;
                    }
                }
            } catch (IOException ignored) {}
        }

        updateTable();
        updateChart();

        lblStatus.setText("✔ Datos cargados — " + loaded + " registros encontrados para Caso " + caseNum);
        lblStatus.setForeground(new Color(100, 200, 100));
    }

    // -----------------------------------------------------------------------
    // Actualizar tabla
    // -----------------------------------------------------------------------
    private void updateTable() {
        tableModel.setRowCount(0);
        for (int a = 0; a < ALGO_NAMES.length; a++) {
            Object[] row = new Object[SIZES.length + 1];
            row[0] = ALGO_NAMES[a];
            for (int s = 0; s < SIZES.length; s++) {
                row[s + 1] = data[a][s] < 0 ? "—" :
                        String.format("%.2f ms", data[a][s]);
            }
            tableModel.addRow(row);
        }
    }

    // -----------------------------------------------------------------------
    // Actualizar gráfica
    // -----------------------------------------------------------------------
    private void updateChart() {
        int sizeIdx = cbSize.getSelectedIndex();
        double[] values = new double[ALGO_NAMES.length];
        for (int a = 0; a < ALGO_NAMES.length; a++) {
            values[a] = data[a][sizeIdx];
        }
        chartPanel.setData(ALGO_NAMES, values, BAR_COLORS,
                "Tiempo de ejecución — " + SIZES[sizeIdx] + "×" + SIZES[sizeIdx] +
                " — Caso " + (cbCase.getSelectedIndex() + 1));
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
