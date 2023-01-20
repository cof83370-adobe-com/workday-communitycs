/**
 * 
 */
package com.workday.community.aem.migration.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TimeZone;
import java.util.stream.Collectors;

import javax.jcr.Node;
import javax.jcr.Session;

import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.day.cq.tagging.Tag;
import com.day.cq.wcm.api.Page;
import com.day.cq.wcm.api.PageManager;
import com.workday.community.aem.migration.constants.MigrationConstants;
import com.workday.community.aem.migration.models.PageNameBean;

/**
 * The Class WokdayUtils.
 *
 * @author pepalla
 */
public class MigrationUtils {

	/** The Constant log. */
	private static final Logger log = LoggerFactory.getLogger(MigrationUtils.class);

	/** The Constant ISO8601DATEFORMAT. */
	static final String ISO8601DATEFORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSSZ";

	/** The Constant JCR_SQL2. */
	private static final String JCR_SQL2 = "JCR-SQL2";

	/** The aem page name. */
	private static String aemPageName = StringUtils.EMPTY;

	/** The Constant dateFmt. */
	private static final SimpleDateFormat dateFmt = new SimpleDateFormat(ISO8601DATEFORMAT);

	/**
	 * Instantiates a new migration utils.
	 */
	private MigrationUtils() {
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

	/**
	 * Days between.
	 *
	 * @param day1 the day 1
	 * @param day2 the day 2
	 * @return the int
	 */
	public static int daysBetween(Calendar day1, Calendar day2) {
		Calendar dayOne = (Calendar) day1.clone(),
				dayTwo = (Calendar) day2.clone();

		if (dayOne.get(Calendar.YEAR) == dayTwo.get(Calendar.YEAR)) {
			return Math.abs(dayOne.get(Calendar.DAY_OF_YEAR) - dayTwo.get(Calendar.DAY_OF_YEAR));
		} else {
			if (dayTwo.get(Calendar.YEAR) > dayOne.get(Calendar.YEAR)) {
				// swap them
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
			return extraDays - dayTwo.get(Calendar.DAY_OF_YEAR) + dayOneOriginalYearDays;
		}
	}

	/**
	 * Gets the aem page name.
	 *
	 * @param list the list
	 * @param nodeId the node id
	 * @return the aem page name
	 */
	public static String getAemPageName(List<PageNameBean> list, final String nodeId) {
		list.stream().forEach((item) -> {
			if (item.getNodeId().equalsIgnoreCase(nodeId)) {
				String[] pathArray = item.getTitle().split("/");
				if (pathArray.length > 1) {
					aemPageName = pathArray[pathArray.length - 1].trim().replace(".html", StringUtils.EMPTY);
				}
				if (pathArray.length == 1) {
					aemPageName = item.getTitle().trim();
				}
				aemPageName = aemPageName.replaceAll("\\s+", "-");
			}
		});
		log.debug("AEM page name for nodeid:: {} is:: {}", nodeId, aemPageName);
		return aemPageName;
	}

	/**
	 * Gets the page created.
	 *
	 * @param resourceResolver the resource resolver
	 * @param paramsMap the params map
	 * @param aemPageTitle the aem page title
	 * @return the page created
	 */
	public static Page getPageCreated(ResourceResolver resourceResolver, final Map<String, String> paramsMap,
			final String aemPageTitle) {
		Page prodPage = null;
		try {
			PageManager pageManager = resourceResolver.adaptTo(PageManager.class);
			prodPage = pageManager.create(paramsMap.get(MigrationConstants.PARENT_PAGE_PATH_PARAM),
					aemPageName, paramsMap.get(MigrationConstants.TEMPLATE_PARAM), aemPageTitle);
		} catch (Exception exec) {
			log.error("Exception occurred while creating page in getPageCreated::{}", exec.getMessage());
		}
		return prodPage;
	}

	/**
	 * Mount tag page props.
	 *
	 * @param jcrNode the jcr node
	 * @param key the key
	 * @param givenTagList the given tag list
	 */
	public static void mountTagPageProps(Node jcrNode, final String key, List<String> givenTagList) {
		try {
			if (!givenTagList.isEmpty()) {
				jcrNode.setProperty(key, givenTagList.stream().toArray(String[]::new));
			}
		} catch (Exception exec) {
			log.error("Exception occurred while adding tags as Page props:{}", exec.getMessage());
		}
	}

	/**
	 * Gets the tags for given inputs.
	 *
	 * @param resourceResolver the resource resolver
	 * @param tagFinderEnum the tag finder enum
	 * @param tagTypeValue the tag type value
	 * @return the tags for given inputs
	 */
	public static List<String> getTagsForGivenInputs(ResourceResolver resourceResolver, TagFinderEnum tagFinderEnum,
			final String tagTypeValue) {
		return Optional.ofNullable(tagFinderUtil(resourceResolver, tagFinderEnum.getValue(), tagTypeValue))
				.orElse(new ArrayList<>());
	}

	
	/**
	 * Tag finder util.
	 *
	 * @param resourceResolver the resource resolver
	 * @param tagRootPath the tag root path
	 * @param tagTitle the tag title
	 * @return the list
	 */
	private static List<String> tagFinderUtil(ResourceResolver resourceResolver, final String tagRootPath,
			final String tagTitle) {
		Iterator<Resource> tagResources = doQueryForTag(resourceResolver, tagRootPath, tagTitle);
		Set<String> tagsSet = new HashSet<>();
		if (null != tagResources) {
			while (tagResources.hasNext()) {
				Resource artcileResource = tagResources.next();
				if (null != artcileResource) {
					Tag tag = artcileResource.adaptTo(Tag.class);
					tagsSet.add(tag.getTagID());
				}
			}
		}
		return tagsSet.stream().collect(Collectors.toList());
	}

	/**
	 * SELECT * FROM [cq:Tag] AS tag
	 * WHERE ISDESCENDANTNODE(tag, "/content/cq:tags/event") AND
	 * [sling:resourceType] = 'cq/tagging/components/tag' AND ([jcr:title] =
	 * 'Rising'
	 * OR [jcr:title] = 'Webinar').
	 *
	 * @param resourceResolver the resource resolver
	 * @param searchPath       the search path
	 * @param tagTitle         the tag title
	 * @return the iterator
	 */
	private static Iterator<Resource> doQueryForTag(ResourceResolver resourceResolver, String searchPath,
			String tagTitle) {
		String partialSqlStmt = "SELECT * FROM [cq:Tag] AS tag WHERE ISDESCENDANTNODE(tag, \"" + searchPath
				+ "\") AND [sling:resourceType] = 'cq/tagging/components/tag' AND ";
		String[] diffTagsList = tagTitle.split(",");
		StringBuilder sbr = new StringBuilder();
		for (int index = 0; index < diffTagsList.length; index++) {
			if (index == 0 && StringUtils.isNotBlank(diffTagsList[index])) {
				sbr.append("[jcr:title] = '" + diffTagsList[index].trim() + "'");
			} else if (StringUtils.isNotBlank(diffTagsList[index])) {
				sbr.append(" OR [jcr:title] = '" + diffTagsList[index].trim() + "'");
			}
		}
		String sqlStmt = String.format("%s%s%s%s", partialSqlStmt, "(", sbr.toString(), ")");
		log.debug("Query sql_stmt: {}", sqlStmt);
		return resourceResolver.findResources(sqlStmt, JCR_SQL2);
	}

	/**
	 * Saving to repo.
	 *
	 * @param session the session
	 */
	public static void savingToRepo(Session session) {
		try {
			session.save();
			session.refresh(true);
		} catch (Exception exec) {
			log.error("Exception occurred while saving to repo::{}", exec.getMessage());
		}
	}
}
