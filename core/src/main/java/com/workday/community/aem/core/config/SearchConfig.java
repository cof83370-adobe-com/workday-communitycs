package com.workday.community.aem.core.config;

import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.AttributeType;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;

@ObjectClassDefinition(
    name = "Coveo Search api configuration",
    description = "Parameters for coveo search."
)
public interface SearchConfig {

  @AttributeDefinition(name = "Search token Api", description = "Search Token api")
  String tokenApi();

  @AttributeDefinition(name = "Search token API Key", description = "Search API Key")
  String tokenApiKey();

  @AttributeDefinition(name = "Recommendation API Key", description = "Recommendation API Key")
  String recommendationApiKey();

  @AttributeDefinition(name = "Upcoming Event API Key", description = "Upcoming Event API Key")
  String upcomingEventApiKey();

  @AttributeDefinition(name = "Organization Id", description = "Organization Id")
  String orgId();

  @AttributeDefinition(name = "Token Valid", description = "Time token is valid for (ms)",  type = AttributeType.INTEGER)
  int tokenValidTime();
}
