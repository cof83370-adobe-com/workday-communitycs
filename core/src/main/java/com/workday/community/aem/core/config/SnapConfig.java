package com.workday.community.aem.core.config;

import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.AttributeType;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;

@ObjectClassDefinition(name = "Snaplogic Config", description = "SnaplogicService OSGi Config Vaues")
public @interface SnapConfig {
    @AttributeDefinition(name = "Snaplogic Url", description = "Sanplogic base Url.", type = AttributeType.STRING)
    String snapUrl();

    @AttributeDefinition(name = "Nav API", description = "Nav menu api endpoint.", type = AttributeType.STRING)
    String navApi() default "/contact/menu?id=%s";

    @AttributeDefinition(name = "Nav Api Key", description = "Nav menu api key.", type = AttributeType.STRING)
    String navApiKey();

    @AttributeDefinition(name = "Nav Api Token", description = "Nav menu api token.", type = AttributeType.STRING)
    String navApiToken();

    @AttributeDefinition(name = "Profile Avatar Url endpoint", description = "Profile Avatar Url endpoint", type = AttributeType.STRING)
    String sfdc_get_photo_url();

    @AttributeDefinition(name = "Profile Avatar Url  Token", description = "Profile Avatar Url token.", type = AttributeType.STRING)
    String sfdc_get_photo_token();

    @AttributeDefinition(name = "Sfdc Api key", description = "Sfdc Api key.", type = AttributeType.STRING)
    String sfdc_api_key();
}
