package org.example;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.io.*;

public class CSVEditorPanel extends JPanel {
    private JTable table;
    private DefaultTableModel tableModel;
    private JLabel fileLabel;

    // ── Colour palette ──────────────────────────────────────
    private static final Color CLR_LOAD    = new Color(52,  152, 219);
    private static final Color CLR_SAVE    = new Color(39,  174, 96);
    private static final Color CLR_ADD     = new Color(155, 89,  182);
    private static final Color CLR_DELETE  = new Color(231, 76,  60);
    private static final Color CLR_HOVER_FACTOR = new Color(255, 255, 255, 30);
    private static final Color ROW_ODD    = new Color(248, 250, 255);
    private static final Color ROW_EVEN   = Color.WHITE;
    private static final Color ROW_SELECT = new Color(173, 216, 230);

    public CSVEditorPanel() {
        setLayout(new BorderLayout(0, 0));

        // ── Table ────────────────────────────────────────────
        tableModel = new DefaultTableModel();
        table = new JTable(tableModel) {
            @Override
            public Component prepareRenderer(javax.swing.table.TableCellRenderer r, int row, int col) {
                Component c = super.prepareRenderer(r, row, col);
                if (!isRowSelected(row)) {
                    c.setBackground(row % 2 == 0 ? ROW_EVEN : ROW_ODD);
                } else {
                    c.setBackground(ROW_SELECT);
                }
                c.setForeground(new Color(40, 40, 50));
                return c;
            }
        };
        table.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        table.setRowHeight(26);
        table.setGridColor(new Color(220, 225, 235));
        table.setSelectionBackground(ROW_SELECT);
        table.setIntercellSpacing(new Dimension(1, 1));
        table.setShowGrid(true);

        // Header styling
        JTableHeader header = table.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 13));
        header.setBackground(new Color(52, 73, 94));
        header.setForeground(Color.WHITE);
        header.setPreferredSize(new Dimension(header.getWidth(), 32));
        header.setDefaultRenderer(new DefaultTableCellRenderer() {
            {
                setHorizontalAlignment(CENTER);
                setOpaque(true);
            }
            @Override
            public Component getTableCellRendererComponent(JTable t, Object val,
                    boolean sel, boolean focus, int row, int col) {
                super.getTableCellRendererComponent(t, val, sel, focus, row, col);
                setBackground(new Color(52, 73, 94));
                setForeground(Color.WHITE);
                setFont(new Font("Segoe UI", Font.BOLD, 13));
                setBorder(BorderFactory.createMatteBorder(0, 0, 2, 1, new Color(80, 100, 120)));
                return this;
            }
        });

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(200, 210, 220)));
        add(scrollPane, BorderLayout.CENTER);

        // ── Top control panel ────────────────────────────────
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(new Color(245, 247, 250));
        topPanel.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(210, 215, 220)));

        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
        controlPanel.setOpaque(false);

        JButton btnLoadCSV    = makeButton("Load CSV",     CLR_LOAD,   "Open a CSV file and load it into the editor");
        JButton btnSaveCSV    = makeButton("Save CSV",     CLR_SAVE,   "Save current table data to a CSV file");
        JButton btnAddRow     = makeButton("+ Row",        CLR_ADD,    "Add an empty row at the bottom");
        JButton btnAddColumn  = makeButton("+ Column",     CLR_ADD,    "Add a new named column");
        JButton btnDeleteRow  = makeButton("− Row",        CLR_DELETE, "Delete the currently selected row");
        JButton btnDeleteColumn = makeButton("− Column",   CLR_DELETE, "Select and delete a column");

        controlPanel.add(btnLoadCSV);
        controlPanel.add(btnSaveCSV);
        controlPanel.add(makeSeparator());
        controlPanel.add(btnAddRow);
        controlPanel.add(btnAddColumn);
        controlPanel.add(makeSeparator());
        controlPanel.add(btnDeleteRow);
        controlPanel.add(btnDeleteColumn);

        topPanel.add(controlPanel, BorderLayout.WEST);

        // ── File name label (right side of top bar) ─────────
        fileLabel = new JLabel("No file loaded");
        fileLabel.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        fileLabel.setForeground(new Color(120, 130, 145));
        fileLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 14));
        topPanel.add(fileLabel, BorderLayout.EAST);

        add(topPanel, BorderLayout.NORTH);

        // ── Action listeners ─────────────────────────────────
        btnLoadCSV.addActionListener(e -> openFileChooserAndLoadData());
        btnAddRow.addActionListener(e -> addEmptyRow());
        btnAddColumn.addActionListener(e -> addNewColumn());
        btnDeleteRow.addActionListener(e -> deleteSelectedRow());
        btnDeleteColumn.addActionListener(e -> deleteSelectedColumn());
        btnSaveCSV.addActionListener(e -> saveCSVFile());
    }

    // ── Helper: styled button ────────────────────────────────
    private JButton makeButton(String text, Color bg, String tooltip) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setOpaque(true);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(btn.getPreferredSize().width + 12, 32));
        btn.setToolTipText(tooltip);

        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            Color original = bg;
            @Override public void mouseEntered(java.awt.event.MouseEvent e) {
                btn.setBackground(bg.brighter());
            }
            @Override public void mouseExited(java.awt.event.MouseEvent e) {
                btn.setBackground(original);
            }
        });
        return btn;
    }

    // ── Helper: visual separator ─────────────────────────────
    private JSeparator makeSeparator() {
        JSeparator sep = new JSeparator(SwingConstants.VERTICAL);
        sep.setPreferredSize(new Dimension(1, 28));
        sep.setForeground(new Color(200, 210, 220));
        return sep;
    }

    // ── CSV operations ───────────────────────────────────────
    private void openFileChooserAndLoadData() {
        JFileChooser fc = new JFileChooser();
        fc.setDialogTitle("Select a CSV file");
        if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File f = fc.getSelectedFile();
            readCSVFile(f);
            fileLabel.setText("📄 " + f.getName());
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
                    for (String header : values) tableModel.addColumn(header.trim());
                    isFirstLine = false;
                } else {
                    tableModel.addRow(values);
                }
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Error reading file: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void addEmptyRow() {
        if (tableModel.getColumnCount() > 0) {
            tableModel.addRow(new Object[tableModel.getColumnCount()]);
        } else {
            JOptionPane.showMessageDialog(this,
                    "Please load a CSV or add a column first.", "Warning", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void addNewColumn() {
        String columnName = JOptionPane.showInputDialog(this, "Enter new column name:");
        if (columnName != null && !columnName.trim().isEmpty()) {
            tableModel.addColumn(columnName.trim());
        }
    }

    private void deleteSelectedRow() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow != -1) {
            tableModel.removeRow(table.convertRowIndexToModel(selectedRow));
        } else {
            JOptionPane.showMessageDialog(this,
                    "Please select a cell in the row you want to delete.", "Warning", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void deleteSelectedColumn() {
        int colCount = tableModel.getColumnCount();
        if (colCount == 0) {
            JOptionPane.showMessageDialog(this, "No columns to delete.", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }
        String[] columns = new String[colCount];
        for (int i = 0; i < colCount; i++) columns[i] = tableModel.getColumnName(i);

        String defaultSelection = columns[0];
        int selectedColIndex = table.getSelectedColumn();
        if (selectedColIndex != -1)
            defaultSelection = tableModel.getColumnName(table.convertColumnIndexToModel(selectedColIndex));

        String selectedColumnName = (String) JOptionPane.showInputDialog(
                this, "Select column to delete:", "Delete Column",
                JOptionPane.QUESTION_MESSAGE, null, columns, defaultSelection);

        if (selectedColumnName != null) {
            int modelColumn = -1;
            for (int i = 0; i < colCount; i++) {
                if (tableModel.getColumnName(i).equals(selectedColumnName)) { modelColumn = i; break; }
            }
            if (modelColumn != -1) {
                int rowCount = tableModel.getRowCount();
                Object[] newIdentifiers = new Object[colCount - 1];
                int idIdx = 0;
                for (int i = 0; i < colCount; i++)
                    if (i != modelColumn) newIdentifiers[idIdx++] = tableModel.getColumnName(i);

                Object[][] newData = new Object[rowCount][colCount - 1];
                for (int i = 0; i < rowCount; i++) {
                    int cIdx = 0;
                    for (int j = 0; j < colCount; j++)
                        if (j != modelColumn) newData[i][cIdx++] = tableModel.getValueAt(i, j);
                }
                tableModel.setDataVector(newData, newIdentifiers);
            }
        }
    }

    private void saveCSVFile() {
        if (tableModel.getRowCount() == 0 && tableModel.getColumnCount() == 0) {
            JOptionPane.showMessageDialog(this, "No data to save.", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }
        JFileChooser fc = new JFileChooser();
        fc.setDialogTitle("Save CSV file");
        if (fc.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            File file = fc.getSelectedFile();
            if (!file.getName().toLowerCase().endsWith(".csv"))
                file = new File(file.getParentFile(), file.getName() + ".csv");

            try (BufferedWriter bw = new BufferedWriter(new FileWriter(file))) {
                for (int i = 0; i < tableModel.getColumnCount(); i++) {
                    bw.write(tableModel.getColumnName(i));
                    if (i < tableModel.getColumnCount() - 1) bw.write(",");
                }
                bw.newLine();
                for (int i = 0; i < tableModel.getRowCount(); i++) {
                    for (int j = 0; j < tableModel.getColumnCount(); j++) {
                        Object value = tableModel.getValueAt(i, j);
                        bw.write(value != null ? value.toString() : "");
                        if (j < tableModel.getColumnCount() - 1) bw.write(",");
                    }
                    bw.newLine();
                }
                JOptionPane.showMessageDialog(this, "File saved successfully!", "Success",
                        JOptionPane.INFORMATION_MESSAGE);
                fileLabel.setText("📄 " + file.getName() + "  ✓");
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, "Error saving file: " + ex.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    public DefaultTableModel getTableModel() { return this.tableModel; }
}