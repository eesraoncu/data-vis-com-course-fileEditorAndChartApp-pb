package org.example;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class LineBarChartPanel extends JPanel {
    private DefaultTableModel tableModel;

    public LineBarChartPanel(DefaultTableModel model) {
        this.tableModel = model;
    }

    public List<Double> getNumericData(int columnIndex) {
        List<Double> data = new ArrayList<>();
        for (int i = 0; i < tableModel.getRowCount(); i++) {
            try {
                Object value = tableModel.getValueAt(i, columnIndex);
                if (value != null) {
                    data.add(Double.parseDouble(value.toString().trim()));
                }
            } catch (Exception e) {
                System.out.println("Skipping non-numeric value at row " + i);
            }
        }
        return data;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;

        List<Double> data = getNumericData(1);

        if (data.isEmpty()) {
            g2d.drawString("No numeric data available", 50, 50);
            return;
        }

        double max = Double.MIN_VALUE;
        double min = Double.MAX_VALUE;

        for (Double value : data) {
            if (value > max) {
                max = value;
            }
            if (value < min) {
                min = value;
            }
        }
        int padding = 50;
        int labelPadding = 25;
        int width = getWidth() - (2 * padding) - labelPadding;
        int height = getHeight() - 2 * (padding) - labelPadding;

        double range = max - min;
        if (range == 0) range = 1;

        double scaleY = (double) height / range;
        double scaleX = (double) width / (data.size() - 1);

        g2d.setColor(Color.BLUE);
        g2d.setStroke(new BasicStroke(2f));

        g2d.drawLine(padding + labelPadding, padding, padding + labelPadding, getHeight() - padding - labelPadding);

        g2d.drawLine(padding + labelPadding, getHeight() - padding - labelPadding, getWidth() - padding, getHeight() - padding - labelPadding);

        g2d.setFont(new Font("Arial", Font.BOLD, 12));
        g2d.drawString(String.format("%.1f", max), padding - 20, padding);
        g2d.drawString(String.format("%.1f", min), padding - 20, getHeight() - padding - labelPadding);

        for (int i = 0; i < data.size() - 1; i++) {

            int x1 = (int) (i * scaleX + padding + labelPadding);
            int y1 = (int) ((max - data.get(i)) * scaleY + padding);

            int x2 = (int) ((i + 1) * scaleX + padding + labelPadding);
            int y2 = (int) ((max - data.get(i + 1)) * scaleY + padding);

            g2d.drawLine(x1, y1, x2, y2);
            g2d.fillOval(x1 - 3, y1 - 3, 6, 6);
        }
    }
}