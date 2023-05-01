package com.workday.community.aem.core.models;

import com.adobe.granite.ui.components.rendercondition.RenderCondition;
import com.adobe.granite.ui.components.rendercondition.SimpleRenderCondition;
import com.workday.community.aem.core.constants.GlobalConstants;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.models.annotations.DefaultInjectionStrategy;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.injectorspecific.Self;
import org.apache.sling.models.annotations.injectorspecific.ValueMapValue;
import javax.annotation.PostConstruct;

/**
 * The Class AuthorTitleRenderConditionModel.
 * 
 * @author uttej.vardineni
 */
@Model(adaptables = { SlingHttpServletRequest.class }, defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL)
public class AuthorTitleRenderConditionModel {

    @Self
    private SlingHttpServletRequest request;

    @ValueMapValue
    private String allowedTemplate;

    /**
     * Inits the Model.
     */
    @PostConstruct
    protected void init() {

        // Get the resource path
        final String suffix = request.getRequestPathInfo().getSuffix();

        if (suffix == null) {
            throw new IllegalArgumentException("Could not determine template from <" + request.getPathInfo() + ">");
        }

        final String[] split = suffix.split(GlobalConstants.JCR_CONTENT_PATH);
        if (split.length < 1) {
            throw new IllegalArgumentException("Could not determine template from <" + request.getPathInfo() + ">");
        }

        // Find the template
        final String pagePath = split[0];

        // Check if we have the right template
        final boolean show = pagePath.equals(allowedTemplate);

        // Add the render condition
        request.setAttribute(RenderCondition.class.getName(), new SimpleRenderCondition(show));
    }
}