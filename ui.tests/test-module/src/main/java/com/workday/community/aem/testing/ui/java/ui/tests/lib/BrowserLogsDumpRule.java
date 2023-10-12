package com.workday.community.aem.testing.ui.java.ui.tests.lib;

import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.logging.LogEntries;
import org.openqa.selenium.logging.LogEntry;
import org.openqa.selenium.logging.LogType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;

public class BrowserLogsDumpRule implements TestRule {
  private static final Logger LOGGER = LoggerFactory.getLogger(BrowserLogsDumpRule.class);

  private final WebDriver driver;

  public BrowserLogsDumpRule(WebDriver driver) {
    this.driver = driver;
  }

  @Override
  public Statement apply(Statement base, Description description) {
    return new Statement() {
      @Override
      public void evaluate() throws Throwable {
        try {
          // clear logs
          driver.manage().logs().get(LogType.BROWSER);
          base.evaluate();
        } finally {
          dumpBrowserLogs();
        }
      }
    };
  }

  /**
   * Fetches browser logs and prints them in stdout
   */
  public void dumpBrowserLogs() {
    LogEntries logEntries = driver.manage().logs().get(LogType.BROWSER);
    if (logEntries.iterator().hasNext()) {
      LOGGER.info("\n\n***** Browser logs *****");
    }
    for (LogEntry entry : logEntries) {
      LOGGER.info(new Date(entry.getTimestamp()) + " " + entry.getLevel() + " " + entry.getMessage());
    }
  }
}
