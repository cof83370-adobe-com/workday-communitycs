package com.workday.community.aem.core.constants;

import static com.day.cq.commons.jcr.JcrConstants.JCR_CREATED_BY;

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
  public static String PROP_USER_PROFILE_FAMILYNAME = "./profile/familyName";

  /** The Constant PROP_AUTHOR. */
  public static String PROP_AUTHOR = "author";

  /** The Constant PROP_JCR_CREATED_BY. */
  public static String PROP_JCR_CREATED_BY = JCR_CREATED_BY;

  /** The Constant PROP_UPDATED_DATE. */
  public static String PROP_UPDATED_DATE = "updatedDate";

  /** The Constant PROP_POSTED_DATE. */
  public static String PROP_POSTED_DATE = "postedDate";

  /** The Constant PUBLISH. */
  public static String PUBLISH = "publish";

  /** The Constant JCR CONTENT NODE. */
  public static String JCR_CONTENT_PATH = "/jcr:content";

  /** The Constant CONTENT_TYPE_MAPPING. */
  public static final Map<String, String> CONTENT_TYPE_MAPPING = Map.of(
    "/conf/workday-community/settings/wcm/templates/event-page-template", "Calendar Event", 
    "/conf/workday-community/settings/wcm/templates/faq", "FAQ", 
    "/conf/workday-community/settings/wcm/templates/kits-and-tools", "Kits and Tools", 
    "/conf/workday-community/settings/wcm/templates/reference", "Reference"
  );

  /** The Constant OKTA_USER_PATH. */
  public static final String OKTA_USER_PATH = "/workdaycommunity/okta";

  /** The Constant COMMUNITY_BOOK_ROOT_PATH. */
  public static final String COMMUNITY_CONTENT_BOOK_ROOT_PATH ="/content/workday-community/en-us/admin-tools/books";

  private GlobalConstants() {
    throw new IllegalStateException("Utility class");
  }

}
