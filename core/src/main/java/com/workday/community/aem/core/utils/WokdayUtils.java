/**
 * 
 */
package com.workday.community.aem.core.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;

import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Class WokdayUtils.
 *
 * @author pepalla
 */
public class WokdayUtils {

	/** The Constant log. */
	private static final Logger log = LoggerFactory.getLogger(WokdayUtils.class);

	/** The Constant ISO8601DATEFORMAT. */
	 static final String ISO8601DATEFORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSSZ";
	 private WokdayUtils(){
		log.info("Initialized");
	 }

	/**
	 * Gets the date string from epoch.
	 *
	 * @param epoch the epoch
	 * @return the date string from epoch
	 */
	public static String getDateStringFromEpoch(long epoch) {
		try {
			java.util.Date time = new java.util.Date(epoch * 1000);
			log.debug("epoch::{}", time);
			SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy");
			return dateFormat.format(time);
		} catch (Exception e) {
			log.error("Exception occurred at getDateFromEpoch::{}", e.getMessage());
		}
		return StringUtils.EMPTY;
	}

	/**
	 * Convert str to aem cal instance.
	 *
	 * @param dateStr the date str
	 * @param format  the format
	 * @return the calendar
	 */
	public static Calendar convertStrToAemCalInstance(String dateStr, String format) {
		try {
			Date date = new SimpleDateFormat(format).parse(dateStr);
			return getCalendarFromISO(formatDate(date));
		} catch (ParseException e) {
			log.error("ParseException occurred at convertStringToDate method::{}", e.getMessage());
		}
		return null;
	}

	/** The Constant dateFmt. */
	private static final SimpleDateFormat dateFmt = new SimpleDateFormat(ISO8601DATEFORMAT);

	/**
	 * Format a date as text.
	 *
	 * @param dat the dat
	 * @return the string
	 */
	public static String formatDate(Date dat) {
		return dateFmt.format(dat);
	}

	/**
	 * Gets the calendar from ISO.
	 *
	 * @param datestring the datestring
	 * @return the calendar from ISO
	 */
	public static Calendar getCalendarFromISO(String datestring) {
		Calendar calendar = Calendar.getInstance(TimeZone.getDefault(), Locale.getDefault());
		SimpleDateFormat dateformat = new SimpleDateFormat(ISO8601DATEFORMAT, Locale.getDefault());
		try {
			Date date = dateformat.parse(datestring);
			date.setHours(date.getHours() - 1);
			calendar.setTime(date);
		} catch (ParseException e) {
			log.error("ParseException occurred at getCalendarFromISO method::{}", e.getMessage());
		}
		return calendar;
	}

	public static int daysBetween(Calendar day1, Calendar day2){
	    Calendar dayOne = (Calendar) day1.clone(),
	            dayTwo = (Calendar) day2.clone();

	    if (dayOne.get(Calendar.YEAR) == dayTwo.get(Calendar.YEAR)) {
	        return Math.abs(dayOne.get(Calendar.DAY_OF_YEAR) - dayTwo.get(Calendar.DAY_OF_YEAR));
	    } else {
	        if (dayTwo.get(Calendar.YEAR) > dayOne.get(Calendar.YEAR)) {
	            //swap them
	            Calendar temp = dayOne;
	            dayOne = dayTwo;
	            dayTwo = temp;
	        }
	        int extraDays = 0;

	        int dayOneOriginalYearDays = dayOne.get(Calendar.DAY_OF_YEAR);

	        while (dayOne.get(Calendar.YEAR) > dayTwo.get(Calendar.YEAR)) {
	            dayOne.add(Calendar.YEAR, -1);
	            // getActualMaximum() important for leap years
	            extraDays += dayOne.getActualMaximum(Calendar.DAY_OF_YEAR);
	        }

	        return extraDays - dayTwo.get(Calendar.DAY_OF_YEAR) + dayOneOriginalYearDays ;
	    }
	}
}
