package com.workday.community.aem.core.schedulers;

import static org.apache.sling.jcr.resource.api.JcrResourceConstants.SLING_RESOURCE_TYPE_PROPERTY;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Property;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.Value;

import org.apache.commons.mail.Email;
import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.SimpleEmail;
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
import org.osgi.service.component.annotations.Reference;
import com.day.cq.mailer.MessageGateway;
import com.day.cq.mailer.MessageGatewayService;
import com.day.cq.search.QueryBuilder;
import com.workday.community.aem.core.config.ReviewNotificationSchedulerConfig;
import com.workday.community.aem.core.constants.GlobalConstants;
import com.workday.community.aem.core.exceptions.CacheException;
import com.workday.community.aem.core.services.CacheManagerService;
import com.workday.community.aem.core.services.QueryService;
import com.workday.community.aem.core.services.RunModeConfigService;

import io.wcm.testing.mock.aem.junit5.AemContext;
import io.wcm.testing.mock.aem.junit5.AemContextExtension;

import org.mockito.ArgumentCaptor;

import static com.day.cq.commons.jcr.JcrConstants.JCR_TITLE;

/**
 * The Class ReviewNotificationSchedulerTest.
 */
@ExtendWith(AemContextExtension.class)
@ExtendWith(MockitoExtension.class)
public class ReviewNotificationSchedulerTest {
	
	/** The context. */
    private final AemContext context = new AemContext(ResourceResolverType.JCR_MOCK);

    /** The session. */
    private final Session session = context.resourceResolver().adaptTo(Session.class);
    
    @InjectMocks
    ReviewNotificationScheduler revNotifScheduler;
    
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
    MessageGatewayService messageGatewayService;
    
    @Mock
    RunModeConfigService runModeConfigService;

    @Mock
    MessageGateway<Email> messageGatewaySimpleEmail;
    
    
    /** The review scheduler config. */
    private final ReviewNotificationSchedulerConfig revNotifSchedulerConfig = new ReviewNotificationSchedulerConfig() {

		@Override
		public String schedulerName() {
			return "ReviewNotificationScheduler";
		}

		@Override
		public String workflowNotificationCron() {
			return "0 0 18 * * ?";
		}

		@Override
		public boolean workflowNotificationReview10Months() {
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
    };
    
    /**
     * Setup.
     *
     * @throws LoginException the login exception
     */
    @BeforeEach
    void setup() throws LoginException {
		lenient().when(scheduler.EXPR(revNotifSchedulerConfig.workflowNotificationCron())).thenReturn(scheduleOptions);
		scheduler.schedule(ReviewNotificationScheduler.class, scheduleOptions);

		revNotifScheduler.activate(revNotifSchedulerConfig);
		revNotifScheduler.deactivate(revNotifSchedulerConfig);
		revNotifScheduler.modified(revNotifSchedulerConfig);
		
		revNotifScheduler.addScheduler(revNotifSchedulerConfig);
		revNotifScheduler.removeScheduler(revNotifSchedulerConfig);
		
		scheduler.unschedule(revNotifSchedulerConfig.schedulerName());

		context.load().json("/com/workday/community/aem/core/models/impl/ReviewNotificationSchedulerTestData.json",
				"/content");
		context.registerService(ResourceResolver.class, resolver);
		context.registerService(QueryService.class, queryService);
		
		lenient().when(messageGatewayService.getGateway(Email.class)).thenReturn(messageGatewaySimpleEmail);

	    context.registerService(MessageGatewayService.class, messageGatewayService);
    }
    
    @Test
    void run() throws CacheException, RepositoryException, EmailException {
		List<String> pathList = new ArrayList<>();
		pathList.add(
				"/content/workday-community/en-us/admin-tools/notifications/workflows/review-reminder-10-months/jcr:content");
		lenient().when(queryService.getReviewReminderPages()).thenReturn(pathList);
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
						GlobalConstants.COMMUNITY_CONTENT_NOTIFICATIONS_ROOT_PATH + "/workflows/review-reminder-10-months"))
				.thenReturn(resource2);
		Node nodeObj = mock(Node.class);
		assertNotNull(nodeObj);
		lenient().when(resource2.adaptTo(Node.class)).thenReturn(nodeObj);

		testProcessTextComponentFromEmailTemplate();

		revNotifScheduler.run();
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
				"/content/workday-community/en-us/admin-tools/notifications/workflows/review-reminder-10-months");
    }
    
    @Test
    public boolean isAuthorInstance() {
        return true;
    }
    
    @Test
    public final void testSendEmail() throws RepositoryException, EmailException {
    	Node nodeObj = mock(Node.class);
		Property prop1 = mock(Property.class);
		lenient().when(nodeObj.hasProperty(JCR_TITLE)).thenReturn(true);
		lenient().when(nodeObj.getProperty(anyString())).thenReturn(prop1);
		assertNotNull(prop1);
		lenient().when(prop1.getString()).thenReturn("title");
    	
        final String expectedMessage = "This is just a message";
        final String expectedSenderName = "John Smith";
        final String expectedSenderEmailAddress = "john@smith.com";
        //This subject is provided directly inside the sample emailtemplate
        final String expectedSubject = "Greetings";

        final Map<String, String> params = new HashMap<String, String>();
        params.put("message", expectedMessage);
        params.put("senderName", expectedSenderName);
        params.put("senderEmailAddress", expectedSenderEmailAddress);
        
        revNotifScheduler.sendEmail(expectedSenderEmailAddress, expectedMessage, nodeObj);
    }
}
