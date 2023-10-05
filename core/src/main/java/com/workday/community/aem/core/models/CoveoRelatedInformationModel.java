package com.workday.community.aem.core.models;

import com.workday.community.aem.core.exceptions.DamException;
import java.util.List;

/**
 * The Sling model interface for Related information in dynamic mode.
 */
public interface CoveoRelatedInformationModel extends CoveoCommonModel {
  /**
   * Get the search criteria for related information
   *
   * @return the search criteria
   * @throws DamException a DamException object.
   */
  List<String> getFacetFields() throws DamException;
}
