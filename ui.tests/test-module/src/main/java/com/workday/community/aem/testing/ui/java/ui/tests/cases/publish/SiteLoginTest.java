package com.workday.community.aem.testing.ui.java.ui.tests.cases.publish;

import com.workday.community.aem.testing.ui.java.ui.tests.Config;
import com.workday.community.aem.testing.ui.java.ui.tests.base.AEMTestBase;
import com.workday.community.aem.testing.ui.java.ui.tests.lib.BrowserLogsDumpRule;
import com.workday.community.aem.testing.ui.java.ui.tests.lib.FailureScreenShotRule;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SiteLoginTest extends AEMTestBase {
    private static final Logger LOGGER = LoggerFactory.getLogger(SiteLoginTest.class);

    /**
     * Using the following rule will create a screenshot in case of tests failure
     */
    @Rule
    public FailureScreenShotRule failure = new FailureScreenShotRule(driver);

    /**
     * Adds browser logs to the test execution reports for troubleshooting
     */
    @Rule
    public BrowserLogsDumpRule browserLogs =  new BrowserLogsDumpRule(driver);

    @Before
    public void forceLogout() {
        LOGGER.info("The Author URL: {}", Config.AEM_PUBLISH_URL);
        // End any existing user session
//        commands.forceLogout(Config.AEM_PUBLISH_URL);
    }

    @Test
    public void checkLoginForm() {
        LOGGER.info("checkLoginForm test");
//        LOGGER.info("Navigating to AEM publish site root");
//        driver.navigate().to(Config.AEM_PUBLISH_URL + "/");
//        LOGGER.info("Finding elements in login form");
//        driver.findElement(By.cssSelector("#username"));
//        driver.findElement(By.cssSelector("#password"));
//        driver.findElement(By.cssSelector("form [type=\"submit\"]"));
    }

    @Test
    public void checkSuccessfulLogin() {
        LOGGER.info("checkSuccessfulLogin test");
//        driver.navigate().to(Config.AEM_PUBLISH_URL + "/");
//
//        commands.aemLogin(Config.AEM_AUTHOR_USERNAME, Config.AEM_AUTHOR_PASSWORD);
//
//        driver.findElement(By.cssSelector("coral-shell"));
//        driver.findElement(By.cssSelector("coral-shell-header"));
//        Assert.assertEquals("AEM Start", driver.getTitle());
    }
}
