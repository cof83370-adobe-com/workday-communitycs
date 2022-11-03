package com.workday.community.aem.core.models;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import org.apache.sling.api.resource.Resource;
import org.apache.sling.models.annotations.DefaultInjectionStrategy;
import org.apache.sling.models.annotations.Model;
 
/**
 * The Class EventDetailModel.
 * 
 * @author pepalla
 */
@Model(adaptables = Resource.class, defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL)
public class EventDetailModel {

	 /** The each outer bullet. */
 	@Inject
	    private String eachOuterBullet;
	 
	    /** The bullet point list. */
    	@Inject
	    @Named("list/.")
	    private List<EventBulletPointModel> bulletPointList;
	 
	    /**
    	 * Gets the each outer bullet.
    	 *
    	 * @return the each outer bullet
    	 */
    	public String getEachOuterBullet() {
	        return eachOuterBullet;
	    }
	 
	    /**
    	 * Gets the bullet point list.
    	 *
    	 * @return the bullet point list
    	 */
    	public List<EventBulletPointModel> getBulletPointList() {
	        return bulletPointList;
	    }
	 
	    
}
