package com.workday.community.aem.core.servlets;

import com.adobe.granite.ui.components.ds.DataSource;
import com.adobe.granite.ui.components.ds.SimpleDataSource;
import com.adobe.granite.ui.components.ds.ValueMapResource;
import com.day.crx.JcrConstants;
import com.workday.community.aem.core.services.CoveoIndexApiConfigService;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import javax.servlet.Servlet;
import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.SlingException;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceMetadata;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.api.servlets.SlingSafeMethodsServlet;
import org.apache.sling.api.wrappers.ValueMapDecorator;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Class TemplatesListProviderServlet.
 *
 * @author pepalla
 */
@Component(immediate = true, service = Servlet.class, property = {
        "sling.servlet.resourceTypes=/bin/workday/templateslist/datasource", "sling.servlet.methods=GET" })
public class TemplatesListProviderServlet extends SlingSafeMethodsServlet {
    /** The log. */
    private final static Logger LOGGER = LoggerFactory.getLogger(TemplatesListProviderServlet.class);

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 1L;

    /** The Constant TEMPLATES_PATH. */
    static final String TEMPLATES_PATH = "/conf/workday-community/settings/wcm/templates";

    /** The CoveoIndexApiConfigService service. */
    @Reference
    private transient CoveoIndexApiConfigService coveoIndexApiConfigService;

    /**
     * Do get.
     *
     * @param request  the request
     * @param response the response
     */
    @Override
    protected void doGet(SlingHttpServletRequest request, SlingHttpServletResponse response) {
        if (Boolean.FALSE.equals(coveoIndexApiConfigService.isCoveoIndexEnabled())) {
            return;
        }

        try {
            ResourceResolver resourceResolver = request.getResourceResolver();
            Resource resource = resourceResolver.getResource(TEMPLATES_PATH);
            Iterator<Resource> iterator;
            List<Resource>  resourceList = new ArrayList<>();

            if (resource != null) {
                iterator = resource.listChildren();
                while (iterator.hasNext()) {
                    Resource res = iterator.next();
                    String title = res.getName();

                    if (StringUtils.isNotBlank(title) && !title.equalsIgnoreCase("rep:policy")) {
                        ValueMap valueMap = new ValueMapDecorator(new HashMap<>());

                        valueMap.put("value", res.getPath());
                        valueMap.put("text", title);
                        resourceList.add(new ValueMapResource(resourceResolver, new ResourceMetadata(), JcrConstants.NT_UNSTRUCTURED, valueMap));
                    }
                }
            }

            DataSource ds = new SimpleDataSource(resourceList.iterator());
            request.setAttribute(DataSource.class.getName(), ds);
        } catch (SlingException e) {
            LOGGER.error("Error in Get Drop Down Values {}", e.getMessage());
        }
    }
}
