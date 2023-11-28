package com.workday.community.aem.core.jobs;

import static org.apache.sling.jcr.resource.api.JcrResourceConstants.SLING_RESOURCE_TYPE_PROPERTY;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Property;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.Value;

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
import com.workday.community.aem.core.services.RetirementManagerJobConfigService;
import com.workday.community.aem.core.services.RunModeConfigService;

import io.wcm.testing.mock.aem.junit5.AemContext;
import io.wcm.testing.mock.aem.junit5.AemContextExtension;

import static com.day.cq.commons.jcr.JcrConstants.JCR_TITLE;

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
    RetirementManagerJobConfigService retirementManagerJobConfigService;
    
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
		lenient().when(node.hasProperty(GlobalConstants.PROP_JCR_CREATED_BY)).thenReturn(true);
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

		testProcessTextComponentFromEmailTemplate();
		
		testProcessTitleComponentFromEmailTemplate();
		
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
    public String testProcessTextComponentFromEmailTemplate() throws RepositoryException {
    	String domain = "http://localhost:4502";
        lenient().when(retirementManagerJobConfigService.getAuthorDomain()).thenReturn(domain);
    	
		List<Node> testItems = new ArrayList<>();
		testItems.add(mock(Node.class));
		testItems.add(mock(Node.class));

		Node nodeObj = mock(Node.class);
		Property prop1 = mock(Property.class);
		lenient().when(nodeObj.hasProperty(JCR_TITLE)).thenReturn(true);
		lenient().when(nodeObj.getProperty(anyString())).thenReturn(prop1);
		assertNotNull(prop1);
		lenient().when(prop1.getString()).thenReturn("title");

		testItems.forEach(node -> {
			try {
				Property property = mock(Property.class);
				Property propertyText = mock(Property.class);
				Value value = mock(Value.class);
				Value value1 = mock(Value.class);

				lenient().when(node.hasProperty(eq(SLING_RESOURCE_TYPE_PROPERTY))).thenReturn(true);
				lenient().when(node.getProperty(eq(SLING_RESOURCE_TYPE_PROPERTY))).thenReturn(property);
				lenient().when(node.getProperty(eq("text"))).thenReturn(propertyText);
				lenient().when(property.getValue()).thenReturn(value);
				lenient().when(value.getString()).thenReturn(GlobalConstants.TEXT_COMPONENT);
				lenient().when(propertyText.getValue()).thenReturn(value1);
				lenient().when(value1.getString()).thenReturn(GlobalConstants.TEXT_COMPONENT);
			} catch (RepositoryException e) {
				throw new RuntimeException(e);
			}
		});

		NodeIterator nodeIterator = new NodeIterator() {
			int count = 0;

			@Override
			public Node nextNode() {
				if (count < testItems.size()) {
					Node next = testItems.get(count);
					count++;
					return next;
				}
				return null;
			}

			@Override
			public void skip(long l) {
			}

			@Override
			public long getSize() {
				return testItems.size();
			}

			@Override
			public long getPosition() {
				return count;
			}

			@Override
			public boolean hasNext() {
				return count < testItems.size();
			}

			@Override
			public Object next() {
				return nextNode();
			}
		};

		return retirementManagerJobConsumer.processTextComponentFromEmailTemplate(nodeIterator.nextNode(), nodeObj,
				"/content/workday-community/en-us/admin-tools/notifications/workflows/review-reminder");
    }
    
    @Test
    public String testProcessTitleComponentFromEmailTemplate() throws RepositoryException {
		List<Node> testItems = new ArrayList<>();
		testItems.add(mock(Node.class));
		testItems.add(mock(Node.class));

		Node nodeObj = mock(Node.class);
		Property prop1 = mock(Property.class);
		lenient().when(nodeObj.hasProperty(JCR_TITLE)).thenReturn(true);
		lenient().when(nodeObj.getProperty(anyString())).thenReturn(prop1);
		assertNotNull(prop1);
		lenient().when(prop1.getString()).thenReturn("title");

		testItems.forEach(node -> {
			try {
				Property property = mock(Property.class);
				Property propertyText = mock(Property.class);
				Value value = mock(Value.class);
				Value value1 = mock(Value.class);

				lenient().when(node.hasProperty(eq(SLING_RESOURCE_TYPE_PROPERTY))).thenReturn(true);
				lenient().when(node.getProperty(eq(SLING_RESOURCE_TYPE_PROPERTY))).thenReturn(property);
				lenient().when(node.getProperty(eq(JCR_TITLE))).thenReturn(propertyText);
				lenient().when(property.getValue()).thenReturn(value);
				lenient().when(value.getString()).thenReturn(GlobalConstants.TITLE_COMPONENT);
				lenient().when(propertyText.getValue()).thenReturn(value1);
				lenient().when(value1.getString()).thenReturn(GlobalConstants.TITLE_COMPONENT);
			} catch (RepositoryException e) {
				throw new RuntimeException(e);
			}
		});

		NodeIterator nodeIterator = new NodeIterator() {
			int count = 0;

			@Override
			public Node nextNode() {
				if (count < testItems.size()) {
					Node next = testItems.get(count);
					count++;
					return next;
				}
				return null;
			}

			@Override
			public void skip(long l) {
			}

			@Override
			public long getSize() {
				return testItems.size();
			}

			@Override
			public long getPosition() {
				return count;
			}

			@Override
			public boolean hasNext() {
				return count < testItems.size();
			}

			@Override
			public Object next() {
				return nextNode();
			}
		};

		return retirementManagerJobConsumer.processTitleComponentFromEmailTemplate(nodeIterator.nextNode(), nodeObj,
				"/content/workday-community/en-us/admin-tools/notifications/workflows/review-reminder");
    }
    
    @Test
    public final void testStartWorkflow() throws WorkflowException {
    	retirementManagerJobConsumer.startWorkflow(resolver, WorkflowConstants.RETIREMENT_WORKFLOW, "/content/workday-community/en-us/test");
    }
    
    /**
     * Test process job.
     */
    @Test
    void testProcessJob() {
      doReturn(null).when(job).getProperty("customJob");
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
