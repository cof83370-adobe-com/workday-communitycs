package com.workday.community.aem.core.constants;

import com.workday.community.aem.core.constants.lambda.BearerToken;

/**
 * The Class GlobalConstants.
 *
 * @author pepalla
 */
public interface GlobalConstants {

  /**
   * The Constant COMMUNITY_CONTENT_ROOT_PATH.
   */
  String COMMUNITY_CONTENT_ROOT_PATH = "/content/workday-community";

  /**
   * The Class PageResourceType.
   */
  interface PageResourceType {
    /**
     * The Constant EVENT.
     */
    String EVENT = "workday-community/components/structure/eventspage";

    /**
     * The Constant RELEASE_NOTES.
     */
    String RELEASE_NOTES = "workday-community/components/core/releasenotespage";

    /**
     * The Constant TRAINING_CATALOG.
     */
    String TRAINING_CATALOG = "workday-community/components/core/trainingcatalogpage";
  }

  /**
   * The Class TagPropertyName.
   */
  interface TagPropertyName {
    /**
     * The Constant EVENT_FORMAT.
     */
    String EVENT_FORMAT = "eventFormat";

    /**
     * The Constant TRAINING_FORMAT.
     */
    String TRAINING_FORMAT = "trainingFormat";

    /**
     * The Constant RELEASE_NOTES_CHNAGE_TYPE.
     */
    String RELEASE_NOTES_CHNAGE_TYPE = "releasNotesChangeType";

  }

  /**
   * The Class EventDetailsConstants.
   */
  interface EventDetailsConstants {
    /**
     * The Constant REQ_TIME_FORMAT.
     */
    String REQ_TIME_FORMAT = "HH:mm";

    /**
     * The Constant REQ_DATE_FORMAT.
     */
    String REQ_DATE_FORMAT = "EEEE, MMM dd, YYYY";

    /**
     * The Constant DATE_TIME_FORMAT.
     */
    String DATE_TIME_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX";

    /**
     * The Constant DAYS_LABEL.
     */
    String DAYS_LABEL = "Days";

    /**
     * The Constant DAY_LABEL.
     */
    String DAY_LABEL = "Day";

    /**
     * The Constant HOURS_LABEL.
     */
    String HOURS_LABEL = "Hours";

    /**
     * The Constant HOUR_LABEL.
     */
    String HOUR_LABEL = "Hour";

    /**
     * The Constant MINUTES_LABEL.
     */
    String MINUTES_LABEL = "Minutes";

    /**
     * The Constant MINUTE_LABEL.
     */
    String MINUTE_LABEL = "Minute";

    /**
     * The Constant MINUTES_IN_1_HOUR.
     */
    long MINUTES_IN_1_HOUR = 60;

    /**
     * The Constant MINUTES_IN_8_HOURS.
     */
    long MINUTES_IN_8_HOURS = 480;

    /**
     * The Constant MINUTES_IN_24_HOURS.
     */
    long MINUTES_IN_24_HOURS = 1440;
  }

  /**
   * The Class RESTAPIConstants.
   */
  interface RESTAPIConstants {

    /**
     * The constant AUTHORIZATION
     */
    String AUTHORIZATION = "Authorization";

    /**
     * The constant BEARER_TYPE
     */
    BearerToken BEARER_TOKEN = (token) -> String.format("Bearer %s", token);

    /**
     * The constant X_API_KEY
     */
    String X_API_KEY = "X-api-key";

    /**
     * The constant CONTENT_TYPE
     */
    String CONTENT_TYPE = "Content-Type";

    /**
     * The constant APPLICATION_SLASH_JSON
     */
    String APPLICATION_SLASH_JSON = "application/json";

    /**
     * The constant TRACE_ID
     */
    String TRACE_ID = "X-Amzn-Trace-Id";

    /**
     * The constant GET_API
     */
    String GET_API = "GET";
  }

  interface WRCConstants {
    /**
     * The profile source id.
     */
    String PROFILE_SOURCE_ID = "./profile/sourceId";

    /**
     * The defeault salesforce id for menu api.
     */
    String DEFAULT_SFID_MASTER = "masterdata";

    /**
     * The timeout for HTTP request (In milliseconds).
     */
    int HTTP_TIMEMOUT = 15000;
  }
}
