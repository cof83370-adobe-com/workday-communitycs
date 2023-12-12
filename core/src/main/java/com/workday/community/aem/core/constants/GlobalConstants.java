package com.workday.community.aem.core.constants;

import static com.day.cq.commons.jcr.JcrConstants.JCR_CREATED_BY;

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
   * The path to the Community content root.
   */
  public static final String COMMUNITY_EVENT_PAGE_PATH = "/content/workday-community/en-us/event1/event2";

  /**
   * The Community Coveo job path.
   */
  public static final String COMMUNITY_COVEO_JOB = "workday-community/common/coveo/job";

  /**
   * The Community Page Update job path.
   */
  public static final String COMMUNITY_PAGE_UPDATE_JOB = "workday-community/common/page/update/job";

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
   * The JCR CREATED BY property. 
   */
  public static final String PROP_JCR_CREATED_BY = JCR_CREATED_BY;

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
   * Constant for EVENTS_TEMPLATE_PATH.
   */
  public static final String EVENTS_TEMPLATE_PATH = "/conf/workday-community/settings/wcm/templates/events";

  /**
   * Constant for FAQ_TEMPLATE_PATH.
   */
  public static final String FAQ_TEMPLATE_PATH = "/conf/workday-community/settings/wcm/templates/faq";

  /**
   * Constant for KITS_AND_TOOLS_TEMPLATE_PATH.
   */
  public static final String KITS_AND_TOOLS_TEMPLATE_PATH =
      "/conf/workday-community/settings/wcm/templates/kits-and-tools";

  /**
   * Constant for REFERENCE_TEMPLATE_PATH.
   */
  public static final String REFERENCE_TEMPLATE_PATH = "/conf/workday-community/settings/wcm/templates/reference";

  /**
   * Constant for TROUBLESHOOTING_TEMPLATE_PATH.
   */
  public static final String TROUBLESHOOTING_TEMPLATE_PATH =
      "/conf/workday-community/settings/wcm/templates/troubleshooting";

  /**
   * Map of content type templates and their labels.
   */
  public static final Map<String, String> CONTENT_TYPE_MAPPING = Collections.unmodifiableMap(Map.of(
      EVENTS_TEMPLATE_PATH, "Calendar Event",
      FAQ_TEMPLATE_PATH, "FAQ",
      KITS_AND_TOOLS_TEMPLATE_PATH, "Kits and Tools",
      REFERENCE_TEMPLATE_PATH, "Reference",
      TROUBLESHOOTING_TEMPLATE_PATH, "Troubleshooting",
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
   * The cq tags property.
   */
  public static final String CQ_TAGS = "cq:tags";

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
   * Path to the notifications root. 
   */
  public static final String COMMUNITY_CONTENT_NOTIFICATIONS_ROOT_PATH = 
      "/content/workday-community/en-us/admin-tools/notifications";
  
  /** 
   * Path of text component. 
   */
  public static final String TEXT_COMPONENT = "workday-community/components/core/text";

  /** The Constant HTML_EXTENSION. */
  public static final String HTML_EXTENSION = ".html";
  
  /** 
   * Path of title component. 
   */
  public static final String TITLE_COMPONENT = "workday-community/components/core/title";
  
  /**
   * The suppress updated date property.
   */
  public static final String PROP_SUPPRESS_UPDATED_DATE = "suppressUpdatedDate";
  
  /**
   * The time zone.
   */
  public static final String TIME_ZONE = "GMT";

  /**
   * Constant for REPLICATION_ACTION_TYPE_ACTIVATE.
   */
  public static final String REPLICATION_ACTION_TYPE_ACTIVATE = "Activate";

  /**
   * Constant for REPLICATION_ACTION_TYPE_DEACTIVATE.
   */
  public static final String REPLICATION_ACTION_TYPE_DEACTIVATE = "Deactivate";

  /**
   * Constant for REPLICATION_ACTION_TYPE_DELETE.
   */
  public static final String REPLICATION_ACTION_TYPE_DELETE = "Delete";

  /**
   * Constant for JCR_UUID.
   */
  public static final String JCR_UUID = "jcr:uuid";

  /**
   * Constant for FIELD_AEM_IDENTIFIER.
   */
  public static final String FIELD_AEM_IDENTIFIER = "field_aem_identifier";

  /**
   * Constant for BUNDLE.
   */
  public static final String BUNDLE = "bundle";

  /**
   * Constant for OWNER.
   */
  public static final String OWNER = "owner";

  /**
   * Constant for FIELD_AEM_LINK.
   */
  public static final String FIELD_AEM_LINK = "field_aem_link";

  /**
   * Constant for FIELD_AEM_STATUS.
   */
  public static final String FIELD_AEM_STATUS = "field_aem_status";

  /**
   * Constant for ACCESS.
   */
  public static final String ACCESS = "access";

  /**
   * Constant for TERMS.
   */
  public static final String TERMS = "terms";

  /**
   * Constant for X_CSRF_TOKEN.
   */
  public static final String X_CSRF_TOKEN = "X-CSRF-Token";

  /**
   * Constant for X_AEM_IDENTIFIER.
   */
  public static final String X_AEM_IDENTIFIER = "X-AEM-Identifier";

  /**
   * Constant for SLASH.
   */
  public static final String SLASH = "/";

  /**
   * Constant for ACCESS_TOKEN.
   */
  public static final String ACCESS_TOKEN = "access_token";

  /**
   * Constant for REST_API_UTIL_MESSAGE.
   */
  public static final String REST_API_UTIL_MESSAGE =
      "Exception in doDrupalUserSearchGet method while executing the request = %s";

  /**
   * Instantiates a new global constants.
   */
  private GlobalConstants() {
    throw new IllegalStateException("Utility class");
  }

}