/**
 * 
 */
package com.workday.community.aem.core.models;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Date;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.models.annotations.DefaultInjectionStrategy;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.injectorspecific.ValueMapValue;

import com.day.cq.wcm.api.Page;

/**
 * The Class EventDetailsModel.
 *
 * @author pepalla
 */
@Model(adaptables = { Resource.class,
		SlingHttpServletRequest.class }, defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL)
public class EventDetailsModel {

	/** The event type. */
	@ValueMapValue
	private String eventType;

	/** The location. */
	@ValueMapValue
	private String location;

	/** The host. */
	@ValueMapValue
	private String host;

	/** The current page. */
	@Inject
	private Page currentPage;

	/** The length. */
	private long length;

	private String dateFormat;

	private String timeFormat;

	/**
	 * Inits the.
	 * 
	 * @throws ParseException
	 */
	@PostConstruct
	protected void init() throws ParseException {
		ValueMap map = currentPage.getProperties();
		String startDateStr = map.get("startDate", String.class);
		String endDateStr = map.get("endDate", String.class);
		if (StringUtils.isNotBlank(startDateStr) && StringUtils.isNotBlank(endDateStr)) {
			calculateRequired(startDateStr, endDateStr);
		}
	}

	/**
	 * Calculate required.
	 *
	 * @param eventStartDate  the event start date
	 * @param eventEndDateStr the event end date str
	 * @throws ParseException the parse exception
	 */
	private void calculateRequired(final String eventStartDate, final String eventEndDateStr) throws ParseException {
		DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
		Date formattedStartDate = formatter.parse(eventStartDate);
		LocalDateTime startDateAndTime = formattedStartDate.toInstant().atZone(ZoneId.systemDefault())
				.toLocalDateTime();

		dateFormat = DateTimeFormatter.ofPattern("EEEE, MMM dd, YYYY").format(startDateAndTime);
		timeFormat = DateTimeFormatter.ofPattern("HH:mm").format(startDateAndTime);

		Date formattedEndDate = formatter.parse(eventEndDateStr);
		LocalDateTime endDateAndTime = formattedEndDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
		length = ChronoUnit.DAYS.between(startDateAndTime, endDateAndTime);
	}

	/**
	 * Gets the length.
	 *
	 * @return the length
	 */
	public long getLength() {
		return length;
	}

	/**
	 * Sets the length.
	 *
	 * @param length the new length
	 */
	public void setLength(long length) {
		this.length = length;
	}

	/**
	 * Gets the event type.
	 *
	 * @return the event type
	 */
	public String getEventType() {
		return eventType;
	}

	/**
	 * Gets the location.
	 *
	 * @return the location
	 */
	public String getLocation() {
		return location;
	}

	/**
	 * Gets the host.
	 *
	 * @return the host
	 */
	public String getHost() {
		return host;
	}

	/**
	 * Checks if is configured.
	 *
	 * @return true, if is configured
	 */
	public boolean isConfigured() {
		return eventType != null && location != null && host != null;
	}

	public String getDateFormat() {
		return dateFormat;
	}

	public String getTimeFormat() {
		return timeFormat;
	}

	@Override
	public String toString() {
		return "EventDetailsModel [eventType=" + eventType + ", location=" + location + ", host=" + host
				+ ", currentPage=" + currentPage + ", length=" + length + ", dateFormat=" + dateFormat + ", timeFormat="
				+ timeFormat + "]";
	}

}
