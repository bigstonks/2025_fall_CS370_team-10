-- SQLite Schema for Financial Driver Application
-- This file is auto-executed by Spring Boot on startup

-- User accounts table
CREATE TABLE IF NOT EXISTS userAccount (
    userID INTEGER PRIMARY KEY AUTOINCREMENT,
    userName VARCHAR(45) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    emailAddress VARCHAR(100),
    authorization VARCHAR(45) DEFAULT 'user'
);

-- Jobs/Work periods table
CREATE TABLE IF NOT EXISTS JobsTable (
    jobsId INTEGER PRIMARY KEY AUTOINCREMENT,
    userId INTEGER NOT NULL,
    startTime BIGINT,
    endTime BIGINT,
    vehicle VARCHAR(45),
    totalEarnings REAL DEFAULT 0.0,
    FOREIGN KEY (userId) REFERENCES userAccount(userID) ON DELETE CASCADE
);

-- Delivery data table
CREATE TABLE IF NOT EXISTS deliveryData (
    iddeliveryData INTEGER PRIMARY KEY AUTOINCREMENT,
    toLocation VARCHAR(100),
    fromLocation VARCHAR(100),
    resturant VARCHAR(100),
    basePay REAL DEFAULT 0.0,
    tips REAL DEFAULT 0.0,
    extraExpenses REAL DEFAULT 0.0,
    platform VARCHAR(45),
    totalTimeSpent INTEGER,
    miles INTEGER DEFAULT 0,
    timeSpentWaiting INTEGER DEFAULT 0,
    startTime BIGINT,
    endTime BIGINT,
    jobsTableId INTEGER NOT NULL,
    FOREIGN KEY (jobsTableId) REFERENCES JobsTable(jobsId) ON DELETE CASCADE
);

-- Bank accounts table
CREATE TABLE IF NOT EXISTS bankAccount (
    idbankAccount INTEGER PRIMARY KEY AUTOINCREMENT,
    userId INTEGER NOT NULL,
    accountType VARCHAR(45),
    balance REAL DEFAULT 0.00,
    interestRate REAL DEFAULT 0.0000,
    accountFees REAL DEFAULT 0.00,
    otherIncome REAL DEFAULT 0.00,
    FOREIGN KEY (userId) REFERENCES userAccount(userID) ON DELETE CASCADE
);

-- Transaction table
CREATE TABLE IF NOT EXISTS "transaction" (
    idTransaction INTEGER PRIMARY KEY AUTOINCREMENT,
    userId INTEGER NOT NULL,
    transactionType VARCHAR(50),
    amount REAL DEFAULT 0.0,
    description TEXT,
    transactionDate BIGINT,
    category VARCHAR(50),
    FOREIGN KEY (userId) REFERENCES userAccount(userID) ON DELETE CASCADE
);

-- Vehicle table
CREATE TABLE IF NOT EXISTS vehicle (
    vehicleId INTEGER PRIMARY KEY AUTOINCREMENT,
    userId INTEGER NOT NULL,
    vehicleName VARCHAR(100),
    vehicleType VARCHAR(45),
    mpg REAL DEFAULT 0.0,
    startingMiles INTEGER DEFAULT 0,
    currentMiles INTEGER DEFAULT 0,
    purchasePrice REAL DEFAULT 0.0,
    isDefault INTEGER DEFAULT 0,
    FOREIGN KEY (userId) REFERENCES userAccount(userID) ON DELETE CASCADE
);

-- Platform table (for delivery platforms like UberEats, DoorDash, etc.)
CREATE TABLE IF NOT EXISTS platform (
    idPlatform INTEGER PRIMARY KEY AUTOINCREMENT,
    platformName VARCHAR(45),
    userId INTEGER,
    FOREIGN KEY (userId) REFERENCES userAccount(userID) ON DELETE CASCADE
);

-- Net value tracking table
CREATE TABLE IF NOT EXISTS netValue (
    idNetValue INTEGER PRIMARY KEY AUTOINCREMENT,
    userId INTEGER NOT NULL,
    totalRevenue REAL DEFAULT 0.0,
    totalExpenses REAL DEFAULT 0.0,
    netProfit REAL DEFAULT 0.0,
    calculatedDate BIGINT,
    FOREIGN KEY (userId) REFERENCES userAccount(userID) ON DELETE CASCADE
);

