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
	
	/** Generated serialVersionUID. */
	@SuppressWarnings("unused")
	private static final long serialVersionUID = -2525852165658067914L;

	/** The end date. */
	@XmlElement(name = "end_date")
	private String end_date;

	/** The readcount. */
	@XmlElement(name = "readcount")
	private String readcount;

	/** The Calendareventtype. */
	@XmlElement(name = "Calendareventtype")
	private String Calendareventtype;

	/** The field retirement date value. */
	@XmlElement(name = "field_retirement_date_value")
	private String field_retirement_date_value;

	/** The Contenttype. */
	@XmlElement(name = "Contenttype")
	private String Contenttype;

	/** The field description value. */
	@XmlElement(name = "field_description_value")
	private String field_description_value;

	/** The Product. */
	@XmlElement(name = "Product")
	private String Product;

	/** The title. */
	@XmlElement(name = "title")
	private String title;

	/** The show ask related question. */
	@XmlElement(name = "show_ask_related_question")
	private String show_ask_related_question;

	/** The changed. */
	@XmlElement(name = "changed")
	private String changed;

	/** The start date. */
	@XmlElement(name = "start_date")
	private String start_date;

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
	 * Gets the calendareventtype.
	 *
	 * @return the calendareventtype
	 */
	public String getCalendareventtype() {
		return Calendareventtype;
	}

	/**
	 * Sets the calendareventtype.
	 *
	 * @param Calendareventtype the new calendareventtype
	 */
	public void setCalendareventtype(String Calendareventtype) {
		this.Calendareventtype = Calendareventtype;
	}

	/**
	 * Gets the field retirement date value.
	 *
	 * @return the field retirement date value
	 */
	public String getField_retirement_date_value() {
		return field_retirement_date_value;
	}

	/**
	 * Sets the field retirement date value.
	 *
	 * @param field_retirement_date_value the new field retirement date value
	 */
	public void setField_retirement_date_value(String field_retirement_date_value) {
		this.field_retirement_date_value = field_retirement_date_value;
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
	 * @param Contenttype the new contenttype
	 */
	public void setContenttype(String Contenttype) {
		this.Contenttype = Contenttype;
	}

	/**
	 * Gets the field description value.
	 *
	 * @return the field description value
	 */
	public String getField_description_value() {
		return field_description_value;
	}

	/**
	 * Sets the field description value.
	 *
	 * @param field_description_value the new field description value
	 */
	public void setField_description_value(String field_description_value) {
		this.field_description_value = field_description_value;
	}

	/**
	 * Gets the product.
	 *
	 * @return the product
	 */
	public String getProduct() {
		return Product;
	}

	/**
	 * Sets the product.
	 *
	 * @param Product the new product
	 */
	public void setProduct(String Product) {
		this.Product = Product;
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
	 * To string.
	 *
	 * @return the string
	 */
	@Override
	public String toString() {
		return "EventPageData [end_date=" + end_date + ", readcount=" + readcount + ", Calendareventtype="
				+ Calendareventtype + ", field_retirement_date_value=" + field_retirement_date_value + ", Contenttype="
				+ Contenttype + ", field_description_value=" + field_description_value + ", Product=" + Product
				+ ", title=" + title + ", show_ask_related_question=" + show_ask_related_question + ", changed="
				+ changed + ", start_date=" + start_date + ", contentTypeLabel=" + contentTypeLabel
				+ ", calendarEventTypeLabel=" + calendarEventTypeLabel + ", productLabel=" + productLabel + "]";
	}
}
