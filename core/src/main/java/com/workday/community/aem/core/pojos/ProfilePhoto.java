package com.workday.community.aem.core.pojos;

import lombok.Getter;
import lombok.Setter;

/**
 * Class for wrapping profile photo API responses from Snap logic.
 */
@Getter
@Setter
public final class ProfilePhoto {

  /**
   * The file name with extension.
   */
  private String fileNameWithExtension;

  /**
   * The success.
   */
  private String success;

  /**
   * The photo content.
   */
  private String base64content;

  /**
   * The photo version id.
   */
  private String photoVersionId;

}
