package org.example.gui;

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
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;

import java.util.LinkedHashMap;
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
    private JTextField deliveryMilesField;
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
    //   REPORTS LABELS & CHARTS
    // =========================================================

    private JLabel reportTotalDeliveriesLabel;
    private JLabel reportTotalEarningsLabel;
    private JLabel reportAvgPerDeliveryLabel;
    private JLabel reportGasCostLabel;
    private JLabel reportTopRestaurantLabel;
    private JLabel reportTopPlatformLabel;
    private JLabel reportTotalMilesLabel;

    private JTextField reportStartDateField;
    private JTextField reportEndDateField;

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

    // Range toggle buttons on the home screen
    private JButton homeTodayButton;
    private JButton homeWeekButton;
    private JButton homeMonthButton;
    private JButton homeAllButton;

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

    // Add root panel for IntelliJ GUI Designer binding
    private JPanel rootPanel;

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
        JButton settingsBtn   = createNavButton("Settings");
        JButton accountBtn    = createNavButton("Account");

        overviewBtn.addActionListener(e -> showScreen("HOME"));
        deliveriesBtn.addActionListener(e -> showScreen("DELIVERIES"));
        reportsBtn.addActionListener(e -> showScreen("REPORTS"));
        settingsBtn.addActionListener(e -> showScreen("SETTINGS"));
        accountBtn.addActionListener(e -> showScreen("ACCOUNT"));

        navButtonsPanel.add(overviewBtn);
        navButtonsPanel.add(deliveriesBtn);
        navButtonsPanel.add(reportsBtn);
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
        JPanel formPanel = new JPanel(new GridLayout(5, 4, 10, 10));
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

        JLabel milesLabel = new JLabel("Miles driven");
        deliveryMilesField = createInputField();

        styleFormLabel(restaurantLabel);
        styleFormLabel(dateLabel);
        styleFormLabel(startLabel);
        styleFormLabel(endLabel);
        styleFormLabel(payLabel);
        styleFormLabel(tipLabel);
        styleFormLabel(platformLabel);
        styleFormLabel(carLabel);
        styleFormLabel(milesLabel);

        // Row 1
        formPanel.add(restaurantLabel);
        formPanel.add(deliveryRestaurantField);
        formPanel.add(dateLabel);
        formPanel.add(deliveryDateField);

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
                    deliveryDateField.setText(Objects.toString(deliveryTableModel.getValueAt(modelRow, 0), ""));
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

        double miles = Double.parseDouble(deliveryMilesField.getText().trim());
        double pay = Double.parseDouble(deliveryPayField.getText().trim());
        double tip = Double.parseDouble(deliveryTipField.getText().trim());
        double total = pay + tip;

        deliveryTableModel.addRow(new Object[]{
                date, start, end, restaurant, platform, carDisplay,
                miles, pay, tip, total
        });

        updateSidebarStats();
        updateReportStats();
        updateHomeOverview();
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

    private void clearDeliveryForm() {
        deliveryRestaurantField.setText("");
        deliveryDateField.setText("");
        deliveryStartTimeField.setText("");
        deliveryEndTimeField.setText("");
        deliveryMilesField.setText("");
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

        JLabel startLabel = new JLabel("Start date (YYYY-MM-DD): ");
        styleFormLabel(startLabel);
        reportStartDateField = createInputField();
        reportStartDateField.setPreferredSize(new Dimension(110, 24));
        reportStartDateField.setMaximumSize(new Dimension(140, 24));

        JLabel endLabel = new JLabel("   End date: ");
        styleFormLabel(endLabel);
        reportEndDateField = createInputField();
        reportEndDateField.setPreferredSize(new Dimension(110, 24));
        reportEndDateField.setMaximumSize(new Dimension(140, 24));

        JButton applyFilterButton = new JButton("Apply");
        styleSecondaryButton(applyFilterButton);
        applyFilterButton.setToolTipText("Apply date range filter to totals, gas, charts, and top items.");
        applyFilterButton.addActionListener(e -> {
            updateReportStats();
            showAutoCloseSuccess("Report updated");
        });

        filterPanel.add(startLabel);
        filterPanel.add(reportStartDateField);
        filterPanel.add(endLabel);
        filterPanel.add(reportEndDateField);
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
        if (reportStartDateField != null) {
            start = parseDateSafe(reportStartDateField.getText().trim());
        }
        if (reportEndDateField != null) {
            end = parseDateSafe(reportEndDateField.getText().trim());
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

        double gallons = miles / mpg;
        return gallons * GAS_PRICE_CA;
    }

    private double computeTotalGasCostFromDeliveries() {
        if (deliveryTableModel == null) return 0.0;
        double totalCost = 0.0;
        int rows = deliveryTableModel.getRowCount();
        for (int i = 0; i < rows; i++) {
            totalCost += computeGasCostForRow(i);
        }
        return totalCost;
    }

    private double lookupMpgForCarDisplay(String carDisplay) {
        if (carDisplay == null) return 0.0;

        int dashIndex = carDisplay.indexOf("-");
        if (dashIndex <= 0) return 0.0;

        String idPart = carDisplay.substring(0, dashIndex).trim();
        int carId;
        try {
            carId = Integer.parseInt(idPart);
        } catch (NumberFormatException ex) {
            return 0.0;
        }

        int rows = carTableModel.getRowCount();
        for (int i = 0; i < rows; i++) {
            Object idObj = carTableModel.getValueAt(i, 0);
            if (idObj instanceof Number && ((Number) idObj).intValue() == carId) {
                Object mpgObj = carTableModel.getValueAt(i, 2);
                if (mpgObj instanceof Number) {
                    return ((Number) mpgObj).doubleValue();
                } else if (mpgObj != null) {
                    try {
                        return Double.parseDouble(mpgObj.toString());
                    } catch (NumberFormatException ignored) { }
                }
            }
        }
        return 0.0;
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
