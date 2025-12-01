import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTable;
import javax.swing.JButton;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.border.Border;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import java.util.Objects;

/**
 * Main GUI window for the Driver Finance Tracker application.
 * Left: summary sidebar.
 * Right: main area with navigation and screens
 * (Welcome, Overview, Deliveries, Reports, Settings, Profile).
 */
public class FinanceAppFrame extends JFrame {

    // =========================================================
    //   THEME COLORS AND FONTS
    // =========================================================

    private static final Color COLOR_BG_ROOT = new Color(15, 18, 28);
    private static final Color COLOR_BG_SIDEBAR = new Color(18, 22, 35);
    private static final Color COLOR_BG_MAIN = new Color(24, 28, 43);
    private static final Color COLOR_BG_CARD = new Color(32, 37, 55);
    private static final Color COLOR_BG_INPUT = new Color(39, 45, 63);

    private static final Color COLOR_TEXT_PRIMARY = new Color(235, 239, 255);
    private static final Color COLOR_TEXT_SECONDARY = new Color(156, 163, 175);
    private static final Color COLOR_TEXT_MUTED = new Color(107, 114, 128);

    private static final Color COLOR_ACCENT = new Color(129, 140, 248);    // indigo
    private static final Color COLOR_ACCENT_DARK = new Color(79, 70, 229); // darker indigo
    private static final Color COLOR_BORDER = new Color(55, 65, 81);
    private static final Color COLOR_DIVIDER = new Color(31, 41, 55);
    private static final Color COLOR_SUCCESS = new Color(16, 185, 129);    // green toast

    private Font primaryFont(int style, int size) {
        return new Font("Segoe UI", style, size);
    }

    // =========================================================
    //   LAYOUT FIELDS
    // =========================================================

    private CardLayout cardLayout;
    private JPanel contentPanel;

    // =========================================================
    //   DELIVERY FORM AND TABLE
    // =========================================================

    private JTextField deliveryRestaurantField;
    private JTextField deliveryPayField;
    private JTextField deliveryTipField;
    private JTextField deliveryDateField;
    private JTextField deliveryStartTimeField;
    private JTextField deliveryEndTimeField;
    private JComboBox<String> deliveryPlatformCombo;
    private JComboBox<String> deliveryCarCombo;

    private DefaultTableModel deliveryTableModel;
    private JTable deliveryTable;

    private JLabel deliveriesSummaryLabel;

    // =========================================================
    //   CAR DATA (USED BY SETTINGS + DELIVERIES)
    // =========================================================

    private DefaultTableModel carTableModel;
    private int nextCarId = 1;

    // =========================================================
    //   SIDEBAR LABELS
    // =========================================================

    private JLabel sidebarNetLabel;
    private JLabel sidebarTotalRevenueLabel;
    private JLabel sidebarTotalExpensesLabel;

    // =========================================================
    //   REPORTS LABELS
    // =========================================================

    private JLabel reportTotalDeliveriesLabel;
    private JLabel reportTotalEarningsLabel;
    private JLabel reportAvgPerDeliveryLabel;

    // =========================================================
    //   PROFILE FIELDS (IN-MEMORY ACCOUNT)
    // =========================================================

    private JTextField profileUsernameField;
    private JTextField profilePasswordField; // plain text per request
    private JTextField profileEmailField;

    private JLabel currentProfileLabel;
    private JButton profileSaveButton;

    private String savedUsername = "";
    private String savedPassword = "";
    private String savedEmail    = "";

    // =========================================================
    //   COMMON BORDERS
    // =========================================================

    private Border normalInputBorder; // initialized in createInputField()

    // =========================================================
    //   CONSTRUCTOR
    // =========================================================

    public FinanceAppFrame() {
        setTitle("Driver Finance Tracker");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1100, 650);
        setLocationRelativeTo(null);

        getContentPane().setBackground(COLOR_BG_ROOT);
        setLayout(new BorderLayout());

        initCarTableModel();

        add(createSidebarPanel(), BorderLayout.WEST);
        add(createMainArea(), BorderLayout.CENTER);

        updateSidebarStats();
        updateReportStats();
    }

    // =========================================================
    //   CAR TABLE MODEL (IN-MEMORY CAR STORAGE)
    // =========================================================

    private void initCarTableModel() {
        String[] carColumns = {"Car ID", "Car Name", "MPG"};
        carTableModel = new DefaultTableModel(carColumns, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }

            @Override
            public Class<?> getColumnClass(int col) {
                if (col == 0) return Integer.class;
                if (col == 2) return Double.class;
                return String.class;
            }
        };
    }

    // =========================================================
    //   LEFT SIDEBAR
    // =========================================================

    private JPanel createSidebarPanel() {
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setPreferredSize(new Dimension(260, 0));
        sidebar.setBackground(COLOR_BG_SIDEBAR);
        sidebar.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel appLabel = new JLabel("Driver Finance");
        appLabel.setForeground(COLOR_TEXT_PRIMARY);
        appLabel.setFont(primaryFont(Font.BOLD, 20));
        appLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        sidebar.add(appLabel);

        sidebar.add(Box.createVerticalStrut(10));

        JLabel subtitle = new JLabel("Delivery tracker");
        subtitle.setForeground(COLOR_TEXT_MUTED);
        subtitle.setFont(primaryFont(Font.PLAIN, 12));
        subtitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        sidebar.add(subtitle);

        sidebar.add(Box.createVerticalStrut(20));

        JLabel netTitle = new JLabel("Net Earnings");
        netTitle.setForeground(COLOR_TEXT_SECONDARY);
        netTitle.setFont(primaryFont(Font.PLAIN, 13));
        netTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        sidebar.add(netTitle);

        sidebarNetLabel = new JLabel("$0.00");
        sidebarNetLabel.setForeground(COLOR_TEXT_PRIMARY);
        sidebarNetLabel.setFont(primaryFont(Font.BOLD, 30));
        sidebarNetLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        sidebar.add(sidebarNetLabel);

        sidebar.add(Box.createVerticalStrut(24));

        JSeparator sep1 = new JSeparator();
        sep1.setForeground(COLOR_DIVIDER);
        sep1.setBackground(COLOR_DIVIDER);
        sep1.setAlignmentX(Component.LEFT_ALIGNMENT);
        sidebar.add(sep1);

        sidebar.add(Box.createVerticalStrut(16));

        JLabel totalsTitle = new JLabel("Totals");
        totalsTitle.setForeground(COLOR_TEXT_SECONDARY);
        totalsTitle.setFont(primaryFont(Font.BOLD, 13));
        totalsTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        sidebar.add(totalsTitle);

        sidebar.add(Box.createVerticalStrut(8));

        sidebarTotalRevenueLabel = new JLabel("Earnings: $0.00");
        sidebarTotalRevenueLabel.setForeground(COLOR_TEXT_PRIMARY);
        sidebarTotalRevenueLabel.setFont(primaryFont(Font.PLAIN, 13));
        sidebarTotalRevenueLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        sidebar.add(sidebarTotalRevenueLabel);

        sidebarTotalExpensesLabel = new JLabel("Expenses: $0.00");
        sidebarTotalExpensesLabel.setForeground(COLOR_TEXT_PRIMARY);
        sidebarTotalExpensesLabel.setFont(primaryFont(Font.PLAIN, 13));
        sidebarTotalExpensesLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        sidebar.add(sidebarTotalExpensesLabel);

        sidebar.add(Box.createVerticalStrut(20));

        JSeparator sep2 = new JSeparator();
        sep2.setForeground(COLOR_DIVIDER);
        sep2.setBackground(COLOR_DIVIDER);
        sep2.setAlignmentX(Component.LEFT_ALIGNMENT);
        sidebar.add(sep2);

        sidebar.add(Box.createVerticalStrut(16));

        JLabel quickTitle = new JLabel("Quick action");
        quickTitle.setForeground(COLOR_TEXT_SECONDARY);
        quickTitle.setFont(primaryFont(Font.BOLD, 13));
        quickTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        sidebar.add(quickTitle);

        sidebar.add(Box.createVerticalStrut(10));

        JButton addDeliveryBtn = new JButton("New delivery");
        styleSidebarButton(addDeliveryBtn);
        addDeliveryBtn.addActionListener(e -> showScreen("DELIVERIES"));
        sidebar.add(addDeliveryBtn);

        sidebar.add(Box.createVerticalGlue());

        JLabel footer = new JLabel("v0.1 • Student project");
        footer.setForeground(COLOR_TEXT_MUTED);
        footer.setFont(primaryFont(Font.PLAIN, 11));
        footer.setAlignmentX(Component.LEFT_ALIGNMENT);
        sidebar.add(footer);

        return sidebar;
    }

    private void styleSidebarButton(JButton button) {
        button.setAlignmentX(Component.LEFT_ALIGNMENT);
        button.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        button.setFocusPainted(false);
        button.setFont(primaryFont(Font.PLAIN, 13));
        button.setBackground(COLOR_ACCENT);
        button.setForeground(Color.BLACK);
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(COLOR_ACCENT_DARK),
                BorderFactory.createEmptyBorder(6, 12, 6, 12)
        ));
    }

    // =========================================================
    //   RIGHT MAIN AREA (NAV + CONTENT)
    // =========================================================

    private JPanel createMainArea() {
        JPanel main = new JPanel(new BorderLayout());
        main.setBackground(COLOR_BG_MAIN);

        JPanel navBar = new JPanel(new BorderLayout());
        navBar.setBorder(BorderFactory.createEmptyBorder(16, 20, 10, 20));
        navBar.setBackground(COLOR_BG_MAIN);

        JLabel sectionTitle = new JLabel("Dashboard");
        sectionTitle.setFont(primaryFont(Font.BOLD, 18));
        sectionTitle.setForeground(COLOR_TEXT_PRIMARY);
        navBar.add(sectionTitle, BorderLayout.WEST);

        JPanel navButtonsPanel = new JPanel();
        navButtonsPanel.setOpaque(false);

        JButton welcomeBtn   = createNavButton("Welcome");
        JButton overviewBtn  = createNavButton("Overview");
        JButton deliveriesBtn = createNavButton("Deliveries");
        JButton reportsBtn   = createNavButton("Reports");
        JButton settingsBtn  = createNavButton("Settings");
        JButton profileBtn   = createNavButton("Profile");

        welcomeBtn.addActionListener(e -> showScreen("WELCOME"));
        overviewBtn.addActionListener(e -> showScreen("HOME"));
        deliveriesBtn.addActionListener(e -> showScreen("DELIVERIES"));
        reportsBtn.addActionListener(e -> showScreen("REPORTS"));
        settingsBtn.addActionListener(e -> showScreen("SETTINGS"));
        profileBtn.addActionListener(e -> showScreen("PROFILE"));

        navButtonsPanel.add(welcomeBtn);
        navButtonsPanel.add(overviewBtn);
        navButtonsPanel.add(deliveriesBtn);
        navButtonsPanel.add(reportsBtn);
        navButtonsPanel.add(settingsBtn);
        navButtonsPanel.add(profileBtn);

        navBar.add(navButtonsPanel, BorderLayout.EAST);

        main.add(navBar, BorderLayout.NORTH);

        cardLayout = new CardLayout();
        contentPanel = new JPanel(cardLayout);
        contentPanel.setBorder(BorderFactory.createEmptyBorder(0, 20, 20, 20));
        contentPanel.setBackground(COLOR_BG_MAIN);

        // Important: Welcome screen is added first, so it shows by default.
        contentPanel.add(createWelcomeScreen(), "WELCOME");
        contentPanel.add(createHomeScreen(), "HOME");
        contentPanel.add(createDeliveriesScreen(), "DELIVERIES");
        contentPanel.add(createReportsScreen(), "REPORTS");
        contentPanel.add(createSettingsScreen(), "SETTINGS");
        contentPanel.add(createProfileScreen(), "PROFILE");

        main.add(contentPanel, BorderLayout.CENTER);

        return main;
    }

    private JButton createNavButton(String text) {
        JButton button = new JButton(text);
        button.setFocusPainted(false);
        button.setFont(primaryFont(Font.PLAIN, 13));
        button.setBackground(COLOR_BG_MAIN);
        button.setForeground(COLOR_TEXT_SECONDARY);
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(COLOR_DIVIDER),
                BorderFactory.createEmptyBorder(5, 12, 5, 12)
        ));
        return button;
    }

    // =========================================================
    //   WELCOME SCREEN (SIGN IN / CREATE ACCOUNT)
    // =========================================================

    private JPanel createWelcomeScreen() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);

        JPanel inner = new JPanel();
        inner.setBackground(COLOR_BG_CARD);
        inner.setBorder(BorderFactory.createEmptyBorder(40, 40, 40, 40));
        inner.setLayout(new BoxLayout(inner, BoxLayout.Y_AXIS));

        JLabel title = new JLabel("Welcome to Driver Finance");
        title.setFont(primaryFont(Font.BOLD, 22));
        title.setForeground(COLOR_TEXT_PRIMARY);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel subtitle = new JLabel("Track your delivery earnings, cars, and simple reports.");
        subtitle.setFont(primaryFont(Font.PLAIN, 13));
        subtitle.setForeground(COLOR_TEXT_SECONDARY);
        subtitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        inner.add(title);
        inner.add(Box.createVerticalStrut(8));
        inner.add(subtitle);
        inner.add(Box.createVerticalStrut(24));

        JButton createAccountButton = new JButton("Create account");
        stylePrimaryButton(createAccountButton);
        createAccountButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        createAccountButton.addActionListener(e -> showScreen("PROFILE"));

        JButton signInButton = new JButton("Sign in");
        styleSecondaryButton(signInButton);
        signInButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        signInButton.setToolTipText("Demo sign-in. In a real app this would check your credentials.");

        signInButton.addActionListener(e -> {
            // For this demo:
            // - If we already have a saved profile, go to the dashboard (HOME).
            // - If not, send user to PROFILE so they can "sign in" by creating one.
            if (savedUsername == null || savedUsername.isEmpty()) {
                showScreen("PROFILE");
            } else {
                showScreen("HOME");
            }
        });

        inner.add(createAccountButton);
        inner.add(Box.createVerticalStrut(10));
        inner.add(signInButton);

        inner.add(Box.createVerticalStrut(30));

        JLabel hint = new JLabel("You can also load demo data in the Deliveries tab to showcase the app.");
        hint.setFont(primaryFont(Font.PLAIN, 11));
        hint.setForeground(COLOR_TEXT_MUTED);
        hint.setAlignmentX(Component.CENTER_ALIGNMENT);

        inner.add(hint);

        panel.add(inner, BorderLayout.CENTER);

        return panel;
    }

    // =========================================================
    //   HOME SCREEN (OVERVIEW)
    // =========================================================

    private JPanel createHomeScreen() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);

        JPanel inner = new JPanel(new BorderLayout());
        inner.setBackground(COLOR_BG_CARD);
        inner.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel titleLabel = new JLabel("Overview");
        titleLabel.setFont(primaryFont(Font.BOLD, 18));
        titleLabel.setForeground(COLOR_TEXT_PRIMARY);
        inner.add(titleLabel, BorderLayout.NORTH);

        JPanel statsPanel = new JPanel(new GridLayout(1, 3, 15, 0));
        statsPanel.setBackground(COLOR_BG_CARD);
        statsPanel.setBorder(BorderFactory.createEmptyBorder(20, 0, 10, 0));

        statsPanel.add(createStatCard("Total deliveries", "0"));
        statsPanel.add(createStatCard("Total earnings", "$0.00"));
        statsPanel.add(createStatCard("Net", "$0.00"));

        inner.add(statsPanel, BorderLayout.CENTER);

        JLabel infoLabel = new JLabel("Deliveries and cars are managed in the Deliveries and Settings tabs.");
        infoLabel.setFont(primaryFont(Font.PLAIN, 12));
        infoLabel.setForeground(COLOR_TEXT_MUTED);
        inner.add(infoLabel, BorderLayout.SOUTH);

        panel.add(inner, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createStatCard(String title, String value) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(COLOR_BG_MAIN);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(COLOR_BORDER),
                BorderFactory.createEmptyBorder(12, 12, 12, 12)
        ));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(primaryFont(Font.PLAIN, 13));
        titleLabel.setForeground(COLOR_TEXT_SECONDARY);

        JLabel valueLabel = new JLabel(value);
        valueLabel.setFont(primaryFont(Font.BOLD, 20));
        valueLabel.setForeground(COLOR_TEXT_PRIMARY);

        card.add(titleLabel, BorderLayout.NORTH);
        card.add(valueLabel, BorderLayout.CENTER);

        return card;
    }

    // =========================================================
    //   DELIVERIES SCREEN
    // =========================================================

    private JPanel createDeliveriesScreen() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);

        JPanel inner = new JPanel(new BorderLayout());
        inner.setBackground(COLOR_BG_CARD);
        inner.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel titleLabel = new JLabel("Deliveries");
        titleLabel.setFont(primaryFont(Font.BOLD, 18));
        titleLabel.setForeground(COLOR_TEXT_PRIMARY);
        inner.add(titleLabel, BorderLayout.NORTH);

        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.setBackground(COLOR_BG_CARD);

        // ---------- Form panel ----------
        JPanel formPanel = new JPanel(new GridLayout(4, 4, 10, 10));
        formPanel.setBackground(COLOR_BG_CARD);

        JLabel restaurantLabel = new JLabel("Restaurant");
        deliveryRestaurantField = createInputField();

        JLabel dateLabel = new JLabel("Date (YYYY-MM-DD)");
        deliveryDateField = createInputField();
        deliveryDateField.setText("2025-12-01"); // default for demo

        JLabel startLabel = new JLabel("Start time (e.g. 17:30)");
        deliveryStartTimeField = createInputField();
        deliveryStartTimeField.setText("17:00");

        JLabel endLabel = new JLabel("End time (e.g. 18:15)");
        deliveryEndTimeField = createInputField();
        deliveryEndTimeField.setText("18:00");

        JLabel payLabel = new JLabel("Pay ($)");
        deliveryPayField = createInputField();

        JLabel tipLabel = new JLabel("Tip ($)");
        deliveryTipField = createInputField();

        JLabel platformLabel = new JLabel("Platform");
        String[] platforms = {"DoorDash", "Uber Eats", "Grubhub", "Postmates", "Other"};
        deliveryPlatformCombo = createComboBox(platforms);
        deliveryPlatformCombo.setToolTipText("Select which delivery app this delivery came from.");

        JLabel carLabel = new JLabel("Car used");
        deliveryCarCombo = new JComboBox<>(new DefaultComboBoxModel<>());
        styleComboBox(deliveryCarCombo);
        deliveryCarCombo.setToolTipText("Cars are created in Settings and can be selected here.");

        styleFormLabel(restaurantLabel);
        styleFormLabel(dateLabel);
        styleFormLabel(startLabel);
        styleFormLabel(endLabel);
        styleFormLabel(payLabel);
        styleFormLabel(tipLabel);
        styleFormLabel(platformLabel);
        styleFormLabel(carLabel);

        formPanel.add(restaurantLabel);
        formPanel.add(deliveryRestaurantField);
        formPanel.add(dateLabel);
        formPanel.add(deliveryDateField);

        formPanel.add(startLabel);
        formPanel.add(deliveryStartTimeField);
        formPanel.add(endLabel);
        formPanel.add(deliveryEndTimeField);

        formPanel.add(payLabel);
        formPanel.add(deliveryPayField);
        formPanel.add(tipLabel);
        formPanel.add(deliveryTipField);

        formPanel.add(platformLabel);
        formPanel.add(deliveryPlatformCombo);
        formPanel.add(carLabel);
        formPanel.add(deliveryCarCombo);

        JPanel topBox = new JPanel();
        topBox.setBackground(COLOR_BG_CARD);
        topBox.setLayout(new BoxLayout(topBox, BoxLayout.Y_AXIS));
        topBox.add(formPanel);
        topBox.add(Box.createVerticalStrut(12));

        JLabel pastLabel = new JLabel("Past deliveries");
        pastLabel.setFont(primaryFont(Font.BOLD, 13));
        pastLabel.setForeground(COLOR_TEXT_SECONDARY);
        pastLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        topBox.add(pastLabel);
        topBox.add(Box.createVerticalStrut(4));

        centerPanel.add(topBox, BorderLayout.NORTH);

        // ---------- Deliveries table ----------
        String[] deliveryColumns = {
                "Date", "Start", "End", "Restaurant", "Platform",
                "Car", "Pay", "Tip", "Total"
        };
        deliveryTableModel = new DefaultTableModel(deliveryColumns, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }

            @Override
            public Class<?> getColumnClass(int col) {
                if (col >= 6) return Double.class;
                return String.class;
            }
        };

        deliveryTable = new JTable(deliveryTableModel);
        deliveryTable.setFillsViewportHeight(true);
        deliveryTable.setRowHeight(22);
        deliveryTable.setBackground(COLOR_BG_MAIN);
        deliveryTable.setForeground(COLOR_TEXT_PRIMARY);
        deliveryTable.setGridColor(COLOR_DIVIDER);
        deliveryTable.setSelectionBackground(COLOR_ACCENT_DARK);
        deliveryTable.setSelectionForeground(COLOR_TEXT_PRIMARY);
        deliveryTable.setFont(primaryFont(Font.PLAIN, 12));
        deliveryTable.setAutoCreateRowSorter(true);

        JTableHeader header = deliveryTable.getTableHeader();
        header.setBackground(COLOR_BG_CARD);
        header.setForeground(COLOR_TEXT_SECONDARY);
        header.setFont(primaryFont(Font.PLAIN, 12));
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, COLOR_DIVIDER));

        // Right-align numeric columns
        DefaultTableCellRenderer rightRenderer = new DefaultTableCellRenderer();
        rightRenderer.setHorizontalAlignment(SwingConstants.RIGHT);
        deliveryTable.getColumnModel().getColumn(6).setCellRenderer(rightRenderer);
        deliveryTable.getColumnModel().getColumn(7).setCellRenderer(rightRenderer);
        deliveryTable.getColumnModel().getColumn(8).setCellRenderer(rightRenderer);

        // Column widths
        deliveryTable.getColumnModel().getColumn(0).setPreferredWidth(90);  // Date
        deliveryTable.getColumnModel().getColumn(1).setPreferredWidth(70);  // Start
        deliveryTable.getColumnModel().getColumn(2).setPreferredWidth(70);  // End
        deliveryTable.getColumnModel().getColumn(3).setPreferredWidth(140); // Restaurant
        deliveryTable.getColumnModel().getColumn(4).setPreferredWidth(100); // Platform
        deliveryTable.getColumnModel().getColumn(5).setPreferredWidth(130); // Car
        deliveryTable.getColumnModel().getColumn(6).setPreferredWidth(70);  // Pay
        deliveryTable.getColumnModel().getColumn(7).setPreferredWidth(70);  // Tip
        deliveryTable.getColumnModel().getColumn(8).setPreferredWidth(80);  // Total

        deliveryTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int viewRow = deliveryTable.getSelectedRow();
                if (viewRow >= 0) {
                    int modelRow = deliveryTable.convertRowIndexToModel(viewRow);
                    deliveryDateField.setText(Objects.toString(deliveryTableModel.getValueAt(modelRow, 0), ""));
                    deliveryStartTimeField.setText(Objects.toString(deliveryTableModel.getValueAt(modelRow, 1), ""));
                    deliveryEndTimeField.setText(Objects.toString(deliveryTableModel.getValueAt(modelRow, 2), ""));
                    deliveryRestaurantField.setText(Objects.toString(deliveryTableModel.getValueAt(modelRow, 3), ""));
                    deliveryPlatformCombo.setSelectedItem(Objects.toString(deliveryTableModel.getValueAt(modelRow, 4), ""));
                    deliveryCarCombo.setSelectedItem(Objects.toString(deliveryTableModel.getValueAt(modelRow, 5), ""));
                    deliveryPayField.setText(Objects.toString(deliveryTableModel.getValueAt(modelRow, 6), ""));
                    deliveryTipField.setText(Objects.toString(deliveryTableModel.getValueAt(modelRow, 7), ""));
                }
            }
        });

        JScrollPane tableScroll = new JScrollPane(deliveryTable);
        tableScroll.getViewport().setBackground(COLOR_BG_MAIN);
        tableScroll.setBorder(BorderFactory.createLineBorder(COLOR_BORDER));

        // Summary label under table
        deliveriesSummaryLabel = new JLabel("0 deliveries \u2022 Total: $0.00");
        deliveriesSummaryLabel.setFont(primaryFont(Font.PLAIN, 12));
        deliveriesSummaryLabel.setForeground(COLOR_TEXT_SECONDARY);

        JPanel tableBox = new JPanel(new BorderLayout());
        tableBox.setBackground(COLOR_BG_CARD);
        tableBox.add(tableScroll, BorderLayout.CENTER);
        tableBox.add(deliveriesSummaryLabel, BorderLayout.SOUTH);

        centerPanel.add(tableBox, BorderLayout.CENTER);

        inner.add(centerPanel, BorderLayout.CENTER);

        // ---------- Bottom buttons ----------
        JPanel bottomPanel = new JPanel();
        bottomPanel.setBackground(COLOR_BG_CARD);

        JButton saveButton = new JButton("Save new delivery");
        JButton updateButton = new JButton("Update selected");
        JButton clearFormButton = new JButton("Clear form");
        JButton demoDataButton = new JButton("Load demo data");

        stylePrimaryButton(saveButton);
        styleSecondaryButton(updateButton);
        styleSecondaryButton(clearFormButton);
        styleSecondaryButton(demoDataButton);

        clearFormButton.setToolTipText("Clear all delivery input fields.");
        demoDataButton.setToolTipText("Load sample deliveries for demo.");

        saveButton.addActionListener(e -> handleSaveDelivery());
        updateButton.addActionListener(e -> handleUpdateDelivery());
        clearFormButton.addActionListener(e -> clearDeliveryForm());
        demoDataButton.addActionListener(e -> loadDemoDeliveries());

        bottomPanel.add(saveButton);
        bottomPanel.add(updateButton);
        bottomPanel.add(clearFormButton);
        bottomPanel.add(demoDataButton);

        inner.add(bottomPanel, BorderLayout.SOUTH);

        panel.add(inner, BorderLayout.CENTER);

        return panel;
    }

    private void handleSaveDelivery() {
        if (!validateDeliveryInputs()) {
            return;
        }

        String restaurant = deliveryRestaurantField.getText().trim();
        String date = deliveryDateField.getText().trim();
        String start = deliveryStartTimeField.getText().trim();
        String end = deliveryEndTimeField.getText().trim();
        String platform = (String) deliveryPlatformCombo.getSelectedItem();
        String carDisplay = (String) deliveryCarCombo.getSelectedItem();

        double pay = Double.parseDouble(deliveryPayField.getText().trim());
        double tip = Double.parseDouble(deliveryTipField.getText().trim());
        double total = pay + tip;

        deliveryTableModel.addRow(new Object[]{
                date, start, end, restaurant, platform, carDisplay, pay, tip, total
        });

        updateSidebarStats();
        updateReportStats();
        showAutoCloseSuccess("Delivery saved");
    }

    private void handleUpdateDelivery() {
        int selectedViewRow = deliveryTable.getSelectedRow();
        if (selectedViewRow < 0) {
            JOptionPane.showMessageDialog(
                    this,
                    "Select a delivery in the table to update.",
                    "No selection",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        if (!validateDeliveryInputs()) {
            return;
        }

        int row = deliveryTable.convertRowIndexToModel(selectedViewRow);

        String restaurant = deliveryRestaurantField.getText().trim();
        String date = deliveryDateField.getText().trim();
        String start = deliveryStartTimeField.getText().trim();
        String end = deliveryEndTimeField.getText().trim();
        String platform = (String) deliveryPlatformCombo.getSelectedItem();
        String carDisplay = (String) deliveryCarCombo.getSelectedItem();

        double pay = Double.parseDouble(deliveryPayField.getText().trim());
        double tip = Double.parseDouble(deliveryTipField.getText().trim());
        double total = pay + tip;

        deliveryTableModel.setValueAt(date, row, 0);
        deliveryTableModel.setValueAt(start, row, 1);
        deliveryTableModel.setValueAt(end, row, 2);
        deliveryTableModel.setValueAt(restaurant, row, 3);
        deliveryTableModel.setValueAt(platform, row, 4);
        deliveryTableModel.setValueAt(carDisplay, row, 5);
        deliveryTableModel.setValueAt(pay, row, 6);
        deliveryTableModel.setValueAt(tip, row, 7);
        deliveryTableModel.setValueAt(total, row, 8);

        updateSidebarStats();
        updateReportStats();
        showAutoCloseSuccess("Delivery updated");
    }

    private boolean validateDeliveryInputs() {
        // reset borders first
        resetDeliveryFieldBorders();

        String payText = deliveryPayField.getText().trim();
        String tipText = deliveryTipField.getText().trim();

        boolean ok = true;

        try {
            Double.parseDouble(payText);
        } catch (NumberFormatException ex) {
            deliveryPayField.setBorder(errorBorder());
            ok = false;
        }

        try {
            Double.parseDouble(tipText);
        } catch (NumberFormatException ex) {
            deliveryTipField.setBorder(errorBorder());
            ok = false;
        }

        if (!ok) {
            JOptionPane.showMessageDialog(
                    this,
                    "Enter valid numbers for pay and tip.",
                    "Input error",
                    JOptionPane.ERROR_MESSAGE
            );
            return false;
        }

        if (deliveryCarCombo.getItemCount() == 0) {
            JOptionPane.showMessageDialog(
                    this,
                    "Add at least one car in Settings and select it before saving a delivery.",
                    "Car required",
                    JOptionPane.WARNING_MESSAGE
            );
            return false;
        }

        if (deliveryCarCombo.getSelectedItem() == null) {
            JOptionPane.showMessageDialog(
                    this,
                    "Select a car for this delivery.",
                    "Car required",
                    JOptionPane.WARNING_MESSAGE
            );
            return false;
        }

        return true;
    }

    private void resetDeliveryFieldBorders() {
        if (normalInputBorder == null) return;
        deliveryPayField.setBorder(normalInputBorder);
        deliveryTipField.setBorder(normalInputBorder);
    }

    private Border errorBorder() {
        return BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.RED),
                BorderFactory.createEmptyBorder(4, 6, 4, 6)
        );
    }

    private void clearDeliveryForm() {
        deliveryRestaurantField.setText("");
        deliveryDateField.setText("");
        deliveryStartTimeField.setText("");
        deliveryEndTimeField.setText("");
        deliveryPayField.setText("");
        deliveryTipField.setText("");
        if (deliveryPlatformCombo.getItemCount() > 0) {
            deliveryPlatformCombo.setSelectedIndex(0);
        }
        if (deliveryCarCombo.getItemCount() > 0) {
            deliveryCarCombo.setSelectedIndex(0);
        }
        resetDeliveryFieldBorders();
    }

    private void loadDemoDeliveries() {
        // Ensure at least one car exists
        if (deliveryCarCombo.getItemCount() == 0) {
            int carId = nextCarId++;
            String carName = "Demo Car";
            double mpg = 30.0;
            carTableModel.addRow(new Object[]{carId, carName, mpg});
            ((DefaultComboBoxModel<String>) deliveryCarCombo.getModel())
                    .addElement(carId + " - " + carName);
        }

        String carDisplay = (String) deliveryCarCombo.getItemAt(0);

        // Clear existing rows
        deliveryTableModel.setRowCount(0);

        // Add some sample rows
        Object[][] demoRows = {
                {"2025-11-28", "17:00", "17:30", "McDonald's", "DoorDash", carDisplay, 8.50, 3.75, 12.25},
                {"2025-11-28", "17:40", "18:05", "Chipotle",  "Uber Eats", carDisplay, 6.75, 4.25, 11.00},
                {"2025-11-29", "12:10", "12:35", "Taco Bell", "Grubhub",   carDisplay, 5.25, 2.50, 7.75},
                {"2025-11-29", "13:00", "13:30", "Panda Express", "DoorDash", carDisplay, 7.80, 3.20, 11.00},
                {"2025-11-30", "19:00", "19:40", "Local Pizza", "Other",    carDisplay, 9.00, 5.00, 14.00}
        };

        for (Object[] row : demoRows) {
            deliveryTableModel.addRow(row);
        }

        updateSidebarStats();
        updateReportStats();
        showAutoCloseSuccess("Demo data loaded");
    }

    // =========================================================
    //   SETTINGS SCREEN (CAR FORM, COMPACT)
    // =========================================================

    private JPanel createSettingsScreen() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);

        JPanel inner = new JPanel(new BorderLayout());
        inner.setBackground(COLOR_BG_CARD);
        inner.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel title = new JLabel("Settings");
        title.setFont(primaryFont(Font.BOLD, 18));
        title.setForeground(COLOR_TEXT_PRIMARY);
        inner.add(title, BorderLayout.NORTH);

        JPanel center = new JPanel();
        center.setBackground(COLOR_BG_CARD);
        center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));

        JLabel desc = new JLabel("Add cars on file. Saved cars are available in the Deliveries tab.");
        desc.setFont(primaryFont(Font.PLAIN, 12));
        desc.setForeground(COLOR_TEXT_SECONDARY);
        desc.setAlignmentX(Component.LEFT_ALIGNMENT);
        center.add(desc);

        center.add(Box.createVerticalStrut(15));

        JPanel card = new JPanel(new GridLayout(2, 2, 10, 8));
        card.setBackground(COLOR_BG_MAIN);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(COLOR_BORDER),
                BorderFactory.createEmptyBorder(12, 12, 12, 12)
        ));
        card.setMaximumSize(new Dimension(420, 90));
        card.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel nameLabel = new JLabel("Car name");
        JTextField carNameField = createInputField();

        JLabel mpgLabel = new JLabel("MPG");
        JTextField carMpgField = createInputField();

        styleFormLabel(nameLabel);
        styleFormLabel(mpgLabel);

        card.add(nameLabel);
        card.add(carNameField);
        card.add(mpgLabel);
        card.add(carMpgField);

        center.add(card);
        center.add(Box.createVerticalStrut(10));

        JButton addCarButton = new JButton("Add car");
        addCarButton.setAlignmentX(Component.LEFT_ALIGNMENT);
        stylePrimaryButton(addCarButton);

        addCarButton.addActionListener(e -> {
            String carName = carNameField.getText().trim();
            String mpgText = carMpgField.getText().trim();

            if (carName.isEmpty()) {
                JOptionPane.showMessageDialog(
                        this,
                        "Enter a car name.",
                        "Input error",
                        JOptionPane.ERROR_MESSAGE
                );
                return;
            }

            double mpg;
            try {
                mpg = Double.parseDouble(mpgText);
            } catch (NumberFormatException ex) {
                carMpgField.setBorder(errorBorder());
                JOptionPane.showMessageDialog(
                        this,
                        "Enter a valid number for MPG.",
                        "Input error",
                        JOptionPane.ERROR_MESSAGE
                );
                return;
            }

            int carId = nextCarId++;
            carTableModel.addRow(new Object[]{carId, carName, mpg});

            String displayString = carId + " - " + carName;
            ((DefaultComboBoxModel<String>) deliveryCarCombo.getModel()).addElement(displayString);

            carNameField.setText("");
            carMpgField.setText("");
            carMpgField.setBorder(normalInputBorder);

            showAutoCloseSuccess("Car added");
        });

        center.add(addCarButton);

        inner.add(center, BorderLayout.CENTER);

        panel.add(inner, BorderLayout.CENTER);
        return panel;
    }

    // =========================================================
    //   PROFILE SCREEN (USERNAME, PASSWORD, EMAIL)
    // =========================================================

    private JPanel createProfileScreen() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);

        JPanel inner = new JPanel(new BorderLayout());
        inner.setBackground(COLOR_BG_CARD);
        inner.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel title = new JLabel("Profile");
        title.setFont(primaryFont(Font.BOLD, 18));
        title.setForeground(COLOR_TEXT_PRIMARY);
        inner.add(title, BorderLayout.NORTH);

        JPanel center = new JPanel();
        center.setBackground(COLOR_BG_CARD);
        center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));

        JLabel desc = new JLabel("Create or update your account details (stored locally for this demo).");
        desc.setFont(primaryFont(Font.PLAIN, 12));
        desc.setForeground(COLOR_TEXT_SECONDARY);
        desc.setAlignmentX(Component.LEFT_ALIGNMENT);
        center.add(desc);

        center.add(Box.createVerticalStrut(8));

        currentProfileLabel = new JLabel(getProfileSummaryText());
        currentProfileLabel.setFont(primaryFont(Font.PLAIN, 12));
        currentProfileLabel.setForeground(COLOR_TEXT_MUTED);
        currentProfileLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        center.add(currentProfileLabel);

        center.add(Box.createVerticalStrut(15));

        JPanel card = new JPanel(new GridLayout(3, 2, 10, 8));
        card.setBackground(COLOR_BG_MAIN);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(COLOR_BORDER),
                BorderFactory.createEmptyBorder(12, 12, 12, 12)
        ));
        card.setMaximumSize(new Dimension(420, 120));
        card.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel userLabel = new JLabel("Username");
        JLabel passLabel = new JLabel("Password");
        JLabel emailLabel = new JLabel("Email");

        styleFormLabel(userLabel);
        styleFormLabel(passLabel);
        styleFormLabel(emailLabel);

        profileUsernameField = createInputField();
        profilePasswordField = createInputField(); // plain text
        profileEmailField    = createInputField();
        profileEmailField.setToolTipText("Enter a valid email address (demo only, no real auth).");

        if (!savedUsername.isEmpty()) {
            profileUsernameField.setText(savedUsername);
        }
        if (!savedPassword.isEmpty()) {
            profilePasswordField.setText(savedPassword);
        }
        if (!savedEmail.isEmpty()) {
            profileEmailField.setText(savedEmail);
        }

        card.add(userLabel);
        card.add(profileUsernameField);
        card.add(passLabel);
        card.add(profilePasswordField);
        card.add(emailLabel);
        card.add(profileEmailField);

        center.add(card);
        center.add(Box.createVerticalStrut(10));

        profileSaveButton = new JButton("Save profile");
        profileSaveButton.setAlignmentX(Component.LEFT_ALIGNMENT);
        stylePrimaryButton(profileSaveButton);

        profileSaveButton.addActionListener(e -> handleSaveProfile());

        center.add(profileSaveButton);

        inner.add(center, BorderLayout.CENTER);

        panel.add(inner, BorderLayout.CENTER);
        return panel;
    }

    private String getProfileSummaryText() {
        if (savedUsername == null || savedUsername.isEmpty()) {
            return "No profile saved yet.";
        }
        if (savedEmail == null || savedEmail.isEmpty()) {
            return "Current profile: " + savedUsername;
        }
        return "Current profile: " + savedUsername + " (" + savedEmail + ")";
    }

    private void handleSaveProfile() {
        // reset email border
        if (normalInputBorder != null) {
            profileEmailField.setBorder(normalInputBorder);
        }

        String username = profileUsernameField.getText().trim();
        String password = profilePasswordField.getText().trim();
        String email    = profileEmailField.getText().trim();

        if (username.isEmpty() || password.isEmpty() || email.isEmpty()) {
            JOptionPane.showMessageDialog(
                    this,
                    "Username, password, and email are required.",
                    "Input error",
                    JOptionPane.ERROR_MESSAGE
            );
            return;
        }

        if (!email.contains("@") || !email.contains(".")) {
            profileEmailField.setBorder(errorBorder());
            JOptionPane.showMessageDialog(
                    this,
                    "Enter a valid email address.",
                    "Input error",
                    JOptionPane.ERROR_MESSAGE
            );
            return;
        }

        savedUsername = username;
        savedPassword = password; // plain text for demo
        savedEmail    = email;

        if (currentProfileLabel != null) {
            currentProfileLabel.setText(getProfileSummaryText());
        }

        showAutoCloseSuccess("Profile saved");

        if (profileSaveButton != null) {
            profileSaveButton.setText("Saved");
            profileSaveButton.setEnabled(false);

            Timer t = new Timer(1500, e -> {
                profileSaveButton.setText("Save profile");
                profileSaveButton.setEnabled(true);
                ((Timer) e.getSource()).stop();
            });
            t.setRepeats(false);
            t.start();
        }
    }

    // =========================================================
    //   REPORTS SCREEN
    // =========================================================

    private JPanel createReportsScreen() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);

        JPanel inner = new JPanel(new BorderLayout());
        inner.setBackground(COLOR_BG_CARD);
        inner.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel title = new JLabel("Reports");
        title.setFont(primaryFont(Font.BOLD, 18));
        title.setForeground(COLOR_TEXT_PRIMARY);
        inner.add(title, BorderLayout.NORTH);

        JPanel center = new JPanel(new BorderLayout());
        center.setBackground(COLOR_BG_CARD);

        JPanel statsPanel = new JPanel(new GridLayout(1, 3, 15, 0));
        statsPanel.setBackground(COLOR_BG_CARD);
        statsPanel.setBorder(BorderFactory.createEmptyBorder(20, 0, 10, 0));

        reportTotalDeliveriesLabel = new JLabel("0");
        reportTotalEarningsLabel = new JLabel("$0.00");
        reportAvgPerDeliveryLabel = new JLabel("$0.00");

        statsPanel.add(createMetricCard("Total deliveries", reportTotalDeliveriesLabel));
        statsPanel.add(createMetricCard("Total earnings", reportTotalEarningsLabel));
        statsPanel.add(createMetricCard("Avg per delivery", reportAvgPerDeliveryLabel));

        center.add(statsPanel, BorderLayout.CENTER);

        JPanel bottom = new JPanel();
        bottom.setBackground(COLOR_BG_CARD);

        JButton refreshButton = new JButton("Recalculate from deliveries");
        styleSecondaryButton(refreshButton);
        refreshButton.setToolTipText("Recalculate summary stats from all current deliveries.");
        refreshButton.addActionListener(e -> {
            updateReportStats();
            showAutoCloseSuccess("Report updated");
        });

        bottom.add(refreshButton);

        inner.add(center, BorderLayout.CENTER);
        inner.add(bottom, BorderLayout.SOUTH);

        panel.add(inner, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createMetricCard(String title, JLabel valueLabel) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(COLOR_BG_MAIN);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(COLOR_BORDER),
                BorderFactory.createEmptyBorder(12, 12, 12, 12)
        ));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(primaryFont(Font.PLAIN, 13));
        titleLabel.setForeground(COLOR_TEXT_SECONDARY);

        valueLabel.setFont(primaryFont(Font.BOLD, 20));
        valueLabel.setForeground(COLOR_TEXT_PRIMARY);

        card.add(titleLabel, BorderLayout.NORTH);
        card.add(valueLabel, BorderLayout.CENTER);

        return card;
    }

    private void updateReportStats() {
        int deliveries = 0;
        double totalEarnings = 0.0;

        if (deliveryTableModel != null) {
            deliveries = deliveryTableModel.getRowCount();
            for (int i = 0; i < deliveries; i++) {
                Object val = deliveryTableModel.getValueAt(i, 8); // Total column
                if (val instanceof Number) {
                    totalEarnings += ((Number) val).doubleValue();
                } else if (val != null) {
                    try {
                        totalEarnings += Double.parseDouble(val.toString());
                    } catch (NumberFormatException ignored) {}
                }
            }
        }

        double avg = deliveries > 0 ? totalEarnings / deliveries : 0.0;

        if (reportTotalDeliveriesLabel != null) {
            reportTotalDeliveriesLabel.setText(String.valueOf(deliveries));
        }
        if (reportTotalEarningsLabel != null) {
            reportTotalEarningsLabel.setText(String.format("$%.2f", totalEarnings));
        }
        if (reportAvgPerDeliveryLabel != null) {
            reportAvgPerDeliveryLabel.setText(String.format("$%.2f", avg));
        }
    }

    // =========================================================
    //   SIDEBAR TOTALS UPDATE + DELIVERY SUMMARY
    // =========================================================

    private void updateSidebarStats() {
        double totalEarnings = 0.0;
        int deliveries = 0;

        if (deliveryTableModel != null) {
            deliveries = deliveryTableModel.getRowCount();
            for (int i = 0; i < deliveries; i++) {
                Object val = deliveryTableModel.getValueAt(i, 8);
                if (val instanceof Number) {
                    totalEarnings += ((Number) val).doubleValue();
                } else if (val != null) {
                    try {
                        totalEarnings += Double.parseDouble(val.toString());
                    } catch (NumberFormatException ignored) {}
                }
            }
        }

        double expenses = 0.0; // placeholder for future expense integration
        double net = totalEarnings - expenses;

        sidebarTotalRevenueLabel.setText(String.format("Earnings: $%.2f", totalEarnings));
        sidebarTotalExpensesLabel.setText(String.format("Expenses: $%.2f", expenses));
        sidebarNetLabel.setText(String.format("$%.2f", net));

        if (deliveriesSummaryLabel != null) {
            deliveriesSummaryLabel.setText(
                    String.format("%d deliveries \u2022 Total: $%.2f", deliveries, totalEarnings)
            );
        }
    }

    // =========================================================
    //   TOAST-STYLE SUCCESS MESSAGE
    // =========================================================

    private void showAutoCloseSuccess(String message) {
        JDialog dialog = new JDialog(this, false);
        dialog.setUndecorated(true);

        JPanel panel = new JPanel();
        panel.setBackground(COLOR_SUCCESS);
        panel.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));

        JLabel label = new JLabel(message);
        label.setForeground(Color.WHITE);
        label.setFont(primaryFont(Font.PLAIN, 13));
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
    //   SMALL UI HELPERS
    // =========================================================

    private JTextField createInputField() {
        if (normalInputBorder == null) {
            normalInputBorder = BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(COLOR_BORDER),
                    BorderFactory.createEmptyBorder(4, 6, 4, 6)
            );
        }
        JTextField field = new JTextField();
        field.setBackground(COLOR_BG_INPUT);
        field.setForeground(COLOR_TEXT_PRIMARY);
        field.setCaretColor(COLOR_TEXT_PRIMARY);
        field.setBorder(normalInputBorder);
        field.setFont(primaryFont(Font.PLAIN, 12));
        return field;
    }

    private JComboBox<String> createComboBox(String[] items) {
        JComboBox<String> combo = new JComboBox<>(items);
        styleComboBox(combo);
        return combo;
    }

    private void styleComboBox(JComboBox<?> combo) {
        combo.setBackground(COLOR_BG_INPUT);
        combo.setForeground(COLOR_TEXT_PRIMARY);
        combo.setFont(primaryFont(Font.PLAIN, 12));
        combo.setBorder(BorderFactory.createLineBorder(COLOR_BORDER));
    }

    private void styleFormLabel(JLabel label) {
        label.setFont(primaryFont(Font.PLAIN, 12));
        label.setForeground(COLOR_TEXT_SECONDARY);
    }

    private void stylePrimaryButton(JButton button) {
        button.setFocusPainted(false);
        button.setFont(primaryFont(Font.PLAIN, 13));
        button.setBackground(COLOR_ACCENT);
        button.setForeground(Color.BLACK);
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(COLOR_ACCENT_DARK),
                BorderFactory.createEmptyBorder(6, 12, 6, 12)
        ));
    }

    private void styleSecondaryButton(JButton button) {
        button.setFocusPainted(false);
        button.setFont(primaryFont(Font.PLAIN, 13));
        button.setBackground(COLOR_BG_MAIN);
        button.setForeground(COLOR_TEXT_SECONDARY);
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(COLOR_BORDER),
                BorderFactory.createEmptyBorder(6, 12, 6, 12)
        ));
    }

    // =========================================================
    //   SCREEN SWITCHING
    // =========================================================

    private void showScreen(String name) {
        cardLayout.show(contentPanel, name);
    }

    // =========================================================
    //   MAIN
    // =========================================================

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            FinanceAppFrame frame = new FinanceAppFrame();
            frame.setVisible(true);
        });
    }
}
