package com.workday.community.aem.core.services.impl;

import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.workday.community.aem.core.constants.GlobalConstants;
import com.workday.community.aem.core.services.CoveoIndexApiConfigService;
import io.wcm.testing.mock.aem.junit5.AemContextExtension;
import java.util.ArrayList;
import java.util.List;
import org.apache.sling.event.jobs.JobManager;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * The Class IndexServicesImplTest.
 */
@ExtendWith({AemContextExtension.class, MockitoExtension.class})
class IndexServicesImplTest {

  /**
   * The JobManager service.
   */
  @Mock
  JobManager jobManager;

  /**
   * The IndexServicesImpl service.
   */
  @InjectMocks
  IndexServicesImpl indexServices;

  /**
   * The CoveoIndexApiConfigService service.
   */
  @Mock
  CoveoIndexApiConfigService coveoIndexApiConfigService;

  /**
   * Test indexPages with data.
   */
  @Test
  void indexPagesTest() {
    List<String> paths = new ArrayList<String>();
    ArrayList<String> processed = new ArrayList<String>();
    paths.add("/page/path");
    processed.add("/page/path");
    when(coveoIndexApiConfigService.getBatchSize()).thenReturn(50);
    indexServices.indexPages(paths);
    verify(jobManager).addJob(eq(GlobalConstants.COMMUNITY_COVEO_JOB), anyMap());
  }

  /**
   * Test indexPages without data.
   */
  @Test
  void indexPagesEmptyTest() {
    List<String> paths = new ArrayList<String>();
    when(coveoIndexApiConfigService.getBatchSize()).thenReturn(50);
    indexServices.indexPages(paths);
    verify(jobManager, never()).addJob(eq(GlobalConstants.COMMUNITY_COVEO_JOB), anyMap());
  }
}
