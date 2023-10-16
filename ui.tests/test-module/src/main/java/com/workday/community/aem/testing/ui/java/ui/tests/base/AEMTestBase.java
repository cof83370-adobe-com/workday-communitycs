package com.workday.community.aem.testing.ui.java.ui.tests.base;

import com.workday.community.aem.testing.ui.java.ui.tests.lib.Commands;
import com.workday.community.aem.testing.ui.java.ui.tests.Config;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.openqa.selenium.Platform;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.logging.LogType;
import org.openqa.selenium.logging.LoggingPreferences;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.logging.Level;

import static com.workday.community.aem.testing.ui.java.ui.tests.Config.SELENIUM_BROWSER;


/**
 * This base class will initialize the web driver instance used within the tests.
 */
public abstract class AEMTestBase {
    protected static WebDriver driver;
    protected static Commands commands;

    public static Logger LOGGER = LoggerFactory.getLogger(AEMTestBase.class);

    @BeforeClass
    public static void init() throws Exception {
        // Initialize remote web driver session
        String browser = SELENIUM_BROWSER;
        DesiredCapabilities dc = new DesiredCapabilities();
        // Enable browser logs
        LoggingPreferences logPrefs = new LoggingPreferences();
        logPrefs.enable(LogType.BROWSER, Level.INFO);

        dc.setBrowserName(browser);
        dc.setPlatform(Platform.LINUX);

        switch (browser) {
            case "chrome":
                ChromeOptions options = new ChromeOptions();
//                options.setHeadless(true);
                options.addArguments("--verbose", "--disable-gpu", "--no-sandbox", "--disable-dev-shm-usage");
                dc.setCapability(ChromeOptions.CAPABILITY, options);
                dc.setCapability("goog:loggingPrefs", logPrefs);
                break;
            case "firefox":
                FirefoxOptions ffOptions = new FirefoxOptions();
                ffOptions.addArguments("-headless");
                dc.setCapability(ChromeOptions.CAPABILITY, ffOptions);
                dc.setCapability("loggingPrefs", logPrefs);
                break;
        }
        URL webDriverUrl = new URL(Config.SELENIUM_BASE_URL + "/wd/hub");
        if (driver == null) {
            driver = new RemoteWebDriver(webDriverUrl, dc);
            commands = new Commands(driver);
        }
    }

    @AfterClass
    public static void cleanup() {
        if (driver != null) {
            driver.quit();
            driver = null;
        }
    }
}