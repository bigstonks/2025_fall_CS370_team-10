package org.example;

import org.example.driverFinancialServiceDispatcher.serviceDispatcher;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

import javax.swing.*;

/**
 * Main Spring Boot application entry point for the Financial Driver application.
 *
 * This class bootstraps the Spring context and launches the Swing GUI.
 * It ensures all Spring-managed beans (controllers, services, DAOs) are
 * properly initialized and autowired before the GUI starts.
 *
 * Run this class to start the application with database connectivity.
 */
@SpringBootApplication(scanBasePackages = "org.example")
public class FinancialDriverApplication {

    public static void main(String[] args) {
        // Disable headless mode so Swing can create windows
        System.setProperty("java.awt.headless", "false");

        // Create SpringApplication instance
        SpringApplication app = new SpringApplication(FinancialDriverApplication.class);
        app.setHeadless(false);

        // Start Spring context - this will initialize all beans
        ConfigurableApplicationContext context = app.run(args);

        // Get the ServiceDispatcher bean from Spring context
        serviceDispatcher.ServiceDispatcher dispatcher = context.getBean(serviceDispatcher.ServiceDispatcher.class);

        // Set the spring context in the dispatcher
        dispatcher.setSpringContext(context);

        // Launch the Swing GUI on the Event Dispatch Thread
        SwingUtilities.invokeLater(() -> {
            dispatcher.startGui();
        });
    }
}

