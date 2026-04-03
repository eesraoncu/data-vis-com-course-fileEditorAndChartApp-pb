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

        JPanel controlPanel = new JPanel();
        controlPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        JButton drawBtn = new JButton("Generate Heatmap Grid");
        drawBtn.setFont(new Font("Arial", Font.BOLD, 12));
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

    private void drawRoundedMathematicalHeatmap(Graphics2D g2d) {
        if (tableModel.getRowCount() == 0 || tableModel.getColumnCount() == 0)
            return;

        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        List<Integer> numericColumnIndices = new ArrayList<>();
        double globalMax = Double.MIN_VALUE;
        double globalMin = Double.MAX_VALUE;

        for (int c = 0; c < tableModel.getColumnCount(); c++) {
            boolean isNumeric = true;
            for (int r = 0; r < tableModel.getRowCount(); r++) {
                Object valObj = tableModel.getValueAt(r, c);
                if (valObj != null && !valObj.toString().trim().isEmpty()) {
                    try {
                        double val = Double.parseDouble(valObj.toString().trim());
                        if (val > globalMax)
                            globalMax = val;
                        if (val < globalMin)
                            globalMin = val;
                    } catch (NumberFormatException ex) {
                        isNumeric = false;
                        break;
                    }
                } else {
                    isNumeric = false;
                    break;
                }
            }
            if (isNumeric)
                numericColumnIndices.add(c);
        }

        if (numericColumnIndices.isEmpty())
            return;

        double dataRange = globalMax - globalMin;
        if (dataRange == 0)
            dataRange = 1;

        int paddingY = 60;
        int paddingX = 80;
        int dWidth = drawingPanel.getWidth() - paddingX - 40;
        int dHeight = drawingPanel.getHeight() - paddingY - 40;

        int rows = tableModel.getRowCount();
        int cols = numericColumnIndices.size();

        double cellWidth = (double) dWidth / cols;
        double cellHeight = (double) dHeight / rows;

        for (int r = 0; r < rows; r++) {
            g2d.setColor(Color.GRAY);
            g2d.setFont(new Font("Arial", Font.BOLD, 12));
            int textYAxis = paddingY + (int) (r * cellHeight) + (int) (cellHeight / 2) + 5;
            g2d.drawString("Row " + (r + 1), 20, textYAxis);

            for (int c = 0; c < cols; c++) {

                if (r == 0) {
                    g2d.drawString(tableModel.getColumnName(numericColumnIndices.get(c)),
                            paddingX + (int) (c * cellWidth) + (int) (cellWidth / 2) - 15, paddingY - 15);
                }

                int actualColIndex = numericColumnIndices.get(c);
                double value = Double.parseDouble(tableModel.getValueAt(r, actualColIndex).toString().trim());

                double percent = (value - globalMin) / dataRange;
                Color cellColor = calculateMathematicalGradient(percent);
                g2d.setColor(cellColor);

                int x = paddingX + (int) (c * cellWidth);
                int y = paddingY + (int) (r * cellHeight);

                g2d.fillRoundRect(x, y, (int) Math.ceil(cellWidth) - 2, (int) Math.ceil(cellHeight) - 2, 10, 10);

                g2d.setColor(percent > 0.4 && percent < 0.7 ? Color.BLACK : Color.WHITE);
                g2d.setFont(new Font("Segoe UI", Font.BOLD, 12));
                String text = String.valueOf((int) value);

                FontMetrics fm = g2d.getFontMetrics();
                int textX = x + ((int) cellWidth - fm.stringWidth(text)) / 2;
                int textY = y + ((int) cellHeight - fm.getHeight()) / 2 + fm.getAscent();
                g2d.setColor(new Color(0, 0, 0, 50));
                g2d.drawString(text, textX + 1, textY + 1);

                g2d.setColor(percent > 0.4 && percent < 0.7 ? Color.BLACK : Color.WHITE);
                g2d.drawString(text, textX, textY);
            }
        }
    }

    private Color calculateMathematicalGradient(double percent) {
        if (percent < 0.0)
            percent = 0.0;
        if (percent > 1.0)
            percent = 1.0;
        int r, g, b;

        if (percent < 0.5) {
            double localPercent = percent * 2.0;
            r = (int) (250 * localPercent);
            g = (int) (250 * localPercent);
            b = 250;
        } else {
            double localPercent = (percent - 0.5) * 2.0;
            r = 250;
            g = (int) (250 * (1.0 - localPercent));
            b = (int) (250 * (1.0 - localPercent));
        }
        return new Color(r, g, b);
    }
}
