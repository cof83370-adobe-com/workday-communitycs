package com.workday.community.aem.core.services;

import java.util.List;

/**
 * The Interface QueryService.
 */
public interface QueryService {
  /**
   * Gets the total number of published pages.
   *
   * @return The total number of pages.
   */
  long getNumOfTotalPublishedPages();

  /**
   * Gets the pages by templates.
   *
   * @param templates the templates
   * @return List of page path.
   */
  List<String> getPagesByTemplates(String[] templates);

  /**
   * Gets inactive users.
   *
   * @return List of inactive users.
   */
  List<String> getInactiveUsers();

  /**
   * Gets the book nodes by path.
   *
   * @param bookPath    the book path
   * @param currentPath the current path
   * @return the book nodes by path
   */
  List<String> getBookNodesByPath(String bookPath, String currentPath);
  
  /**
   * Gets the review reminder pages
   * 
   * @return List of page paths
   */
  List<String> getReviewReminderPages();
}
