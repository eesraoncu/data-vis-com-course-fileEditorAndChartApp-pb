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

        JPanel controlPanel = new JPanel();

        controlPanel.add(new JLabel("Select Data Column:"));
        columnSelector = new JComboBox<>();
        controlPanel.add(columnSelector);

        JButton refreshBtn = new JButton("Load Columns");
        refreshBtn.addActionListener(e -> updateColumnList());
        controlPanel.add(refreshBtn);

        lineChartRadio = new JRadioButton("Line Chart", true);
        barChartRadio = new JRadioButton("Bar Chart");

        ButtonGroup chartGroup = new ButtonGroup();
        chartGroup.add(lineChartRadio);
        chartGroup.add(barChartRadio);

        controlPanel.add(lineChartRadio);
        controlPanel.add(barChartRadio);

        JButton drawBtn = new JButton("Draw Chart");
        drawBtn.addActionListener(e -> drawingPanel.repaint());
        controlPanel.add(drawBtn);

        add(controlPanel, BorderLayout.NORTH);

        drawingPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                drawChart((Graphics2D) g);
            }
        };
        drawingPanel.setBackground(Color.WHITE);
        add(drawingPanel, BorderLayout.CENTER);
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

    private void drawChart(Graphics2D g2d) {
        List<Double> data = getSelectedNumericData();

        if (data.isEmpty()) {
            g2d.drawString("Please load CSV, select a numeric column and click Draw Chart.", 50, 50);
            return;
        }

        double max = Double.MIN_VALUE;
        double min = Double.MAX_VALUE;
        for (Double value : data) {
            if (value > max)
                max = value;
            if (value < min)
                min = value;
        }

        if (min > 0) {
            min = 0;
        }

        double range = max - min;
        if (range == 0)
            range = 1;

        int padding = 50;
        int labelPadding = 25;
        int drawingWidth = drawingPanel.getWidth() - (2 * padding) - labelPadding;
        int drawingHeight = drawingPanel.getHeight() - (2 * padding) - labelPadding;

        double scaleY = (double) drawingHeight / range;

        g2d.setColor(Color.BLACK);
        g2d.setStroke(new BasicStroke(2f));

        g2d.drawLine(padding + labelPadding, padding, padding + labelPadding,
                drawingPanel.getHeight() - padding - labelPadding);

        int xAxisY = (int) ((max - 0) * scaleY + padding);
        if (min < 0) {
            g2d.drawLine(padding + labelPadding, xAxisY, drawingPanel.getWidth() - padding, xAxisY);
        } else {
            xAxisY = drawingPanel.getHeight() - padding - labelPadding;
            g2d.drawLine(padding + labelPadding, xAxisY, drawingPanel.getWidth() - padding, xAxisY);
        }

        g2d.setFont(new Font("Arial", Font.BOLD, 12));
        g2d.drawString(String.format("%.1f", max), padding - 20, padding);
        g2d.drawString(String.format("%.1f", min), padding - 20, drawingPanel.getHeight() - padding - labelPadding);
        if (lineChartRadio.isSelected()) {
            g2d.setColor(Color.BLUE);
            double scaleX = (double) drawingWidth / (data.size() - 1);
            if (data.size() == 1)
                scaleX = 0;

            for (int i = 0; i < data.size() - 1; i++) {
                int x1 = (int) (i * scaleX + padding + labelPadding);
                int y1 = (int) ((max - data.get(i)) * scaleY + padding);

                int x2 = (int) ((i + 1) * scaleX + padding + labelPadding);
                int y2 = (int) ((max - data.get(i + 1)) * scaleY + padding);

                g2d.drawLine(x1, y1, x2, y2);
                g2d.fillOval(x1 - 3, y1 - 3, 6, 6);
            }
            if (data.size() > 0) {
                int lastX = (int) ((data.size() - 1) * scaleX + padding + labelPadding);
                int lastY = (int) ((max - data.get(data.size() - 1)) * scaleY + padding);
                g2d.fillOval(lastX - 3, lastY - 3, 6, 6);
            }

        } else if (barChartRadio.isSelected()) {
            g2d.setColor(Color.decode("#2E8B57"));

            int totalBars = data.size();
            double elementWidth = (double) drawingWidth / totalBars;
            double barGap = elementWidth * 0.2;
            double barActualWidth = elementWidth - barGap;

            for (int i = 0; i < data.size(); i++) {
                int x = (int) (padding + labelPadding + (i * elementWidth) + (barGap / 2));

                double value = data.get(i);
                int barHeightPixels = (int) (Math.abs(value) * scaleY);

                int y;
                if (value >= 0) {
                    y = xAxisY - barHeightPixels;
                } else {
                    y = xAxisY;
                }

                g2d.fillRect(x, y, (int) barActualWidth, barHeightPixels);
                g2d.setColor(Color.BLACK);
                g2d.drawRect(x, y, (int) barActualWidth, barHeightPixels);
                g2d.setColor(Color.decode("#2E8B57"));
            }
        }
    }
}
