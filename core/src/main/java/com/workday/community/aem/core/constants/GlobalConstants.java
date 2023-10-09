package com.workday.community.aem.core.constants;

import java.util.Collections;
import java.util.Map;

/**
 * Defines global constants.
 *
 * @author pepalla
 */
public final class GlobalConstants {

  /**
   * The path to the Community content root.
   */
  public static final String COMMUNITY_CONTENT_ROOT_PATH = "/content/workday-community";

  /**
   * The Community Coveo job path.
   */
  public static final String COMMUNITY_COVEO_JOB = "workday-community/common/coveo/job";

  /**
   * The given name property.
   */
  public static final String PROP_USER_PROFILE_GIVENNAME = "./profile/givenName";

  /**
   * The family name property.
   */
  public static final String PROP_USER_PROFILE_FAMILYNAME = "./profile/familyName";

  /**
   * The author property.
   */
  public static final String PROP_AUTHOR = "author";

  /**
   * The updated date property.
   */
  public static final String PROP_UPDATED_DATE = "updatedDate";

  /**
   * The posted date property.
   */
  public static final String PROP_POSTED_DATE = "postedDate";

  /**
   * The ID of the publish instance.
   */
  public static final String PUBLISH = "publish";

  /**
   * The JCR content path.
   */
  public static final String JCR_CONTENT_PATH = "/jcr:content";

  /**
   * Map of content type templates and their labels.
   */
  public static final Map<String, String> CONTENT_TYPE_MAPPING = Collections.unmodifiableMap(Map.of(
      "/conf/workday-community/settings/wcm/templates/events", "Calendar Event",
      "/conf/workday-community/settings/wcm/templates/faq", "FAQ",
      "/conf/workday-community/settings/wcm/templates/kits-and-tools", "Kits and Tools",
      "/conf/workday-community/settings/wcm/templates/reference", "Reference",
      "/conf/workday-community/settings/wcm/templates/troubleshooting", "Troubleshooting",
      "/conf/workday-community/settings/wcm/templates/training-catalog", "Training Catalog",
      "/conf/workday-community/settings/wcm/templates/page-content", "Content Page",
      "/conf/workday-community/settings/wcm/templates/book", "Book"));

  /**
   * Path to Okta.
   */
  public static final String OKTA_USER_PATH = "/workdaycommunity/okta";

  /**
   * Path to users.
   */
  public static final String USER_ROOT_PATH = "/home/users/";

  /**
   * Path to the book root.
   */
  public static final String COMMUNITY_CONTENT_BOOK_ROOT_PATH =
      "/content/workday-community/en-us/admin-tools/books";

  /**
   * Value of unset configs.
   */
  public static final String CLOUD_CONFIG_NULL_VALUE = "null";

  /**
   * The access control tags property.
   */
  public static final String TAG_PROPERTY_ACCESS_CONTROL = "accessControlTags";

  /**
   * The user service user.
   */
  public static final String READ_SERVICE_USER = "readserviceuser";

  /**
   * Admin service user.
   */
  public static final String ADMIN_SERVICE_USER = "workday-community-administrative-service";

  /**
   * The name of the user group service.
   */
  public static final String SERVICE_USER_GROUP = "adminusergroup";

  /**
   * The IDO 8601 date format.
   */
  public static final String ISO_8601_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX";

  /**
   * Instantiates a new global constants.
   */
  private GlobalConstants() {
    throw new IllegalStateException("Utility class");
  }

}
