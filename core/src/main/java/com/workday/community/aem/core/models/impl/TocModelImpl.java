package com.workday.community.aem.core.models.impl;

import com.day.cq.wcm.api.Page;
import com.workday.community.aem.core.models.TocModel;
import com.workday.community.aem.core.services.QueryService;
import java.util.List;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.models.annotations.DefaultInjectionStrategy;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.injectorspecific.OSGiService;

/**
 * The Class TocModelImpl.
 */
@Slf4j
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
    log.debug("bookResourcePath::Entry");
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
