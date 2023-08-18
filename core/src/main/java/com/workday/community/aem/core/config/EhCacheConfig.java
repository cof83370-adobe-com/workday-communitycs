package com.workday.community.aem.core.config;

import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.AttributeType;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;

@ObjectClassDefinition(name = "EhCache configuration", description = "Parameters for Eh Cache")
public @interface EhCacheConfig {
  @AttributeDefinition(
      name = "In Memory Cache only",
      description = "In Memory Cache only",
      type = AttributeType.BOOLEAN
  )
  boolean inMemoryOnly();

  @AttributeDefinition(
      name = "Cache pool heap size",
      description = "Cache pool heap size",
      type = AttributeType.INTEGER
  )
  int heapSize() default 10;

}
