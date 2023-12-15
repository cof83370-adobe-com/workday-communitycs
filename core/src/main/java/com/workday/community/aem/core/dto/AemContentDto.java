package com.workday.community.aem.core.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.Data;
import lombok.Setter;

/**
 * The Class AemContentDto.
 */
@Data
@Setter
public class AemContentDto {

  /**
   * AEM identifier for the field.
   */
  @JsonProperty("field_aem_identifier")
  private String fieldAemIdentifier;

  /**
   * AEM identifier for the field.
   */
  @JsonProperty("field_aem_link")
  private String fieldAemLink;

  /**
   * AEM page link for the field.
   */
  @JsonProperty("field_aem_status")
  private String fieldAemStatus;

  /**
   * Bundle information.
   */
  private String bundle;

  /**
   * Owner of the AEM content.
   */
  private String owner;

  /**
   * Label associated with the AEM content.
   */
  private String label;

  /**
   * List of access permissions.
   */
  private List<String> access;

  /**
   * List of terms associated with the AEM content.
   */
  private List<String> terms;
}
