package com.workday.community.aem.utils;

import com.workday.community.aem.core.constants.WccConstants;
import com.workday.community.aem.core.utils.CommonUtils;
import org.apache.jackrabbit.api.security.user.User;
import org.apache.jackrabbit.api.security.user.UserManager;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.Value;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;

/**
 * The Class CommonUtilsTest.
 */
@ExtendWith({MockitoExtension.class})
public class CommonUtilsTest {
    
    /** The ResourceResolver. */
    ResourceResolver resourceResolver;
    
    /** The service UserManager. */
    private UserManager userManager;

    /** The user. */
    private User user;
    
    @BeforeEach
    public void setup() throws RepositoryException {
        resourceResolver = mock(ResourceResolver.class);
        Session session = mock(Session.class);
        userManager = mock(UserManager.class);
        user = mock(User.class);
        lenient().when(resourceResolver.adaptTo(Session.class)).thenReturn(session);
        lenient().when(session.getUserID()).thenReturn("testUser");
        lenient().when(resourceResolver.adaptTo(UserManager.class)).thenReturn(userManager);
        lenient().when(userManager.getAuthorizable(anyString())).thenReturn(user);
    }

    /**
     * Test getLoggedInUserSourceId.
     */
    @Test
    public void testGetLoggedInUserSourceId() throws RepositoryException {
        Value value = mock(Value.class);
        Value[] values = { value };
        String expectedSfId = "test sfid";
        lenient().when(value.getString()).thenReturn(expectedSfId);
        lenient().when(user.getProperty(WccConstants.PROFILE_SOURCE_ID)).thenReturn(values);
        String sfId = CommonUtils.getLoggedInUserSourceId(resourceResolver);
        assertEquals(expectedSfId, sfId);
    }

    /**
     * Test getLoggedInUserId.
     */
    @Test 
    public void testGetLoggedInUserId() throws RepositoryException {
        Value value = mock(Value.class);
        String expectedUserId = "test user id";
        Value[] values = {value};
        lenient().when(value.getString()).thenReturn(expectedUserId);
        lenient().when(user.getProperty(WccConstants.PROFILE_OKTA_ID)).thenReturn(values);
        String userId = CommonUtils.getLoggedInUserId(resourceResolver);
        assertEquals(expectedUserId, userId);
    }

    /**
     * Test getLoggedInCustomerType.
     */
    @Test 
    public void testGetLoggedInCustomerType() throws RepositoryException {
        Value value = mock(Value.class);
        String expectedCustomerType = "test customer type";
        Value[] values = {value};
        lenient().when(value.getString()).thenReturn(expectedCustomerType);
        lenient().when(user.getProperty(WccConstants.CC_TYPE)).thenReturn(values);
        String customerType = CommonUtils.getLoggedInCustomerType(resourceResolver);
        assertEquals(expectedCustomerType, customerType);
    }

    /**
     * Test getLoggedInUser.
     */
    @Test
    public void testGetLoggedInUser() {
        User testUser = CommonUtils.getLoggedInUser(resourceResolver);
        assertEquals(testUser, user);

    }

    /**
     * Test getLoggedInUserAsNode.
     */
    @Test
    public void testGetLoggedInUserAsNode() throws RepositoryException {
        User testUser = CommonUtils.getLoggedInUser(resourceResolver);
        lenient().when(testUser.getPath()).thenReturn("user path");
        
        Resource resource = mock(Resource.class);
        Node expectedUserNode = mock(Node.class);
        lenient().when(resourceResolver.getResource("user path")).thenReturn(resource);
        lenient().when(resource.adaptTo(Node.class)).thenReturn(expectedUserNode);
        Node userNode = CommonUtils.getLoggedInUserAsNode(resourceResolver);
        assertEquals(userNode, expectedUserNode);
    }
    
}
