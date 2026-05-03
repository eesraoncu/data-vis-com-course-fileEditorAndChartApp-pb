package org.example;

import javax.swing.*;
import java.awt.*;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                e.printStackTrace();
            }

            JFrame frame = new JFrame("Data Visualization & Communication Tool");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(1100, 750);
            frame.setMinimumSize(new Dimension(800, 600));
            frame.setLocationRelativeTo(null);

            JTabbedPane tabbedPane = new JTabbedPane();
            tabbedPane.setFont(new Font("Arial", Font.BOLD, 13));

            CSVEditorPanel editorPanel = new CSVEditorPanel();
            tabbedPane.addTab("📁  CSV Editor", editorPanel);

            LineBarChartPanel lineBarPanel = new LineBarChartPanel(editorPanel.getTableModel());
            tabbedPane.addTab("📈  Line & Bar Charts", lineBarPanel);

            PieChartPanel piePanel = new PieChartPanel(editorPanel.getTableModel());
            tabbedPane.addTab("🥧  Pie Chart", piePanel);

            HeatmapChartPanel heatmapPanel = new HeatmapChartPanel(editorPanel.getTableModel());
            tabbedPane.addTab("🌡  Heatmap", heatmapPanel);

            // Auto-refresh column lists when switching to chart tabs
            tabbedPane.addChangeListener(e -> {
                int idx = tabbedPane.getSelectedIndex();
                switch (idx) {
                    case 1 -> lineBarPanel.updateColumnList();
                    case 2 -> piePanel.updateColumnList();
                    // Heatmap uses all numeric columns automatically — no action needed
                }
            });

            frame.add(tabbedPane);
            frame.setVisible(true);
        });
    }
}