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

  @AttributeDefinition(name = "Snap logic Context API Path", description = "Snap logic Context Path.", type = AttributeType.STRING)
  String snapContextPath();

  @AttributeDefinition(name = "Snap logic context Api Token", description = "Snap logic Context api token.", type = AttributeType.STRING)
  String snapContextApiToken();

  @AttributeDefinition(name = "Snap logic Context Api key", description = "Snap logic Context API key.", type = AttributeType.STRING)
  String snapContextApiKey();

  @AttributeDefinition(name = "Snap logic Profile API Path", description = "Snap logic Profile Path.", type = AttributeType.STRING)
  String snapProfilePath();

  @AttributeDefinition(name = "Snap logic Profile Api Token", description = "Snap logic Profile api token.", type = AttributeType.STRING)
  String snapProfileApiToken();

  @AttributeDefinition(name = "Snap logic Profile Api key", description = "Snap logic Profile API key.", type = AttributeType.STRING)
  String snapProfileApiKey();

  @AttributeDefinition(name = "SF roles to AEM groups map", description = "SF roles to AEM groups map json file path.", type = AttributeType.STRING)
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

  @AttributeDefinition(name = "User max menu cache size", description = "User Max Menu Cache size", type = AttributeType.INTEGER)
  int menuCacheMax() default 100;

  @AttributeDefinition(name = "User menu cache timeout duration", description = "User Menu Cache Timeout Duration (mills)", type = AttributeType.LONG)
  long menuCacheTimeout() default 86400000L; // Default to one day.

  @AttributeDefinition(name = "User profile URL", description = "User profile page URL", type = AttributeType.STRING)
  String userProfileUrl();
}
