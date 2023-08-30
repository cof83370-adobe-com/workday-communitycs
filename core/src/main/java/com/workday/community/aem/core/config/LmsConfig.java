package com.workday.community.aem.core.config;

import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.AttributeType;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;

/**
 * The Lms service configuration interface.
 */
@ObjectClassDefinition(name = "Lms API Config", description = "LmsService OSGi Config Vaues")
public @interface LmsConfig {
    @AttributeDefinition(name = "Lms API Url", description = "Lms API Url.", type = AttributeType.STRING)
    String lmsUrl();

    @AttributeDefinition(name = "Lms Token API Path", description = "Lms Token API Path.", type = AttributeType.STRING)
    String lmsTokenPath();

    @AttributeDefinition(name = "Lms Course Detail API Path", description = "Lms Course Detail API Path.", type = AttributeType.STRING)
    String lmsCourseDetailPath();

    @AttributeDefinition(name = "Lms API Client Id", description = "Lms API Client Secret.", type = AttributeType.STRING)
    String lmsAPIClientId();

    @AttributeDefinition(name = "Lms API Client Secret", description = "Lms API Client Secret.", type = AttributeType.STRING)
    String lmsAPIClientSecret();

    @AttributeDefinition(name = "Lms API Refresh Token", description = "Lms API Refresh Token.", type = AttributeType.STRING)
    String lmsAPIRefreshToken();

    @AttributeDefinition(name = "Lms token cache size", description = "Lms Token Cache size", type = AttributeType.INTEGER)
    int lmsTokenCacheMax() default 10;

    @AttributeDefinition(name = "Lms token cache timeout duration", description = "Lms Token Cache Timeout Duration (mills)", type = AttributeType.LONG)
    long lmsTokenCacheTimeout() default 3540000L; // Default to 59 minutes.
}
