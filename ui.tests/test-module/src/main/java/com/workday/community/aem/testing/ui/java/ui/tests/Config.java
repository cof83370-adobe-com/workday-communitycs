package com.workday.community.aem.testing.ui.java.ui.tests;

public class Config {
    // Selenium
    public static String SELENIUM_BROWSER = System.getProperty("SELENIUM_BROWSER", "chrome");
    public static String SELENIUM_BASE_URL = System.getProperty("SELENIUM_BASE_URL", "http://localhost:4444");

    // AEM Author
    public static String AEM_AUTHOR_URL = System.getProperty("AEM_AUTHOR_URL", "http://localhost:4502");
    public static String AEM_AUTHOR_USERNAME = System.getProperty("AEM_AUTHOR_USERNAME", "admin");
    public static String AEM_AUTHOR_PASSWORD = System.getProperty("AEM_AUTHOR_PASSWORD", "admin");

    // AEM Publish
    public static String AEM_PUBLISH_URL = System.getProperty("AEM_PUBLISH_URL", "http://localhost:4503");
    public static String AEM_PUBLISH_USERNAME = System.getProperty("AEM_PUBLISH_USERNAME", "admin");
    public static String AEM_PUBLISH_PASSWORD = System.getProperty("AEM_PUBLISH_PASSWORD", "admin");

    // Reports
    public static String REPORTS_PATH = System.getProperty("REPORTS_PATH", "/tmp/reports");
    public static String SCREENSHOTS_PATH = REPORTS_PATH + "/screenshots";

    // File uploads
    public static String UPLOAD_URL = System.getProperty("UPLOAD_URL", "");
    public static String SHARED_FOLDER = System.getProperty("SHARED_FOLDER", "");
}
