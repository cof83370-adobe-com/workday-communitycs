package com.workday.community.aem.core.models;

import com.workday.community.aem.core.constants.GlobalConstants;
import javax.annotation.PostConstruct;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.models.annotations.DefaultInjectionStrategy;
import org.apache.sling.models.annotations.Model;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Class RootPathModel.
 *
 * @author uttej.vardineni
 */
@Model(adaptables = {
    SlingHttpServletRequest.class}, defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL)
public class RootPathModel {

  /**
   * The rootPath.
   */
  protected static final String rootPath =
      String.format("%s%s", GlobalConstants.COMMUNITY_CONTENT_ROOT_PATH, "/");

  /**
   * The logger.
   */
  private final Logger logger = LoggerFactory.getLogger(this.getClass().getName());

  /**
   * Inits the RootPathModel.
   */
  @PostConstruct
  protected void init() {
    logger.debug("Initializing RootPathModel ....");
  }

  /**
   * Gets the root path
   *
   * @return the String
   */
  public String getRootPath() {
    return rootPath;
  }
}
