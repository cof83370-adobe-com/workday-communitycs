package com.workday.community.aem.core.services.impl;

import com.workday.community.aem.core.config.OurmUserSearchConfig;
import com.workday.community.aem.core.services.OurmUsersApiConfigService;

import io.wcm.testing.mock.aem.junit5.AemContextExtension;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.annotation.Annotation;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * The Class OurmUsersApiConfigServiceImplTest.
 */
@ExtendWith({ AemContextExtension.class, MockitoExtension.class })
public class OurmUsersApiConfigServiceImplTest {

    /** The ourmUsers api config service. */
    private final OurmUsersApiConfigService ourmUsersApiConfigService = new OurmUsersApiConfigServiceImpl();
   
   /** The ourmUsers config. */
   private final OurmUserSearchConfig ourmUsersConfig = new OurmUserSearchConfig() {

    @Override
    public Class<? extends Annotation> annotationType() {
      return Annotation.class;
    }

    @Override
    public String searchFieldLookupApi() {
      return "https://den.community-workday.com/user/search/";
    }

    @Override
    public String searchFieldConsumerKey() {
      return "r4hd9dxB9ToJWYBQpJAhUauGXoh4r35r";
    }

    @Override
    public String searchFieldConsumerSecret() {
      return "Gx9qk47hwzubLymkfyv4xCS42oTJiDMv";
    }
};

    /**
     * Setup.
     */
    @BeforeEach
    public void setup() {
        ((OurmUsersApiConfigServiceImpl) ourmUsersApiConfigService).activate(ourmUsersConfig);
    }

    /**
     * Test all apis.
     */
    @Test
    public void testAllApis() {
        assertEquals(ourmUsersApiConfigService.getSearchFieldLookupAPI(), ourmUsersConfig.searchFieldLookupApi());
        assertEquals(ourmUsersApiConfigService.getSearchFieldConsumerKey(), ourmUsersConfig.searchFieldConsumerKey());
        assertEquals(ourmUsersApiConfigService.getSearchFieldConsumerSecret(), ourmUsersConfig.searchFieldConsumerSecret());
    }
}