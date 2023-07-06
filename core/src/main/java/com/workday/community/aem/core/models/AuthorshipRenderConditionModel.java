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
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.injectorspecific.Self;
import org.apache.sling.models.annotations.injectorspecific.ValueMapValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.adobe.granite.ui.components.rendercondition.RenderCondition;
import com.adobe.granite.ui.components.rendercondition.SimpleRenderCondition;

import org.apache.sling.models.annotations.DefaultInjectionStrategy;

import static java.util.Objects.*;

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

        UserManager userManager = request.getResourceResolver().adaptTo(UserManager.class);
        Session userSession = request.getResourceResolver().adaptTo(Session.class);
        String userId = requireNonNull(userSession).getUserID();
        Authorizable auth;
        try {
            auth = requireNonNull(userManager).getAuthorizable(userId);
            Iterator<Group> groups = requireNonNull(auth).memberOf();
            while (groups.hasNext()) {
                Group g = groups.next();
                for (String groupStr : editGroups) {
                    if (g.getID().startsWith(groupStr)) {
                        allowed = true;
                    }
                }
            }
        } catch (RepositoryException e) {
            logger.error("User not found");
        }
        if (allowed) {
            check = !suffix.endsWith("ReadOnly/granite:rendercondition");
        } else {
            check = suffix.endsWith("ReadOnly/granite:rendercondition");
        }

        request.setAttribute(RenderCondition.class.getName(), new SimpleRenderCondition(check));
    }
}