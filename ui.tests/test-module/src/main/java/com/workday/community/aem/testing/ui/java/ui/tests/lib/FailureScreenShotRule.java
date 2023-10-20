package com.workday.community.aem.testing.ui.java.ui.tests.lib;

import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;
import org.openqa.selenium.WebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FailureScreenShotRule implements TestRule {
  public static Logger LOGGER = LoggerFactory.getLogger(FailureScreenShotRule.class);
  protected static Commands commands;

  public FailureScreenShotRule(WebDriver driver) {
    commands = new Commands(driver);
  }

  @Override
  public Statement apply(Statement statement, Description description) {
    return new Statement() {
      @Override
      public void evaluate() throws Throwable {
        try {
          statement.evaluate();
        } catch (Throwable t) {
          try {
            String snapshotName = description.getMethodName();
            commands.snapshot(snapshotName);
          } catch (Exception e) {
            LOGGER.error("could not take snapshot " + e);
          }
          throw t;
        }
      }
    };
  }
}
