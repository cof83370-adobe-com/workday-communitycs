package com.workday.community.aem.testing.ui.java.ui.tests;

import java.util.HashMap;
import java.util.Map;

public class Config {
    // Environment
    private static final Map<String, String> authorEnvLoginTitleMap = new HashMap<>();
    private static final Map<String, String> publishEnvLoginTitleMap = new HashMap<>();

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

    public static String getAuthorEnvLoginTitle(String url) {
        return getTitle(url, authorEnvLoginTitleMap, false);
    }

    public static String getPublishEnvLoginTitle(String url) {
        return getTitle(url, publishEnvLoginTitleMap, true);
    }

    private static String getTitle(String url, Map<String, String> authorEnvLoginTitleMap, boolean isPublish) {
        if (authorEnvLoginTitleMap.isEmpty()) {
            if (isPublish) {
                authorEnvLoginTitleMap.put("https://dev-content.workday.com", "WorkdaySandBox - Sign In");
                authorEnvLoginTitleMap.put("https://qa-content.workday.com", "WorkdaySandBox - Sign In");
                authorEnvLoginTitleMap.put("https://stage-content.workday.com", "WorkdaySandBox - Sign In");
                authorEnvLoginTitleMap.put("https://beta-content.workday.com", "Workday Prod - Sign In");
            } else {
                return "Adobe ID";
            }
        }

        if (url != null && url.endsWith("/")) {
            url = url.substring(0, url.length() - 1);
            return authorEnvLoginTitleMap.get(url);
        }

        return "";
    }
}
