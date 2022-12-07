/**
 * 
 */
package com.workday.community.aem.core.utils;

/**
 * The Enum TagFinderEnum.
 *
 * @author pepalla
 */
public enum TagFinderEnum {
	
	/** The Calendareventtype. */
	CALENDAREVENTTYPE("Events", "/content/cq:tags/event"), 
	
	/** The Product. */
	PRODUCT("Product", "/content/cq:tags/product"),
	
	/** The using worday. */
	USING_WORDAY("Using Workday", "/content/cq:tags/using-workday"),
	
	/** The release tag. */
	RELEASE_TAG("Release", "/content/cq:tags/release"),
	
	INDUSTRY("Industry", "/content/cq:tags/industry");

	/** The key. */
	private final String key;
	
	/** The value. */
	private final String value;

	/**
	 * Instantiates a new tag finder enum.
	 *
	 * @param key the key
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
