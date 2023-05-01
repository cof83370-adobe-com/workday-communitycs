package com.workday.community.aem.core.models;

import com.adobe.granite.ui.components.rendercondition.RenderCondition;
import com.adobe.granite.ui.components.rendercondition.SimpleRenderCondition;
import com.day.cq.wcm.api.Page;
import com.day.cq.wcm.api.PageManager;
import com.workday.community.aem.core.constants.GlobalConstants;

import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.ResourceNotFoundException;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.models.annotations.DefaultInjectionStrategy;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.injectorspecific.Self;
import org.apache.sling.models.annotations.injectorspecific.ValueMapValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import javax.annotation.PostConstruct;
import javax.jcr.RepositoryException;

/**
 * The Class AuthorTitleRenderConditionModel.
 * 
 * @author uttej.vardineni
 */
@Model(adaptables = { SlingHttpServletRequest.class }, defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL)
public class AuthorTitleRenderConditionModel {
    /** The logger. */
    private static final Logger logger = LoggerFactory.getLogger(AuthorTitleRenderConditionModel.class);

    @Self
    private SlingHttpServletRequest request;

    @ValueMapValue
    private List<String> showAuthorInAdvanced;

    /**
     * Inits the Model.
     */
    @PostConstruct
    protected void init() throws RepositoryException {
        // Get the template path from url suffix
        String suffix = request.getRequestPathInfo().getSuffix();

        if (StringUtils.isBlank(suffix)) {
            PageManager pm = request.getResourceResolver().adaptTo(PageManager.class);
            String pagePath = request.getParameter("item");
            Page page = null;
            if (pm != null) {
                page = pm.getPage(pagePath);
            }
            if (page == null) {
                logger.info("Page not found");
                throw new ResourceNotFoundException("Page not found");
            }

            ValueMap data = page.getContentResource().getValueMap();
            suffix = data.get("cq:template", null);
        }
        if (StringUtils.isNotBlank(suffix)) {
            final String[] split = suffix.split(GlobalConstants.JCR_CONTENT_PATH);
            if (split.length > 0) {
                // Find the template
                final String templatePath = split[0];

                // Check if we have the right template

                final boolean show = showAuthorInAdvanced.contains(templatePath);

                // Add the render condition
                request.setAttribute(RenderCondition.class.getName(), new SimpleRenderCondition(show));
            }
        }
    }
}