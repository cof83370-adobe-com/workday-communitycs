package com.workday.community.aem.core.listerners;

import static org.mockito.Mockito.when;

import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.apache.sling.testing.mock.sling.ResourceResolverType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.day.cq.wcm.api.Page;
import com.workday.community.aem.core.listeners.PageResourceListener;
import com.workday.community.aem.core.services.QueryService;
import com.workday.community.aem.core.utils.ResolverUtil;

import io.wcm.testing.mock.aem.junit5.AemContext;
import io.wcm.testing.mock.aem.junit5.AemContextExtension;

/**
 * The Class PageResourceListenerTest.
 */
@ExtendWith({ AemContextExtension.class, MockitoExtension.class })
public class PageResourceListenerTest {
    /**
     * The PageResourceListener.
     */
    @InjectMocks
    PageResourceListener pageResourceListener;

    /**
     * The resolver factory.
     */
    @Mock
    private ResourceResolverFactory resolverFactory;

    /**
     * The resolver.
     */
    @Mock
    private ResourceResolver resolver;

    /**
     * The query service.
     */
    @Mock
    QueryService queryService;

    /**
     * The context.
     */
    private final AemContext context = new AemContext(ResourceResolverType.RESOURCERESOLVER_MOCK);

    /**
     * Sets the up.
     *
     * @throws Exception the exception
     */
    @BeforeEach
    public void setUp() throws Exception {
        context.load().json("/com/workday/community/aem/core/models/impl/BookOperationsServiceImplTestData.json",
                "/content");
        Page currentPage = context.currentResource("/content/book-faq-page").adaptTo(Page.class);
        context.registerService(Page.class, currentPage);
        context.registerService(ResourceResolver.class, resolver);
    }

    /**
     * Test Remove Book Nodes.
     *
     * @throws Exception the exception
     */
    @Test
    void testRemoveBookNodes() throws Exception {
        when(ResolverUtil.newResolver(resolverFactory, "workday-community-administrative-service")).thenReturn(resolver);
        pageResourceListener.removeBookNodes(context.currentPage().getPath());  
    }
}