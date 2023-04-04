package com.workday.community.aem.core.config;

import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.AttributeType;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;

@ObjectClassDefinition(
    name = "AEM run mode configuration",
    description = "AEM run mode configuration."
)
public @interface AemRunModeConfig {

    @AttributeDefinition (
        name = "AEM Instance",
        description = "AEM Instance values: author, publish.",
        type = AttributeType.STRING
    )
    String aemInstance();

    @AttributeDefinition (
        name = "AEM Environment",
        description = "AEM Environment values: dev, qa, stage, prod.",
        type = AttributeType.STRING
    )
    String aemEnv();
    
}
