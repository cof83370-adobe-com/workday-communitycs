package com.workday.community.aem.core.servlets;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import javax.servlet.Servlet;

import org.apache.commons.collections4.iterators.TransformIterator;
import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceMetadata;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.api.servlets.SlingSafeMethodsServlet;
import org.apache.sling.api.wrappers.ValueMapDecorator;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.adobe.granite.ui.components.ds.DataSource;
import com.adobe.granite.ui.components.ds.SimpleDataSource;
import com.adobe.granite.ui.components.ds.ValueMapResource;
import com.day.crx.JcrConstants;

/**
 * The Class TemplatesListProviderServlet.
 * 
 * @author pepalla
 */
@Component(immediate = true,
service = Servlet.class, 
property = { "sling.servlet.resourceTypes=/bin/workday/templateslist/datasource",
              "sling.servlet.methods=GET" })
public class TemplatesListProviderServlet  extends SlingSafeMethodsServlet{

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1L;
	
	/** The log. */
	private final Logger log = LoggerFactory.getLogger(this.getClass());

    /**
     * Do get.
     *
     * @param request the request
     * @param response the response
     */
    @Override
    protected void doGet(SlingHttpServletRequest request,
                         SlingHttpServletResponse response) {
        try {
            ResourceResolver resourceResolver = request.getResourceResolver();
            List<KeyValue> dropDownList = new ArrayList<>();
                Resource resource = request.getResourceResolver().getResource("/conf/community/settings/wcm/templates");
                Iterator<Resource> iterator = resource.listChildren();
                List<Resource> list = new ArrayList<>();
                iterator.forEachRemaining(list::add);
				list.forEach(res -> {
					String title = res.getName();
					if (StringUtils.isNotBlank(title) && !title.equalsIgnoreCase("rep:policy")) {
						dropDownList.add(new KeyValue(res.getPath(), title));
					}
				});
                log.info("DropdownList " + dropDownList);
                @SuppressWarnings("unchecked")
            
                DataSource ds =
                        new SimpleDataSource(
                                new TransformIterator(
                                        dropDownList.iterator(),
                                        input -> {
                                            final KeyValue keyValue = (KeyValue) input;
                                            final ValueMap vm = new ValueMapDecorator(new HashMap<>());
                                            vm.put("value", keyValue.key);
                                            vm.put("text", keyValue.value);
                                            return new ValueMapResource(
                                                    resourceResolver, new ResourceMetadata(),
                                                    JcrConstants.NT_UNSTRUCTURED, vm);
                                        }));
                request.setAttribute(DataSource.class.getName(), ds);

        } catch (Exception e) {
            log.error("Error in Get Drop Down Values", e);
        }
    }

    /**
     * The Class KeyValue.
     */
    private class KeyValue {

        /**
         * key property.
         */
        private String key;
        /**
         * value property.
         */
        private String value;

        /**
         * constructor instance instance.
         *
         * @param newKey   -
         * @param newValue -
         */
        private KeyValue(final String newKey, final String newValue) {
            this.key = newKey;
            this.value = newValue;
        }
    }

}
