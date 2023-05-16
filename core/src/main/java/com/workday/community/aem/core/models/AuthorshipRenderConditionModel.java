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

@Model(adaptables = { SlingHttpServletRequest.class }, defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL)
public class AuthorshipRenderConditionModel {

    @Self
    private SlingHttpServletRequest request;
    /** The logger. */
    private static final Logger logger = LoggerFactory.getLogger(AuthorshipRenderConditionModel.class);

    @ScriptVariable
    ResourceResolver resolver;

    @ValueMapValue
    private List<String> editGroups;

    Boolean check = false;

    String authorType = "standard";

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
                    authorType = "CC-ADMIN";
                }
            }
        } catch (RepositoryException e) {
            logger.info("Page not found");
        }

        check = suffix.contains("authorReadOnly") ? authorType.equals("standard") : !authorType.equals("standard");

        request.setAttribute(RenderCondition.class.getName(), new SimpleRenderCondition(check));
    }
}