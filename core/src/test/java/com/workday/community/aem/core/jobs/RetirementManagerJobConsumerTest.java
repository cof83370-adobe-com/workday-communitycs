package com.workday.community.aem.core.jobs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.apache.commons.mail.EmailException;
import org.apache.sling.api.resource.LoginException;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.event.jobs.Job;
import org.apache.sling.event.jobs.consumer.JobConsumer.JobResult;
import org.apache.sling.testing.mock.sling.ResourceResolverType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.adobe.granite.workflow.WorkflowException;
import com.adobe.granite.workflow.WorkflowSession;
import com.adobe.granite.workflow.exec.WorkflowData;
import com.adobe.granite.workflow.model.WorkflowModel;
import com.day.cq.replication.ReplicationStatus;
import com.day.cq.replication.Replicator;
import com.day.cq.search.QueryBuilder;
import com.day.cq.wcm.api.Page;
import com.day.cq.wcm.api.PageManager;
import com.workday.community.aem.core.constants.GlobalConstants;
import com.workday.community.aem.core.constants.WorkflowConstants;
import com.workday.community.aem.core.exceptions.CacheException;
import com.workday.community.aem.core.services.CacheManagerService;
import com.workday.community.aem.core.services.QueryService;
import com.workday.community.aem.core.services.WorkflowConfigService;
import com.workday.community.aem.core.services.RunModeConfigService;

import io.wcm.testing.mock.aem.junit5.AemContext;
import io.wcm.testing.mock.aem.junit5.AemContextExtension;

/**
 * The Class ReviewNotificationSchedulerTest.
 */
@ExtendWith(AemContextExtension.class)
@ExtendWith(MockitoExtension.class)
public class RetirementManagerJobConsumerTest {
	
	/** The context. */
    private final AemContext context = new AemContext(ResourceResolverType.JCR_MOCK);
    
    @InjectMocks
    RetirementManagerJobConsumer retirementManagerJobConsumer;
    
    /**
     * Mocked Job object.
     */
    @Mock
    private Job job;
    
    /** The resolver. */
    @Mock
    private ResourceResolver resolver;
    
    /** The query service. */
    @Mock
    private QueryService queryService;
    
    /** The cache manager. */
    @Mock
    CacheManagerService cacheManager;
    
    @Mock
    QueryBuilder queryBuilder;
    
    /**
     * The replicator.
     */
    @Mock
    private Replicator replicator;
    
    /**
     * The replication status.
     */
    @Mock
    private ReplicationStatus repStatus;
    
    @Mock
    WorkflowConfigService workflowConfigService;
    
    @Mock
    RunModeConfigService runModeConfigService;
    
    /**
     * The workflow session.
     */
    @Mock
    private WorkflowSession workflowSession;
    
    /**
     * The workflow model.
     */
    @Mock
    private WorkflowModel workflowModel;
    
    /**
     * The workflow data.
     */
    @Mock
    private WorkflowData workflowData;
    
    /**
     * Setup.
     *
     * @throws LoginException the login exception
     */
    @BeforeEach
    void setup() throws LoginException {
		context.load().json("/com/workday/community/aem/core/models/impl/RetirementManagerJobConsumerTestData.json",
				"/content");
		context.registerService(ResourceResolver.class, resolver);
		context.registerService(QueryService.class, queryService);
		
		lenient().when(resolver.adaptTo(WorkflowSession.class)).thenReturn(workflowSession);
    }
    
    @Test
    void testRunJob() throws CacheException, RepositoryException, EmailException, WorkflowException {
		List<String> pathList = new ArrayList<>();
		pathList.add(
				"/content/workday-community/en-us/admin-tools/notifications/workflows/review-reminder/jcr:content");
		lenient().when(queryService.getPagesDueTodayByDateProp("jcr:content/reviewReminderDate")).thenReturn(pathList);
		lenient().when(cacheManager.getServiceResolver(anyString())).thenReturn(resolver);
		Resource resource = mock(Resource.class);
		lenient().when(resolver.getResource(anyString())).thenReturn(resource);
		Node node = mock(Node.class);
		lenient().when(resource.adaptTo(Node.class)).thenReturn(node);
		Property prop1 = mock(Property.class);
		lenient().when(resource.adaptTo(Node.class)).thenReturn(node);
		lenient().when(node.hasProperty(GlobalConstants.PROP_AUTHOR)).thenReturn(true);
		lenient().when(node.getProperty(anyString())).thenReturn(prop1);
		assertNotNull(prop1);
		lenient().when(prop1.getString()).thenReturn("test@user.com");

		Pattern pattern = mock(Pattern.class);
		Matcher matcher = mock(Matcher.class);
		lenient().when(pattern.matcher("test@user.com")).thenReturn(matcher);
		lenient().when(matcher.matches()).thenReturn(true);

		Resource resource2 = mock(Resource.class);
		lenient()
				.when(resolver.getResource(
						GlobalConstants.COMMUNITY_CONTENT_NOTIFICATIONS_ROOT_PATH + "/workflows/review-reminder"))
				.thenReturn(resource2);
		Node nodeObj = mock(Node.class);
		assertNotNull(nodeObj);
		lenient().when(resource2.adaptTo(Node.class)).thenReturn(nodeObj);
		
		testSendReviewNotification();
		
		testStartRetirementAndNotify();
		
		testSendNotification();
		
		testStartWorkflow();

		retirementManagerJobConsumer.runJob();
    }
    
    @Test
    public final void testSendReviewNotification() {
    	retirementManagerJobConsumer.sendReviewNotification(resolver);
    }
    
    @Test
    public final void testStartRetirementAndNotify() {
    	retirementManagerJobConsumer.startRetirementAndNotify(resolver);
    }
    
    @Test
    public final void testSendNotification() {
    	List<String> paths = new ArrayList<>();
    	paths.add("/content/workday-community/en-us/test");
    	retirementManagerJobConsumer.sendNotification(resolver, paths, "/workflows/retirement-notification/jcr:content/root/container/container/text", " to Retire in 30 Days", true);
    }
    
    @Test
    public final void testStartWorkflow() throws WorkflowException {
    	retirementManagerJobConsumer.startWorkflow(resolver, WorkflowConstants.RETIREMENT_WORKFLOW, "/content/workday-community/en-us/test");
    }
    
    /**
     * Test process job.
     */
    @Test
    void testProcessJobSuccess() {
      lenient().when(job.getProperty("jobTimestamp")).thenReturn(true);
      JobResult result = retirementManagerJobConsumer.process(job);
      assertEquals(JobResult.OK, result);
    }
    
    /**
     * Test archive content.
     *
     * @throws Exception the exception
     */
    @Test
    void testArchiveContent() throws Exception {
      Session session = mock(Session.class);
      lenient().when(resolver.adaptTo(Session.class)).thenReturn(session);
      PageManager pm = mock(PageManager.class);
      lenient().when(resolver.adaptTo(PageManager.class)).thenReturn(pm);
      Page page = mock(Page.class);
      lenient().when(pm.getPage("/content/workday-community/en-us/test")).thenReturn(page);
      Resource resource = mock(Resource.class);
      lenient().when(resolver.getResource(anyString())).thenReturn(resource);
      lenient().when(replicator.getReplicationStatus(session, "/content/workday-community/en-us/test")).thenReturn(repStatus);
      lenient().when(repStatus.isActivated()).thenReturn(true);
      
  	  retirementManagerJobConsumer.archiveContent(resolver);
      assertNotNull(session);
      assertNotNull(replicator);
      assertNotNull(repStatus);
    }
}
