package com.workday.community.aem.core.listerners;

import static com.workday.community.aem.core.constants.WorkflowConstants.RETIREMENT_STATUS_PROP;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.day.cq.wcm.api.Page;
import com.day.cq.wcm.api.Template;
import com.workday.community.aem.core.constants.GlobalConstants;
import com.workday.community.aem.core.exceptions.CacheException;
import com.workday.community.aem.core.exceptions.DrupalException;
import com.workday.community.aem.core.listeners.PageUpdateJobConsumer;
import com.workday.community.aem.core.pojos.restclient.ApiResponse;
import com.workday.community.aem.core.services.CacheManagerService;
import com.workday.community.aem.core.services.CoveoPushApiService;
import com.workday.community.aem.core.services.DrupalService;
import com.workday.community.aem.core.services.ExtractPagePropertiesService;
import com.workday.community.aem.core.services.RunModeConfigService;
import java.util.Collections;
import java.util.List;
import org.apache.http.HttpStatus;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.event.jobs.Job;
import org.apache.sling.event.jobs.consumer.JobConsumer.JobResult;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * The Class CoveoIndexJobConsumerTest.
 */
@ExtendWith(MockitoExtension.class)
public class PageUpdateJobConsumerTest {

  /**
   * The path to the Community content root.
   */
   static final String COMMUNITY_EVENT_PAGE_PATH = "/content/workday-community/en-us/event1/event2";

  /**
   * The PageUpdateJobConsumer.
   */
  @InjectMocks
  private PageUpdateJobConsumer pageUpdateJobConsumer;

  /**
   * The CoveoPushApiService.
   */
  @Mock
  private CoveoPushApiService coveoPushService;

  /**
   * The ExtractPagePropertiesService.
   */
  @Mock
  private ExtractPagePropertiesService extractPagePropertiesService;

  /**
   * Mocked Job object.
   */
  @Mock
  private Job job;

  @Mock
  private CacheManagerService cacheManager;

  @Mock
  private RunModeConfigService runModeConfigService;

  @Mock
  private DrupalService drupalService;

  @Mock
  private ResourceResolver resourceResolver;

  @Mock
  private Resource resource;

  @Mock
  private Page page;

  @Mock
  private ValueMap valueMap;

  @Mock
  private Template eventTemplate;

  public void setUp() throws CacheException {
    lenient().when(cacheManager.getServiceResolver(anyString())).thenReturn(resourceResolver);
    lenient().when(runModeConfigService.getPublishInstanceDomain()).thenReturn("https://localhost:3503/");
    String path = COMMUNITY_EVENT_PAGE_PATH;
    List<String> pathsList = Collections.singletonList(path);

    when(job.getProperty("paths", List.class)).thenReturn(pathsList);
    when(resourceResolver.getResource(path)).thenReturn(resource);
    when(resource.adaptTo(Page.class)).thenReturn(page);
    when(page.getName()).thenReturn("Test Page");
    when(page.getPath()).thenReturn(path);
    when(page.getTemplate()).thenReturn(eventTemplate);
    when(eventTemplate.getPath()).thenReturn(GlobalConstants.EVENTS_TEMPLATE_PATH);
    when(page.getProperties()).thenReturn(valueMap);
    when(valueMap.get(RETIREMENT_STATUS_PROP, "")).thenReturn("active");
    when(valueMap.get(GlobalConstants.JCR_UUID, String.class)).thenReturn("12345");
    when(valueMap.get(GlobalConstants.CQ_TAGS, String[].class)).thenReturn(new String[] {"tag1", "tag2"});
    when(valueMap.get(GlobalConstants.TAG_PROPERTY_ACCESS_CONTROL, String[].class)).thenReturn(
        new String[] {"acl1", "acl2"});
    when(valueMap.get(GlobalConstants.PROP_AUTHOR, String.class)).thenReturn("John Doe");
  }


  @Test
  public void testProcessPageActivationSuccess() throws DrupalException, CacheException {

    setUp();
    when(job.getProperty("op")).thenReturn("Activate");
    ApiResponse apiResponse = new ApiResponse();
    apiResponse.setResponseBody("success");
    apiResponse.setResponseCode(HttpStatus.SC_OK);
    when(drupalService.createOrUpdateEntity(any())).thenReturn(apiResponse);
    JobResult result = pageUpdateJobConsumer.process(job);

    verify(drupalService, times(1)).createOrUpdateEntity(any());
    assert result == JobResult.OK;
  }

  @Test
  public void testProcessPageDeActivationSuccess() throws DrupalException, CacheException {
    setUp();
    when(job.getProperty("op")).thenReturn("Deactivate");
    ApiResponse apiResponse = new ApiResponse();
    apiResponse.setResponseBody("success");
    apiResponse.setResponseCode(HttpStatus.SC_OK);
    when(drupalService.createOrUpdateEntity(any())).thenReturn(apiResponse);
    JobResult result = pageUpdateJobConsumer.process(job);

    verify(drupalService, times(1)).createOrUpdateEntity(any());
    assert result == JobResult.OK;
  }

  @Test
  public void testProcessPageDeletionSuccess() throws DrupalException, CacheException {

    lenient().when(cacheManager.getServiceResolver(anyString())).thenReturn(resourceResolver);
    lenient().when(runModeConfigService.getPublishInstanceDomain()).thenReturn("https://localhost:3503/");
    String path = COMMUNITY_EVENT_PAGE_PATH;
    List<String> pathsList = Collections.singletonList(path);
    when(job.getProperty("paths", List.class)).thenReturn(pathsList);
    when(job.getProperty("op")).thenReturn("Delete");

    ApiResponse apiResponse = new ApiResponse();
    apiResponse.setResponseBody("success");
    apiResponse.setResponseCode(HttpStatus.SC_NO_CONTENT);
    when(drupalService.deleteEntity(any())).thenReturn(apiResponse);
    JobResult result = pageUpdateJobConsumer.process(job);

    verify(drupalService, times(1)).deleteEntity(any());
    assert result == JobResult.OK;
  }

  @AfterEach
  public void after() {
    resourceResolver.close();
  }
}
