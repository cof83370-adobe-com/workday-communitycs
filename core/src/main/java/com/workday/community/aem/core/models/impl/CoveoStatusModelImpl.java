package com.workday.community.aem.core.models.impl;

import com.workday.community.aem.core.models.CoveoStatusModel;
import com.workday.community.aem.core.services.CoveoIndexApiConfigService;
import com.workday.community.aem.core.services.CoveoSourceApiService;
import com.workday.community.aem.core.services.QueryService;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.PostConstruct;
import lombok.Getter;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.models.annotations.DefaultInjectionStrategy;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.injectorspecific.OSGiService;
import org.apache.sling.models.annotations.injectorspecific.ValueMapValue;

/**
 * The Class CoveoStatusModelImpl.
 */
@Model(
    adaptables = SlingHttpServletRequest.class,
    adapters = {CoveoStatusModel.class},
    resourceType = {CoveoStatusModelImpl.RESOURCE_TYPE},
    defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL
)
public class CoveoStatusModelImpl implements CoveoStatusModel {

  /**
   * The Constant RESOURCE_TYPE.
   */
  protected static final String RESOURCE_TYPE = "workday-community/components/common/coveostatus";

  /**
   * The query service.
   */
  @OSGiService
  private QueryService queryService;

  /**
   * The query service.
   */
  @OSGiService
  private CoveoSourceApiService coveoSourceApiService;

  /**
   * The query service.
   */
  @OSGiService
  private CoveoIndexApiConfigService coveoIndexApiConfigService;

  /**
   * The total pages.
   */
  @Getter
  private long totalPages;

  /**
   * The total indexed pages.
   */
  @Getter
  private long indexedPages;

  /**
   * A list of templates.
   */
  @ValueMapValue
  private List<String> templates;

  /**
   * The coveo source server status.
   */
  private boolean serverHasError;

  /**
   * Initializes the CoveoStatusModelImpl class.
   */
  @PostConstruct
  private void init() {
    totalPages = queryService.getNumOfTotalPublishedPages();
    long number = coveoSourceApiService.getTotalIndexedNumber();
    serverHasError = (number == -1);
    indexedPages = number == -1 ? 0 : number;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public float getPercentage() {
    if (totalPages == 0) {
      return (float) 0.0;
    }
    return (float) indexedPages / totalPages;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public List<String> getTemplates() {
    return new ArrayList<>(templates);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean getServerHasError() {
    return serverHasError;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean isCoveoEnabled() {
    return coveoIndexApiConfigService.isCoveoIndexEnabled();
  }

}
