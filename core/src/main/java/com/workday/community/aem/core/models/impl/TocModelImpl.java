package com.workday.community.aem.core.models.impl;

import com.day.cq.wcm.api.Page;
import com.workday.community.aem.core.models.TocModel;
import com.workday.community.aem.core.services.QueryService;
import java.util.List;
import javax.inject.Inject;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.models.annotations.DefaultInjectionStrategy;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.injectorspecific.OSGiService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Class TocModelImpl.
 */
@Model(
    adaptables = {Resource.class, SlingHttpServletRequest.class},
    adapters = {TocModel.class},
    resourceType = {TocModelImpl.RESOURCE_TYPE},
    defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL
)
public class TocModelImpl implements TocModel {

  /**
   * The Constant RESOURCE_TYPE.
   */
  protected static final String RESOURCE_TYPE = "workday-community/components/common/toc";

  /**
   * The logger.
   */
  private static final Logger logger = LoggerFactory.getLogger(TocModelImpl.class);

  /**
   * The current page.
   */
  @Inject
  private Page currentPage;

  /**
   * The query service.
   */
  @OSGiService
  private QueryService queryService;

  /**
   * Book resource path.
   *
   * @return the string
   */
  @Override
  public String bookResourcePath() {
    logger.debug("bookResourcePath::Entry");
    String bookResourcePath = null;
    if (null != currentPage) {
      List<String> bookPathList = queryService.getBookNodesByPath(currentPage.getPath(), null);
      if (!bookPathList.isEmpty()) {
        bookResourcePath = bookPathList.get(0).split("/firstlevel")[0];
      }
    }
    return bookResourcePath;
  }
}
