package com.workday.community.aem.core;

import com.workday.community.aem.core.config.CacheConfig;
import com.workday.community.aem.core.config.CoveoSearchConfig;
import org.apache.jackrabbit.api.security.user.User;

import javax.jcr.RepositoryException;
import javax.jcr.Value;
import java.lang.annotation.Annotation;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;

public class TestUtil {
  public static CacheConfig getCacheConfig() {
    return new CacheConfig() {
      @Override
      public Class<? extends Annotation> annotationType() {
        return null;
      }

      @Override
      public int maxSize() {
        return 10;
      }

      public int maxUUID() {
        return 10;
      }

      @Override
      public int maxJcrUser() {
        return 10;
      }

      @Override
      public int expireDuration() {
        return 1000;
      }

      @Override
      public int jcrUserExpireDuration() {
        return 1000;
      }

      @Override
      public int refreshDuration() {
        return 100;
      }

      @Override
      public boolean enabled() {
        return true;
      }
    };
  }
  public static CoveoSearchConfig getCoveoSearchConfig() {
    return new CoveoSearchConfig() {

      @Override
      public Class<? extends Annotation> annotationType() {
        return Annotation.class;
      }

      @Override
      public String tokenApi() {
        return "http://coveo/token";
      }

      @Override
      public String searchFieldLookupApi() {
        return "foo/";
      }

      @Override
      public String tokenApiKey() {
        return "tokenApiKey";
      }

      @Override
      public String defaultEmail() {
        return "foo@workday.com";
      }

      @Override
      public String recommendationApiKey() {
        return "recommendationApiKey";
      }

      @Override
      public String upcomingEventApiKey() {
        return "upcomingEventApiKey";
      }

      @Override
      public String orgId() {
        return "orgId";
      }

      @Override
      public String userIdProvider() {
        return "null";
      }

      @Override
      public String userType() {
        return "null";
      }

      @Override
      public String searchHub() {
        return "searchHub";
      }

      @Override
      public int tokenValidTime() {
        return 12000;
      }

      @Override
      public boolean devMode() {
        return true;
      }

      @Override
      public String globalSearchURL() {
        return "https://resourcecenter.workday.com/en-us/wrc/home/search.html";
      }
    };
  }
  public static User getMockUser() throws RepositoryException {
    User user = mock(User.class);
    Value val1 = mock(Value.class);
    Value val2 = mock(Value.class);
    Value[] val = new Value[]{val1, val2};
    lenient().when(user.getProperty(anyString())).thenReturn(val);
    lenient().when(val[0].getString()).thenReturn("testSfId");
    return user;
  }
}
