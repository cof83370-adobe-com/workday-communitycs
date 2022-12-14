package com.workday.community.aem.core.models;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * The Class EventPageData.
 * 
 * @author pepalla
 */
@XmlRootElement(name = "row")
@XmlAccessorType(XmlAccessType.FIELD)
public class EventPageData {
	
	private static final String STRING_EMPTY = "";

	/** Generated serialVersionUID. */
	@SuppressWarnings("unused")
	private static final long serialVersionUID = -2525852165658067914L;
	
	/** The nid. */
	@XmlElement(name = "nid")
	private String nid;

	/** The title. */
	@XmlElement(name = "title")
	private String title;
	
	/** The changed. */
	@XmlElement(name = "changed")
	private String changed;
	
	/** The field retirement date value. */
	@XmlElement(name = "field_retirement_date_value")
	private String fieldRetirementDateValue;
	
	/** The readcount. */
	@XmlElement(name = "readcount")
	private String readcount;
	
	/** The field description value. */
	@XmlElement(name = "description")
	private String description;
	
	/** The start date. */
	@XmlElement(name = "start_date")
	private String startDate;
	
	/** The end date. */
	@XmlElement(name = "end_date")
	private String endDate;
	
	/** The Contenttype. */
	@XmlElement(name = "Contenttype")
	private String contentType;

	/** The Calendareventtype. */
	@XmlElement(name = "Calendareventtype")
	private String calendarEventType;

	/** The Product. */
	@XmlElement(name = "Product")
	private String product;

	/** The show ask related question. */
	@XmlElement(name = "show_ask_related_question")
	private String showAskRelatedQuestion;

	/** The access control. */
	@XmlElement(name = "access_control")
	private String accessControl;
	
	/** The group name. */
	@XmlElement(name = "group_name")
	private String groupName;
	
	/** The data center. */
	@XmlElement(name = "data_center")
	private String dataCenter;

	/** The registration url. */
	@XmlElement(name = "registration_url")
	private String registrationUrl;
	
	/** The release tag. */
	@XmlElement(name = "release_tag")
	private String releaseTag;

	/** The using worday. */
	@XmlElement(name = "using_worday")
	private String usingWorday;

	public String getNid() {
		return nid;
	}

	public void setNid(String nid) {
		this.nid = nid;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getChanged() {
		return changed;
	}

	public void setChanged(String changed) {
		this.changed = changed;
	}

	public String getFieldRetirementDateValue() {
		return fieldRetirementDateValue;
	}

	public void setFieldRetirementDateValue(String fieldRetirementDateValue) {
		this.fieldRetirementDateValue = fieldRetirementDateValue;
	}

	public String getReadcount() {
		return readcount;
	}

	public void setReadcount(String readcount) {
		this.readcount = readcount;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getStartDate() {
		return startDate;
	}

	public void setStartDate(String startDate) {
		this.startDate = startDate;
	}

	public String getEndDate() {
		return endDate;
	}

	public void setEndDate(String endDate) {
		this.endDate = endDate;
	}

	public String getContentType() {
		return contentType;
	}

	public void setContentType(String contentType) {
		this.contentType = contentType;
	}

	public String getCalendarEventType() {
		if(calendarEventType.equalsIgnoreCase("NULL")) {
			return STRING_EMPTY;
		}
		return calendarEventType;
	}

	public void setCalendarEventType(String calendarEventType) {
		this.calendarEventType = calendarEventType;
	}

	public String getProduct() {
		if(product.equalsIgnoreCase("NULL")) {
			return STRING_EMPTY;
		}
		return product;
	}

	public void setProduct(String product) {
		this.product = product;
	}

	public String getShowAskRelatedQuestion() {
		return showAskRelatedQuestion;
	}

	public void setShowAskRelatedQuestion(String showAskRelatedQuestion) {
		this.showAskRelatedQuestion = showAskRelatedQuestion;
	}

	public String getAccessControl() {
		return accessControl;
	}

	public void setAccessControl(String accessControl) {
		this.accessControl = accessControl;
	}

	public String getGroupName() {
		return groupName;
	}

	public void setGroupName(String groupName) {
		this.groupName = groupName;
	}

	public String getDataCenter() {
		if(dataCenter.equalsIgnoreCase("NULL")) {
			return STRING_EMPTY;
		}
		return dataCenter;
	}

	public void setDataCenter(String dataCenter) {
		this.dataCenter = dataCenter;
	}

	public String getRegistrationUrl() {
		if (registrationUrl.equalsIgnoreCase("NULL")) {
			return STRING_EMPTY;
		}
		return registrationUrl;
	}

	public void setRegistrationUrl(String registrationUrl) {
		this.registrationUrl = registrationUrl;
	}

	public String getReleaseTag() {
		if(releaseTag.equalsIgnoreCase("NULL")) {
			return STRING_EMPTY;
		}
		return releaseTag;
	}

	public void setReleaseTag(String releaseTag) {
		this.releaseTag = releaseTag;
	}

	public String getUsingWorday() {
		if(usingWorday.equalsIgnoreCase("NULL")) {
			return STRING_EMPTY;
		}
		return usingWorday;
	}

	public void setUsingWorday(String usingWorday) {
		this.usingWorday = usingWorday;
	}

	@Override
	public String toString() {
		return "EventPageData [nid=" + nid + ", title=" + title + ", changed=" + changed + ", fieldRetirementDateValue="
				+ fieldRetirementDateValue + ", readcount=" + readcount + ", description=" + description
				+ ", startDate=" + startDate + ", endDate=" + endDate + ", contentType=" + contentType
				+ ", calendarEventType=" + calendarEventType + ", product=" + product + ", showAskRelatedQuestion="
				+ showAskRelatedQuestion + ", accessControl=" + accessControl + ", groupName=" + groupName
				+ ", dataCenter=" + dataCenter + ", registrationUrl=" + registrationUrl + ", releaseTag=" + releaseTag
				+ ", usingWorday=" + usingWorday + "]";
	}
	
}
