package com.workday.community.aem.core.utils;

import static java.util.Objects.requireNonNull;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.gson.reflect.TypeToken;
import com.workday.community.aem.core.constants.WccConstants;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import org.apache.jackrabbit.api.security.user.User;
import org.apache.jackrabbit.api.security.user.UserManager;
import org.apache.sling.api.resource.ResourceResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility class for common operations.
 */
public class CommonUtils {

  /**
   * The logger.
   */
  private static final Logger LOGGER = LoggerFactory.getLogger(CommonUtils.class);

  /**
   * Get the Salesforce id of logged-in user.
   *
   * @param resourceResolver the Resource Resolver object.
   * @return The Salesforce id.
   */
  public static String getLoggedInUserSourceId(ResourceResolver resourceResolver) {
    Session session = resourceResolver.adaptTo(Session.class);
    UserManager userManager = resourceResolver.adaptTo(UserManager.class);
    String sfId = null;
    try {
      User user =
          (User) requireNonNull(userManager).getAuthorizable(requireNonNull(session).getUserID());
      sfId = requireNonNull(user).getProperty(WccConstants.PROFILE_SOURCE_ID) != null
          ? requireNonNull(user.getProperty(WccConstants.PROFILE_SOURCE_ID))[0].getString() : null;
    } catch (RepositoryException e) {
      LOGGER.error("Exception in getLoggedInUserSourceId method {}", e.getMessage());
    }

    return sfId;
  }

  /**
   * Get the user id of logged-in user.
   *
   * @param resourceResolver the Resource Resolver object.
   * @return The user id.
   */
  public static String getLoggedInUserId(ResourceResolver resourceResolver) {
    Session session = resourceResolver.adaptTo(Session.class);
    UserManager userManager = resourceResolver.adaptTo(UserManager.class);
    String userId = null;
    try {
      User user =
          (User) requireNonNull(userManager).getAuthorizable(requireNonNull(session).getUserID());
      userId = requireNonNull(user).getProperty(WccConstants.PROFILE_OKTA_ID) != null
          ? requireNonNull(user.getProperty(WccConstants.PROFILE_OKTA_ID))[0].getString() : null;
    } catch (RepositoryException e) {
      LOGGER.error("Exception in getLoggedInUserSourceId method = {}", e.getMessage());
    }

    return userId;
  }

  /**
   * Get the customer type of logged-in user.
   *
   * @param resourceResolver the Resource Resolver object.
   * @return The customer type.
   */
  public static String getLoggedInCustomerType(ResourceResolver resourceResolver) {
    Session session = resourceResolver.adaptTo(Session.class);
    UserManager userManager = resourceResolver.adaptTo(UserManager.class);
    String ccType = null;
    try {
      User user =
          (User) requireNonNull(userManager).getAuthorizable(requireNonNull(session).getUserID());
      ccType = requireNonNull(user).getProperty(WccConstants.CC_TYPE) != null
          ? requireNonNull(user.getProperty(WccConstants.CC_TYPE))[0].getString() : null;
    } catch (RepositoryException e) {
      LOGGER.error("Exception in getLoggedInUserSourceId method = {}", e.getMessage());
    }

    return ccType;
  }

  /**
   * Get the logged-in user.
   *
   * @param resourceResolver the Resource Resolver object.
   * @return The user.
   */
  public static User getLoggedInUser(ResourceResolver resourceResolver) {
    Session session = resourceResolver.adaptTo(Session.class);
    UserManager userManager = resourceResolver.adaptTo(UserManager.class);
    User user = null;
    try {
      user =
          (User) requireNonNull(userManager).getAuthorizable(requireNonNull(session).getUserID());
    } catch (RepositoryException e) {
      LOGGER.error("Exception in getLoggedInUser method = {}", e.getMessage());
    }

    return user;
  }

  /**
   * Get the logged in user node.
   *
   * @param resourceResolver the Resource Resolver object.
   * @return The user node.
   */
  public static Node getLoggedInUserAsNode(ResourceResolver resourceResolver) {

    User user;
    Node userNode = null;
    try {
      user = getLoggedInUser(resourceResolver);
      if (user != null) {
        String userPath = user.getPath();
        LOGGER.debug("getLoggedInUserAsNode userPath--{}", userPath);
        userNode = requireNonNull(resourceResolver.getResource(userPath)).adaptTo(Node.class);
      }
    } catch (RepositoryException e) {
      LOGGER.error("Exception in getLoggedInUser method = {}", e.getMessage());
    }

    return userNode;
  }

  /**
   * Replace all values in the source JSON object from the corresponding target JSON object, given
   * they have the same structure.
   *
   * @param source The source Json object.
   * @param target The target Json object.
   * @param attr   The attribute.
   * @param env    The target environment.
   */
  public static void updateSourceFromTarget(JsonObject source, JsonObject target, String attr,
                                            String env) {
    for (String key : target.keySet()) {
      if (source.has(key)) {
        JsonElement valSource = source.get(key);
        JsonElement valTarget = target.get(key);

        if (valSource instanceof JsonObject && valTarget instanceof JsonObject) {
          updateSourceFromTarget((JsonObject) valSource, (JsonObject) valTarget, attr, env);
        } else if (valSource instanceof JsonArray && valTarget instanceof JsonArray) {
          updateSourceFromTarget((JsonArray) valSource, (JsonArray) valTarget, attr, env);
        } else if (valTarget != null && !valTarget.isJsonNull()
            && (valSource == null || !valSource.equals(valTarget))) {
          JsonElement sourceAttr = source.get(attr);
          JsonElement targetAttr = target.get(attr);
          if ((sourceAttr == null) || (targetAttr == null)
              || sourceAttr.isJsonNull() || targetAttr.isJsonNull()
              || !sourceAttr.equals(targetAttr)) {
            return;
          } else {
            // Only update link in beta
            if (key.equals("href")) {
              String valString = valTarget.getAsString();
              if (valString.contains("beta-content.workday.com")) {
                if (env != null && !env.equalsIgnoreCase("prod")) {
                  valString =
                      valString.replace("beta-content.workday.com", env + "-content.workday.com");
                }
                valTarget = new JsonPrimitive(valString);
                source.add(key, valTarget);
              }
            }
          }
        }
      }
    }
  }

  /**
   * Replace all values in the source JSON array from the corresponding target JSON array, given
   * they have the same json structure.
   *
   * @param source The source Json array.
   * @param target The target Json array.
   * @param attr   The attribute.
   */
  public static void updateSourceFromTarget(JsonArray source, JsonArray target, String attr,
                                            String env) {
    for (int i = 0; i < source.size(); i++) {
      JsonElement valSource = source.get(i);

      if (valSource instanceof JsonObject) {
        JsonElement srcAttr = ((JsonObject) valSource).get(attr);
        if (srcAttr != null && !srcAttr.isJsonNull()) {
          for (int j = 0; j < target.size(); j++) {
            JsonElement valTarget = target.get(j);
            if (valTarget instanceof JsonObject) {
              String targetAttr = ((JsonObject) valTarget).get(attr).getAsString();
              if (srcAttr.getAsString().equals(targetAttr)) {
                updateSourceFromTarget((JsonObject) valSource, (JsonObject) valTarget, attr, env);
                break;
              }
            }
          }
        }
      } else if (valSource instanceof JsonArray) {
        JsonElement valTarget = target.get(i);
        if (valTarget instanceof JsonArray) {
          updateSourceFromTarget((JsonArray) valSource, (JsonArray) valTarget, attr, env);
        }
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
