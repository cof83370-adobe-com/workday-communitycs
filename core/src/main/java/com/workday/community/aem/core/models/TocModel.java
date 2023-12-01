package com.workday.community.aem.core.models;

import com.workday.community.aem.core.dto.BookDto;
import java.util.List;

/**
 * The Interface TocModel.
 */
public interface TocModel {

  /**
   * Gets the final list.
   *
   * @return the final list
   */
  List<BookDto> getFinalList();

  /**
   * Checks if is toc display.
   *
   * @return true, if is toc display
   */
  boolean isTocDisplay();

}