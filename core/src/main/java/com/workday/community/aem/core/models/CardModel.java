package com.workday.community.aem.core.models;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.models.annotations.DefaultInjectionStrategy;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.injectorspecific.ValueMapValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Class CardModel.
 * 
 * @author pepalla
 */
@Model(adaptables = {Resource.class,SlingHttpServletRequest.class}, defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL)
public class CardModel {

	/** The Constant log. */
	private static final Logger log = LoggerFactory.getLogger(CardModel.class);

	/** The heading. */
	@ValueMapValue
	private String heading;

	/** The description. */
	@ValueMapValue
	private String description;
	
	/** The theme. */
	@ValueMapValue
	private String theme;
	
	/** The card details. */
	private HashMap<String, String> cardDetails;
	
	/**
	 * Inits the.
	 */
	@PostConstruct
	private void init() {
		log.info("up and running the card model");
		cardDetails = new HashMap<>();
        cardDetails.put("data-description", description);
        cardDetails.put("data-heading", heading);
        cardDetails.put("data-theme", theme);
		log.info("the attribute map " +getCardAttributes());
	}

	/**
 	 * Gets the card heading.
 	 *
 	 * @return the card heading
 	 */
	public String getHeading() {
		return heading;
	}
	 
 	/**
 	 * Gets the card attributes.
 	 *
 	 * @return the card attributes
 	 */
 	public Map<String, String> getCardAttributes(){
		return  cardDetails;
	 }
}
