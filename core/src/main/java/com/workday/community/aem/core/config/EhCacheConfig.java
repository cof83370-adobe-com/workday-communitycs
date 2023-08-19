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
      name = "Cache pool off heap size",
      description = "Cache pool off heap size",
      type = AttributeType.INTEGER
  )
  int offHeapSize() default -1;

  @AttributeDefinition(
      name = "Cache duration in seconds",
      description = "Cache duration in seconds (-1 no expire)",
      type = AttributeType.INTEGER
  )
  int duration() default 24 * 60 * 60;

  @AttributeDefinition(
      name = "Cache Storage path",
      description = "Cache Storage Path",
      type = AttributeType.STRING
  )
  String storagePath();

  @AttributeDefinition(
      name = "Cache Storage disk size (MB)",
      description = "Cache Storage disk size (MB)",
      type = AttributeType.INTEGER
  )
  int diskSize() default 20;
}
