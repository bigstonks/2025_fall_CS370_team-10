package org.example.userAccountController.src;

/**
 * Handles user authorization levels and controls visibility of information
 * based on user roles.
 *
 * Authorization Levels:
 * - "admin": Full access, can see all information including data flow details
 * - "user": Standard access, data flow information is hidden
 */
public class loginAuthorization {

    // Authorization level constants
    public static final String ROLE_ADMIN = "admin";
    public static final String ROLE_USER = "user";

    // Current user's authorization level
    private String authorizationLevel;

    /**
     * Default constructor - sets authorization to standard user.
     */
    public loginAuthorization() {
        this.authorizationLevel = ROLE_USER;
    }

    /**
     * Constructor with authorization level.
     * @param authorizationLevel The authorization level (admin or user)
     */
    public loginAuthorization(String authorizationLevel) {
        setAuthorizationLevel(authorizationLevel);
    }

    /**
     * Gets the current authorization level.
     * @return The authorization level
     */
    public String getAuthorizationLevel() {
        return authorizationLevel;
    }

    /**
     * Sets the authorization level.
     * Defaults to "user" if null or empty.
     * @param authorizationLevel The authorization level to set
     */
    public void setAuthorizationLevel(String authorizationLevel) {
        if (authorizationLevel == null || authorizationLevel.trim().isEmpty()) {
            this.authorizationLevel = ROLE_USER;
        } else {
            this.authorizationLevel = authorizationLevel.trim().toLowerCase();
        }
    }

    /**
     * Checks if the current user is an admin.
     * @return true if user has admin authorization
     */
    public boolean isAdmin() {
        return ROLE_ADMIN.equalsIgnoreCase(authorizationLevel);
    }

    /**
     * Checks if the current user is a standard user.
     * @return true if user has standard user authorization
     */
    public boolean isUser() {
        return ROLE_USER.equalsIgnoreCase(authorizationLevel) || !isAdmin();
    }

    /**
     * Checks if data flow information should be visible.
     * Only admins can see data flow details.
     * @return true if data flow info should be shown
     */
    public boolean canViewDataFlow() {
        return isAdmin();
    }

    /**
     * Checks if debug/technical information should be visible.
     * Only admins can see debug info.
     * @return true if debug info should be shown
     */
    public boolean canViewDebugInfo() {
        return isAdmin();
    }

    /**
     * Checks if MVC architecture details should be visible.
     * Only admins can see MVC details.
     * @return true if MVC details should be shown
     */
    public boolean canViewMvcDetails() {
        return isAdmin();
    }

    /**
     * Filters text to remove data flow information for non-admin users.
     * @param text The text to filter
     * @return Filtered text (data flow sections removed for non-admins)
     */
    public String filterDataFlowText(String text) {
        if (isAdmin() || text == null) {
            return text;
        }

        // Remove data flow related sections
        StringBuilder filtered = new StringBuilder();
        String[] lines = text.split("\n");
        boolean skipSection = false;

        for (String line : lines) {
            String lowerLine = line.toLowerCase();

            // Check if we're entering a data flow section
            if (lowerLine.contains("data flow") ||
                lowerLine.contains("mvc") ||
                lowerLine.contains("→ servicedispatcher") ||
                lowerLine.contains("→ reportgenerator") ||
                lowerLine.contains("→ database") ||
                lowerLine.contains("→ dao") ||
                lowerLine.contains("gui →") ||
                lowerLine.contains("financeappframe (view)") ||
                lowerLine.contains("(controller)") ||
                lowerLine.contains("(service)") ||
                lowerLine.contains("(data access)")) {
                skipSection = true;
                continue;
            }

            // Check if we're exiting the section (next major section header)
            if (skipSection && (line.startsWith("═") || line.startsWith("─") && !lowerLine.contains("data flow"))) {
                // Check if this is a new section header (not just a separator within data flow)
                if (line.contains("═") ||
                    (lines.length > 0 && !lowerLine.contains("→"))) {
                    skipSection = false;
                }
            }

            if (!skipSection) {
                // Also filter out inline method references
                if (!lowerLine.contains("method:") &&
                    !lowerLine.contains("servicedispatcher.") &&
                    !lowerLine.contains("gui →") &&
                    !lowerLine.contains("→ database")) {
                    filtered.append(line).append("\n");
                }
            }
        }

        return filtered.toString().trim();
    }

    /**
     * Returns a simple filtered version that removes all technical details.
     * @param text The text to filter
     * @return Clean text for standard users
     */
    public String filterForUser(String text) {
        if (isAdmin() || text == null) {
            return text;
        }

        StringBuilder filtered = new StringBuilder();
        String[] lines = text.split("\n");

        for (String line : lines) {
            String lowerLine = line.toLowerCase();

            // Skip technical/data flow lines
            if (lowerLine.contains("data flow") ||
                lowerLine.contains("mvc") ||
                lowerLine.contains("method:") ||
                lowerLine.contains("→") ||
                lowerLine.contains("servicedispatcher") ||
                lowerLine.contains("reportgenerator") ||
                lowerLine.contains("deliverycalculator") ||
                lowerLine.contains("reportdao") ||
                lowerLine.contains("(view)") ||
                lowerLine.contains("(controller)") ||
                lowerLine.contains("(service)") ||
                lowerLine.contains("(data access)") ||
                lowerLine.contains("sqlite database") ||
                lowerLine.contains("gui (")) {
                continue;
            }

            filtered.append(line).append("\n");
        }

        // Clean up extra blank lines
        String result = filtered.toString();
        while (result.contains("\n\n\n")) {
            result = result.replace("\n\n\n", "\n\n");
        }

        return result.trim();
    }

    @Override
    public String toString() {
        return "loginAuthorization{" +
                "authorizationLevel='" + authorizationLevel + '\'' +
                ", isAdmin=" + isAdmin() +
                '}';
    }
}
