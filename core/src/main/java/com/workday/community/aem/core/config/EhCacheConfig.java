package com.workday.community.aem.core.config;

import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.AttributeType;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;

@ObjectClassDefinition(name = "EhCache configuration", description = "Parameters for Eh Cache")
public @interface EhCacheConfig {
  @AttributeDefinition(
      name = "Cache pool heap size",
      description = "Cache pool heap size",
      type = AttributeType.INTEGER
  )
  int heapSize() default 20;

  @AttributeDefinition(
      name = "Cache duration in seconds",
      description = "Cache duration in seconds (default 1 hours)",
      type = AttributeType.INTEGER
  )
  int duration() default 60 * 60;
}
