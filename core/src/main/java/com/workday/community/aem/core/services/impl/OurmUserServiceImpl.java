package com.workday.community.aem.core.services.impl;

import static com.workday.community.aem.core.constants.RestApiConstants.GET_API;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.workday.community.aem.core.config.OurmDrupalConfig;
import com.workday.community.aem.core.exceptions.OurmException;
import com.workday.community.aem.core.services.OurmUserService;
import com.workday.community.aem.core.utils.CommunityUtils;
import com.workday.community.aem.core.utils.Oauth1Util;
import com.workday.community.aem.core.utils.RestApiUtil;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.metatype.annotations.Designate;

/**
 * The Class OurmUserServiceImpl.
 */
@Slf4j
@Component(
    service = OurmUserService.class,
    property = {"service.pid=aem.core.services.ourmUsers"},
    configurationPid = "com.workday.community.aem.core.config.OurmDrupalConfig",
    configurationPolicy = ConfigurationPolicy.OPTIONAL
)
@Designate(ocd = OurmDrupalConfig.class)
public class OurmUserServiceImpl implements OurmUserService {

  /**
   * The gson service.
   */
  private final Gson gson = new Gson();

  /**
   * The ourm drupal config.
   */
  private OurmDrupalConfig ourmDrupalConfig;

  /**
   * {@inheritDoc}
   */
  @Activate
  @Modified
  @Override
  public void activate(OurmDrupalConfig config) {
    this.ourmDrupalConfig = config;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public JsonObject searchOurmUserList(String searchText) throws OurmException {

    String endpoint = this.ourmDrupalConfig.ourmDrupalRestRoot();
    String consumerKey = this.ourmDrupalConfig.ourmDrupalConsumerKey();
    String consumerSecret = this.ourmDrupalConfig.ourmDrupalConsumerSecret();
    String searchPath = this.ourmDrupalConfig.ourmDrupalUserSearchPath();
    if (StringUtils.isNotBlank(endpoint) && StringUtils.isNotBlank(consumerKey)
        && StringUtils.isNotBlank(consumerSecret) && StringUtils.isNotBlank(searchPath)) {
      try {
        String apiUrl = String.format("%s/%s", CommunityUtils.formUrl(endpoint, searchPath),
            URLEncoder.encode(searchText, StandardCharsets.UTF_8));
        String headerString =
            Oauth1Util.getHeader(GET_API, apiUrl, consumerKey, consumerSecret, new HashMap<>());
        log.info("OurmUserServiceImpl::searchOurmUserList - apiUrl {}", apiUrl);

        // Execute the request.
        String jsonResponse = RestApiUtil.doOurmGet(apiUrl, headerString);
        return gson.fromJson(jsonResponse, JsonObject.class);
      } catch (OurmException | InvalidKeyException | NoSuchAlgorithmException e) {
        String errorMessage = e.getMessage();
        throw new OurmException(
            String.format("Error Occurred in searchOurmUserList Method in OurmUserServiceImpl : %s",
                errorMessage));
      }
    }

    return new JsonObject();
  }
}
