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
	Calendareventtype("Events", "/content/cq:tags/event"), 
	
	/** The Product. */
	Product("Product", "/content/cq:tags/product"),
	
	/** The using worday. */
	using_worday("Using Workday", "/content/cq:tags/using-workday"),
	
	/** The release tag. */
	release_tag("Release", "/content/cq:tags/release"),
	
	Industry("Industry", "/content/cq:tags/industry");

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
