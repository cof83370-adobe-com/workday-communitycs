package com.workday.community.aem.core.dto;

import java.util.List;
import lombok.Data;

/**
 * The Class RelatedInfoDto.
 */
@Data
public class RelatedInfoDto {
  /** The heading title. */
  private String headingTitle;
  
  /** The type. */
  private String type;
  
  /** The rows. */
  private String rows;
  
  /** The decorative. */
  private String decorative;
  
  /** The file reference. */
  private String fileReference;
  
  /** The footer link text. */
  private String footerLinkText;
  
  /** The footer link url. */
  private String footerLinkUrl;
  
  /** The footer new tab. */
  private String footerNewTab;
  
  /** The description. */
  private String description;
  
  /** The alt text. */
  private String altText;

  /** The related info items list. */
  private List<RelatedInfoItemDto> relatedInfoItemsList;

  /** The show comp. */
  private boolean showComp;
}