package com.workday.community.aem.core.config;

import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;

@ObjectClassDefinition(name = "Ourm Users Search Config", description = "Ourm Users Search OSGi Config Vaues")
public @interface OurmUserSearchConfig {
  
  @AttributeDefinition(name = "Ourm Users Search field look up Api endpoint", description = "Ourm Users Search field lookup Api endpoint")
  String searchFieldLookupApi() default "https://den.community-workday.com/user/search/";

  @AttributeDefinition(name = "Ourm Users Search field look up Api Consumer Key", description = "Ourm Users Search field look up Api Consumer Key")
  String searchFieldConsumerKey() default "r4hd9dxB9ToJWYBQpJAhUauGXoh4r35r";

  @AttributeDefinition(name = "Ourm Users Search field look up Api Consumer Secret", description = "Ourm Users Search field look up Api Consumer Secret")
  String searchFieldConsumerSecret() default "Gx9qk47hwzubLymkfyv4xCS42oTJiDMv";

}
