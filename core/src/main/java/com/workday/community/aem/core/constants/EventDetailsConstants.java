package com.workday.community.aem.core.constants;

/**
 * Constants for event details data.
 */
public interface EventDetailsConstants {

  /**
   * Formatted date example "13:05".
   */
  String REQ_TIME_FORMAT = "HH:mm";

  /**
   * Formatted date example "Monday, Oct 09, 2023".
   */
  String REQ_DATE_FORMAT = "EEEE, MMM dd, YYYY";

  /**
   * The Constant DATE_TIME_FORMAT.
   */
  String DATE_TIME_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX";

  /**
   * Label for plural days.
   */
  String DAYS_LABEL = "Days";

  /**
   * Label for a single day.
   */
  String DAY_LABEL = "Day";

  /**
   * Label for plural hours.
   */
  String HOURS_LABEL = "Hours";

  /**
   * Label for a single hour.
   */
  String HOUR_LABEL = "Hour";

  /**
   * Label for plural minutes.
   */
  String MINUTES_LABEL = "Minutes";

  /**
   * Label for a single minute.
   */
  String MINUTE_LABEL = "Minute";

  /**
   * The number of minutes in an hour.
   */
  long MINUTES_IN_1_HOUR = 60;

  /**
   * The number of minutes in 8 hours.
   */
  long MINUTES_IN_8_HOURS = 480;
  /**
   * The number of minutes in 24 hours.
   */
  long MINUTES_IN_24_HOURS = 1440;

}
