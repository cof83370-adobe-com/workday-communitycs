package com.workday.community.aem.testing.ui.java.ui.tests.lib;

import org.openqa.selenium.*;
import org.openqa.selenium.support.events.WebDriverListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;

// deals with dialogs that might appear during tests
public class DialogListener implements WebDriverListener {
    public static Logger LOGGER = LoggerFactory.getLogger(DialogListener.class);

    public DialogListener() {
    }

    @Override
    public void beforeAnyWebDriverCall(WebDriver driver, Method method, Object[] args) {
        handleOnBoardingDialog(driver);
    }

    // closes onboarding pop up dialogs that may appear during tests
    private void handleOnBoardingDialog(WebDriver driver){
        try {
            WebElement overlay = driver.findElement(By.cssSelector("coral-overlay[class*='onboarding']"));
            overlay.sendKeys(Keys.ESCAPE);
        } catch (org.openqa.selenium.NoSuchElementException e) {
            LOGGER.debug("No onboarding dialog present");
        }
    }
}
