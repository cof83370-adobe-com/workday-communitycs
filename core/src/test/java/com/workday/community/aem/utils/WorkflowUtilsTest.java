package com.workday.community.aem.utils;

import static com.day.cq.commons.jcr.JcrConstants.JCR_TITLE;
import static org.apache.sling.jcr.resource.api.JcrResourceConstants.SLING_RESOURCE_TYPE_PROPERTY;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;

import com.adobe.granite.workflow.WorkflowSession;
import com.workday.community.aem.core.constants.GlobalConstants;
import com.workday.community.aem.core.services.EmailService;
import com.workday.community.aem.core.services.WorkflowConfigService;
import com.workday.community.aem.core.utils.WorkflowUtils;

import io.wcm.testing.mock.aem.junit5.AemContext;
import io.wcm.testing.mock.aem.junit5.AemContextExtension;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Property;
import javax.jcr.RepositoryException;
import javax.jcr.Value;

import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.testing.mock.sling.ResourceResolverType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * The Class WorkflowUtilsTest.
 */
@ExtendWith(AemContextExtension.class)
@ExtendWith({MockitoExtension.class})
public class WorkflowUtilsTest {
	
 /**
  * The context.
  */
  private final AemContext context = new AemContext(ResourceResolverType.JCR_MOCK);
	
  /**
   * The resolver.
   */
  @Mock
  private ResourceResolver resolver;	

  @Mock
  private WorkflowConfigService workflowConfigService;
  
  @Mock
  private EmailService emailService;
  
  /**
   * The workflow session.
   */
  @Mock
  private WorkflowSession workflowSession;

  @BeforeEach
  public void setup() throws RepositoryException {
	  context.load().json("/com/workday/community/aem/core/models/impl/WorkflowUtilsTestData.json",
				"/content");
	  context.registerService(ResourceResolver.class, resolver);
	  
	  lenient().when(workflowSession.adaptTo(ResourceResolver.class)).thenReturn(resolver);
  }
  
  @Test
  public final void testSendNotification() throws RepositoryException {
  	List<String> paths = new ArrayList<>();
  	paths.add("/content/workday-community/en-us/test");
  	
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
	
	testProcessTextComponentFromEmailTemplate();
	testProcessTitleComponentFromEmailTemplate();
	
	WorkflowUtils.sendNotification("test@oworkday.com", resolver, "/workflows/publish-notification/jcr:content/root/container/container/text", "Page Publish", "/content/process-publish-content", node, workflowConfigService, emailService);
  }
  
  @Test
  public String testProcessTextComponentFromEmailTemplate() throws RepositoryException {
  	String domain = "http://localhost:4502";
      lenient().when(workflowConfigService.getAuthorDomain()).thenReturn(domain);
  	
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

		return WorkflowUtils.processTextComponentFromEmailTemplate(nodeIterator.nextNode(), nodeObj,
				"/content/workday-community/en-us/admin-tools/notifications/workflows/publish-notification", workflowConfigService, emailService);
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

		return WorkflowUtils.processTitleComponentFromEmailTemplate(nodeIterator.nextNode(), nodeObj,
				"/content/workday-community/en-us/admin-tools/notifications/workflows/publish-notification", workflowConfigService, emailService);
  }
}
