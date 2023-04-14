package com.workday.community.aem.core.config;

import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.AttributeType;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;

@ObjectClassDefinition(
    name = "Coveo api configuration",
    description = "Parameters for coveo api."
)
public @interface CoveoIndexApiConfig {

    @AttributeDefinition (
        name = "Enabled",
        description = "Is coveo indexing enabled",
        type = AttributeType.BOOLEAN
    )
    boolean isCoveoIndexingEnabled() default true;

    @AttributeDefinition (
        name = "Api key",
        description = "Coveo api key.",
        type = AttributeType.STRING
    )
    String coveoApiKey() default "xxc65c7915-1e84-4137-a913-948dd927c424";

    @AttributeDefinition (
        name = "Push Api Uri",
        description = "Coveo push api endpoint.",
        type = AttributeType.STRING
    )
    String pushApiUri() default "https://api.cloud.coveo.com/push/v1/organizations/";

    @AttributeDefinition (
        name = "Source Api Uri",
        description = "Coveo source api endpoint.",
        type = AttributeType.STRING
    )
    String sourceApiUri() default "https://platform.cloud.coveo.com/rest/organizations/";

    @AttributeDefinition (
        name = "Organization Id",
        description = "Coveo organization id.",
        type = AttributeType.STRING
    )
    String organizationId() default "workdayp3sqtwnv";

    @AttributeDefinition (
        name = "Source Id",
        description = "Coveo source id.",
        type = AttributeType.STRING
    )
    String sourceId() default "workdayp3sqtwnv-uto34xdscujxnir5wcbi2ncare";

    @AttributeDefinition (
        name = "Batch size",
        description = "Coveo job batch size.",
        type = AttributeType.INTEGER
    )
    int batchSize() default 50;
}
