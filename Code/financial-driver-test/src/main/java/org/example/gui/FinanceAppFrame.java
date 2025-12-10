package org.example.gui;

import org.example.deliveryRecorder.src.overviewService;
import org.example.driverFinancialServiceDispatcher.serviceDispatcher;

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
import javax.swing.JTabbedPane;
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
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Main GUI window for the Driver Finance Tracker application.
 * Left: summary sidebar.
 * Right: main area with navigation and screens
 * (Account, Overview, Deliveries, Reports, Settings).
 */
public class FinanceAppFrame extends JFrame {

    // =========================================================
    //   THEME COLORS, FONTS, CONSTANTS
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

    // Approximate average gas price in California (USD per gallon)
    private static final double GAS_PRICE_CA = 4.80;

    // Default MPG values for different vehicle types
    private static final double DEFAULT_CAR_MPG = 28.0;        // Average car MPG
    private static final double DEFAULT_MOTORCYCLE_MPG = 50.0; // Average motorcycle MPG

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
    // Delivery date dropdowns
    private JComboBox<Integer> deliveryDateYearCombo;
    private JComboBox<Integer> deliveryDateMonthCombo;
    private JComboBox<Integer> deliveryDateDayCombo;
    private JTextField deliveryStartTimeField;
    private JTextField deliveryEndTimeField;
    private JTextField deliveryMilesField;
    private JTextField deliveryFromAddressField;
    private JTextField deliveryToAddressField;
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
    private JComboBox<String> defaultVehicleCombo; // For settings tab default vehicle selection

    // =========================================================
    //   SIDEBAR LABELS
    // =========================================================

    private JLabel sidebarNetLabel;
    private JLabel sidebarTotalRevenueLabel;
    private JLabel sidebarTotalExpensesLabel;
    private JLabel sidebarCurrentVehicleLabel;

    // =========================================================
    //   REPORTS LABELS & CHARTS
    // =========================================================

    private JLabel reportTotalDeliveriesLabel;
    private JLabel reportTotalEarningsLabel;
    private JLabel reportAvgPerDeliveryLabel;
    private JLabel reportGasCostLabel;
    private JLabel reportTopRestaurantLabel;
    private JLabel reportTopPlatformLabel;
    private JLabel reportTotalMilesLabel;

    // Report date dropdowns (start date)
    private JComboBox<Integer> reportStartYearCombo;
    private JComboBox<Integer> reportStartMonthCombo;
    private JComboBox<Integer> reportStartDayCombo;
    // Report date dropdowns (end date)
    private JComboBox<Integer> reportEndYearCombo;
    private JComboBox<Integer> reportEndMonthCombo;
    private JComboBox<Integer> reportEndDayCombo;

    private BarChartPanel platformChart;
    private BarChartPanel dailyChart;

    // =========================================================
    //   ACCOUNT / PROFILE FIELDS (IN-MEMORY)
    // =========================================================

    private JTextField profileUsernameField;
    private JTextField profilePasswordField; // plain text per request
    private JTextField profileEmailField;

    private JLabel accountStatusLabel;
    private JButton profileSaveButton;

    // Login text fields on Account screen (so we can clear them)
    private JTextField accountLoginUserField;
    private JTextField accountLoginPassField;

    // "Fake" account stored only in memory for this run
    private String savedUsername = "";
    private String savedPassword = "";
    private String savedEmail    = "";
    private boolean isLoggedIn   = false;

    // =========================================================
    //   HOME / OVERVIEW LABELS
    // =========================================================

    private JLabel homeWelcomeLabel;
    private JLabel homeTotalDeliveriesValueLabel;
    private JLabel homeTotalEarningsValueLabel;
    private JLabel homeNetValueLabel;
    private JLabel homeActivitySummaryLabel;
    private JLabel homeTopRestaurantLabel;
    private JLabel homeTopPlatformLabel;

    // =========================================================
    //   BANK ACCOUNT TAB FIELDS
    // =========================================================

    private DefaultTableModel bankAccountTableModel;
    private DefaultTableModel bankTransactionTableModel;
    private JLabel bankAccountIdValue;
    private JLabel bankAccountNameValue;
    private JLabel bankAccountTypeValue;
    private JLabel bankAccountBalanceValue;
    private JLabel bankTotalIncomeLabel;
    private JLabel bankTotalExpensesLabel;
    private JLabel bankNetLabel;
    private JLabel bankTransactionCountLabel;

    // Summary tab fields
    private DefaultTableModel summaryTransactionTableModel;
    private JLabel summaryAssetsValueLabel;
    private JLabel summaryIncomeValueLabel;
    private JLabel summaryExpensesValueLabel;
    private JLabel summaryNetValueLabel;
    private JLabel summaryTransactionCountLabel;

    // Range toggle buttons on the home screen
    private JButton homeTodayButton;
    private JButton homeWeekButton;
    private JButton homeMonthButton;
    private JButton homeAllButton;

    private void createUIComponents() {
        // TODO: place custom component creation code here
    }

    // =========================================================
    //   HOME OVERVIEW RANGE
    // =========================================================
    private enum HomeRange { TODAY, WEEK, MONTH, ALL }
    private HomeRange homeRange = HomeRange.ALL;

    // =========================================================
    //   COMMON BORDERS
    // =========================================================

    private Border normalInputBorder; // initialized in createInputField()

    // =========================================================
    //   SERVICE DISPATCHER (SPRING SERVICES BRIDGE)
    // =========================================================

    private serviceDispatcher.ServiceDispatcher serviceDispatcher;

    // =========================================================
    //   WORK PERIOD TRACKING
    // =========================================================

    private long currentWorkPeriodId = -1;
    private JButton workPeriodButton;
    private JLabel workPeriodStatusLabel;

    // Add root panel for IntelliJ GUI Designer binding
    private JPanel rootPanel;
    private JPanel sidebarPanel; // designer placeholder binding

    /**
     * Returns the root component used by the IntelliJ GUI Designer (.form).
     * When editing in the Designer, fields will be bound to the names declared in the .form file.
     */
    public javax.swing.JComponent getRootComponent() {
        return rootPanel;
    }

    /**
     * Sets the ServiceDispatcher to connect Spring services to this GUI.
     * @param dispatcher The ServiceDispatcher instance
     */
    public void setServiceDispatcher(serviceDispatcher.ServiceDispatcher dispatcher) {
        this.serviceDispatcher = dispatcher;
        System.out.println("FinanceAppFrame: ServiceDispatcher attached.");
    }

    /**
     * Gets the ServiceDispatcher instance.
     * @return The ServiceDispatcher, or null if not set
     */
    public serviceDispatcher.ServiceDispatcher getServiceDispatcher() {
        return serviceDispatcher;
    }

    /**
     * Checks if the ServiceDispatcher is available.
     * @return true if dispatcher is set
     */
    public boolean hasServiceDispatcher() {
        return serviceDispatcher != null;
    }

    /**
     * Checks if the user is effectively logged in (either via local state or serviceDispatcher).
     * @return true if user is logged in
     */
    private boolean isEffectivelyLoggedIn() {
        return isLoggedIn || (serviceDispatcher != null && serviceDispatcher.isLoggedIn());
    }

    // =========================================================
    //   CONSTRUCTOR
    // =========================================================

    public FinanceAppFrame() {
        setTitle("Driver Finance Tracker");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1100, 650);
        setLocationRelativeTo(null);

        // If a Designer-created rootPanel exists, use it as the content pane and populate placeholders.
        if (rootPanel != null) {
            setContentPane(rootPanel);

            // Ensure models are initialized before populating placeholders
            initCarTableModel();

            // Populate the Designer-provided sidebarPanel
            if (sidebarPanel != null) {
                sidebarPanel.removeAll();
                sidebarPanel.setLayout(new BorderLayout());
                sidebarPanel.add(createSidebarPanel(), BorderLayout.CENTER);
                sidebarPanel.revalidate();
                sidebarPanel.repaint();
            }

            // Populate the Designer-provided contentPanel
            if (contentPanel != null) {
                contentPanel.removeAll();
                contentPanel.setLayout(new BorderLayout());
                contentPanel.add(createMainArea(), BorderLayout.CENTER);
                contentPanel.revalidate();
                contentPanel.repaint();
            }

            // Update data-driven UI
            updateSidebarStats();
            updateReportStats();
            updateHomeOverview();

            // Start on the Account screen
            if (cardLayout != null && contentPanel != null) {
                showScreen("ACCOUNT");
            }

        } else {
            // Fallback: no Designer UI available -> build UI programmatically as before.
            getContentPane().setBackground(COLOR_BG_ROOT);
            setLayout(new BorderLayout());

            initCarTableModel();

            add(createSidebarPanel(), BorderLayout.WEST);
            add(createMainArea(), BorderLayout.CENTER);

            updateSidebarStats();
            updateReportStats();  // full range initially
            updateHomeOverview();

            // Start on the Account screen so user sees sign-in / create-account first.
            showScreen("ACCOUNT");
        }
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

        sidebarTotalExpensesLabel = new JLabel("Gas est: $0.00");
        sidebarTotalExpensesLabel.setForeground(COLOR_TEXT_PRIMARY);
        sidebarTotalExpensesLabel.setFont(primaryFont(Font.PLAIN, 13));
        sidebarTotalExpensesLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        sidebar.add(sidebarTotalExpensesLabel);

        sidebar.add(Box.createVerticalStrut(20));

        JSeparator sepVehicle = new JSeparator();
        sepVehicle.setForeground(COLOR_DIVIDER);
        sepVehicle.setBackground(COLOR_DIVIDER);
        sepVehicle.setAlignmentX(Component.LEFT_ALIGNMENT);
        sidebar.add(sepVehicle);

        sidebar.add(Box.createVerticalStrut(16));

        JLabel vehicleTitle = new JLabel("Current Vehicle");
        vehicleTitle.setForeground(COLOR_TEXT_SECONDARY);
        vehicleTitle.setFont(primaryFont(Font.BOLD, 13));
        vehicleTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        sidebar.add(vehicleTitle);

        sidebar.add(Box.createVerticalStrut(8));

        sidebarCurrentVehicleLabel = new JLabel("No vehicle set");
        sidebarCurrentVehicleLabel.setForeground(COLOR_TEXT_PRIMARY);
        sidebarCurrentVehicleLabel.setFont(primaryFont(Font.PLAIN, 13));
        sidebarCurrentVehicleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        sidebar.add(sidebarCurrentVehicleLabel);

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

        JButton overviewBtn   = createNavButton("Overview");
        JButton deliveriesBtn = createNavButton("Deliveries");
        JButton reportsBtn    = createNavButton("Reports");
        JButton bankAccountBtn = createNavButton("Bank Account");
        JButton settingsBtn   = createNavButton("Settings");
        JButton accountBtn    = createNavButton("Account");

        overviewBtn.addActionListener(e -> showScreen("HOME"));
        deliveriesBtn.addActionListener(e -> showScreen("DELIVERIES"));
        reportsBtn.addActionListener(e -> showScreen("REPORTS"));
        bankAccountBtn.addActionListener(e -> showScreen("BANK_ACCOUNT"));
        settingsBtn.addActionListener(e -> showScreen("SETTINGS"));
        accountBtn.addActionListener(e -> showScreen("ACCOUNT"));

        navButtonsPanel.add(overviewBtn);
        navButtonsPanel.add(deliveriesBtn);
        navButtonsPanel.add(reportsBtn);
        navButtonsPanel.add(bankAccountBtn);
        navButtonsPanel.add(settingsBtn);
        navButtonsPanel.add(accountBtn);

        navBar.add(navButtonsPanel, BorderLayout.EAST);

        main.add(navBar, BorderLayout.NORTH);

        cardLayout = new CardLayout();
        contentPanel = new JPanel(cardLayout);
        contentPanel.setBorder(BorderFactory.createEmptyBorder(0, 20, 20, 20));
        contentPanel.setBackground(COLOR_BG_MAIN);

        // Screen order: Account first (start here), then others
        contentPanel.add(createAccountScreen(), "ACCOUNT");
        contentPanel.add(createHomeScreen(), "HOME");
        contentPanel.add(createDeliveriesScreen(), "DELIVERIES");
        contentPanel.add(createReportsScreen(), "REPORTS");
        contentPanel.add(createBankAccountScreen(), "BANK_ACCOUNT");
        contentPanel.add(createSettingsScreen(), "SETTINGS");

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
    //   ACCOUNT SCREEN (SIGN IN + CREATE ACCOUNT)
    // =========================================================

    private JPanel createAccountScreen() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);

        JPanel inner = new JPanel(new BorderLayout());
        inner.setBackground(COLOR_BG_CARD);
        inner.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel title = new JLabel("Account & Sign in");
        title.setFont(primaryFont(Font.BOLD, 18));
        title.setForeground(COLOR_TEXT_PRIMARY);
        inner.add(title, BorderLayout.NORTH);

        JPanel center = new JPanel();
        center.setBackground(COLOR_BG_CARD);
        center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));

        JLabel desc = new JLabel("Create an account for this demo and sign in using in-memory credentials.");
        desc.setFont(primaryFont(Font.PLAIN, 12));
        desc.setForeground(COLOR_TEXT_SECONDARY);
        desc.setAlignmentX(Component.LEFT_ALIGNMENT);
        center.add(desc);

        center.add(Box.createVerticalStrut(8));

        accountStatusLabel = new JLabel(getAccountStatusText());
        accountStatusLabel.setFont(primaryFont(Font.PLAIN, 12));
        accountStatusLabel.setForeground(COLOR_TEXT_MUTED);
        accountStatusLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        center.add(accountStatusLabel);

        center.add(Box.createVerticalStrut(20));

        // === Sign-in card ===
        JLabel signInTitle = new JLabel("Sign in");
        signInTitle.setFont(primaryFont(Font.BOLD, 13));
        signInTitle.setForeground(COLOR_TEXT_SECONDARY);
        signInTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        center.add(signInTitle);
        center.add(Box.createVerticalStrut(6));

        JPanel signInCard = new JPanel(new GridLayout(3, 2, 10, 8));
        signInCard.setBackground(COLOR_BG_MAIN);
        signInCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(COLOR_BORDER),
                BorderFactory.createEmptyBorder(12, 12, 12, 12)
        ));
        signInCard.setMaximumSize(new Dimension(420, 110));
        signInCard.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel loginUserLabel = new JLabel("Username");
        JLabel loginPassLabel = new JLabel("Password");
        styleFormLabel(loginUserLabel);
        styleFormLabel(loginPassLabel);

        accountLoginUserField = createInputField();
        accountLoginPassField = createInputField();

        JButton signInButton = new JButton("Sign in");
        stylePrimaryButton(signInButton);

        JButton logoutButton = new JButton("Log out");
        styleSecondaryButton(logoutButton);

        signInCard.add(loginUserLabel);
        signInCard.add(accountLoginUserField);
        signInCard.add(loginPassLabel);
        signInCard.add(accountLoginPassField);
        signInCard.add(signInButton);
        signInCard.add(logoutButton);

        center.add(signInCard);
        center.add(Box.createVerticalStrut(20));

        signInButton.addActionListener(e -> handleSignIn());

        logoutButton.addActionListener(e -> {
            if (!isEffectivelyLoggedIn()) {
                showAutoCloseSuccess("Already logged out");
                clearLoginFields();
                return;
            }
            // Use serviceDispatcher to logout from database session
            if (serviceDispatcher != null) {
                serviceDispatcher.logout();
            }
            isLoggedIn = false;
            savedUsername = "";
            savedPassword = "";
            savedEmail = "";
            accountStatusLabel.setText(getAccountStatusText());
            clearLoginFields();
            updateHomeOverview();
            showAutoCloseSuccess("Logged out");
        });

        // === Account details card ===
        JLabel detailsTitle = new JLabel("Account details");
        detailsTitle.setFont(primaryFont(Font.BOLD, 13));
        detailsTitle.setForeground(COLOR_TEXT_SECONDARY);
        detailsTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        center.add(detailsTitle);
        center.add(Box.createVerticalStrut(6));

        JPanel detailsCard = new JPanel(new GridLayout(3, 2, 10, 8));
        detailsCard.setBackground(COLOR_BG_MAIN);
        detailsCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(COLOR_BORDER),
                BorderFactory.createEmptyBorder(12, 12, 12, 12)
        ));
        detailsCard.setMaximumSize(new Dimension(420, 120));
        detailsCard.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel userLabel = new JLabel("Username");
        JLabel passLabel = new JLabel("Password");
        JLabel emailLabel = new JLabel("Email");

        styleFormLabel(userLabel);
        styleFormLabel(passLabel);
        styleFormLabel(emailLabel);

        profileUsernameField = createInputField();
        profilePasswordField = createInputField(); // plain text
        profileEmailField    = createInputField();
        profileEmailField.setToolTipText("Demo only: used for display, no real authentication.");

        // Pre-fill if previously saved
        if (!savedUsername.isEmpty()) {
            profileUsernameField.setText(savedUsername);
        }
        if (!savedPassword.isEmpty()) {
            profilePasswordField.setText(savedPassword);
        }
        if (!savedEmail.isEmpty()) {
            profileEmailField.setText(savedEmail);
        }

        detailsCard.add(userLabel);
        detailsCard.add(profileUsernameField);
        detailsCard.add(passLabel);
        detailsCard.add(profilePasswordField);
        detailsCard.add(emailLabel);
        detailsCard.add(profileEmailField);

        center.add(detailsCard);
        center.add(Box.createVerticalStrut(10));

        // Button text: "Create account"
        profileSaveButton = new JButton("Create account");
        profileSaveButton.setAlignmentX(Component.LEFT_ALIGNMENT);
        stylePrimaryButton(profileSaveButton);

        profileSaveButton.addActionListener(e -> handleSaveProfile());

        center.add(profileSaveButton);

        inner.add(center, BorderLayout.CENTER);
        panel.add(inner, BorderLayout.CENTER);

        return panel;
    }

    private String getAccountStatusText() {
        if (savedUsername == null || savedUsername.isEmpty()) {
            if (isEffectivelyLoggedIn()) {
                return "Signed in via database";
            }
            return "No account saved yet.";
        }
        String base = (savedEmail == null || savedEmail.isEmpty())
                ? savedUsername
                : savedUsername + " (" + savedEmail + ")";
        if (isEffectivelyLoggedIn()) {
            return "Signed in as " + base;
        } else {
            return "Account saved but not signed in: " + base;
        }
    }

    private void clearLoginFields() {
        if (accountLoginUserField != null) {
            accountLoginUserField.setText("");
        }
        if (accountLoginPassField != null) {
            accountLoginPassField.setText("");
        }
    }

    private void handleSignIn() {
        String username = accountLoginUserField.getText().trim();
        String password = accountLoginPassField.getText().trim();

        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(
                    this,
                    "Username and password are required.",
                    "Input error",
                    JOptionPane.ERROR_MESSAGE
            );
            return;
        }

        // Use serviceDispatcher to authenticate against database
        if (serviceDispatcher != null && serviceDispatcher.login(username, password)) {
            isLoggedIn = true;
            savedUsername = username;
            accountStatusLabel.setText(getAccountStatusText());
            clearLoginFields();
            showAutoCloseSuccess("Signed in as " + username);
            loadVehiclesFromDatabase(); // Load vehicles from database
            loadDeliveriesFromDatabase(); // Load past deliveries from database
            loadCurrentVehicle(); // Load and display current vehicle
            loadBankAccountsFromDatabase(); // Load bank accounts from database
            updateHomeOverview();
            showScreen("HOME");
        } else if (serviceDispatcher == null) {
            // Fallback to in-memory check if no dispatcher (for testing)
            if (savedUsername != null && !savedUsername.isEmpty()
                    && username.equals(savedUsername) && password.equals(savedPassword)) {
                isLoggedIn = true;
                accountStatusLabel.setText(getAccountStatusText());
                clearLoginFields();
                showAutoCloseSuccess("Signed in as " + username);
                updateHomeOverview();
                showScreen("HOME");
            } else {
                JOptionPane.showMessageDialog(
                        this,
                        "Invalid username or password.",
                        "Sign-in failed",
                        JOptionPane.ERROR_MESSAGE
                );
            }
        } else {
            JOptionPane.showMessageDialog(
                    this,
                    "Invalid username or password.",
                    "Sign-in failed",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    /**
     * Loads all past deliveries from the database for the current user and populates the delivery table.
     */
    private void loadDeliveriesFromDatabase() {
        if (serviceDispatcher == null || !serviceDispatcher.isLoggedIn()) {
            System.out.println("FinanceAppFrame: Cannot load deliveries - not logged in or no dispatcher.");
            return;
        }

        try {
            // Clear existing table data
            deliveryTableModel.setRowCount(0);

            // Fetch deliveries from database via serviceDispatcher
            List<overviewService.OverviewDTO> deliveries = serviceDispatcher.getCurrentUserPastDeliveries();

            if (deliveries == null || deliveries.isEmpty()) {
                System.out.println("FinanceAppFrame: No deliveries found for current user.");
                updateDeliverySummaryLabel();
                return;
            }

            System.out.println("FinanceAppFrame: Loading " + deliveries.size() + " deliveries from database.");

            // Date/time formatters
            DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");

            for (overviewService.OverviewDTO delivery : deliveries) {
                // Convert epoch timestamp to formatted date and time
                String date = "";
                String startTime = "";

                long timestamp = delivery.getDateTime();
                if (timestamp > 0) {
                    LocalDateTime dateTime = LocalDateTime.ofInstant(
                        Instant.ofEpochMilli(timestamp), ZoneId.systemDefault());
                    date = dateTime.format(dateFormatter);
                    startTime = dateTime.format(timeFormatter);
                }

                // Get values from DTO
                String restaurant = delivery.getRestaurant() != null ? delivery.getRestaurant() : "";
                String platform = delivery.getPlatform() != null ? delivery.getPlatform() : "";
                String vehicle = delivery.getVehicle() != null ? delivery.getVehicle() : "";
                double miles = delivery.getMilesDriven();
                double basePay = delivery.getBasePay();
                double tips = delivery.getTips();
                double total = basePay + tips;

                // End time is not stored separately, so leave blank or calculate if needed
                String endTime = "";

                // Add row to table
                deliveryTableModel.addRow(new Object[]{
                    date,
                    startTime,
                    endTime,
                    restaurant,
                    platform,
                    vehicle,
                    miles,
                    basePay,
                    tips,
                    total
                });
            }

            // Update UI components with loaded data
            updateDeliverySummaryLabel();
            updateSidebarStats();
            updateReportStats();

            System.out.println("FinanceAppFrame: Successfully loaded " + deliveries.size() + " deliveries.");

        } catch (Exception e) {
            System.err.println("FinanceAppFrame: Error loading deliveries from database: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Updates the delivery summary label with count and total from table.
     */
    private void updateDeliverySummaryLabel() {
        if (deliveryTableModel == null || deliveriesSummaryLabel == null) {
            return;
        }

        int count = deliveryTableModel.getRowCount();
        double total = 0.0;

        for (int i = 0; i < count; i++) {
            Object totalObj = deliveryTableModel.getValueAt(i, 9); // Total column
            if (totalObj instanceof Number) {
                total += ((Number) totalObj).doubleValue();
            }
        }

        deliveriesSummaryLabel.setText(count + " deliveries \u2022 Total: $" + String.format("%.2f", total));
    }

    /**
     * Loads and displays the current vehicle from the database.
     * If no vehicle is set as current, the first vehicle in the database is used.
     */
    private void loadCurrentVehicle() {
        if (serviceDispatcher == null) {
            System.out.println("FinanceAppFrame: ServiceDispatcher not available, cannot load current vehicle.");
            return;
        }

        try {
            org.example.deliveryRecorder.src.vehicle currentVehicle = serviceDispatcher.getCurrentVehicle();

            if (currentVehicle != null) {
                String vehicleDisplay = currentVehicle.getVehicleModel();
                if (currentVehicle.getVehicleType() != null && !currentVehicle.getVehicleType().isEmpty()) {
                    vehicleDisplay = currentVehicle.getVehicleType() + " - " + currentVehicle.getVehicleModel();
                }

                sidebarCurrentVehicleLabel.setText(vehicleDisplay);
                System.out.println("FinanceAppFrame: Current vehicle loaded: " + vehicleDisplay);

                // Also select this vehicle in the delivery car combo if it exists
                selectVehicleInCombo(currentVehicle.getVehicleModel());
            } else {
                sidebarCurrentVehicleLabel.setText("No vehicle set");
                System.out.println("FinanceAppFrame: No vehicles found in database.");
            }
        } catch (Exception e) {
            System.err.println("FinanceAppFrame: Error loading current vehicle: " + e.getMessage());
            sidebarCurrentVehicleLabel.setText("Error loading vehicle");
        }
    }

    /**
     * Selects the specified vehicle in the delivery car combo box.
     * @param vehicleModel The vehicle model to select
     */
    private void selectVehicleInCombo(String vehicleModel) {
        if (deliveryCarCombo == null || vehicleModel == null) {
            return;
        }

        for (int i = 0; i < deliveryCarCombo.getItemCount(); i++) {
            String item = deliveryCarCombo.getItemAt(i);
            if (item != null && item.contains(vehicleModel)) {
                deliveryCarCombo.setSelectedIndex(i);
                break;
            }
        }
    }

    /**
     * Updates the current vehicle display in the sidebar.
     * @param vehicleModel The vehicle model to display
     */
    public void updateCurrentVehicleDisplay(String vehicleModel) {
        if (sidebarCurrentVehicleLabel != null && vehicleModel != null) {
            sidebarCurrentVehicleLabel.setText(vehicleModel);
        }
    }

    /**
     * Loads all bank accounts from the database for the current user and populates the bank account table.
     * Called automatically on login.
     */
    private void loadBankAccountsFromDatabase() {
        if (serviceDispatcher == null || !serviceDispatcher.isLoggedIn()) {
            System.out.println("FinanceAppFrame: Cannot load bank accounts - not logged in or no dispatcher.");
            return;
        }

        if (bankAccountTableModel == null) {
            System.out.println("FinanceAppFrame: Bank account table model not initialized yet.");
            return;
        }

        try {
            List<org.example.manageFinances.src.selectBankAccount> accounts =
                    serviceDispatcher.getCurrentUserBankAccounts();

            bankAccountTableModel.setRowCount(0); // Clear table

            for (org.example.manageFinances.src.selectBankAccount acc : accounts) {
                bankAccountTableModel.addRow(new Object[]{
                        acc.getAccountID(),
                        acc.getAccountName(),
                        acc.getAccountType(),
                        String.format("$%.2f", acc.getBalance())
                });
            }

            // Clear transactions and details
            if (bankTransactionTableModel != null) {
                bankTransactionTableModel.setRowCount(0);
            }
            if (bankAccountIdValue != null) {
                bankAccountIdValue.setText("-");
                bankAccountNameValue.setText("-");
                bankAccountTypeValue.setText("-");
                bankAccountBalanceValue.setText("-");
            }
            if (bankTotalIncomeLabel != null) {
                bankTotalIncomeLabel.setText("Total Income: $0.00");
                bankTotalExpensesLabel.setText("Total Expenses: $0.00");
                bankNetLabel.setText("Net: $0.00");
                bankTransactionCountLabel.setText("Transactions: 0");
            }

            System.out.println("FinanceAppFrame: Loaded " + accounts.size() + " bank account(s) from database.");

            // Also load summary transactions (auto-load on login)
            loadSummaryTransactions("All Transactions");

        } catch (Exception ex) {
            System.err.println("Error loading bank accounts: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    /**
     * Loads all vehicles from the database and populates the car combo box and car table.
     */
    private void loadVehiclesFromDatabase() {
        if (serviceDispatcher == null) {
            System.out.println("FinanceAppFrame: ServiceDispatcher not available, cannot load vehicles.");
            return;
        }

        try {
            List<org.example.deliveryRecorder.src.vehicle> vehicles = serviceDispatcher.getAllVehicles();

            if (vehicles == null || vehicles.isEmpty()) {
                System.out.println("FinanceAppFrame: No vehicles found in database.");
                return;
            }

            System.out.println("FinanceAppFrame: Loading " + vehicles.size() + " vehicles from database.");

            // Clear existing data
            carTableModel.setRowCount(0);
            ((DefaultComboBoxModel<String>) deliveryCarCombo.getModel()).removeAllElements();

            for (org.example.deliveryRecorder.src.vehicle v : vehicles) {
                String vehicleName = v.getVehicleModel();
                if (vehicleName == null || vehicleName.isEmpty()) {
                    continue;
                }

                int carId = nextCarId++;
                // Use MPG from database, or default based on vehicle type if not set
                double mpg = v.getVehicleMpg();
                if (mpg <= 0) {
                    mpg = getDefaultMpgForVehicleType(v.getVehicleType());
                }
                String vehicleType = v.getVehicleType();
                String displayName = (vehicleType != null && !vehicleType.isEmpty())
                        ? vehicleType + " - " + vehicleName
                        : vehicleName;
                carTableModel.addRow(new Object[]{carId, displayName, mpg});

                String displayString = carId + " - " + displayName;
                ((DefaultComboBoxModel<String>) deliveryCarCombo.getModel()).addElement(displayString);
            }

            System.out.println("FinanceAppFrame: Successfully loaded " + vehicles.size() + " vehicles.");

            // Also refresh the default vehicle combo in settings
            refreshDefaultVehicleCombo();

        } catch (Exception e) {
            System.err.println("FinanceAppFrame: Error loading vehicles from database: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Returns a default MPG value based on the vehicle type.
     * @param vehicleType The type of vehicle (Car, Motorcycle, Bike)
     * @return Default MPG for gas cost calculations
     */
    private double getDefaultMpgForVehicleType(String vehicleType) {
        if (vehicleType == null || vehicleType.isEmpty()) {
            return DEFAULT_CAR_MPG; // Default to car MPG
        }
        switch (vehicleType.toLowerCase()) {
            case "car":
                return DEFAULT_CAR_MPG;
            case "motorcycle":
                return DEFAULT_MOTORCYCLE_MPG;
            case "bike":
                return 0.0; // Bikes don't use gas
            default:
                return DEFAULT_CAR_MPG;
        }
    }

    /**
     * Refreshes the default vehicle combo box in the Settings tab with vehicles from the database.
     */
    private void refreshDefaultVehicleCombo() {
        if (defaultVehicleCombo == null) {
            return;
        }

        defaultVehicleCombo.removeAllItems();

        // Try to load from database first
        if (serviceDispatcher != null) {
            try {
                List<org.example.deliveryRecorder.src.vehicle> vehicles = serviceDispatcher.getAllVehicles();
                if (vehicles != null && !vehicles.isEmpty()) {
                    for (org.example.deliveryRecorder.src.vehicle v : vehicles) {
                        String vehicleName = v.getVehicleModel();
                        String vehicleType = v.getVehicleType();
                        if (vehicleName == null || vehicleName.isEmpty()) {
                            continue;
                        }
                        String displayStr;
                        if (vehicleType != null && !vehicleType.isEmpty()) {
                            displayStr = vehicleType + " - " + vehicleName;
                        } else {
                            displayStr = vehicleName;
                        }
                        defaultVehicleCombo.addItem(displayStr);
                    }
                    System.out.println("FinanceAppFrame: Loaded " + vehicles.size() + " vehicles into default vehicle dropdown.");
                }
            } catch (Exception ex) {
                System.err.println("FinanceAppFrame: Error loading vehicles for default dropdown: " + ex.getMessage());
            }
        }

        // Fallback: if no vehicles loaded from database, try carTableModel
        if (defaultVehicleCombo.getItemCount() == 0 && carTableModel != null) {
            for (int i = 0; i < carTableModel.getRowCount(); i++) {
                Object idObj = carTableModel.getValueAt(i, 0);
                Object nameObj = carTableModel.getValueAt(i, 1);
                String displayStr = idObj + " - " + nameObj;
                defaultVehicleCombo.addItem(displayStr);
            }
        }

        // Try to select the current vehicle
        if (serviceDispatcher != null) {
            try {
                org.example.deliveryRecorder.src.vehicle currentVehicleObj = serviceDispatcher.getCurrentVehicle();
                if (currentVehicleObj != null) {
                    String currentVehicle = currentVehicleObj.getVehicleModel();
                    if (currentVehicle != null && !currentVehicle.isEmpty()) {
                        for (int i = 0; i < defaultVehicleCombo.getItemCount(); i++) {
                            String item = defaultVehicleCombo.getItemAt(i);
                            if (item != null && item.contains(currentVehicle)) {
                                defaultVehicleCombo.setSelectedIndex(i);
                                break;
                            }
                        }
                    }
                }
            } catch (Exception ex) {
                System.err.println("FinanceAppFrame: Error getting current vehicle: " + ex.getMessage());
            }
        }
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

        // Use serviceDispatcher to create account in database
        boolean accountCreated = false;
        if (serviceDispatcher != null) {
            System.out.println("FinanceAppFrame: Attempting to create account via serviceDispatcher for: " + username);
            accountCreated = serviceDispatcher.createAccount(username, password, email);
            System.out.println("FinanceAppFrame: Account creation result: " + accountCreated);
        } else {
            System.out.println("FinanceAppFrame: WARNING - serviceDispatcher is null! Running in-memory mode.");
            System.out.println("FinanceAppFrame: To use database, run the app via ServiceDispatcher.main() instead of FinanceAppFrame.main()");
        }

        if (accountCreated) {
            // Account created in database - now log in
            savedUsername = username;
            savedPassword = password;
            savedEmail = email;

            // Automatically log in after account creation
            if (serviceDispatcher.login(username, password)) {
                isLoggedIn = true;
                accountStatusLabel.setText(getAccountStatusText());
                clearLoginFields();

                // Clear the create-account text boxes after saving
                profileUsernameField.setText("");
                profilePasswordField.setText("");
                profileEmailField.setText("");

                updateHomeOverview();
                showAutoCloseSuccess("Account created and signed in");
                showScreen("HOME");
            } else {
                accountStatusLabel.setText(getAccountStatusText());
                showAutoCloseSuccess("Account created - please sign in");
            }
        } else if (serviceDispatcher == null) {
            // Fallback to in-memory if no dispatcher (for testing)
            savedUsername = username;
            savedPassword = password;
            savedEmail = email;

            isLoggedIn = true;
            accountStatusLabel.setText(getAccountStatusText());
            clearLoginFields();

            profileUsernameField.setText("");
            profilePasswordField.setText("");
            profileEmailField.setText("");

            updateHomeOverview();
            showAutoCloseSuccess("Account saved and signed in (in-memory mode)");
            showScreen("HOME");
        } else {
            JOptionPane.showMessageDialog(
                    this,
                    "Failed to create account. Username may already exist.",
                    "Account creation failed",
                    JOptionPane.ERROR_MESSAGE
            );
        }
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

        // ====== Top header: Welcome (left) + Range toggle (right) ======
        JPanel headerRow = new JPanel(new BorderLayout());
        headerRow.setBackground(COLOR_BG_CARD);

        // Left: welcome + subtitle
        JPanel topLeft = new JPanel();
        topLeft.setBackground(COLOR_BG_CARD);
        topLeft.setLayout(new BoxLayout(topLeft, BoxLayout.Y_AXIS));

        homeWelcomeLabel = new JLabel("Welcome");
        homeWelcomeLabel.setFont(primaryFont(Font.BOLD, 20));
        homeWelcomeLabel.setForeground(COLOR_TEXT_PRIMARY);
        homeWelcomeLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel subtitle = new JLabel("Overview of your deliveries, earnings, and gas cost.");
        subtitle.setFont(primaryFont(Font.PLAIN, 12));
        subtitle.setForeground(COLOR_TEXT_SECONDARY);
        subtitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        topLeft.add(homeWelcomeLabel);
        topLeft.add(Box.createVerticalStrut(4));
        topLeft.add(subtitle);

        // Right: range toggle buttons
        JPanel topRight = new JPanel();
        topRight.setBackground(COLOR_BG_CARD);
        topRight.setLayout(new BoxLayout(topRight, BoxLayout.X_AXIS));

        JLabel rangeLabel = new JLabel("Range: ");
        rangeLabel.setFont(primaryFont(Font.PLAIN, 12));
        rangeLabel.setForeground(COLOR_TEXT_SECONDARY);

        homeTodayButton = new JButton("Today");
        homeWeekButton  = new JButton("This week");
        homeMonthButton = new JButton("This month");
        homeAllButton   = new JButton("All time");

        styleHomeRangeButton(homeTodayButton);
        styleHomeRangeButton(homeWeekButton);
        styleHomeRangeButton(homeMonthButton);
        styleHomeRangeButton(homeAllButton);

        homeTodayButton.addActionListener(e -> setHomeRange(HomeRange.TODAY));
        homeWeekButton.addActionListener(e -> setHomeRange(HomeRange.WEEK));
        homeMonthButton.addActionListener(e -> setHomeRange(HomeRange.MONTH));
        homeAllButton.addActionListener(e -> setHomeRange(HomeRange.ALL));

        topRight.add(rangeLabel);
        topRight.add(homeTodayButton);
        topRight.add(homeWeekButton);
        topRight.add(homeMonthButton);
        topRight.add(homeAllButton);

        headerRow.add(topLeft, BorderLayout.WEST);
        headerRow.add(topRight, BorderLayout.EAST);

        inner.add(headerRow, BorderLayout.NORTH);

        // ====== Middle: three stat cards ======
        JPanel statsPanel = new JPanel(new GridLayout(1, 3, 15, 0));
        statsPanel.setBackground(COLOR_BG_CARD);
        statsPanel.setBorder(BorderFactory.createEmptyBorder(20, 0, 10, 0));

        homeTotalDeliveriesValueLabel = new JLabel("0");
        homeTotalEarningsValueLabel  = new JLabel("$0.00");
        homeNetValueLabel            = new JLabel("$0.00");

        statsPanel.add(createHomeStatCard("Total deliveries", homeTotalDeliveriesValueLabel));
        statsPanel.add(createHomeStatCard("Total earnings", homeTotalEarningsValueLabel));
        statsPanel.add(createHomeStatCard("Net after gas", homeNetValueLabel));

        inner.add(statsPanel, BorderLayout.CENTER);

        // ====== Bottom: activity summary + top restaurant/platform ======
        JPanel bottom = new JPanel();
        bottom.setBackground(COLOR_BG_CARD);
        bottom.setLayout(new BoxLayout(bottom, BoxLayout.Y_AXIS));

        homeActivitySummaryLabel = new JLabel("No deliveries yet in this range.");
        homeActivitySummaryLabel.setFont(primaryFont(Font.PLAIN, 12));
        homeActivitySummaryLabel.setForeground(COLOR_TEXT_SECONDARY);
        homeActivitySummaryLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        homeTopRestaurantLabel = new JLabel("Top restaurant: (none yet)");
        homeTopRestaurantLabel.setFont(primaryFont(Font.PLAIN, 12));
        homeTopRestaurantLabel.setForeground(COLOR_TEXT_SECONDARY);
        homeTopRestaurantLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        homeTopPlatformLabel = new JLabel("Top platform: (none yet)");
        homeTopPlatformLabel.setFont(primaryFont(Font.PLAIN, 12));
        homeTopPlatformLabel.setForeground(COLOR_TEXT_SECONDARY);
        homeTopPlatformLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        bottom.add(homeActivitySummaryLabel);
        bottom.add(Box.createVerticalStrut(4));
        bottom.add(homeTopRestaurantLabel);
        bottom.add(Box.createVerticalStrut(2));
        bottom.add(homeTopPlatformLabel);

        inner.add(bottom, BorderLayout.SOUTH);

        panel.add(inner, BorderLayout.CENTER);

        // Initialize buttons visual state
        updateHomeRangeButtons();

        return panel;
    }

    private JPanel createHomeStatCard(String title, JLabel valueLabel) {
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

    private void styleHomeRangeButton(JButton button) {
        button.setFocusPainted(false);
        button.setFont(primaryFont(Font.PLAIN, 11));
        button.setBackground(COLOR_BG_CARD);
        button.setForeground(COLOR_TEXT_SECONDARY);
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(COLOR_BORDER),
                BorderFactory.createEmptyBorder(3, 8, 3, 8)
        ));
    }

    private void setHomeRange(HomeRange newRange) {
        this.homeRange = newRange;
        updateHomeOverview();
    }

    private void updateHomeRangeButtons() {
        if (homeTodayButton == null) return; // not initialized yet

        JButton[] buttons = { homeTodayButton, homeWeekButton, homeMonthButton, homeAllButton };

        for (JButton b : buttons) {
            b.setBackground(COLOR_BG_CARD);
            b.setForeground(COLOR_TEXT_SECONDARY);
            b.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(COLOR_BORDER),
                    BorderFactory.createEmptyBorder(3, 8, 3, 8)
            ));
        }

        JButton activeButton;
        switch (homeRange) {
            case TODAY:
                activeButton = homeTodayButton;
                break;
            case WEEK:
                activeButton = homeWeekButton;
                break;
            case MONTH:
                activeButton = homeMonthButton;
                break;
            case ALL:
            default:
                activeButton = homeAllButton;
                break;
        }

        activeButton.setBackground(COLOR_ACCENT_DARK);
        activeButton.setForeground(Color.WHITE);
    }

    private void updateHomeOverview() {
        // Decide date range based on homeRange
        LocalDate today = LocalDate.now();
        LocalDate start = null;
        LocalDate end   = null;

        switch (homeRange) {
            case TODAY:
                start = today;
                end   = today;
                break;
            case WEEK:
                start = today.minusDays(6); // last 7 days including today
                end   = today;
                break;
            case MONTH:
                start = today.withDayOfMonth(1); // from start of current month
                end   = today;
                break;
            case ALL:
            default:
                start = null;
                end   = null;
                break;
        }

        // Welcome text
        String welcome;
        if (isEffectivelyLoggedIn() && savedUsername != null && !savedUsername.isEmpty()) {
            welcome = "Welcome, " + savedUsername;
        } else if (savedUsername != null && !savedUsername.isEmpty()) {
            welcome = "Welcome, " + savedUsername + " (not signed in)";
        } else {
            welcome = "Welcome";
        }
        if (homeWelcomeLabel != null) {
            homeWelcomeLabel.setText(welcome);
        }

        // If no deliveries yet
        if (deliveryTableModel == null || deliveryTableModel.getRowCount() == 0) {
            if (homeTotalDeliveriesValueLabel != null) homeTotalDeliveriesValueLabel.setText("0");
            if (homeTotalEarningsValueLabel  != null) homeTotalEarningsValueLabel.setText("$0.00");
            if (homeNetValueLabel            != null) homeNetValueLabel.setText("$0.00");
            if (homeActivitySummaryLabel     != null) homeActivitySummaryLabel.setText("No deliveries yet in this range.");
            if (homeTopRestaurantLabel       != null) homeTopRestaurantLabel.setText("Top restaurant: (none yet)");
            if (homeTopPlatformLabel         != null) homeTopPlatformLabel.setText("Top platform: (none yet)");
            updateHomeRangeButtons();
            return;
        }

        // Aggregate for selected range
        int rows = deliveryTableModel.getRowCount();
        int deliveries = 0;
        double totalEarnings = 0.0;
        double totalMiles = 0.0;
        double gasCost = 0.0;

        Map<String, Integer> restaurantCounts = new LinkedHashMap<>();
        Map<String, Integer> platformCounts = new LinkedHashMap<>();

        for (int i = 0; i < rows; i++) {
            Object dateObj = deliveryTableModel.getValueAt(i, 0); // Date column
            if (dateObj == null) continue;
            String dateStr = dateObj.toString().trim();
            LocalDate rowDate = parseDateSafe(dateStr);
            if (rowDate == null) continue;

            if (start != null && rowDate.isBefore(start)) continue;
            if (end != null && rowDate.isAfter(end)) continue;

            deliveries++;

            // Total earnings
            Object totalObj = deliveryTableModel.getValueAt(i, 9); // Total column
            double total = 0.0;
            if (totalObj instanceof Number) {
                total = ((Number) totalObj).doubleValue();
            } else if (totalObj != null) {
                try {
                    total = Double.parseDouble(totalObj.toString());
                } catch (NumberFormatException ignored) { }
            }
            totalEarnings += total;

            // Miles
            Object milesObj = deliveryTableModel.getValueAt(i, 6);
            double miles = 0.0;
            if (milesObj instanceof Number) {
                miles = ((Number) milesObj).doubleValue();
            } else if (milesObj != null) {
                try {
                    miles = Double.parseDouble(milesObj.toString());
                } catch (NumberFormatException ignored) { }
            }
            totalMiles += miles;

            // Gas cost per row
            gasCost += computeGasCostForRow(i);

            // Restaurant counts
            Object restObj = deliveryTableModel.getValueAt(i, 3);
            if (restObj != null) {
                String rest = restObj.toString().trim();
                if (!rest.isEmpty()) {
                    restaurantCounts.put(rest, restaurantCounts.getOrDefault(rest, 0) + 1);
                }
            }

            // Platform counts
            Object platformObj = deliveryTableModel.getValueAt(i, 4);
            String platform = platformObj == null ? "Unknown" : platformObj.toString();
            platformCounts.put(platform, platformCounts.getOrDefault(platform, 0) + 1);
        }

        double net = totalEarnings - gasCost;

        // Update main cards
        if (homeTotalDeliveriesValueLabel != null) {
            homeTotalDeliveriesValueLabel.setText(String.valueOf(deliveries));
        }
        if (homeTotalEarningsValueLabel != null) {
            homeTotalEarningsValueLabel.setText(String.format("$%.2f", totalEarnings));
        }
        if (homeNetValueLabel != null) {
            homeNetValueLabel.setText(String.format("$%.2f", net));
        }

        // Activity summary sentence
        if (homeActivitySummaryLabel != null) {
            if (deliveries == 0) {
                homeActivitySummaryLabel.setText("No deliveries yet in this range.");
            } else {
                homeActivitySummaryLabel.setText(String.format(
                        "You completed %d deliveries, drove %.1f miles, and earned $%.2f in this period.",
                        deliveries, totalMiles, totalEarnings
                ));
            }
        }

        // Top restaurant
        String topRestText = "Top restaurant: (none yet)";
        if (!restaurantCounts.isEmpty()) {
            String bestName = null;
            int bestCount = 0;
            for (Map.Entry<String, Integer> entry : restaurantCounts.entrySet()) {
                if (entry.getValue() > bestCount) {
                    bestCount = entry.getValue();
                    bestName = entry.getKey();
                }
            }
            if (bestName != null) {
                topRestText = String.format("Top restaurant: %s (%d deliveries)", bestName, bestCount);
            }
        }
        if (homeTopRestaurantLabel != null) {
            homeTopRestaurantLabel.setText(topRestText);
        }

        // Top platform
        String topPlatText = "Top platform: (none yet)";
        if (!platformCounts.isEmpty()) {
            String bestName = null;
            int bestCount = 0;
            for (Map.Entry<String, Integer> entry : platformCounts.entrySet()) {
                if (entry.getValue() > bestCount) {
                    bestCount = entry.getValue();
                    bestName = entry.getKey();
                }
            }
            if (bestName != null) {
                topPlatText = String.format("Top platform: %s (%d deliveries)", bestName, bestCount);
            }
        }
        if (homeTopPlatformLabel != null) {
            homeTopPlatformLabel.setText(topPlatText);
        }

        updateHomeRangeButtons();
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
        JPanel formPanel = new JPanel(new GridLayout(6, 4, 10, 10));
        formPanel.setBackground(COLOR_BG_CARD);

        JLabel restaurantLabel = new JLabel("Restaurant");
        deliveryRestaurantField = createInputField();

        JLabel dateLabel = new JLabel("Date");
        // Create date dropdowns for delivery date
        int currentYear = java.time.Year.now().getValue();
        Integer[] deliveryYears = new Integer[11];
        for (int i = 0; i < 11; i++) {
            deliveryYears[i] = currentYear - 5 + i;
        }
        Integer[] deliveryMonths = new Integer[12];
        for (int i = 0; i < 12; i++) {
            deliveryMonths[i] = i + 1;
        }
        Integer[] deliveryDays = new Integer[31];
        for (int i = 0; i < 31; i++) {
            deliveryDays[i] = i + 1;
        }

        deliveryDateYearCombo = new JComboBox<>(deliveryYears);
        deliveryDateYearCombo.setSelectedItem(currentYear);
        deliveryDateMonthCombo = new JComboBox<>(deliveryMonths);
        deliveryDateMonthCombo.setSelectedItem(java.time.LocalDate.now().getMonthValue());
        deliveryDateDayCombo = new JComboBox<>(deliveryDays);
        deliveryDateDayCombo.setSelectedItem(java.time.LocalDate.now().getDayOfMonth());

        // Create a panel to hold the date dropdowns
        JPanel datePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 2, 0));
        datePanel.setBackground(COLOR_BG_CARD);
        datePanel.add(deliveryDateYearCombo);
        datePanel.add(new JLabel("-"));
        datePanel.add(deliveryDateMonthCombo);
        datePanel.add(new JLabel("-"));
        datePanel.add(deliveryDateDayCombo);

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

        JLabel milesLabel = new JLabel("Miles driven");
        deliveryMilesField = createInputField();

        JLabel fromAddressLabel = new JLabel("From address");
        deliveryFromAddressField = createInputField();
        deliveryFromAddressField.setToolTipText("Pickup address for this delivery.");

        JLabel toAddressLabel = new JLabel("To address");
        deliveryToAddressField = createInputField();
        deliveryToAddressField.setToolTipText("Drop-off address for this delivery.");

        styleFormLabel(restaurantLabel);
        styleFormLabel(dateLabel);
        styleFormLabel(startLabel);
        styleFormLabel(endLabel);
        styleFormLabel(payLabel);
        styleFormLabel(tipLabel);
        styleFormLabel(platformLabel);
        styleFormLabel(carLabel);
        styleFormLabel(milesLabel);
        styleFormLabel(fromAddressLabel);
        styleFormLabel(toAddressLabel);

        // Row 1
        formPanel.add(restaurantLabel);
        formPanel.add(deliveryRestaurantField);
        formPanel.add(dateLabel);
        formPanel.add(datePanel);

        // Row 2
        formPanel.add(startLabel);
        formPanel.add(deliveryStartTimeField);
        formPanel.add(endLabel);
        formPanel.add(deliveryEndTimeField);

        // Row 3
        formPanel.add(payLabel);
        formPanel.add(deliveryPayField);
        formPanel.add(tipLabel);
        formPanel.add(deliveryTipField);

        // Row 4
        formPanel.add(platformLabel);
        formPanel.add(deliveryPlatformCombo);
        formPanel.add(carLabel);
        formPanel.add(deliveryCarCombo);

        // Row 5
        formPanel.add(milesLabel);
        formPanel.add(deliveryMilesField);
        formPanel.add(fromAddressLabel);
        formPanel.add(deliveryFromAddressField);

        // Row 6
        formPanel.add(toAddressLabel);
        formPanel.add(deliveryToAddressField);
        formPanel.add(new JLabel()); // spacer
        formPanel.add(new JLabel()); // spacer

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
                "Car", "Miles", "Pay", "Tip", "Total"
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

        DefaultTableCellRenderer rightRenderer = new DefaultTableCellRenderer();
        rightRenderer.setHorizontalAlignment(SwingConstants.RIGHT);
        deliveryTable.getColumnModel().getColumn(6).setCellRenderer(rightRenderer);
        deliveryTable.getColumnModel().getColumn(7).setCellRenderer(rightRenderer);
        deliveryTable.getColumnModel().getColumn(8).setCellRenderer(rightRenderer);
        deliveryTable.getColumnModel().getColumn(9).setCellRenderer(rightRenderer);

        deliveryTable.getColumnModel().getColumn(0).setPreferredWidth(90);  // Date
        deliveryTable.getColumnModel().getColumn(1).setPreferredWidth(70);  // Start
        deliveryTable.getColumnModel().getColumn(2).setPreferredWidth(70);  // End
        deliveryTable.getColumnModel().getColumn(3).setPreferredWidth(140); // Restaurant
        deliveryTable.getColumnModel().getColumn(4).setPreferredWidth(100); // Platform
        deliveryTable.getColumnModel().getColumn(5).setPreferredWidth(130); // Car
        deliveryTable.getColumnModel().getColumn(6).setPreferredWidth(70);  // Miles
        deliveryTable.getColumnModel().getColumn(7).setPreferredWidth(70);  // Pay
        deliveryTable.getColumnModel().getColumn(8).setPreferredWidth(70);  // Tip
        deliveryTable.getColumnModel().getColumn(9).setPreferredWidth(80);  // Total

        deliveryTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int viewRow = deliveryTable.getSelectedRow();
                if (viewRow >= 0) {
                    int modelRow = deliveryTable.convertRowIndexToModel(viewRow);
                    setDeliveryDateFromString(Objects.toString(deliveryTableModel.getValueAt(modelRow, 0), ""));
                    deliveryStartTimeField.setText(Objects.toString(deliveryTableModel.getValueAt(modelRow, 1), ""));
                    deliveryEndTimeField.setText(Objects.toString(deliveryTableModel.getValueAt(modelRow, 2), ""));
                    deliveryRestaurantField.setText(Objects.toString(deliveryTableModel.getValueAt(modelRow, 3), ""));
                    deliveryPlatformCombo.setSelectedItem(Objects.toString(deliveryTableModel.getValueAt(modelRow, 4), ""));
                    deliveryCarCombo.setSelectedItem(Objects.toString(deliveryTableModel.getValueAt(modelRow, 5), ""));
                    deliveryMilesField.setText(Objects.toString(deliveryTableModel.getValueAt(modelRow, 6), ""));
                    deliveryPayField.setText(Objects.toString(deliveryTableModel.getValueAt(modelRow, 7), ""));
                    deliveryTipField.setText(Objects.toString(deliveryTableModel.getValueAt(modelRow, 8), ""));
                }
            }
        });

        JScrollPane tableScroll = new JScrollPane(deliveryTable);
        tableScroll.getViewport().setBackground(COLOR_BG_MAIN);
        tableScroll.setBorder(BorderFactory.createLineBorder(COLOR_BORDER));

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
        workPeriodButton = new JButton("Start Work Period");

        stylePrimaryButton(saveButton);
        styleSecondaryButton(updateButton);
        styleSecondaryButton(clearFormButton);
        styleSecondaryButton(workPeriodButton);

        clearFormButton.setToolTipText("Clear all delivery input fields.");
        workPeriodButton.setToolTipText("Start a new work period for today. All deliveries will be associated with this period.");

        saveButton.addActionListener(e -> handleSaveDelivery());
        updateButton.addActionListener(e -> handleUpdateDelivery());
        clearFormButton.addActionListener(e -> clearDeliveryForm());
        workPeriodButton.addActionListener(e -> handleStartWorkPeriod());

        // Work period status label
        workPeriodStatusLabel = new JLabel("No active work period");
        workPeriodStatusLabel.setFont(primaryFont(Font.ITALIC, 11));
        workPeriodStatusLabel.setForeground(COLOR_TEXT_MUTED);

        bottomPanel.add(saveButton);
        bottomPanel.add(updateButton);
        bottomPanel.add(clearFormButton);
        bottomPanel.add(workPeriodButton);
        bottomPanel.add(Box.createHorizontalStrut(10));
        bottomPanel.add(workPeriodStatusLabel);

        inner.add(bottomPanel, BorderLayout.SOUTH);

        panel.add(inner, BorderLayout.CENTER);

        return panel;
    }

    private void handleSaveDelivery() {
        if (!validateDeliveryInputs()) {
            return;
        }

        // Check if there's an active work period
        if (currentWorkPeriodId == -1) {
            JOptionPane.showMessageDialog(
                    this,
                    "Please start a work period before adding deliveries.\n" +
                    "Click the 'Start Work Period' button first.",
                    "Work Period Required",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        String restaurant = deliveryRestaurantField.getText().trim();
        String date = getDeliveryDateString();
        String start = deliveryStartTimeField.getText().trim();
        String end = deliveryEndTimeField.getText().trim();
        String platform = (String) deliveryPlatformCombo.getSelectedItem();
        String carDisplay = (String) deliveryCarCombo.getSelectedItem();
        String fromAddress = deliveryFromAddressField.getText().trim();
        String toAddress = deliveryToAddressField.getText().trim();

        double miles = Double.parseDouble(deliveryMilesField.getText().trim());
        double pay = Double.parseDouble(deliveryPayField.getText().trim());
        double tip = Double.parseDouble(deliveryTipField.getText().trim());
        double total = pay + tip;

        // Convert date and time to epoch milliseconds
        long startTimeEpoch = parseDateTime(date, start);
        long endTimeEpoch = parseDateTime(date, end);

        // Save to database via serviceDispatcher if available
        if (serviceDispatcher != null) {
            boolean saved = serviceDispatcher.addDelivery(
                    restaurant,
                    (float) pay,
                    (float) tip,
                    platform,
                    (int) miles,
                    startTimeEpoch,
                    endTimeEpoch,
                    0 // wait time - can be added as a field later
            );

            if (!saved) {
                JOptionPane.showMessageDialog(
                        this,
                        "Failed to save delivery to database.",
                        "Error",
                        JOptionPane.ERROR_MESSAGE
                );
                return;
            }
        }

        // Add to local table for display
        deliveryTableModel.addRow(new Object[]{
                date, start, end, restaurant, platform, carDisplay,
                miles, pay, tip, total
        });

        updateSidebarStats();
        updateReportStats();
        updateHomeOverview();
        showAutoCloseSuccess("Delivery saved (Work Period: " + currentWorkPeriodId + ")");
    }

    /**
     * Parses date and time strings to epoch milliseconds.
     * @param date Date string in YYYY-MM-DD format
     * @param time Time string in HH:mm format
     * @return Epoch milliseconds
     */
    private long parseDateTime(String date, String time) {
        try {
            String dateTimeStr = date + "T" + time + ":00";
            LocalDateTime ldt = LocalDateTime.parse(dateTimeStr);
            return ldt.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
        } catch (DateTimeParseException e) {
            // Fallback to current time if parsing fails
            return System.currentTimeMillis();
        }
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
        String date = getDeliveryDateString();
        String start = deliveryStartTimeField.getText().trim();
        String end = deliveryEndTimeField.getText().trim();
        String platform = (String) deliveryPlatformCombo.getSelectedItem();
        String carDisplay = (String) deliveryCarCombo.getSelectedItem();

        double miles = Double.parseDouble(deliveryMilesField.getText().trim());
        double pay = Double.parseDouble(deliveryPayField.getText().trim());
        double tip = Double.parseDouble(deliveryTipField.getText().trim());
        double total = pay + tip;

        deliveryTableModel.setValueAt(date, row, 0);
        deliveryTableModel.setValueAt(start, row, 1);
        deliveryTableModel.setValueAt(end, row, 2);
        deliveryTableModel.setValueAt(restaurant, row, 3);
        deliveryTableModel.setValueAt(platform, row, 4);
        deliveryTableModel.setValueAt(carDisplay, row, 5);
        deliveryTableModel.setValueAt(miles, row, 6);
        deliveryTableModel.setValueAt(pay, row, 7);
        deliveryTableModel.setValueAt(tip, row, 8);
        deliveryTableModel.setValueAt(total, row, 9);

        updateSidebarStats();
        updateReportStats();
        updateHomeOverview();
        showAutoCloseSuccess("Delivery updated");
    }

    private boolean validateDeliveryInputs() {
        resetDeliveryFieldBorders();

        String milesText = deliveryMilesField.getText().trim();
        String payText = deliveryPayField.getText().trim();
        String tipText = deliveryTipField.getText().trim();

        boolean ok = true;

        try {
            double miles = Double.parseDouble(milesText);
            if (miles < 0) throw new NumberFormatException();
        } catch (NumberFormatException ex) {
            deliveryMilesField.setBorder(errorBorder());
            ok = false;
        }

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
                    "Enter valid numbers for miles, pay, and tip.",
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
        deliveryMilesField.setBorder(normalInputBorder);
        deliveryPayField.setBorder(normalInputBorder);
        deliveryTipField.setBorder(normalInputBorder);
    }

    private Border errorBorder() {
        return BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.RED),
                BorderFactory.createEmptyBorder(4, 6, 4, 6)
        );
    }

    /**
     * Handles starting a new work period for the current day.
     * A work period groups deliveries together and is associated with a vehicle.
     */
    private void handleStartWorkPeriod() {
        // Check if user is logged in
        if (!isEffectivelyLoggedIn()) {
            JOptionPane.showMessageDialog(
                    this,
                    "Please log in before starting a work period.",
                    "Login Required",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        // Check if a vehicle is selected
        if (deliveryCarCombo.getSelectedItem() == null) {
            JOptionPane.showMessageDialog(
                    this,
                    "Please select a vehicle from the Car dropdown before starting a work period.",
                    "Vehicle Required",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        // Check if there's already an active work period
        if (currentWorkPeriodId != -1) {
            int confirm = JOptionPane.showConfirmDialog(
                    this,
                    "You already have an active work period (ID: " + currentWorkPeriodId + ").\n" +
                    "Do you want to start a new one? The old period will be ended.",
                    "Active Work Period",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE
            );
            if (confirm != JOptionPane.YES_OPTION) {
                return;
            }
        }

        String selectedVehicle = (String) deliveryCarCombo.getSelectedItem();
        long startTime = System.currentTimeMillis(); // Start time is now

        if (serviceDispatcher != null) {
            // Use serviceDispatcher to create work period in database
            currentWorkPeriodId = serviceDispatcher.startWorkPeriod(selectedVehicle, startTime);

            if (currentWorkPeriodId != -1) {
                workPeriodButton.setText("End Work Period");
                workPeriodStatusLabel.setText("Work Period Active (ID: " + currentWorkPeriodId + ")");
                workPeriodStatusLabel.setForeground(COLOR_SUCCESS);
                showAutoCloseSuccess("Work period started for today!");
                System.out.println("FinanceAppFrame: Work period created with ID: " + currentWorkPeriodId);
            } else {
                JOptionPane.showMessageDialog(
                        this,
                        "Failed to create work period in database.",
                        "Error",
                        JOptionPane.ERROR_MESSAGE
                );
            }
        } else {
            // Fallback: create local work period ID for in-memory use
            currentWorkPeriodId = System.currentTimeMillis(); // Use timestamp as ID
            workPeriodButton.setText("End Work Period");
            workPeriodStatusLabel.setText("Work Period Active (Local ID: " + currentWorkPeriodId + ")");
            workPeriodStatusLabel.setForeground(COLOR_SUCCESS);
            showAutoCloseSuccess("Work period started (local mode)!");
            System.out.println("FinanceAppFrame: Local work period created with ID: " + currentWorkPeriodId);
        }
    }

    /**
     * Checks if there is an active work period.
     * @return true if a work period is active
     */
    public boolean hasActiveWorkPeriod() {
        return currentWorkPeriodId != -1;
    }

    /**
     * Gets the current work period ID.
     * @return the work period ID, or -1 if none active
     */
    public long getCurrentWorkPeriodId() {
        return currentWorkPeriodId;
    }

    private void clearDeliveryForm() {
        deliveryRestaurantField.setText("");
        // Reset date combo boxes to today's date
        java.time.LocalDate today = java.time.LocalDate.now();
        deliveryDateYearCombo.setSelectedItem(today.getYear());
        deliveryDateMonthCombo.setSelectedItem(today.getMonthValue());
        deliveryDateDayCombo.setSelectedItem(today.getDayOfMonth());
        deliveryStartTimeField.setText("");
        deliveryEndTimeField.setText("");
        deliveryMilesField.setText("");
        deliveryPayField.setText("");
        deliveryTipField.setText("");
        deliveryFromAddressField.setText("");
        deliveryToAddressField.setText("");
        if (deliveryPlatformCombo.getItemCount() > 0) {
            deliveryPlatformCombo.setSelectedIndex(0);
        }
        if (deliveryCarCombo.getItemCount() > 0) {
            deliveryCarCombo.setSelectedIndex(0);
        }
        resetDeliveryFieldBorders();
    }

    /**
     * Gets the delivery date as a YYYY-MM-DD string from the combo boxes.
     */
    private String getDeliveryDateString() {
        try {
            int year = (Integer) deliveryDateYearCombo.getSelectedItem();
            int month = (Integer) deliveryDateMonthCombo.getSelectedItem();
            int day = (Integer) deliveryDateDayCombo.getSelectedItem();
            // Adjust day if it exceeds the actual days in the month
            int maxDay = java.time.YearMonth.of(year, month).lengthOfMonth();
            day = Math.min(day, maxDay);
            return String.format("%04d-%02d-%02d", year, month, day);
        } catch (Exception e) {
            return java.time.LocalDate.now().toString();
        }
    }

    /**
     * Sets the delivery date combo boxes from a YYYY-MM-DD string.
     */
    private void setDeliveryDateFromString(String dateStr) {
        try {
            if (dateStr != null && !dateStr.isEmpty()) {
                java.time.LocalDate date = java.time.LocalDate.parse(dateStr);
                deliveryDateYearCombo.setSelectedItem(date.getYear());
                deliveryDateMonthCombo.setSelectedItem(date.getMonthValue());
                deliveryDateDayCombo.setSelectedItem(date.getDayOfMonth());
            }
        } catch (Exception e) {
            // If parsing fails, set to today's date
            java.time.LocalDate today = java.time.LocalDate.now();
            deliveryDateYearCombo.setSelectedItem(today.getYear());
            deliveryDateMonthCombo.setSelectedItem(today.getMonthValue());
            deliveryDateDayCombo.setSelectedItem(today.getDayOfMonth());
        }
    }

    private void loadDemoDeliveries() {
        if (deliveryCarCombo.getItemCount() == 0) {
            int carId = nextCarId++;
            String carName = "Demo Car";
            double mpg = 30.0;
            carTableModel.addRow(new Object[]{carId, carName, mpg});
            ((DefaultComboBoxModel<String>) deliveryCarCombo.getModel())
                    .addElement(carId + " - " + carName);
        }

        String carDisplay = (String) deliveryCarCombo.getItemAt(0);

        deliveryTableModel.setRowCount(0);

        // Lots of demo data: different days, platforms, restaurants, miles, amounts
        Object[][] demoRows = {
                // September
                {"2025-09-02", "11:00", "11:25", "McDonald's",      "DoorDash",  carDisplay, 4.2,  7.50,  2.00,  9.50},
                {"2025-09-02", "11:40", "12:05", "Chipotle",        "Uber Eats", carDisplay, 3.8,  6.25,  3.75, 10.00},
                {"2025-09-05", "17:15", "17:40", "Taco Bell",       "Grubhub",   carDisplay, 5.0,  5.75,  2.50,  8.25},
                {"2025-09-05", "18:00", "18:30", "Panda Express",   "DoorDash",  carDisplay, 7.1,  8.80,  3.20, 12.00},
                {"2025-09-10", "19:10", "19:45", "Local Sushi",     "Other",     carDisplay, 9.0, 10.25,  4.75, 15.00},

                // October
                {"2025-10-01", "12:05", "12:35", "Burger King",     "DoorDash",  carDisplay, 4.7,  6.80,  2.20,  9.00},
                {"2025-10-01", "13:00", "13:25", "Subway",          "Uber Eats", carDisplay, 3.2,  5.60,  2.40,  8.00},
                {"2025-10-03", "17:30", "18:00", "Domino's Pizza",  "Grubhub",   carDisplay, 6.3,  7.25,  4.00, 11.25},
                {"2025-10-03", "18:20", "18:50", "KFC",             "DoorDash",  carDisplay, 5.4,  6.90,  3.10, 10.00},
                {"2025-10-07", "20:00", "20:35", "Local Thai",      "Uber Eats", carDisplay, 8.5,  9.40,  5.10, 14.50},

                // Late October
                {"2025-10-20", "10:10", "10:35", "Starbucks",       "DoorDash",  carDisplay, 3.5,  4.80,  2.20,  7.00},
                {"2025-10-20", "10:45", "11:10", "Panera",          "Uber Eats", carDisplay, 4.0,  5.25,  2.75,  8.00},
                {"2025-10-25", "18:10", "18:40", "In-N-Out",        "DoorDash",  carDisplay, 7.9,  8.50,  4.50, 13.00},
                {"2025-10-26", "19:15", "19:45", "Five Guys",       "Grubhub",   carDisplay, 6.7,  7.75,  3.25, 11.00},

                // November
                {"2025-11-01", "12:00", "12:30", "McDonald's",      "DoorDash",  carDisplay, 4.5,  7.00,  3.00, 10.00},
                {"2025-11-02", "13:00", "13:25", "Chipotle",        "DoorDash",  carDisplay, 3.9,  6.10,  3.90, 10.00},
                {"2025-11-05", "17:05", "17:35", "Wingstop",        "Uber Eats", carDisplay, 6.0,  8.20,  4.30, 12.50},
                {"2025-11-10", "18:10", "18:40", "Local Pizza",     "DoorDash",  carDisplay, 7.8,  9.00,  5.00, 14.00},
                {"2025-11-15", "19:00", "19:40", "Panda Express",   "DoorDash",  carDisplay, 8.2,  8.90,  4.60, 13.50},
                {"2025-11-20", "11:20", "11:50", "Starbucks",       "Uber Eats", carDisplay, 3.0,  4.50,  1.50,  6.00},
                {"2025-11-22", "12:15", "12:45", "Subway",          "Grubhub",   carDisplay, 4.4,  5.50,  2.50,  8.00},
                {"2025-11-25", "13:10", "13:40", "Domino's Pizza",  "DoorDash",  carDisplay, 5.6,  7.40,  3.60, 11.00},
                {"2025-11-28", "17:00", "17:30", "McDonald's",      "DoorDash",  carDisplay, 6.0,  8.50,  3.75, 12.25},
                {"2025-11-28", "17:40", "18:05", "Chipotle",        "Uber Eats", carDisplay, 4.5,  6.75,  4.25, 11.00},
                {"2025-11-29", "12:10", "12:35", "Taco Bell",       "Grubhub",   carDisplay, 5.2,  5.25,  2.50,  7.75},
                {"2025-11-29", "13:00", "13:30", "Panda Express",   "DoorDash",  carDisplay, 7.1,  7.80,  3.20, 11.00},
                {"2025-11-30", "19:00", "19:40", "Local Pizza",     "Other",     carDisplay, 8.0,  9.00,  5.00, 14.00},

                // Early December
                {"2025-12-01", "11:45", "12:10", "Panera",          "DoorDash",  carDisplay, 4.1,  6.10,  2.90,  9.00},
                {"2025-12-02", "18:15", "18:45", "In-N-Out",        "DoorDash",  carDisplay, 7.3,  8.60,  4.40, 13.00},
                {"2025-12-03", "19:20", "19:50", "Local Thai",      "Uber Eats", carDisplay, 9.1,  9.80,  4.70, 14.50}
        };

        for (Object[] row : demoRows) {
            deliveryTableModel.addRow(row);
        }

        updateSidebarStats();
        updateReportStats();
        updateHomeOverview();
        showAutoCloseSuccess("Demo data loaded");
    }

    // =========================================================
    //   BANK ACCOUNT SCREEN
    // =========================================================

    /**
     * Creates the Bank Account management screen with three tabs:
     * - Summary: Overview of all bank accounts and financial position
     * - Select Bank Account: View and select from existing bank accounts
     * - Add New Bank Account: Form to create a new bank account
     */
    private JPanel createBankAccountScreen() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);

        JPanel inner = new JPanel(new BorderLayout());
        inner.setBackground(COLOR_BG_CARD);
        inner.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel title = new JLabel("Bank Accounts");
        title.setFont(primaryFont(Font.BOLD, 18));
        title.setForeground(COLOR_TEXT_PRIMARY);
        inner.add(title, BorderLayout.NORTH);

        // Create tabbed pane for the three sections
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setBackground(COLOR_BG_CARD);
        tabbedPane.setForeground(COLOR_TEXT_PRIMARY);
        tabbedPane.setFont(primaryFont(Font.PLAIN, 13));

        // Tab 1: Summary
        tabbedPane.addTab("Summary", createBankAccountSummaryTab());

        // Tab 2: Select Bank Account
        tabbedPane.addTab("Select Bank Account", createSelectBankAccountTab());

        // Tab 3: Add New Bank Account
        tabbedPane.addTab("Add New Bank Account", createAddBankAccountTab());

        inner.add(tabbedPane, BorderLayout.CENTER);

        panel.add(inner, BorderLayout.CENTER);
        return panel;
    }

    /**
     * Creates the Summary tab for bank accounts.
     * Displays all transactions from all bank accounts (read-only view).
     * Uses generalFinancialData to pull transaction data.
     * Transactions are automatically loaded on login.
     */
    private JPanel createBankAccountSummaryTab() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(COLOR_BG_CARD);
        panel.setBorder(BorderFactory.createEmptyBorder(15, 10, 10, 10));

        // =========================================================
        // TOP PANEL - Financial Summary Cards
        // =========================================================
        JPanel summaryCardsPanel = new JPanel(new GridLayout(1, 4, 10, 0));
        summaryCardsPanel.setBackground(COLOR_BG_CARD);
        summaryCardsPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));

        // Create value labels using class fields so they can be updated externally
        summaryAssetsValueLabel = new JLabel("$0.00");
        summaryIncomeValueLabel = new JLabel("$0.00");
        summaryExpensesValueLabel = new JLabel("$0.00");
        summaryNetValueLabel = new JLabel("$0.00");

        // Total Assets Card
        JPanel assetsCard = createSummaryCardWithLabel("Total Assets", summaryAssetsValueLabel, COLOR_SUCCESS);

        // Total Income Card
        JPanel incomeCard = createSummaryCardWithLabel("Total Income", summaryIncomeValueLabel, new Color(59, 130, 246));

        // Total Expenses Card
        JPanel expensesCard = createSummaryCardWithLabel("Total Expenses", summaryExpensesValueLabel, new Color(239, 68, 68));

        // Net Position Card
        JPanel netCard = createSummaryCardWithLabel("Net Position", summaryNetValueLabel, COLOR_ACCENT);

        summaryCardsPanel.add(assetsCard);
        summaryCardsPanel.add(incomeCard);
        summaryCardsPanel.add(expensesCard);
        summaryCardsPanel.add(netCard);

        panel.add(summaryCardsPanel, BorderLayout.NORTH);

        // =========================================================
        // CENTER PANEL - All Transactions Table
        // =========================================================
        JPanel centerPanel = new JPanel(new BorderLayout(5, 5));
        centerPanel.setBackground(COLOR_BG_CARD);

        // Table title
        JLabel tableTitle = new JLabel("All Transactions (All Accounts)");
        tableTitle.setFont(primaryFont(Font.BOLD, 14));
        tableTitle.setForeground(COLOR_TEXT_PRIMARY);
        tableTitle.setBorder(BorderFactory.createEmptyBorder(0, 0, 8, 0));
        centerPanel.add(tableTitle, BorderLayout.NORTH);

        // Transactions table - read-only, use class field
        // Column order: Description (first), Date, Type, Amount, Account
        String[] columnNames = {"Description", "Date", "Type", "Amount", "Account"};
        summaryTransactionTableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Read-only
            }
        };
        JTable summaryTable = new JTable(summaryTransactionTableModel);
        summaryTable.setBackground(COLOR_BG_INPUT);
        summaryTable.setForeground(COLOR_TEXT_PRIMARY);
        summaryTable.setSelectionBackground(COLOR_ACCENT);
        summaryTable.setSelectionForeground(Color.BLACK);
        summaryTable.setGridColor(COLOR_BORDER);
        summaryTable.setRowHeight(26);
        summaryTable.getTableHeader().setBackground(COLOR_BG_MAIN);
        summaryTable.getTableHeader().setForeground(COLOR_TEXT_PRIMARY);

        // Set column widths - Description gets more space
        summaryTable.getColumnModel().getColumn(0).setPreferredWidth(200); // Description
        summaryTable.getColumnModel().getColumn(1).setPreferredWidth(100); // Date
        summaryTable.getColumnModel().getColumn(2).setPreferredWidth(120); // Type
        summaryTable.getColumnModel().getColumn(3).setPreferredWidth(100); // Amount
        summaryTable.getColumnModel().getColumn(4).setPreferredWidth(80);  // Account

        // Custom renderer for Amount column (index 3) to show color based on positive/negative
        summaryTable.getColumnModel().getColumn(3).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if (!isSelected) {
                    String amountStr = value != null ? value.toString() : "";
                    if (amountStr.startsWith("-") || amountStr.startsWith("($")) {
                        c.setForeground(new Color(239, 68, 68)); // Red for negative
                    } else {
                        c.setForeground(COLOR_SUCCESS); // Green for positive
                    }
                }
                setHorizontalAlignment(SwingConstants.RIGHT);
                return c;
            }
        });

        // Custom renderer for Type column (index 2) with color coding
        summaryTable.getColumnModel().getColumn(2).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if (!isSelected) {
                    String type = value != null ? value.toString().toLowerCase() : "";
                    if (type.contains("income")) {
                        c.setForeground(COLOR_SUCCESS);
                    } else if (type.equals("purchase") || type.equals("withdrawal")) {
                        c.setForeground(new Color(239, 68, 68));
                    } else {
                        c.setForeground(COLOR_TEXT_PRIMARY);
                    }
                }
                return c;
            }
        });

        JScrollPane tableScroll = new JScrollPane(summaryTable);
        tableScroll.getViewport().setBackground(COLOR_BG_INPUT);
        centerPanel.add(tableScroll, BorderLayout.CENTER);

        // Transaction count label - use class field
        summaryTransactionCountLabel = new JLabel("Total Transactions: 0");
        summaryTransactionCountLabel.setFont(primaryFont(Font.PLAIN, 12));
        summaryTransactionCountLabel.setForeground(COLOR_TEXT_SECONDARY);
        summaryTransactionCountLabel.setBorder(BorderFactory.createEmptyBorder(8, 0, 0, 0));
        centerPanel.add(summaryTransactionCountLabel, BorderLayout.SOUTH);

        panel.add(centerPanel, BorderLayout.CENTER);

        // =========================================================
        // BOTTOM PANEL - Filter Only (transactions auto-load on login)
        // =========================================================
        JPanel buttonPanel = new JPanel();
        buttonPanel.setBackground(COLOR_BG_CARD);
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));

        // Filter combo box
        JLabel filterLabel = new JLabel("Filter by Type:");
        filterLabel.setForeground(COLOR_TEXT_SECONDARY);
        filterLabel.setFont(primaryFont(Font.PLAIN, 12));

        String[] filterOptions = {"All Transactions", "Income Only", "Expenses Only", "Purchases", "Withdrawals", "Delivery Income", "Other Income"};
        JComboBox<String> filterCombo = new JComboBox<>(filterOptions);
        styleComboBox(filterCombo);

        filterCombo.addActionListener(e -> {
            loadSummaryTransactions((String) filterCombo.getSelectedItem());
        });

        // Optional refresh button
        JButton refreshButton = new JButton("Refresh");
        styleSecondaryButton(refreshButton);
        refreshButton.addActionListener(e -> {
            loadSummaryTransactions((String) filterCombo.getSelectedItem());
        });

        buttonPanel.add(filterLabel);
        buttonPanel.add(filterCombo);
        buttonPanel.add(Box.createHorizontalStrut(20));
        buttonPanel.add(refreshButton);

        panel.add(buttonPanel, BorderLayout.SOUTH);

        return panel;
    }

    /**
     * Creates a summary card for displaying financial metrics with a pre-created value label.
     * This allows external code to update the value label directly.
     */
    private JPanel createSummaryCardWithLabel(String title, JLabel valueLabel, Color valueColor) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(COLOR_BG_MAIN);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(COLOR_BORDER),
                BorderFactory.createEmptyBorder(12, 12, 12, 12)
        ));

        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBackground(COLOR_BG_MAIN);

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(primaryFont(Font.PLAIN, 11));
        titleLabel.setForeground(COLOR_TEXT_SECONDARY);
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Style the provided value label
        valueLabel.setFont(primaryFont(Font.BOLD, 18));
        valueLabel.setForeground(valueColor);
        valueLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        contentPanel.add(titleLabel);
        contentPanel.add(Box.createVerticalStrut(5));
        contentPanel.add(valueLabel);

        card.add(contentPanel, BorderLayout.CENTER);
        return card;
    }

    /**
     * Creates a summary card for displaying financial metrics.
     */
    private JPanel createSummaryCard(String title, String value, Color valueColor) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(COLOR_BG_MAIN);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(COLOR_BORDER),
                BorderFactory.createEmptyBorder(12, 12, 12, 12)
        ));

        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBackground(COLOR_BG_MAIN);

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(primaryFont(Font.PLAIN, 11));
        titleLabel.setForeground(COLOR_TEXT_SECONDARY);
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel valueLabel = new JLabel(value);
        valueLabel.setFont(primaryFont(Font.BOLD, 18));
        valueLabel.setForeground(valueColor);
        valueLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        contentPanel.add(titleLabel);
        contentPanel.add(Box.createVerticalStrut(5));
        contentPanel.add(valueLabel);

        card.add(contentPanel, BorderLayout.CENTER);
        return card;
    }

    /**
     * Loads all transactions summary for the current user.
     */
    private void loadAllTransactionsSummary(DefaultTableModel tableModel, JLabel assetsLabel,
            JLabel incomeLabel, JLabel expensesLabel, JLabel netLabel, JLabel countLabel, String filter) {

        if (serviceDispatcher == null || !isEffectivelyLoggedIn()) {
            JOptionPane.showMessageDialog(this,
                    "Please log in to view your financial summary.",
                    "Login Required",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            // Get summary values
            float totalAssets = serviceDispatcher.getCurrentUserTotalAssets();
            float totalIncome = serviceDispatcher.getCurrentUserTotalIncome();
            float totalExpenses = serviceDispatcher.getCurrentUserTotalExpenses();
            float netPosition = totalAssets + totalIncome - totalExpenses;

            // Update summary cards
            assetsLabel.setText(String.format("$%.2f", totalAssets));
            incomeLabel.setText(String.format("$%.2f", totalIncome));
            expensesLabel.setText(String.format("$%.2f", totalExpenses));
            netLabel.setText(String.format("%s$%.2f", netPosition < 0 ? "-" : "", Math.abs(netPosition)));
            netLabel.setForeground(netPosition >= 0 ? COLOR_SUCCESS : new Color(239, 68, 68));

            // Get all transactions
            List<org.example.manageFinances.src.generalFinancialData.TransactionSummary> transactions =
                    serviceDispatcher.getCurrentUserAllTransactionDetails();

            tableModel.setRowCount(0); // Clear table

            int displayedCount = 0;
            for (org.example.manageFinances.src.generalFinancialData.TransactionSummary tx : transactions) {
                // Apply filter
                String type = tx.getTransactionType() != null ? tx.getTransactionType().toLowerCase() : "";
                boolean include = false;

                switch (filter) {
                    case "All Transactions":
                        include = true;
                        break;
                    case "Income Only":
                        include = type.contains("income");
                        break;
                    case "Expenses Only":
                        include = type.equals("purchase") || type.equals("withdrawal");
                        break;
                    case "Purchases":
                        include = type.equals("purchase");
                        break;
                    case "Withdrawals":
                        include = type.equals("withdrawal");
                        break;
                    case "Delivery Income":
                        include = type.equals("delivery income");
                        break;
                    case "Other Income":
                        include = type.equals("other income");
                        break;
                    default:
                        include = true;
                }

                if (include) {
                    String amountStr;
                    float amount = tx.getAmount();
                    if (amount < 0) {
                        amountStr = String.format("($%.2f)", Math.abs(amount));
                    } else {
                        amountStr = String.format("$%.2f", amount);
                    }

                    tableModel.addRow(new Object[]{
                            tx.getTransactionId(),
                            tx.getTransactionDate() != null ? tx.getTransactionDate() : "-",
                            tx.getTransactionType() != null ? tx.getTransactionType() : "-",
                            amountStr,
                            tx.getBankAccountId() > 0 ? tx.getBankAccountId() : "-",
                            tx.getDescription() != null ? tx.getDescription() : "-"
                    });
                    displayedCount++;
                }
            }

            countLabel.setText("Total Transactions: " + transactions.size() +
                    (displayedCount != transactions.size() ? " (Showing: " + displayedCount + ")" : ""));

            if (transactions.isEmpty()) {
                showAutoCloseSuccess("No transactions found.");
            } else {
                showAutoCloseSuccess("Loaded " + transactions.size() + " transaction(s).");
            }

        } catch (Exception ex) {
            System.err.println("Error loading transactions summary: " + ex.getMessage());
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    "Error loading summary: " + ex.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Loads summary transactions using class field components.
     * Called on login and when filter changes.
     * @param filter The filter to apply (e.g., "All Transactions", "Income Only")
     */
    private void loadSummaryTransactions(String filter) {
        if (serviceDispatcher == null || !isEffectivelyLoggedIn()) {
            return; // Silently return if not logged in
        }

        if (summaryTransactionTableModel == null) {
            return; // UI not initialized yet
        }

        try {
            // Get summary values
            float totalAssets = serviceDispatcher.getCurrentUserTotalAssets();
            float totalIncome = serviceDispatcher.getCurrentUserTotalIncome();
            float totalExpenses = serviceDispatcher.getCurrentUserTotalExpenses();
            float netPosition = totalAssets + totalIncome - totalExpenses;

            // Update summary cards
            if (summaryAssetsValueLabel != null) {
                summaryAssetsValueLabel.setText(String.format("$%.2f", totalAssets));
            }
            if (summaryIncomeValueLabel != null) {
                summaryIncomeValueLabel.setText(String.format("$%.2f", totalIncome));
            }
            if (summaryExpensesValueLabel != null) {
                summaryExpensesValueLabel.setText(String.format("$%.2f", totalExpenses));
            }
            if (summaryNetValueLabel != null) {
                summaryNetValueLabel.setText(String.format("%s$%.2f", netPosition < 0 ? "-" : "", Math.abs(netPosition)));
                summaryNetValueLabel.setForeground(netPosition >= 0 ? COLOR_SUCCESS : new Color(239, 68, 68));
            }

            // Get all transactions
            List<org.example.manageFinances.src.generalFinancialData.TransactionSummary> transactions =
                    serviceDispatcher.getCurrentUserAllTransactionDetails();

            summaryTransactionTableModel.setRowCount(0); // Clear table

            int displayedCount = 0;
            for (org.example.manageFinances.src.generalFinancialData.TransactionSummary tx : transactions) {
                // Apply filter
                String type = tx.getTransactionType() != null ? tx.getTransactionType().toLowerCase() : "";
                boolean include = false;

                if (filter == null) filter = "All Transactions";

                switch (filter) {
                    case "All Transactions":
                        include = true;
                        break;
                    case "Income Only":
                        include = type.contains("income");
                        break;
                    case "Expenses Only":
                        include = type.equals("purchase") || type.equals("withdrawal");
                        break;
                    case "Purchases":
                        include = type.equals("purchase");
                        break;
                    case "Withdrawals":
                        include = type.equals("withdrawal");
                        break;
                    case "Delivery Income":
                        include = type.equals("delivery income");
                        break;
                    case "Other Income":
                        include = type.equals("other income");
                        break;
                    default:
                        include = true;
                }

                if (include) {
                    String amountStr;
                    float amount = tx.getAmount();
                    if (amount < 0) {
                        amountStr = String.format("($%.2f)", Math.abs(amount));
                    } else {
                        amountStr = String.format("$%.2f", amount);
                    }

                    // Column order: Description, Date, Type, Amount, Account
                    summaryTransactionTableModel.addRow(new Object[]{
                            tx.getDescription() != null ? tx.getDescription() : "-",
                            tx.getTransactionDate() != null ? tx.getTransactionDate() : "-",
                            tx.getTransactionType() != null ? tx.getTransactionType() : "-",
                            amountStr,
                            tx.getBankAccountId() > 0 ? tx.getBankAccountId() : "-"
                    });
                    displayedCount++;
                }
            }

            if (summaryTransactionCountLabel != null) {
                summaryTransactionCountLabel.setText("Total Transactions: " + transactions.size() +
                        (displayedCount != transactions.size() ? " (Showing: " + displayedCount + ")" : ""));
            }

            System.out.println("FinanceAppFrame: Loaded " + transactions.size() + " summary transactions.");

        } catch (Exception ex) {
            System.err.println("Error loading summary transactions: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    /**
     * Creates the Select Bank Account tab.
     * Displays user's bank accounts and shows transactions for the selected account.
     */
    private JPanel createSelectBankAccountTab() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(COLOR_BG_CARD);
        panel.setBorder(BorderFactory.createEmptyBorder(15, 10, 10, 10));

        // Description
        JLabel desc = new JLabel("Select a bank account to view details and transactions:");
        desc.setFont(primaryFont(Font.PLAIN, 12));
        desc.setForeground(COLOR_TEXT_SECONDARY);
        desc.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(desc, BorderLayout.NORTH);

        // Main split panel - left for accounts, right for transactions
        JPanel mainPanel = new JPanel(new GridLayout(1, 2, 10, 0));
        mainPanel.setBackground(COLOR_BG_CARD);

        // =========================================================
        // LEFT PANEL - Bank Accounts List and Details
        // =========================================================
        JPanel leftPanel = new JPanel(new BorderLayout(5, 5));
        leftPanel.setBackground(COLOR_BG_CARD);

        // Table for bank accounts - use class field
        String[] columnNames = {"Account ID", "Account Name", "Account Type", "Balance"};
        bankAccountTableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        JTable bankAccountTable = new JTable(bankAccountTableModel);
        bankAccountTable.setBackground(COLOR_BG_INPUT);
        bankAccountTable.setForeground(COLOR_TEXT_PRIMARY);
        bankAccountTable.setSelectionBackground(COLOR_ACCENT);
        bankAccountTable.setSelectionForeground(Color.BLACK);
        bankAccountTable.setGridColor(COLOR_BORDER);
        bankAccountTable.setRowHeight(28);
        bankAccountTable.getTableHeader().setBackground(COLOR_BG_MAIN);
        bankAccountTable.getTableHeader().setForeground(COLOR_TEXT_PRIMARY);

        JScrollPane accountTableScroll = new JScrollPane(bankAccountTable);
        accountTableScroll.setPreferredSize(new Dimension(350, 150));
        accountTableScroll.getViewport().setBackground(COLOR_BG_INPUT);

        JPanel accountTablePanel = new JPanel(new BorderLayout());
        accountTablePanel.setBackground(COLOR_BG_CARD);
        accountTablePanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(COLOR_BORDER),
                "Bank Accounts",
                0, 0,
                primaryFont(Font.BOLD, 12),
                COLOR_TEXT_SECONDARY
        ));
        accountTablePanel.add(accountTableScroll, BorderLayout.CENTER);
        leftPanel.add(accountTablePanel, BorderLayout.CENTER);

        // Details panel - shows selected account details
        JPanel detailsPanel = new JPanel(new GridLayout(4, 2, 10, 5));
        detailsPanel.setBackground(COLOR_BG_MAIN);
        detailsPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(
                        BorderFactory.createLineBorder(COLOR_BORDER),
                        "Account Details",
                        0, 0,
                        primaryFont(Font.BOLD, 12),
                        COLOR_TEXT_SECONDARY
                ),
                BorderFactory.createEmptyBorder(8, 8, 8, 8)
        ));

        // Detail labels - use class fields
        JLabel idLabel = new JLabel("Account ID:");
        bankAccountIdValue = new JLabel("-");
        JLabel nameLabel = new JLabel("Account Name:");
        bankAccountNameValue = new JLabel("-");
        JLabel typeLabel = new JLabel("Account Type:");
        bankAccountTypeValue = new JLabel("-");
        JLabel balanceLabel = new JLabel("Balance:");
        bankAccountBalanceValue = new JLabel("-");

        styleFormLabel(idLabel);
        styleFormLabel(nameLabel);
        styleFormLabel(typeLabel);
        styleFormLabel(balanceLabel);

        bankAccountIdValue.setForeground(COLOR_TEXT_PRIMARY);
        bankAccountNameValue.setForeground(COLOR_TEXT_PRIMARY);
        bankAccountTypeValue.setForeground(COLOR_TEXT_PRIMARY);
        bankAccountBalanceValue.setForeground(COLOR_SUCCESS);

        detailsPanel.add(idLabel);
        detailsPanel.add(bankAccountIdValue);
        detailsPanel.add(nameLabel);
        detailsPanel.add(bankAccountNameValue);
        detailsPanel.add(typeLabel);
        detailsPanel.add(bankAccountTypeValue);
        detailsPanel.add(balanceLabel);
        detailsPanel.add(bankAccountBalanceValue);

        leftPanel.add(detailsPanel, BorderLayout.SOUTH);

        // =========================================================
        // RIGHT PANEL - Transactions for Selected Account
        // =========================================================
        JPanel rightPanel = new JPanel(new BorderLayout(5, 5));
        rightPanel.setBackground(COLOR_BG_CARD);

        // Transactions table - use class field
        String[] transactionColumns = {"Date", "Type", "Amount", "Description"};
        bankTransactionTableModel = new DefaultTableModel(transactionColumns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        JTable transactionTable = new JTable(bankTransactionTableModel);
        transactionTable.setBackground(COLOR_BG_INPUT);
        transactionTable.setForeground(COLOR_TEXT_PRIMARY);
        transactionTable.setSelectionBackground(COLOR_ACCENT);
        transactionTable.setSelectionForeground(Color.BLACK);
        transactionTable.setGridColor(COLOR_BORDER);
        transactionTable.setRowHeight(26);
        transactionTable.getTableHeader().setBackground(COLOR_BG_MAIN);
        transactionTable.getTableHeader().setForeground(COLOR_TEXT_PRIMARY);

        // Custom renderer for amount column to show color based on positive/negative
        transactionTable.getColumnModel().getColumn(2).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if (!isSelected) {
                    String amountStr = value != null ? value.toString() : "";
                    if (amountStr.startsWith("-") || amountStr.startsWith("($")) {
                        c.setForeground(new Color(239, 68, 68)); // Red for negative
                    } else {
                        c.setForeground(COLOR_SUCCESS); // Green for positive
                    }
                }
                setHorizontalAlignment(SwingConstants.RIGHT);
                return c;
            }
        });

        JScrollPane transactionTableScroll = new JScrollPane(transactionTable);
        transactionTableScroll.getViewport().setBackground(COLOR_BG_INPUT);

        JPanel transactionTablePanel = new JPanel(new BorderLayout());
        transactionTablePanel.setBackground(COLOR_BG_CARD);
        transactionTablePanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(COLOR_BORDER),
                "Account Transactions",
                0, 0,
                primaryFont(Font.BOLD, 12),
                COLOR_TEXT_SECONDARY
        ));
        transactionTablePanel.add(transactionTableScroll, BorderLayout.CENTER);

        // Transaction summary panel
        JPanel transactionSummaryPanel = new JPanel(new GridLayout(1, 4, 10, 0));
        transactionSummaryPanel.setBackground(COLOR_BG_MAIN);
        transactionSummaryPanel.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        // Use class fields for labels
        bankTotalIncomeLabel = new JLabel("Total Income: $0.00");
        bankTotalExpensesLabel = new JLabel("Total Expenses: $0.00");
        bankNetLabel = new JLabel("Net: $0.00");
        bankTransactionCountLabel = new JLabel("Transactions: 0");

        bankTotalIncomeLabel.setForeground(COLOR_SUCCESS);
        bankTotalExpensesLabel.setForeground(new Color(239, 68, 68));
        bankNetLabel.setForeground(COLOR_TEXT_PRIMARY);
        bankTransactionCountLabel.setForeground(COLOR_TEXT_SECONDARY);

        bankTotalIncomeLabel.setFont(primaryFont(Font.BOLD, 11));
        bankTotalExpensesLabel.setFont(primaryFont(Font.BOLD, 11));
        bankNetLabel.setFont(primaryFont(Font.BOLD, 11));
        bankTransactionCountLabel.setFont(primaryFont(Font.PLAIN, 11));

        transactionSummaryPanel.add(bankTotalIncomeLabel);
        transactionSummaryPanel.add(bankTotalExpensesLabel);
        transactionSummaryPanel.add(bankNetLabel);
        transactionSummaryPanel.add(bankTransactionCountLabel);

        rightPanel.add(transactionTablePanel, BorderLayout.CENTER);
        rightPanel.add(transactionSummaryPanel, BorderLayout.SOUTH);

        mainPanel.add(leftPanel);
        mainPanel.add(rightPanel);

        panel.add(mainPanel, BorderLayout.CENTER);

        // =========================================================
        // BUTTON PANEL
        // =========================================================
        JPanel buttonPanel = new JPanel();
        buttonPanel.setBackground(COLOR_BG_CARD);

        JButton refreshButton = new JButton("Refresh Accounts");
        JButton viewTransactionsButton = new JButton("View Transactions");

        styleSecondaryButton(refreshButton);
        stylePrimaryButton(viewTransactionsButton);

        // Refresh button - loads accounts from database
        refreshButton.addActionListener(e -> loadBankAccountsFromDatabase());

        // Table selection listener - updates details panel and loads transactions
        bankAccountTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int selectedRow = bankAccountTable.getSelectedRow();
                if (selectedRow >= 0) {
                    // Update details panel
                    String accountId = String.valueOf(bankAccountTableModel.getValueAt(selectedRow, 0));
                    bankAccountIdValue.setText(accountId);
                    bankAccountNameValue.setText(String.valueOf(bankAccountTableModel.getValueAt(selectedRow, 1)));
                    bankAccountTypeValue.setText(String.valueOf(bankAccountTableModel.getValueAt(selectedRow, 2)));
                    bankAccountBalanceValue.setText(String.valueOf(bankAccountTableModel.getValueAt(selectedRow, 3)));

                    // Load transactions for selected account
                    loadTransactionsForAccount(
                            accountId,
                            bankTransactionTableModel,
                            bankTotalIncomeLabel,
                            bankTotalExpensesLabel,
                            bankNetLabel,
                            bankTransactionCountLabel
                    );
                }
            }
        });

        // View Transactions button - explicitly loads transactions for selected account
        viewTransactionsButton.addActionListener(e -> {
            int selectedRow = bankAccountTable.getSelectedRow();
            if (selectedRow >= 0) {
                String accountId = String.valueOf(bankAccountTableModel.getValueAt(selectedRow, 0));
                loadTransactionsForAccount(
                        accountId,
                        bankTransactionTableModel,
                        bankTotalIncomeLabel,
                        bankTotalExpensesLabel,
                        bankNetLabel,
                        bankTransactionCountLabel
                );
            } else {
                JOptionPane.showMessageDialog(this,
                        "Please select a bank account from the table.",
                        "No Selection",
                        JOptionPane.WARNING_MESSAGE);
            }
        });

        // Add Expense button
        JButton addExpenseButton = new JButton("Add Expense");
        styleSecondaryButton(addExpenseButton);
        addExpenseButton.setBackground(new Color(220, 38, 38)); // Red for expense
        addExpenseButton.addActionListener(e -> {
            int selectedRow = bankAccountTable.getSelectedRow();
            if (selectedRow < 0) {
                JOptionPane.showMessageDialog(this,
                        "Please select a bank account first.",
                        "No Account Selected",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }
            String accountId = String.valueOf(bankAccountTableModel.getValueAt(selectedRow, 0));
            showAddTransactionDialog(false, accountId, bankTransactionTableModel,
                    bankTotalIncomeLabel, bankTotalExpensesLabel, bankNetLabel, bankTransactionCountLabel);
        });

        // Add Income button
        JButton addIncomeButton = new JButton("Add Income");
        styleSecondaryButton(addIncomeButton);
        addIncomeButton.setBackground(new Color(22, 163, 74)); // Green for income
        addIncomeButton.addActionListener(e -> {
            int selectedRow = bankAccountTable.getSelectedRow();
            if (selectedRow < 0) {
                JOptionPane.showMessageDialog(this,
                        "Please select a bank account first.",
                        "No Account Selected",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }
            String accountId = String.valueOf(bankAccountTableModel.getValueAt(selectedRow, 0));
            showAddTransactionDialog(true, accountId, bankTransactionTableModel,
                    bankTotalIncomeLabel, bankTotalExpensesLabel, bankNetLabel, bankTransactionCountLabel);
        });

        buttonPanel.add(refreshButton);
        buttonPanel.add(viewTransactionsButton);
        buttonPanel.add(Box.createHorizontalStrut(20)); // Spacer
        buttonPanel.add(addExpenseButton);
        buttonPanel.add(addIncomeButton);

        panel.add(buttonPanel, BorderLayout.SOUTH);

        return panel;
    }

    /**
     * Shows a dialog to add a new transaction (expense or income) for a bank account.
     * @param isIncome true for income, false for expense
     * @param accountId The bank account ID to associate the transaction with
     * @param transactionTableModel The table model to refresh after adding
     * @param totalIncomeLabel Label to update
     * @param totalExpensesLabel Label to update
     * @param netLabel Label to update
     * @param transactionCountLabel Label to update
     */
    private void showAddTransactionDialog(boolean isIncome, String accountId,
            DefaultTableModel transactionTableModel, JLabel totalIncomeLabel,
            JLabel totalExpensesLabel, JLabel netLabel, JLabel transactionCountLabel) {

        String title = isIncome ? "Add Income" : "Add Expense";
        Color titleColor = isIncome ? COLOR_SUCCESS : new Color(239, 68, 68);

        JDialog dialog = new JDialog(this, title, true);
        dialog.setSize(400, 300);
        dialog.setLocationRelativeTo(this);
        dialog.getContentPane().setBackground(COLOR_BG_CARD);

        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBackground(COLOR_BG_CARD);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // Title label
        JLabel titleLabel = new JLabel(title + " for Account #" + accountId);
        titleLabel.setFont(primaryFont(Font.BOLD, 16));
        titleLabel.setForeground(titleColor);
        mainPanel.add(titleLabel, BorderLayout.NORTH);

        // Form panel
        JPanel formPanel = new JPanel(new GridLayout(3, 2, 10, 10));
        formPanel.setBackground(COLOR_BG_MAIN);
        formPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // Amount field
        JLabel amountLabel = new JLabel("Amount ($):");
        styleFormLabel(amountLabel);
        JTextField amountField = createInputField();
        amountField.setText("0.00");

        // Type selection
        JLabel typeLabel = new JLabel("Type:");
        styleFormLabel(typeLabel);
        String[] incomeTypes = {"delivery income", "other income"};
        String[] expenseTypes = {"purchase", "withdrawal"};
        JComboBox<String> typeCombo = new JComboBox<>(isIncome ? incomeTypes : expenseTypes);
        styleComboBox(typeCombo);

        // Description field
        JLabel descLabel = new JLabel("Description:");
        styleFormLabel(descLabel);
        JTextField descField = createInputField();
        descField.setToolTipText("Optional description for this transaction");

        formPanel.add(amountLabel);
        formPanel.add(amountField);
        formPanel.add(typeLabel);
        formPanel.add(typeCombo);
        formPanel.add(descLabel);
        formPanel.add(descField);

        mainPanel.add(formPanel, BorderLayout.CENTER);

        // Button panel
        JPanel buttonPanel = new JPanel();
        buttonPanel.setBackground(COLOR_BG_CARD);

        JButton saveButton = new JButton("Save " + (isIncome ? "Income" : "Expense"));
        JButton cancelButton = new JButton("Cancel");

        stylePrimaryButton(saveButton);
        styleSecondaryButton(cancelButton);

        if (isIncome) {
            saveButton.setBackground(new Color(22, 163, 74)); // Green
        } else {
            saveButton.setBackground(new Color(220, 38, 38)); // Red
        }

        saveButton.addActionListener(ev -> {
            try {
                String amountText = amountField.getText().trim().replace("$", "").replace(",", "");
                float amount = Float.parseFloat(amountText);

                if (amount <= 0) {
                    JOptionPane.showMessageDialog(dialog,
                            "Please enter a positive amount.",
                            "Invalid Amount",
                            JOptionPane.WARNING_MESSAGE);
                    return;
                }

                String transactionType = (String) typeCombo.getSelectedItem();
                String description = descField.getText().trim();
                if (description.isEmpty()) {
                    description = null;
                }

                int bankAccountId = Integer.parseInt(accountId);

                // Save to database via serviceDispatcher
                if (serviceDispatcher != null) {
                    serviceDispatcher.addTransactionForBankAccount(amount, transactionType, bankAccountId, description);
                    showAutoCloseSuccess((isIncome ? "Income" : "Expense") + " of $" + String.format("%.2f", amount) + " added successfully!");

                    // Refresh the transactions table
                    loadTransactionsForAccount(accountId, transactionTableModel,
                            totalIncomeLabel, totalExpensesLabel, netLabel, transactionCountLabel);

                    dialog.dispose();
                } else {
                    JOptionPane.showMessageDialog(dialog,
                            "Service not available. Please try again.",
                            "Error",
                            JOptionPane.ERROR_MESSAGE);
                }

            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dialog,
                        "Please enter a valid amount.",
                        "Invalid Input",
                        JOptionPane.WARNING_MESSAGE);
            } catch (Exception ex) {
                System.err.println("Error saving transaction: " + ex.getMessage());
                ex.printStackTrace();
                JOptionPane.showMessageDialog(dialog,
                        "Error saving transaction: " + ex.getMessage(),
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        });

        cancelButton.addActionListener(ev -> dialog.dispose());

        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);

        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        dialog.add(mainPanel);
        dialog.setVisible(true);
    }

    /**
     * Loads transactions for a specific bank account and updates the UI.
     */
    private void loadTransactionsForAccount(String accountId, DefaultTableModel transactionTableModel,
            JLabel totalIncomeLabel, JLabel totalExpensesLabel, JLabel netLabel, JLabel transactionCountLabel) {
        if (serviceDispatcher == null) {
            return;
        }

        try {
            int bankAccountId = Integer.parseInt(accountId);
            List<org.example.manageFinances.src.generalFinancialDataDAO.TransactionRecord> transactions =
                    serviceDispatcher.getTransactionsForBankAccount(bankAccountId);

            transactionTableModel.setRowCount(0); // Clear table

            float totalIncome = 0;
            float totalExpenses = 0;

            for (org.example.manageFinances.src.generalFinancialDataDAO.TransactionRecord tx : transactions) {
                String amountStr;
                float amount = tx.getAmount();
                if (amount < 0) {
                    amountStr = String.format("($%.2f)", Math.abs(amount));
                    totalExpenses += Math.abs(amount);
                } else {
                    amountStr = String.format("$%.2f", amount);
                    totalIncome += amount;
                }

                transactionTableModel.addRow(new Object[]{
                        tx.getTransactionDate() != null ? tx.getTransactionDate() : "-",
                        tx.getTransactionType(),
                        amountStr,
                        tx.getDescription() != null ? tx.getDescription() : "-"
                });
            }

            // Update summary labels
            totalIncomeLabel.setText(String.format("Total Income: $%.2f", totalIncome));
            totalExpensesLabel.setText(String.format("Total Expenses: $%.2f", totalExpenses));
            float net = totalIncome - totalExpenses;
            netLabel.setText(String.format("Net: %s$%.2f", net < 0 ? "-" : "", Math.abs(net)));
            netLabel.setForeground(net >= 0 ? COLOR_SUCCESS : new Color(239, 68, 68));
            transactionCountLabel.setText("Transactions: " + transactions.size());

            if (transactions.isEmpty()) {
                showAutoCloseSuccess("No transactions found for this account.");
            }

        } catch (NumberFormatException ex) {
            System.err.println("Invalid account ID: " + accountId);
        } catch (Exception ex) {
            System.err.println("Error loading transactions: " + ex.getMessage());
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    "Error loading transactions: " + ex.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Creates the Add New Bank Account tab with form fields from addBankAccount class.
     */
    private JPanel createAddBankAccountTab() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(COLOR_BG_CARD);
        panel.setBorder(BorderFactory.createEmptyBorder(15, 10, 10, 10));

        JPanel formContainer = new JPanel();
        formContainer.setBackground(COLOR_BG_CARD);
        formContainer.setLayout(new BoxLayout(formContainer, BoxLayout.Y_AXIS));

        JLabel desc = new JLabel("Enter the details for your new bank account:");
        desc.setFont(primaryFont(Font.PLAIN, 12));
        desc.setForeground(COLOR_TEXT_SECONDARY);
        desc.setAlignmentX(Component.LEFT_ALIGNMENT);
        formContainer.add(desc);
        formContainer.add(Box.createVerticalStrut(15));

        // Form panel with grid layout
        JPanel formPanel = new JPanel(new GridLayout(6, 2, 10, 10));
        formPanel.setBackground(COLOR_BG_MAIN);
        formPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(COLOR_BORDER),
                BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));
        formPanel.setMaximumSize(new Dimension(500, 250));
        formPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Account Name
        JLabel accountNameLabel = new JLabel("Account Name");
        styleFormLabel(accountNameLabel);
        JTextField accountNameField = createInputField();
        accountNameField.setToolTipText("Enter a name for this account (e.g., 'Main Checking')");

        // Account Type
        JLabel accountTypeLabel = new JLabel("Account Type");
        styleFormLabel(accountTypeLabel);
        String[] accountTypes = {"Checking", "Savings", "Credit Card", "Investment", "Other"};
        JComboBox<String> accountTypeCombo = new JComboBox<>(accountTypes);
        styleComboBox(accountTypeCombo);

        // Initial Balance
        JLabel balanceLabel = new JLabel("Initial Balance ($)");
        styleFormLabel(balanceLabel);
        JTextField balanceField = createInputField();
        balanceField.setText("0.00");
        balanceField.setToolTipText("Enter the current balance of this account");

        // Interest Rate
        JLabel interestRateLabel = new JLabel("Interest Rate (%)");
        styleFormLabel(interestRateLabel);
        JTextField interestRateField = createInputField();
        interestRateField.setText("0.00");
        interestRateField.setToolTipText("Annual interest rate (e.g., 2.5 for 2.5%)");

        // Account Fees
        JLabel accountFeesLabel = new JLabel("Monthly Fees ($)");
        styleFormLabel(accountFeesLabel);
        JTextField accountFeesField = createInputField();
        accountFeesField.setText("0.00");
        accountFeesField.setToolTipText("Monthly account maintenance fees");

        // Other Income
        JLabel otherIncomeLabel = new JLabel("Other Income ($)");
        styleFormLabel(otherIncomeLabel);
        JTextField otherIncomeField = createInputField();
        otherIncomeField.setText("0.00");
        otherIncomeField.setToolTipText("Any other regular income associated with this account");

        // Add components to form
        formPanel.add(accountNameLabel);
        formPanel.add(accountNameField);
        formPanel.add(accountTypeLabel);
        formPanel.add(accountTypeCombo);
        formPanel.add(balanceLabel);
        formPanel.add(balanceField);
        formPanel.add(interestRateLabel);
        formPanel.add(interestRateField);
        formPanel.add(accountFeesLabel);
        formPanel.add(accountFeesField);
        formPanel.add(otherIncomeLabel);
        formPanel.add(otherIncomeField);

        formContainer.add(formPanel);
        formContainer.add(Box.createVerticalStrut(15));

        // Create Bank Account Button
        JButton createAccountButton = new JButton("Create Bank Account");
        createAccountButton.setAlignmentX(Component.LEFT_ALIGNMENT);
        stylePrimaryButton(createAccountButton);

        createAccountButton.addActionListener(e -> {
            // Validate inputs
            String accountName = accountNameField.getText().trim();
            String accountType = (String) accountTypeCombo.getSelectedItem();

            if (accountName.isEmpty()) {
                JOptionPane.showMessageDialog(this,
                        "Please enter an account name.",
                        "Input Error",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

            double balance, interestRate, accountFees, otherIncome;
            try {
                balance = Double.parseDouble(balanceField.getText().trim());
                interestRate = Double.parseDouble(interestRateField.getText().trim());
                accountFees = Double.parseDouble(accountFeesField.getText().trim());
                otherIncome = Double.parseDouble(otherIncomeField.getText().trim());
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this,
                        "Please enter valid numbers for balance, interest rate, fees, and income.",
                        "Input Error",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Save to database via serviceDispatcher
            if (serviceDispatcher != null) {
                try {
                    boolean success = serviceDispatcher.createBankAccount(
                            savedUsername,  // owner username
                            accountName,
                            accountType,
                            balance,
                            interestRate,
                            accountFees,
                            otherIncome
                    );

                    if (success) {
                        showAutoCloseSuccess("Bank account '" + accountName + "' created successfully!");
                        // Clear form fields
                        accountNameField.setText("");
                        accountTypeCombo.setSelectedIndex(0);
                        balanceField.setText("0.00");
                        interestRateField.setText("0.00");
                        accountFeesField.setText("0.00");
                        otherIncomeField.setText("0.00");
                    } else {
                        JOptionPane.showMessageDialog(this,
                                "Failed to create bank account. Please try again.",
                                "Error",
                                JOptionPane.ERROR_MESSAGE);
                    }
                } catch (Exception ex) {
                    System.err.println("Error creating bank account: " + ex.getMessage());
                    JOptionPane.showMessageDialog(this,
                            "Error creating bank account: " + ex.getMessage(),
                            "Error",
                            JOptionPane.ERROR_MESSAGE);
                }
            } else {
                JOptionPane.showMessageDialog(this,
                        "Service not available. Please log in first.",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        });

        formContainer.add(createAccountButton);

        panel.add(formContainer, BorderLayout.NORTH);
        return panel;
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

        JLabel desc = new JLabel("Add vehicles on file. Saved vehicles are available in the Deliveries tab.");
        desc.setFont(primaryFont(Font.PLAIN, 12));
        desc.setForeground(COLOR_TEXT_SECONDARY);
        desc.setAlignmentX(Component.LEFT_ALIGNMENT);
        center.add(desc);

        center.add(Box.createVerticalStrut(15));

        JPanel card = new JPanel(new GridLayout(3, 2, 10, 8));
        card.setBackground(COLOR_BG_MAIN);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(COLOR_BORDER),
                BorderFactory.createEmptyBorder(12, 12, 12, 12)
        ));
        card.setMaximumSize(new Dimension(420, 120));
        card.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel typeLabel = new JLabel("Vehicle type");
        String[] vehicleTypes = {"Car", "Motorcycle", "Bike"};
        JComboBox<String> vehicleTypeCombo = new JComboBox<>(vehicleTypes);
        styleComboBox(vehicleTypeCombo);
        vehicleTypeCombo.setToolTipText("Select the type of vehicle.");

        JLabel nameLabel = new JLabel("Vehicle name");
        JTextField carNameField = createInputField();

        JLabel mpgLabel = new JLabel("MPG");
        JTextField carMpgField = createInputField();

        styleFormLabel(typeLabel);
        styleFormLabel(nameLabel);
        styleFormLabel(mpgLabel);

        card.add(typeLabel);
        card.add(vehicleTypeCombo);
        card.add(nameLabel);
        card.add(carNameField);
        card.add(mpgLabel);
        card.add(carMpgField);

        center.add(card);
        center.add(Box.createVerticalStrut(10));

        JButton addCarButton = new JButton("Add vehicle");
        addCarButton.setAlignmentX(Component.LEFT_ALIGNMENT);
        stylePrimaryButton(addCarButton);

        // Create the default vehicle combo box early so it can be updated when adding vehicles
        defaultVehicleCombo = new JComboBox<>();
        styleComboBox(defaultVehicleCombo);
        defaultVehicleCombo.setToolTipText("Select the vehicle to use as default for new deliveries.");
        defaultVehicleCombo.setPreferredSize(new Dimension(250, 28));
        defaultVehicleCombo.setMaximumSize(new Dimension(300, 28));

        // Populate with vehicles from database (will be refreshed when serviceDispatcher is set)
        refreshDefaultVehicleCombo();

        addCarButton.addActionListener(e -> {
            String vehicleType = (String) vehicleTypeCombo.getSelectedItem();
            String carName = carNameField.getText().trim();
            String mpgText = carMpgField.getText().trim();

            if (carName.isEmpty()) {
                JOptionPane.showMessageDialog(
                        this,
                        "Enter a vehicle name.",
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

            // Save to database if serviceDispatcher is available
            if (serviceDispatcher != null) {
                try {
                    // Determine if this should be the current vehicle (first vehicle added)
                    boolean isFirstVehicle = carTableModel.getRowCount() == 0;
                    String currentVehicleStatus = isFirstVehicle ? "true" : "false";

                    // Add vehicle to database with selected type and MPG
                    serviceDispatcher.addVehicle(vehicleType, carName, currentVehicleStatus, 0, mpg);
                    System.out.println("FinanceAppFrame: Vehicle '" + carName + "' (" + vehicleType + ", " + mpg + " MPG) saved to database.");

                    // If this is the first vehicle, update the sidebar display
                    if (isFirstVehicle) {
                        sidebarCurrentVehicleLabel.setText(vehicleType + " - " + carName);
                    }
                } catch (Exception ex) {
                    System.err.println("FinanceAppFrame: Error saving vehicle to database: " + ex.getMessage());
                    JOptionPane.showMessageDialog(
                            this,
                            "Vehicle added locally but failed to save to database: " + ex.getMessage(),
                            "Database Warning",
                            JOptionPane.WARNING_MESSAGE
                    );
                }
            }

            int carId = nextCarId++;
            carTableModel.addRow(new Object[]{carId, vehicleType + " - " + carName, mpg});

            String displayString = carId + " - " + vehicleType + " - " + carName;
            ((DefaultComboBoxModel<String>) deliveryCarCombo.getModel()).addElement(displayString);

            // Also add to the default vehicle combo box
            defaultVehicleCombo.addItem(displayString);

            carNameField.setText("");
            carMpgField.setText("");
            carMpgField.setBorder(normalInputBorder);
            vehicleTypeCombo.setSelectedIndex(0); // Reset to first option

            showAutoCloseSuccess("Car added");
        });

        center.add(addCarButton);

        center.add(Box.createVerticalStrut(15));

        // =========================================================
        // Default Vehicle Selection Section
        // =========================================================
        JLabel defaultVehicleLabel = new JLabel("Select Default Vehicle:");
        defaultVehicleLabel.setFont(primaryFont(Font.BOLD, 13));
        defaultVehicleLabel.setForeground(COLOR_TEXT_PRIMARY);
        defaultVehicleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        center.add(defaultVehicleLabel);

        center.add(Box.createVerticalStrut(8));

        JPanel defaultVehiclePanel = new JPanel();
        defaultVehiclePanel.setBackground(COLOR_BG_MAIN);
        defaultVehiclePanel.setLayout(new BoxLayout(defaultVehiclePanel, BoxLayout.X_AXIS));
        defaultVehiclePanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(COLOR_BORDER),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        defaultVehiclePanel.setMaximumSize(new Dimension(420, 50));
        defaultVehiclePanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JButton setDefaultButton = new JButton("Set as Default");
        styleSecondaryButton(setDefaultButton);
        setDefaultButton.setPreferredSize(new Dimension(120, 28));

        setDefaultButton.addActionListener(e -> {
            String selectedVehicle = (String) defaultVehicleCombo.getSelectedItem();
            if (selectedVehicle == null || selectedVehicle.isEmpty()) {
                JOptionPane.showMessageDialog(
                        this,
                        "Please select a vehicle from the dropdown.",
                        "No vehicle selected",
                        JOptionPane.WARNING_MESSAGE
                );
                return;
            }

            // Extract vehicle name from display string (format: "id - type - name" or "id - name")
            String vehicleName = selectedVehicle;
            if (selectedVehicle.contains(" - ")) {
                // Get everything after the first " - "
                vehicleName = selectedVehicle.substring(selectedVehicle.indexOf(" - ") + 3);
            }

            // Update in database
            if (serviceDispatcher != null) {
                try {
                    serviceDispatcher.setCurrentVehicle(vehicleName);
                    sidebarCurrentVehicleLabel.setText(vehicleName);

                    // Also update the deliveryCarCombo selection
                    for (int i = 0; i < deliveryCarCombo.getItemCount(); i++) {
                        String item = deliveryCarCombo.getItemAt(i);
                        if (item != null && item.equals(selectedVehicle)) {
                            deliveryCarCombo.setSelectedIndex(i);
                            break;
                        }
                    }

                    showAutoCloseSuccess("'" + vehicleName + "' set as default vehicle");
                    System.out.println("FinanceAppFrame: Set '" + vehicleName + "' as default vehicle.");
                } catch (Exception ex) {
                    System.err.println("FinanceAppFrame: Error setting default vehicle: " + ex.getMessage());
                    JOptionPane.showMessageDialog(
                            this,
                            "Failed to set default vehicle: " + ex.getMessage(),
                            "Error",
                            JOptionPane.ERROR_MESSAGE
                    );
                }
            } else {
                // Just update UI if no dispatcher
                sidebarCurrentVehicleLabel.setText(vehicleName);
                showAutoCloseSuccess("'" + vehicleName + "' set as default vehicle (local only)");
            }
        });

        defaultVehiclePanel.add(defaultVehicleCombo);
        defaultVehiclePanel.add(Box.createHorizontalStrut(10));
        defaultVehiclePanel.add(setDefaultButton);

        center.add(defaultVehiclePanel);

        center.add(Box.createVerticalStrut(10));

        // Button to set selected car as current vehicle
        JButton setCurrentButton = new JButton("Set as Current Vehicle");
        setCurrentButton.setAlignmentX(Component.LEFT_ALIGNMENT);
        styleSecondaryButton(setCurrentButton);

        setCurrentButton.addActionListener(e -> {
            String selectedCar = (String) deliveryCarCombo.getSelectedItem();
            if (selectedCar == null || selectedCar.isEmpty()) {
                JOptionPane.showMessageDialog(
                        this,
                        "Please select a vehicle first.",
                        "No vehicle selected",
                        JOptionPane.WARNING_MESSAGE
                );
                return;
            }

            // Extract car name from display string (format: "id - carName")
            String carName = selectedCar;
            if (selectedCar.contains(" - ")) {
                carName = selectedCar.substring(selectedCar.indexOf(" - ") + 3);
            }

            // Update in database
            if (serviceDispatcher != null) {
                try {
                    serviceDispatcher.setCurrentVehicle(carName);
                    sidebarCurrentVehicleLabel.setText(carName);

                    // Also update the defaultVehicleCombo selection
                    for (int i = 0; i < defaultVehicleCombo.getItemCount(); i++) {
                        String item = defaultVehicleCombo.getItemAt(i);
                        if (item != null && item.equals(selectedCar)) {
                            defaultVehicleCombo.setSelectedIndex(i);
                            break;
                        }
                    }

                    showAutoCloseSuccess("'" + carName + "' set as current vehicle");
                    System.out.println("FinanceAppFrame: Set '" + carName + "' as current vehicle.");
                } catch (Exception ex) {
                    System.err.println("FinanceAppFrame: Error setting current vehicle: " + ex.getMessage());
                    JOptionPane.showMessageDialog(
                            this,
                            "Failed to set current vehicle: " + ex.getMessage(),
                            "Error",
                            JOptionPane.ERROR_MESSAGE
                    );
                }
            } else {
                // Just update UI if no dispatcher
                sidebarCurrentVehicleLabel.setText(carName);
                showAutoCloseSuccess("'" + carName + "' set as current vehicle (local only)");
            }
        });

        center.add(setCurrentButton);

        inner.add(center, BorderLayout.CENTER);

        panel.add(inner, BorderLayout.CENTER);
        return panel;
    }

    // =========================================================
    //   REPORTS SCREEN (SUMMARY + DATE FILTER + GRAPHS)
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

        // --- Filter row (start/end date + Apply button) ---
        JPanel filterPanel = new JPanel();
        filterPanel.setBackground(COLOR_BG_CARD);
        filterPanel.setLayout(new BoxLayout(filterPanel, BoxLayout.X_AXIS));

        JLabel startLabel = new JLabel("Start date: ");
        styleFormLabel(startLabel);

        // Create start date dropdowns (Year, Month, Day)
        int currentYear = java.time.Year.now().getValue();
        Integer[] years = new Integer[11]; // 5 years back, current, 5 years forward
        for (int i = 0; i < 11; i++) {
            years[i] = currentYear - 5 + i;
        }
        Integer[] months = new Integer[12];
        for (int i = 0; i < 12; i++) {
            months[i] = i + 1;
        }
        Integer[] days = new Integer[31];
        for (int i = 0; i < 31; i++) {
            days[i] = i + 1;
        }

        reportStartYearCombo = new JComboBox<>(years);
        reportStartYearCombo.setSelectedItem(currentYear);
        reportStartYearCombo.setPreferredSize(new Dimension(70, 24));
        reportStartYearCombo.setMaximumSize(new Dimension(80, 24));

        reportStartMonthCombo = new JComboBox<>(months);
        reportStartMonthCombo.setSelectedItem(1);
        reportStartMonthCombo.setPreferredSize(new Dimension(50, 24));
        reportStartMonthCombo.setMaximumSize(new Dimension(60, 24));

        reportStartDayCombo = new JComboBox<>(days);
        reportStartDayCombo.setSelectedItem(1);
        reportStartDayCombo.setPreferredSize(new Dimension(50, 24));
        reportStartDayCombo.setMaximumSize(new Dimension(60, 24));

        JLabel endLabel = new JLabel("   End date: ");
        styleFormLabel(endLabel);

        // Create end date dropdowns (Year, Month, Day)
        reportEndYearCombo = new JComboBox<>(years);
        reportEndYearCombo.setSelectedItem(currentYear);
        reportEndYearCombo.setPreferredSize(new Dimension(70, 24));
        reportEndYearCombo.setMaximumSize(new Dimension(80, 24));

        reportEndMonthCombo = new JComboBox<>(months);
        reportEndMonthCombo.setSelectedItem(12);
        reportEndMonthCombo.setPreferredSize(new Dimension(50, 24));
        reportEndMonthCombo.setMaximumSize(new Dimension(60, 24));

        reportEndDayCombo = new JComboBox<>(days);
        reportEndDayCombo.setSelectedItem(31);
        reportEndDayCombo.setPreferredSize(new Dimension(50, 24));
        reportEndDayCombo.setMaximumSize(new Dimension(60, 24));

        JButton applyFilterButton = new JButton("Apply");
        styleSecondaryButton(applyFilterButton);
        applyFilterButton.setToolTipText("Apply date range filter to totals, gas, charts, and top items.");
        applyFilterButton.addActionListener(e -> {
            updateReportStats();
            showAutoCloseSuccess("Report updated");
        });

        filterPanel.add(startLabel);
        filterPanel.add(reportStartYearCombo);
        filterPanel.add(new JLabel("-"));
        filterPanel.add(reportStartMonthCombo);
        filterPanel.add(new JLabel("-"));
        filterPanel.add(reportStartDayCombo);
        filterPanel.add(endLabel);
        filterPanel.add(reportEndYearCombo);
        filterPanel.add(new JLabel("-"));
        filterPanel.add(reportEndMonthCombo);
        filterPanel.add(new JLabel("-"));
        filterPanel.add(reportEndDayCombo);
        filterPanel.add(Box.createHorizontalStrut(10));
        filterPanel.add(applyFilterButton);

        // --- Top summary metrics ---
        JPanel statsPanel = new JPanel(new GridLayout(1, 3, 15, 0));
        statsPanel.setBackground(COLOR_BG_CARD);
        statsPanel.setBorder(BorderFactory.createEmptyBorder(12, 0, 10, 0));

        reportTotalDeliveriesLabel = new JLabel("0");
        reportTotalEarningsLabel = new JLabel("$0.00");
        reportAvgPerDeliveryLabel = new JLabel("$0.00");

        statsPanel.add(createMetricCard("Total deliveries", reportTotalDeliveriesLabel));
        statsPanel.add(createMetricCard("Total earnings", reportTotalEarningsLabel));
        statsPanel.add(createMetricCard("Avg per delivery", reportAvgPerDeliveryLabel));

        JPanel topBox = new JPanel();
        topBox.setBackground(COLOR_BG_CARD);
        topBox.setLayout(new BoxLayout(topBox, BoxLayout.Y_AXIS));
        topBox.add(filterPanel);
        topBox.add(Box.createVerticalStrut(8));
        topBox.add(statsPanel);

        center.add(topBox, BorderLayout.NORTH);

        // --- Charts area (slightly smaller) ---
        JPanel chartsPanel = new JPanel(new GridLayout(1, 2, 12, 0));
        chartsPanel.setBackground(COLOR_BG_CARD);
        chartsPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));

        platformChart = new BarChartPanel("Earnings by platform");
        dailyChart = new BarChartPanel("Earnings by day");

        chartsPanel.add(platformChart);
        chartsPanel.add(dailyChart);

        center.add(chartsPanel, BorderLayout.CENTER);

        // --- Bottom info row: gas + most frequent restaurant/platform + miles ---
        JPanel bottom = new JPanel(new BorderLayout());
        bottom.setBackground(COLOR_BG_CARD);

        JPanel bottomLeft = new JPanel();
        bottomLeft.setBackground(COLOR_BG_CARD);
        bottomLeft.setLayout(new BoxLayout(bottomLeft, BoxLayout.Y_AXIS));

        reportGasCostLabel = new JLabel("Estimated gas cost: $0.00 (CA avg $" + GAS_PRICE_CA + "/gal)");
        reportGasCostLabel.setFont(primaryFont(Font.BOLD, 13));
        reportGasCostLabel.setForeground(COLOR_TEXT_PRIMARY);
        reportGasCostLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        reportTopRestaurantLabel = new JLabel("Most frequent restaurant: (none yet)");
        reportTopRestaurantLabel.setFont(primaryFont(Font.BOLD, 13));
        reportTopRestaurantLabel.setForeground(COLOR_TEXT_PRIMARY);
        reportTopRestaurantLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        reportTopPlatformLabel = new JLabel("Most frequent platform: (none yet)");
        reportTopPlatformLabel.setFont(primaryFont(Font.BOLD, 13));
        reportTopPlatformLabel.setForeground(COLOR_TEXT_PRIMARY);
        reportTopPlatformLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        reportTotalMilesLabel = new JLabel("Total miles driven: 0.0 mi");
        reportTotalMilesLabel.setFont(primaryFont(Font.BOLD, 13));
        reportTotalMilesLabel.setForeground(COLOR_TEXT_PRIMARY);
        reportTotalMilesLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        bottomLeft.add(reportGasCostLabel);
        bottomLeft.add(Box.createVerticalStrut(2));
        bottomLeft.add(reportTotalMilesLabel);
        bottomLeft.add(Box.createVerticalStrut(2));
        bottomLeft.add(reportTopRestaurantLabel);
        bottomLeft.add(Box.createVerticalStrut(2));
        bottomLeft.add(reportTopPlatformLabel);

        bottom.add(bottomLeft, BorderLayout.WEST);

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

        // Make totals a bit more visible
        valueLabel.setFont(primaryFont(Font.BOLD, 24));
        valueLabel.setForeground(COLOR_TEXT_PRIMARY);

        card.add(titleLabel, BorderLayout.NORTH);
        card.add(valueLabel, BorderLayout.CENTER);

        return card;
    }

    // === Report stats with optional date range ===

    private void updateReportStats() {
        LocalDate start = null;
        LocalDate end = null;
        if (reportStartYearCombo != null && reportStartMonthCombo != null && reportStartDayCombo != null) {
            try {
                int year = (Integer) reportStartYearCombo.getSelectedItem();
                int month = (Integer) reportStartMonthCombo.getSelectedItem();
                int day = (Integer) reportStartDayCombo.getSelectedItem();
                // Adjust day if it exceeds the actual days in the month
                int maxDay = java.time.YearMonth.of(year, month).lengthOfMonth();
                day = Math.min(day, maxDay);
                start = LocalDate.of(year, month, day);
            } catch (Exception e) {
                start = null;
            }
        }
        if (reportEndYearCombo != null && reportEndMonthCombo != null && reportEndDayCombo != null) {
            try {
                int year = (Integer) reportEndYearCombo.getSelectedItem();
                int month = (Integer) reportEndMonthCombo.getSelectedItem();
                int day = (Integer) reportEndDayCombo.getSelectedItem();
                // Adjust day if it exceeds the actual days in the month
                int maxDay = java.time.YearMonth.of(year, month).lengthOfMonth();
                day = Math.min(day, maxDay);
                end = LocalDate.of(year, month, day);
            } catch (Exception e) {
                end = null;
            }
        }
        updateReportStats(start, end);
    }

    private LocalDate parseDateSafe(String s) {
        if (s == null || s.isEmpty()) return null;
        try {
            return LocalDate.parse(s); // expects YYYY-MM-DD
        } catch (DateTimeParseException e) {
            // Invalid format -> ignore and treat as no bound
            return null;
        }
    }

    private void updateReportStats(LocalDate start, LocalDate end) {
        if (deliveryTableModel == null) {
            if (reportTotalDeliveriesLabel != null) reportTotalDeliveriesLabel.setText("0");
            if (reportTotalEarningsLabel != null) reportTotalEarningsLabel.setText("$0.00");
            if (reportAvgPerDeliveryLabel != null) reportAvgPerDeliveryLabel.setText("$0.00");
            if (reportGasCostLabel != null) {
                reportGasCostLabel.setText(
                        String.format("Estimated gas cost: $%.2f (CA avg $%.2f/gal)", 0.0, GAS_PRICE_CA)
                );
            }
            if (reportTopRestaurantLabel != null) {
                reportTopRestaurantLabel.setText("Most frequent restaurant: (none yet)");
            }
            if (reportTopPlatformLabel != null) {
                reportTopPlatformLabel.setText("Most frequent platform: (none yet)");
            }
            if (reportTotalMilesLabel != null) {
                reportTotalMilesLabel.setText("Total miles driven: 0.0 mi");
            }
            if (platformChart != null) platformChart.setData(new ChartData(new String[0], new double[0]));
            if (dailyChart != null) dailyChart.setData(new ChartData(new String[0], new double[0]));
            return;
        }

        int rows = deliveryTableModel.getRowCount();

        int deliveries = 0;
        double totalEarnings = 0.0;
        double gasCost = 0.0;
        double totalMiles = 0.0;

        Map<String, Integer> restaurantCounts = new LinkedHashMap<>();
        Map<String, Integer> platformCounts = new LinkedHashMap<>();
        Map<String, Double> byPlatform = new LinkedHashMap<>();
        Map<String, Double> byDate = new LinkedHashMap<>();

        for (int i = 0; i < rows; i++) {
            Object dateObj = deliveryTableModel.getValueAt(i, 0); // Date column
            if (dateObj == null) continue;
            String dateStr = dateObj.toString().trim();
            LocalDate rowDate = parseDateSafe(dateStr);
            if (rowDate == null) continue;

            if (start != null && rowDate.isBefore(start)) continue;
            if (end != null && rowDate.isAfter(end)) continue;

            // In range -> include in aggregates
            deliveries++;

            Object totalObj = deliveryTableModel.getValueAt(i, 9); // Total column
            double total = 0.0;
            if (totalObj instanceof Number) {
                total = ((Number) totalObj).doubleValue();
            } else if (totalObj != null) {
                try {
                    total = Double.parseDouble(totalObj.toString());
                } catch (NumberFormatException ignored) {}
            }
            totalEarnings += total;

            // Miles for this row
            Object milesObj = deliveryTableModel.getValueAt(i, 6);
            double miles = 0.0;
            if (milesObj instanceof Number) {
                miles = ((Number) milesObj).doubleValue();
            } else if (milesObj != null) {
                try {
                    miles = Double.parseDouble(milesObj.toString());
                } catch (NumberFormatException ignored) {}
            }
            totalMiles += miles;

            // Gas cost for this row
            gasCost += computeGasCostForRow(i);

            // Restaurant counts
            Object restObj = deliveryTableModel.getValueAt(i, 3);
            if (restObj != null) {
                String rest = restObj.toString().trim();
                if (!rest.isEmpty()) {
                    restaurantCounts.put(rest, restaurantCounts.getOrDefault(rest, 0) + 1);
                }
            }

            // Platform totals + counts
            Object platformObj = deliveryTableModel.getValueAt(i, 4);
            String platform = platformObj == null ? "Unknown" : platformObj.toString();
            byPlatform.put(platform, byPlatform.getOrDefault(platform, 0.0) + total);
            platformCounts.put(platform, platformCounts.getOrDefault(platform, 0) + 1);

            // Date totals
            byDate.put(dateStr, byDate.getOrDefault(dateStr, 0.0) + total);
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
        if (reportGasCostLabel != null) {
            reportGasCostLabel.setText(
                    String.format("Estimated gas cost: $%.2f (CA avg $%.2f/gal)", gasCost, GAS_PRICE_CA)
            );
        }
        if (reportTotalMilesLabel != null) {
            reportTotalMilesLabel.setText(
                    String.format("Total miles driven: %.1f mi", totalMiles)
            );
        }

        String topRestaurantText = "Most frequent restaurant: (none yet)";
        if (!restaurantCounts.isEmpty()) {
            String bestName = null;
            int bestCount = 0;
            for (Map.Entry<String, Integer> entry : restaurantCounts.entrySet()) {
                if (entry.getValue() > bestCount) {
                    bestCount = entry.getValue();
                    bestName = entry.getKey();
                }
            }
            if (bestName != null) {
                topRestaurantText = String.format(
                        "Most frequent restaurant: %s (%d deliveries)", bestName, bestCount
                );
            }
        }
        if (reportTopRestaurantLabel != null) {
            reportTopRestaurantLabel.setText(topRestaurantText);
        }

        String topPlatformText = "Most frequent platform: (none yet)";
        if (!platformCounts.isEmpty()) {
            String bestName = null;
            int bestCount = 0;
            for (Map.Entry<String, Integer> entry : platformCounts.entrySet()) {
                if (entry.getValue() > bestCount) {
                    bestCount = entry.getValue();
                    bestName = entry.getKey();
                }
            }
            if (bestName != null) {
                topPlatformText = String.format(
                        "Most frequent platform: %s (%d deliveries)", bestName, bestCount
                );
            }
        }
        if (reportTopPlatformLabel != null) {
            reportTopPlatformLabel.setText(topPlatformText);
        }

        if (platformChart != null) {
            platformChart.setData(mapToArrays(byPlatform));
        }
        if (dailyChart != null) {
            dailyChart.setData(mapToArrays(byDate));
        }
    }

    // =========================================================
    //   GAS COST CALCULATION HELPERS
    // =========================================================

    private double computeGasCostForRow(int rowIndex) {
        if (deliveryTableModel == null || carTableModel == null) return 0.0;

        Object milesObj = deliveryTableModel.getValueAt(rowIndex, 6); // Miles
        Object carObj = deliveryTableModel.getValueAt(rowIndex, 5);   // Car display

        if (milesObj == null || carObj == null) return 0.0;

        double miles;
        try {
            miles = (milesObj instanceof Number)
                    ? ((Number) milesObj).doubleValue()
                    : Double.parseDouble(milesObj.toString());
        } catch (NumberFormatException ex) {
            return 0.0;
        }

        if (miles <= 0) return 0.0;

        String carDisplay = carObj.toString();
        double mpg = lookupMpgForCarDisplay(carDisplay);
        if (mpg <= 0) return 0.0;

        // Use serviceDispatcher's gas calculation if available, otherwise fallback to local calculation
        if (serviceDispatcher != null) {
            return serviceDispatcher.calculateGasCost(mpg, miles, GAS_PRICE_CA);
        } else {
            // Fallback: local calculation using gasUsed formula
            double gallons = miles / mpg;
            return gallons * GAS_PRICE_CA;
        }
    }

    private double computeTotalGasCostFromDeliveries() {
        if (deliveryTableModel == null) return 0.0;

        // Collect total miles and average MPG for all deliveries
        double totalMiles = 0.0;
        double totalMpgWeighted = 0.0;
        int validDeliveries = 0;

        int rows = deliveryTableModel.getRowCount();
        for (int i = 0; i < rows; i++) {
            Object milesObj = deliveryTableModel.getValueAt(i, 6);
            Object carObj = deliveryTableModel.getValueAt(i, 5);

            if (milesObj == null || carObj == null) continue;

            double miles;
            try {
                miles = (milesObj instanceof Number)
                        ? ((Number) milesObj).doubleValue()
                        : Double.parseDouble(milesObj.toString());
            } catch (NumberFormatException ex) {
                continue;
            }

            if (miles <= 0) continue;

            String carDisplay = carObj.toString();
            double mpg = lookupMpgForCarDisplay(carDisplay);
            if (mpg <= 0) continue;

            totalMiles += miles;
            totalMpgWeighted += mpg * miles; // Weight MPG by miles for accurate averaging
            validDeliveries++;
        }

        if (validDeliveries == 0 || totalMiles <= 0) return 0.0;

        // Calculate weighted average MPG
        double avgMpg = totalMpgWeighted / totalMiles;

        // Use serviceDispatcher's gas calculation if available
        if (serviceDispatcher != null) {
            return serviceDispatcher.calculateTotalGasCost(totalMiles, avgMpg, GAS_PRICE_CA);
        } else {
            // Fallback: local calculation
            double gallons = totalMiles / avgMpg;
            return gallons * GAS_PRICE_CA;
        }
    }

    private double lookupMpgForCarDisplay(String carDisplay) {
        if (carDisplay == null) return DEFAULT_CAR_MPG;

        int dashIndex = carDisplay.indexOf("-");
        if (dashIndex <= 0) return DEFAULT_CAR_MPG;

        String idPart = carDisplay.substring(0, dashIndex).trim();
        int carId;
        try {
            carId = Integer.parseInt(idPart);
        } catch (NumberFormatException ex) {
            return DEFAULT_CAR_MPG;
        }

        int rows = carTableModel.getRowCount();
        for (int i = 0; i < rows; i++) {
            Object idObj = carTableModel.getValueAt(i, 0);
            if (idObj instanceof Number && ((Number) idObj).intValue() == carId) {
                Object mpgObj = carTableModel.getValueAt(i, 2);
                double mpg = 0.0;
                if (mpgObj instanceof Number) {
                    mpg = ((Number) mpgObj).doubleValue();
                } else if (mpgObj != null) {
                    try {
                        mpg = Double.parseDouble(mpgObj.toString());
                    } catch (NumberFormatException ignored) { }
                }
                // If MPG is 0 or negative, check vehicle type in display name and use appropriate default
                if (mpg <= 0) {
                    Object nameObj = carTableModel.getValueAt(i, 1);
                    if (nameObj != null) {
                        String vehicleName = nameObj.toString().toLowerCase();
                        if (vehicleName.contains("motorcycle")) {
                            return DEFAULT_MOTORCYCLE_MPG;
                        } else if (vehicleName.contains("bike")) {
                            return 0.0; // Bikes don't use gas
                        }
                    }
                    return DEFAULT_CAR_MPG;
                }
                return mpg;
            }
        }
        return DEFAULT_CAR_MPG;
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
                Object val = deliveryTableModel.getValueAt(i, 9);
                if (val instanceof Number) {
                    totalEarnings += ((Number) val).doubleValue();
                } else if (val != null) {
                    try {
                        totalEarnings += Double.parseDouble(val.toString());
                    } catch (NumberFormatException ignored) {}
                }
            }
        }

        double expenses = computeTotalGasCostFromDeliveries();
        double net = totalEarnings - expenses;

        sidebarTotalRevenueLabel.setText(String.format("Earnings: $%.2f", totalEarnings));
        sidebarTotalExpensesLabel.setText(String.format("Gas est: $%.2f", expenses));
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
    //   BAR CHART PANEL (FOR REPORTS)
    // =========================================================

    // Note: BarChartPanel and ChartData have been moved to top-level classes
    // org.example.gui.BarChartPanel and org.example.gui.ChartData so the GUI
    // Designer can reference them as custom components.

    private static ChartData mapToArrays(Map<String, Double> map) {
        int n = map.size();
        String[] labels = new String[n];
        double[] values = new double[n];
        int idx = 0;
        for (Map.Entry<String, Double> e : map.entrySet()) {
            labels[idx] = e.getKey();
            values[idx] = e.getValue();
            idx++;
        }
        return new ChartData(labels, values);
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
