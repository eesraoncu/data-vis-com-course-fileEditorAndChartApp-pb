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

        JPanel controlPanel = new JPanel();

        controlPanel.add(new JLabel("Select Data Column:"));
        columnSelector = new JComboBox<>();
        controlPanel.add(columnSelector);

        JButton refreshBtn = new JButton("Load Columns");
        refreshBtn.addActionListener(e -> updateColumnList());
        controlPanel.add(refreshBtn);

        JButton drawBtn = new JButton("Draw Pie Chart");
        drawBtn.addActionListener(e -> drawingPanel.repaint());
        controlPanel.add(drawBtn);

        add(controlPanel, BorderLayout.NORTH);

        drawingPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                drawMathematicalPieChart((Graphics2D) g);
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
                    data.add(Math.abs(Double.parseDouble(value.toString().trim())));
                }
            } catch (Exception e) {
            }
        }
        return data;
    }

    private void drawMathematicalPieChart(Graphics2D g2d) {
        List<Double> data = getSelectedNumericData();

        if (data.isEmpty()) {
            g2d.drawString("Please load CSV, select a numeric column and click Draw Pie Chart.", 50, 50);
            return;
        }

        double totalSum = 0;
        for (Double value : data) {
            totalSum += value;
        }

        if (totalSum == 0)
            return;

        int width = drawingPanel.getWidth();
        int height = drawingPanel.getHeight();
        int cx = width / 2;
        int cy = height / 2;
        int minDimension = Math.min(width, height);
        int radius = (minDimension / 2) - 50;

        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        Color[] sliceColors = {
                Color.decode("#E74C3C"), Color.decode("#3498DB"), Color.decode("#F1C40F"),
                Color.decode("#2ECC71"), Color.decode("#9B59B6"), Color.decode("#E67E22"), Color.decode("#1ABC9C")
        };

        double currentAngleDegrees = 0;

        for (int i = 0; i < data.size(); i++) {
            double value = data.get(i);

            double percent = value / totalSum;
            double sweepAngleDegrees = percent * 360.0;

            Color segmentColor = sliceColors[i % sliceColors.length];
            g2d.setColor(segmentColor);

            double startAngle = currentAngleDegrees;
            double endAngle = currentAngleDegrees + sweepAngleDegrees;

            fillMathematicalPieSlice(g2d, cx, cy, radius, startAngle, endAngle);

            currentAngleDegrees += sweepAngleDegrees;
        }
    }

    private void fillMathematicalPieSlice(Graphics2D g2d, int cx, int cy, int radius, double startAngle,
            double endAngle) {

        Polygon slicePolygon = new Polygon();

        slicePolygon.addPoint(cx, cy);
        for (double angle = startAngle; angle <= endAngle; angle += 0.5) {
            double radian = Math.toRadians(angle);

            int arcX = (int) (cx + radius * Math.cos(radian));
            int arcY = (int) (cy - radius * Math.sin(radian));

            slicePolygon.addPoint(arcX, arcY);
        }

        double endRadian = Math.toRadians(endAngle);
        slicePolygon.addPoint(
                (int) (cx + radius * Math.cos(endRadian)),
                (int) (cy - radius * Math.sin(endRadian)));
        g2d.fillPolygon(slicePolygon);

        g2d.setColor(Color.WHITE);
        g2d.setStroke(new BasicStroke(1.5f));
        g2d.drawPolygon(slicePolygon);
    }
}
