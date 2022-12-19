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
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.models.annotations.DefaultInjectionStrategy;
import org.apache.sling.models.annotations.Model;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.day.cq.tagging.Tag;
import com.day.cq.tagging.TagManager;
import com.day.cq.wcm.api.Page;
import com.workday.community.aem.core.constants.GlobalConstants;

/**
 * The Class EventDetailsModel.
 *
 * @author pepalla
 */
@Model(adaptables = { Resource.class,
		SlingHttpServletRequest.class }, defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL)
public class EventDetailsModel {

	/** The logger. */
	private final Logger logger = LoggerFactory.getLogger(this.getClass().getName());

	private List<String> eventFormat = new ArrayList<>();

	/** The location. */
	private String eventLocation;

	/** The host. */
	private String eventHost;

	/** The length. */
	private long length;

	private String daysLabel;

	private String dateFormat;

	private String timeFormat;
	/** The current page. */
	@Inject
	private Page currentPage;

	@Inject
	private ResourceResolver resolver;

	/**
	 * Inits the.
	 * 
	 * @throws ParseException
	 */
	@PostConstruct
	protected void init() throws ParseException {
		final ValueMap map = currentPage.getProperties();
		String startDateStr = map.get("startDate", String.class);
		String endDateStr = map.get("endDate", String.class);
		if (StringUtils.isNotBlank(startDateStr) && StringUtils.isNotBlank(endDateStr)) {
			calculateRequired(startDateStr, endDateStr);
		}
		eventLocation = map.get("eventLocation", String.class);
		eventHost = map.get("eventHost", String.class);
		getEventFormatList(map);
	}

	private List<String> getEventFormatList(final ValueMap map) {
		String[] eventFormatTags = map.get("eventFormat", String[].class);
		try {
			if (null != eventFormatTags && eventFormatTags.length > 0) {
				TagManager tagManager = resolver.adaptTo(TagManager.class);
				for (String eachTag : eventFormatTags) {
					Tag tag = tagManager.resolve(eachTag);
					eventFormat.add(tag.getTitle());
				}
			}
		} catch (Exception exec) {
			logger.error("Exception occurred at getEventFormatList method of EventDetailsModel:{} ", exec.getMessage());
		}
		return eventFormat;

	}

	/**
	 * Calculate required.
	 *
	 * @param eventStartDate  the event start date
	 * @param eventEndDateStr the event end date str
	 * @throws ParseException the parse exception
	 */
	private void calculateRequired(final String eventStartDate, final String eventEndDateStr) throws ParseException {
		DateFormat formatter = new SimpleDateFormat(GlobalConstants.EventDetailsConstants.DATE_TIME_FORMAT);
		Date formattedStartDate = formatter.parse(eventStartDate);
		LocalDateTime startDateAndTime = formattedStartDate.toInstant().atZone(ZoneId.systemDefault())
				.toLocalDateTime();

		dateFormat = DateTimeFormatter.ofPattern(GlobalConstants.EventDetailsConstants.REQ_DATE_FORMAT)
				.format(startDateAndTime);
		timeFormat = DateTimeFormatter.ofPattern(GlobalConstants.EventDetailsConstants.REQ_TIME_FORMAT)
				.format(startDateAndTime);

		Date formattedEndDate = formatter.parse(eventEndDateStr);
		LocalDateTime endDateAndTime = formattedEndDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
		length = ChronoUnit.DAYS.between(startDateAndTime, endDateAndTime);
		daysLabel = (length <= 1) ? GlobalConstants.EventDetailsConstants.DAY_LABEL
				: GlobalConstants.EventDetailsConstants.DAYS_LABEL;
	}

	public String getDaysLabel() {
		return daysLabel;
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
	 * Checks if is configured.
	 *
	 * @return true, if is configured
	 */
	public boolean isConfigured() {
		return eventFormat != null && eventLocation != null && eventHost != null;
	}

	public String getDateFormat() {
		return dateFormat;
	}

	public String getTimeFormat() {
		return timeFormat;
	}

	public List<String> getEventFormat() {
		return eventFormat;
	}

	public void setEventFormat(List<String> eventFormat) {
		this.eventFormat = eventFormat;
	}

	public String getEventLocation() {
		return eventLocation;
	}

	public void setEventLocation(String evenLlocation) {
		this.eventLocation = evenLlocation;
	}

	public String getEventHost() {
		return eventHost;
	}

	public void setEventHost(String eventHost) {
		this.eventHost = eventHost;
	}

	public void setDateTimeFormat(String dateFormat) {
		this.dateFormat = dateFormat;
	}

	public void setTimeFormat(String timeFormat) {
		this.timeFormat = timeFormat;
	}

	@Override
	public String toString() {
		return "EventDetailsModel [eventFormat=" + eventFormat + ", eventLocation=" + eventLocation + ", eventHost="
				+ eventHost + ", length=" + length + ", daysLabel=" + daysLabel + ", dateFormat=" + dateFormat
				+ ", timeFormat=" + timeFormat + "]";
	}
}
