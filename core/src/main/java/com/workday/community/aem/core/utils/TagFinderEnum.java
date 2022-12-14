/**
 * 
 */
package com.workday.community.aem.core.utils;

import com.workday.community.aem.core.constants.GlobalConstants;

/**
 * The Enum TagFinderEnum.
 *
 * @author pepalla
 */
public enum TagFinderEnum {

	/** The Calendareventtype. */
	CALENDAREVENTTYPE("Events", GlobalConstants.TagRootPaths.EVENT_TAG_ROOT),

	/** The Product. */
	PRODUCT("Product", GlobalConstants.TagRootPaths.PRODUCT_TAG_ROOT),

	/** The using worday. */
	USING_WORKDAY("Using Workday", GlobalConstants.TagRootPaths.USING_WORKDAY_TAG_ROOT),

	/** The release tag. */
	RELEASE_TAG("Release", GlobalConstants.TagRootPaths.RELEASE_TAG_ROOT),

	INDUSTRY("Industry", GlobalConstants.TagRootPaths.INDUSTRY_TAG_ROOT),

	USER("User", GlobalConstants.TagRootPaths.USER_TAG_ROOT),

	RELEASE_NOTES("Release Notes", GlobalConstants.TagRootPaths.RELEASE_NOTES_TAG_ROOT),

	PROGRAM_TYPE("Program Type", GlobalConstants.TagRootPaths.PROGRAM_TYPE_TAG_ROOT),

	REGION_AND_COUNTRY("Region And Country", GlobalConstants.TagRootPaths.REGION_AND_COUNTRY_TAG_ROOT);

	/** The key. */
	private final String key;

	/** The value. */
	private final String value;

	/**
	 * Instantiates a new tag finder enum.
	 *
	 * @param key   the key
	 * @param value the value
	 */
	TagFinderEnum(String key, String value) {
		this.key = key;
		this.value = value;
	}

	/**
	 * Gets the key.
	 *
	 * @return the key
	 */
	public String getKey() {
		return key;
	}

	/**
	 * Gets the value.
	 *
	 * @return the value
	 */
	public String getValue() {
		return value;
	}
}
