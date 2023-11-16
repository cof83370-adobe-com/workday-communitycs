package com.workday.community.aem.core.schedulers;

import static org.apache.sling.jcr.resource.api.JcrResourceConstants.SLING_RESOURCE_TYPE_PROPERTY;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Property;
import javax.jcr.RepositoryException;
import javax.jcr.Value;

import org.apache.commons.mail.EmailException;
import org.apache.sling.api.resource.LoginException;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.commons.scheduler.ScheduleOptions;
import org.apache.sling.commons.scheduler.Scheduler;
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
import com.day.cq.search.QueryBuilder;
import com.workday.community.aem.core.config.RetirementManagerSchedulerConfig;
import com.workday.community.aem.core.constants.GlobalConstants;
import com.workday.community.aem.core.constants.WorkflowConstants;
import com.workday.community.aem.core.exceptions.CacheException;
import com.workday.community.aem.core.services.CacheManagerService;
import com.workday.community.aem.core.services.QueryService;
import com.workday.community.aem.core.services.RunModeConfigService;

import io.wcm.testing.mock.aem.junit5.AemContext;
import io.wcm.testing.mock.aem.junit5.AemContextExtension;

import static com.day.cq.commons.jcr.JcrConstants.JCR_TITLE;

/**
 * The Class ReviewNotificationSchedulerTest.
 */
@ExtendWith(AemContextExtension.class)
@ExtendWith(MockitoExtension.class)
public class RetirementManagerSchedulerTest {
	
	/** The context. */
    private final AemContext context = new AemContext(ResourceResolverType.JCR_MOCK);
    
    @InjectMocks
    RetirementManagerScheduler revNotifScheduler;
    
    /** The scheduler options. */
    @Mock
    ScheduleOptions scheduleOptions;
    
    /** The scheduler. */
    @Mock
    Scheduler scheduler;
    
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
    
    
    /** The review scheduler config. */
    private final RetirementManagerSchedulerConfig revNotifSchedulerConfig = new RetirementManagerSchedulerConfig() {

		@Override
		public String workflowNotificationCron() {
			return "0 0 18 * * ?";
		}

		@Override
		public boolean enableWorkflowNotificationReview() {
			return true;
		}

		@Override
		public String authorDomain() {
			return "http://localhost:4502";
		}

		@Override
		public Class<? extends Annotation> annotationType() {
			return Annotation.class;
		}

		@Override
		public boolean enableWorkflowNotificationRetirement() {
			return true;
		}
    };
    
    /**
     * Setup.
     *
     * @throws LoginException the login exception
     */
    @BeforeEach
    void setup() throws LoginException {
		lenient().when(scheduler.EXPR(revNotifSchedulerConfig.workflowNotificationCron())).thenReturn(scheduleOptions);
		scheduler.schedule(RetirementManagerScheduler.class, scheduleOptions);

		revNotifScheduler.activate(revNotifSchedulerConfig);
		revNotifScheduler.deactivate(revNotifSchedulerConfig);
		
		revNotifScheduler.addScheduler(revNotifSchedulerConfig);
		
		scheduler.unschedule("RetirementManagerScheduler");

		context.load().json("/com/workday/community/aem/core/models/impl/RetirementManagerSchedulerTestData.json",
				"/content");
		context.registerService(ResourceResolver.class, resolver);
		context.registerService(QueryService.class, queryService);
		
		lenient().when(resolver.adaptTo(WorkflowSession.class)).thenReturn(workflowSession);
    }
    
    @Test
    void run() throws CacheException, RepositoryException, EmailException, WorkflowException {
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
		
		testStartWorkflow();

		revNotifScheduler.run();
    }
    
    @Test
    public final void testSendReviewNotification() {
        revNotifScheduler.sendReviewNotification(resolver);
    }
    
    @Test
    public final void testStartRetirementAndNotify() {
        revNotifScheduler.startRetirementAndNotify(resolver);
    }
    
    @Test
    public final void testSendNotification() {
    	List<String> paths = new ArrayList<>();
    	paths.add("/content/workday-community/en-us/test");
        revNotifScheduler.sendNotification(resolver, paths, "/workflows/retirement-notification/jcr:content/root/container/container/text", " to Retire in 30 Days", true);
    }
    
    @Test
    public String testProcessTextComponentFromEmailTemplate() throws RepositoryException {
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

		return revNotifScheduler.processTextComponentFromEmailTemplate(nodeIterator.nextNode(), nodeObj,
				"/content/workday-community/en-us/admin-tools/notifications/workflows/review-reminder");
    }
    
    @Test
    public final void testStartWorkflow() throws WorkflowException {
        revNotifScheduler.startWorkflow(resolver, WorkflowConstants.RETIREMENT_WORKFLOW, "/content/workday-community/en-us/test");
    }
}
