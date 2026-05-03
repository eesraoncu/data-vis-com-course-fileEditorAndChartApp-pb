package org.example;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class PieChartPanel extends JPanel {
    private DefaultTableModel tableModel;
    private JComboBox<String> columnSelector;
    private JPanel drawingPanel;

    public PieChartPanel(DefaultTableModel model) {
        this.tableModel = model;
        setLayout(new BorderLayout());

        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
        controlPanel.setBackground(new Color(245, 247, 250));
        controlPanel.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(210, 215, 220)));

        JLabel colLabel = new JLabel("Column:");
        colLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        controlPanel.add(colLabel);

        columnSelector = new JComboBox<>();
        columnSelector.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        columnSelector.setPreferredSize(new Dimension(160, 30));
        columnSelector.setToolTipText("Select the data column to visualize");
        controlPanel.add(columnSelector);

        JButton refreshBtn = makeCtrlButton("↻  Refresh", new Color(52, 152, 219),
                "Reload column list from current CSV data");
        refreshBtn.addActionListener(e -> updateColumnList());
        controlPanel.add(refreshBtn);

        controlPanel.add(makeSep());

        JButton drawBtn = makeCtrlButton("▶  Render Pie", new Color(155, 89, 182),
                "Render the pie chart for the selected column");
        drawBtn.addActionListener(e -> drawingPanel.repaint());
        controlPanel.add(drawBtn);

        add(controlPanel, BorderLayout.NORTH);

        drawingPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                drawEnhancedMathematicalPieChart((Graphics2D) g);
            }
        };
        drawingPanel.setBackground(new Color(250, 250, 252));
        add(drawingPanel, BorderLayout.CENTER);
    }

    private JButton makeCtrlButton(String text, Color bg, String tooltip) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setOpaque(true);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(btn.getPreferredSize().width + 14, 30));
        btn.setToolTipText(tooltip);
        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseEntered(java.awt.event.MouseEvent e) { btn.setBackground(bg.brighter()); }
            @Override public void mouseExited (java.awt.event.MouseEvent e) { btn.setBackground(bg); }
        });
        return btn;
    }

    private JSeparator makeSep() {
        JSeparator sep = new JSeparator(SwingConstants.VERTICAL);
        sep.setPreferredSize(new Dimension(1, 26));
        sep.setForeground(new Color(200, 210, 220));
        return sep;
    }

    public void updateColumnList() {
        columnSelector.removeAllItems();
        for (int i = 0; i < tableModel.getColumnCount(); i++) {
            columnSelector.addItem(tableModel.getColumnName(i));
        }
    }

    public List<Double> getSelectedNumericData() {
        List<Double> data = new ArrayList<>();
        int colIndex = columnSelector.getSelectedIndex();
        if (colIndex == -1)
            return data;
        for (int i = 0; i < tableModel.getRowCount(); i++) {
            try {
                Object value = tableModel.getValueAt(i, colIndex);
                if (value != null && !value.toString().trim().isEmpty()) {
                    data.add(Math.abs(Double.parseDouble(value.toString().trim())));
                }
            } catch (Exception e) {
            }
        }
        return data;
    }

    private void drawEnhancedMathematicalPieChart(Graphics2D g2d) {
        List<Double> data = getSelectedNumericData();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        String selectedColumn = (String) columnSelector.getSelectedItem();

        if (data.isEmpty()) {
            g2d.setFont(new Font("Arial", Font.ITALIC, 14));
            g2d.setColor(Color.GRAY);
            g2d.drawString("Please load CSV and render the pie chart.", 50, 50);
            return;
        }

        double totalSum = 0;
        for (Double value : data) totalSum += value;
        if (totalSum == 0) return;

        int width  = drawingPanel.getWidth();
        int height = drawingPanel.getHeight();

        // --- Chart title ---
        String title = (selectedColumn != null ? selectedColumn : "Data") + " – Distribution (Pie Chart)";
        g2d.setFont(new Font("Arial", Font.BOLD, 14));
        g2d.setColor(new Color(50, 50, 60));
        FontMetrics fmT = g2d.getFontMetrics();
        g2d.drawString(title, (width - fmT.stringWidth(title)) / 2, 24);

        // Legend area on the right (140 px wide)
        int legendWidth = 140;
        int cx = (width - legendWidth) / 2;
        int cy = height / 2 + 10;
        int radius = (Math.min(width - legendWidth, height - 60) / 2) - 30;
        if (radius < 20) radius = 20;

        Color[] sliceColors = {
                new Color(231, 76, 60), new Color(52, 152, 219), new Color(241, 196, 15),
                new Color(46, 204, 113), new Color(155, 89, 182), new Color(230, 126, 34), new Color(26, 188, 156)
        };

        // Shadow
        g2d.setColor(new Color(0, 0, 0, 30));
        g2d.fillOval(cx - radius + 8, cy - radius + 8, radius * 2, radius * 2);

        double currentAngleDegrees = 0;

        for (int i = 0; i < data.size(); i++) {
            double value   = data.get(i);
            double percent = value / totalSum;
            double sweepAngleDegrees = percent * 360.0;
            double startAngle = currentAngleDegrees;
            double endAngle   = currentAngleDegrees + sweepAngleDegrees;

            Color segmentColor = sliceColors[i % sliceColors.length];
            g2d.setColor(segmentColor);
            fillPieSlice(g2d, cx, cy, radius, startAngle, endAngle);

            // Percentage label inside slice
            double middleAngle  = startAngle + (sweepAngleDegrees / 2.0);
            double middleRadian = Math.toRadians(middleAngle);
            int textX = (int) (cx + (radius * 0.65) * Math.cos(middleRadian));
            int textY = (int) (cy - (radius * 0.65) * Math.sin(middleRadian));

            if (sweepAngleDegrees > 5) { // only draw if slice is big enough
                String label = String.format("%.1f%%", percent * 100);
                g2d.setFont(new Font("Arial", Font.BOLD, 12));
                FontMetrics fm = g2d.getFontMetrics();
                int lx = textX - fm.stringWidth(label) / 2;
                int ly = textY + fm.getAscent() / 2;
                g2d.setColor(new Color(0, 0, 0, 80));
                g2d.drawString(label, lx + 1, ly + 1);
                g2d.setColor(Color.WHITE);
                g2d.drawString(label, lx, ly);
            }

            currentAngleDegrees += sweepAngleDegrees;
        }

        // --- Legend ---
        int legendX = cx + radius + 20;
        int legendStartY = cy - (data.size() * 22) / 2;
        g2d.setFont(new Font("Arial", Font.PLAIN, 11));

        // Column name label above legend
        g2d.setFont(new Font("Arial", Font.BOLD, 11));
        g2d.setColor(new Color(50, 50, 60));
        String colLabel = selectedColumn != null ? selectedColumn : "Column";
        g2d.drawString(colLabel, legendX, legendStartY - 10);

        g2d.setFont(new Font("Arial", Font.PLAIN, 11));
        for (int i = 0; i < data.size(); i++) {
            int ly = legendStartY + i * 22;
            g2d.setColor(sliceColors[i % sliceColors.length]);
            g2d.fillRoundRect(legendX, ly, 14, 14, 4, 4);
            g2d.setColor(new Color(60, 60, 70));
            String entry = "Row " + (i + 1) + ": " + String.format("%.1f", data.get(i));
            g2d.drawString(entry, legendX + 20, ly + 12);
        }

        // --- Column name as X-axis equivalent label below the chart ---
        g2d.setFont(new Font("Arial", Font.BOLD, 12));
        g2d.setColor(new Color(80, 80, 90));
        String xLabel = "Category: " + (selectedColumn != null ? selectedColumn : "Selected Column");
        FontMetrics fmX = g2d.getFontMetrics();
        g2d.drawString(xLabel, (width - fmX.stringWidth(xLabel)) / 2, cy + radius + 30);
    }

    private void fillPieSlice(Graphics2D g2d, int cx, int cy, int radius, double startAngle, double endAngle) {
        Polygon slicePolygon = new Polygon();
        slicePolygon.addPoint(cx, cy);

        for (double angle = startAngle; angle <= endAngle; angle += 0.5) {
            double radian = Math.toRadians(angle);
            int arcX = (int) (cx + radius * Math.cos(radian));
            int arcY = (int) (cy - radius * Math.sin(radian));
            slicePolygon.addPoint(arcX, arcY);
        }

        double endRadian = Math.toRadians(endAngle);
        slicePolygon.addPoint((int) (cx + radius * Math.cos(endRadian)), (int) (cy - radius * Math.sin(endRadian)));

        g2d.fillPolygon(slicePolygon);

        g2d.setColor(Color.WHITE);
        g2d.setStroke(new BasicStroke(2.5f));
        g2d.drawPolygon(slicePolygon);
    }
}
