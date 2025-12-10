package org.example.driverFinancialServiceDispatcher;

import org.example.deliveryRecorder.src.deliveryContoller;
import org.example.deliveryRecorder.src.deliveryDataService;
import org.example.deliveryRecorder.src.overviewService;
import org.example.deliveryRecorder.src.vehicle;
import org.example.deliveryRecorder.src.vehicleDAO;
import org.example.deliveryRecorder.src.workPeriodService;
import org.example.gui.FinanceAppFrame;
import org.example.manageFinances.src.addBankAccount;
import org.example.manageFinances.src.generalFinancialData;
import org.example.manageFinances.src.manageFinancesController;
import org.example.manageFinances.src.selectBankAccount;
import org.example.manageFinances.src.selectBankAccountDAO;
import org.example.reportGenerator.src.deliveryCalculator;
import org.example.reportGenerator.src.generalReports;
import org.example.reportGenerator.src.reportDAO;
import org.example.userAccountController.src.userAccountController;
import org.example.userAccountController.src.createAccountDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Component;

import javax.swing.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public class serviceDispatcher {

    /**
     * ServiceDispatcher connects the Spring-managed services to the Swing GUI (FinanceAppFrame).
     * It acts as a bridge between the UI layer and the backend services.
     *
     * Integrated Modules:
     * - userAccountController: Login, account creation, password reset
     * - deliveryRecorder: Work periods, deliveries, vehicles
     * - manageFinances: Bank accounts, financial overview
     * - reportGenerator: Reports, analytics, calculations
     */
    /**
     * ServiceDispatcher is the central Spring-managed component that bridges the Swing GUI
     * with the database-backed services. It coordinates all application modules:
     * - User account management (login, registration, password reset)
     * - Delivery recording (work periods, deliveries, vehicles)
     * - Financial management (bank accounts, expenses, income)
     * - Report generation (analytics, calculations)
     *
     * To run the application with full database support, use FinancialDriverApplication.main()
     */
    @Component
    public static class ServiceDispatcher {

        // =========================================================
        //   USER ACCOUNT MODULE
        // =========================================================
        @Autowired
        private userAccountController accountController;

        @Autowired
        private createAccountDAO accountDAO;

        // =========================================================
        //   DELIVERY RECORDER MODULE
        // =========================================================
        @Autowired
        private workPeriodService workPeriodService;

        @Autowired
        private deliveryDataService deliveryDataService;

        @Autowired
        private deliveryContoller deliveryController;

        @Autowired
        private vehicleDAO vehicleDAO;

        @Autowired
        private overviewService overviewService;

        // =========================================================
        //   MANAGE FINANCES MODULE
        // =========================================================
        @Autowired
        private addBankAccount addBankAccountService;

        @Autowired
        private selectBankAccountDAO selectBankAccountDAO;

        @Autowired
        private generalFinancialData generalFinancialDataService;

        @Autowired
        private org.example.manageFinances.src.generalFinancialDataDAO generalFinancialDataDAO;

        // =========================================================
        //   REPORT GENERATOR MODULE
        // =========================================================
        @Autowired
        private reportDAO reportDAO;

        @Autowired
        private generalReports generalReportsService;

        // Calculator instance (uses reportDAO)
        private deliveryCalculator calculator;

        // Reference to the GUI frame
        private FinanceAppFrame financeAppFrame;

        // Current logged-in user ID
        private int currentUserId = -1;

        // Current logged-in username
        private String currentUsername = null;

        // Current work period ID
        private long currentWorkPeriodId = -1;

        // Spring application context
        private static ConfigurableApplicationContext springContext;

        /**
         * Main method to launch the application with Spring context and Swing GUI.
         * @deprecated Use FinancialDriverApplication.main() instead for proper Spring Boot startup.
         */
        @Deprecated
        public static void main(String[] args) {
            System.out.println("WARNING: ServiceDispatcher.main() is deprecated.");
            System.out.println("Please use org.example.FinancialDriverApplication.main() instead.");

            // Delegate to the proper application entry point
            org.example.FinancialDriverApplication.main(args);
        }

        /**
         * Sets the Spring application context.
         * Called by FinancialDriverApplication after context initialization.
         * @param context The Spring context
         */
        public void setSpringContext(ConfigurableApplicationContext context) {
            springContext = context;
        }

        /**
         * Initializes the delivery calculator with the reportDAO.
         */
        private void initializeCalculator() {
            this.calculator = new deliveryCalculator(reportDAO);
        }

        /**
         * Creates and displays the FinanceAppFrame GUI.
         */
        private void launchGUI() {
            // If we're running in a headless environment (CI, test runner, or no graphical display),
            // don't attempt to create Swing windows — that causes java.awt.HeadlessException.
            if (java.awt.GraphicsEnvironment.isHeadless()) {
                System.err.println("ServiceDispatcher: AWT is headless in this environment; skipping GUI startup.");
                return;
            }

            financeAppFrame = new FinanceAppFrame();
            financeAppFrame.setServiceDispatcher(this);
            financeAppFrame.setVisible(true);
        }

        // =========================================================
        //   ACCOUNT MANAGEMENT
        // =========================================================

        /**
         * Handles user login request.
         * @param username The username
         * @param password The password
         * @return true if login successful
         */
        public boolean login(String username, String password) {
            boolean success = accountController.handleLoginRequest(username, password);
            if (success) {
                // Get actual userId from database after login
                Integer userId = accountDAO.getUserIdByUsername(username);
                if (userId != null) {
                    currentUserId = userId;
                    currentUsername = username;
                    System.out.println("ServiceDispatcher: User '" + username + "' logged in successfully. UserID: " + currentUserId);
                } else {
                    // Fallback - shouldn't happen if login succeeded
                    currentUserId = -1;
                    System.out.println("ServiceDispatcher: WARNING - Could not retrieve user ID after successful login.");
                }
            }
            return success;
        }

        /**
         * Handles user account creation.
         * @param username The desired username
         * @param password The desired password
         * @param email The user's email
         * @return true if account created successfully
         */
        public boolean createAccount(String username, String password, String email) {
            return accountController.handleCreateAccount(username, password, email);
        }

        /**
         * Handles password reset request.
         * @param email The user's email
         * @return true if reset initiated successfully
         */
        public boolean resetPassword(String email) {
            return accountController.handlePasswordReset(email);
        }

        /**
         * Logs out the current user.
         */
        public void logout() {
            currentUserId = -1;
            currentUsername = null;
            currentWorkPeriodId = -1;
            System.out.println("ServiceDispatcher: User logged out.");
        }

        /**
         * Checks if a user is currently logged in.
         * @return true if logged in
         */
        public boolean isLoggedIn() {
            return currentUserId != -1;
        }

        /**
         * Gets the current logged-in username.
         * @return the username, or null if not logged in
         */
        public String getCurrentUsername() {
            return currentUsername;
        }

        /**
         * Gets the current logged-in user ID.
         * @return the user ID, or -1 if not logged in
         */
        public int getCurrentUserId() {
            return currentUserId;
        }

        // =========================================================
        //   WORK PERIOD MANAGEMENT
        // =========================================================

        /**
         * Starts a new work period for the current user.
         * @param vehicle The vehicle being used
         * @param startTime The start time (epoch milliseconds)
         * @return The work period ID, or -1 if failed
         */
        public long startWorkPeriod(String vehicle, long startTime) {
            if (currentUserId == -1) {
                System.out.println("ServiceDispatcher: Cannot start work period - user not logged in.");
                return -1;
            }

            workPeriodService.setVehicle(vehicle);
            workPeriodService.setStartTime(startTime);
            currentWorkPeriodId = workPeriodService.createWorkPeriod(currentUserId);
            return currentWorkPeriodId;
        }

        /**
         * Ends the current work period.
         * @param endTime The end time (epoch milliseconds)
         * @param totalMiles Total miles driven during the period (use -1 to auto-calculate from deliveries)
         */
        public void endWorkPeriod(long endTime, int totalMiles) {
            if (currentWorkPeriodId == -1) {
                System.out.println("ServiceDispatcher: No active work period to end.");
                return;
            }

            workPeriodService.setEndTime(endTime);

            if (totalMiles == -1) {
                // Auto-calculate total miles from all deliveries in this work period
                workPeriodService.calculateAndSetTotalVehicleMiles(currentWorkPeriodId);
            } else {
                workPeriodService.setTotalVehicleMiles(totalMiles);
            }

            // TODO: Update work period in database with end time and miles
            System.out.println("ServiceDispatcher: Work period ended. ID: " + currentWorkPeriodId +
                    ", Total Miles: " + workPeriodService.getTotalVehicleMiles());
        }

        /**
         * Calculates and returns the total miles for all deliveries in the current work period.
         * @return the total miles, or 0 if no active work period
         */
        public int calculateWorkPeriodMiles() {
            if (currentWorkPeriodId == -1) {
                System.out.println("ServiceDispatcher: No active work period.");
                return 0;
            }
            return workPeriodService.calculateAndSetTotalVehicleMiles(currentWorkPeriodId);
        }

        /**
         * Calculates and returns the total miles for all deliveries associated with a specific jobId.
         * @param jobId the work period/job ID
         * @return the total miles for that work period
         */
        public int calculateMilesForJob(long jobId) {
            return workPeriodService.calculateAndSetTotalVehicleMiles(jobId);
        }

        /**
         * Gets the current work period ID.
         * @return the work period ID, or -1 if no active period
         */
        public long getCurrentWorkPeriodId() {
            return currentWorkPeriodId;
        }

        // =========================================================
        //   DELIVERY MANAGEMENT
        // =========================================================

        /**
         * Adds a new delivery to the current work period.
         * @param restaurant The restaurant name
         * @param basePay The base pay amount
         * @param tips The tip amount
         * @param platform The delivery platform (e.g., DoorDash, UberEats)
         * @param miles Miles driven for this delivery
         * @param startTime Delivery start time (epoch milliseconds)
         * @param endTime Delivery end time (epoch milliseconds)
         * @param waitTime Minutes spent waiting at restaurant
         * @return true if delivery added successfully
         */
        public boolean addDelivery(String restaurant, float basePay, float tips, String platform,
                                    int miles, long startTime, long endTime, int waitTime) {
            if (currentWorkPeriodId == -1) {
                System.out.println("ServiceDispatcher: Cannot add delivery - no active work period.");
                return false;
            }

            deliveryDataService.setRestaurant(restaurant);
            deliveryDataService.setBasePay(basePay);
            deliveryDataService.setExpenses(tips); // Tips stored in expenses field? Check data model
            deliveryDataService.setPlatform(platform);
            deliveryDataService.setMilesDriven(miles);
            deliveryDataService.setDateTimeStart(startTime);
            deliveryDataService.setDateTimeEnd(endTime);
            deliveryDataService.setMinutesSpentWaitingAtResturant(waitTime);

            // Calculate total time spent
            int totalMinutes = (int) ((endTime - startTime) / 60000);
            deliveryDataService.setTotalTimeSpent(totalMinutes);

            return workPeriodService.addDelivery(deliveryDataService);
        }

        // =========================================================
        //   REPORT & ANALYTICS
        // =========================================================

        /**
         * Gets delivery pay data for a date range.
         * @param startDate Start date
         * @param endDate End date
         * @return List of delivery data maps
         */
        public List<Map<String, Object>> getDeliveryPayByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
            return reportDAO.getDeliveryPayWithTimestampByDateRange(startDate, endDate);
        }

        /**
         * Gets all jobs from the database.
         * @return List of job data maps
         */
        public List<Map<String, Object>> getAllJobs() {
            return reportDAO.getAllJobs();
        }

        /**
         * Gets job start times from the database.
         * @return List of start times (epoch milliseconds)
         */
        public List<Long> getJobStartTimes() {
            return reportDAO.getJobStartTimes();
        }

        /**
         * Gets the job start time range from the database.
         * @return Map with minStartTime and maxStartTime
         */
        public Map<String, Object> getJobStartTimeRange() {
            return reportDAO.getJobStartTimeRange();
        }

        // =========================================================
        //   CALCULATIONS
        // =========================================================

        /**
         * Calculates net profit.
         * @param revenue Total revenue
         * @param expenses Total expenses
         * @return Net profit
         */
        public float calculateNetProfit(float revenue, float expenses) {
            return calculator.calculateNetProfit(revenue, expenses);
        }

        /**
         * Calculates total revenue from base pay and tips arrays.
         * @param basePay Array of base pay values
         * @param tips Array of tip values
         * @return Total revenue
         */
        public float calculateRevenue(float[] basePay, float[] tips) {
            return calculator.calculuateRevenue(basePay, tips);
        }

        /**
         * Calculates total expenses.
         * @param expenses Array of expense values
         * @return Total expenses
         */
        public float calculateExpenses(float[] expenses) {
            return calculator.calculateExpenses(expenses);
        }

        /**
         * Calculates vehicle depreciation.
         * @param startingValue Starting vehicle value
         * @param milesDriven Miles driven
         * @return Depreciated value
         */
        public float calculateVehicleDepreciation(float startingValue, int milesDriven) {
            return calculator.calcualteVehicleDeprication(startingValue, milesDriven);
        }

        /**
         * Finds optimal restaurants based on profit.
         * @param restaurants Array of restaurant names
         * @param profitList Array of profit values
         * @return Sorted array of restaurants (highest profit first)
         */
        public String[] findOptimalRestaurants(String[] restaurants, float[] profitList) {
            return calculator.findOptimalResturnats(restaurants, profitList);
        }

        /**
         * Calculates the gallons of gas used based on miles driven and vehicle MPG.
         * @param mpg The miles per gallon of the vehicle
         * @param milesDriven The total miles driven
         * @return The gallons of gas used
         */
        public double calculateGasUsed(double mpg, double milesDriven) {
            return calculator.gasUsed(mpg, milesDriven);
        }

        /**
         * Calculates the estimated gas cost based on miles driven, vehicle MPG, and gas price per gallon.
         * @param mpg The miles per gallon of the vehicle
         * @param milesDriven The total miles driven
         * @param gasPricePerGallon The current price of gas per gallon
         * @return The estimated gas cost in dollars
         */
        public double calculateGasCost(double mpg, double milesDriven, double gasPricePerGallon) {
            return calculator.calculateGasCost(mpg, milesDriven, gasPricePerGallon);
        }

        /**
         * Calculates the total gas cost using a single MPG value for all deliveries.
         * @param totalMiles The total miles driven across all deliveries
         * @param mpg The miles per gallon of the vehicle
         * @param gasPricePerGallon The current price of gas per gallon
         * @return The total estimated gas cost
         */
        public double calculateTotalGasCost(double totalMiles, double mpg, double gasPricePerGallon) {
            return calculator.calculateTotalGasCost(totalMiles, mpg, gasPricePerGallon);
        }

        // =========================================================
        //   BANK ACCOUNT MANAGEMENT
        // =========================================================

        /**
         * Creates a new bank account for a user.
         * @param ownerUsername The username of the account owner
         * @param accountName The name for the account
         * @param accountType The type of account (checking, savings, etc.)
         * @param initialBalance The initial balance
         * @param interestRate The interest rate
         * @param accountFees The monthly account fees
         * @param otherIncome Other income associated with account
         * @return true if account created successfully
         */
        public boolean createBankAccount(String ownerUsername, String accountName, String accountType,
                                         double initialBalance, double interestRate, double accountFees, double otherIncome) {
            return addBankAccountService.createNewBankAccount(
                    ownerUsername, accountName, accountType, initialBalance, interestRate, accountFees, otherIncome);
        }

        /**
         * Gets all bank accounts for a user.
         * @param userId The user ID
         * @return List of bank accounts
         */
        public List<selectBankAccount> getUserBankAccounts(int userId) {
            return selectBankAccountDAO.getAccountsForUser(userId);
        }

        /**
         * Gets all bank accounts for the current logged-in user.
         * @return List of bank accounts, or empty list if not logged in
         */
        public List<selectBankAccount> getCurrentUserBankAccounts() {
            if (currentUserId == -1) {
                System.out.println("ServiceDispatcher: Cannot get accounts - user not logged in.");
                return List.of();
            }
            return selectBankAccountDAO.getAccountsForUser(currentUserId);
        }

        /**
         * Gets a bank account by its ID.
         * @param accountId The account ID
         * @return The bank account, or null if not found
         */
        public selectBankAccount getBankAccountById(String accountId) {
            return selectBankAccountDAO.findById(accountId);
        }

        /**
         * Gets all bank accounts in the system.
         * @return List of all bank accounts
         */
        public List<selectBankAccount> getAllBankAccounts() {
            return selectBankAccountDAO.findAll();
        }

        /**
         * Updates the balance of a bank account.
         * @param accountId The account ID
         * @param newBalance The new balance
         */
        public void updateAccountBalance(String accountId, float newBalance) {
            selectBankAccountDAO.updateBalance(accountId, newBalance);
        }

        /**
         * Updates the interest rate of a bank account.
         * @param accountId The account ID
         * @param interestRate The new interest rate
         */
        public void updateAccountInterestRate(String accountId, float interestRate) {
            selectBankAccountDAO.updateInterestRate(accountId, interestRate);
        }

        /**
         * Updates the account fees.
         * @param accountId The account ID
         * @param accountFees The new account fees
         */
        public void updateAccountFees(String accountId, float accountFees) {
            selectBankAccountDAO.updateAccountFees(accountId, accountFees);
        }

        /**
         * Updates other income for an account.
         * @param accountId The account ID
         * @param otherIncome The new other income value
         */
        public void updateOtherIncome(String accountId, float otherIncome) {
            selectBankAccountDAO.updateOtherIncome(accountId, otherIncome);
        }

        /**
         * Deletes a bank account.
         * @param accountId The account ID to delete
         */
        public void deleteBankAccount(String accountId) {
            selectBankAccountDAO.delete(accountId);
        }

        // =========================================================
        //   VEHICLE MANAGEMENT
        // =========================================================

        /**
         * Gets a vehicle by its model name.
         * @param vehicleModel The vehicle model name
         * @return The vehicle, or null if not found
         */
        public vehicle getVehicleByModel(String vehicleModel) {
            return vehicleDAO.findByModel(vehicleModel);
        }

        /**
         * Gets all vehicles in the system.
         * @return List of all vehicles
         */
        public List<vehicle> getAllVehicles() {
            return vehicleDAO.findAll();
        }

        /**
         * Adds a new vehicle.
         * @param vehicleType The type of vehicle
         * @param vehicleModel The model name
         * @param currentVehicleDriven The current vehicle driven status
         * @param currentMiles Current vehicle miles
         */
        public void addVehicle(String vehicleType, String vehicleModel, String currentVehicleDriven, int currentMiles) {
            addVehicle(vehicleType, vehicleModel, currentVehicleDriven, currentMiles, 0.0);
        }

        /**
         * Adds a new vehicle with MPG.
         * @param vehicleType The type of vehicle
         * @param vehicleModel The model name
         * @param currentVehicleDriven The current vehicle driven status
         * @param currentMiles Current vehicle miles
         * @param mpg Miles per gallon for gas cost calculations
         */
        public void addVehicle(String vehicleType, String vehicleModel, String currentVehicleDriven, int currentMiles, double mpg) {
            vehicle v = new vehicle();
            v.setVehicleType(vehicleType);
            v.setVehicleModel(vehicleModel);
            v.setCurrentVehicleDriven(currentVehicleDriven);
            v.setCurrentVehicleMiles(currentMiles);
            v.setVehicleMpg(mpg);
            vehicleDAO.create(v);
        }

        /**
         * Updates an existing vehicle.
         * @param v The vehicle with updated data
         */
        public void updateVehicle(vehicle v) {
            vehicleDAO.update(v);
        }

        /**
         * Deletes a vehicle by model name.
         * @param vehicleModel The vehicle model to delete
         */
        public void deleteVehicle(String vehicleModel) {
            vehicleDAO.delete(vehicleModel);
        }

        /**
         * Gets the current vehicle being used.
         * If no vehicle is marked as current, the first vehicle in the database is returned and marked as current.
         * @return The current vehicle, or null if no vehicles exist
         */
        public vehicle getCurrentVehicle() {
            return vehicleDAO.findCurrentVehicle();
        }

        /**
         * Sets the specified vehicle as the current vehicle.
         * @param vehicleModel The vehicle model to set as current
         */
        public void setCurrentVehicle(String vehicleModel) {
            vehicleDAO.setAsCurrentVehicle(vehicleModel);
        }

        // =========================================================
        //   FINANCIAL OVERVIEW
        // =========================================================

        /**
         * Gets total assets for a user.
         * @param userId The user ID
         * @return Total assets value
         */
        public float getTotalAssets(int userId) {
            return generalFinancialDataService.getTotalAssets(userId);
        }

        /**
         * Gets total assets for the current logged-in user.
         * @return Total assets value, or 0 if not logged in
         */
        public float getCurrentUserTotalAssets() {
            if (currentUserId == -1) return 0;
            return generalFinancialDataService.getTotalAssets(currentUserId);
        }

        /**
         * Gets total expenses for a user.
         * @param userId The user ID
         * @return Total expenses value
         */
        public float getTotalExpenses(int userId) {
            return generalFinancialDataService.getTotalExpense(userId);
        }

        /**
         * Gets total expenses for the current logged-in user.
         * @return Total expenses value, or 0 if not logged in
         */
        public float getCurrentUserTotalExpenses() {
            if (currentUserId == -1) return 0;
            return generalFinancialDataService.getTotalExpense(currentUserId);
        }

        /**
         * Gets net financial position (Assets - Expenses) for a user.
         * @param userId The user ID
         * @return Net financial position
         */
        public float getNetFinancialPosition(int userId) {
            return generalFinancialDataService.getNetFinancialPosition(userId);
        }

        /**
         * Gets net financial position for the current logged-in user.
         * @return Net financial position, or 0 if not logged in
         */
        public float getCurrentUserNetFinancialPosition() {
            if (currentUserId == -1) return 0;
            return generalFinancialDataService.getNetFinancialPosition(currentUserId);
        }

        /**
         * Gets all transactions for a user.
         * @param userId The user ID
         * @return Map of transaction categories to transaction lists
         */
        public Map<String, List<Float>> getAllTransactions(int userId) {
            return generalFinancialDataService.getAllTransactions(userId);
        }

        /**
         * Gets all transactions for the current logged-in user.
         * @return Map of transactions, or empty map if not logged in
         */
        public Map<String, List<Float>> getCurrentUserTransactions() {
            if (currentUserId == -1) return Map.of();
            return generalFinancialDataService.getAllTransactions(currentUserId);
        }

        /**
         * Gets all detailed transaction records for the current user across all bank accounts.
         * @return List of TransactionSummary objects, or empty list if not logged in.
         */
        public List<org.example.manageFinances.src.generalFinancialData.TransactionSummary> getCurrentUserAllTransactionDetails() {
            if (currentUserId == -1) return List.of();
            return generalFinancialDataService.getAllTransactionDetails(currentUserId);
        }

        /**
         * Gets total income for the current user.
         * @return Total income, or 0 if not logged in.
         */
        public float getCurrentUserTotalIncome() {
            if (currentUserId == -1) return 0;
            return generalFinancialDataService.getTotalIncome(currentUserId);
        }

        // =========================================================
        //   BANK ACCOUNT-SPECIFIC TRANSACTIONS
        // =========================================================

        /**
         * Gets all detailed transaction records for a specific bank account.
         * @param bankAccountId The bank account ID.
         * @return List of TransactionRecord objects.
         */
        public List<org.example.manageFinances.src.generalFinancialDataDAO.TransactionRecord> getTransactionsForBankAccount(int bankAccountId) {
            return generalFinancialDataDAO.getTransactionsForBankAccount(bankAccountId);
        }

        /**
         * Gets detailed transaction records for a bank account filtered by type.
         * @param bankAccountId The bank account ID.
         * @param transactionType The transaction type to filter by.
         * @return List of TransactionRecord objects.
         */
        public List<org.example.manageFinances.src.generalFinancialDataDAO.TransactionRecord> getTransactionsForBankAccountByType(int bankAccountId, String transactionType) {
            return generalFinancialDataDAO.getTransactionsForBankAccountByType(bankAccountId, transactionType);
        }

        /**
         * Adds a transaction tied to a specific bank account for the current user.
         * @param amount The amount.
         * @param transactionType The transaction type.
         * @param bankAccountId The bank account ID.
         * @param description Optional description.
         */
        public void addTransactionForBankAccount(float amount, String transactionType, int bankAccountId, String description) {
            if (currentUserId == -1) {
                throw new IllegalStateException("User must be logged in to add transactions.");
            }
            generalFinancialDataDAO.addTransactionForBankAccount(currentUserId, amount, transactionType, bankAccountId, description);
        }

        // =========================================================
        //   GENERAL REPORTS
        // =========================================================

        /**
         * Calculates total income from an array.
         * @param income Array of income values
         * @return Total income
         */
        public float getTotalIncome(float[] income) {
            return generalReportsService.totalIncome(income);
        }

        /**
         * Calculates average income from an array.
         * @param income Array of income values
         * @return Average income
         */
        public float getAverageIncome(float[] income) {
            return generalReportsService.averageIncome(income);
        }

        /**
         * Gets total earnings from database for a date range.
         * @param startTime Start of date range
         * @param endTime End of date range
         * @return Total earnings
         */
        public float getTotalEarningsFromDB(LocalDateTime startTime, LocalDateTime endTime) {
            return generalReportsService.getTotalEarningsFromDB(startTime, endTime);
        }

        /**
         * Gets average earnings per delivery from database for a date range.
         * @param startTime Start of date range
         * @param endTime End of date range
         * @return Average earnings per delivery
         */
        public float getAverageEarningsFromDB(LocalDateTime startTime, LocalDateTime endTime) {
            return generalReportsService.getAverageEarningsFromDB(startTime, endTime);
        }

        // =========================================================
        //   DELIVERY CONTROLLER
        // =========================================================

        /**
         * Saves a delivery record using the delivery controller.
         * @param deliveryData The delivery data to save
         * @return true if saved successfully
         */
        public boolean saveDeliveryRecord(deliveryDataService deliveryData) {
            return deliveryController.saveDeliveryRecord(deliveryData);
        }

        /**
         * Finishes the current shift.
         */
        /*public void finishShift() {
            deliveryController.finishShift();
        }*/
        /**
         * Sets the vehicle for the current shift.
         * @param vehicleName The vehicle name
         */
        /*public void setShiftVehicle(String vehicleName) {
            deliveryController.setVehicle(vehicleName);
        }*/

        /**
         * Gets the delivery data service for direct field manipulation.
         * @return The delivery data service
         */
        public deliveryDataService getDeliveryDataService() {
            return deliveryDataService;
        }

        /**
         * Gets the work period service for direct manipulation.
         * @return The work period service
         */
        public workPeriodService getWorkPeriodService() {
            return workPeriodService;
        }

        // =========================================================
        //   DELIVERY OVERVIEW (VIEW PAST JOBS)
        // =========================================================

        /**
         * Gets all past deliveries for the current user with combined work period data.
         * @return List of OverviewDTO containing delivery and work period information
         */
        public List<overviewService.OverviewDTO> getCurrentUserPastDeliveries() {
            if (currentUserId == -1) {
                System.out.println("ServiceDispatcher: Cannot get past deliveries - user not logged in.");
                return List.of();
            }
            return overviewService.getFullOverviewByUser(currentUserId);
        }

        /**
         * Gets all past deliveries for a specific user with combined work period data.
         * @param userId The user ID
         * @return List of OverviewDTO containing delivery and work period information
         */
        public List<overviewService.OverviewDTO> getPastDeliveriesByUser(int userId) {
            return overviewService.getFullOverviewByUser(userId);
        }

        /**
         * Gets total earnings for the current user across all deliveries.
         * @return Total earnings (base pay + tips)
         */
        public double getCurrentUserTotalDeliveryEarnings() {
            if (currentUserId == -1) return 0.0;
            return overviewService.getTotalEarningsByUser(currentUserId);
        }

        /**
         * Gets total earnings for a specific user across all deliveries.
         * @param userId The user ID
         * @return Total earnings (base pay + tips)
         */
        public double getTotalDeliveryEarningsByUser(int userId) {
            return overviewService.getTotalEarningsByUser(userId);
        }

        /**
         * Gets the total number of deliveries for the current user.
         * @return Total delivery count
         */
        public int getCurrentUserTotalDeliveries() {
            if (currentUserId == -1) return 0;
            return overviewService.getTotalDeliveriesByUser(currentUserId);
        }

        /**
         * Gets the total number of deliveries for a specific user.
         * @param userId The user ID
         * @return Total delivery count
         */
        public int getTotalDeliveriesByUser(int userId) {
            return overviewService.getTotalDeliveriesByUser(userId);
        }

        /**
         * Gets total miles driven for the current user across all deliveries.
         * @return Total miles driven
         */
        public int getCurrentUserTotalMiles() {
            if (currentUserId == -1) return 0;
            return overviewService.getTotalMilesByUser(currentUserId);
        }

        /**
         * Gets total miles driven for a specific user across all deliveries.
         * @param userId The user ID
         * @return Total miles driven
         */
        public int getTotalMilesByUser(int userId) {
            return overviewService.getTotalMilesByUser(userId);
        }

        /**
         * Gets all work periods for the current user.
         * @return List of work periods
         */
        public List<workPeriodService> getCurrentUserWorkPeriods() {
            if (currentUserId == -1) {
                System.out.println("ServiceDispatcher: Cannot get work periods - user not logged in.");
                return List.of();
            }
            return overviewService.getAllWorkPeriodsByUser(currentUserId);
        }

        /**
         * Gets all work periods for a specific user.
         * @param userId The user ID
         * @return List of work periods
         */
        public List<workPeriodService> getWorkPeriodsByUser(int userId) {
            return overviewService.getAllWorkPeriodsByUser(userId);
        }

        /**
         * Gets all deliveries for the current user.
         * @return List of deliveries
         */
        public List<deliveryDataService> getCurrentUserDeliveries() {
            if (currentUserId == -1) {
                System.out.println("ServiceDispatcher: Cannot get deliveries - user not logged in.");
                return List.of();
            }
            return overviewService.getAllDeliveriesByUser(currentUserId);
        }

        /**
         * Gets all deliveries for a specific user.
         * @param userId The user ID
         * @return List of deliveries
         */
        public List<deliveryDataService> getDeliveriesByUser(int userId) {
            return overviewService.getAllDeliveriesByUser(userId);
        }

        /**
         * Gets deliveries for a specific work period.
         * @param workPeriodId The work period ID
         * @return List of deliveries in the work period
         */
        public List<deliveryDataService> getDeliveriesForWorkPeriod(int workPeriodId) {
            return overviewService.getDeliveriesByWorkPeriod(workPeriodId);
        }

        /**
         * Gets a specific work period by its ID.
         * @param workPeriodId The work period ID
         * @return The work period, or null if not found
         */
        public workPeriodService getWorkPeriodById(int workPeriodId) {
            return overviewService.getWorkPeriodById(workPeriodId);
        }

        /**
         * Gets a specific delivery by its ID.
         * @param deliveryId The delivery ID
         * @return The delivery, or null if not found
         */
        public deliveryDataService getDeliveryById(int deliveryId) {
            return overviewService.getDeliveryById(deliveryId);
        }

        // =========================================================
        //   USER SESSION MANAGEMENT
        // =========================================================

        /**
         * Sets the current user ID after successful login.
         * This should be called by the login service after authentication.
         * @param userId The user ID to set
         */
        public void setCurrentUserId(int userId) {
            this.currentUserId = userId;
        }

        // =========================================================
        //   UTILITY METHODS
        // =========================================================

        /**
         * Gets the Spring application context.
         * @return The Spring context
         */
        public static ConfigurableApplicationContext getSpringContext() {
            return springContext;
        }

        /**
         * Shuts down the application gracefully.
         */
        public void shutdown() {
            System.out.println("ServiceDispatcher: Shutting down application...");
            if (springContext != null) {
                springContext.close();
            }
            System.exit(0);
        }

        /**
         * Gets the FinanceAppFrame reference.
         * @return The GUI frame
         */
        public FinanceAppFrame getFinanceAppFrame() {
            return financeAppFrame;
        }

        /**
         * Public helper to initialize and start the GUI from external mains.
         * Ensures calculator is initialized and GUI is launched on the EDT.
         */
        public void startGui() {
            initializeCalculator();
            SwingUtilities.invokeLater(() -> launchGUI());
        }
    }
}
