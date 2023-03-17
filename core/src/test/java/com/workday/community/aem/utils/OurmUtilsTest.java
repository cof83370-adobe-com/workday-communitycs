package com.workday.community.aem.utils;

import com.workday.community.aem.core.utils.OurmUtils;
import org.apache.jackrabbit.api.security.user.User;
import org.apache.jackrabbit.api.security.user.UserManager;
import org.apache.sling.api.resource.ResourceResolver;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.jcr.Session;
import javax.jcr.Value;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;

@ExtendWith({ MockitoExtension.class })
public class OurmUtilsTest {
    ResourceResolver resolverMock;

    private UserManager userManager;

    @BeforeEach
    public void setup() {
        resolverMock = mock(ResourceResolver.class);
        Session session = mock(Session.class);
        userManager = mock(UserManager.class);

        lenient().when(resolverMock.adaptTo(Session.class)).thenReturn(session);
        lenient().when(session.getUserID()).thenReturn("fool");

        lenient().when(resolverMock.adaptTo(UserManager.class)).thenReturn(userManager);
    }

    @Test
    public void testGetSalesForceIdDefaultMaster() throws Exception {
        // case 1:
        lenient().when(userManager.getAuthorizable(anyString())).thenThrow(new RuntimeException());
        String sfId = OurmUtils.getSalesForceId(resolverMock);
        assertEquals("masterdata", sfId);
    }

    @Test
    public void testGetSalesForceIdMock() throws Exception {
        // case 2:
        User user = mock(User.class);
        Value val1 = mock(Value.class);
        Value val2 = mock(Value.class);
        Value[] val =  new Value[]{val1, val2};
        lenient().when(user.getProperty(anyString())).thenReturn(val);
        lenient().when(val[0].getString()).thenReturn("testSfId");
        lenient().when(userManager.getAuthorizable(anyString())).thenReturn(user);

        String testSfId = OurmUtils.getSalesForceId(resolverMock);
        assertEquals("testSfId", testSfId);
    }
}
