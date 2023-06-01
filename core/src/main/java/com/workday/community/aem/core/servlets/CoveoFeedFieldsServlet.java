package com.workday.community.aem.core.servlets;

import com.adobe.granite.ui.components.ds.DataSource;
import com.adobe.granite.ui.components.ds.SimpleDataSource;
import com.adobe.granite.ui.components.ds.ValueMapResource;
import com.day.crx.JcrConstants;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.workday.community.aem.core.models.CoveoTabListModel;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceMetadata;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.api.servlets.HttpConstants;
import org.apache.sling.api.servlets.SlingSafeMethodsServlet;
import org.apache.sling.api.wrappers.ValueMapDecorator;
import org.osgi.framework.Constants;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.Servlet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * The Class CoveoFeedFieldsServlet.
 *
 * @author wangchun zhang
 */
@Component(service = Servlet.class, property = {Constants.SERVICE_DESCRIPTION + "= Coveo Feed Fields Servlet",
        "sling.servlet.paths=" + "/bin/feedFields", "sling.servlet.methods=" + HttpConstants.METHOD_GET
})
public class CoveoFeedFieldsServlet extends SlingSafeMethodsServlet {
    
    /** The Constant LOGGER. */
    private static final Logger LOGGER = LoggerFactory.getLogger(CoveoFeedFieldsServlet.class);

    /**
     * Do get.
     *
     * @param request  the request
     * @param response the response
     */
    @Override
    protected void doGet(SlingHttpServletRequest request, SlingHttpServletResponse response) {
      LOGGER.info("start to fetch feed fields");
      ResourceResolver resourceResolver = request.getResourceResolver();
      List<Resource> resourceList = new ArrayList<>();
      CoveoTabListModel model = request.adaptTo(CoveoTabListModel.class);
      JsonArray fields = model.getFields();

      if (fields != null && fields.size() > 0) {
        fields.forEach( field -> {
          ValueMap valueMap = new ValueMapDecorator(new HashMap<>());
          valueMap.put("value", ((JsonObject)field).get("name").getAsString());
          valueMap.put("text",  ((JsonObject)field).get("desc").getAsString());
          resourceList.add(new ValueMapResource(resourceResolver, new ResourceMetadata(), JcrConstants.NT_UNSTRUCTURED, valueMap));
        });
      } else {
        LOGGER.debug("Feed fields are not fetched from CoveoTabListModel, please fix it.");
      }

      /*Create a DataSource that is used to populate the drop-down control*/
      DataSource dataSource = new SimpleDataSource(resourceList.iterator());
      request.setAttribute(DataSource.class.getName(), dataSource);
    }
}