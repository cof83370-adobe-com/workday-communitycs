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
  String snapContextPath();

  @AttributeDefinition(name = "Snap logic context Api Token", description = "Nav menu api token.", type = AttributeType.STRING)
  String snapContextApiToken();

  @AttributeDefinition(name = "Snap logic Context Api key", description = "Snap logic Context API key.", type = AttributeType.STRING)
  String snapContextApiKey();

  @AttributeDefinition (name = "SF roles to AEM groups map", description = "file path for SF roles to AEM groups map json.", type = AttributeType.STRING)
  String sfToAemUserGroupMap() default "/content/dam/workday-community/resources/sf-to-aem-group-map.json";

  @AttributeDefinition(name = "Nav API", description = "Nav menu api endpoint.", type = AttributeType.STRING)
  String navApi();

  @AttributeDefinition(name = "Nav Api Key", description = "Nav menu api key.", type = AttributeType.STRING)
  String navApiKey();

  @AttributeDefinition(name = "Nav Api Token", description = "Nav menu api token.", type = AttributeType.STRING)
  String navApiToken();

  @AttributeDefinition(name = "Fallback Menu Data", description = "Fallback Menu Data.", type = AttributeType.STRING)
  String navFallbackMenuData() default "/content/dam/workday-community/resources/local-header-data.json";

  @AttributeDefinition(name = "Fallback Menu Service user", description = "Fallback Menu service user.", type = AttributeType.STRING)
  String navFallbackMenuServiceUser() default "readserviceuser";

  @AttributeDefinition(name = "Profile Avatar Url endpoint", description = "Profile Avatar Url endpoint", type = AttributeType.STRING)
  String sfdcUserAvatarUrl();

  @AttributeDefinition(name = "Profile Avatar Url  Token", description = "Profile Avatar Url token.", type = AttributeType.STRING)
  String sfdcUserAvatarToken();

  @AttributeDefinition(name = "Profile Avatar Api key", description = "Profile Avatar Api key.", type = AttributeType.STRING)
  String sfdcUserAvatarApiKey();

  @AttributeDefinition(name = "enable AEM beta", description = "AEM Beta?", type = AttributeType.STRING)
  boolean beta() default true;
}
