package com.workday.community.aem.core.models;

import com.workday.community.aem.core.pojos.book.BookDto;
import java.util.List;

/**
 * The Interface TocModel.
 */
public interface TocModel {

  /**
   * Checks for access to view link.
   *
   * @param pagePath the page path
   * @return true, if successful
   */
  boolean hasAccessToViewLink(final String pagePath);

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