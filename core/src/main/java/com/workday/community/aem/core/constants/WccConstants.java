package com.workday.community.aem.core.constants;

/**
 * Class for defining WCC constants.
 */
public class WccConstants {

  /**
   * Path to a profile source ID.
   */
  public static final String PROFILE_SOURCE_ID = "./profile/sourceId";

  /**
   * Path to a profile Okta ID.
   */
  public static final String PROFILE_OKTA_ID = "./profile/oktaId";

  /**
   * Path to a profile CC type.
   */
  public static final String CC_TYPE = "./profile/multipleCC";

  /**
   * Path to 403 forbidden page.
   */
  public static final String FORBIDDEN_PAGE_PATH =
      "/content/workday-community/en-us/errors/403.html";

  /**
   * Path to 404 not found page.
   */
  public static final String PAGE_NOT_FOUND_PATH =
      "/content/workday-community/en-us/errors/404.html";

  public static final String ERROR_PAGE_PATH = "/content/workday-community/en-us/errors/500.html";

  /**
   * The roles property name.
   */
  public static final String ROLES = "roles";

  /**
   * Path to the Community root page.
   */
  public static final String WORKDAY_ROOT_PAGE_PATH = "/content/workday-community";

  /**
   * Path to the Community public page root.
   */
  public static final String WORKDAY_PUBLIC_PAGE_PATH = "/content/workday-community/en-us/public";

  /**
   * Path to the Community public assets root.
   */
  public static final String WORKDAY_PUBLIC_ASSETS_PATH =
      "/content/dam/workday-community/en-us/public";

  /**
   * Path to the Community secured assets root.
   */
  public static final String WORKDAY_SECURED_ASSETS_PATH =
      "/content/dam/workday-community/en-us/images";

  /**
   * Path to the Community secured documents root.
   */
  public static final String WORKDAY_SECURED_DOCUMENTS_PATH =
      "/content/dam/workday-community/en-us/documents";

  /**
   * Path to the errors root.
   */
  public static final String WORKDAY_ERROR_PAGES_FORMAT = "/errors/";

  /**
   * Path to the Okta root.
   */
  public static final String WORKDAY_OKTA_USERS_ROOT_PATH = "/okta";

  /**
   * The name of the Community administrative service.
   */
  public static final String WORKDAY_COMMUNITY_ADMINISTRATIVE_SERVICE =
      "workday-community-administrative-service";

  /**
   * The authenticated role name.
   */
  public static final String AUTHENTICATED = "authenticated";

  /**
   * The workday role name.
   */
  public static final String INTERNAL_WORKMATES = "internal_workmates";

  /**
   * The access control tag name.
   */
  public static final String ACCESS_CONTROL_TAG = "access-control";

}
