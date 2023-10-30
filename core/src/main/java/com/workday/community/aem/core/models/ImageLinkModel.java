package com.workday.community.aem.core.models;

import com.workday.community.aem.core.dto.ImageLinkDto;
import java.util.List;

/**
 * The Interface ImageLinkModel.
 */
public interface ImageLinkModel {

  /**
   * Gets the final list.
   *
   * @return the final list
   */
  List<ImageLinkDto> getFinalList();
}