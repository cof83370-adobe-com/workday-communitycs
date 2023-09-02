package com.workday.community.aem.utils;

import com.workday.community.aem.core.utils.ResolverUtil;
import org.apache.sling.api.resource.LoginException;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static com.workday.community.aem.core.constants.GlobalConstants.READ_SERVICE_USER;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith({MockitoExtension.class})
public class ResolverUtilTest {
  @Mock
  ResourceResolverFactory resourceResolverFactory;

  @Test
  public void testNewResolver() throws LoginException {
    ResourceResolver mockResolver = mock(ResourceResolver.class);
    when(resourceResolverFactory.getServiceResourceResolver(any())).thenReturn(mockResolver);
    ResourceResolver resolver = ResolverUtil.newResolver(resourceResolverFactory, READ_SERVICE_USER);
    assertEquals(resolver, mockResolver);
  }

}
