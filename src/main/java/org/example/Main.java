package org.example;

import javax.swing.*;
import java.awt.*;

public class Main {
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        JFrame frame = new JFrame("Data Visualization Tools");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1000, 700);
        frame.setLocationRelativeTo(null);

        JTabbedPane tabbedPane = new JTabbedPane();

        CSVEditorPanel editorPanel = new CSVEditorPanel();
        tabbedPane.addTab("CSV Editor", editorPanel);

        LineBarChartPanel lineBarPanel = new LineBarChartPanel(editorPanel.getTableModel());
        tabbedPane.addTab("Line & Bar Charts", lineBarPanel);

        PieChartPanel piePanel = new PieChartPanel(editorPanel.getTableModel());
        tabbedPane.addTab("Pie Chart", piePanel);

        HeatmapChartPanel heatmapPanel = new HeatmapChartPanel(editorPanel.getTableModel());
        tabbedPane.addTab("Heatmap", heatmapPanel);

        frame.add(tabbedPane);

        frame.setVisible(true);
    }
}