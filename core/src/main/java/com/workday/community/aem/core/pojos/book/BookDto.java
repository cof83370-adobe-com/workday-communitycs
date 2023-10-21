package com.workday.community.aem.core.pojos.book;

import java.util.ArrayList;
import java.util.List;
import lombok.Data;
import lombok.Setter;

/**
 * The Class BookDTO.
 */
@Data
@Setter
public class BookDto {

  /** The heading title. */
  private String headingTitle;

  /** The heading link. */
  private String headingLink;

  /** The child level list. */
  private List<BookDto> childLevelList = new ArrayList<>();
}