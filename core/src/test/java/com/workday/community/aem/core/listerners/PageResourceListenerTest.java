package com.workday.community.aem.core.listerners;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.Property;

import org.apache.jackrabbit.api.security.user.User;
import org.apache.jackrabbit.api.security.user.UserManager;
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
import com.workday.community.aem.core.constants.GlobalConstants;
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

    /** The service PageManager. */
    @Mock
    private PageManager pageManager;
    /** The ValueMap */
    @Mock
    private ValueMap valueMap;

    /** The Page */
    @Mock
    private Page page;

    /** The Node */
    @Mock Node node;

    /** The Resource */
    @Mock
    private Resource resource;

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

        pageResourceListener.addAuthorPropertyToContentNode(context.currentPage().getContentResource().getPath(), resolver);
    }

    /**
     *  Test Add Internal Workmates Tag.
     * 
     * @throws Exception
     */
    @Test
    void testAddInternalWorkmatesTag() throws Exception {
        List<String> updatedACLTags = new ArrayList<>(Arrays.asList("product:hcm", "access-control:internal_workmates"));
        String [] aclTags = {"product:hcm"};
        when(resolver.adaptTo(PageManager.class)).thenReturn(pageManager);
        when(pageManager.getContainingPage(context.currentPage().getContentResource().getPath())).thenReturn(page);
        when(page.getProperties()).thenReturn(valueMap);
        when(page.getContentResource()).thenReturn(resource);
        when(resource.adaptTo(Node.class)).thenReturn(node);
        when(valueMap.get(GlobalConstants.TAG_PROPERTY_ACCESS_CONTROL, String[].class)).thenReturn(aclTags);
        pageResourceListener.addInternalWorkmatesTag(context.currentPage().getContentResource().getPath(), resolver);
        verify(node, times(1)).setProperty(GlobalConstants.TAG_PROPERTY_ACCESS_CONTROL, updatedACLTags.stream().toArray(String[]::new));
    }
}