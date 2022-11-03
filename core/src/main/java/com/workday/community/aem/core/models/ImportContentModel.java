package com.workday.community.aem.core.models;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.apache.sling.models.annotations.DefaultInjectionStrategy;
import org.apache.sling.models.annotations.Model;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Class ImportContentModel.
 * 
 * @author pepalla
 */
@Model(adaptables = SlingHttpServletRequest.class, defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL)
public class ImportContentModel {

	/** The Constant log. */
	private static final Logger log = LoggerFactory.getLogger(ImportContentModel.class);
	
	private static final String COMMUNITY_TEMPLATES_ROOT_PATH ="/conf/community/settings/wcm/templates";
	private static final String REP_POLICY_PROP ="rep:policy";

	/** The resolver factory. */
	@Inject
	private ResourceResolverFactory resolverFactory;

	/** The wg service param. */
	Map<String, Object> wdServiceParam = Collections.singletonMap(ResourceResolverFactory.SUBSERVICE,
			"workday-community-administrative-service");

	/**
	 * Inits the.
	 */
	@PostConstruct
	private void init() {
		try (ResourceResolver resourceResolver = resolverFactory.getServiceResourceResolver(wdServiceParam)) {
			log.debug("the attribute map:{} ",  getDataAttributes());
		} catch (Exception e) {
			log.error("Error in Get Drop Down Values:{}", e.getMessage());
		}
	}

	/**
	 * Gets the data attributes.
	 *
	 * @return the data attributes
	 */
	public Map<String, String> getDataAttributes() {
		try (ResourceResolver resourceResolver = resolverFactory.getServiceResourceResolver(wdServiceParam)) {
			Map<String, String> attributes = new HashMap<String, String>();
			Resource resource = resourceResolver.getResource(COMMUNITY_TEMPLATES_ROOT_PATH);
			Iterator<Resource> iterator = resource.listChildren();
			while (iterator.hasNext()) {
				Resource res = iterator.next();
				String title = res.getName();
				if (StringUtils.isNotBlank(title) && !title.equalsIgnoreCase(REP_POLICY_PROP)) {
					attributes.put(title, res.getPath());
				}
			}
			return attributes;
		} catch (Exception e) {
			log.error("Error in getDataAttributes method of ImportContentModel: {}", e.getMessage());
		}
		return null;
	}
}
