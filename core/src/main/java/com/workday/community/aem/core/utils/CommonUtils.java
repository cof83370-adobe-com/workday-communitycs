package com.workday.community.aem.core.utils;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.workday.community.aem.core.constants.WccConstants;
import org.apache.jackrabbit.api.security.user.User;
import org.apache.jackrabbit.api.security.user.UserManager;
import org.apache.sling.api.resource.ResourceResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;

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
		
		User user;
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

	/**
	 * Replace all values in the source Json object from the corresponding target Json object, given them
	 * have some json structure.
	 * @param source The source Json object.
	 * @param target The target Json object.
	 */
	public static void  updateSourceFromTarget(JsonObject source, JsonObject target) {
		for (String key : target.keySet()) {
			if (source.has(key)) {
				JsonElement valSource = source.get(key);
				JsonElement valTarget = target.get(key);

				if (valSource instanceof JsonObject && valTarget instanceof JsonObject) {
					updateSourceFromTarget((JsonObject)valSource, (JsonObject)valTarget);
				} else if (valSource instanceof JsonArray && valTarget instanceof JsonArray) {
					updateSourceFromTarget((JsonArray)valSource, (JsonArray)valTarget);
				} else if (valTarget != null && (valSource == null || !valSource.equals(valTarget))) {
					source.add(key, valTarget);
				}
			}
		}
	}

	public static void updateSourceFromTarget(JsonArray source, JsonArray target) {
		for (int i=0; i<source.size(); i++) {
			JsonElement valSource = source.get(i);
			JsonElement valTarget = target.get(i);

			if (valSource instanceof JsonObject && valTarget instanceof JsonObject) {
				updateSourceFromTarget((JsonObject)valSource, (JsonObject)valTarget);
			} else if (valSource instanceof JsonArray && valTarget instanceof JsonArray) {
				updateSourceFromTarget((JsonArray)valSource, (JsonArray)valTarget);
			} else if (valTarget != null && (valSource == null || !valSource.equals(valTarget))) {
				source.set(i, valTarget);
			}
		}
	}


    /**
     * Gets the path list from json string.
     *
     * @param jsonStr from the request json str
     * @return the path list from json
     */
    public static List<String> getPathListFromJsonString(String jsonStr) {
        Gson gson = new Gson();
        Type type = new TypeToken<List<String>>() {
        }.getType();
        List<String> pathDataList = gson.fromJson(jsonStr, type);
        return Optional.ofNullable(pathDataList).orElse(new ArrayList<>());
    }
}
