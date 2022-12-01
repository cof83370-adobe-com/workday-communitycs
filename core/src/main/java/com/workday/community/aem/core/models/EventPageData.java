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
	private String field_retirement_date_value;
	
	/** The readcount. */
	@XmlElement(name = "readcount")
	private String readcount;
	
	/** The field description value. */
	@XmlElement(name = "description")
	private String description;
	
	/** The start date. */
	@XmlElement(name = "start_date")
	private String start_date;
	
	/** The end date. */
	@XmlElement(name = "end_date")
	private String end_date;
	
	/** The Contenttype. */
	@XmlElement(name = "Contenttype")
	private String Contenttype;

	/** The Calendareventtype. */
	@XmlElement(name = "Calendareventtype")
	private String Calendareventtype;

	/** The Product. */
	@XmlElement(name = "Product")
	private String Product;

	/** The show ask related question. */
	@XmlElement(name = "show_ask_related_question")
	private String show_ask_related_question;

	/** The access control. */
	@XmlElement(name = "access_control")
	private String access_control;
	
	/** The group name. */
	@XmlElement(name = "group_name")
	private String group_name;
	
	/** The data center. */
	@XmlElement(name = "data_center")
	private String data_center;

	/** The registration url. */
	@XmlElement(name = "registration_url")
	private String registration_url;
	
	/** The release tag. */
	@XmlElement(name = "release_tag")
	private String release_tag;

	/** The using worday. */
	@XmlElement(name = "using_worday")
	private String using_worday;
	
	/** The content type label. */
	private String contentTypeLabel = "Contenttype";

	/** The calendar event type label. */
	private String calendarEventTypeLabel = "Calendareventtype";

	/** The product label. */
	private String productLabel = "Product";

	/**
	 * Gets the content type label.
	 *
	 * @return the content type label
	 */
	public String getContentTypeLabel() {
		return contentTypeLabel;
	}

	/**
	 * Gets the calendar event type label.
	 *
	 * @return the calendar event type label
	 */
	public String getCalendarEventTypeLabel() {
		return calendarEventTypeLabel;
	}

	/**
	 * Gets the product label.
	 *
	 * @return the product label
	 */
	public String getProductLabel() {
		return productLabel;
	}

	/**
	 * Gets the nid.
	 *
	 * @return the nid
	 */
	public String getNid() {
		return nid;
	}

	/**
	 * Sets the nid.
	 *
	 * @param nid the new nid
	 */
	public void setNid(String nid) {
		this.nid = nid;
	}

	/**
	 * Gets the title.
	 *
	 * @return the title
	 */
	public String getTitle() {
		return title;
	}

	/**
	 * Sets the title.
	 *
	 * @param title the new title
	 */
	public void setTitle(String title) {
		this.title = title;
	}

	/**
	 * Gets the changed.
	 *
	 * @return the changed
	 */
	public String getChanged() {
		return changed;
	}

	/**
	 * Sets the changed.
	 *
	 * @param changed the new changed
	 */
	public void setChanged(String changed) {
		this.changed = changed;
	}

	/**
	 * Gets the retirement date.
	 *
	 * @return the retirement date
	 */
	public String getRetirement_date() {
		return field_retirement_date_value;
	}

	/**
	 * Sets the retirement date.
	 *
	 * @param retirement_date the new retirement date
	 */
	public void setRetirement_date(String field_retirement_date_value) {
		this.field_retirement_date_value = field_retirement_date_value;
	}

	/**
	 * Gets the readcount.
	 *
	 * @return the readcount
	 */
	public String getReadcount() {
		return readcount;
	}

	/**
	 * Sets the readcount.
	 *
	 * @param readcount the new readcount
	 */
	public void setReadcount(String readcount) {
		this.readcount = readcount;
	}

	/**
	 * Gets the description.
	 *
	 * @return the description
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * Sets the description.
	 *
	 * @param description the new description
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * Gets the start date.
	 *
	 * @return the start date
	 */
	public String getStart_date() {
		return start_date;
	}

	/**
	 * Sets the start date.
	 *
	 * @param start_date the new start date
	 */
	public void setStart_date(String start_date) {
		this.start_date = start_date;
	}

	/**
	 * Gets the end date.
	 *
	 * @return the end date
	 */
	public String getEnd_date() {
		return end_date;
	}

	/**
	 * Sets the end date.
	 *
	 * @param end_date the new end date
	 */
	public void setEnd_date(String end_date) {
		this.end_date = end_date;
	}

	/**
	 * Gets the contenttype.
	 *
	 * @return the contenttype
	 */
	public String getContenttype() {
		return Contenttype;
	}

	/**
	 * Sets the contenttype.
	 *
	 * @param contenttype the new contenttype
	 */
	public void setContenttype(String contenttype) {
		Contenttype = contenttype;
	}

	/**
	 * Gets the calendareventtype.
	 *
	 * @return the calendareventtype
	 */
	public String getCalendareventtype() {
		if(Calendareventtype.equalsIgnoreCase("NULL")) {
			return STRING_EMPTY;
		}
		return Calendareventtype;
	}

	/**
	 * Sets the calendareventtype.
	 *
	 * @param calendareventtype the new calendareventtype
	 */
	public void setCalendareventtype(String calendareventtype) {
		Calendareventtype = calendareventtype;
	}

	/**
	 * Gets the product.
	 *
	 * @return the product
	 */
	public String getProduct() {
		if(Product.equalsIgnoreCase("NULL")) {
			return STRING_EMPTY;
		}
		return Product;
	}

	/**
	 * Sets the product.
	 *
	 * @param product the new product
	 */
	public void setProduct(String product) {
		Product = product;
	}

	/**
	 * Gets the show ask related question.
	 *
	 * @return the show ask related question
	 */
	public String getShow_ask_related_question() {
		return show_ask_related_question;
	}

	/**
	 * Sets the show ask related question.
	 *
	 * @param show_ask_related_question the new show ask related question
	 */
	public void setShow_ask_related_question(String show_ask_related_question) {
		this.show_ask_related_question = show_ask_related_question;
	}

	/**
	 * Gets the access control.
	 *
	 * @return the access control
	 */
	public String getAccess_control() {
		return access_control;
	}

	/**
	 * Sets the access control.
	 *
	 * @param access_control the new access control
	 */
	public void setAccess_control(String access_control) {
		this.access_control = access_control;
	}

	/**
	 * Gets the group name.
	 *
	 * @return the group name
	 */
	public String getGroup_name() {
		return group_name;
	}

	/**
	 * Sets the group name.
	 *
	 * @param group_name the new group name
	 */
	public void setGroup_name(String group_name) {
		this.group_name = group_name;
	}

	/**
	 * Gets the data center.
	 *
	 * @return the data center
	 */
	public String getData_center() {
		if(data_center.equalsIgnoreCase("NULL")) {
			return STRING_EMPTY;
		}
		return data_center;
	}

	/**
	 * Sets the data center.
	 *
	 * @param data_center the new data center
	 */
	public void setData_center(String data_center) {
		this.data_center = data_center;
	}

	/**
	 * Gets the registration url.
	 *
	 * @return the registration url
	 */
	public String getRegistration_url() {
		return registration_url;
	}

	/**
	 * Sets the registration url.
	 *
	 * @param registration_url the new registration url
	 */
	public void setRegistration_url(String registration_url) {
		this.registration_url = registration_url;
	}

	/**
	 * Gets the release tag.
	 *
	 * @return the release tag
	 */
	public String getRelease_tag() {
		if(release_tag.equalsIgnoreCase("NULL")) {
			return STRING_EMPTY;
		}
		return release_tag;
	}

	/**
	 * Sets the release tag.
	 *
	 * @param release_tag the new release tag
	 */
	public void setRelease_tag(String release_tag) {
		this.release_tag = release_tag;
	}

	/**
	 * Gets the using worday.
	 *
	 * @return the using worday
	 */
	public String getUsing_worday() {
		if(using_worday.equalsIgnoreCase("NULL")) {
			return STRING_EMPTY;
		}
		return using_worday;
	}

	/**
	 * Sets the using worday.
	 *
	 * @param using_worday the new using worday
	 */
	public void setUsing_worday(String using_worday) {
		this.using_worday = using_worday;
	}

	/**
	 * To string.
	 *
	 * @return the string
	 */
	@Override
	public String toString() {
		return "EventPageData [nid=" + nid + ", title=" + title + ", changed=" + changed + ", field_retirement_date_value="
				+ field_retirement_date_value + ", readcount=" + readcount + ", description=" + description + ", start_date="
				+ start_date + ", end_date=" + end_date + ", Contenttype=" + Contenttype + ", Calendareventtype="
				+ Calendareventtype + ", Product=" + Product + ", show_ask_related_question="
				+ show_ask_related_question + ", access_control=" + access_control + ", group_name=" + group_name
				+ ", data_center=" + data_center + ", registration_url=" + registration_url + ", release_tag="
				+ release_tag + ", using_worday=" + using_worday + ", contentTypeLabel=" + contentTypeLabel
				+ ", calendarEventTypeLabel=" + calendarEventTypeLabel + ", productLabel=" + productLabel + "]";
	}
}
