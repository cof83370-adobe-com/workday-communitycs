package com.workday.community.aem.core.models.impl;

import com.day.cq.wcm.api.Page;
import com.workday.community.aem.core.models.CoveoStatusModel;
import com.workday.community.aem.core.models.Metadata;
import com.workday.community.aem.core.models.TocModel;
import com.workday.community.aem.core.services.QueryService;
import io.wcm.testing.mock.aem.junit5.AemContext;
import io.wcm.testing.mock.aem.junit5.AemContextExtension;
import org.apache.jackrabbit.api.security.user.Authorizable;
import org.apache.jackrabbit.api.security.user.UserManager;
import org.apache.sling.api.resource.ResourceResolver;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.jcr.RepositoryException;
import javax.jcr.Value;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

/**
 * The Class MetadataImplTest.
 */
@ExtendWith({ AemContextExtension.class, MockitoExtension.class })
public class TocModelmplTest {
    /** The context. */
    private final AemContext context = new AemContext();
    /** The currentPage. */
    private Page currentPage;

    /**
     * Setup.
     *
     * @throws Exception the exception
     */
    @BeforeEach
    public void setup() throws Exception {
        context.addModelsForClasses(CoveoStatusModelImpl.class);
        context.load().json("/com/workday/community/aem/core/models/impl/TocModelImplTest.json", "/content");
    }

    /**
     * Test bookResourcePath
     */
    @Test
    void testBookResourcePath() throws Exception {
        Page currentPage = context.currentResource("/content/event-page").adaptTo(Page.class);

        QueryService queryService = mock(QueryService.class);
        context.registerService(QueryService.class, queryService);
        List<String> pathList = new ArrayList<>();
        pathList.add("/content/book-1/jcr:content/root/container/container/book");
        lenient().when(queryService.getBookNodesByPath(currentPage.getPath(), null)).thenReturn(pathList);
        context.registerService(Page.class, currentPage);
        TocModel tocModel = context.request().adaptTo(TocModel.class);
        assertEquals("/content/book-1/jcr:content/root/container/container/book", tocModel.bookResourcePath());
    }

    /**
     * Test bookResourcePath
     */
    @Test
    void testBookResourcePathNull() throws Exception {
        Page currentPage = context.currentResource("/content/event-page").adaptTo(Page.class);

        QueryService queryService = mock(QueryService.class);
        context.registerService(QueryService.class, queryService);
        lenient().when(queryService.getBookNodesByPath(currentPage.getPath(), null)).thenReturn(null);
        context.registerService(Page.class, currentPage);
        TocModel tocModel = context.request().adaptTo(TocModel.class);
        assertEquals(null, tocModel.bookResourcePath());
    }
}
