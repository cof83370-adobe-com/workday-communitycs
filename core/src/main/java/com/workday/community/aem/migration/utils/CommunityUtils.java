package com.workday.community.aem.migration.utils;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ValueMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.day.cq.tagging.Tag;
import com.day.cq.tagging.TagManager;

/**
 * The Class CommunityUtils.
 */
public class CommunityUtils {

    /** The Constant logger. */
    private static final Logger logger = LoggerFactory.getLogger(CommunityUtils.class);

    /**
     * Instantiates a new community utils.
     */
    private CommunityUtils() {
		logger.info("Initialized");
	}

    /**
     * Gets the page tags list.
     *
     * @param map the map
     * @param propName the prop name
     * @param resolver the resolver
     * @return the page tags list
     */
    public static List<String> getPageTagsList(final ValueMap map, final String propName, ResourceResolver resolver) {
        List<String> tagType = new ArrayList<>();
            String[] givenTags = map.get(propName, String[].class);
            if (null != givenTags && null != resolver && givenTags.length > 0) {
                TagManager tagManager = resolver.adaptTo(TagManager.class);
                for (String eachTag : givenTags) {
                    Tag tag = tagManager.resolve(eachTag);
                    if(null != tag) {
                        tagType.add(tag.getTitle());
                    }
                }
            }
        logger.debug("Tags for given input: {} is {}", propName,  tagType);
        return Collections.unmodifiableList(tagType);
    }
}
