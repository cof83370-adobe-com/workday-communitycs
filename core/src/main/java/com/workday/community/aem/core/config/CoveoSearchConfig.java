package com.workday.community.aem.core.config;

import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.AttributeType;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;

import static com.workday.community.aem.core.constants.SearchConstants.SEARCH_EMAIL_SECURITY_PROVIDER;

/**
 * The Coveo search configuration class.
 */
@ObjectClassDefinition(
    name = "Coveo Search configuration",
    description = "Parameters for Coveo search."
)
public @interface CoveoSearchConfig {

  @AttributeDefinition(name = "Search token Api endpoint", description = "Search Token api endpoint")
  String tokenApi() default "https://platform.cloud.coveo.com/rest/search/v2/token";

  @AttributeDefinition(name = "Search token API Key", description = "Search API Key")
  String tokenApiKey() default "xx2b1507e9-0e42-4f7f-845c-9d0fbabae3c9";

  @AttributeDefinition(name = "Default email", description = "Default email for fetching token")
  String defaultEmail() default "wangchun.zhang@workday.com.uat";

  @AttributeDefinition(name = "Recommendation API Key", description = "Recommendation API Key")
  String recommendationApiKey();

  @AttributeDefinition(name = "Upcoming Event API Key", description = "Upcoming Event API Key")
  String upcomingEventApiKey();

  @AttributeDefinition(name = "Organization Id", description = "Organization Id")
  String orgId() default "workdayp3sqtwnv";

  @AttributeDefinition(name = "Search Hub", description = "Search Hub", type = AttributeType.STRING)
  String searchHub() default "communityv1";

  @AttributeDefinition(name = "Token Valid period", description = "Time token is valid for (ms)",  type = AttributeType.INTEGER)
  int tokenValidTime() default 60 * 1000;

  @AttributeDefinition(name = "Dev Mode", description = "Dev Mode",  type = AttributeType.BOOLEAN)
  boolean devMode();
}
