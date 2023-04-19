package com.workday.community.aem.core.constants;

import static com.day.cq.commons.jcr.JcrConstants.JCR_CREATED_BY;

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
  
	/** The Constant COMMUNITY_COVEO_JOB. */
	String COMMUNITY_COVEO_JOB = "workday-community/common/coveo/job";

  /** The Constant PROP_USER_PROFILE_GIVENNAME. */
  String PROP_USER_PROFILE_GIVENNAME = "./profile/givenName";

  /** The Constant PROP_USER_PROFILE_FAMILYNAME. */
  String PROP_USER_PROFILE_FAMILYNAME = "./profile/familyName";

  /** The Constant PROP_AUTHOR. */
  String PROP_AUTHOR = "author";

  /** The Constant PROP_JCR_CREATED_BY. */
  String PROP_JCR_CREATED_BY = JCR_CREATED_BY;

  /** The Constant PROP_UPDATED_DATE. */
  String PROP_UPDATED_DATE = "updatedDate";

  /** The Constant PROP_POSTED_DATE. */
  String PROP_POSTED_DATE = "postedDate";

  /** The Constant PUBLISH. */
  String PUBLISH = "publish";

  /** The Constant JCR CONTENT NODE . */
  String JCR_CONTENT_PATH = "/jcr:content";
}
