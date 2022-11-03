package com.workday.community.aem.core.models;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import org.apache.sling.api.resource.Resource;
import org.apache.sling.models.annotations.DefaultInjectionStrategy;
import org.apache.sling.models.annotations.Model;
 
/**
 * The Class EventDetailsListModel.
 * 
 * @author pepalla
 */
@Model(adaptables = Resource.class, defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL)
public class EventDetailsListModel {

	/** The heading. */
	@Inject
    private String heading;
 
    /** The accordion list. */
    @Inject
    @Named("eventAccordion/.")
    public List<EventDetailModel> accordionList;
 
    /**
     * Gets the accordion list.
     *
     * @return the accordion list
     */
    public List<EventDetailModel> getAccordionList() {
        return accordionList;
    }
 
    /**
     * Gets the heading.
     *
     * @return the heading
     */
    public String getHeading() {
        return heading;
    }
 
    /**
     * Checks if is configured.
     *
     * @return true, if is configured
     */
    public boolean isConfigured() {
        return accordionList != null && !accordionList.isEmpty();
    }
}
