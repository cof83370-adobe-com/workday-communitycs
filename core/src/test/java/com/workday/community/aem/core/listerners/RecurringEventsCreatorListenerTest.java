package com.workday.community.aem.core.listerners;

import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Calendar;

import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.Session;

import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.testing.mock.sling.ResourceResolverType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.day.cq.wcm.api.Page;
import com.day.cq.wcm.api.PageManager;
import com.workday.community.aem.core.listeners.RecurringEventsCreatorListener;
import com.workday.community.aem.core.utils.ResolverUtil;

import io.wcm.testing.mock.aem.junit5.AemContext;
import io.wcm.testing.mock.aem.junit5.AemContextExtension;

/**
 * The Class RecurringEventsCreatorListenerTest.
 */
@ExtendWith({ AemContextExtension.class, MockitoExtension.class })
public class RecurringEventsCreatorListenerTest {

        /**
         * The PageResourceListener.
         */
        @InjectMocks
        RecurringEventsCreatorListener recurringEventsCreatorListener;

        /**
         * The resolver factory.
         */
        @Mock
        private ResourceResolverFactory resolverFactory;

        /** The mocked event node. */
        @Mock
        private Node mockedEventNode;

        /** The value map. */
        @Mock
        private ValueMap valueMap;

        /** The page manager. */
        @Mock
        private PageManager pageManager;

        /** The session. */
        @Mock
        private Session session;

        /** The expected event node. */
        Node expectedEventNode;

        /** The expected event map. */
        ValueMap expectedEventMap;

        /**
         * The resolver.
         */
        @Mock
        private ResourceResolver resolver;

        /** The context. */
        private final AemContext context = new AemContext(ResourceResolverType.RESOURCERESOLVER_MOCK);

        /**
         * Sets the up.
         *
         * @throws Exception the exception
         */
        @BeforeEach
        public void setUp() throws Exception {
                context.load().json(
                                "/com/workday/community/aem/core/models/impl/RecurringEventsCreatorTestData.json",
                                "/content");
                Page currentPage = context.currentResource("/content/sample-recurring-events-page").adaptTo(Page.class);
                context.registerService(Page.class, currentPage);
                context.registerService(ResourceResolver.class, resolver);
                when(ResolverUtil.newResolver(resolverFactory, "workday-community-administrative-service"))
                                .thenReturn(resolver);
                Resource resource = mock(Resource.class);
                expectedEventNode = mock(Node.class);
                expectedEventMap = mock(ValueMap.class);
                lenient().when(resolver.getResource(context.currentPage().getContentResource().getPath()))
                                .thenReturn(resource);
                lenient().when(resolver.adaptTo(PageManager.class)).thenReturn(pageManager);
                lenient().when(resource.adaptTo(Node.class)).thenReturn(expectedEventNode);
                lenient().when(resource.adaptTo(ValueMap.class)).thenReturn(expectedEventMap);
        }

        /**
         * Test generate recurring event pages.
         *
         * @throws Exception the exception
         */
        @Test
        void testGenerateRecurringEventPages() throws Exception {
                Property prop = mock(Property.class);
                lenient().when(expectedEventNode.hasProperty("recurringEvents")).thenReturn(true);
                lenient().when(expectedEventNode.getProperty("recurringEvents")).thenReturn(prop);
                lenient().when(prop.getString()).thenReturn("true");

                Property prop2 = mock(Property.class);
                lenient().when(expectedEventNode.getProperty("jcr:title")).thenReturn(prop2);
                lenient().when(prop2.getString()).thenReturn("recurring page title");

                Property prop3 = mock(Property.class);
                lenient().when(expectedEventNode.getProperty("eventFrequency")).thenReturn(prop3);
                lenient().when(prop3.getString()).thenReturn("monthly");

                Property prop4 = mock(Property.class);
                lenient().when(expectedEventNode.getProperty("eventStartDate")).thenReturn(prop4);
                lenient().when(prop4.getDate()).thenReturn(Calendar.getInstance());

                Page newPage = mock(Page.class);
                lenient().when(pageManager.create(anyString(), anyString(), anyString(), anyString(), anyBoolean()))
                                .thenReturn(newPage);
                String newlyCreatedPagePath = "/content/wd-coomunity/en-us/events/test-11-10-2023";
                lenient().when(newPage.getPath()).thenReturn(newlyCreatedPagePath);
                Node expectedNewEventNode = mock(Node.class);
                Resource newPageresource = mock(Resource.class);
                lenient().when(resolver.resolve(anyString())).thenReturn(newPageresource);
                lenient().when(newPageresource.adaptTo(Node.class)).thenReturn(expectedNewEventNode);
                String[] eventFormatTags = { "event:event-format/webinar" };
                lenient().when(expectedEventMap.get("eventFormat", String[].class)).thenReturn(eventFormatTags);

                lenient().when(expectedNewEventNode.getSession()).thenReturn(session);
                recurringEventsCreatorListener
                                .generateRecurringEventPages(context.currentPage().getContentResource().getPath());
                verify(resolver).close();
        }

     
}
