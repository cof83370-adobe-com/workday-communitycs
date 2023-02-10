package com.workday.community.aem.core.constants;

/**
 * The Interface GlobalConstants.
 * 
 * @author pepalla Palla
 */
public interface GlobalConstants {
	
	/**
	 * The Interface PageResourceType.
	 */
	public interface PageResourceType {
		
		/** The Constant EVENT. */
		public static final String EVENT = "community/components/eventspage";
		
		/** The Constant RELEASE_NOTES. */
		public static final String RELEASE_NOTES = "community/components/releasenotespage";
		
		/** The Constant TRAINING_CATALOG. */
		public static final String TRAINING_CATALOG = "community/components/trainingcatalogpage";
	}

	/**
	 * The Interface TagPropertyName.
	 */
	public interface TagPropertyName {
		
		/** The Constant EVENT_FORMAT. */
		public static final String EVENT_FORMAT = "eventFormat";
		
		/** The Constant TRAINING_FORMAT. */
		public static final String TRAINING_FORMAT = "trainingFormat";
		
		/** The Constant RELEASE_NOTES_CHNAGE_TYPE. */
		public static final String RELEASE_NOTES_CHNAGE_TYPE = "releasNotesChangeType";

	}

	/**
	 * The Interface EventDetailsConstants.
	 */
	public interface EventDetailsConstants {

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
