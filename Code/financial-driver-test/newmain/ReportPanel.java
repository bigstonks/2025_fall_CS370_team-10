import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.BorderFactory;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.Font;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionListener;

/**
 * ReportPanel shows a simple "report generator" screen.
 * Layout:
 *  - Top: Filters (date range + platform) and a "Generate Report" button.
 *  - Center: Table with delivery/report rows.
 *  - Bottom: Summary totals (deliveries, earnings, expenses, net).
 *
 * This panel does not depend on a database yet. The Generate button
 * currently loads sample data so the GUI can be tested visually.
 */
public class ReportPanel extends JPanel {

    // Filter fields
    private javax.swing.JTextField startDateField;    // Example format: 2025-11-01
    private javax.swing.JTextField endDateField;      // Example format: 2025-11-30
    private JComboBox<String> platformCombo;
    private JButton generateButton;

    // Table for detailed rows
    private JTable reportTable;
    private DefaultTableModel tableModel;

    // Summary labels at the bottom
    private JLabel totalDeliveriesValue;
    private JLabel totalEarningsValue;
    private JLabel totalExpensesValue;
    private JLabel netValue;

    /**
     * Constructs the ReportPanel and initializes all components.
     */
    public ReportPanel() {
        // Use BorderLayout: top (filters), center (table), bottom (summary).
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        setBackground(Color.WHITE);

        // Build UI sections.
        add(createFilterPanel(), BorderLayout.NORTH);
        add(createTablePanel(), BorderLayout.CENTER);
        add(createSummaryPanel(), BorderLayout.SOUTH);

        // Hook up button behavior.
        attachGenerateAction();
    }

    /**
     * Creates the top filter panel with date fields, platform selector, and button.
     */
    private JPanel createFilterPanel() {
        JPanel filterPanel = new JPanel();
        filterPanel.setLayout(new GridBagLayout());
        filterPanel.setBackground(Color.WHITE);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5); // Space between components
        gbc.anchor = GridBagConstraints.WEST;

        // Start Date
        JLabel startLabel = new JLabel("Start Date (YYYY-MM-DD):");
        startLabel.setFont(new Font("SansSerif", Font.PLAIN, 14));
        startDateField = new javax.swing.JTextField(10);

        gbc.gridx = 0;
        gbc.gridy = 0;
        filterPanel.add(startLabel, gbc);

        gbc.gridx = 1;
        filterPanel.add(startDateField, gbc);

        // End Date
        JLabel endLabel = new JLabel("End Date (YYYY-MM-DD):");
        endLabel.setFont(new Font("SansSerif", Font.PLAIN, 14));
        endDateField = new javax.swing.JTextField(10);

        gbc.gridx = 0;
        gbc.gridy = 1;
        filterPanel.add(endLabel, gbc);

        gbc.gridx = 1;
        filterPanel.add(endDateField, gbc);

        // Platform selector (optional filter)
        JLabel platformLabel = new JLabel("Platform:");
        platformLabel.setFont(new Font("SansSerif", Font.PLAIN, 14));
        platformCombo = new JComboBox<>(new String[] {
                "All",
                "DoorDash",
                "UberEats",
                "Grubhub",
                "Other"
        });

        gbc.gridx = 0;
        gbc.gridy = 2;
        filterPanel.add(platformLabel, gbc);

        gbc.gridx = 1;
        filterPanel.add(platformCombo, gbc);

        // Generate button on the right side spanning rows
        generateButton = new JButton("Generate Report");
        generateButton.setFont(new Font("SansSerif", Font.BOLD, 14));
        generateButton.setPreferredSize(new Dimension(160, 40));

        gbc.gridx = 2;
        gbc.gridy = 0;
        gbc.gridheight = 3;
        gbc.fill = GridBagConstraints.VERTICAL;
        filterPanel.add(generateButton, gbc);

        return filterPanel;
    }

    /**
     * Creates the center panel with the table that displays report rows.
     */
    private JScrollPane createTablePanel() {
        // Table columns: adjust as needed.
        String[] columnNames = {
                "Date",
                "Platform",
                "Base Pay",
                "Tips",
                "Extra Expenses",
                "Total Earnings"
        };

        // Table model: 0 rows at start.
        tableModel = new DefaultTableModel(columnNames, 0) {
            // Make cells non-editable by default.
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }

            // Use proper column types for numbers (helps with sorting, etc.).
            @Override
            public Class<?> getColumnClass(int columnIndex) {
                switch (columnIndex) {
                    case 2:
                    case 3:
                    case 4:
                    case 5:
                        return Double.class;
                    default:
                        return String.class;
                }
            }
        };

        reportTable = new JTable(tableModel);
        reportTable.setFillsViewportHeight(true);
        reportTable.setAutoCreateRowSorter(true); // Allow sorting by clicking headers.
        reportTable.setRowHeight(24);
        reportTable.setFont(new Font("SansSerif", Font.PLAIN, 13));
        reportTable.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 13));

        JScrollPane scrollPane = new JScrollPane(reportTable);
        scrollPane.setPreferredSize(new Dimension(600, 300));
        return scrollPane;
    }

    /**
     * Creates the bottom panel that displays summary totals.
     */
    private JPanel createSummaryPanel() {
        JPanel summaryPanel = new JPanel(new GridLayout(2, 4, 10, 5));
        summaryPanel.setBackground(Color.WHITE);
        summaryPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));

        JLabel totalDeliveriesLabel = new JLabel("Total Deliveries:");
        totalDeliveriesValue = new JLabel("0");

        JLabel totalEarningsLabel = new JLabel("Total Earnings:");
        totalEarningsValue = new JLabel("$0.00");

        JLabel totalExpensesLabel = new JLabel("Total Expenses:");
        totalExpensesValue = new JLabel("$0.00");

        JLabel netLabelTitle = new JLabel("Net:");
        netValue = new JLabel("$0.00");

        Font labelFont = new Font("SansSerif", Font.PLAIN, 14);
        Font valueFont = new Font("SansSerif", Font.BOLD, 14);

        totalDeliveriesLabel.setFont(labelFont);
        totalEarningsLabel.setFont(labelFont);
        totalExpensesLabel.setFont(labelFont);
        netLabelTitle.setFont(labelFont);

        totalDeliveriesValue.setFont(valueFont);
        totalEarningsValue.setFont(valueFont);
        totalExpensesValue.setFont(valueFont);
        netValue.setFont(valueFont);

        summaryPanel.add(totalDeliveriesLabel);
        summaryPanel.add(totalDeliveriesValue);
        summaryPanel.add(totalEarningsLabel);
        summaryPanel.add(totalEarningsValue);

        summaryPanel.add(totalExpensesLabel);
        summaryPanel.add(totalExpensesValue);
        summaryPanel.add(netLabelTitle);
        summaryPanel.add(netValue);

        return summaryPanel;
    }

    /**
     * Connects the "Generate Report" button to an action:
     * For now, fills the table with example data and calculates totals.
     * Later, this method can call a real service/DAO to fetch data from the database.
     */
    private void attachGenerateAction() {
        ActionListener generateListener = e -> {
            String startDate = startDateField.getText().trim();
            String endDate = endDateField.getText().trim();
            String platform = (String) platformCombo.getSelectedItem();

            // For now: ignore the actual date filter values and load sample data.
            // The platform filter still applies.
            loadSampleData(platform);

            // After loading rows into the table, recalculate summary totals.
            updateSummaryTotals();
        };

        generateButton.addActionListener(generateListener);
    }

    /**
     * Loads some hard-coded sample rows into the table.
     * This keeps the GUI testable even before the database is connected.
     */
    private void loadSampleData(String platformFilter) {
        // Clear any existing rows.
        tableModel.setRowCount(0);

        // Example data: date, platform, base pay, tips, extra expenses, total earnings.
        Object[][] sampleRows = {
                {"2025-11-01", "DoorDash", 10.50, 5.00, 1.25, 15.50},
                {"2025-11-01", "UberEats", 8.00, 3.50, 0.00, 11.50},
                {"2025-11-02", "DoorDash", 12.00, 4.00, 2.00, 16.00},
                {"2025-11-02", "Grubhub", 9.75, 2.25, 0.50, 11.50}
        };

        for (Object[] row : sampleRows) {
            String rowPlatform = (String) row[1];

            // If "All" is selected, include every row.
            // Otherwise, only include rows where the platform matches.
            if ("All".equals(platformFilter) || platformFilter.equals(rowPlatform)) {
                tableModel.addRow(row);
            }
        }
    }

    /**
     * Goes through the table rows and calculates:
     *  - Total deliveries (number of rows)
     *  - Sum of total earnings
     *  - Sum of extra expenses
     *  - Net = earnings - expenses
     */
    private void updateSummaryTotals() {
        int rowCount = tableModel.getRowCount();

        double totalEarnings = 0.0;
        double totalExpenses = 0.0;

        for (int row = 0; row < rowCount; row++) {
            // Column indexes match the table columns created earlier.
            double earnings = safeToDouble(tableModel.getValueAt(row, 5)); // Total Earnings
            double extraExpenses = safeToDouble(tableModel.getValueAt(row, 4)); // Extra Expenses

            totalEarnings += earnings;
            totalExpenses += extraExpenses;
        }

        double net = totalEarnings - totalExpenses;

        // Update labels.
        totalDeliveriesValue.setText(String.valueOf(rowCount));
        totalEarningsValue.setText(String.format("$%.2f", totalEarnings));
        totalExpensesValue.setText(String.format("$%.2f", totalExpenses));
        netValue.setText(String.format("$%.2f", net));
    }

    /**
     * Helper method to safely convert an Object to double.
     * Handles both Double and String values.
     */
    private double safeToDouble(Object value) {
        if (value == null) {
            return 0.0;
        }
        if (value instanceof Number) {
            return ((Number) value).doubleValue();
        }
        try {
            return Double.parseDouble(value.toString());
        } catch (NumberFormatException ex) {
            return 0.0;
        }
    }
}
