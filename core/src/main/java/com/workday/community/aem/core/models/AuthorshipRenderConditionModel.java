package com.workday.community.aem.core.models;

import java.util.Iterator;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.apache.jackrabbit.api.security.user.Authorizable;
import org.apache.jackrabbit.api.security.user.Group;
import org.apache.jackrabbit.api.security.user.UserManager;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.injectorspecific.ScriptVariable;
import org.apache.sling.models.annotations.injectorspecific.Self;
import org.apache.sling.models.annotations.injectorspecific.ValueMapValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.adobe.granite.ui.components.rendercondition.RenderCondition;
import com.adobe.granite.ui.components.rendercondition.SimpleRenderCondition;

import org.apache.sling.models.annotations.DefaultInjectionStrategy;

/**
 * The Class AuthorshipRenderConditionModel.
 */
@Model(adaptables = { SlingHttpServletRequest.class }, defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL)
public class AuthorshipRenderConditionModel {

    /** The request. */
    @Self
    private SlingHttpServletRequest request;
    /** The logger. */
    private static final Logger logger = LoggerFactory.getLogger(AuthorshipRenderConditionModel.class);

    /** The resolver. */
    @ScriptVariable
    ResourceResolver resolver;

    /** The edit groups. */
    @ValueMapValue
    private List<String> editGroups;

    /** The rendered condition check. */
    Boolean check = false;

    /** The allowed group condition check. */
    Boolean allowed = false;

    /**
     * Inits the Model
     */
    @PostConstruct
    public void init() {
        String suffix = request.getRequestPathInfo().getResourcePath();

        UserManager userManager = resolver.adaptTo(UserManager.class);
        Session userSession = resolver.adaptTo(Session.class);
        String userId = userSession.getUserID();
        Authorizable auth;
        try {
            auth = userManager.getAuthorizable(userId);
            Iterator<Group> groups = auth.memberOf();
            while (groups.hasNext()) {
                Group g = groups.next();
                if (editGroups.contains(g.getID())) {
                    allowed = true;
                }
            }
        } catch (RepositoryException e) {
            logger.info("User not found");
        }
        if (allowed) {
            check = suffix.contains("ReadOnly") ? false : true;
        } else {
            check = suffix.contains("ReadOnly") ? true : false;
        }

        request.setAttribute(RenderCondition.class.getName(), new SimpleRenderCondition(check));
    }
}