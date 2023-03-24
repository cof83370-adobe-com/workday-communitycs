package com.workday.community.aem.core.config;

import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.AttributeType;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;

/**
 * The snap logic service configuration interface.
 */
@ObjectClassDefinition(name = "Snaplogic Config", description = "SnaplogicService OSGi Config Vaues")
public @interface SnapConfig {
  @AttributeDefinition(name = "Snap logic API Url", description = "Sanplogic API Url.", type = AttributeType.STRING)
  String snapUrl();

  @AttributeDefinition(name = "Snap logic Context API Url", description = "Sanplogic Context Url.", type = AttributeType.STRING)
  String snapContextUrl();

  @AttributeDefinition(name = "Nav API", description = "Nav menu api endpoint.", type = AttributeType.STRING)
  String navApi() default "/contact/menu?id=%s";

  @AttributeDefinition(name = "Nav Api Key", description = "Nav menu api key.", type = AttributeType.STRING)
  String navApiKey();

  @AttributeDefinition(name = "Nav Api Token", description = "Nav menu api token.", type = AttributeType.STRING)
  String navApiToken();

  @AttributeDefinition(name = "Snap logic context Api Token", description = "Nav menu api token.", type = AttributeType.STRING)
  String snapContextApiToken();

  @AttributeDefinition(name = "Profile Avatar Url endpoint", description = "Profile Avatar Url endpoint", type = AttributeType.STRING)
  String sfdcUserAvatarUrl();

  @AttributeDefinition(name = "Profile Avatar Url  Token", description = "Profile Avatar Url token.", type = AttributeType.STRING)
  String sfdcUserAvatarToken();

  @AttributeDefinition(name = "Sfdc Api key", description = "Sfdc Api key.", type = AttributeType.STRING)
  String sfdcApiKey();

  @AttributeDefinition(name = "Fallback Menu Data", description = "Fallback Menu Data.", type = AttributeType.STRING)
  String fallbackMenuData() default "/content/dam/workday-community/jcr-content/FailStateHeaderData.json";

  @AttributeDefinition(name = "Fallback Menu Service user", description = "Fallback Menu service user.", type = AttributeType.STRING)
  String fallbackMenuServiceUser() default "navserviceuser";

  @AttributeDefinition(name = "enable AEM beta", description = "AEM Beta?", type = AttributeType.STRING)
  boolean beta() default true;
}
