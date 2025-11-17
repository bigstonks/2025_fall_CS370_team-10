// ========================= IMPORTS =========================
// Swing imports: GUI components and helpers from the Swing library.
import javax.swing.JFrame;        // Top-level window (application main window)
import javax.swing.JPanel;        // Generic container to group components
import javax.swing.JButton;       // Clickable button
import javax.swing.JLabel;        // Text label
import javax.swing.JTextField;    // Single-line text input field
import javax.swing.JComboBox;     // Drop-down selection box
import javax.swing.SwingUtilities; // Utility for running GUI code on the Event Dispatch Thread
import javax.swing.BorderFactory; // Utility for creating borders (padding, lines)
import javax.swing.BoxLayout;     // Layout manager for vertical or horizontal stacking
import javax.swing.JOptionPane;   // Simple dialog boxes (message popups)

// AWT imports: layout managers, dimension and color utilities, fonts, etc.
import java.awt.BorderLayout;     // Layout with 5 regions: NORTH, SOUTH, EAST, WEST, CENTER
import java.awt.CardLayout;       // Layout that stores multiple "cards" and shows one at a time
import java.awt.Dimension;        // Encapsulates width and height values
import java.awt.Font;             // Font style, size, and type
import java.awt.GridLayout;       // Layout that arranges components in a grid (rows x columns)
import java.awt.Color;            // Represents colors (RGB values)
import java.awt.Component;        // Base class for all AWT components

// Event import: useful for action event types (clicks, etc.)
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Main GUI window for the Driver Finance Tracker application.
 * This class extends JFrame, so each instance represents a window on the screen.
 */
public class FinanceAppFrame extends JFrame {

    // =========================================================
    //   FIELDS (STATE OF THE WINDOW)
    // =========================================================

    /**
     * CardLayout controller for switching between different screens (Home, Revenue, etc.).
     */
    private CardLayout cardLayout;

    /**
     * Main panel in the center of the frame that holds all screens for CardLayout.
     */
    private JPanel contentPanel;

    // ----- Revenue form fields -----
    /** Text field for the revenue date (e.g., "2025-11-17"). */
    private JTextField revenueDateField;

    /** Drop-down for selecting the revenue source (DoorDash, Uber Eats, etc.). */
    private JComboBox<String> revenueSourceCombo;

    /** Text field for entering hours worked. */
    private JTextField revenueHoursField;

    /** Text field for entering base pay amount. */
    private JTextField revenueBasePayField;

    /** Text field for entering tip amount. */
    private JTextField revenueTipsField;

    // ----- Expenses form fields -----
    /** Text field for the expense date. */
    private JTextField expenseDateField;

    /** Drop-down for selecting the expense category. */
    private JComboBox<String> expenseCategoryCombo;

    /** Text field for the expense amount. */
    private JTextField expenseAmountField;

    /** Text field for a short description or note about the expense. */
    private JTextField expenseNotesField;

    // =========================================================
    //   CONSTRUCTOR
    // =========================================================

    /**
     * Constructs the main application frame and initializes all GUI components.
     */
    public FinanceAppFrame() {
        // ---------- Basic window settings ----------

        // Sets the text shown in the window's title bar.
        setTitle("Driver Finance Tracker");

        // Ensures that the entire application exits when the window is closed.
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Sets the window size: width = 900 pixels, height = 600 pixels.
        setSize(900, 600);

        // Centers the window on the screen rather than placing it at the top-left corner.
        setLocationRelativeTo(null);

        // ---------- Main layout for the frame ----------

        // BorderLayout divides the space into: NORTH, SOUTH, EAST, WEST, CENTER.
        // Suitable for layouts with a top bar, side menu, and central content.
        setLayout(new BorderLayout());

        // Create and add the top bar (application title area).
        JPanel topBar = createTopBar();
        add(topBar, BorderLayout.NORTH);

        // Create and add the side menu (navigation buttons).
        JPanel sideMenu = createSideMenu();
        add(sideMenu, BorderLayout.WEST);

        // Create and add the main content area (CardLayout screens).
        contentPanel = createContentPanel();
        add(contentPanel, BorderLayout.CENTER);
    }

    // =========================================================
    //   TOP BAR
    // =========================================================

    /**
     * Creates the top bar panel that displays the application title.
     *
     * @return a JPanel configured as the top bar.
     */
    private JPanel createTopBar() {
        // BorderLayout allows placement of components at WEST, CENTER, etc.
        JPanel topBar = new JPanel(new BorderLayout());

        // Adds padding around the inside of the panel: top, left, bottom, right.
        topBar.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));

        // Sets a blue background color (RGB values).
        topBar.setBackground(new Color(33, 150, 243)); // Blue similar to modern UI themes

        // Label for the application title.
        JLabel titleLabel = new JLabel("Driver Finance Tracker");

        // Sets font family, style (bold), and size.
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 24));

        // Sets the text color to white so it is visible on the blue background.
        titleLabel.setForeground(Color.WHITE);

        // Places the title label on the left side of the top bar.
        topBar.add(titleLabel, BorderLayout.WEST);

        return topBar;
    }

    // =========================================================
    //   SIDE MENU
    // =========================================================

    /**
     * Creates the side menu with navigation buttons.
     *
     * @return a JPanel configured as a vertical menu.
     */
    private JPanel createSideMenu() {
        JPanel sideMenu = new JPanel();

        // BoxLayout with Y_AXIS stacks components vertically.
        sideMenu.setLayout(new BoxLayout(sideMenu, BoxLayout.Y_AXIS));

        // Sets the preferred width of the menu; height is flexible (0 lets the layout decide).
        sideMenu.setPreferredSize(new Dimension(200, 0));

        // Adds inner padding for spacing from the frame edges.
        sideMenu.setBorder(BorderFactory.createEmptyBorder(15, 10, 15, 10));

        // Light gray background color for a subtle UI appearance.
        sideMenu.setBackground(new Color(245, 245, 245));

        // Label for the menu section.
        JLabel menuLabel = new JLabel("Menu");
        menuLabel.setFont(new Font("SansSerif", Font.BOLD, 16));

        // AlignmentX controls horizontal alignment in a BoxLayout container.
        menuLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        sideMenu.add(menuLabel);

        // Adds a small vertical space between the "Menu" label and the buttons.
        sideMenu.add(javax.swing.Box.createVerticalStrut(10));

        // ----------- Create navigation buttons -----------

        JButton homeButton = new JButton("Home");
        JButton revenueButton = new JButton("Revenue");
        JButton expensesButton = new JButton("Expenses");
        JButton reportsButton = new JButton("Reports");
        JButton settingsButton = new JButton("Settings");

        // Apply consistent styling to each button.
        styleMenuButton(homeButton);
        styleMenuButton(revenueButton);
        styleMenuButton(expensesButton);
        styleMenuButton(reportsButton);
        styleMenuButton(settingsButton);

        // ----------- Attach actions to buttons -----------

        // Each button triggers a screen change in the CardLayout.
        homeButton.addActionListener(e -> showScreen("HOME"));
        revenueButton.addActionListener(e -> showScreen("REVENUE"));
        expensesButton.addActionListener(e -> showScreen("EXPENSES"));
        reportsButton.addActionListener(e -> showScreen("REPORTS"));
        settingsButton.addActionListener(e -> showScreen("SETTINGS"));

        // Add buttons to the side menu in order.
        sideMenu.add(homeButton);
        sideMenu.add(revenueButton);
        sideMenu.add(expensesButton);
        sideMenu.add(reportsButton);
        sideMenu.add(settingsButton);

        // Adds flexible space below buttons so they stay at the top of the menu area.
        sideMenu.add(javax.swing.Box.createVerticalGlue());

        return sideMenu;
    }

    /**
     * Applies consistent styling to a menu button.
     *
     * @param button the JButton to style.
     */
    private void styleMenuButton(JButton button) {
        // Aligns the button to the left within the BoxLayout.
        button.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Sets the maximum size so the button can stretch horizontally
        // while maintaining a fixed height of 40 pixels.
        button.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));

        // Disables the focus painting (dotted outline around text) for a cleaner look.
        button.setFocusPainted(false);

        // Sets a standard font for menu buttons.
        button.setFont(new Font("SansSerif", Font.PLAIN, 14));
    }

    // =========================================================
    //   MAIN CONTENT AREA (CardLayout)
    // =========================================================

    /**
     * Creates the main content panel that uses CardLayout to manage multiple screens.
     *
     * @return a JPanel containing all screens.
     */
    private JPanel createContentPanel() {
        // CardLayout controls which "card" (screen) is visible.
        cardLayout = new CardLayout();

        // This panel acts as the container for all different screens.
        JPanel panel = new JPanel(cardLayout);

        // Create each screen:
        JPanel homeScreen = createHomeScreen();
        JPanel revenueScreen = createRevenueScreen();
        JPanel expensesScreen = createExpensesScreen();
        JPanel reportsScreen = createReportsScreen();
        JPanel settingsScreen = createSettingsScreen();

        // Add each screen to the CardLayout container with a unique string key.
        panel.add(homeScreen, "HOME");
        panel.add(revenueScreen, "REVENUE");
        panel.add(expensesScreen, "EXPENSES");
        panel.add(reportsScreen, "REPORTS");
        panel.add(settingsScreen, "SETTINGS");

        return panel;
    }

    // =========================================================
    //   HOME SCREEN (Dashboard)
    // =========================================================

    /**
     * Creates the Home screen, which serves as a dashboard.
     * The layout includes:
     *  - A title at the top
     *  - A row of "stat cards" in the center
     *  - A small info label at the bottom
     *
     * @return a JPanel representing the Home screen.
     */
    private JPanel createHomeScreen() {
        // BorderLayout is suitable for top, center, and bottom sections.
        JPanel panel = new JPanel(new BorderLayout());

        // Adds outer padding: top, left, bottom, right.
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Sets background to white for a clean look.
        panel.setBackground(Color.WHITE);

        // ---------- Title at the top ----------
        JLabel titleLabel = new JLabel("Home Dashboard");
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 22));
        panel.add(titleLabel, BorderLayout.NORTH);

        // ---------- Center: stat cards row ----------
        // GridLayout(1, 3) creates 1 row and 3 columns.
        // The gap arguments (15, 0) specify horizontal and vertical spacing.
        JPanel statsPanel = new JPanel(new GridLayout(1, 3, 15, 0));
        statsPanel.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));
        statsPanel.setBackground(Color.WHITE);

        // Currently, these cards use placeholder values.
        statsPanel.add(createStatCard("Total Revenue", "$0.00"));
        statsPanel.add(createStatCard("Total Expenses", "$0.00"));
        statsPanel.add(createStatCard("Total", "$0.00"));

        panel.add(statsPanel, BorderLayout.CENTER);

        // ---------- Bottom: info label ----------
        JLabel infoLabel = new JLabel("Recent activity and insights will appear here.");
        infoLabel.setFont(new Font("SansSerif", Font.PLAIN, 14));
        panel.add(infoLabel, BorderLayout.SOUTH);

        return panel;
    }

    /**
     * Helper method to create a small "stat card" showing a label and a value.
     *
     * @param title text describing what the value represents, e.g., "Total Revenue".
     * @param value text showing the numeric value, e.g., "$0.00".
     * @return a JPanel styled as a card with title and value.
     */
    private JPanel createStatCard(String title, String value) {
        // BorderLayout is used inside the card: title at top, value in center.
        JPanel card = new JPanel(new BorderLayout());

        // Adds padding inside the card.
        card.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Sets a very light background color to separate the card from the main background.
        card.setBackground(new Color(250, 250, 250));

        // Creates a compound border: a thin line around the card and extra inner padding.
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 220, 220)),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));

        // Title label (smaller font).
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("SansSerif", Font.PLAIN, 14));

        // Value label (larger, bold font).
        JLabel valueLabel = new JLabel(value);
        valueLabel.setFont(new Font("SansSerif", Font.BOLD, 20));

        // Place title at the top and value in the center.
        card.add(titleLabel, BorderLayout.NORTH);
        card.add(valueLabel, BorderLayout.CENTER);

        return card;
    }

    // =========================================================
    //   REVENUE SCREEN (Form)
    // =========================================================

    /**
     * Creates the Revenue screen, which provides a form for entering delivery earnings.
     *
     * @return a JPanel representing the Revenue form screen.
     */
    private JPanel createRevenueScreen() {
        // BorderLayout for: title at top, form in center, button at bottom.
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        panel.setBackground(Color.WHITE);

        // ---------- Title ----------
        JLabel titleLabel = new JLabel("Revenue - Add Delivery Earnings");
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 20));
        panel.add(titleLabel, BorderLayout.NORTH);

        // ---------- Form panel ----------
        // GridLayout(5, 2) for 5 rows and 2 columns (label + input).
        JPanel formPanel = new JPanel(new GridLayout(5, 2, 10, 10));
        formPanel.setBackground(Color.WHITE);

        // Row 1: Date
        JLabel dateLabel = new JLabel("Date (e.g., 2025-11-17):");
        revenueDateField = new JTextField();
        formPanel.add(dateLabel);
        formPanel.add(revenueDateField);

        // Row 2: Source (drop-down)
        JLabel sourceLabel = new JLabel("Source:");
        String[] sources = {"DoorDash", "Uber Eats", "Grubhub", "Postmates", "Other"};
        revenueSourceCombo = new JComboBox<>(sources);
        formPanel.add(sourceLabel);
        formPanel.add(revenueSourceCombo);

        // Row 3: Hours worked
        JLabel hoursLabel = new JLabel("Hours worked:");
        revenueHoursField = new JTextField();
        formPanel.add(hoursLabel);
        formPanel.add(revenueHoursField);

        // Row 4: Base pay
        JLabel basePayLabel = new JLabel("Base pay ($):");
        revenueBasePayField = new JTextField();
        formPanel.add(basePayLabel);
        formPanel.add(revenueBasePayField);

        // Row 5: Tips
        JLabel tipsLabel = new JLabel("Tips ($):");
        revenueTipsField = new JTextField();
        formPanel.add(tipsLabel);
        formPanel.add(revenueTipsField);

        panel.add(formPanel, BorderLayout.CENTER);

        // ---------- Bottom panel: Save button ----------
        JPanel bottomPanel = new JPanel();
        bottomPanel.setBackground(Color.WHITE);

        JButton saveButton = new JButton("Save Revenue");

        // The button click triggers handling of the form input.
        saveButton.addActionListener(e -> handleSaveRevenue());
        bottomPanel.add(saveButton);

        panel.add(bottomPanel, BorderLayout.SOUTH);

        return panel;
    }

    /**
     * Handles the "Save Revenue" action.
     * Reads user input from the form, validates numeric fields, calculates totals,
     * and shows the result in a confirmation dialog.
     */
    private void handleSaveRevenue() {
        // Retrieve text from text fields and drop-down selection.
        String date = revenueDateField.getText().trim();
        String source = (String) revenueSourceCombo.getSelectedItem();
        String hoursText = revenueHoursField.getText().trim();
        String basePayText = revenueBasePayField.getText().trim();
        String tipsText = revenueTipsField.getText().trim();

        double hours;
        double basePay;
        double tips;

        // Attempt to convert text input to numeric values.
        try {
            hours = Double.parseDouble(hoursText);
            basePay = Double.parseDouble(basePayText);
            tips = Double.parseDouble(tipsText);
        } catch (NumberFormatException ex) {
            // If any numeric field is invalid, show an error dialog and abort saving.
            JOptionPane.showMessageDialog(
                    this,
                    "Please enter valid numbers for hours, base pay, and tips.",
                    "Input Error",
                    JOptionPane.ERROR_MESSAGE
            );
            return;
        }

        // Compute total revenue (base pay + tips).
        double total = basePay + tips;

        // Compute approximate hourly rate, protecting against division by zero.
        double hourlyRate = hours > 0 ? total / hours : 0.0;

        // Build a detailed message summarizing the entry.
        String message = "Saved revenue:\n"
                + "Date: " + date + "\n"
                + "Source: " + source + "\n"
                + "Hours: " + hours + "\n"
                + "Base pay: $" + basePay + "\n"
                + "Tips: $" + tips + "\n"
                + "Total: $" + total + "\n"
                + "Approx. hourly: $" + String.format("%.2f", hourlyRate);

        // Show confirmation message in an information dialog.
        JOptionPane.showMessageDialog(
                this,
                message,
                "Revenue Saved",
                JOptionPane.INFORMATION_MESSAGE
        );

        // Future extension: at this point, data could be saved to a database or file.
    }

    // =========================================================
    //   EXPENSES SCREEN (Form)
    // =========================================================

    /**
     * Creates the Expenses screen, which provides a form for entering driver expenses.
     *
     * @return a JPanel representing the Expenses form screen.
     */
    private JPanel createExpensesScreen() {
        // BorderLayout for: title at top, form in center, button at bottom.
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        panel.setBackground(Color.WHITE);

        // ---------- Title ----------
        JLabel titleLabel = new JLabel("Expenses - Add Driver Expenses");
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 20));
        panel.add(titleLabel, BorderLayout.NORTH);

        // ---------- Form panel ----------
        // GridLayout(4, 2) for 4 rows and 2 columns (label + input).
        JPanel formPanel = new JPanel(new GridLayout(4, 2, 10, 10));
        formPanel.setBackground(Color.WHITE);

        // Row 1: Date
        JLabel dateLabel = new JLabel("Date (e.g., 2025-11-17):");
        expenseDateField = new JTextField();
        formPanel.add(dateLabel);
        formPanel.add(expenseDateField);

        // Row 2: Category
        JLabel categoryLabel = new JLabel("Category:");
        String[] categories = {"Gas", "Maintenance", "Parking", "Tolls", "Car Wash", "Other"};
        expenseCategoryCombo = new JComboBox<>(categories);
        formPanel.add(categoryLabel);
        formPanel.add(expenseCategoryCombo);

        // Row 3: Amount
        JLabel amountLabel = new JLabel("Amount ($):");
        expenseAmountField = new JTextField();
        formPanel.add(amountLabel);
        formPanel.add(expenseAmountField);

        // Row 4: Notes / Description
        JLabel notesLabel = new JLabel("Notes (optional):");
        expenseNotesField = new JTextField();
        formPanel.add(notesLabel);
        formPanel.add(expenseNotesField);

        panel.add(formPanel, BorderLayout.CENTER);

        // ---------- Bottom panel: Save button ----------
        JPanel bottomPanel = new JPanel();
        bottomPanel.setBackground(Color.WHITE);

        JButton saveButton = new JButton("Save Expense");

        // Button click triggers expense handling logic.
        saveButton.addActionListener(e -> handleSaveExpense());
        bottomPanel.add(saveButton);

        panel.add(bottomPanel, BorderLayout.SOUTH);

        return panel;
    }

    /**
     * Handles the "Save Expense" action.
     * Reads user input from the expenses form, validates the amount field,
     * and shows the result in a confirmation dialog.
     */
    private void handleSaveExpense() {
        // Retrieve text from fields and combo box.
        String date = expenseDateField.getText().trim();
        String category = (String) expenseCategoryCombo.getSelectedItem();
        String amountText = expenseAmountField.getText().trim();
        String notes = expenseNotesField.getText().trim();

        double amount;

        // Validate numeric input for amount.
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

        // Summary message for confirmation.
        String message = "Saved expense:\n"
                + "Date: " + date + "\n"
                + "Category: " + category + "\n"
                + "Amount: $" + amount + "\n"
                + "Notes: " + (notes.isEmpty() ? "(none)" : notes);

        JOptionPane.showMessageDialog(
                this,
                message,
                "Expense Saved",
                JOptionPane.INFORMATION_MESSAGE
        );

        // Future extension: data could be stored in a list, database, or file.
    }

    // =========================================================
    //   PLACEHOLDER SCREENS (Reports, Settings)
    // =========================================================

    /**
     * Creates a placeholder Reports screen.
     *
     * @return a simple JPanel with a label.
     */
    private JPanel createReportsScreen() {
        JPanel panel = new JPanel();
        JLabel label = new JLabel("Reports - Graphs and summaries will go here.");
        panel.add(label);
        return panel;
    }

    /**
     * Creates a placeholder Settings screen.
     *
     * @return a simple JPanel with a label.
     */
    private JPanel createSettingsScreen() {
        JPanel panel = new JPanel();
        JLabel label = new JLabel("Settings - App preferences and (later) login stuff.");
        panel.add(label);
        return panel;
    }

    // =========================================================
    //   SCREEN SWITCHING
    // =========================================================

    /**
     * Switches the visible screen in the CardLayout by name.
     *
     * @param name the key associated with the screen, such as "HOME" or "REVENUE".
     */
    private void showScreen(String name) {
        cardLayout.show(contentPanel, name);
    }

    // =========================================================
    //   MAIN METHOD (Program Entry Point)
    // =========================================================

    /**
     * Application entry point.
     * Responsible for creating and showing the main frame on the Event Dispatch Thread.
     */
    public static void main(String[] args) {
        // Ensures that GUI creation runs on the Event Dispatch Thread,
        // which is required for thread-safe Swing operations.
        SwingUtilities.invokeLater(() -> {
            FinanceAppFrame frame = new FinanceAppFrame();

            // Makes the application window visible on the screen.
            frame.setVisible(true);
        });
    }
}
