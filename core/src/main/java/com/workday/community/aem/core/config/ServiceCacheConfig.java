package com.workday.community.aem.core.config;

import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.AttributeType;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;

@ObjectClassDefinition(name = "EhCache configuration", description = "Parameters for Eh Cache")
public @interface ServiceCacheConfig {
  @AttributeDefinition(
      name = "Cache maximum size",
      description = "Cache maximum size",
      type = AttributeType.INTEGER
  )
  int maxSize() default 2000;

  @AttributeDefinition(
      name = "Cache duration in seconds before expire",
      description = "Cache duration in seconds before expire (default 1 hours)",
      type = AttributeType.INTEGER
  )
  int expireDuration() default 60 * 60;

  @AttributeDefinition(
      name = "Cache refresh duration in seconds ",
      description = "Cache duration in seconds (default 2 hours)",
      type = AttributeType.INTEGER
  )
  int refreshDuration() default 2 * 60 * 60;
}
