package com.workday.community.aem.core.services;

import java.util.ArrayList;
import java.util.HashMap;

import javax.jcr.NodeIterator;

import com.day.cq.wcm.api.Page;
import org.apache.sling.api.resource.ValueMap;

import org.apache.jackrabbit.api.security.user.UserManager;
import com.day.cq.tagging.TagManager;

/**
 * The ExtractPagePropertiesService interface.
 */
public interface ExtractPagePropertiesService {

    /**
	 * Get page properties.
     * 
     * @param path The page path
     * @return The page properties
	 */
    HashMap<String, Object> extractPageProperties(String path);

    /**
	 * Process date fields.
     * 
     * @param data The value map data
     * @param properties The extracted page properties
	 */
    void processDateFields(ValueMap data, HashMap<String, Object> properties);

    /**
	 * Process page access permission.
     * 
     * @param data The value map data
     * @param properties The extracted page properties
     * @param email Page author's email
	 */
    void processPermission(ValueMap data, HashMap<String, Object> properties, String email);

    /**
	 * Process string fields.
     * 
     * @param data The value map data
     * @param properties The extracted page properties
	 */
    void processStringFields(ValueMap data, HashMap<String, Object> properties);

	/**
	 * Process taxonomy field.
     * 
     * @param tagManager The tag manager
     * @param taxonomyTagIds Taxonomy ids
     * @param taxonomyField Taxonomy field name
     * @return The processed taxonomy values
	 */
    ArrayList<String> processTaxonomyFields (TagManager tagManager, String[] taxonomyTagIds, String taxonomyField);

	/**
	 * Process text field.
     * 
     * @param it The node iterator
     * @param textlist List of text value
	 */
    void processTextComponent(NodeIterator it, ArrayList<String> textlist);

    /**
	 * Process user fields.
     * 
     * @param data The value map data
     * @param userManager The user manager
     * @param properties The extracted page properties
     * @return The user's email
	 */
    String processUserFields(ValueMap data, UserManager userManager, HashMap<String, Object> properties);

	/**
	 * Process custom components and extract values.
	 *
	 * @param page page we are indexing.
	 * @param properties Collected values.
	 */
	void processCustomComponents(Page page, HashMap<String, Object> properties);
}
