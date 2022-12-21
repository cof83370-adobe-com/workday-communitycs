/**
 * 
 */
package com.workday.community.aem.migration.utils;

import com.workday.community.aem.migration.constants.MigrationConstants;

/**
 * The Enum TagFinderEnum.
 *
 * @author pepalla
 */
public enum TagFinderEnum {

	/** The Calendareventtype. */
	CALENDAREVENTTYPE("Events", MigrationConstants.TagRootPaths.EVENT_TAG_ROOT),

	/** The Product. */
	PRODUCT("Product", MigrationConstants.TagRootPaths.PRODUCT_TAG_ROOT),

	/** The using worday. */
	USING_WORKDAY("Using Workday", MigrationConstants.TagRootPaths.USING_WORKDAY_TAG_ROOT),

	/** The release tag. */
	RELEASE_TAG("Release", MigrationConstants.TagRootPaths.RELEASE_TAG_ROOT),

	INDUSTRY("Industry", MigrationConstants.TagRootPaths.INDUSTRY_TAG_ROOT),

	USER("User", MigrationConstants.TagRootPaths.USER_TAG_ROOT),

	RELEASE_NOTES("Release Notes", MigrationConstants.TagRootPaths.RELEASE_NOTES_TAG_ROOT),

	PROGRAM_TYPE("Program Type", MigrationConstants.TagRootPaths.PROGRAM_TYPE_TAG_ROOT),

	REGION_AND_COUNTRY("Region And Country", MigrationConstants.TagRootPaths.REGION_AND_COUNTRY_TAG_ROOT);

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
