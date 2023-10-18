package com.workday.community.aem.core.servlets;

import static com.workday.community.aem.core.constants.RestApiConstants.BEARER_TOKEN;

import com.adobe.granite.ui.components.ds.DataSource;
import com.adobe.granite.ui.components.ds.SimpleDataSource;
import com.adobe.granite.ui.components.ds.ValueMapResource;
import com.day.crx.JcrConstants;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.workday.community.aem.core.pojos.EventTypes;
import com.workday.community.aem.core.services.SearchApiConfigService;
import com.workday.community.aem.core.services.SnapService;
import com.workday.community.aem.core.services.UserService;
import com.workday.community.aem.core.utils.CoveoUtils;
import com.workday.community.aem.core.utils.ServletCallback;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import javax.servlet.Servlet;
import javax.servlet.ServletException;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceMetadata;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.api.servlets.HttpConstants;
import org.apache.sling.api.servlets.SlingSafeMethodsServlet;
import org.apache.sling.api.wrappers.ValueMapDecorator;
import org.osgi.framework.Constants;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * The Class CoveoEventsTypeServlet.
 *
 * @author Thabrez
 */
@Slf4j
@Component(service = Servlet.class, property = {
    Constants.SERVICE_DESCRIPTION + "= Coveo Events type Dropdown Service",
    "sling.servlet.paths=" + "/bin/eventTypes", "sling.servlet.methods=" + HttpConstants.METHOD_GET
})
public class CoveoEventsTypeServlet extends SlingSafeMethodsServlet {

  private static final String EVENT_TYPE_CRITERIA = "?field=@commoneventtype";

  private final transient Gson gson = new Gson();

  private transient ObjectMapper objectMapper = new ObjectMapper();

  @Reference
  private transient SearchApiConfigService searchApiConfigService;

  @Reference
  private transient SnapService snapService;

  @Reference
  private transient UserService userService;

  public void setObjectMapper(ObjectMapper objectMapper) {
    this.objectMapper = objectMapper;
  }

  /**
   * Do get.
   *
   * @param request  the request
   * @param response the response
   */
  @Override
  protected void doGet(SlingHttpServletRequest request, SlingHttpServletResponse response)
      throws ServletException, IOException {

    ServletCallback callback =
        (SlingHttpServletRequest req, SlingHttpServletResponse res, String body) -> {
          List<Resource> resourceList = new ArrayList<>();

          try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            String token = gson.fromJson(body, JsonObject.class).get("searchToken").getAsString();
            EventTypes eventTypes = getEventTypes(httpClient, token);

            if (eventTypes != null && !eventTypes.getValues().isEmpty()) {
              eventTypes.getValues().forEach(value -> {
                ValueMap valueMap = new ValueMapDecorator(new HashMap<>());
                valueMap.put("value", value.getLookupValue());
                valueMap.put("text", value.getValue());
                resourceList.add(new ValueMapResource(request.getResourceResolver(),
                    new ResourceMetadata(), JcrConstants.NT_UNSTRUCTURED, valueMap));
              });
            }

            // Create a DataSource that is used to populate the drop-down control.
            DataSource dataSource = new SimpleDataSource(resourceList.iterator());
            request.setAttribute(DataSource.class.getName(), dataSource);
          } catch (IOException exception) {
            log.error("Error Occurred in DoGet Method in CoveoEventsTypeServlet : {}",
                exception.getMessage());
          }

          // Create a DataSource that is used to populate the drop-down control.
          DataSource dataSource = new SimpleDataSource(resourceList.iterator());
          request.setAttribute(DataSource.class.getName(), dataSource);
          return null;
        };

    CoveoUtils.executeSearchForCallback(request,
        response, searchApiConfigService, snapService, userService,
        gson, objectMapper, callback);
  }

  private EventTypes getEventTypes(CloseableHttpClient httpClient, String token)
      throws IOException {
    String endpoint = this.searchApiConfigService.getSearchFieldLookupApi();
    endpoint += EVENT_TYPE_CRITERIA;
    HttpGet request = new HttpGet(endpoint);
    request.addHeader(HttpConstants.HEADER_ACCEPT, ContentType.APPLICATION_JSON.getMimeType());
    request.addHeader(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.getMimeType());
    request.addHeader(HttpHeaders.AUTHORIZATION, BEARER_TOKEN.token(token));
    HttpResponse response = httpClient.execute(request);
    int status = response.getStatusLine().getStatusCode();
    if (status == HttpStatus.SC_OK) {
      return objectMapper.readValue(response.getEntity().getContent(),
          EventTypes.class);
    }

    log.error("Retrieve event type returns empty");
    return new EventTypes();
  }
}
