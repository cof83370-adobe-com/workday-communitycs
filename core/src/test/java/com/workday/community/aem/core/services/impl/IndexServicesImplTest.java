package com.workday.community.aem.core.services.impl;

import com.day.cq.wcm.api.Page;
import com.workday.community.aem.core.constants.GlobalConstants;
import io.wcm.testing.mock.aem.junit5.AemContextExtension;
import org.apache.sling.event.jobs.JobManager;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

@ExtendWith({AemContextExtension.class, MockitoExtension.class})
class IndexServicesImplTest {
    private Page rootPage;
    @Mock
    JobManager jobManager;

    @InjectMocks
    IndexServicesImpl indexServices;

    @Test
    void indexPagesTest() {
        List<String> paths = List.of("/page/path");
        indexServices.indexPages(paths);
        verify(jobManager).addJob(eq(GlobalConstants.COMMUNITY_COVEO_JOB), anyMap());
    }
}