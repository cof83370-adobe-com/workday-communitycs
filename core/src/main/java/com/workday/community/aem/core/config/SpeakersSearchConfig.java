package com.workday.community.aem.core.config;

import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;

@ObjectClassDefinition(name = "Speakers Search Config", description = "Speakers Search OSGi Config Vaues")
public @interface SpeakersSearchConfig {
      @AttributeDefinition(name = "Speakers Search field look up Api endpoint", description = "Speakers Search field lookup Api endpoint")
  String searchFieldLookupApi() default "https://den.community-workday.com/user/search/";
  String searchFieldConsumerKey() default "r4hd9dxB9ToJWYBQpJAhUauGXoh4r35r";
  String searchFieldConsumerSecret() default "Gx9qk47hwzubLymkfyv4xCS42oTJiDMv";

}
