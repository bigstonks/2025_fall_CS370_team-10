package org.example;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * Initializes the SQLite database tables on application startup.
 * This ensures all required tables exist before the application starts.
 */
@Component
@Order(1)  // Run first among CommandLineRunners
public class DatabaseInitializer implements CommandLineRunner {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Override
    public void run(String... args) {
        System.out.println("DatabaseInitializer: Checking and creating database tables...");
        System.out.println("DatabaseInitializer: Working directory = " + System.getProperty("user.dir"));

        try {
            createUserAccountTable();
            createJobsTable();
            createDeliveryDataTable();
            createBankAccountTable();
            createTransactionTable();
            createVehicleTable();
            createPlatformTable();
            createNetValueTable();

            System.out.println("DatabaseInitializer: All tables verified/created successfully!");
        } catch (Exception e) {
            System.err.println("DatabaseInitializer: Error creating tables - " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void createUserAccountTable() {
        String sql = "CREATE TABLE IF NOT EXISTS userAccount (" +
            "userID INTEGER PRIMARY KEY AUTOINCREMENT, " +
            "userName VARCHAR(45) NOT NULL UNIQUE, " +
            "password VARCHAR(255) NOT NULL, " +
            "emailAddress VARCHAR(100), " +
            "authorization VARCHAR(45) DEFAULT 'user'" +
            ")";
        jdbcTemplate.execute(sql);
        System.out.println("DatabaseInitializer: userAccount table ready");
    }

    private void createJobsTable() {
        String sql = "CREATE TABLE IF NOT EXISTS JobsTable (" +
            "jobsId INTEGER PRIMARY KEY AUTOINCREMENT, " +
            "userId INTEGER NOT NULL, " +
            "startTime BIGINT, " +
            "endTime BIGINT, " +
            "vehicle VARCHAR(45), " +
            "totalEarnings REAL DEFAULT 0.0, " +
            "FOREIGN KEY (userId) REFERENCES userAccount(userID) ON DELETE CASCADE" +
            ")";
        jdbcTemplate.execute(sql);
        System.out.println("DatabaseInitializer: JobsTable ready");
    }

    private void createDeliveryDataTable() {
        String sql = "CREATE TABLE IF NOT EXISTS deliveryData (" +
            "iddeliveryData INTEGER PRIMARY KEY AUTOINCREMENT, " +
            "toLocation VARCHAR(100), " +
            "fromLocation VARCHAR(100), " +
            "resturant VARCHAR(100), " +
            "basePay REAL DEFAULT 0.0, " +
            "tips REAL DEFAULT 0.0, " +
            "extraExpenses REAL DEFAULT 0.0, " +
            "platform VARCHAR(45), " +
            "totalTimeSpent INTEGER, " +
            "miles INTEGER DEFAULT 0, " +
            "timeSpentWaiting INTEGER DEFAULT 0, " +
            "startTime BIGINT, " +
            "endTime BIGINT, " +
            "jobsTableId INTEGER NOT NULL, " +
            "FOREIGN KEY (jobsTableId) REFERENCES JobsTable(jobsId) ON DELETE CASCADE" +
            ")";
        jdbcTemplate.execute(sql);
        System.out.println("DatabaseInitializer: deliveryData table ready");
    }

    private void createBankAccountTable() {
        String sql = "CREATE TABLE IF NOT EXISTS bankAccount (" +
            "idbankAccount INTEGER PRIMARY KEY AUTOINCREMENT, " +
            "userId INTEGER NOT NULL, " +
            "accountType VARCHAR(45), " +
            "balance REAL DEFAULT 0.00, " +
            "interestRate REAL DEFAULT 0.0000, " +
            "accountFees REAL DEFAULT 0.00, " +
            "otherIncome REAL DEFAULT 0.00, " +
            "FOREIGN KEY (userId) REFERENCES userAccount(userID) ON DELETE CASCADE" +
            ")";
        jdbcTemplate.execute(sql);
        System.out.println("DatabaseInitializer: bankAccount table ready");
    }

    private void createTransactionTable() {
        String sql = "CREATE TABLE IF NOT EXISTS [transaction] (" +
            "idTransaction INTEGER PRIMARY KEY AUTOINCREMENT, " +
            "userId INTEGER NOT NULL, " +
            "transactionType VARCHAR(50), " +
            "amount REAL DEFAULT 0.0, " +
            "description TEXT, " +
            "transactionDate BIGINT, " +
            "category VARCHAR(50), " +
            "FOREIGN KEY (userId) REFERENCES userAccount(userID) ON DELETE CASCADE" +
            ")";
        jdbcTemplate.execute(sql);
        System.out.println("DatabaseInitializer: transaction table ready");
    }

    private void createVehicleTable() {
        String sql = "CREATE TABLE IF NOT EXISTS vehicle (" +
            "vehicleId INTEGER PRIMARY KEY AUTOINCREMENT, " +
            "userId INTEGER NOT NULL, " +
            "vehicleName VARCHAR(100), " +
            "vehicleType VARCHAR(45), " +
            "mpg REAL DEFAULT 0.0, " +
            "startingMiles INTEGER DEFAULT 0, " +
            "currentMiles INTEGER DEFAULT 0, " +
            "purchasePrice REAL DEFAULT 0.0, " +
            "isDefault INTEGER DEFAULT 0, " +
            "FOREIGN KEY (userId) REFERENCES userAccount(userID) ON DELETE CASCADE" +
            ")";
        jdbcTemplate.execute(sql);
        System.out.println("DatabaseInitializer: vehicle table ready");
    }

    private void createPlatformTable() {
        String sql = "CREATE TABLE IF NOT EXISTS platform (" +
            "idPlatform INTEGER PRIMARY KEY AUTOINCREMENT, " +
            "platformName VARCHAR(45), " +
            "userId INTEGER, " +
            "FOREIGN KEY (userId) REFERENCES userAccount(userID) ON DELETE CASCADE" +
            ")";
        jdbcTemplate.execute(sql);
        System.out.println("DatabaseInitializer: platform table ready");
    }

    private void createNetValueTable() {
        String sql = "CREATE TABLE IF NOT EXISTS netValue (" +
            "idNetValue INTEGER PRIMARY KEY AUTOINCREMENT, " +
            "userId INTEGER NOT NULL, " +
            "totalRevenue REAL DEFAULT 0.0, " +
            "totalExpenses REAL DEFAULT 0.0, " +
            "netProfit REAL DEFAULT 0.0, " +
            "calculatedDate BIGINT, " +
            "FOREIGN KEY (userId) REFERENCES userAccount(userID) ON DELETE CASCADE" +
            ")";
        jdbcTemplate.execute(sql);
        System.out.println("DatabaseInitializer: netValue table ready");
    }
}

