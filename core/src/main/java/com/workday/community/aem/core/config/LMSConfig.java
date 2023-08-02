package com.workday.community.aem.core.config;

import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.AttributeType;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;

/**
 * The LMS service configuration interface.
 */
@ObjectClassDefinition(name = "LMS API Config", description = "LMSService OSGi Config Vaues")
public @interface LMSConfig {
    @AttributeDefinition(name = "LMS API Url", description = "LMS API Url.", type = AttributeType.STRING)
    String lmsUrl();

    @AttributeDefinition(name = "LMS Token API Path", description = "LMS Token API Path.", type = AttributeType.STRING)
    String lmsTokenPath();

    @AttributeDefinition(name = "LMS Course List API Path", description = "LMS Course List API Path.", type = AttributeType.STRING)
    String lmsCourseListPath();

    @AttributeDefinition(name = "LMS Course Detail API Path", description = "LMS Course Detail API Path.", type = AttributeType.STRING)
    String lmsCourseDetailPath();

    @AttributeDefinition(name = "LMS API Client Id", description = "LMS API Client Secret.", type = AttributeType.STRING)
    String lmsAPIClientId();

    @AttributeDefinition(name = "LMS API Client Secret", description = "LMS API Client Secret.", type = AttributeType.STRING)
    String lmsAPIClientSecret();

    @AttributeDefinition(name = "LMS API Refresh Token", description = "LMS API Refresh Token.", type = AttributeType.STRING)
    String lmsAPIRefreshToken();
}
