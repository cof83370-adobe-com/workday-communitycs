package com.workday.community.aem.core.models;

import com.day.cq.wcm.api.Page;
import com.day.cq.wcm.api.PageManager;
import com.workday.community.aem.core.constants.GlobalConstants;
import javax.annotation.PostConstruct;
import javax.inject.Inject;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.models.annotations.DefaultInjectionStrategy;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.injectorspecific.RequestAttribute;

/**
 * The Class BookModel.
 *
 * @author uttej.vardineni
 */
@Slf4j
@Model(
    adaptables = {SlingHttpServletRequest.class},
    defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL
)
public class BookModel {

  /**
   * The path to the Community content root.
   */
  @Getter
  protected static final String rootPath =
      String.format("%s%s", GlobalConstants.COMMUNITY_CONTENT_ROOT_PATH, "/");

  /**
   * The page path.
   */
  @RequestAttribute
  private String pagePath;

  /**
   * The resource resolver.
   */
  @Inject
  private ResourceResolver resourceResolver;

  /**
   * Inits the BookModel.
   */
  @PostConstruct
  protected void init() {
    log.debug("Initializing BookModel ....");
  }

  /**
   * Gets the book page object.
   *
   * @return the page
   */
  public Page getBookPage() {
    PageManager pm = resourceResolver.adaptTo(PageManager.class);
    return pm.getPage(pagePath);
  }

}
