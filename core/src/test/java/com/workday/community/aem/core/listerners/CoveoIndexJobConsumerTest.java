package com.workday.community.aem.core.listerners;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

import com.workday.community.aem.core.listeners.CoveoIndexJobConsumer;
import com.workday.community.aem.core.services.CoveoPushApiService;
import com.workday.community.aem.core.services.ExtractPagePropertiesService;
import com.workday.community.aem.core.services.RunModeConfigService;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Stream;
import org.apache.http.HttpStatus;
import org.apache.sling.event.jobs.Job;
import org.apache.sling.event.jobs.consumer.JobConsumer.JobResult;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * The Class CoveoIndexJobConsumerTest.
 */
@ExtendWith(MockitoExtension.class)
public class CoveoIndexJobConsumerTest {

  /**
   * The CoveoIndexJobConsumer.
   */
  @InjectMocks
  private CoveoIndexJobConsumer consumer;

  /**
   * The CoveoPushApiService.
   */
  @Mock
  private CoveoPushApiService coveoPushService;

  /**
   * The RunModeConfigService.
   */
  @Mock
  private RunModeConfigService runModeConfigService;

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

  /**
   * Provides data to test indexing and deleting Coveo content.
   */
  private static Stream<Arguments> provideParameters() {
    return Stream.of(
        Arguments.of(HttpStatus.SC_ACCEPTED, JobResult.OK),
        Arguments.of(HttpStatus.SC_REQUEST_TOO_LONG, JobResult.FAILED)
    );
  }

  /**
   * Tests deleting Coveo content.
   */
  @ParameterizedTest
  @MethodSource("provideParameters")
  void testDeleteParameterized(int httpStatus, JobResult jobResult) {
    List<String> paths = new ArrayList<>(List.of("/sample/path"));

    String documentId = "https://www.test.link";

    when(job.getProperty("paths")).thenReturn(paths);
    when(job.getProperty("op")).thenReturn("delete");

    doReturn(documentId).when(runModeConfigService).getPublishInstanceDomain();
    doReturn(httpStatus).when(coveoPushService).callDeleteSingleItemUri(any());
    JobResult result = consumer.process(job);
    assertEquals(jobResult, result);
  }

  @ParameterizedTest
  @MethodSource("provideParameters")
  void testSuccessfulIndexing(int httpStatus, JobResult jobResult) {
    List<String> paths = new ArrayList<>(List.of("/sample/path"));

    List<Object> payload = new ArrayList<>();
    HashMap<String, Object> property = new HashMap<>();
    property.put("pageTitle", "Sample page");
    payload.add(property);

    when(job.getProperty("paths")).thenReturn(paths);
    when(job.getProperty("op")).thenReturn("index");

    doReturn(property).when(extractPagePropertiesService).extractPageProperties(any());
    doReturn(httpStatus).when(coveoPushService).indexItems(payload);
    JobResult result = consumer.process(job);
    assertEquals(jobResult, result);
  }

  /**
   * Test process job failed.
   */
  @Test
  void testProcessJobFail() {
    doReturn(null).when(job).getProperty("op");
    doReturn(null).when(job).getProperty("paths");
    JobResult result = consumer.process(job);
    assertEquals(JobResult.FAILED, result);
  }

}
