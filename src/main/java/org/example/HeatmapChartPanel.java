package org.example;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class HeatmapChartPanel extends JPanel {
    private DefaultTableModel tableModel;
    private JPanel drawingPanel;

    public HeatmapChartPanel(DefaultTableModel model) {
        this.tableModel = model;
        setLayout(new BorderLayout());

        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
        controlPanel.setBackground(new Color(245, 247, 250));
        controlPanel.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(210, 215, 220)));

        JButton drawBtn = makeCtrlButton("▶  Generate Heatmap", new Color(230, 126, 34),
                "Render the heatmap using all fully numeric columns");
        drawBtn.addActionListener(e -> drawingPanel.repaint());
        controlPanel.add(drawBtn);

        add(controlPanel, BorderLayout.NORTH);

        drawingPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                drawRoundedMathematicalHeatmap((Graphics2D) g);
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

    private void drawRoundedMathematicalHeatmap(Graphics2D g2d) {
        if (tableModel.getRowCount() == 0 || tableModel.getColumnCount() == 0) return;

        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        // ----------------------------------------------------------------
        // Identify numeric columns. A column is numeric if it has at least
        // one parseable number; non-parseable and empty cells are skipped.
        // ----------------------------------------------------------------
        List<Integer> numericColumnIndices = new ArrayList<>();
        double globalMax = Double.NEGATIVE_INFINITY;
        double globalMin = Double.POSITIVE_INFINITY;

        for (int c = 0; c < tableModel.getColumnCount(); c++) {
            boolean hasAnyNumber = false;
            boolean hasNonNumber = false;
            for (int r = 0; r < tableModel.getRowCount(); r++) {
                Object valObj = tableModel.getValueAt(r, c);
                if (valObj == null || valObj.toString().trim().isEmpty()) continue;
                try {
                    double val = Double.parseDouble(valObj.toString().trim());
                    if (val > globalMax) globalMax = val;
                    if (val < globalMin) globalMin = val;
                    hasAnyNumber = true;
                } catch (NumberFormatException ex) {
                    hasNonNumber = true;
                }
            }
            // Include the column only if it has numbers and no text mixed in
            if (hasAnyNumber && !hasNonNumber) {
                numericColumnIndices.add(c);
            }
        }

        if (numericColumnIndices.isEmpty()) {
            g2d.setFont(new Font("Arial", Font.ITALIC, 14));
            g2d.setColor(Color.GRAY);
            g2d.drawString("No fully numeric columns found. Please check your CSV data.", 20, 40);
            return;
        }

        double dataRange = globalMax - globalMin;
        if (dataRange == 0) dataRange = 1;

        int panelW = drawingPanel.getWidth();
        int panelH = drawingPanel.getHeight();

        // Margins: top for title + column headers, left for row labels, right/bottom for legend
        int marginTop    = 70;   // title + column header space
        int marginLeft   = 75;   // row label space
        int marginRight  = 20;
        int marginBottom = 50;   // x-axis title

        int dWidth  = panelW - marginLeft - marginRight;
        int dHeight = panelH - marginTop - marginBottom;

        int rows = tableModel.getRowCount();
        int cols = numericColumnIndices.size();

        double cellWidth  = (double) dWidth  / cols;
        double cellHeight = (double) dHeight / rows;

        // --- Chart title ---
        g2d.setFont(new Font("Arial", Font.BOLD, 14));
        g2d.setColor(new Color(50, 50, 60));
        String title = "Heatmap – Numeric Column Correlation";
        FontMetrics fmTitle = g2d.getFontMetrics();
        g2d.drawString(title, (panelW - fmTitle.stringWidth(title)) / 2, 22);

        // --- X axis label (column names header already shown; add axis title at bottom) ---
        g2d.setFont(new Font("Arial", Font.BOLD, 12));
        g2d.setColor(new Color(60, 60, 70));
        String xAxisLabel = "Columns (Variables)";
        FontMetrics fmX = g2d.getFontMetrics();
        g2d.drawString(xAxisLabel, marginLeft + (dWidth - fmX.stringWidth(xAxisLabel)) / 2, panelH - 10);

        // --- Y axis label (rotated) ---
        Graphics2D gRot = (Graphics2D) g2d.create();
        gRot.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        gRot.setFont(new Font("Arial", Font.BOLD, 12));
        gRot.setColor(new Color(60, 60, 70));
        String yAxisLabel = "Rows (Observations)";
        FontMetrics fmY = gRot.getFontMetrics();
        int yLabelX = 13;
        int yLabelY = marginTop + dHeight / 2 + fmY.stringWidth(yAxisLabel) / 2;
        gRot.rotate(-Math.PI / 2, yLabelX, yLabelY);
        gRot.drawString(yAxisLabel, yLabelX, yLabelY);
        gRot.dispose();

        // --- Column header labels (X ticks) ---
        g2d.setFont(new Font("Arial", Font.BOLD, 11));
        g2d.setColor(Color.DARK_GRAY);
        for (int c = 0; c < cols; c++) {
            String colName = tableModel.getColumnName(numericColumnIndices.get(c));
            FontMetrics fmCol = g2d.getFontMetrics();
            int cx = marginLeft + (int) (c * cellWidth) + (int) (cellWidth / 2) - fmCol.stringWidth(colName) / 2;
            g2d.drawString(colName, cx, marginTop - 10);
        }

        // --- Row labels (Y ticks) ---
        g2d.setFont(new Font("Arial", Font.BOLD, 11));
        g2d.setColor(Color.DARK_GRAY);
        for (int r = 0; r < rows; r++) {
            String rowLabel = "Row " + (r + 1);
            FontMetrics fmRow = g2d.getFontMetrics();
            int ry = marginTop + (int) (r * cellHeight) + (int) (cellHeight / 2) + fmRow.getAscent() / 2;
            g2d.drawString(rowLabel, marginLeft - fmRow.stringWidth(rowLabel) - 5, ry);
        }

        // --- Cells ---
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                int actualColIndex = numericColumnIndices.get(c);
                Object valObj = tableModel.getValueAt(r, actualColIndex);

                double value;
                try {
                    if (valObj == null || valObj.toString().trim().isEmpty()) continue;
                    value = Double.parseDouble(valObj.toString().trim());
                } catch (NumberFormatException ex) {
                    continue;
                }

                double percent   = (value - globalMin) / dataRange;
                Color cellColor  = calculateMathematicalGradient(percent);

                int x = marginLeft + (int) (c * cellWidth);
                int y = marginTop  + (int) (r * cellHeight);

                g2d.setColor(cellColor);
                g2d.fillRoundRect(x, y, (int) Math.ceil(cellWidth) - 2, (int) Math.ceil(cellHeight) - 2, 10, 10);

                // Value text
                String text = String.valueOf((int) value);
                g2d.setFont(new Font("Segoe UI", Font.BOLD, 12));
                FontMetrics fm = g2d.getFontMetrics();
                int textX = x + ((int) cellWidth  - fm.stringWidth(text)) / 2;
                int textY = y + ((int) cellHeight - fm.getHeight())       / 2 + fm.getAscent();

                // shadow
                g2d.setColor(new Color(0, 0, 0, 50));
                g2d.drawString(text, textX + 1, textY + 1);

                // text color depending on background brightness
                g2d.setColor(percent > 0.4 && percent < 0.7 ? Color.BLACK : Color.WHITE);
                g2d.drawString(text, textX, textY);
            }
        }

        // --- Color scale legend (right-side vertical bar) ---
        drawColorScaleLegend(g2d, panelW - marginRight - 18, marginTop, 14, dHeight, globalMin, globalMax);
    }

    /** Draws a vertical color-scale bar with min/max labels. */
    private void drawColorScaleLegend(Graphics2D g2d, int x, int y, int barWidth, int barHeight,
                                       double minVal, double maxVal) {
        for (int i = 0; i <= barHeight; i++) {
            double percent = 1.0 - (double) i / barHeight;
            g2d.setColor(calculateMathematicalGradient(percent));
            g2d.fillRect(x, y + i, barWidth, 1);
        }
        g2d.setColor(Color.DARK_GRAY);
        g2d.setStroke(new BasicStroke(1f));
        g2d.drawRect(x, y, barWidth, barHeight);

        g2d.setFont(new Font("Arial", Font.PLAIN, 10));
        g2d.setColor(new Color(60, 60, 70));
        FontMetrics fm = g2d.getFontMetrics();
        String maxStr = String.format("%.0f", maxVal);
        String minStr = String.format("%.0f", minVal);
        g2d.drawString(maxStr, x - fm.stringWidth(maxStr) - 2, y + fm.getAscent());
        g2d.drawString(minStr, x - fm.stringWidth(minStr) - 2, y + barHeight);
    }

    private Color calculateMathematicalGradient(double percent) {
        if (percent < 0.0) percent = 0.0;
        if (percent > 1.0) percent = 1.0;
        int r, g, b;
        if (percent < 0.5) {
            double lp = percent * 2.0;
            r = (int) (250 * lp);
            g = (int) (250 * lp);
            b = 250;
        } else {
            double lp = (percent - 0.5) * 2.0;
            r = 250;
            g = (int) (250 * (1.0 - lp));
            b = (int) (250 * (1.0 - lp));
        }
        return new Color(r, g, b);
    }
}
