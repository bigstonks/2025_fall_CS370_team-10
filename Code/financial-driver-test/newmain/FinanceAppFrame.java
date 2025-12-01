// ========================= IMPORTS =========================
// Swing imports
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.JComboBox;
import javax.swing.SwingUtilities;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.JScrollPane;
import javax.swing.table.DefaultTableModel;
import javax.swing.JDialog;
import javax.swing.Timer;

// AWT imports
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

// Utility
import java.util.Objects;

/**
 * Main GUI window for the Driver Finance Tracker application.
 */
public class FinanceAppFrame extends JFrame {

    // =========================================================
    //   FIELDS (STATE OF THE WINDOW)
    // =========================================================

    /** CardLayout controller for switching between screens. */
    private CardLayout cardLayout;

    /** Main panel that holds all screens for CardLayout. */
    private JPanel contentPanel;

    // ----- Revenue form fields -----
    private JTextField revenueDateField;
    private JComboBox<String> revenueSourceCombo;
    private JTextField revenueHoursField;
    private JTextField revenueBasePayField;
    private JTextField revenueTipsField;

    // Revenue table model and table for past entries
    private DefaultTableModel revenueTableModel;
    private JTable revenueTable;

    // ----- Expenses form fields -----
    private JTextField expenseDateField;
    private JComboBox<String> expenseCategoryCombo;
    private JTextField expenseAmountField;
    private JTextField expenseNotesField;

    // Expenses table model and table for past entries
    private DefaultTableModel expenseTableModel;
    private JTable expenseTable;

    // =========================================================
    //   CONSTRUCTOR
    // =========================================================

    public FinanceAppFrame() {
        setTitle("Driver Finance Tracker");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(900, 600);
        setLocationRelativeTo(null);

        setLayout(new BorderLayout());

        JPanel topBar = createTopBar();
        add(topBar, BorderLayout.NORTH);

        JPanel sideMenu = createSideMenu();
        add(sideMenu, BorderLayout.WEST);

        contentPanel = createContentPanel();
        add(contentPanel, BorderLayout.CENTER);
    }

    // =========================================================
    //   TOP BAR
    // =========================================================

    private JPanel createTopBar() {
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));
        topBar.setBackground(new Color(33, 150, 243));

        JLabel titleLabel = new JLabel("Driver Finance Tracker");
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 24));
        titleLabel.setForeground(Color.WHITE);

        topBar.add(titleLabel, BorderLayout.WEST);

        return topBar;
    }

    // =========================================================
    //   SIDE MENU
    // =========================================================

    private JPanel createSideMenu() {
        JPanel sideMenu = new JPanel();
        sideMenu.setLayout(new BoxLayout(sideMenu, BoxLayout.Y_AXIS));
        sideMenu.setPreferredSize(new Dimension(200, 0));
        sideMenu.setBorder(BorderFactory.createEmptyBorder(15, 10, 15, 10));
        sideMenu.setBackground(new Color(245, 245, 245));

        JLabel menuLabel = new JLabel("Menu");
        menuLabel.setFont(new Font("SansSerif", Font.BOLD, 16));
        menuLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        sideMenu.add(menuLabel);
        sideMenu.add(javax.swing.Box.createVerticalStrut(10));

        JButton homeButton = new JButton("Home");
        JButton revenueButton = new JButton("Revenue");
        JButton expensesButton = new JButton("Expenses");
        JButton reportsButton = new JButton("Reports");
        JButton settingsButton = new JButton("Settings");

        styleMenuButton(homeButton);
        styleMenuButton(revenueButton);
        styleMenuButton(expensesButton);
        styleMenuButton(reportsButton);
        styleMenuButton(settingsButton);

        homeButton.addActionListener(e -> showScreen("HOME"));
        revenueButton.addActionListener(e -> showScreen("REVENUE"));
        expensesButton.addActionListener(e -> showScreen("EXPENSES"));
        reportsButton.addActionListener(e -> showScreen("REPORTS"));
        settingsButton.addActionListener(e -> showScreen("SETTINGS"));

        sideMenu.add(homeButton);
        sideMenu.add(revenueButton);
        sideMenu.add(expensesButton);
        sideMenu.add(reportsButton);
        sideMenu.add(settingsButton);

        sideMenu.add(javax.swing.Box.createVerticalGlue());

        return sideMenu;
    }

    private void styleMenuButton(JButton button) {
        button.setAlignmentX(Component.LEFT_ALIGNMENT);
        button.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        button.setFocusPainted(false);
        button.setFont(new Font("SansSerif", Font.PLAIN, 14));
    }

    // =========================================================
    //   MAIN CONTENT AREA (CardLayout)
    // =========================================================

    private JPanel createContentPanel() {
        cardLayout = new CardLayout();
        JPanel panel = new JPanel(cardLayout);

        JPanel homeScreen = createHomeScreen();
        JPanel revenueScreen = createRevenueScreen();
        JPanel expensesScreen = createExpensesScreen();
        JPanel reportsScreen = createReportsScreen();
        JPanel settingsScreen = createSettingsScreen();

        panel.add(homeScreen, "HOME");
        panel.add(revenueScreen, "REVENUE");
        panel.add(expensesScreen, "EXPENSES");
        panel.add(reportsScreen, "REPORTS");
        panel.add(settingsScreen, "SETTINGS");

        return panel;
    }

    // =========================================================
    //   HOME SCREEN
    // =========================================================

    private JPanel createHomeScreen() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        panel.setBackground(Color.WHITE);

        JLabel titleLabel = new JLabel("Home Dashboard");
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 22));
        panel.add(titleLabel, BorderLayout.NORTH);

        JPanel statsPanel = new JPanel(new GridLayout(1, 3, 15, 0));
        statsPanel.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));
        statsPanel.setBackground(Color.WHITE);

        statsPanel.add(createStatCard("Total Revenue", "$0.00"));
        statsPanel.add(createStatCard("Total Expenses", "$0.00"));
        statsPanel.add(createStatCard("Total", "$0.00"));

        panel.add(statsPanel, BorderLayout.CENTER);

        JLabel infoLabel = new JLabel("Recent activity and insights will appear here.");
        infoLabel.setFont(new Font("SansSerif", Font.PLAIN, 14));
        panel.add(infoLabel, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createStatCard(String title, String value) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(new Color(250, 250, 250));
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 220, 220)),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("SansSerif", Font.PLAIN, 14));

        JLabel valueLabel = new JLabel(value);
        valueLabel.setFont(new Font("SansSerif", Font.BOLD, 20));

        card.add(titleLabel, BorderLayout.NORTH);
        card.add(valueLabel, BorderLayout.CENTER);

        return card;
    }

    // =========================================================
    //   REVENUE SCREEN (Form + Past Entries Table)
    // =========================================================

    private JPanel createRevenueScreen() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        panel.setBackground(Color.WHITE);

        JLabel titleLabel = new JLabel("Revenue - Add and Edit Delivery Earnings");
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 20));
        panel.add(titleLabel, BorderLayout.NORTH);

        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.setBackground(Color.WHITE);

        // Form panel
        JPanel formPanel = new JPanel(new GridLayout(5, 2, 10, 10));
        formPanel.setBackground(Color.WHITE);

        JLabel dateLabel = new JLabel("Date (e.g., 2025-11-17):");
        revenueDateField = new JTextField();
        formPanel.add(dateLabel);
        formPanel.add(revenueDateField);

        JLabel sourceLabel = new JLabel("Source:");
        String[] sources = {"DoorDash", "Uber Eats", "Grubhub", "Postmates", "Other"};
        revenueSourceCombo = new JComboBox<>(sources);
        formPanel.add(sourceLabel);
        formPanel.add(revenueSourceCombo);

        JLabel hoursLabel = new JLabel("Hours worked:");
        revenueHoursField = new JTextField();
        formPanel.add(hoursLabel);
        formPanel.add(revenueHoursField);

        JLabel basePayLabel = new JLabel("Base pay ($):");
        revenueBasePayField = new JTextField();
        formPanel.add(basePayLabel);
        formPanel.add(revenueBasePayField);

        JLabel tipsLabel = new JLabel("Tips ($):");
        revenueTipsField = new JTextField();
        formPanel.add(tipsLabel);
        formPanel.add(revenueTipsField);

        centerPanel.add(formPanel, BorderLayout.NORTH);

        // Table for past revenue records
        String[] revenueColumns = {"Date", "Source", "Hours", "Base Pay", "Tips", "Total"};
        revenueTableModel = new DefaultTableModel(revenueColumns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // editing happens via form
            }

            @Override
            public Class<?> getColumnClass(int columnIndex) {
                if (columnIndex >= 2) {
                    return Double.class;
                }
                return String.class;
            }
        };

        revenueTable = new JTable(revenueTableModel);
        revenueTable.setFillsViewportHeight(true);
        revenueTable.setRowHeight(22);
        revenueTable.setAutoCreateRowSorter(true);

        // Click row to load into form
        revenueTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int viewRow = revenueTable.getSelectedRow();
                if (viewRow >= 0) {
                    int modelRow = revenueTable.convertRowIndexToModel(viewRow);
                    revenueDateField.setText(Objects.toString(revenueTableModel.getValueAt(modelRow, 0), ""));
                    revenueSourceCombo.setSelectedItem(Objects.toString(revenueTableModel.getValueAt(modelRow, 1), ""));
                    revenueHoursField.setText(Objects.toString(revenueTableModel.getValueAt(modelRow, 2), ""));
                    revenueBasePayField.setText(Objects.toString(revenueTableModel.getValueAt(modelRow, 3), ""));
                    revenueTipsField.setText(Objects.toString(revenueTableModel.getValueAt(modelRow, 4), ""));
                }
            }
        });

        JScrollPane revenueScrollPane = new JScrollPane(revenueTable);
        revenueScrollPane.setBorder(BorderFactory.createTitledBorder("Past Revenue Entries"));
        centerPanel.add(revenueScrollPane, BorderLayout.CENTER);

        panel.add(centerPanel, BorderLayout.CENTER);

        // Bottom buttons
        JPanel bottomPanel = new JPanel();
        bottomPanel.setBackground(Color.WHITE);

        JButton saveButton = new JButton("Save New Revenue");
        JButton updateButton = new JButton("Update Selected");

        saveButton.addActionListener(e -> handleSaveRevenue());
        updateButton.addActionListener(e -> handleUpdateRevenue());

        bottomPanel.add(saveButton);
        bottomPanel.add(updateButton);

        panel.add(bottomPanel, BorderLayout.SOUTH);

        return panel;
    }

    private void handleSaveRevenue() {
        String date = revenueDateField.getText().trim();
        String source = (String) revenueSourceCombo.getSelectedItem();
        String hoursText = revenueHoursField.getText().trim();
        String basePayText = revenueBasePayField.getText().trim();
        String tipsText = revenueTipsField.getText().trim();

        double hours;
        double basePay;
        double tips;

        try {
            hours = Double.parseDouble(hoursText);
            basePay = Double.parseDouble(basePayText);
            tips = Double.parseDouble(tipsText);
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(
                    this,
                    "Please enter valid numbers for hours, base pay, and tips.",
                    "Input Error",
                    JOptionPane.ERROR_MESSAGE
            );
            return;
        }

        double total = basePay + tips;

        revenueTableModel.addRow(new Object[]{date, source, hours, basePay, tips, total});

        showAutoCloseSuccess("Revenue saved");
    }

    private void handleUpdateRevenue() {
        int selectedViewRow = revenueTable.getSelectedRow();
        if (selectedViewRow < 0) {
            JOptionPane.showMessageDialog(
                    this,
                    "Select a row in the table to update.",
                    "No Selection",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        int row = revenueTable.convertRowIndexToModel(selectedViewRow);

        String date = revenueDateField.getText().trim();
        String source = (String) revenueSourceCombo.getSelectedItem();
        String hoursText = revenueHoursField.getText().trim();
        String basePayText = revenueBasePayField.getText().trim();
        String tipsText = revenueTipsField.getText().trim();

        double hours;
        double basePay;
        double tips;

        try {
            hours = Double.parseDouble(hoursText);
            basePay = Double.parseDouble(basePayText);
            tips = Double.parseDouble(tipsText);
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(
                    this,
                    "Please enter valid numbers for hours, base pay, and tips.",
                    "Input Error",
                    JOptionPane.ERROR_MESSAGE
            );
            return;
        }

        double total = basePay + tips;

        revenueTableModel.setValueAt(date, row, 0);
        revenueTableModel.setValueAt(source, row, 1);
        revenueTableModel.setValueAt(hours, row, 2);
        revenueTableModel.setValueAt(basePay, row, 3);
        revenueTableModel.setValueAt(tips, row, 4);
        revenueTableModel.setValueAt(total, row, 5);

        showAutoCloseSuccess("Revenue updated");
    }

    // =========================================================
    //   EXPENSES SCREEN (Form + Past Entries Table)
    // =========================================================

    private JPanel createExpensesScreen() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        panel.setBackground(Color.WHITE);

        JLabel titleLabel = new JLabel("Expenses - Add and Edit Driver Expenses");
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 20));
        panel.add(titleLabel, BorderLayout.NORTH);

        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.setBackground(Color.WHITE);

        // Form panel
        JPanel formPanel = new JPanel(new GridLayout(4, 2, 10, 10));
        formPanel.setBackground(Color.WHITE);

        JLabel dateLabel = new JLabel("Date (e.g., 2025-11-17):");
        expenseDateField = new JTextField();
        formPanel.add(dateLabel);
        formPanel.add(expenseDateField);

        JLabel categoryLabel = new JLabel("Category:");
        String[] categories = {"Gas", "Maintenance", "Parking", "Tolls", "Car Wash", "Other"};
        expenseCategoryCombo = new JComboBox<>(categories);
        formPanel.add(categoryLabel);
        formPanel.add(expenseCategoryCombo);

        JLabel amountLabel = new JLabel("Amount ($):");
        expenseAmountField = new JTextField();
        formPanel.add(amountLabel);
        formPanel.add(expenseAmountField);

        JLabel notesLabel = new JLabel("Notes (optional):");
        expenseNotesField = new JTextField();
        formPanel.add(notesLabel);
        formPanel.add(expenseNotesField);

        centerPanel.add(formPanel, BorderLayout.NORTH);

        // Table for past expenses
        String[] expenseColumns = {"Date", "Category", "Amount", "Notes"};
        expenseTableModel = new DefaultTableModel(expenseColumns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }

            @Override
            public Class<?> getColumnClass(int columnIndex) {
                if (columnIndex == 2) {
                    return Double.class;
                }
                return String.class;
            }
        };

        expenseTable = new JTable(expenseTableModel);
        expenseTable.setFillsViewportHeight(true);
        expenseTable.setRowHeight(22);
        expenseTable.setAutoCreateRowSorter(true);

        // Click row to load into form
        expenseTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int viewRow = expenseTable.getSelectedRow();
                if (viewRow >= 0) {
                    int modelRow = expenseTable.convertRowIndexToModel(viewRow);
                    expenseDateField.setText(Objects.toString(expenseTableModel.getValueAt(modelRow, 0), ""));
                    expenseCategoryCombo.setSelectedItem(Objects.toString(expenseTableModel.getValueAt(modelRow, 1), ""));
                    expenseAmountField.setText(Objects.toString(expenseTableModel.getValueAt(modelRow, 2), ""));
                    expenseNotesField.setText(Objects.toString(expenseTableModel.getValueAt(modelRow, 3), ""));
                }
            }
        });

        JScrollPane expenseScrollPane = new JScrollPane(expenseTable);
        expenseScrollPane.setBorder(BorderFactory.createTitledBorder("Past Expense Entries"));
        centerPanel.add(expenseScrollPane, BorderLayout.CENTER);

        panel.add(centerPanel, BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel();
        bottomPanel.setBackground(Color.WHITE);

        JButton saveButton = new JButton("Save New Expense");
        JButton updateButton = new JButton("Update Selected");

        saveButton.addActionListener(e -> handleSaveExpense());
        updateButton.addActionListener(e -> handleUpdateExpense());

        bottomPanel.add(saveButton);
        bottomPanel.add(updateButton);

        panel.add(bottomPanel, BorderLayout.SOUTH);

        return panel;
    }

    private void handleSaveExpense() {
        String date = expenseDateField.getText().trim();
        String category = (String) expenseCategoryCombo.getSelectedItem();
        String amountText = expenseAmountField.getText().trim();
        String notes = expenseNotesField.getText().trim();

        double amount;

        try {
            amount = Double.parseDouble(amountText);
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(
                    this,
                    "Please enter a valid number for the expense amount.",
                    "Input Error",
                    JOptionPane.ERROR_MESSAGE
            );
            return;
        }

        expenseTableModel.addRow(new Object[]{date, category, amount, notes});

        showAutoCloseSuccess("Expense saved");
    }

    private void handleUpdateExpense() {
        int selectedViewRow = expenseTable.getSelectedRow();
        if (selectedViewRow < 0) {
            JOptionPane.showMessageDialog(
                    this,
                    "Select a row in the table to update.",
                    "No Selection",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        int row = expenseTable.convertRowIndexToModel(selectedViewRow);

        String date = expenseDateField.getText().trim();
        String category = (String) expenseCategoryCombo.getSelectedItem();
        String amountText = expenseAmountField.getText().trim();
        String notes = expenseNotesField.getText().trim();

        double amount;

        try {
            amount = Double.parseDouble(amountText);
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(
                    this,
                    "Please enter a valid number for the expense amount.",
                    "Input Error",
                    JOptionPane.ERROR_MESSAGE
            );
            return;
        }

        expenseTableModel.setValueAt(date, row, 0);
        expenseTableModel.setValueAt(category, row, 1);
        expenseTableModel.setValueAt(amount, row, 2);
        expenseTableModel.setValueAt(notes, row, 3);

        showAutoCloseSuccess("Expense updated");
    }

    // =========================================================
    //   REPORTS & SETTINGS SCREENS
    // =========================================================

    private JPanel createReportsScreen() {
        return new ReportPanel(); // Uses separate ReportPanel class
    }

    private JPanel createSettingsScreen() {
        JPanel panel = new JPanel();
        JLabel label = new JLabel("Settings - App preferences and (later) login stuff.");
        panel.add(label);
        return panel;
    }

    // =========================================================
    //   TOAST-STYLE AUTO-CLOSE SUCCESS MESSAGE
    // =========================================================

    /**
     * Displays a small green non-modal toast message that auto-closes.
     *
     * @param message Text to display in the toast.
     */
    private void showAutoCloseSuccess(String message) {
        JDialog dialog = new JDialog(this, false);
        dialog.setUndecorated(true);

        JPanel panel = new JPanel();
        panel.setBackground(new Color(46, 139, 87)); // Green for success
        panel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        JLabel label = new JLabel(message);
        label.setForeground(Color.WHITE);
        label.setFont(new Font("SansSerif", Font.PLAIN, 14));

        panel.add(label);
        dialog.add(panel);
        dialog.pack();

        dialog.setLocationRelativeTo(this);
        dialog.setAlwaysOnTop(true);
        dialog.setVisible(true);

        Timer timer = new Timer(1500, e -> {
            dialog.dispose();
            ((Timer) e.getSource()).stop();
        });
        timer.setRepeats(false);
        timer.start();
    }

    // =========================================================
    //   SCREEN SWITCHING
    // =========================================================

    private void showScreen(String name) {
        cardLayout.show(contentPanel, name);
    }

    // =========================================================
    //   MAIN METHOD
    // =========================================================

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            FinanceAppFrame frame = new FinanceAppFrame();
            frame.setVisible(true);
        });
    }
}
