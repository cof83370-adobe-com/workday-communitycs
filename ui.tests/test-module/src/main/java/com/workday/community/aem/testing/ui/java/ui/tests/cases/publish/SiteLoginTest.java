package com.workday.community.aem.testing.ui.java.ui.tests.cases.publish;

import com.workday.community.aem.testing.ui.java.ui.tests.Config;
import com.workday.community.aem.testing.ui.java.ui.tests.base.AEMTestBase;
import com.workday.community.aem.testing.ui.java.ui.tests.lib.BrowserLogsDumpRule;
import com.workday.community.aem.testing.ui.java.ui.tests.lib.FailureScreenShotRule;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

public class SiteLoginTest extends AEMTestBase {
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
        // End any existing user session
        commands.forceLogout(Config.AEM_PUBLISH_URL);
    }

    @Test
    public void checkLoginForm() {
//        LOGGER.info("Navigating to AEM publish site root");
//        driver.navigate().to(Config.AEM_PUBLISH_URL + "/");
//        LOGGER.info("Finding elements in login form");
//        driver.findElement(By.cssSelector("#username"));
//        driver.findElement(By.cssSelector("#password"));
//        driver.findElement(By.cssSelector("form [type=\"submit\"]"));
    }

    @Test
    public void checkSuccessfulLogin() {
//        driver.navigate().to(Config.AEM_PUBLISH_URL + "/");
//
//        commands.aemLogin(Config.AEM_AUTHOR_USERNAME, Config.AEM_AUTHOR_PASSWORD);
//
//        driver.findElement(By.cssSelector("coral-shell"));
//        driver.findElement(By.cssSelector("coral-shell-header"));
//        Assert.assertEquals("AEM Start", driver.getTitle());
    }
}
