package com.workday.community.aem.core.config;

import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.AttributeType;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;
import org.osgi.service.metatype.annotations.Option;

@ObjectClassDefinition(
    name = "Run mode configuration",
    description = "Run mode configuration."
)
public @interface RunModeConfig {

    @AttributeDefinition (
        name = "The Instance",
        description = "The Instance values: author, publish.",
        options = {
			@Option(label = "Author", value = "author"), @Option(label = "Publish", value = "publish") 
        }
    )
    String instance();

    @AttributeDefinition (
        name = "The Environment",
        description = "The Environment values: dev, qa, stage, prod.",
        options = {
			@Option(label = "Dev", value = "dev"), @Option(label = "QA", value = "qa"), @Option(label = "Stage", value = "stage"), @Option(label = "Production", value = "prod") 
        }
    )
    String env();
    
}
