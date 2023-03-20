package com.workday.community.aem.core.config;

import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.AttributeType;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;

/**
 * The snap logic service configuration interface.
 */
@ObjectClassDefinition(name = "Snaplogic Config", description = "SnaplogicService OSGi Config Vaues")
public @interface SnapConfig {
  @AttributeDefinition(name = "Snaplogic Url", description = "Sanplogic base Url.", type = AttributeType.STRING)
  String snapUrl();

  @AttributeDefinition(name = "Nav API", description = "Nav menu api endpoint.", type = AttributeType.STRING)
  String navApi() default "/contact/menu?id=%s";

  @AttributeDefinition(name = "Nav Api Key", description = "Nav menu api key.", type = AttributeType.STRING)
  String navApiKey();

  @AttributeDefinition(name = "Nav Api Token", description = "Nav menu api token.", type = AttributeType.STRING)
  String navApiToken();

  @AttributeDefinition(name = "Profile Avatar Url endpoint", description = "Profile Avatar Url endpoint", type = AttributeType.STRING)
  String sfdcUserAvatarUrl();

  @AttributeDefinition(name = "Profile Avatar Url  Token", description = "Profile Avatar Url token.", type = AttributeType.STRING)
  String sfdcUserAvatarToken();

  @AttributeDefinition(name = "Sfdc Api key", description = "Sfdc Api key.", type = AttributeType.STRING)
  String sfdcApiKey();

  @AttributeDefinition(name = "enable AEM beta", description = "AEM Beta?", type = AttributeType.STRING)
  boolean beta() default true;
}
