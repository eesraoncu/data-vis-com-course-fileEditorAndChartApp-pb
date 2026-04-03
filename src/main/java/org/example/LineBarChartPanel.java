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
        controlPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10)); // Estetik boşluk

        controlPanel.add(new JLabel("Select Data Column: "));
        columnSelector = new JComboBox<>();
        controlPanel.add(columnSelector);

        JButton refreshBtn = new JButton("Load Columns");
        refreshBtn.addActionListener(e -> updateColumnList());
        controlPanel.add(refreshBtn);

        lineChartRadio = new JRadioButton("Line/Area Chart", true);
        barChartRadio = new JRadioButton("3D Bar Chart");

        ButtonGroup chartGroup = new ButtonGroup();
        chartGroup.add(lineChartRadio);
        chartGroup.add(barChartRadio);

        controlPanel.add(Box.createHorizontalStrut(15));
        controlPanel.add(lineChartRadio);
        controlPanel.add(barChartRadio);

        JButton drawBtn = new JButton("Draw Chart");
        drawBtn.setFont(new Font("Arial", Font.BOLD, 12));
        drawBtn.addActionListener(e -> drawingPanel.repaint());
        controlPanel.add(Box.createHorizontalStrut(15));
        controlPanel.add(drawBtn);

        add(controlPanel, BorderLayout.NORTH);

        drawingPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                drawEnhancedChart((Graphics2D) g);
            }
        };
        drawingPanel.setBackground(new Color(250, 250, 252)); // Hafif gri/mavi modern arka plan
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

    private void drawEnhancedChart(Graphics2D g2d) {
        List<Double> data = getSelectedNumericData();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        if (data.isEmpty()) {
            g2d.drawString("Please load CSV and draw chart.", 50, 50);
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
        if (min > 0)
            min = 0;

        double range = max - min;
        if (range == 0)
            range = 1;

        int padding = 50;
        int labelPadding = 35;
        int drawingWidth = drawingPanel.getWidth() - (2 * padding) - labelPadding;
        int drawingHeight = drawingPanel.getHeight() - (2 * padding) - labelPadding;
        double scaleY = (double) drawingHeight / range;

        g2d.setColor(new Color(220, 225, 230));
        g2d.setStroke(new BasicStroke(1f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[] { 5 }, 0));
        int numGrids = 5;
        for (int i = 0; i <= numGrids; i++) {
            int yGrid = padding + (int) (i * (drawingHeight / (double) numGrids));
            g2d.drawLine(padding + labelPadding, yGrid, drawingPanel.getWidth() - padding, yGrid);
        }

        g2d.setColor(Color.DARK_GRAY);
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
        g2d.drawString(String.format("%.1f", max), padding - 15, padding);
        g2d.drawString(String.format("%.1f", min), padding - 15, drawingPanel.getHeight() - padding - labelPadding);

        if (lineChartRadio.isSelected()) {
            double scaleX = (double) drawingWidth / (data.size() - 1);
            if (data.size() == 1)
                scaleX = 0;

            Polygon areaPolygon = new Polygon();
            areaPolygon.addPoint(padding + labelPadding, xAxisY);

            for (int i = 0; i < data.size(); i++) {
                int x = (int) (i * scaleX + padding + labelPadding);
                int y = (int) ((max - data.get(i)) * scaleY + padding);
                areaPolygon.addPoint(x, y);
            }
            areaPolygon.addPoint((int) ((data.size() - 1) * scaleX + padding + labelPadding), xAxisY);

            g2d.setColor(new Color(65, 130, 220, 50));
            g2d.fillPolygon(areaPolygon);

            g2d.setColor(new Color(41, 128, 185));
            g2d.setStroke(new BasicStroke(3f));

            for (int i = 0; i < data.size() - 1; i++) {
                int x1 = (int) (i * scaleX + padding + labelPadding);
                int y1 = (int) ((max - data.get(i)) * scaleY + padding);
                int x2 = (int) ((i + 1) * scaleX + padding + labelPadding);
                int y2 = (int) ((max - data.get(i + 1)) * scaleY + padding);
                g2d.drawLine(x1, y1, x2, y2);

                g2d.fillOval(x1 - 4, y1 - 4, 8, 8);
            }
            if (data.size() > 0) {
                int lastX = (int) ((data.size() - 1) * scaleX + padding + labelPadding);
                int lastY = (int) ((max - data.get(data.size() - 1)) * scaleY + padding);
                g2d.fillOval(lastX - 4, lastY - 4, 8, 8);
            }

        } else if (barChartRadio.isSelected()) {
            int totalBars = data.size();
            double elementWidth = (double) drawingWidth / totalBars;
            double barGap = elementWidth * 0.2;
            double barActualWidth = elementWidth - barGap;

            for (int i = 0; i < data.size(); i++) {
                int x = (int) (padding + labelPadding + (i * elementWidth) + (barGap / 2));
                double value = data.get(i);
                int barHeightPixels = (int) (Math.abs(value) * scaleY);
                int y = (value >= 0) ? (xAxisY - barHeightPixels) : xAxisY;

                g2d.setColor(new Color(0, 0, 0, 40));
                g2d.fillRect(x + 5, y + 5, (int) barActualWidth, barHeightPixels);

                g2d.setColor(value >= 0 ? new Color(39, 174, 96) : new Color(231, 76, 60));
                g2d.fillRect(x, y, (int) barActualWidth, barHeightPixels);

                g2d.setColor(Color.WHITE);
                g2d.drawRect(x, y, (int) barActualWidth, barHeightPixels);
            }
        }
    }
}
