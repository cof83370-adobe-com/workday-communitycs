package com.workday.community.aem.core.dto;

import lombok.Data;
import lombok.Setter;

/**
 * The Class RelatedInfoItemDto.
 */
@Data
@Setter
public class RelatedInfoItemDto {

  /** The link title. */
  private String linkTitle;
  
  /** The page path. */
  private String pagePath;
  
  /** The new tab. */
  private String newTab;

}