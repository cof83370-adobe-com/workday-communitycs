package com.workday.community.aem.migration.services.impl;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

/**
 * The Class KitsAndToolsPageCreationService.
 */
@Component(immediate = true, service = PageCreationService.class, property = { "type=kitsandtools-page" })
@ServiceDescription("Workday - Kits And Tools Page Creation Service")
public class KitsAndToolsPageCreationService implements PageCreationService {

    /** The logger. */
    private final Logger logger = LoggerFactory.getLogger(this.getClass().getName());

    /** The Constant KITS_PURPOSE_REGEX. */
    private static final String KITS_PURPOSE_REGEX = "(?s)^<h2 class=(.*)title__h2(.*)>(.*)Purpose(.*)<\\/h2>(.*)$";

    /** The Constant KITS_REC_UPDATES_REGEX. */
    private static final String KITS_REC_UPDATES_REGEX = "(?s)^<h2 class=(.*)title__h2(.*)>(.*)Recent Updates(.*)<\\/h2>(.*)$";

    /** The Constant KITS_USE_CASE_REGEX. */
    private static final String KITS_USE_CASE_REGEX = "(?s)^<h2 class=(.*)title__h2(.*)>(.*)Use Case(.*)<\\/h2>(.*)$";

    /** The Constant KITS_FILES_REGEX. */
    private static final String KITS_FILES_REGEX = "(?s)^<h2 class=(.*)title__h2(.*)>(.*)Files(.*)<\\/h2>(.*)$";

    /** The Constant KITS_PRE_REQ_REGEX. */
    private static final String KITS_PRE_REQ_REGEX = "(?s)^<h2 class=(.*)title__h2(.*)>(.*)Prerequisites(.*)<\\/h2>(.*)$";

    /** The Constant KITS_DESIGN_NOTES_REGEX. */
    private static final String KITS_DESIGN_NOTES_REGEX = "(?s)^<h2 class=(.*)title__h2(.*)>(.*)Design Notes(.*)<\\/h2>(.*)$";

    /** The Constant KITS_IMPL_NOTES_REGEX. */
    private static final String KITS_IMPL_NOTES_REGEX = "(?s)^<h2 class=(.*)title__h2(.*)>(.*)Implementation Notes(.*)<\\/h2>(.*)$";

    /** The Constant KITS_STEPS_REGEX. */
    private static final String KITS_STEPS_REGEX = "(?s)^<h2 class=(.*)title__h2(.*)>(.*)Steps(.*)<\\/h2>(.*)$";

    /** The Constant H2_ELE_END_TAG. */
    private static final String H2_ELE_END_TAG = "</h2>";

    /** The Constant IMG_TAG_REGEX. */
    private static final String IMG_TAG_REGEX = "(<img[^>]* \\/>)";

    /** The Constant IMG_SRC_REGX. */
    private static final String IMG_SRC_REGX = "<img[^>]*src=\"([^\"]+)\"[^>]*>";

    private static final String ACCORDION_NODE = "accordion";

    private static final String CONTAINER_MAIN = "container_main";

    private static final String CONTAINER_CENTER = "container_center";

    /** The aem page name. */
    String aemPageName = StringUtils.EMPTY;

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

    /** The jcr node. */
    Node jcrNode = null;

    /**
     * Gets the jcr node.
     *
     * @return the jcr node
     */
    public Node getJcrNode() {
        return jcrNode;
    }

    /**
     * Do create page.
     *
     * @param paramsMap  the params map
     * @param objecDdata the objec ddata
     * @param list       the list
     */
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

                Node accordion = getAccordionNode();
                if (null != accordion) {
                    createKitsAndToolsDescription(accordion, data);
                }
                MigrationUtils.saveToRepo(session);
            }
        } catch (Exception exec) {
            logger.error(
                    "Exception occurred while creating page in doCreatePage of KitsAndToolsPageCreationService::{}",
                    exec.getMessage());
        }
    }

    /**
     * Gets the accordion node.
     *
     * @return the accordion node
     */
    private Node getAccordionNode() {
        try {
            Node rootNode = jcrNode.hasNode(MigrationConstants.ROOT_NODE)
                    ? jcrNode.getNode(MigrationConstants.ROOT_NODE)
                    : jcrNode.addNode(MigrationConstants.ROOT_NODE);

            Node containerNode = rootNode.hasNode(MigrationConstants.CONTAINER_NODE)
                    ? rootNode.getNode(MigrationConstants.CONTAINER_NODE)
                    : rootNode.addNode(MigrationConstants.CONTAINER_NODE);

            Node innerContainerNode = containerNode.hasNode(MigrationConstants.CONTAINER_NODE)
                    ? containerNode.getNode(MigrationConstants.CONTAINER_NODE)
                    : containerNode.addNode(MigrationConstants.CONTAINER_NODE);

            Node containerCenter = innerContainerNode.hasNode(CONTAINER_CENTER)
                    ? innerContainerNode.getNode(CONTAINER_CENTER)
                    : innerContainerNode.addNode(CONTAINER_CENTER);

            Node containerMain = containerCenter.hasNode(CONTAINER_MAIN)
                    ? containerCenter.getNode(CONTAINER_MAIN)
                    : containerCenter.addNode(CONTAINER_MAIN);

            return containerMain.hasNode(ACCORDION_NODE) ? containerMain.getNode(ACCORDION_NODE)
                    : containerMain.addNode(ACCORDION_NODE);
        } catch (Exception exec) {
            logger.error(
                    "Exception occurred while retrieving the accordion node of KitsAndToolsPageCreationService::{}",
                    exec.getMessage());
        }
        return null;
    }

    /**
     * Creates the kits and tools description.
     *
     * @param accordionNode the accordion node
     * @param data          the data
     */
    private void createKitsAndToolsDescription(Node accordionNode, KitsAndToolsPageData data) {
        final String descText = data.getDescription();
        if (StringUtils.isNotBlank(descText)) {
            List<Integer> indicesList = MigrationUtils.findAllIndicesOfGivenString(descText, "<h2 class=");
            if (!indicesList.isEmpty()) {
                for (int index = 0; index < indicesList.size(); index++) {
                    if (index == indicesList.size() - 1) {
                        String parseStr = descText.substring(indicesList.get(index));
                        findCompType(accordionNode, parseStr);
                    } else {
                        String parseStr = descText.substring(indicesList.get(index), indicesList.get(index + 1));
                        findCompType(accordionNode, parseStr);
                    }
                }
            }
        }
    }

    /**
     * Check and get given node.
     *
     * @param givenNode     the given node
     * @param childNodeName the child node name
     * @return the node
     */
    private Node checkAndGetGivenNode(Node givenNode, String childNodeName) {
        try {
            if (StringUtils.isNotBlank(childNodeName)) {
                return givenNode.hasNode(childNodeName) ? givenNode.getNode(childNodeName)
                        : givenNode.addNode(childNodeName);
            }
        } catch (Exception exec) {
            logger.error("Exception in checkAndGetGivenNode method::{}", exec.getMessage());
        }
        return null;
    }

    /**
     * Find comp type.
     *
     * @param accordionNode the accordion node
     * @param parseString   the parse string
     */
    private void findCompType(Node accordionNode, String parseString) {
        String reqText = parseString.substring(parseString.indexOf(H2_ELE_END_TAG), parseString.length());
        if (MigrationUtils.isMatchedRegex(KITS_PURPOSE_REGEX, parseString)) {
            createPurposeTextForKitsPage(accordionNode, reqText);
        } else if (MigrationUtils.isMatchedRegex(KITS_REC_UPDATES_REGEX, parseString)) {
            Node recentUpdatesNode = checkAndGetGivenNode(accordionNode, "container_recent_updates");
                findForImageSource(recentUpdatesNode, reqText, "text", "kits-recentupdates");
        } else if (MigrationUtils.isMatchedRegex(KITS_USE_CASE_REGEX, parseString)) {
            Node useCaseNode = checkAndGetGivenNode(accordionNode, "container_use_case");
                findForImageSource(useCaseNode, reqText, "text", "kits-usecase");
        } else if (MigrationUtils.isMatchedRegex(KITS_FILES_REGEX, parseString)) {
            Node filesNode = checkAndGetGivenNode(accordionNode, "container_files");
                findForImageSource(filesNode, reqText, "text", "kits-files");
        } else if (MigrationUtils.isMatchedRegex(KITS_PRE_REQ_REGEX, parseString)) {
            Node preReqNode = checkAndGetGivenNode(accordionNode, "container_prerequisites");
                findForImageSource(preReqNode, reqText, "text", "kits-prereq");
        } else if (MigrationUtils.isMatchedRegex(KITS_DESIGN_NOTES_REGEX, parseString)) {
            Node designNotesNode = checkAndGetGivenNode(accordionNode, "container_design_notes");
                findForImageSource(designNotesNode, reqText, "text", "kits-designnotes");
        } else if (MigrationUtils.isMatchedRegex(KITS_IMPL_NOTES_REGEX, parseString)) {
            Node implNotesNode = checkAndGetGivenNode(accordionNode, "container_implementation_notes");
                findForImageSource(implNotesNode, reqText, "text", "kits-implnotes");
        } else if (MigrationUtils.isMatchedRegex(KITS_STEPS_REGEX, parseString)) {
            Node stepsNode = checkAndGetGivenNode(accordionNode, "container_steps");
                findForImageSource(stepsNode, reqText, "text", "kits-steps");
        }
    }

    

    /**
     * Creates the purpose text for kits page.
     *
     * @param accordionNode the accordion node
     * @param reqText       the req text
     */
    private void createPurposeTextForKitsPage(Node accordionNode, String reqText) {
        try {
            Node mainNode = accordionNode.getParent();
            MigrationUtils.createCoreTextComponent(mainNode, reqText, "text", "kits-purpose");
        } catch (Exception exec) {
            logger.error("Exception in findCompType method::{}", exec.getMessage());
        }
    }

    /**
     * Find for image source.
     *
     * @param node     the node
     * @param txt      the txt
     * @param nodeName the node name
     * @param compId   the comp id
     */
    private void findForImageSource(Node node, String txt, String nodeName, String compId) {
        if(null != node){
            Pattern p = Pattern.compile(IMG_TAG_REGEX, Pattern.CASE_INSENSITIVE);
            Matcher m = p.matcher(txt);
            int counter = 1;
            while (m.find()) {
                String word = m.group(1);
                String priorString = txt.substring(0, txt.indexOf(word));
                MigrationUtils.createCoreTextComponent(node, priorString, nodeName + "_" + counter, compId);
                extractImageSrc(node, word, counter);
                String remString = txt.substring(txt.indexOf(word) + word.length(), txt.length() - 1);
                txt = remString;
                counter++;
            }
            // default text node with data
            MigrationUtils.createCoreTextComponent(node, txt, nodeName + "_0", compId);
        }
    }

    /**
     * Extract image src.
     *
     * @param node         the node
     * @param imageTagData the image tag data
     * @param counter      the counter
     */
    private void extractImageSrc(Node node, String imageTagData, int counter) {
        Pattern p = Pattern.compile(IMG_SRC_REGX, Pattern.CASE_INSENSITIVE);
        Matcher m = p.matcher(imageTagData);
        while (m.find()) {
            String word = m.group(1);
            createCoreImageComponent(node, word, "image" + "_" + counter);
        }
    }

    /**
     * Creates the core image component.
     *
     * @param parentNode    the parent node
     * @param imgSrc        the img src
     * @param imageNodeName the node name
     */
    private void createCoreImageComponent(Node parentNode, final String imgSrc, final String imageNodeName) {
        try {
            Map<String, String> map = new HashMap<>();
            map.put(MigrationConstants.AEM_SLING_RESOURCE_TYPE_PROP, "community/components/image");
            map.put("fileReference", imgSrc);
            map.put("linkTarget", "_self");
            map.put("imageFromPageImage", "false");
            MigrationUtils.createCoreImageComponent(parentNode, imageNodeName, map);
        } catch (Exception exec) {
            logger.error("Exception in createCoreTextComponent method::{}", exec.getMessage());
        }
    }

    /**
     * Sets the page props.
     *
     * @param jcrNode          the jcr node
     * @param data             the data
     * @param resourceResolver the resource resolver
     */
    private void setPageProps(final Node jcrNode, final KitsAndToolsPageData data, ResourceResolver resourceResolver) {
        try {
            if (StringUtils.isNotBlank(data.getDrupalNodeId())) {
                jcrNode.setProperty(MigrationConstants.DRUPAL_NODE_ID, Long.parseLong(data.getDrupalNodeId()));
            }

            if (StringUtils.isNotBlank(data.getWorkflowStatus())) {
                jcrNode.setProperty(MigrationConstants.WORKFLOW_STATUS, data.getWorkflowStatus());
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

    /**
     * Collect all tags for given page.
     *
     * @param resourceResolver the resource resolver
     * @param data             the data
     */
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
