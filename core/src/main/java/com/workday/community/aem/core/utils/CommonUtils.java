package com.workday.community.aem.core.utils;


import com.workday.community.aem.core.constants.WccConstants;
import org.apache.jackrabbit.api.security.user.User;
import org.apache.jackrabbit.api.security.user.UserManager;
import org.apache.sling.api.resource.ResourceResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;


public class CommonUtils {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(CommonUtils.class);
	
	public static String getLoggedInUserSourceId(ResourceResolver resourceResolver) {
		Session session = resourceResolver.adaptTo(Session.class);
		UserManager userManager = resourceResolver.adaptTo(UserManager.class);
		String sfId = null;
		try {
			User user = (User) userManager.getAuthorizable(session.getUserID());
			sfId = user.getProperty(WccConstants.PROFILE_SOURCE_ID) != null ? user.getProperty(WccConstants.PROFILE_SOURCE_ID)[0].getString() : null;
		} catch (RepositoryException e) {
			LOGGER.error("Exception in getLoggedInUserSourceId method {}", e.getMessage());
		}
		return sfId;
	}

	public static String getLoggedInUserId(ResourceResolver resourceResolver) {
		Session session = resourceResolver.adaptTo(Session.class);
		UserManager userManager = resourceResolver.adaptTo(UserManager.class);
		String userId = null;
		try {
			User user = (User) userManager.getAuthorizable(session.getUserID());
			userId =user.getProperty(WccConstants.PROFILE_OKTA_ID)!=null?user.getProperty(WccConstants.PROFILE_OKTA_ID)[0].getString():null;
		} catch (RepositoryException e) {
			LOGGER.error("Exception in getLoggedInUserSourceId method = {}",e.getMessage());
		}
		return userId;
		
	}

	public static String getLoggedInCustomerType(ResourceResolver resourceResolver) {
		Session session = resourceResolver.adaptTo(Session.class);
		UserManager userManager = resourceResolver.adaptTo(UserManager.class);
		String ccType = null;
		try {
			User user = (User) userManager.getAuthorizable(session.getUserID());
			ccType =user.getProperty(WccConstants.CC_TYPE)!=null?user.getProperty(WccConstants.CC_TYPE)[0].getString():null;
		} catch (RepositoryException e) {
			LOGGER.error("Exception in getLoggedInUserSourceId method = {}",e.getMessage());
		}
		return ccType;

	}
	
	public static User getLoggedInUser(ResourceResolver resourceResolver) {
		Session session = resourceResolver.adaptTo(Session.class);
		UserManager userManager = resourceResolver.adaptTo(UserManager.class);
		User user = null;
		try {
			 user = (User) userManager.getAuthorizable(session.getUserID());
		} catch (RepositoryException e) {
			LOGGER.error("Exception in getLoggedInUser method = {}",e.getMessage());
		}
		return user;
		
	}
	
	public static Node getLoggedInUserAsNode(ResourceResolver resourceResolver) {
		
		User user = null;
		Node userNode = null;
		try {
			 user=getLoggedInUser(resourceResolver);
			 if (user != null) {
				 String userPath = user.getPath();
				 LOGGER.debug("getLoggedInUserAsNode userPath--{}", userPath);
				 userNode = resourceResolver.getResource(userPath).adaptTo(Node.class);
			 }
		} catch (RepositoryException e) {
			LOGGER.error("Exception in getLoggedInUser method = {}", e.getMessage());
		}
		return userNode;
		
	}

    
}
