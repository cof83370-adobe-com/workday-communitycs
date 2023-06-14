package com.workday.community.aem.core.constants;

import static com.day.cq.commons.jcr.JcrConstants.JCR_CREATED_BY;

import java.util.Collections;
import java.util.Map;

/**
 * The Class GlobalConstants.
 *
 * @author pepalla
 */
public final class GlobalConstants {

  /** The Constant COMMUNITY_CONTENT_ROOT_PATH. */
  public static final String COMMUNITY_CONTENT_ROOT_PATH = "/content/workday-community";

  /** The Constant COMMUNITY_COVEO_JOB. */
  public static final String COMMUNITY_COVEO_JOB = "workday-community/common/coveo/job";

  /** The Constant PROP_USER_PROFILE_GIVENNAME. */
  public static final String PROP_USER_PROFILE_GIVENNAME = "./profile/givenName";

  /** The Constant PROP_USER_PROFILE_FAMILYNAME. */
  public static final String PROP_USER_PROFILE_FAMILYNAME = "./profile/familyName";

  /** The Constant PROP_AUTHOR. */
  public static final String PROP_AUTHOR = "author";

  /** The Constant PROP_JCR_CREATED_BY. */
  public static final String PROP_JCR_CREATED_BY = JCR_CREATED_BY;

  /** The Constant PROP_UPDATED_DATE. */
  public static final String PROP_UPDATED_DATE = "updatedDate";

  /** The Constant PROP_POSTED_DATE. */
  public static final String PROP_POSTED_DATE = "postedDate";

  /** The Constant PUBLISH. */
  public static final String PUBLISH = "publish";

  /** The Constant JCR CONTENT NODE. */
  public static final String JCR_CONTENT_PATH = "/jcr:content";

  /** The Constant CONTENT_TYPE_MAPPING. */
  public static final Map<String, String> CONTENT_TYPE_MAPPING = Collections.unmodifiableMap(Map.of(
    "/conf/workday-community/settings/wcm/templates/events", "Calendar Event", 
    "/conf/workday-community/settings/wcm/templates/faq", "FAQ",
    "/conf/workday-community/settings/wcm/templates/kits-and-tools", "Kits and Tools", 
    "/conf/workday-community/settings/wcm/templates/reference", "Reference",
    "/conf/workday-community/settings/wcm/templates/troubleshooting", "Troubleshooting"
  ));

  /** The Constant OKTA_USER_PATH. */
  public static final String OKTA_USER_PATH = "/workdaycommunity/okta";

  /** The Constant USER_ROOT_PATH. */
  public static final String USER_ROOT_PATH = "/home/users/";

  /** The Constant COMMUNITY_BOOK_ROOT_PATH. */
  public static final String COMMUNITY_CONTENT_BOOK_ROOT_PATH ="/content/workday-community/en-us/admin-tools/books";

  public static final String CLOUD_CONFIG_NULL_VALUE = "null";

  private GlobalConstants() {
    throw new IllegalStateException("Utility class");
  }

  /** Workflow Process Label */
  String PROCESS_LABEL = "process.label";

  /** Equals */
  String EQUALS = "=";
}
