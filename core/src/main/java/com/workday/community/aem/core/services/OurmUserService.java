package com.workday.community.aem.core.services;

import java.io.IOException;

import com.google.gson.JsonObject;
import com.workday.community.aem.core.config.OurmDrupalConfig;
import com.workday.community.aem.core.exceptions.OurmException;

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
   * @return the json object
   * @throws OurmException the ourm exception
   * @throws IOException Signals that an I/O exception has occurred.
   */
  JsonObject searchOurmUserList(String searchText) throws OurmException, IOException;

}
