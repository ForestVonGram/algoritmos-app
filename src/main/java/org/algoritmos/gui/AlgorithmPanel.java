package org.algoritmos.gui;

import org.algoritmos.MatrixGenerator;
import org.algoritmos.naiveonarray.NaivOnArray;
import org.algoritmos.naivloopunrollingtwo.NaivLoopUnrollingTwo;
import org.algoritmos.naivloopunrollingfour.NaivLoopUnrollingFour;
import org.algoritmos.winogradoriginal.WinogradOriginal;
import org.algoritmos.winogradscaled.WinogradScaled;
import org.algoritmos.strassennaiv.StrassenNaiv;
import org.algoritmos.strassenwinograd.StrassenWinograd;
import org.algoritmos.threesequentialblock.ThreeSequentialBlock;
import org.algoritmos.threeparallelblock.ThreeParallelBlock;
import org.algoritmos.threeenhancedparallelblock.ThreeEnhancedParallelBlock;
import org.algoritmos.foursequentialblock.FourSequentialBlock;
import org.algoritmos.fourparallelblock.FourParallelBlock;
import org.algoritmos.fourenhancedparallelblock.FourEnhancedParallelBlock;
import org.algoritmos.fivesequentialblock.FiveSequentialBlock;
import org.algoritmos.fiveparallelblock.FiveParallelBlock;

import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;
import java.io.PrintStream;
import java.io.OutputStream;
import java.util.LinkedHashMap;

/**
 * AlgorithmPanel - Pestaña para seleccionar y ejecutar algoritmos.
 * Muestra checkboxes con todos los algoritmos, botones de control
 * y una consola de salida en tiempo real.
 */
public class AlgorithmPanel extends JPanel {

    // -----------------------------------------------------------------------
    // Colores del tema oscuro (paleta centralizada)
    // -----------------------------------------------------------------------
    private static final Color BG      = MainWindow.C_BG;
    private static final Color BG2     = MainWindow.C_BG_PANEL;
    private static final Color ACCENT  = MainWindow.C_ACCENT;
    private static final Color SUCCESS = MainWindow.C_ACCENT2;
    private static final Color WARNING = new Color(255, 200, 80);
    private static final Color FG      = MainWindow.C_TEXT;

    // -----------------------------------------------------------------------
    // Mapa de algoritmos
    // -----------------------------------------------------------------------
    private static final LinkedHashMap<String, Runnable> ALGORITHMS = new LinkedHashMap<>();

    static {
        ALGORITHMS.put("1. NaivOnArray",                  () -> run(NaivOnArray::run));
        ALGORITHMS.put("2. NaivLoopUnrollingTwo",         () -> run(NaivLoopUnrollingTwo::run));
        ALGORITHMS.put("3. NaivLoopUnrollingFour",        () -> run(NaivLoopUnrollingFour::run));
        ALGORITHMS.put("4. WinogradOriginal",             () -> run(WinogradOriginal::run));
        ALGORITHMS.put("5. WinogradScaled",               () -> run(WinogradScaled::run));
        ALGORITHMS.put("6. StrassenNaiv",                 () -> run(StrassenNaiv::run));
        ALGORITHMS.put("7. StrassenWinograd",             () -> run(StrassenWinograd::run));
        ALGORITHMS.put("8. III.3 ThreeSequentialBlock",   () -> run(ThreeSequentialBlock::run));
        ALGORITHMS.put("9. III.4 ThreeParallelBlock",     () -> run(ThreeParallelBlock::run));
        ALGORITHMS.put("10. III.5 ThreeEnhancedParallel", () -> run(ThreeEnhancedParallelBlock::run));
        ALGORITHMS.put("11. IV.3 FourSequentialBlock",    () -> run(FourSequentialBlock::run));
        ALGORITHMS.put("12. IV.4 FourParallelBlock",      () -> run(FourParallelBlock::run));
        ALGORITHMS.put("13. IV.5 FourEnhancedParallel",   () -> run(FourEnhancedParallelBlock::run));
        ALGORITHMS.put("14. V.3 FiveSequentialBlock",     () -> run(FiveSequentialBlock::run));
        ALGORITHMS.put("15. V.4 FiveParallelBlock",       () -> run(FiveParallelBlock::run));
    }

    @FunctionalInterface
    interface ThrowingRunnable { void run() throws Exception; }

    private static void run(ThrowingRunnable r) {
        try { r.run(); } catch (Exception e) { throw new RuntimeException(e); }
    }

    // -----------------------------------------------------------------------
    // Componentes UI
    // -----------------------------------------------------------------------
    private final LinkedHashMap<String, JCheckBox> checkboxes = new LinkedHashMap<>();
    private JTextPane console;
    private StyledDocument doc;
    private JButton btnRun;
    private JButton btnAll;
    private JButton btnGenerate;
    private JProgressBar progressBar;
    private final TimingPanel timingPanel;

    public AlgorithmPanel(TimingPanel timingPanel) {
        this.timingPanel = timingPanel;
        setLayout(new BorderLayout(8, 8));
        setBackground(BG);
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        add(buildLeftPanel(),   BorderLayout.WEST);
        add(buildConsole(),     BorderLayout.CENTER);
        add(buildBottomBar(),   BorderLayout.SOUTH);

        redirectSystemOut();
    }

    // -----------------------------------------------------------------------
    // Panel izquierdo: checkboxes de algoritmos
    // -----------------------------------------------------------------------
    private JPanel buildLeftPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 8));
        panel.setBackground(BG2);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(60, 60, 80), 1),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        panel.setPreferredSize(new Dimension(260, 0));

        // Título
        JLabel lbl = new JLabel("Algoritmos disponibles");
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lbl.setForeground(ACCENT);
        panel.add(lbl, BorderLayout.NORTH);

        // Lista de checkboxes
        JPanel listPanel = new JPanel();
        listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.Y_AXIS));
        listPanel.setBackground(BG2);

        for (String name : ALGORITHMS.keySet()) {
            JCheckBox cb = new JCheckBox(name);
            cb.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            cb.setForeground(FG);
            cb.setBackground(BG2);
            cb.setFocusPainted(false);
            cb.setBorder(BorderFactory.createEmptyBorder(3, 0, 3, 0));
            checkboxes.put(name, cb);
            listPanel.add(cb);
        }

        JScrollPane scroll = new JScrollPane(listPanel);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.setBackground(BG2);
        scroll.getViewport().setBackground(BG2);
        panel.add(scroll, BorderLayout.CENTER);

        // Botones seleccionar/deseleccionar
        JPanel selPanel = new JPanel(new GridLayout(1, 2, 5, 0));
        selPanel.setBackground(BG2);

        JButton selAll = makeSmallButton("Todos", new Color(60, 100, 60));
        JButton selNone = makeSmallButton("Ninguno", new Color(100, 60, 60));

        selAll.addActionListener(e -> checkboxes.values().forEach(cb -> cb.setSelected(true)));
        selNone.addActionListener(e -> checkboxes.values().forEach(cb -> cb.setSelected(false)));

        selPanel.add(selAll);
        selPanel.add(selNone);
        panel.add(selPanel, BorderLayout.SOUTH);

        return panel;
    }

    // -----------------------------------------------------------------------
    // Consola de salida
    // -----------------------------------------------------------------------
    private JPanel buildConsole() {
        JPanel panel = new JPanel(new BorderLayout(0, 5));
        panel.setBackground(BG);

        JLabel lbl = new JLabel("  Consola de ejecución");
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lbl.setForeground(ACCENT);
        panel.add(lbl, BorderLayout.NORTH);

        console = new JTextPane();
        console.setEditable(false);
        console.setBackground(new Color(15, 15, 25));
        console.setFont(new Font("Consolas", Font.PLAIN, 12));
        doc = console.getStyledDocument();

        JScrollPane scroll = new JScrollPane(console);
        scroll.setBorder(BorderFactory.createLineBorder(new Color(60, 60, 80)));
        panel.add(scroll, BorderLayout.CENTER);

        return panel;
    }

    // -----------------------------------------------------------------------
    // Barra inferior: botones y progreso
    // -----------------------------------------------------------------------
    private JPanel buildBottomBar() {
        JPanel panel = new JPanel(new BorderLayout(10, 0));
        panel.setBackground(BG);
        panel.setBorder(BorderFactory.createEmptyBorder(8, 0, 0, 0));

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        btnPanel.setBackground(BG);

        btnGenerate = makeButton("Generar Matrices",      new Color(50, 75, 120));
        btnRun      = makeButton("Ejecutar Seleccionados", new Color(35, 110, 55));
        btnAll      = makeButton("Ejecutar Todos",         new Color(110, 70, 20));

        JButton btnClear = makeButton("Limpiar Consola", new Color(100, 40, 40));

        btnGenerate.addActionListener(e -> generateMatrices());
        btnRun.addActionListener(e -> runSelected());
        btnAll.addActionListener(e -> runAll());
        btnClear.addActionListener(e -> clearConsole());

        btnPanel.add(btnGenerate);
        btnPanel.add(btnRun);
        btnPanel.add(btnAll);
        btnPanel.add(btnClear);

        progressBar = new JProgressBar();
        progressBar.setIndeterminate(false);
        progressBar.setStringPainted(true);
        progressBar.setString("Listo");
        progressBar.setBackground(new Color(40, 40, 55));
        progressBar.setForeground(ACCENT);
        progressBar.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        progressBar.setPreferredSize(new Dimension(200, 24));

        panel.add(btnPanel, BorderLayout.WEST);
        panel.add(progressBar, BorderLayout.EAST);

        return panel;
    }

    // -----------------------------------------------------------------------
    // Acciones
    // -----------------------------------------------------------------------
    private void generateMatrices() {
        setRunning(true, "Generando matrices...");
        new Thread(() -> {
            try {
                MatrixGenerator.generateAll();
                appendToConsole("\n✔ Matrices generadas correctamente.\n", SUCCESS);
            } catch (Exception ex) {
                appendToConsole("\n✘ Error: " + ex.getMessage() + "\n", Color.RED);
            } finally {
                setRunning(false, "Listo");
            }
        }).start();
    }

    private void runSelected() {
        java.util.List<String> selected = new java.util.ArrayList<>();
        for (var entry : checkboxes.entrySet()) {
            if (entry.getValue().isSelected()) selected.add(entry.getKey());
        }
        if (selected.isEmpty()) {
            appendToConsole("⚠ Selecciona al menos un algoritmo.\n", WARNING);
            return;
        }
        runAlgorithms(selected);
    }

    private void runAll() {
        runAlgorithms(new java.util.ArrayList<>(ALGORITHMS.keySet()));
    }

    private void runAlgorithms(java.util.List<String> names) {
        setRunning(true, "Ejecutando...");
        new Thread(() -> {
            for (String name : names) {
                appendToConsole("\n══════════════════════════════════\n", new Color(60, 60, 80));
                appendToConsole("▶ " + name + "\n", ACCENT);
                try {
                    ALGORITHMS.get(name).run();
                    appendToConsole("✔ Completado\n", SUCCESS);
                } catch (Exception ex) {
                    appendToConsole("✘ Error: " + ex.getMessage() + "\n", Color.RED);
                }
            }
            appendToConsole("\n══════════════════════════════════\n", new Color(60, 60, 80));
            appendToConsole("✔ Todos los algoritmos finalizaron.\n", SUCCESS);
            setRunning(false, "Listo");
            // Refrescar automáticamente la pestaña de tiempos
            timingPanel.loadTimings();
        }).start();
    }

    private void clearConsole() {
        try {
            doc.remove(0, doc.getLength());
        } catch (BadLocationException ignored) {}
    }

    // -----------------------------------------------------------------------
    // Redirigir System.out a la consola gráfica
    // -----------------------------------------------------------------------
    private void redirectSystemOut() {
        PrintStream ps = new PrintStream(new OutputStream() {
            private final StringBuilder buf = new StringBuilder();

            @Override
            public void write(int b) {
                buf.append((char) b);
                if (b == '\n') {
                    String line = buf.toString();
                    buf.setLength(0);
                    SwingUtilities.invokeLater(() -> appendToConsole(line, FG));
                }
            }
        }, true);
        System.setOut(ps);
    }

    // -----------------------------------------------------------------------
    // Helpers
    // -----------------------------------------------------------------------
    void appendToConsole(String text, Color color) {
        SwingUtilities.invokeLater(() -> {
            try {
                SimpleAttributeSet attrs = new SimpleAttributeSet();
                StyleConstants.setForeground(attrs, color);
                doc.insertString(doc.getLength(), text, attrs);
                console.setCaretPosition(doc.getLength());
            } catch (BadLocationException ignored) {}
        });
    }

    private void setRunning(boolean running, String status) {
        SwingUtilities.invokeLater(() -> {
            btnRun.setEnabled(!running);
            btnAll.setEnabled(!running);
            btnGenerate.setEnabled(!running);
            progressBar.setIndeterminate(running);
            progressBar.setString(status);
        });
    }

    private JButton makeButton(String text, Color bg) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setBorder(BorderFactory.createEmptyBorder(8, 14, 8, 14));
        return btn;
    }

    private JButton makeSmallButton(String text, Color bg) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }
}
