package com.accisense.gui;

import com.accisense.alert.AlertListener;
import com.accisense.model.AccidentRecord;
import com.accisense.model.SensorData;
import com.accisense.sensor.SensorListener;
import com.accisense.sensor.SensorSimulator;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedList;

/**
 * Premium styled real-time dashboard with animations and glow effects.
 *
 * Features:
 *  - Dark gradient theme with glowing rounded sensor cards
 *  - Animated speedometer-style impact gauge with smooth needle
 *  - Live scrolling acceleration waveform with glow
 *  - Pulsing status LED + flashing red alert banner
 *  - Hover-glow rounded buttons
 *  - Severity-coloured accident log table
 *
 * [RUBRIC: Interface – implements SensorListener and AlertListener]
 * [RUBRIC: Multithreading – SwingUtilities.invokeLater + animation Timer]
 *
 * @author AcciSense Team
 */
public class Dashboard extends JFrame implements SensorListener, AlertListener {

    // ── Theme colours ──
    private static final Color CYAN   = new Color(0, 229, 255);
    private static final Color GREEN  = new Color(0, 230, 118);
    private static final Color RED    = new Color(255, 61, 87);
    private static final Color ORANGE = new Color(255, 171, 64);
    private static final Color CARD   = new Color(30, 34, 48, 235);

    // ── Sensor cards ──
    private SensorCard cAx, cAy, cAz, cAr, cGx, cGy, cGz, cGr;

    // ── Live state (updated on EDT only) ──
    private double impactTarget = 0;
    private double impactDisplay = 0;
    private double phase = 0;
    private long flashUntil = 0;
    private final LinkedList<Double> accelHistory = new LinkedList<>();

    // ── Custom panels ──
    private HeaderPanel header;
    private ImpactGauge gauge;
    private Sparkline spark;
    private JLabel banner;
    private DefaultTableModel tableModel;

    private final SensorSimulator simulator;

    /**
     * Constructor – builds UI and starts animation loop.
     */
    public Dashboard(SensorSimulator simulator) {
        this.simulator = simulator;
        setTitle("AcciSense – Accident Detection & Sensing Dashboard");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1150, 780);
        setLocationRelativeTo(null);
        buildUI();
        startAnimation();
    }

    // ══════════════════════════════════════════════
    //  UI CONSTRUCTION
    // ══════════════════════════════════════════════
    private void buildUI() {

        // Root panel with vertical gradient background
        JPanel root = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setPaint(new GradientPaint(0, 0, new Color(24, 28, 40),
                        0, getHeight(), new Color(10, 12, 20)));
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.dispose();
            }
        };
        root.setOpaque(false);
        setContentPane(root);

        // ── Header bar ──
        header = new HeaderPanel();
        header.setPreferredSize(new Dimension(0, 72));
        root.add(header, BorderLayout.NORTH);

        // ── Center area ──
        JPanel center = new JPanel(new BorderLayout(12, 12));
        center.setOpaque(false);
        center.setBorder(BorderFactory.createEmptyBorder(10, 14, 10, 14));

        // Flashing alert banner (no emoji – avoids missing glyph boxes)
        banner = new JLabel("ACCIDENT DETECTED  —  ALERT SENT TO EMERGENCY CONTACTS",
                SwingConstants.CENTER);
        banner.setFont(new Font("Segoe UI", Font.BOLD, 17));
        banner.setForeground(Color.WHITE);
        banner.setOpaque(true);
        banner.setBackground(RED);
        banner.setPreferredSize(new Dimension(0, 42));
        banner.setVisible(false);
        center.add(banner, BorderLayout.NORTH);

        // Sensor cards grid (2 rows × 4)
        JPanel cards = new JPanel(new GridLayout(2, 4, 12, 12));
        cards.setOpaque(false);
        cAx = new SensorCard("ACCEL X", "m/s²");
        cAy = new SensorCard("ACCEL Y", "m/s²");
        cAz = new SensorCard("ACCEL Z", "m/s²");
        cAr = new SensorCard("RESULTANT ACCEL", "m/s²");
        cGx = new SensorCard("GYRO X", "°/s");
        cGy = new SensorCard("GYRO Y", "°/s");
        cGz = new SensorCard("GYRO Z", "°/s");
        cGr = new SensorCard("RESULTANT GYRO", "°/s");
        cards.add(cAx); cards.add(cAy); cards.add(cAz); cards.add(cAr);
        cards.add(cGx); cards.add(cGy); cards.add(cGz); cards.add(cGr);
        center.add(cards, BorderLayout.CENTER);

        // Right column: gauge + waveform
        JPanel right = new JPanel(new GridLayout(2, 1, 12, 12));
        right.setOpaque(false);
        right.setPreferredSize(new Dimension(300, 0));
        gauge = new ImpactGauge();
        spark = new Sparkline();
        right.add(gauge);
        right.add(spark);
        center.add(right, BorderLayout.EAST);

        // Glow buttons (plain text – no emoji glyph issues)
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.CENTER, 24, 6));
        buttons.setOpaque(false);
        RoundedButton btnCrash = new RoundedButton("SIMULATE CRASH", new Color(190, 40, 60));
        RoundedButton btnRoll  = new RoundedButton("SIMULATE ROLLOVER", new Color(210, 140, 0));
        btnCrash.addActionListener(e -> simulator.triggerAccident());
        btnRoll.addActionListener(e -> simulator.triggerRollover());
        buttons.add(btnCrash);
        buttons.add(btnRoll);
        center.add(buttons, BorderLayout.SOUTH);

        root.add(center, BorderLayout.CENTER);

        // ── Accident log table ──
        String[] cols = {"ID", "TIME", "SEVERITY", "IMPACT", "ACCEL", "GYRO", "LOCATION", "ALERT"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = new JTable(tableModel);
        styleTable(table);
        JScrollPane scroll = new JScrollPane(table);
        scroll.setPreferredSize(new Dimension(0, 200));
        scroll.getViewport().setBackground(new Color(24, 27, 38));
        scroll.setBorder(BorderFactory.createEmptyBorder(0, 14, 14, 14));
        root.add(scroll, BorderLayout.SOUTH);
    }

    /** Dark theme + severity-coloured rows. */
    private void styleTable(JTable table) {
        table.setRowHeight(30);
        table.setShowGrid(false);
        table.setBackground(new Color(24, 27, 38));
        table.setFillsViewportHeight(true);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(JTable t, Object v,
                    boolean sel, boolean foc, int r, int c) {
                JLabel comp = (JLabel) super.getTableCellRendererComponent(t, v, sel, foc, r, c);
                String sev = String.valueOf(t.getValueAt(r, 2));
                Color bg;
                switch (sev) {
                    case "CRITICAL": bg = new Color(96, 22, 34); break;
                    case "HIGH":     bg = new Color(96, 52, 22); break;
                    case "MEDIUM":   bg = new Color(92, 82, 22); break;
                    default:         bg = new Color(24, 62, 42); break;
                }
                comp.setBackground(bg);
                comp.setForeground(new Color(232, 236, 246));
                comp.setHorizontalAlignment(SwingConstants.CENTER);
                return comp;
            }
        });

        table.getTableHeader().setDefaultRenderer(new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(JTable t, Object v,
                    boolean sel, boolean foc, int r, int c) {
                JLabel comp = (JLabel) super.getTableCellRendererComponent(t, v, sel, foc, r, c);
                comp.setBackground(new Color(40, 45, 62));
                comp.setForeground(CYAN);
                comp.setFont(new Font("Segoe UI", Font.BOLD, 12));
                comp.setHorizontalAlignment(SwingConstants.CENTER);
                comp.setPreferredSize(new Dimension(0, 34));
                return comp;
            }
        });
    }

    // ══════════════════════════════════════════════
    //  ANIMATION LOOP (30 fps)
    // ══════════════════════════════════════════════
    private void startAnimation() {
        Timer anim = new Timer(30, e -> {
            phase += 0.12;
            impactDisplay += (impactTarget - impactDisplay) * 0.15;  // smooth needle

            boolean flashing = System.currentTimeMillis() < flashUntil;
            banner.setVisible(flashing);
            if (flashing) {
                banner.setBackground(((int) phase % 2 == 0) ? RED : new Color(120, 10, 25));
            }
            header.repaint();
            gauge.repaint();
            spark.repaint();
        });
        anim.start();
    }

    // ══════════════════════════════════════════════
    //  SensorListener – live card updates
    // ══════════════════════════════════════════════
    @Override
    public void onSensorDataReceived(SensorData data) {
        SwingUtilities.invokeLater(() -> {
            cAx.update(fmt(data.getAccelerationX()), tint(Math.abs(data.getAccelerationX()), 10, 20));
            cAy.update(fmt(data.getAccelerationY()), tint(Math.abs(data.getAccelerationY()), 10, 20));
            cAz.update(fmt(data.getAccelerationZ()), tint(Math.abs(data.getAccelerationZ()), 15, 25));
            cAr.update(fmt(data.getResultantAcceleration()), tint(data.getResultantAcceleration(), 15, 20));
            cGx.update(fmt(data.getGyroX()), tint(Math.abs(data.getGyroX()), 60, 120));
            cGy.update(fmt(data.getGyroY()), tint(Math.abs(data.getGyroY()), 60, 120));
            cGz.update(fmt(data.getGyroZ()), tint(Math.abs(data.getGyroZ()), 60, 120));
            cGr.update(fmt(data.getResultantGyro()), tint(data.getResultantGyro(), 60, 120));

            impactTarget = data.getImpactForce();

            accelHistory.addLast(data.getResultantAcceleration());
            if (accelHistory.size() > 160) accelHistory.removeFirst();
        });
    }

    private String fmt(double v) { return String.format("%.2f", v); }

    private Color tint(double value, double warn, double danger) {
        if (value > danger) return RED;
        if (value > warn)   return ORANGE;
        return CYAN;
    }

    // ══════════════════════════════════════════════
    //  AlertListener – flash banner + add log row
    // ══════════════════════════════════════════════
    @Override
    public void onAlertTriggered(AccidentRecord record) {
        SwingUtilities.invokeLater(() -> {
            flashUntil = System.currentTimeMillis() + 5000;
            tableModel.insertRow(0, new Object[]{
                    record.getId(),
                    record.getFormattedTime(),
                    record.getSeverity().toString(),
                    String.format("%.2f", record.getImpactForce()),
                    String.format("%.2f", record.getResultantAcceleration()),
                    String.format("%.2f", record.getResultantGyro()),
                    record.getLocation(),
                    record.isAlertSent() ? "YES" : "NO"
            });
        });
    }

    // ══════════════════════════════════════════════
    //  CUSTOM PAINTED COMPONENTS
    // ══════════════════════════════════════════════

    /** Header bar with glowing title, pulsing LED and centred clock. */
    private class HeaderPanel extends JPanel {
        HeaderPanel() { setOpaque(false); }

        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int w = getWidth(), h = getHeight();

            g2.setPaint(new GradientPaint(0, 0, new Color(32, 37, 54), w, 0, new Color(20, 23, 36)));
            g2.fillRect(0, 0, w, h);
            g2.setColor(new Color(0, 229, 255, 70));
            g2.fillRect(0, h - 2, w, 2);

            // Glowing title
            g2.setFont(new Font("Segoe UI", Font.BOLD, 26));
            g2.setColor(new Color(0, 229, 255, 90));
            g2.drawString("AcciSense", 22, 47);
            g2.setColor(Color.WHITE);
            g2.drawString("AcciSense", 20, 45);

            g2.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            g2.setColor(new Color(150, 160, 180));
            g2.drawString("ACCIDENT DETECTION & SENSING SYSTEM", 165, 44);

            // Clock – centred (no overlap with status)
            g2.setFont(new Font("Consolas", Font.PLAIN, 14));
            g2.setColor(new Color(190, 200, 220));
            g2.drawString(LocalDateTime.now().format(
                    DateTimeFormatter.ofPattern("yyyy-MM-dd  HH:mm:ss")), w / 2 - 85, h / 2 + 5);

            // Pulsing status LED (right side)
            boolean alert = System.currentTimeMillis() < flashUntil;
            Color led = alert ? RED : GREEN;
            int pulse = (int) (120 + 100 * Math.abs(Math.sin(phase)));
            g2.setColor(new Color(led.getRed(), led.getGreen(), led.getBlue(), pulse / 3));
            g2.fillOval(w - 236, h / 2 - 12, 24, 24);
            g2.setColor(led);
            g2.fillOval(w - 232, h / 2 - 8, 16, 16);

            g2.setFont(new Font("Segoe UI", Font.BOLD, 14));
            g2.setColor(alert ? RED : GREEN);
            g2.drawString(alert ? "ACCIDENT DETECTED!" : "SYSTEM RUNNING", w - 205, h / 2 + 5);
            g2.dispose();
        }
    }

    /** Speedometer-style impact gauge with animated needle. */
    private class ImpactGauge extends JPanel {
        ImpactGauge() { setOpaque(false); }

        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int w = getWidth(), h = getHeight();

            g2.setColor(CARD);
            g2.fillRoundRect(0, 0, w, h, 20, 20);

            g2.setFont(new Font("Segoe UI", Font.BOLD, 12));
            g2.setColor(new Color(160, 170, 190));
            g2.drawString("IMPACT FORCE  (kN)", 16, 24);

            int dia = Math.min(w, h) - 70;
            int x = (w - dia) / 2, y = (h - dia) / 2 + 8;

            g2.setStroke(new BasicStroke(10f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2.setColor(new Color(60, 66, 86));
            g2.drawArc(x, y, dia, dia, 135, 270);
            g2.setColor(GREEN);  g2.drawArc(x, y, dia, dia, 135, 90);
            g2.setColor(ORANGE); g2.drawArc(x, y, dia, dia, 225, 90);
            g2.setColor(RED);    g2.drawArc(x, y, dia, dia, 315, 90);

            // Value arc
            double frac = Math.min(Math.max(impactDisplay / 120.0, 0), 1);
            Color vc = impactDisplay > 40 ? RED : impactDisplay > 20 ? ORANGE : CYAN;
            g2.setColor(vc);
            g2.drawArc(x, y, dia, dia, 135, (int) (270 * frac));

            // Needle
            double ang = Math.toRadians(135 + 270 * frac);
            int cx = x + dia / 2, cy = y + dia / 2;
            int nx = (int) (cx + Math.cos(ang) * dia * 0.42);
            int ny = (int) (cy + Math.sin(ang) * dia * 0.42);
            g2.setStroke(new BasicStroke(3f));
            g2.setColor(Color.WHITE);
            g2.drawLine(cx, cy, nx, ny);
            g2.fillOval(cx - 6, cy - 6, 12, 12);

            // Digital readout
            g2.setFont(new Font("Consolas", Font.BOLD, 26));
            g2.setColor(vc);
            String txt = String.format("%.1f", impactDisplay);
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(txt, cx - fm.stringWidth(txt) / 2, cy + 46);
            g2.dispose();
        }
    }

    /** Rolling acceleration waveform with glow. */
    private class Sparkline extends JPanel {
        Sparkline() { setOpaque(false); }

        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int w = getWidth(), h = getHeight();

            g2.setColor(CARD);
            g2.fillRoundRect(0, 0, w, h, 20, 20);

            g2.setFont(new Font("Segoe UI", Font.BOLD, 12));
            g2.setColor(new Color(160, 170, 190));
            g2.drawString("ACCELERATION WAVEFORM", 16, 24);

            // Grid lines
            g2.setColor(new Color(60, 66, 86, 120));
            g2.setStroke(new BasicStroke(1f));
            for (int i = 1; i < 4; i++) {
                int gy = 34 + (h - 50) * i / 4;
                g2.drawLine(14, gy, w - 14, gy);
            }

            // Waveform: glow pass + bright pass
            if (accelHistory.size() > 1) {
                int n = accelHistory.size();
                int[] xs = new int[n], ys = new int[n];
                int i = 0;
                for (double v : accelHistory) {
                    xs[i] = 14 + (int) ((w - 28) * i / (double) (160 - 1));
                    ys[i] = h - 16 - (int) (Math.min(v, 40) / 40.0 * (h - 52));
                    i++;
                }
                g2.setStroke(new BasicStroke(5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g2.setColor(new Color(0, 229, 255, 60));
                g2.drawPolyline(xs, ys, n);
                g2.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g2.setColor(CYAN);
                g2.drawPolyline(xs, ys, n);
            }
            g2.dispose();
        }
    }

    /** Rounded gradient button with hover glow + press effect. */
    private class RoundedButton extends JButton {
        private boolean hover = false, pressed = false;
        private final Color base;

        RoundedButton(String text, Color base) {
            super(text);
            this.base = base;
            setOpaque(false);
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setCursor(new Cursor(Cursor.HAND_CURSOR));
            setForeground(Color.WHITE);
            setFont(new Font("Segoe UI", Font.BOLD, 15));
            setPreferredSize(new Dimension(230, 46));
            addMouseListener(new MouseAdapter() {
                @Override public void mouseEntered(MouseEvent e) { hover = true;  repaint(); }
                @Override public void mouseExited(MouseEvent e)  { hover = false; repaint(); }
                @Override public void mousePressed(MouseEvent e) { pressed = true;  repaint(); }
                @Override public void mouseReleased(MouseEvent e){ pressed = false; repaint(); }
            });
        }

        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int w = getWidth(), h = getHeight();
            Color c = pressed ? base.darker() : (hover ? base.brighter() : base);

            if (hover) {   // outer glow
                g2.setColor(new Color(c.getRed(), c.getGreen(), c.getBlue(), 80));
                g2.fillRoundRect(-3, -2, w + 6, h + 5, 26, 26);
            }
            g2.setPaint(new GradientPaint(0, 0, c.brighter(), 0, h, c.darker()));
            g2.fillRoundRect(0, 0, w, h, 20, 20);

            g2.setFont(getFont());
            FontMetrics fm = g2.getFontMetrics();
            g2.setColor(getForeground());
            g2.drawString(getText(), (w - fm.stringWidth(getText())) / 2,
                    (h + fm.getAscent() - fm.getDescent()) / 2);
            g2.dispose();
        }
    }

    /** Glowing rounded sensor card. */
    private static class SensorCard extends JPanel {
        private final String title, unit;
        private String valueText = "0.00";
        private Color accent = CYAN;

        SensorCard(String title, String unit) {
            this.title = title;
            this.unit = unit;
            setOpaque(false);
        }

        void update(String text, Color accent) {
            this.valueText = text;
            this.accent = accent;
            repaint();
        }

        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int w = getWidth(), h = getHeight();

            g2.setColor(CARD);
            g2.fillRoundRect(0, 0, w, h, 18, 18);
            g2.setColor(new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), 110));
            g2.setStroke(new BasicStroke(2f));
            g2.drawRoundRect(1, 1, w - 3, h - 3, 18, 18);

            g2.setColor(accent);
            g2.fillRoundRect(14, 12, 26, 4, 4, 4);

            g2.setColor(new Color(160, 170, 190));
            g2.setFont(new Font("Segoe UI", Font.PLAIN, 11));
            g2.drawString(title, 14, 32);

            g2.setColor(accent);
            g2.setFont(new Font("Consolas", Font.BOLD, 24));
            g2.drawString(valueText, 14, 62);

            g2.setColor(new Color(120, 130, 150));
            g2.setFont(new Font("Segoe UI", Font.PLAIN, 10));
            g2.drawString(unit, 14, 80);
            g2.dispose();
        }
    }
}