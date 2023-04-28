package com.workday.community.aem.core.listerners;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import java.util.ArrayList;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.Value;

import org.apache.jackrabbit.api.security.user.User;
import org.apache.jackrabbit.api.security.user.UserManager;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.apache.sling.testing.mock.sling.ResourceResolverType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.apache.jackrabbit.vault.util.JcrConstants;
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

    /** The user. */
    private User user;

    /** The service UserManager. */
    private UserManager userManager;

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
        List<String> pathList = new ArrayList<>();
        pathList.add("/content/book-1/jcr:content/root/container/container/book");
        lenient().when(queryService.getBookNodesByPath(context.currentPage().getPath(), null)).thenReturn(pathList);
        pageResourceListener.removeBookNodes(context.currentPage().getPath());
        verify(resolver).close();  
    }

    /**
     * Test Adding Author Property to Content Node.
     *
     * @throws Exception the exception
     */

    @Test
    void tesAddAuthorPropertyToContentNode() throws Exception {
        when(ResolverUtil.newResolver(resolverFactory, "workday-community-administrative-service")).thenReturn(resolver);
        Resource resource = mock(Resource.class);
        Node expectedUserNode = mock(Node.class);
        userManager = mock(UserManager.class);
        Property prop1 =mock(Property.class);
        user = mock(User.class);
        lenient().when(resolver.getResource(context.currentPage().getContentResource().getPath())).thenReturn(resource);
        lenient().when(resource.adaptTo(Node.class)).thenReturn(expectedUserNode);
        lenient().when(expectedUserNode.getProperty(anyString())).thenReturn(prop1);
        lenient().when(prop1.getString()).thenReturn("test user");
        lenient().when(resolver.adaptTo(UserManager.class)).thenReturn(userManager);
        lenient().when(userManager.getAuthorizable(anyString())).thenReturn(user);

        pageResourceListener.addAuthorPropertyToContentNode(context.currentPage().getContentResource().getPath());
    }
}