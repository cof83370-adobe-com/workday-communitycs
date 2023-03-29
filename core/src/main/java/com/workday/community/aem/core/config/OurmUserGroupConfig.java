package com.workday.community.aem.core.config;

import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.AttributeType;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;

@ObjectClassDefinition(
    name = "Get user groups configuration",
    description = "Parameters for getting user group info."
)
public @interface OurmUserGroupConfig {

    @AttributeDefinition (
        name = "X api key",
        description = "X api key.",
        type = AttributeType.STRING
    )
    String xApiKey();

    @AttributeDefinition (
        name = "Api uri",
        description = "Api uri to get user groups.",
        type = AttributeType.STRING
    )
    String apiUri();

    @AttributeDefinition (
        name = "Api bear token",
        description = "Bear token for api call.",
        type = AttributeType.STRING
    )
    String token();
    
}
