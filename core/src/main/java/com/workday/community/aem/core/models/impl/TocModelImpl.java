package com.workday.community.aem.core.models.impl;

import com.day.cq.wcm.api.Page;
import com.workday.community.aem.core.models.TocModel;
import com.workday.community.aem.core.pojos.BookPageBean;
import com.workday.community.aem.core.services.QueryService;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.models.annotations.DefaultInjectionStrategy;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.injectorspecific.OSGiService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.ArrayList;
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
     * The resolver.
     */
    @Inject
    private ResourceResolver resolver;

    /**
     * The query service.
     */
    @OSGiService
    private QueryService queryService;

    @Override
    public List<BookPageBean> bookPageList() {
        List<BookPageBean> bookPageList = new ArrayList<>();
        try {
            if (null != currentPage) {
                List<String> bookPathList = queryService.getBookNodesByPath(currentPage.getPath(), null);
                for (String bookPath : bookPathList) {
                    String bookNodePath = bookPath.split("/firstlevel")[0];
                    Resource bookResource = resolver.resolve(bookNodePath);
                    if (bookResource != null) {
                        final Iterable<Resource> children = bookResource.getChildren();

                        for (final Resource child : children) {
                            if ("firstlevel".equals(child.getName())) {
                                final Iterable<Resource> firstLevelChildren = child.getChildren();
                                for (final Resource firstlevelchild : firstLevelChildren) {
                                    BookPageBean bookPageBean = new BookPageBean();
                                    ValueMap vm = firstlevelchild.adaptTo(ValueMap.class);
                                    bookPageBean.setMainPagePath(vm.get("mainpagepath", String.class));
                                    if (bookPageBean.getMainPagePath() != null) {
                                        Resource firstLevelRes = resolver.getResource(bookPageBean.getMainPagePath() + "/jcr:content");
                                        if (firstLevelRes != null) {
                                            ValueMap properties = firstLevelRes.adaptTo(ValueMap.class);
                                            bookPageBean.setMainPageTitle(properties.get("jcr:title", String.class));
                                        } else {
                                            bookPageBean.setMainPagePath(null);
                                        }
                                    }
                                    if (bookPageBean.getMainPagePath() != null) {
                                        final Iterable<Resource> secondLevelParentIter = firstlevelchild.getChildren();
                                        for (final Resource secondlevelparent : secondLevelParentIter) {
                                            final Iterable<Resource> secondLevelChildren = secondlevelparent.getChildren();
                                            for (final Resource secondlevelchild : secondLevelChildren) {
                                                BookPageBean.SecondPageBean secondPageBean = bookPageBean.new SecondPageBean();
                                                ValueMap secondvm = secondlevelchild.adaptTo(ValueMap.class);
                                                secondPageBean.setSecondPagePath(secondvm.get("secondpagepath", String.class));
                                                if (secondPageBean.getSecondPagePath() != null) {
                                                    Resource secondLevelRes = resolver.getResource(secondPageBean.getSecondPagePath() + "/jcr:content");
                                                    if (secondLevelRes != null) {
                                                        ValueMap properties = secondLevelRes.adaptTo(ValueMap.class);
                                                        secondPageBean.setSecondPageTitle(properties.get("jcr:title", String.class));
                                                    } else {
                                                        secondPageBean.setSecondPagePath(null);
                                                    }
                                                }
                                                if (secondPageBean.getSecondPagePath() != null) {
                                                    final Iterable<Resource> thirdLevelParentIter = secondlevelchild.getChildren();
                                                    for (final Resource thirdlevelparent : thirdLevelParentIter) {
                                                        final Iterable<Resource> thirdLevelChildren = thirdlevelparent.getChildren();
                                                        for (final Resource thirdlevelchild : thirdLevelChildren) {
                                                            BookPageBean.ThirdPageBean thirdPageBean = bookPageBean.new ThirdPageBean();
                                                            ValueMap thirdvm = thirdlevelchild.adaptTo(ValueMap.class);
                                                            thirdPageBean.setThirdPagePath(thirdvm.get("thirdpagepath", String.class));

                                                            if (thirdPageBean.getThirdPagePath() != null) {
                                                                Resource thirdLevelRes = resolver.getResource(thirdPageBean.getThirdPagePath() + "/jcr:content");
                                                                if (thirdLevelRes != null) {
                                                                    ValueMap properties = thirdLevelRes.adaptTo(ValueMap.class);
                                                                    thirdPageBean.setThirdPageTitle(properties.get("jcr:title", String.class));
                                                                } else {
                                                                    thirdPageBean.setThirdPagePath(null);
                                                                }
                                                            }
                                                            secondPageBean.getThirdPageBeanList().add(thirdPageBean);
                                                        }
                                                    }
                                                    bookPageBean.getSecondPageBeanList().add(secondPageBean);
                                                }
                                            }
                                        }
                                        bookPageList.add(bookPageBean);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            logger.error(String.format("RepositoryException in TocImpl::bookPageList: %s", e.getMessage()));
        }
        return bookPageList;
    }

    @Override
    public Resource bookResource() {
        Resource bookResource = null;
        try {
            if (null != currentPage) {
                List<String> bookPathList = queryService.getBookNodesByPath(currentPage.getPath(), null);
                for (String bookPath : bookPathList) {
                    String bookNodePath = bookPath.split("/firstlevel")[0];
                    bookResource = resolver.resolve(bookNodePath);
                }
            }
        } catch (Exception e) {
            logger.error(String.format("RepositoryException in TocImpl::bookPageList: %s", e.getMessage()));
        }
        return bookResource;
    }
}