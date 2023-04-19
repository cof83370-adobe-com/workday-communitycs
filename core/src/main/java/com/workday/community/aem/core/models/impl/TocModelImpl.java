package com.workday.community.aem.core.models.impl;

import com.day.cq.wcm.api.Page;
import com.workday.community.aem.core.models.TocModel;
import com.workday.community.aem.core.services.QueryService;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.models.annotations.DefaultInjectionStrategy;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.injectorspecific.OSGiService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.List;

@Model(adaptables = {Resource.class, SlingHttpServletRequest.class}, adapters = {TocModel.class}, resourceType = {TocModelImpl.RESOURCE_TYPE}, defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL)
public class TocModelImpl implements TocModel {
    /**
     * The Constant RESOURCE_TYPE.
     */
    protected static final String RESOURCE_TYPE = "workday-community/components/common/toc";

    /**
     * The logger.
     */
    private final Logger logger = LoggerFactory.getLogger(this.getClass().getName());
    /**
     * The current page.
     */
    @Inject
    private Page currentPage;

    /**
     * The query service.
     */
    @OSGiService
    private QueryService queryService;

    @Override
    public String bookResourcePath() {
        String bookResourcePath = null;
        try {
            if (null != currentPage) {
                List<String> bookPathList = queryService.getBookNodesByPath(currentPage.getPath(), null);
                if (bookPathList.size() > 0) {
                    bookResourcePath = bookPathList.get(0).split("/firstlevel")[0];
                }
            }
        } catch (NullPointerException e) {
            logger.error(String.format("RepositoryException in TocImpl::bookPathList: %s", e.getMessage()));
        }
        return bookResourcePath;
    }
}