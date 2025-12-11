package org.example.gui;

import javax.swing.*;
import java.awt.*;

public class BarChartPanel extends JPanel {
    private String title;
    private String[] labels = new String[0];
    private double[] values = new double[0];

    // Public no-arg constructor for IntelliJ GUI Designer
    public BarChartPanel() {
        this("Chart");
    }

    // Keep constructor that accepts a title (make it public so Designer can access it too)
    public BarChartPanel(String title) {
        this.title = title;
        setBackground(new Color(24, 28, 43));
        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(55, 65, 81)),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        setPreferredSize(new Dimension(280, 180));
    }

    public void setData(ChartData data) {
        if (data == null) {
            this.labels = new String[0];
            this.values = new double[0];
        } else {
            this.labels = data.labels;
            this.values = data.values;
        }
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int width = getWidth();
        int height = getHeight();

        int padding = 16;
        int top = padding + 18; // space for title
        int bottom = padding + 24; // space for labels
        int left = padding;
        int right = padding;

        // Title
        g2.setFont(new Font("Segoe UI", Font.BOLD, 13));
        g2.setColor(new Color(235, 239, 255));
        g2.drawString(title, left, padding + 12);

        if (labels == null || labels.length == 0) {
            g2.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            g2.setColor(new Color(107, 114, 128));
            g2.drawString("No data yet", left, height / 2);
            g2.dispose();
            return;
        }

        double max = 0;
        for (double v : values) {
            if (v > max) max = v;
        }
        if (max <= 0) {
            g2.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            g2.setColor(new Color(107, 114, 128));
            g2.drawString("No positive values to display", left, height / 2);
            g2.dispose();
            return;
        }

        int chartHeight = height - top - bottom;
        int chartWidth = width - left - right;
        int n = labels.length;
        int barWidth = Math.max(8, chartWidth / Math.max(n, 1) - 8);

        double scale = (double) chartHeight / max;

        int x = left + 8;

        g2.setFont(new Font("Segoe UI", Font.PLAIN, 10));

        for (int i = 0; i < n; i++) {
            double value = values[i];
            int barHeight = (int) Math.round(value * scale);

            int barX = x + i * (barWidth + 8);
            int barY = top + chartHeight - barHeight;

            g2.setColor(new Color(129, 140, 248));
            g2.fillRoundRect(barX, barY, barWidth, barHeight, 6, 6);

            g2.setColor(new Color(156, 163, 175));
            String valueText = String.format("$%.0f", value);
            int valWidth = g2.getFontMetrics().stringWidth(valueText);
            g2.drawString(valueText, barX + (barWidth - valWidth) / 2, barY - 4);

            String labelText = labels[i];
            int labelWidth = g2.getFontMetrics().stringWidth(labelText);
            int labelX = barX + (barWidth - labelWidth) / 2;
            int labelY = top + chartHeight + 14;
            g2.setColor(new Color(107, 114, 128));
            g2.drawString(labelText, labelX, labelY);
        }

        g2.dispose();
    }
}
