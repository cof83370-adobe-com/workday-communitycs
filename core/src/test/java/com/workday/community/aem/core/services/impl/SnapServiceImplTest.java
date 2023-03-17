package com.workday.community.aem.core.services.impl;

import com.day.cq.dam.api.Asset;
import com.day.cq.dam.api.Rendition;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.workday.community.aem.core.config.SnapConfig;
import com.workday.community.aem.core.pojos.ProfilePhoto;
import com.workday.community.aem.core.services.SnapService;

import com.workday.community.aem.core.utils.RestApiUtil;
import io.wcm.testing.mock.aem.junit5.AemContext;
import io.wcm.testing.mock.aem.junit5.AemContextExtension;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;

@ExtendWith({ AemContextExtension.class, MockitoExtension.class })
public class SnapServiceImplTest {
    /** AemContext */
    private final AemContext context = new AemContext();

    @Mock
    ResourceResolverFactory resResolverFactory;

    private final ObjectMapper objectMapper = new ObjectMapper();

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

    Resource resource;

    private final static String sfId = "test";

    @BeforeEach
    public void setup() {
        context.registerService(ResourceResolverFactory.class, resResolverFactory);
        context.registerService(objectMapper);
        context.registerService(snapConfig);

        snapService = new SnapServiceImpl();
        resource = mock(Resource.class);
        snapService.setResourceResolverFactory(resResolverFactory);
    }

    @Test
    public void testGetUserHeaderMenu() throws Exception {
        Asset asset = mock(Asset.class);
        Rendition original = mock(Rendition.class);

        ResourceResolver resolverMock = mock(ResourceResolver.class);

        // Case 0: no resolver mock
        snapService.activate(testSnapConfig);
        String menuData0 = this.snapService.getUserHeaderMenu(sfId);
        assertEquals("", menuData0);

        lenient().when(resolverMock.getResource(any())).thenReturn(resource);
        lenient().when(resResolverFactory.getServiceResourceResolver(any())).thenReturn(resolverMock);

        lenient().when(resource.adaptTo(any())).thenReturn(asset);
        lenient().when(asset.getOriginal()).thenReturn(original);

        // Case 1 No content
        snapService.activate(testSnapConfig);
        String menuData1 = this.snapService.getUserHeaderMenu(sfId);
        assertEquals("", menuData1);

        // Case 2 With Mock Content
        ByteArrayInputStream content = getTestContent("/com/workday/community/aem/core/models/impl/FailStateHeaderTestData.json");
        lenient().when(original.adaptTo(any())).thenReturn(content);
        // Case 1 use incorrect test configuration.
        snapService.activate(testSnapConfig);
        String menuData2 = this.snapService.getUserHeaderMenu(sfId);
        assertTrue(menuData2.length() == 16756);

        // Case 3 use the default with empty value
        snapService.activate(testEmptySnapConfig);
        assertEquals("", this.snapService.getUserHeaderMenu(sfId));
    }

    @Test
    public void testGetProfilePhoto() throws Exception {
        // Case 1: use empty config.
        snapService.activate(testEmptySnapConfig);
        assertNull(this.snapService.getProfilePhoto(sfId));

        snapService.activate(testSnapConfig);
        // Case 2: No return from failed call
        assertNull(this.snapService.getProfilePhoto(sfId));

        // Case 3: return from mocked call.
        ProfilePhoto retObj = new ProfilePhoto();
        retObj.setDescription("test");
        retObj.setPhotoVersionId("1.1");
        retObj.setFileNameWithExtension("foo.png");
        retObj.setBase64content("test fdfdf");
        String mockRet = objectMapper.writeValueAsString(retObj);

        try (MockedStatic<RestApiUtil> mocked = mockStatic(RestApiUtil.class)) {
            mocked.when(() -> RestApiUtil.requestSnapJsonResponse(anyString(), anyString(), anyString())).thenReturn(mockRet);
            context.registerService(mocked);
            ProfilePhoto photoObj = this.snapService.getProfilePhoto(sfId);
            assertEquals(retObj.getBase64content(), photoObj.getBase64content());
        }
    }

    private ByteArrayInputStream getTestContent(String jsonFile) {
        InputStream inputStream = getClass().getResourceAsStream(jsonFile);

        byte[] buffer = new byte[1024];
        int length;
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try {
            while ((length = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, length);
            }
        } catch (IOException e) {
            // handle the exception
        } finally {
            try {
                inputStream.close();
                outputStream.close();
            } catch (IOException e) {
                // handle the exception
            }
        }
        byte[] byteArray = outputStream.toByteArray();

        return new ByteArrayInputStream(byteArray);
    }
}
