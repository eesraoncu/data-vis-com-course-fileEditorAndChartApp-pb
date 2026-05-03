package org.example;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class LineBarChartPanel extends JPanel {
    private DefaultTableModel tableModel;
    private JComboBox<String> columnSelector;
    private JRadioButton lineChartRadio;
    private JRadioButton barChartRadio;
    private JPanel drawingPanel;

    public LineBarChartPanel(DefaultTableModel model) {
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

        lineChartRadio = new JRadioButton("Line / Area", true);
        barChartRadio  = new JRadioButton("Bar Chart");
        lineChartRadio.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        barChartRadio .setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lineChartRadio.setOpaque(false);
        barChartRadio .setOpaque(false);
        lineChartRadio.setToolTipText("Draw a line/area chart");
        barChartRadio .setToolTipText("Draw a 3-D style bar chart");

        ButtonGroup chartGroup = new ButtonGroup();
        chartGroup.add(lineChartRadio);
        chartGroup.add(barChartRadio);
        controlPanel.add(lineChartRadio);
        controlPanel.add(barChartRadio);

        controlPanel.add(makeSep());

        JButton drawBtn = makeCtrlButton("▶  Draw Chart", new Color(39, 174, 96),
                "Render the selected chart type");
        drawBtn.addActionListener(e -> drawingPanel.repaint());
        controlPanel.add(drawBtn);

        add(controlPanel, BorderLayout.NORTH);

        drawingPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                drawEnhancedChart((Graphics2D) g);
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
                    data.add(Double.parseDouble(value.toString().trim()));
                }
            } catch (Exception e) {
            }
        }
        return data;
    }

    private void drawEnhancedChart(Graphics2D g2d) {
        List<Double> data = getSelectedNumericData();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        if (data.isEmpty()) {
            g2d.setFont(new Font("Arial", Font.ITALIC, 14));
            g2d.setColor(Color.GRAY);
            g2d.drawString("Please load CSV and draw chart.", 50, 50);
            return;
        }

        String selectedColumn = (String) columnSelector.getSelectedItem();
        String chartTitle = (selectedColumn != null ? selectedColumn : "Data") +
                (lineChartRadio.isSelected() ? " – Line/Area Chart" : " – Bar Chart");

        // Margins: left=70 (Y labels), bottom=60 (X labels+title), right=20, top=40 (title)
        int marginLeft = 70;
        int marginRight = 20;
        int marginTop = 45;
        int marginBottom = 60;

        int panelW = drawingPanel.getWidth();
        int panelH = drawingPanel.getHeight();

        int drawW = panelW - marginLeft - marginRight;
        int drawH = panelH - marginTop - marginBottom;

        if (drawW <= 0 || drawH <= 0) return;

        // --- Chart title ---
        g2d.setFont(new Font("Arial", Font.BOLD, 14));
        g2d.setColor(new Color(50, 50, 60));
        FontMetrics fmTitle = g2d.getFontMetrics();
        int titleX = marginLeft + (drawW - fmTitle.stringWidth(chartTitle)) / 2;
        g2d.drawString(chartTitle, titleX, 22);

        // --- Compute range ---
        double max = Double.NEGATIVE_INFINITY;
        double min = Double.POSITIVE_INFINITY;
        for (Double v : data) {
            if (v > max) max = v;
            if (v < min) min = v;
        }
        if (min > 0) min = 0;
        double range = max - min;
        if (range == 0) range = 1;

        double scaleY = (double) drawH / range;

        // x-axis pixel Y (baseline)
        int xAxisY = marginTop + (int) ((max - 0) * scaleY);
        if (min >= 0) xAxisY = marginTop + drawH;

        // --- Grid lines + Y axis tick labels ---
        int numTicks = 5;
        g2d.setFont(new Font("Arial", Font.PLAIN, 11));
        FontMetrics fmTick = g2d.getFontMetrics();

        for (int i = 0; i <= numTicks; i++) {
            double tickValue = min + (range / numTicks) * (numTicks - i);
            int yTick = marginTop + (int) (i * (drawH / (double) numTicks));

            // grid line
            g2d.setColor(new Color(220, 225, 230));
            g2d.setStroke(new BasicStroke(1f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[]{5}, 0));
            g2d.drawLine(marginLeft, yTick, marginLeft + drawW, yTick);

            // tick label
            g2d.setStroke(new BasicStroke(1f));
            String tickLabel = String.format("%.1f", tickValue);
            int labelW = fmTick.stringWidth(tickLabel);
            g2d.setColor(new Color(80, 80, 90));
            g2d.drawString(tickLabel, marginLeft - labelW - 5, yTick + fmTick.getAscent() / 2);
        }

        // --- Axes ---
        g2d.setColor(Color.DARK_GRAY);
        g2d.setStroke(new BasicStroke(2f));
        // Y axis
        g2d.drawLine(marginLeft, marginTop, marginLeft, marginTop + drawH);
        // X axis (baseline)
        g2d.drawLine(marginLeft, xAxisY, marginLeft + drawW, xAxisY);

        // --- Y axis label (rotated) ---
        g2d.setFont(new Font("Arial", Font.BOLD, 12));
        g2d.setColor(new Color(50, 50, 60));
        String yAxisLabel = selectedColumn != null ? selectedColumn : "Value";
        Graphics2D g2dRotated = (Graphics2D) g2d.create();
        g2dRotated.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        FontMetrics fmAxisLabel = g2dRotated.getFontMetrics(new Font("Arial", Font.BOLD, 12));
        g2dRotated.setFont(new Font("Arial", Font.BOLD, 12));
        g2dRotated.setColor(new Color(50, 50, 60));
        int yLabelX = 14;
        int yLabelY = marginTop + drawH / 2 + fmAxisLabel.stringWidth(yAxisLabel) / 2;
        g2dRotated.rotate(-Math.PI / 2, yLabelX, yLabelY);
        g2dRotated.drawString(yAxisLabel, yLabelX, yLabelY);
        g2dRotated.dispose();

        // --- X axis label ---
        g2d.setFont(new Font("Arial", Font.BOLD, 12));
        g2d.setColor(new Color(50, 50, 60));
        String xAxisLabel = "Data Points (Rows)";
        FontMetrics fmX = g2d.getFontMetrics();
        int xLabelX = marginLeft + (drawW - fmX.stringWidth(xAxisLabel)) / 2;
        int xLabelY = panelH - 8;
        g2d.drawString(xAxisLabel, xLabelX, xLabelY);

        // --- Draw chart data ---
        if (lineChartRadio.isSelected()) {
            double scaleX = data.size() > 1 ? (double) drawW / (data.size() - 1) : 0;

            // Area fill
            Polygon areaPolygon = new Polygon();
            areaPolygon.addPoint(marginLeft, xAxisY);
            for (int i = 0; i < data.size(); i++) {
                int x = (int) (i * scaleX + marginLeft);
                int y = (int) ((max - data.get(i)) * scaleY + marginTop);
                areaPolygon.addPoint(x, y);
            }
            areaPolygon.addPoint((int) ((data.size() - 1) * scaleX + marginLeft), xAxisY);
            g2d.setColor(new Color(65, 130, 220, 50));
            g2d.fillPolygon(areaPolygon);

            // Line
            g2d.setColor(new Color(41, 128, 185));
            g2d.setStroke(new BasicStroke(3f));
            for (int i = 0; i < data.size() - 1; i++) {
                int x1 = (int) (i * scaleX + marginLeft);
                int y1 = (int) ((max - data.get(i)) * scaleY + marginTop);
                int x2 = (int) ((i + 1) * scaleX + marginLeft);
                int y2 = (int) ((max - data.get(i + 1)) * scaleY + marginTop);
                g2d.drawLine(x1, y1, x2, y2);
                g2d.fillOval(x1 - 4, y1 - 4, 8, 8);
            }
            if (!data.isEmpty()) {
                int lastX = (int) ((data.size() - 1) * scaleX + marginLeft);
                int lastY = (int) ((max - data.get(data.size() - 1)) * scaleY + marginTop);
                g2d.fillOval(lastX - 4, lastY - 4, 8, 8);
            }

            // X tick labels (show at most 10 evenly spaced)
            drawXTickLabels(g2d, data.size(), marginLeft, marginTop + drawH, drawW, scaleX);

        } else if (barChartRadio.isSelected()) {
            int totalBars = data.size();
            double elementWidth = (double) drawW / totalBars;
            double barGap = elementWidth * 0.2;
            double barActualWidth = elementWidth - barGap;

            for (int i = 0; i < data.size(); i++) {
                int x = (int) (marginLeft + (i * elementWidth) + (barGap / 2));
                double value = data.get(i);
                int barHeightPixels = (int) (Math.abs(value) * scaleY);
                int y = (value >= 0) ? (xAxisY - barHeightPixels) : xAxisY;

                // Shadow
                g2d.setColor(new Color(0, 0, 0, 40));
                g2d.fillRect(x + 5, y + 5, (int) barActualWidth, barHeightPixels);

                // Bar
                g2d.setColor(value >= 0 ? new Color(39, 174, 96) : new Color(231, 76, 60));
                g2d.fillRect(x, y, (int) barActualWidth, barHeightPixels);

                g2d.setColor(Color.WHITE);
                g2d.drawRect(x, y, (int) barActualWidth, barHeightPixels);
            }

            // X tick labels
            double scaleX = elementWidth;
            drawXTickLabelsBar(g2d, data.size(), marginLeft, marginTop + drawH, elementWidth, (int) barActualWidth);
        }
    }

    /** Draw row-number labels on the X axis for line chart */
    private void drawXTickLabels(Graphics2D g2d, int count, int marginLeft, int axisY,
                                  int drawW, double scaleX) {
        g2d.setFont(new Font("Arial", Font.PLAIN, 10));
        g2d.setColor(new Color(80, 80, 90));
        FontMetrics fm = g2d.getFontMetrics();
        int maxLabels = Math.min(count, 10);
        int step = Math.max(1, count / maxLabels);
        for (int i = 0; i < count; i += step) {
            int x = (int) (i * scaleX + marginLeft);
            String label = String.valueOf(i + 1);
            g2d.drawString(label, x - fm.stringWidth(label) / 2, axisY + 16);
        }
    }

    /** Draw row-number labels on the X axis for bar chart */
    private void drawXTickLabelsBar(Graphics2D g2d, int count, int marginLeft, int axisY,
                                     double elementWidth, int barWidth) {
        g2d.setFont(new Font("Arial", Font.PLAIN, 10));
        g2d.setColor(new Color(80, 80, 90));
        FontMetrics fm = g2d.getFontMetrics();
        double barGap = elementWidth - barWidth;
        int step = Math.max(1, count / 10);
        for (int i = 0; i < count; i += step) {
            int centerX = (int) (marginLeft + i * elementWidth + barGap / 2 + barWidth / 2.0);
            String label = String.valueOf(i + 1);
            g2d.drawString(label, centerX - fm.stringWidth(label) / 2, axisY + 16);
        }
    }
}
