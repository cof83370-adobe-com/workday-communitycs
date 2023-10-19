package com.workday.community.aem.core.config;

import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.AttributeType;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;

/**
 * The snap logic service configuration interface.
 */
@ObjectClassDefinition(
    name = "Snaplogic Config",
    description = "SnaplogicService OSGi Config Vaues"
)
public @interface SnapConfig {

  /**
   * The snap logic API endpoint.
   *
   * @return The endpoint URL.
   */
  @AttributeDefinition(
      name = "Snap logic API Url",
      description = "Sanplogic API Url.",
      type = AttributeType.STRING
  )
  String snapUrl();

  /**
   * The snap logic context API endpoint.
   *
   * @return The endpoint URL.
   */
  @AttributeDefinition(
      name = "Snap logic Context API Path",
      description = "Snap logic Context Path.",
      type = AttributeType.STRING
  )
  String snapContextPath();

  /**
   * The snap logic context API token.
   *
   * @return The token.
   */
  @AttributeDefinition(
      name = "Snap logic context Api Token",
      description = "Snap logic Context api token.",
      type = AttributeType.STRING
  )
  String snapContextApiToken();

  /**
   * The snap logic context API key.
   *
   * @return The key.
   */
  @AttributeDefinition(
      name = "Snap logic Context Api key",
      description = "Snap logic Context API key.",
      type = AttributeType.STRING
  )
  String snapContextApiKey();

  /**
   * The snap logic profile API endpoint.
   *
   * @return The endpoint URL.
   */
  @AttributeDefinition(
      name = "Snap logic Profile API Path",
      description = "Snap logic Profile Path.",
      type = AttributeType.STRING
  )
  String snapProfilePath();

  /**
   * The snap logic profile API token.
   *
   * @return The token.
   */
  @AttributeDefinition(
      name = "Snap logic Profile Api Token",
      description = "Snap logic Profile api token.",
      type = AttributeType.STRING
  )
  String snapProfileApiToken();

  /**
   * The snap logic profile API key.
   *
   * @return The key.
   */
  @AttributeDefinition(
      name = "Snap logic Profile Api key",
      description = "Snap logic Profile API key.",
      type = AttributeType.STRING
  )
  String snapProfileApiKey();

  /**
   * Path to file that contains a mapping of Salesforce to AEM groups.
   *
   * @return The file path.
   */
  @AttributeDefinition(
      name = "SF roles to AEM groups map",
      description = "SF roles to AEM groups map json file path.",
      type = AttributeType.STRING
  )
  String sfToAemUserGroupMap()
      default "/content/dam/workday-community/resources/sf-to-aem-group-map.json";

  /**
   * The nav menu API endpoint.
   *
   * @return The endpoint URL.
   */
  @AttributeDefinition(
      name = "Nav API",
      description = "Nav menu api endpoint.",
      type = AttributeType.STRING
  )
  String navApi();

  /**
   * The navigation API key.
   *
   * @return The key.
   */
  @AttributeDefinition(
      name = "Nav Api Key",
      description = "Nav menu api key.",
      type = AttributeType.STRING
  )
  String navApiKey();

  /**
   * The navigation API token.
   *
   * @return The token.
   */
  @AttributeDefinition(
      name = "Nav Api Token",
      description = "Nav menu api token.",
      type = AttributeType.STRING
  )
  String navApiToken();

  /**
   * The navigation menu fallback data.
   *
   * @return The data.
   */
  @AttributeDefinition(
      name = "Fallback Menu Data",
      description = "Fallback Menu Data.",
      type = AttributeType.STRING
  )
  String navFallbackMenuData()
      default "/content/dam/workday-community/resources/local-header-data.json";

  /**
   * The snap logic user avatar API endpoint.
   *
   * @return The endpoint URL.
   */
  @AttributeDefinition(
      name = "Profile Avatar Url endpoint",
      description = "Profile Avatar Url endpoint",
      type = AttributeType.STRING
  )
  String sfdcUserAvatarUrl();

  /**
   * The snap logic user avatar API token.
   *
   * @return The token.
   */
  @AttributeDefinition(
      name = "Profile Avatar Url  Token",
      description = "Profile Avatar Url token.",
      type = AttributeType.STRING
  )
  String sfdcUserAvatarToken();

  /**
   * The snap logic user avatar API key.
   *
   * @return The key.
   */
  @AttributeDefinition(
      name = "Profile Avatar Api key",
      description = "Profile Avatar Api key.",
      type = AttributeType.STRING
  )
  String sfdcUserAvatarApiKey();

  /**
   * Whether AEM beta is enabled.
   *
   * @return True if enabled, otherwise false.
   */
  @AttributeDefinition(
      name = "enable AEM beta",
      description = "AEM Beta?",
      type = AttributeType.STRING
  )
  boolean beta() default true;

  /**
   * The user profile URL.
   *
   * @return The URL.
   */
  @AttributeDefinition(
      name = "User profile URL",
      description = "User profile page URL",
      type = AttributeType.STRING
  )
  String userProfileUrl();

  /**
   * Whether the menu browser cache is enabled.
   *
   * @return True if enabled, otherwise false.
   */
  @AttributeDefinition(
      name = "Enable Menu browser cache",
      description = "Enable Menu browser cache(default true)",
      type = AttributeType.BOOLEAN
  )
  boolean enableCache() default true;
}
