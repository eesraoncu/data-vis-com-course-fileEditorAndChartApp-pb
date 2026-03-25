package org.example;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.*;

public class CSVEditorPanel extends JPanel {
    private JTable table;
    private DefaultTableModel tableModel;

    public CSVEditorPanel() {
        setLayout(new BorderLayout());

        tableModel = new DefaultTableModel();
        table = new JTable(tableModel);

        JScrollPane scrollPane = new JScrollPane(table);
        add(scrollPane, BorderLayout.CENTER);

        JPanel controlPanel = new JPanel();
        JButton btnLoadCSV = new JButton("Load CSV");
        JButton btnAddRow = new JButton("Add Row");
        JButton btnAddColumn = new JButton("Add Column");
        JButton btnSaveCSV = new JButton("Save CSV");

        controlPanel.add(btnLoadCSV);
        controlPanel.add(btnAddRow);
        controlPanel.add(btnAddColumn);
        controlPanel.add(btnSaveCSV);
        add(controlPanel, BorderLayout.NORTH);

        btnLoadCSV.addActionListener(e -> openFileChooserAndLoadData());
        btnAddRow.addActionListener(e -> addEmptyRow());
        btnAddColumn.addActionListener(e -> addNewColumn());
        btnSaveCSV.addActionListener(e -> saveCSVFile());
    }

    private void openFileChooserAndLoadData() {
        JFileChooser fileChooser = new JFileChooser();
        int returnValue = fileChooser.showOpenDialog(this);

        if (returnValue == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            readCSVFile(selectedFile);
        }
    }

    private void readCSVFile(File file) {
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            boolean isFirstLine = true;

            tableModel.setRowCount(0);
            tableModel.setColumnCount(0);

            while ((line = br.readLine()) != null) {
                String[] values = line.split(",");

                if (isFirstLine) {
                    for (String header : values) {
                        tableModel.addColumn(header);
                    }
                    isFirstLine = false;
                } else {
                    tableModel.addRow(values);
                }
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Error reading file: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void addEmptyRow() {
        if (tableModel.getColumnCount() > 0) {
            tableModel.addRow(new Object[tableModel.getColumnCount()]);
        } else {
            JOptionPane.showMessageDialog(this, "Please load a CSV or add a column first.", "Warning", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void addNewColumn() {
        String columnName = JOptionPane.showInputDialog(this, "Enter new column name:");
        if (columnName != null && !columnName.trim().isEmpty()) {
            tableModel.addColumn(columnName);
        }
    }

    private void saveCSVFile() {
        if (tableModel.getRowCount() == 0 && tableModel.getColumnCount() == 0) {
            JOptionPane.showMessageDialog(this, "No data to save.", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }

        JFileChooser fileChooser = new JFileChooser();
        int returnValue = fileChooser.showSaveDialog(this);

        if (returnValue == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();

            if (!file.getName().toLowerCase().endsWith(".csv")) {
                file = new File(file.getParentFile(), file.getName() + ".csv");
            }

            try (BufferedWriter bw = new BufferedWriter(new FileWriter(file))) {
                for (int i = 0; i < tableModel.getColumnCount(); i++) {
                    bw.write(tableModel.getColumnName(i));
                    if (i < tableModel.getColumnCount() - 1) {
                        bw.write(",");
                    }
                }
                bw.newLine();

                for (int i = 0; i < tableModel.getRowCount(); i++) {
                    for (int j = 0; j < tableModel.getColumnCount(); j++) {
                        Object value = tableModel.getValueAt(i, j);
                        bw.write(value != null ? value.toString() : "");
                        if (j < tableModel.getColumnCount() - 1) {
                            bw.write(",");
                        }
                    }
                    bw.newLine();
                }
                JOptionPane.showMessageDialog(this, "File saved successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, "Error saving file: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    public DefaultTableModel getTableModel() {
        return this.tableModel;
    }
}