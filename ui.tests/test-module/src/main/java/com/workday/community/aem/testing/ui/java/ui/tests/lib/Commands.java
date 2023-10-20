package com.workday.community.aem.testing.ui.java.ui.tests.lib;

import com.workday.community.aem.testing.ui.java.ui.tests.Config;
import org.apache.commons.io.FileUtils;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.Date;

import static com.workday.community.aem.testing.ui.java.ui.tests.base.AEMTestBase.LOGGER;

/**
 * Helper class containing a pre-defined set of functions for UI testing with Adobe Experience Manager.
 */
public class Commands {
    protected static WebDriver driver;

    public Commands(WebDriver driver) {
        Commands.driver = driver;
    }

    public void forceLogout(String url) {
        driver.navigate().to(url + "/");
        String signTitle = Config.getPublishEnvLoginTitle(url);
        if (signTitle != null && !signTitle.equals(driver.getTitle())) {
            LOGGER.info("Need to log out");
            driver.navigate().to(url + "/bin/user/logout");

            //Re-visit the home.
            driver.navigate().to(url + "/");
        }
    }

    public void aemLogin(String username, String password) {
        if (driver.findElements(By.cssSelector("[class*=\"Accordion\"]")).size() > 0) {
            // Check presence of local sign-in Accordion
            try {
                driver.findElement(By.cssSelector("#username")).click();
                driver.findElement(By.cssSelector("#password")).click();
            }
            // Form field not interactable, not visible
            // Need to open the Accordion
            catch (Exception e) {
                driver.findElement(By.cssSelector("[class*=\"Accordion\"] button")).click();

                try {
                    Thread.sleep(500);
                } catch (InterruptedException ex) {
                   LOGGER.error("AEM login thread fails");
                }
            }
        }

        driver.findElement(By.cssSelector("#username")).sendKeys(username);
        driver.findElement(By.cssSelector("#password")).sendKeys(password);

        driver.findElement(By.cssSelector("form [type=\"submit\"]")).click();

        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(50));

        wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("coral-shell-content")));
    }

    public void snapshot(String fileName) throws IOException {
        File capture = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd-HHmmss.SSS");
        String timestamp = dateFormat.format(new Date());

        fileName = Config.SCREENSHOTS_PATH + "/" +  fileName + "-" + timestamp + ".png";
        LOGGER.debug("copying to " + fileName);
        File targetFile = new File(fileName);
        FileUtils.copyFile(capture, targetFile);
    }
}
