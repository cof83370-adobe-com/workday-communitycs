package com.workday.community.aem.core.services;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.lang.annotation.Annotation;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import com.workday.community.aem.core.config.OurmUserGroupConfig;

/**
 * The Class OurmUserGroupConfigServiceTest.
 */
@ExtendWith({MockitoExtension.class})
public class OurmUserGroupConfigServiceTest {
    
    /** The service OurmUserGroupConfigService. */
    private final OurmUserGroupConfigService snapUserGroupConfigService = new OurmUserGroupConfigService();

    /** The mocked OurmUserGroupConfig config. */
    private final OurmUserGroupConfig mockConfig = new OurmUserGroupConfig() {
        
        @Override
        public String xApiKey() {
            return "apiKey";
        }
        
        @Override
        public String token() {
            return "authorization";
        }
        
        @Override
        public String apiUri() {
            return "apiUri";
        }
        
        @Override
        public Class<? extends Annotation> annotationType() {
            return null;
        }
    };
    
    /**
     * Set up service.
     */
    @BeforeEach
    public void setup() {
        ((OurmUserGroupConfigService) snapUserGroupConfigService).activate(mockConfig);
    }

    /**
     * Test all get methods in the service.
     */
    @Test
    public void testAllGetMethods() {
        assertEquals(snapUserGroupConfigService.getXApiKey(), mockConfig.xApiKey());
        assertEquals(snapUserGroupConfigService.getApiUri(), mockConfig.apiUri());
        assertEquals(snapUserGroupConfigService.getToken(), mockConfig.token());
  }

    
}
