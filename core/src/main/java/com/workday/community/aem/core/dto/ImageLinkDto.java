package com.workday.community.aem.core.dto;

import lombok.Data;

/**
 * The Class ImageLink.
 */
@Data
public class ImageLinkDto {

  /** The file reference. */
  private String fileReference;

  /** The image alt text. */
  private String imageAltText;

  /** The link text. */
  private String linkText;

  /** The new tab. */
  private String newTab;

  /** The page path. */
  private String pagePath;
}