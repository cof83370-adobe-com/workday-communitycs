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

	/**
	 * The Class RESTAPIConstants.
	 */
	public final class RESTAPIConstants {

		/**
		 * Instantiates a new RESTAPIConstants.
		 */
		private RESTAPIConstants() {
			throw new UnsupportedOperationException(UNSUPPORTED_EXCEPTION_MSG);
		}

		/** The constant AUTHORIZATION */
		public static final String AUTHORIZATION = "Authorization";

		/** The constant BEARER_TYPE */
		public static final String BEARER_TYPE = "Bearer ";

		/** The constant X_API_KEY */
		public static final String X_API_KEY = "X-api-key";

		/** The constant CONTENT_TYPE */
		public static final String CONTENT_TYPE = "Content-Type";

		/** The constant ACCEPT_TYPE */
		public static final String ACCEPT_TYPE = "Accept";

		/** The constant APPLICATION_SLASH_JSON */
		public static final String APPLICATION_SLASH_JSON = "application/json";

		/** The constant TRACE_ID */
		public static final String TRACE_ID = "X-Amzn-Trace-Id";

		/** The constant GET_API */
		public static final String GET_API = "GET";

		/** The constant POST_API */
		public static final String POST_API = "POST";

		/** The constant PUT_API */
		public static final String PUT_API = "PUT";

		/** The constant DELETE_API */
		public static final String DELETE_API = "DELETE";

		/** The constant HTTP_TIMEMOUT_UPDATE */
		public static final int HTTP_TIMEMOUT_UPDATE = 15000;
	}

	public interface WRCConstants {
		String PROFILE_SOURCE_ID	= "./profile/sourceId";
	}
}
