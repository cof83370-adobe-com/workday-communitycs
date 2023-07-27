package com.workday.community.aem.core.services;

import java.io.IOException;

import com.workday.community.aem.core.config.OurmDrupalConfig;
import com.workday.community.aem.core.exceptions.OurmException;
import com.workday.community.aem.core.pojos.OurmUserList;


/**
 * The Interface OurmUserService.
 */
public interface OurmUserService {

  /**
   * Activate.
   *
   * @param config the config
   */
  void activate(OurmDrupalConfig config);

  /**
   * Search ourm user list.
   *
   * @param searchText the search text
   * @return the ourm user list
   * @throws OurmException the ourm exception
   * @throws IOException Signals that an I/O exception has occurred.
   */
  OurmUserList searchOurmUserList(String searchText) throws OurmException, IOException;

}
