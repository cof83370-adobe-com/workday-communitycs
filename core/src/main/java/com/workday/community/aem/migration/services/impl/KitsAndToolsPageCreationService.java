package com.workday.community.aem.migration.services.impl;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.jcr.Node;
import javax.jcr.Session;

import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.propertytypes.ServiceDescription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.day.cq.wcm.api.Page;
import com.workday.community.aem.migration.constants.MigrationConstants;
import com.workday.community.aem.migration.models.KitsAndToolsPageData;
import com.workday.community.aem.migration.models.PageNameBean;
import com.workday.community.aem.migration.services.PageCreationService;
import com.workday.community.aem.migration.utils.MigrationUtils;
import com.workday.community.aem.migration.utils.TagFinderEnum;

@Component(immediate = true, service = PageCreationService.class, property = { "type=kitsandtools-page" })
@ServiceDescription("Workday - Kits And Tools Page Creation Service")
public class KitsAndToolsPageCreationService implements PageCreationService {

    /** The logger. */
    private final Logger logger = LoggerFactory.getLogger(this.getClass().getName());

    /** The Constant RETIREMENT_DATE. */
    private static final String RETIREMENT_DATE = "retirementDate";

    /** The Constant READ_COUNT. */
    private static final String READ_COUNT = "readCount";

    /** The Constant UPDATED_DATE. */
    private static final String UPDATED_DATE = "updatedDate";

    /** The resolver factory. */
    @Reference
    private ResourceResolverFactory resolverFactory;

    /** The wd service param. */
    Map<String, Object> wdServiceParam = Collections.singletonMap(ResourceResolverFactory.SUBSERVICE,
            "workday-community-administrative-service");

    Node jcrNode = null;

    public Node getJcrNode() {
        return jcrNode;
    }

    String aemPageName = StringUtils.EMPTY;

    @Override
    public void doCreatePage(Map<String, String> paramsMap, Object objecDdata, List<PageNameBean> list) {
        try (ResourceResolver resourceResolver = resolverFactory.getServiceResourceResolver(wdServiceParam)) {
            Session session = resourceResolver.adaptTo(Session.class);
            if (session != null) {
                KitsAndToolsPageData data = (KitsAndToolsPageData) objecDdata;
                // Derive the page title and page name.
                final String nodeId = data.getDrupalNodeId();
                String aemPageTitle = data.getTitle();
                MigrationUtils.getAemPageName(list, nodeId);
                // Create Page.
                final Page prodPage = MigrationUtils.getPageCreated(resourceResolver, paramsMap, aemPageTitle);
                if (null != prodPage) {
                    jcrNode = prodPage.hasContent() ? prodPage.getContentResource().adaptTo(Node.class) : null;
                }
                if (null == jcrNode) {
                    return;
                }

                // Set page properties.
                setPageProps(jcrNode, data, resourceResolver);
                MigrationUtils.saveToRepo(session);
            }
        } catch (Exception exec) {
            logger.error(
                    "Exception occurred while creating page in doCreatePage of KitsAndToolsPageCreationService::{}",
                    exec.getMessage());
        }
    }

    private void setPageProps(final Node jcrNode, final KitsAndToolsPageData data, ResourceResolver resourceResolver) {
        try {
            if (StringUtils.isNotBlank(data.getDrupalNodeId())) {
                jcrNode.setProperty(MigrationConstants.DRUPAL_NODE_ID, Long.parseLong(data.getDrupalNodeId()));
            }
            if (StringUtils.isNotBlank(data.getAuthor())) {
                jcrNode.setProperty(MigrationConstants.AUTHOR, data.getAuthor());
            }

            if (StringUtils.isNotBlank(data.getPostedDate())) {
                String postedDateStr = MigrationUtils.getDateStringFromEpoch(Long.parseLong(data.getPostedDate()));
                Calendar postedDate = MigrationUtils.convertStrToAemCalInstance(postedDateStr,
                        MigrationConstants.EventsPageConstants.MMM_DD_COMMA_YYYY_FORMAT);
                postedDate.add(Calendar.DATE, 1); 
                jcrNode.setProperty(MigrationConstants.POSTED_DATE, postedDate);
            }

            if (StringUtils.isNoneBlank(data.getDrupalAccessControl())) {
                jcrNode.setProperty(MigrationConstants.DRUPAL_ACEESS_CONTROL, data.getDrupalAccessControl());
            }

            if (StringUtils.isNotBlank(data.getRetirementDate())) {
                Calendar retirementDate = MigrationUtils.convertStrToAemCalInstance(data.getRetirementDate(),
                        MigrationConstants.EventsPageConstants.YYYY_MM_DD_FORMAT);
                retirementDate.add(Calendar.DATE, 1); 
                jcrNode.setProperty(RETIREMENT_DATE, retirementDate);
            }
            if (StringUtils.isNotBlank(data.getReadCount())) {
                jcrNode.setProperty(READ_COUNT, Long.parseLong(data.getReadCount()));
            }
            if (StringUtils.isNotBlank(data.getUpdatedDate())) {
                String dateStr = MigrationUtils.getDateStringFromEpoch(Long.parseLong(data.getUpdatedDate()));
                Calendar updatedDate = MigrationUtils.convertStrToAemCalInstance(dateStr,
                        MigrationConstants.EventsPageConstants.MMM_DD_COMMA_YYYY_FORMAT);
                updatedDate.add(Calendar.DATE, 1);
                jcrNode.setProperty(UPDATED_DATE, updatedDate);
            }

            collectAllTagsForGivenPage(resourceResolver, data);

        } catch (Exception exec) {
            logger.error("Exception occurred in setPageProps::{}", exec.getMessage());
        }
    }

    private void collectAllTagsForGivenPage(ResourceResolver resourceResolver, final KitsAndToolsPageData data) {
        // To add release tags.
        if (StringUtils.isNotBlank(data.getReleaseTag())) {
            List<String> releaseTags = Optional
                    .ofNullable(
                            MigrationUtils.getTagsForGivenInputs(resourceResolver, TagFinderEnum.RELEASE_TAG,
                                    data.getReleaseTag()))
                    .orElse(new ArrayList<>());
            MigrationUtils.mountTagPageProps(getJcrNode(), MigrationConstants.TagPropertyName.RELEASE, releaseTags);
        }

        // To add product tags.
        if (StringUtils.isNotBlank(data.getProduct())) {
            List<String> productTags = Optional
                    .ofNullable(MigrationUtils.getTagsForGivenInputs(resourceResolver, TagFinderEnum.PRODUCT,
                            data.getProduct()))
                    .orElse(new ArrayList<>());
            MigrationUtils.mountTagPageProps(getJcrNode(), MigrationConstants.TagPropertyName.PRODUCT, productTags);
        }

        // To add using workday tags.
        if (StringUtils.isNotBlank(data.getUsingWorkday())) {
            List<String> usingWorkdayTags = Optional
                    .ofNullable(
                            MigrationUtils.getTagsForGivenInputs(resourceResolver, TagFinderEnum.USING_WORKDAY,
                                    data.getUsingWorkday()))
                    .orElse(new ArrayList<>());
            MigrationUtils.mountTagPageProps(getJcrNode(), MigrationConstants.TagPropertyName.USING_WORKDAY,
                    usingWorkdayTags);
        }

        // To add industry tags.
        if (StringUtils.isNotBlank(data.getIndustry())) {
            List<String> industryTags = Optional
                    .ofNullable(
                            MigrationUtils.getTagsForGivenInputs(resourceResolver, TagFinderEnum.INDUSTRY,
                                    data.getUsingWorkday()))
                    .orElse(new ArrayList<>());
            MigrationUtils.mountTagPageProps(getJcrNode(), MigrationConstants.TagPropertyName.INDUSTRY,
            industryTags);
        }

        // To add programsTools tags.
        if (StringUtils.isNotBlank(data.getProgramType())) {
            List<String> programTypeTags = Optional
                    .ofNullable(
                            MigrationUtils.getTagsForGivenInputs(resourceResolver, TagFinderEnum.PROGRAM_TYPE,
                                    data.getProgramType()))
                    .orElse(new ArrayList<>());
            MigrationUtils.mountTagPageProps(getJcrNode(), MigrationConstants.TagPropertyName.PROGRAM_TYPE,
            programTypeTags);
        }
    }
}