package com.workday.community.aem.core.models;

import com.workday.community.aem.core.constants.GlobalConstants;
import javax.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.models.annotations.DefaultInjectionStrategy;
import org.apache.sling.models.annotations.Model;

/**
 * The Class RootPathModel.
 *
 * @author uttej.vardineni
 */
@Slf4j
@Model(adaptables = {
    SlingHttpServletRequest.class}, defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL)
public class RootPathModel {

  /**
   * The rootPath.
   */
  protected static final String rootPath =
      String.format("%s%s", GlobalConstants.COMMUNITY_CONTENT_ROOT_PATH, "/");

  /**
   * Inits the RootPathModel.
   */
  @PostConstruct
  protected void init() {
    log.debug("Initializing RootPathModel ....");
  }

  /**
   * Gets the root path.
   *
   * @return the String
   */
  public String getRootPath() {
    return rootPath;
  }
}
