package com.workday.community.aem.core.config;

import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.AttributeType;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;

@ObjectClassDefinition(
    name = "Run mode configuration",
    description = "Run mode configuration."
)
public @interface RunModeConfig {

    @AttributeDefinition (
        name = "The Instance",
        description = "The Instance values: author, publish.",
        type = AttributeType.STRING
    )
    String instance();

    @AttributeDefinition (
        name = "The Environment",
        description = "The Environment values: dev, qa, stage, prod.",
        type = AttributeType.STRING
    )
    String env();
    
}
