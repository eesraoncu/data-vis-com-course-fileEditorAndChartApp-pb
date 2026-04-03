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
        controlPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        controlPanel.add(new JLabel("Select Data Column:"));
        columnSelector = new JComboBox<>();
        controlPanel.add(columnSelector);

        JButton refreshBtn = new JButton("Load Columns");
        refreshBtn.addActionListener(e -> updateColumnList());
        controlPanel.add(refreshBtn);

        JButton drawBtn = new JButton("Render 3D Pie");
        drawBtn.setFont(new Font("Arial", Font.BOLD, 12));
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

        if (data.isEmpty())
            return;

        double totalSum = 0;
        for (Double value : data)
            totalSum += value;
        if (totalSum == 0)
            return;

        int width = drawingPanel.getWidth();
        int height = drawingPanel.getHeight();
        int cx = width / 2;
        int cy = height / 2;
        int radius = (Math.min(width, height) / 2) - 60;

        Color[] sliceColors = {
                new Color(231, 76, 60), new Color(52, 152, 219), new Color(241, 196, 15),
                new Color(46, 204, 113), new Color(155, 89, 182), new Color(230, 126, 34), new Color(26, 188, 156)
        };

        g2d.setColor(new Color(0, 0, 0, 30));
        g2d.fillOval(cx - radius + 8, cy - radius + 8, radius * 2, radius * 2);

        double currentAngleDegrees = 0;

        for (int i = 0; i < data.size(); i++) {
            double value = data.get(i);
            double percent = value / totalSum;
            double sweepAngleDegrees = percent * 360.0;
            double startAngle = currentAngleDegrees;
            double endAngle = currentAngleDegrees + sweepAngleDegrees;

            Color segmentColor = sliceColors[i % sliceColors.length];
            g2d.setColor(segmentColor);
            fillPieSlice(g2d, cx, cy, radius, startAngle, endAngle);

            double middleAngle = startAngle + (sweepAngleDegrees / 2.0);
            double middleRadian = Math.toRadians(middleAngle);

            int textX = (int) (cx + (radius * 0.65) * Math.cos(middleRadian));
            int textY = (int) (cy - (radius * 0.65) * Math.sin(middleRadian)); // Eksi çünkü Y aşağı doğru

            String label = String.format("%.1f%%", percent * 100);
            g2d.setColor(Color.WHITE);
            g2d.setFont(new Font("Arial", Font.BOLD, 14));

            FontMetrics fm = g2d.getFontMetrics();
            int correctedX = textX - (fm.stringWidth(label) / 2);
            int correctedY = textY + (fm.getAscent() / 2);

            g2d.setColor(new Color(0, 0, 0, 100));
            g2d.drawString(label, correctedX + 1, correctedY + 1);

            g2d.setColor(Color.WHITE);
            g2d.drawString(label, correctedX, correctedY);

            currentAngleDegrees += sweepAngleDegrees;
        }
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
