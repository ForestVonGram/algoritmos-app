package org.algoritmos.gui;

import javax.swing.*;
import javax.swing.plaf.basic.BasicTabbedPaneUI;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * MainWindow - Ventana principal con pestañas completamente personalizadas.
 * Tema oscuro consistente en toda la interfaz.
 */
public class MainWindow extends JFrame {

    // Paleta global
    static final Color C_BG_DARK   = new Color(18, 18, 28);   // fondo más oscuro
    static final Color C_BG        = new Color(26, 26, 38);   // fondo principal
    static final Color C_BG_PANEL  = new Color(34, 34, 50);   // paneles
    static final Color C_BG_CARD   = new Color(42, 42, 60);   // tarjetas/controles
    static final Color C_ACCENT    = new Color(82, 170, 255);  // azul acento
    static final Color C_ACCENT2   = new Color(120, 220, 160); // verde acento
    static final Color C_TEXT      = new Color(210, 215, 230); // texto principal
    static final Color C_TEXT_DIM  = new Color(130, 135, 155); // texto secundario
    static final Color C_BORDER    = new Color(55, 55, 75);    // bordes
    static final Color C_TAB_SEL   = new Color(42, 42, 62);   // pestaña seleccionada
    static final Color C_TAB_HOVER = new Color(36, 36, 54);   // pestaña hover

    private TimingPanel    timingPanel;
    private AlgorithmPanel algorithmPanel;

    public MainWindow() {
        setTitle("Multiplicación de Matrices — Universidad del Quindío");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1200, 820);
        setMinimumSize(new Dimension(1000, 680));
        setLocationRelativeTo(null);

        // Forzar colores del sistema para que no interfieran
        applyGlobalUIDefaults();
        buildUI();
    }

    // -----------------------------------------------------------------------
    // Forzar defaults de UI para que Swing no pinte blanco encima
    // -----------------------------------------------------------------------
    private void applyGlobalUIDefaults() {
        UIManager.put("TabbedPane.background",         C_BG_DARK);
        UIManager.put("TabbedPane.foreground",         C_TEXT);
        UIManager.put("TabbedPane.selected",           C_TAB_SEL);
        UIManager.put("TabbedPane.contentAreaColor",   C_BG);
        UIManager.put("TabbedPane.tabAreaBackground",  C_BG_DARK);
        UIManager.put("TabbedPane.unselectedBackground", C_BG_DARK);
        UIManager.put("TabbedPane.shadow",             C_BG_DARK);
        UIManager.put("TabbedPane.darkShadow",         C_BG_DARK);
        UIManager.put("TabbedPane.light",              C_BG_DARK);
        UIManager.put("TabbedPane.highlight",          C_BG_DARK);
        UIManager.put("TabbedPane.focus",              C_BG_DARK);
        UIManager.put("TabbedPane.borderHightlightColor", C_BORDER);
        UIManager.put("Panel.background",              C_BG);
        UIManager.put("ScrollPane.background",         C_BG);
        UIManager.put("Viewport.background",           C_BG);
        UIManager.put("SplitPane.background",          C_BG);
        UIManager.put("SplitPane.dividerSize",         6);
        UIManager.put("ComboBox.background",           C_BG_CARD);
        UIManager.put("ComboBox.foreground",           C_TEXT);
        UIManager.put("ComboBox.selectionBackground",  C_ACCENT);
        UIManager.put("ComboBox.selectionForeground",  Color.WHITE);
        UIManager.put("CheckBox.background",           C_BG_PANEL);
        UIManager.put("CheckBox.foreground",           C_TEXT);
        UIManager.put("ProgressBar.background",        C_BG_CARD);
        UIManager.put("ProgressBar.foreground",        C_ACCENT);
        UIManager.put("ProgressBar.selectionBackground", C_TEXT);
        UIManager.put("ProgressBar.selectionForeground", C_BG_DARK);
        UIManager.put("ScrollBar.background",          C_BG_PANEL);
        UIManager.put("ScrollBar.thumb",               C_BG_CARD);
        UIManager.put("ScrollBar.track",               C_BG_PANEL);
        UIManager.put("Table.background",              C_BG_DARK);
        UIManager.put("Table.foreground",              C_TEXT);
        UIManager.put("Table.gridColor",               C_BORDER);
        UIManager.put("TableHeader.background",        new Color(40, 60, 100));
        UIManager.put("TableHeader.foreground",        Color.WHITE);
        UIManager.put("Label.foreground",              C_TEXT);
        UIManager.put("Button.background",             C_BG_CARD);
        UIManager.put("Button.foreground",             C_TEXT);
    }

    // -----------------------------------------------------------------------
    // Construcción de la UI
    // -----------------------------------------------------------------------
    private void buildUI() {
        JPanel root = new JPanel(new BorderLayout(0, 0));
        root.setBackground(C_BG_DARK);

        root.add(buildHeader(), BorderLayout.NORTH);
        root.add(buildTabs(),   BorderLayout.CENTER);

        setContentPane(root);
    }

    // -----------------------------------------------------------------------
    // Header con gradiente
    // -----------------------------------------------------------------------
    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                // Gradiente horizontal
                GradientPaint gp = new GradientPaint(
                        0, 0, new Color(15, 20, 45),
                        getWidth(), 0, new Color(25, 30, 55));
                g2.setPaint(gp);
                g2.fillRect(0, 0, getWidth(), getHeight());
                // Línea inferior decorativa
                g2.setColor(C_ACCENT);
                g2.fillRect(0, getHeight() - 2, getWidth(), 2);
            }
        };
        header.setOpaque(false);
        header.setBorder(BorderFactory.createEmptyBorder(14, 24, 14, 24));

        // Lado izquierdo: título + subtítulo
        JPanel left = new JPanel();
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
        left.setOpaque(false);

        JLabel title = new JLabel("Multiplicación de Matrices Grandes");
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        title.setForeground(C_ACCENT);

        JLabel subtitle = new JLabel("Análisis de algoritmos iterativos y divide y vencerás  ·  Seguimiento 2");
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        subtitle.setForeground(C_TEXT_DIM);

        left.add(title);
        left.add(Box.createVerticalStrut(3));
        left.add(subtitle);

        // Lado derecho: badge universidad
        JPanel right = new JPanel();
        right.setLayout(new BoxLayout(right, BoxLayout.Y_AXIS));
        right.setOpaque(false);

        JLabel uq1 = new JLabel("Universidad del Quindío");
        uq1.setFont(new Font("Segoe UI", Font.BOLD, 12));
        uq1.setForeground(C_TEXT);
        uq1.setAlignmentX(Component.RIGHT_ALIGNMENT);

        JLabel uq2 = new JLabel("Ingeniería de Sistemas y Computación");
        uq2.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        uq2.setForeground(C_TEXT_DIM);
        uq2.setAlignmentX(Component.RIGHT_ALIGNMENT);

        right.add(uq1);
        right.add(Box.createVerticalStrut(2));
        right.add(uq2);

        header.add(left,  BorderLayout.WEST);
        header.add(right, BorderLayout.EAST);

        return header;
    }

    // -----------------------------------------------------------------------
    // Pestañas con UI completamente personalizada
    // -----------------------------------------------------------------------
    private JTabbedPane buildTabs() {
        timingPanel    = new TimingPanel();
        algorithmPanel = new AlgorithmPanel(timingPanel);

        JTabbedPane tabs = new JTabbedPane() {
            @Override
            public void updateUI() {
                setUI(new DarkTabbedPaneUI());
            }
        };
        tabs.setBackground(C_BG_DARK);
        tabs.setForeground(C_TEXT);
        tabs.setFont(new Font("Segoe UI", Font.BOLD, 13));
        tabs.setTabPlacement(JTabbedPane.TOP);

        tabs.addTab("  Ejecutar  ",      algorithmPanel);
        tabs.addTab("  Matrices  ",      new MatrixViewPanel());
        tabs.addTab("  Tiempos  ",       timingPanel);
        tabs.addTab("  Complejidades  ", new ComplexityPanel());

        return tabs;
    }

    // -----------------------------------------------------------------------
    // UI personalizada para las pestañas (resuelve el problema blanco/blanco)
    // -----------------------------------------------------------------------
    static class DarkTabbedPaneUI extends BasicTabbedPaneUI {

        private int hoveredTab = -1;

        @Override
        protected void installListeners() {
            super.installListeners();
            tabPane.addMouseMotionListener(new MouseAdapter() {
                @Override
                public void mouseMoved(MouseEvent e) {
                    int tab = tabForCoordinate(tabPane, e.getX(), e.getY());
                    if (tab != hoveredTab) {
                        hoveredTab = tab;
                        tabPane.repaint();
                    }
                }
            });
            tabPane.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseExited(MouseEvent e) {
                    hoveredTab = -1;
                    tabPane.repaint();
                }
            });
        }

        @Override
        protected void paintTabArea(Graphics g, int tabPlacement, int selectedIndex) {
            // Fondo de la barra de pestañas
            g.setColor(C_BG_DARK);
            g.fillRect(0, 0, tabPane.getWidth(), maxTabHeight + 6);
            // Línea separadora inferior
            g.setColor(C_BORDER);
            g.fillRect(0, maxTabHeight + 4, tabPane.getWidth(), 1);
            super.paintTabArea(g, tabPlacement, selectedIndex);
        }

        @Override
        protected void paintTabBackground(Graphics g, int tabPlacement,
                int tabIndex, int x, int y, int w, int h, boolean isSelected) {
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            if (isSelected) {
                // Pestaña seleccionada: fondo con acento superior
                g2.setColor(C_TAB_SEL);
                g2.fillRoundRect(x + 2, y + 2, w - 4, h, 8, 8);
                // Línea de acento superior
                g2.setColor(C_ACCENT);
                g2.fillRoundRect(x + 4, y + 2, w - 8, 3, 3, 3);
            } else if (tabIndex == hoveredTab) {
                g2.setColor(C_TAB_HOVER);
                g2.fillRoundRect(x + 2, y + 4, w - 4, h - 2, 8, 8);
            } else {
                g2.setColor(C_BG_DARK);
                g2.fillRect(x, y, w, h);
            }
        }

        @Override
        protected void paintTabBorder(Graphics g, int tabPlacement,
                int tabIndex, int x, int y, int w, int h, boolean isSelected) {
            // Sin borde por defecto — lo manejamos en paintTabBackground
        }

        @Override
        protected void paintText(Graphics g, int tabPlacement, Font font,
                FontMetrics metrics, int tabIndex, String title,
                Rectangle textRect, boolean isSelected) {
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                    RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g2.setFont(font);
            g2.setColor(isSelected ? C_ACCENT : C_TEXT_DIM);
            g2.drawString(title, textRect.x, textRect.y + metrics.getAscent());
        }

        @Override
        protected void paintFocusIndicator(Graphics g, int tabPlacement,
                Rectangle[] rects, int tabIndex, Rectangle iconRect,
                Rectangle textRect, boolean isSelected) {
            // Sin indicador de foco
        }

        @Override
        protected void paintContentBorder(Graphics g, int tabPlacement, int selectedIndex) {
            // Sin borde de contenido
        }

        @Override
        protected int getTabRunOverlay(int tabPlacement) { return 0; }

        @Override
        protected Insets getTabInsets(int tabPlacement, int tabIndex) {
            return new Insets(10, 16, 10, 16);
        }

        @Override
        protected Insets getContentBorderInsets(int tabPlacement) {
            return new Insets(1, 0, 0, 0);
        }
    }
}
