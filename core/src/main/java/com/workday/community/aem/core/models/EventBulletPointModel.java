package com.workday.community.aem.core.models;

import javax.inject.Inject;

import org.apache.sling.api.resource.Resource;
import org.apache.sling.models.annotations.DefaultInjectionStrategy;
import org.apache.sling.models.annotations.Model;
 
/**
 * The Class EventBulletPointModel.
 * 
 * @author pepalla
 */
@Model(adaptables = Resource.class, defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL)
public class EventBulletPointModel  {
	 
 	/** The each inner bullet. */
 	@Inject
	    private String eachInnerBullet;
	 
	    /**
    	 * Gets the each inner bullet.
    	 *
    	 * @return the each inner bullet
    	 */
    	public String getEachInnerBullet() {
	        return eachInnerBullet;
	    }
}
