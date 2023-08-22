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
  int heapSize() default 10;

  @AttributeDefinition(
     name = "Regular Cache duration in seconds",
     description = "Regular Cache duration in seconds (default 1 day)",
     type = AttributeType.INTEGER
  )
 int longDuration() default 12 * 60 * 60;

  @AttributeDefinition(
      name = "Regular Cache duration in seconds",
      description = "Regular Cache duration in seconds (default 1 hours)",
      type = AttributeType.INTEGER
  )
  int regularDuration() default 60 * 60;

  @AttributeDefinition(
      name = "Short Cache duration in seconds",
      description = "Short Cache duration in seconds (default 10 minutes)",
      type = AttributeType.INTEGER
  )
  int shortDuration() default 10 * 60;
}
