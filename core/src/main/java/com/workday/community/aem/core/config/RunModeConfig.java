package com.workday.community.aem.core.config;

import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.AttributeType;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;
import org.osgi.service.metatype.annotations.Option;

/**
 * The run mode configuration interface.
 */
@ObjectClassDefinition(name = "Run mode configuration", description = "Run mode configuration.")
public @interface RunModeConfig {

  /**
   * The current instance.
   *
   * @return The instance name.
   */
  @AttributeDefinition(
      name = "The Instance",
      description = "The Instance values: author, publish.",
      options = {
          @Option(label = "Author", value = "author"),
          @Option(label = "Publish", value = "publish")
      }
  )
  String instance();

  /**
   * The current environment.
   *
   * @return The environment name.
   */
  @AttributeDefinition(
      name = "The Environment",
      description = "The Environment values: dev, qa, stage, prod.",
      type = AttributeType.STRING
  )
  String env();

  /**
   * The URI to the Adobe analytics script.
   *
   * @return The URI.
   */
  @AttributeDefinition(
      name = "Adobe analytics script uri",
      description = "Adobe script uri.",
      type = AttributeType.STRING
  )
  String adobeAnalyticsUri();

  /**
   * The "publish" instance domain.
   *
   * @return The domain.
   */
  @AttributeDefinition(
      name = "Publish instance domain",
      description = "Publish instance domain.",
      type = AttributeType.STRING
  )
  String publishInstanceDomain();

}
