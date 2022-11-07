package com.workday.community.aem.core.models;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * The Class EventPagesList.
 * 
 * @author pepalla
 */
@XmlRootElement(name = "root")
@XmlAccessorType(XmlAccessType.FIELD)
public class EventPagesList
{
	
	/** The root. */
	@XmlElement(name = "row")
	private List<EventPageData> root;
	
	/**
	 * Instantiates a new event pages list.
	 */
	public EventPagesList() {
		super();
	}

	/**
	 * Instantiates a new event pages list.
	 *
	 * @param root the root
	 */
	public EventPagesList(List<EventPageData> root) {
		super();
		this.root = root;
	}

	/**
	 * Gets the root.
	 *
	 * @return the root
	 */
	public List<EventPageData> getRoot() {
		return root;
	}

	/**
	 * Sets the root.
	 *
	 * @param root the new root
	 */
	public void setRoot(List<EventPageData> root) {
		this.root = root;
	}
}
