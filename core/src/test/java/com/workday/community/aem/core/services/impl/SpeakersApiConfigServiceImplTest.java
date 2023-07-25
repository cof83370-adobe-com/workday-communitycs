package com.workday.community.aem.core.services.impl;

import com.workday.community.aem.core.config.SpeakersSearchConfig;
import com.workday.community.aem.core.services.SpeakersApiConfigService;

import io.wcm.testing.mock.aem.junit5.AemContextExtension;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.annotation.Annotation;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * The Class SpeakersApiConfigServiceImplTest.
 */
@ExtendWith({ AemContextExtension.class, MockitoExtension.class })
public class SpeakersApiConfigServiceImplTest {

    /** The speakers api config service. */
    private final SpeakersApiConfigService speakersApiConfigService = new SpeakersApiConfigServiceImpl();
   
   /** The speakers config. */
   private final SpeakersSearchConfig speakersConfig = new SpeakersSearchConfig() {

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
        ((SpeakersApiConfigServiceImpl) speakersApiConfigService).activate(speakersConfig);
    }

    /**
     * Test all apis.
     */
    @Test
    public void testAllApis() {
        assertEquals(speakersApiConfigService.getSearchFieldLookupAPI(), speakersConfig.searchFieldLookupApi());
        assertEquals(speakersApiConfigService.getSearchFieldConsumerKey(), speakersConfig.searchFieldConsumerKey());
        assertEquals(speakersApiConfigService.getSearchFieldConsumerSecret(), speakersConfig.searchFieldConsumerSecret());
    }
}