package com.workday.community.aem.core.services.impl;

import com.workday.community.aem.core.constants.GlobalConstants;
import com.workday.community.aem.core.services.BookOperationsService;
import com.workday.community.aem.core.services.QueryService;
import com.workday.community.aem.core.utils.CommonUtils;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.jcr.Node;
import javax.jcr.RepositoryException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.resource.PersistenceException;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Reference;

/**
 * The Class BookOperationsServiceImpl.
 */
@Slf4j
@Component(
    service = {BookOperationsService.class},
    configurationPolicy = ConfigurationPolicy.OPTIONAL
)
public class BookOperationsServiceImpl implements BookOperationsService {

  /**
   * The query service.
   */
  @Reference
  private QueryService queryService;

  /**
   * {@inheritDoc}
   */
  @Override
  public Set<String> processBookPaths(ResourceResolver resolver, String bookResourcePath,
                                      String bookRequestJsonStr) {
    Set<String> activatePaths = new HashSet<>();
    Resource bookResource = resolver.getResource(bookResourcePath);
    if (StringUtils.isBlank(bookRequestJsonStr) || bookResource == null || queryService == null) {
      return activatePaths;
    }

    try {
      bookResourcePath = bookResource.getPath().split(GlobalConstants.JCR_CONTENT_PATH)[0];
      List<String> bookPathDataList = CommonUtils.getPathListFromJsonString(bookRequestJsonStr);

      if (bookPathDataList == null || bookPathDataList.isEmpty()) {
        return activatePaths;
      }

      for (String bookPagePath : bookPathDataList) {
        List<String> paths = queryService.getBookNodesByPath(bookPagePath, bookResourcePath);
        for (String path : paths) {
          if (resolver.getResource(path) != null) {
            Node root = resolver.getResource(path).adaptTo(Node.class);
            if (root != null) {
              activatePaths.add(root.getPath().split(GlobalConstants.JCR_CONTENT_PATH)[0]);
              root.remove();
            }
          }
        }
      }
      if (resolver.hasChanges()) {
        resolver.commit();
      }
      log.trace("processBook...completeBookData {}", bookPathDataList);
    } catch (RepositoryException | PersistenceException e) {
      log.error("Exception occurred when update book: {} ", e.getMessage());
    }
    return activatePaths;
  }
}
