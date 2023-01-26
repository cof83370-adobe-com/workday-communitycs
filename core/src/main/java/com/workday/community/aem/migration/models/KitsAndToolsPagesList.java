package com.workday.community.aem.migration.models;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * The Class KitsAndToolsPagesList.
 * 
 * 
 * @author palla.pentayya
 */
@XmlRootElement(name = "root")
@XmlAccessorType(XmlAccessType.FIELD)
public class KitsAndToolsPagesList {

	/** The root. */
	@XmlElement(name = "row")
	private List<KitsAndToolsPageData> root;

	/**
	 * Instantiates a new kits and tools pages list.
	 */
	public KitsAndToolsPagesList() {
		super();
	}

	/**
	 * Gets the root.
	 *
	 * @return the root
	 */
	public List<KitsAndToolsPageData> getRoot() {
		return root;
	}

	/**
	 * Sets the root.
	 *
	 * @param root the new root
	 */
	public void setRoot(List<KitsAndToolsPageData> root) {
		this.root = root;
	}

	/**
	 * To string.
	 *
	 * @return the string
	 */
	@Override
	public String toString() {
		return "KitsAndToolsPagesList [root=" + root + "]";
	}
}
