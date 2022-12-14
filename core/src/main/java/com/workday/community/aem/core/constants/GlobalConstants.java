package com.workday.community.aem.core.constants;

/**
 * The Interface GlobalConstants.
 * 
 * @author pepalla
 */
public interface GlobalConstants {

	/** The Constant TEMPLATE_PARAM. */
	public static final String TEMPLATE_PARAM = "template";

	/** The Constant SOURC_FILE_PARAM. */
	public static final String SOURC_FILE_PARAM = "source";

	/** The Constant PARENT_PAGE_PATH_PARAM. */
	public static final String PARENT_PAGE_PATH_PARAM = "pagePath";

	/** The Constant AEM_SLING_RESOURCE_TYPE_PROP. */
	public static final String AEM_SLING_RESOURCE_TYPE_PROP = "sling:resourceType";

	/** The Constant AEM_CAL_INSTANCE_FORMAT. */
	public static final String AEM_CAL_INSTANCE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss";

	/** The Constant TITLE_COMP_SLING_RESOURCE. */
	public static final String TITLE_COMP_SLING_RESOURCE = "community/components/title";

	public static final String TEXT_COMP_SLING_RESOURCE = "community/components/text";

	/** The Constant BREADCRUMB_COMP_SLING_RESOURCE. */
	public static final String BUTTON_COMP_SLING_RESOURCE = "community/components/button";

	/** The Constant BUTTON_COMP_NODE_NAME. */
	public static final String BUTTON_COMP_NODE_NAME = "button";

	/** The Constant JCR_CONTENT_NODE. */
	public static final String JCR_CONTENT_NODE = "jcr:content";

	/** The Constant JCR_DATA_NODE. */
	public static final String JCR_DATA_NODE = "jcr:data";

	/** The Constant JCR_TITLE_PROP. */
	public static final String JCR_TITLE_PROP = "jcr:title";

	/** The Constant LINK_TARGET_PROP. */
	public static final String LINK_TARGET_PROP = "linkTarget";

	/** The Constant LINK_URL_PROP. */
	public static final String LINK_URL_PROP = "linkURL";

	/** The Constant TEXT_UNDERSCORE_SELF. */
	public static final String TEXT_UNDERSCORE_SELF = "_self";
	public static final String EVENT_PAGE_NAMES_FINDER_JSON = "/content/dam/community/page_names_finder.json";

	public interface TagRootPaths {
		public static final String REGION_AND_COUNTRY_TAG_ROOT = "/content/cq:tags/region-and-country";
		public static final String PROGRAM_TYPE_TAG_ROOT = "/content/cq:tags/program-type";
		public static final String RELEASE_NOTES_TAG_ROOT = "/content/cq:tags/release-notes";
		public static final String INDUSTRY_TAG_ROOT = "/content/cq:tags/industry";
		public static final String RELEASE_TAG_ROOT = "/content/cq:tags/release";
		public static final String USER_TAG_ROOT = "/content/cq:tags/user";
		public static final String USING_WORKDAY_TAG_ROOT = "/content/cq:tags/using-workday";
		public static final String PRODUCT_TAG_ROOT = "/content/cq:tags/product";
		public static final String EVENT_TAG_ROOT = "/content/cq:tags/event";
	}

	/**
	 * The Interface EventsPageConstants.
	 */
	public interface EventsPageConstants {

		/** The Constant EVENT_DETAILS_SLING_RESOURCE. */
		public static final String EVENT_DETAILS_SLING_RESOURCE = "community/components/events/eventdetails";

		/** The Constant EVENT_DETAILS_COMP_NODE_NAME. */
		public static final String EVENT_DETAILS_COMP_NODE_NAME = "eventdetails";

		/** The Constant EVENT_REGISTRATION_SLING_RESOURCE. */
		public static final String EVENT_REGISTRATION_SLING_RESOURCE = "community/components/events/eventregistration";

		/** The Constant EVENT_REGISTRATION_COMP_NODE_NAME. */
		public static final String EVENT_REGISTRATION_COMP_NODE_NAME = "eventregistration";

		/** The Constant MMM_DD_COMMA_YYYY_FORMAT. */
		public static final String MMM_DD_COMMA_YYYY_FORMAT = "MMM dd, yyyy";

		/** The Constant YYYY_MM_DD_FORMAT. */
		public static final String YYYY_MM_DD_FORMAT = "yyyy-MM-dd";

		/** The Constant TEXT_REGISTER_FOR_EVENT. */
		public static final String TEXT_REGISTER_FOR_EVENT = "Register for Event";

	}

	public interface EventDetailsConstants {

		public static final String REQ_TIME_FORMAT = "HH:mm";

		public static final String REQ_DATE_FORMAT = "EEEE, MMM dd, YYYY";

		public static final String DATE_TIME_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX";

		public static final String DAYS_LABEL = "Days";

		public static final String DAY_LABEL = "Day";
	}
}
