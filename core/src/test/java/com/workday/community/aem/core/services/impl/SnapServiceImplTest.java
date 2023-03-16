package com.workday.community.aem.core.services.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.workday.community.aem.core.config.SnapConfig;
import com.workday.community.aem.core.services.SnapService;

import io.wcm.testing.mock.aem.junit5.AemContext;
import io.wcm.testing.mock.aem.junit5.AemContextExtension;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.annotation.Annotation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

@ExtendWith({ AemContextExtension.class, MockitoExtension.class  })
public class SnapServiceImplTest {
    /** AemContext */
    private final AemContext context = new AemContext();

    @Mock
    ResourceResolverFactory resResolverFactory;

    @Mock
    ObjectMapper objectMapper;

    @Mock
    SnapConfig snapConfig;

    private final SnapConfig testEmptySnapConfig = new SnapConfig() {

        @Override
        public Class<? extends Annotation> annotationType() {
            return null;
        }

        @Override
        public String snapUrl() {
            return null;
        }

        @Override
        public String navApi() {
            return null;
        }

        @Override
        public String navApiKey() {
            return null;
        }

        @Override
        public String navApiToken() {
            return null;
        }

        @Override
        public String sfdc_get_photo_url() {
            return null;
        }

        @Override
        public String sfdc_get_photo_token() {
            return null;
        }

        @Override
        public String sfdc_api_key() {
            return null;
        }
    };

    private final SnapConfig testSnapConfig = new SnapConfig() {
        @Override
        public Class<? extends Annotation> annotationType() {
            return null;
        }

        @Override
        public String snapUrl() {
            return "http://test/snap";
        }

        @Override
        public String navApi() {
            return "/menu";
        }

        @Override
        public String navApiKey() {
            return "testApiKey";
        }

        @Override
        public String navApiToken() {
            return "testApiToken";
        }

        @Override
        public String sfdc_get_photo_url() {
            return "/foo";
        }

        @Override
        public String sfdc_get_photo_token() {
            return "TestPhotoToken";
        }

        @Override
        public String sfdc_api_key() {
            return "testSfApiToken";
        }
    };

    SnapService snapService;

    private final static String sfId = "test";

    @BeforeEach
    public void setup() {
        context.registerService(ResourceResolverFactory.class, resResolverFactory);
        context.registerService(objectMapper);
        context.registerService(snapConfig);

        snapService = new SnapServiceImpl();
        snapService.setResourceResolverFactory(resResolverFactory);
    }

    @Test
    public void testGetUserHeaderMenuWithIncorrectConfig() {
        // Case 1 use incorrect test configuration.
        snapService.activate(testSnapConfig);
        assertEquals("", this.snapService.getUserHeaderMenu(sfId));

        // Case 2 use the default with empty value
        snapService.activate(testEmptySnapConfig);
        assertEquals("", this.snapService.getUserHeaderMenu(sfId));
    }

    @Test
    public void testGetProfilePhotoWithIncorrectConfig() {
        // Case 1 use incorrect test configuration.
        snapService.activate(testSnapConfig);
        assertNull(this.snapService.getProfilePhoto(sfId));

        // Case 2 use the default with empty value
        snapService.activate(testEmptySnapConfig);
        assertNull(this.snapService.getProfilePhoto(sfId));
    }
}
