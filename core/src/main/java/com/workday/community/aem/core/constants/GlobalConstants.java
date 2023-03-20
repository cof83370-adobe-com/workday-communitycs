package com.workday.community.aem.core.constants;

/**
 * The Class GlobalConstants.
 * 
 * @author pepalla
 */
public final class GlobalConstants {

	/** The Constant UNSUPPORTED_EXCEPTION_MSG. */
	public static final String UNSUPPORTED_EXCEPTION_MSG = "This is a utility class and cannot be instantiated";

	/** The Constant COMMUNITY_CONTENT_ROOT_PATH. */
	public static final String COMMUNITY_CONTENT_ROOT_PATH = "/content/workday-community";

	/** The Constant COMMUNITY_COVEO_JOB. */
	public static final String COMMUNITY_COVEO_JOB = "workday-community/common/coveo/job";

	/**
	 * Instantiates a new global constants.
	 */
	private GlobalConstants() {
		throw new UnsupportedOperationException(UNSUPPORTED_EXCEPTION_MSG);
	}

	/**
	 * The Class PageResourceType.
	 */
	public final class PageResourceType {

		/**
		 * Instantiates a new page resource type.
		 */
		private PageResourceType() {
			throw new UnsupportedOperationException(UNSUPPORTED_EXCEPTION_MSG);
		}

		/** The Constant EVENT. */
		public static final String EVENT = "workday-community/components/structure/eventspage";

		/** The Constant RELEASE_NOTES. */
		public static final String RELEASE_NOTES = "workday-community/components/core/releasenotespage";

		/** The Constant TRAINING_CATALOG. */
		public static final String TRAINING_CATALOG = "workday-community/components/core/trainingcatalogpage";
	}

	/**
	 * The Class TagPropertyName.
	 */
	public final class TagPropertyName {

		/**
		 * Instantiates a new tag property name.
		 */
		private TagPropertyName() {
			throw new UnsupportedOperationException(UNSUPPORTED_EXCEPTION_MSG);
		}

		/** The Constant EVENT_FORMAT. */
		public static final String EVENT_FORMAT = "eventFormat";

		/** The Constant TRAINING_FORMAT. */
		public static final String TRAINING_FORMAT = "trainingFormat";

		/** The Constant RELEASE_NOTES_CHNAGE_TYPE. */
		public static final String RELEASE_NOTES_CHNAGE_TYPE = "releasNotesChangeType";

	}

	/**
	 * The Class EventDetailsConstants.
	 */
	public final class EventDetailsConstants {

		/**
		 * Instantiates a new event details constants.
		 */
		private EventDetailsConstants() {
			throw new UnsupportedOperationException(UNSUPPORTED_EXCEPTION_MSG);
		}

		/** The Constant REQ_TIME_FORMAT. */
		public static final String REQ_TIME_FORMAT = "HH:mm";

		/** The Constant REQ_DATE_FORMAT. */
		public static final String REQ_DATE_FORMAT = "EEEE, MMM dd, YYYY";

		/** The Constant DATE_TIME_FORMAT. */
		public static final String DATE_TIME_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX";

		/** The Constant DAYS_LABEL. */
		public static final String DAYS_LABEL = "Days";

		/** The Constant DAY_LABEL. */
		public static final String DAY_LABEL = "Day";

		/** The Constant HOURS_LABEL. */
		public static final String HOURS_LABEL = "Hours";

		/** The Constant HOUR_LABEL. */
		public static final String HOUR_LABEL = "Hour";

		/** The Constant MINUTES_LABEL. */
		public static final String MINUTES_LABEL = "Minutes";

		/** The Constant MINUTE_LABEL. */
		public static final String MINUTE_LABEL = "Minute";

		/** The Constant MINUTES_IN_1_HOUR. */
		public static final long MINUTES_IN_1_HOUR = 60;

		/** The Constant MINUTES_IN_8_HOURS. */
		public static final long MINUTES_IN_8_HOURS = 480;

		/** The Constant MINUTES_IN_24_HOURS. */
		public static final long MINUTES_IN_24_HOURS = 1440;
	}
}
