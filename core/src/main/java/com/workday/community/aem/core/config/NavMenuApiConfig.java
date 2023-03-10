package com.workday.community.aem.core.config;

import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.AttributeType;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;

@ObjectClassDefinition(name = "Nav menu api configuration", description = "Parameters for nav menu api.")
public @interface NavMenuApiConfig {

    @AttributeDefinition(name = "Snaplogic Url", description = "Sanplogic base Url.", type = AttributeType.STRING)
    String snapUrl();

    @AttributeDefinition(name = "Nav API", description = "Nav menu api endpoint.", type = AttributeType.STRING)
    String navApi() default "/contact/menu?id=%s";

    @AttributeDefinition(name = "Nav Api Key", description = "Nav menu api key.", type = AttributeType.STRING)
    String navApiKey();

    @AttributeDefinition(name = "Nav Api Token", description = "Nav menu api token.", type = AttributeType.STRING)
    String navApiToken();
}
