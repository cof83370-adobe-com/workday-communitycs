package com.workday.community.aem.migration.services.impl;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.jcr.Node;
import javax.jcr.Session;

import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.propertytypes.ServiceDescription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.day.cq.tagging.Tag;
import com.day.cq.wcm.api.Page;
import com.day.cq.wcm.api.PageManager;
import com.workday.community.aem.migration.constants.MigrationConstants;
import com.workday.community.aem.migration.models.EventPageData;
import com.workday.community.aem.migration.models.PageNameBean;
import com.workday.community.aem.migration.services.PageCreationService;
import com.workday.community.aem.migration.utils.TagFinderEnum;
import com.workday.community.aem.migration.utils.MigrationUtils;

/**
 * The Class PageCreationServiceImpl.
 * 
 * @author pepalla
 */
@Component(immediate = true, service = PageCreationService.class, property = { "type=events-page" })
@ServiceDescription("Workday - Events Page Creation Service")
public class EventsPageCreationService implements PageCreationService {

    /** The logger. */
    private final Logger logger = LoggerFactory.getLogger(this.getClass().getName());

    /** The Constant DEFAULTEVENTLOCATION. */
    private static final String DEFAULTEVENTLOCATION = "Virtual";

    /** The Constant DEFAULTEVENTHOST. */
    private static final String DEFAULTEVENTHOST = "workday";

    /** The Constant CONTAINER. */
    private static final String CONTAINER = "container";

    /** The Constant CENTERCONTAINER. */
    private static final String CENTERCONTAINER = "centercontainer";

    /** The Constant TOPRIGHTCONTAINER. */
    private static final String TOPRIGHTCONTAINER = "toprightcontainer";

    /** The Constant EVENTREGISTRATIONCONTAINER. */
    private static final String EVENTREGISTRATIONCONTAINER = "eventregistration";

    /** The Constant HOST. */
    private static final String HOST = "host";

    /** The Constant LOCATION. */
    private static final String LOCATION = "location";

    /** The Constant TRUE. */
    private static final String TRUE = "true";

    /** The Constant JCR_TITLE. */
    private static final String JCR_TITLE = "jcr:title";

    /** The Constant TYPE. */
    private static final String TYPE = "type";

    /** The Constant TEXT_IS_RICH_PROP. */
    private static final String TEXT_IS_RICH_PROP = "textIsRich";

    /** The Constant TEXT. */
    private static final String TEXT = "text";

    /** The Constant JCR_SQL2. */
    private static final String JCR_SQL2 = "JCR-SQL2";

    /** The Constant RETIREMENT_DATE. */
    private static final String RETIREMENT_DATE = "retirementDate";

    /** The Constant READ_COUNT. */
    private static final String READ_COUNT = "readCount";

    /** The Constant UPDATED_DATE. */
    private static final String UPDATED_DATE = "updatedDate";

    /** The Constant EVENT_TYPE. */
    private static final String EVENT_TYPE = "eventType";

    /** The Constant START_DATE. */
    private static final String START_DATE = "startDate";

    /** The Constant END_DATE. */
    private static final String END_DATE = "endDate";

    /** The Constant TEXT_DESC. */
    private static final String TEXT_DESC = "text_desc";

    /** The Constant TITLE_DESC. */
    private static final String TITLE_DESC = "title_desc";

    /** The Constant EVENT_DESCRIPTION. */
    private static final String EVENT_DESCRIPTION = "Event Description";

    /** The Constant STYLE_TAG_START. */
    private static final String  STYLE_TAG_START = "<style";
    
    /** The Constant STYLE_TAG_END. */
    private static final String  STYLE_TAG_END = "</style>";

    /** The resolver factory. */
    @Reference
    private ResourceResolverFactory resolverFactory;

    /** The wd service param. */
    Map<String, Object> wdServiceParam = Collections.singletonMap(ResourceResolverFactory.SUBSERVICE,
            "workday-community-administrative-service");

    Node jcrNode = null;

    /**
     * Get JCR node.
     * 
     * @return jcr node
     */
    public Node getJcrNode() {
        return jcrNode;
    }

    String aemPageName = StringUtils.EMPTY;

    /**
     * Creates page in JCR with with given inputs.
     *
     * @param paramsMap the params map
     * @param data      the data
     * @param list      the page name bean list
     */
    @Override
    public void doCreatePage(final Map<String, String> paramsMap, EventPageData data, List<PageNameBean> list) {
        try (ResourceResolver resourceResolver = resolverFactory.getServiceResourceResolver(wdServiceParam)) {
            Session session = resourceResolver.adaptTo(Session.class);
            if (session != null) {
                // Derive the page title and page name.
                final String nodeId = data.getNid();
                String aemPageTitle = data.getTitle();
                getAemPageName(list, nodeId);
                // Create Page.
                final Page prodPage = getPageCreated(resourceResolver, paramsMap, aemPageTitle);
                if (null != prodPage) {
                    jcrNode = prodPage.hasContent() ? prodPage.getContentResource().adaptTo(Node.class) : null;
                }
                if (null == jcrNode) {
                    return;
                }

                // set Page properties.
                setPageProps(jcrNode, data, resourceResolver);
                Node rootNode = jcrNode.hasNode("root") ? jcrNode.getNode("root") : jcrNode.addNode("root");

                Node containerNode = rootNode.hasNode(CONTAINER) ? rootNode.getNode(CONTAINER)
                        : rootNode.addNode(CONTAINER);

                // Creation of Register for Event Core Button component.
                Node eventRegistrationContainer = containerNode.hasNode(EVENTREGISTRATIONCONTAINER)
                        ? containerNode.getNode(EVENTREGISTRATIONCONTAINER)
                        : containerNode.addNode(EVENTREGISTRATIONCONTAINER);
                createRegisterForEventCoreButton(eventRegistrationContainer, data);

                // TODO top right container image.

                // Creation of event details component.
                Node toprightContainer = containerNode.hasNode(TOPRIGHTCONTAINER)
                        ? containerNode.getNode(TOPRIGHTCONTAINER)
                        : containerNode.addNode(TOPRIGHTCONTAINER);

                createEventDetailsComponent(toprightContainer, data);

                // Creation of event description component.
                Node centerContainer = containerNode.hasNode(CENTERCONTAINER)
                        ? containerNode.getNode(CENTERCONTAINER)
                        : containerNode.addNode(CENTERCONTAINER);

                createEventDescription(centerContainer, data);

                // TODO top bottom left container.
                // TODO top bottom right image.
                saveToRepo(session);
            }
        } catch (Exception exec) {
            logger.error("Exception occurred while creating page in doCreatePage::{}", exec.getMessage());
        }
    }

    /**
     * Gets the page object created for given inputs.
     * 
     * @param   resourceResolver the resource resolver
     * @param   paramsMap        the parameter map
     * @param   aemPageTitle     the aem page title
     * @return                   the created page object
     */
    private Page getPageCreated(ResourceResolver resourceResolver, final Map<String, String> paramsMap,
            final String aemPageTitle) {
        Page prodPage = null;
        try {
            PageManager pageManager = resourceResolver.adaptTo(PageManager.class);
            prodPage = pageManager.create(paramsMap.get(MigrationConstants.PARENT_PAGE_PATH_PARAM),
                    aemPageName, paramsMap.get(MigrationConstants.TEMPLATE_PARAM), aemPageTitle);
        } catch (Exception exec) {
            logger.error("Exception occurred while creating page in getPageCreated::{}", exec.getMessage());
        }
        return prodPage;
    }

    /**
     * Save page to JCR repository.
     * 
     * @param session the JCR session
     */
    private void saveToRepo(Session session) {
        try {
            session.save();
            session.refresh(true);
        } catch (Exception exec) {
            logger.error("Exception occurred while saving to repo::{}", exec.getMessage());
        }

    }

    /**
     * Sets the page props.
     *
     * @param jcrNode          the jcr node
     * @param data             the data
     * @param resourceResolver the resource resolver
     */
    private void setPageProps(final Node jcrNode, final EventPageData data, ResourceResolver resourceResolver) {
        try {
            if (StringUtils.isNotBlank(data.getRetirementDate())) {
                Calendar retirementDate = MigrationUtils.convertStrToAemCalInstance(data.getRetirementDate(),
                        MigrationConstants.EventsPageConstants.YYYY_MM_DD_FORMAT);
                // Add one day.
                retirementDate.add(Calendar.DATE, 1);
                jcrNode.setProperty(RETIREMENT_DATE, retirementDate);
            }
            if (StringUtils.isNotBlank(data.getReadcount())) {
                jcrNode.setProperty(READ_COUNT, Long.parseLong(data.getReadcount()));
            }
            if (StringUtils.isNotBlank(data.getUpdatedDate())) {
                String dateStr = MigrationUtils.getDateStringFromEpoch(Long.parseLong(data.getUpdatedDate()));
                Calendar updatedDate = MigrationUtils.convertStrToAemCalInstance(dateStr,
                        MigrationConstants.EventsPageConstants.MMM_DD_COMMA_YYYY_FORMAT);
                // Add one day.
                updatedDate.add(Calendar.DATE, 1);
                jcrNode.setProperty(UPDATED_DATE, updatedDate);
            }
            if (StringUtils.isNotBlank(data.getStartDate())) {
                Calendar startDateCal = MigrationUtils.convertStrToAemCalInstance(data.getStartDate(),
                        MigrationConstants.AEM_CAL_INSTANCE_FORMAT);
                // Add one hour.
                startDateCal.add(Calendar.HOUR_OF_DAY, 1);
                jcrNode.setProperty(START_DATE, startDateCal);
            }
            if (StringUtils.isNotBlank(data.getEndDate())) {
                Calendar endDateCal = MigrationUtils.convertStrToAemCalInstance(data.getEndDate(),
                        MigrationConstants.AEM_CAL_INSTANCE_FORMAT);
                // Add one hour.
                endDateCal.add(Calendar.HOUR_OF_DAY, 1);
                jcrNode.setProperty(END_DATE, endDateCal);
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
     * @param data             the event page data
     */
    private void collectAllTagsForGivenPage(ResourceResolver resourceResolver, final EventPageData data) {
        // To add event tags.
        createEventTypePageTags(resourceResolver, data);
        // To add release tags.
        if (StringUtils.isNotBlank(data.getReleaseTag())) {
            List<String> releaseTags = Optional
                    .ofNullable(
                            getTagsForGivenInputs(resourceResolver, TagFinderEnum.RELEASE_TAG, data.getReleaseTag()))
                    .orElse(new ArrayList<>());
            mountTagPageProps(MigrationConstants.TagPropertyName.RELEASE, releaseTags);
        }

        // To add product tags.
        if (StringUtils.isNotBlank(data.getProduct())) {
            List<String> productTags = Optional
                    .ofNullable(getTagsForGivenInputs(resourceResolver, TagFinderEnum.PRODUCT, data.getProduct()))
                    .orElse(new ArrayList<>());
            mountTagPageProps(MigrationConstants.TagPropertyName.PRODUCT, productTags);
        }

        // To add using workday tags.
        if (StringUtils.isNotBlank(data.getUsingWorkday())) {
            List<String> usingWorkdayTags = Optional
                    .ofNullable(
                            getTagsForGivenInputs(resourceResolver, TagFinderEnum.USING_WORKDAY, data.getUsingWorkday()))
                    .orElse(new ArrayList<>());
            mountTagPageProps(MigrationConstants.TagPropertyName.USING_WORKDAY, usingWorkdayTags);
        }
        // TODO To add industry tags.
    }

    /**
     * Mount page tags.
     * 
     * @param key           the key
     * @param givenTagList  the given tag list
     */
    private void mountTagPageProps(final String key, List<String> givenTagList) {
        try {
            if (!givenTagList.isEmpty()) {
                getJcrNode().setProperty(key, givenTagList.stream().toArray(String[]::new));
            }
        } catch (Exception exec) {
            logger.error("Exception occurred while adding tags as Page props:{}", exec.getMessage());
        }

    }

    /**
     * To add event format and event audience tags.
     * 
     * @param resourceResolver the resource resolver
     * @param data             the event page data
     */
    private void createEventTypePageTags(ResourceResolver resourceResolver, final EventPageData data) {
        // To add event type and format tags.
        if (StringUtils.isNotBlank(data.getCalendarEventType())) {
            List<String> eventTypeTags = Optional.ofNullable(getTagsForGivenInputs(resourceResolver,
                    TagFinderEnum.CALENDAREVENTTYPE, data.getCalendarEventType())).orElse(new ArrayList<>());
            if (!eventTypeTags.isEmpty()) {
                List<String> eventFormatTags = new ArrayList<>();
                List<String> eventAudienceTags = new ArrayList<>();
                for (String tagId : eventTypeTags) {
                    if (tagId.contains("event:event-format/")) {
                        eventFormatTags.add(tagId);
                    } else if (tagId.contains("event:event-audience/")) {
                        eventAudienceTags.add(tagId);
                    }
                }
                mountEventTypePageTags(eventFormatTags, eventAudienceTags);
            }
        }
    }

    /**
     * Mount event type page tags.
     * 
     * @param eventFormatTags   the event format tags
     * @param eventAudienceTags the event audience tags
     */
    private void mountEventTypePageTags(final List<String> eventFormatTags, final List<String> eventAudienceTags) {
        if (!eventFormatTags.isEmpty()) {
            mountTagPageProps(MigrationConstants.TagPropertyName.EVENT_FORMAT, eventFormatTags);
        }
        if (!eventAudienceTags.isEmpty()) {
            mountTagPageProps(MigrationConstants.TagPropertyName.EVENT_AUDIENCE, eventAudienceTags);
        }
    }

    /**
     * Gets the tags for given inputs.
     *
     * @param resourceResolver the resource resolver
     * @param tagFinderEnum    the tag finder enum
     * @param tagTypeValue     the tag type value
     * @return                 the tags for given inputs
     */
    private List<String> getTagsForGivenInputs(ResourceResolver resourceResolver, TagFinderEnum tagFinderEnum,
            final String tagTypeValue) {
        return Optional.ofNullable(tagFinderUtil(resourceResolver, tagFinderEnum.getValue(), tagTypeValue))
                .orElse(new ArrayList<>());
    }

    /**
     * Collect page tags.
     *
     * @param resourceResolver the resource resolver
     * @param tagRootPath      the tag root path
     * @param tagTitle         the tag title
     * @return the list
     */
    private List<String> tagFinderUtil(ResourceResolver resourceResolver, final String tagRootPath,
            final String tagTitle) {
        Iterator<Resource> tagResources = doQueryForTag(resourceResolver, tagRootPath, tagTitle);
        Set<String> tagsSet = new HashSet<>();
        if (null != tagResources) {
            while (tagResources.hasNext()) {
                Resource artcileResource = tagResources.next();
                if (null != artcileResource) {
                    Tag tag = artcileResource.adaptTo(Tag.class);
                    tagsSet.add(tag.getTagID());
                }
            }
        }
        return tagsSet.stream().collect(Collectors.toList());
    }

    /**
     * SELECT * FROM [cq:Tag] AS tag
     * WHERE ISDESCENDANTNODE(tag, "/content/cq:tags/event") AND
     * [sling:resourceType] = 'cq/tagging/components/tag' AND ([jcr:title] =
     * 'Rising'
     * OR [jcr:title] = 'Webinar').
     *
     * @param resourceResolver the resource resolver
     * @param searchPath       the search path
     * @param tagTitle         the tag title
     * @return                 the tag resource iterator
     */
    private Iterator<Resource> doQueryForTag(ResourceResolver resourceResolver, String searchPath, String tagTitle) {
        String partialSqlStmt = "SELECT * FROM [cq:Tag] AS tag WHERE ISDESCENDANTNODE(tag, \"" + searchPath
                + "\") AND [sling:resourceType] = 'cq/tagging/components/tag' AND ";
        String[] diffTagsList = tagTitle.split(",");
        StringBuilder sbr = new StringBuilder();
        for (int index = 0; index < diffTagsList.length; index++) {
            if (index == 0 && StringUtils.isNotBlank(diffTagsList[index])) {
                sbr.append("[jcr:title] = '" + diffTagsList[index].trim() + "'");
            } else if (StringUtils.isNotBlank(diffTagsList[index])) {
                sbr.append(" OR [jcr:title] = '" + diffTagsList[index].trim() + "'");
            }
        }
        String sqlStmt = String.format("%s%s%s%s", partialSqlStmt, "(", sbr.toString(), ")");
        logger.info("Query sql_stmt: {}", sqlStmt);
        return resourceResolver.findResources(sqlStmt, JCR_SQL2);
    }

    /**
     * Find comp type.
     *
     * @param innerContainer the inner container
     * @param parseString    the parse string
     */
    public void findCompType(Node innerContainer, String parseString) {
        final String eventDescTitle = "<h2 id=\"event-description\">Event Description</h2>";
        final String eventRegistrationTitle = "<h2 id=\"event-registration\">Registration Information</h2>";
        final String eventPreReadTitle = "<h2 id=\"event-prereading\">Pre Reading</h2>";
        final String eventAgendaTitle = "<h2 id=\"event-agenda\">Agenda</h2>";
        final String TEXT_AGENDA = "text_agenda";

        final String TITLE_AGENDA = "title_agenda";

        final String TEXT_PREREAD = "text_preread";

        final String TITLE_PREREAD = "title_preread";

        final String TITLE_REG = "title_reg";

        final String TEXT_REG = "text_reg";

        final String AGENDA = "Agenda";

        final String PRE_READING = "Pre Reading";

        final String REGISTRATION_INFORMATION = "Registration Information";

        if (parseString.startsWith(eventDescTitle)) {
            createCoreTitleComponent(innerContainer, EVENT_DESCRIPTION, TITLE_DESC);
            createCoreTextComponent(innerContainer, parseString.replace(eventDescTitle, StringUtils.EMPTY), TEXT_DESC);
        } else if (parseString.startsWith(eventRegistrationTitle)) {
            createCoreTitleComponent(innerContainer, REGISTRATION_INFORMATION, TITLE_REG);
            createCoreTextComponent(innerContainer, parseString.replace(eventRegistrationTitle, StringUtils.EMPTY),
                    TEXT_REG);
        } else if (parseString.startsWith(eventPreReadTitle)) {
            createCoreTitleComponent(innerContainer, PRE_READING, TITLE_PREREAD);
            createCoreTextComponent(innerContainer, parseString.replace(eventPreReadTitle, StringUtils.EMPTY),
                    TEXT_PREREAD);
        } else if (parseString.startsWith(eventAgendaTitle)) {
            createCoreTitleComponent(innerContainer, AGENDA, TITLE_AGENDA);
            createCoreTextComponent(innerContainer, parseString.replace(eventAgendaTitle, StringUtils.EMPTY),
                    TEXT_AGENDA);
        }
    }

    /**
     * Creates the core text component.
     *
     * @param innerContainer the inner container
     * @param richText       the rich text
     * @param nodeName       the node name
     */
    private void createCoreTextComponent(Node innerContainer, final String richText, final String nodeName) {
        try {
            Node textCompNode = innerContainer.addNode(nodeName);
            textCompNode.setProperty(MigrationConstants.AEM_SLING_RESOURCE_TYPE_PROP,
                    MigrationConstants.TEXT_COMP_SLING_RESOURCE);
            textCompNode.setProperty(TEXT, removeStyleElementsWithCDATA(richText));
            textCompNode.setProperty(TEXT_IS_RICH_PROP, TRUE);
        } catch (Exception exec) {
            logger.error("Exception in createCoreTextComponent method::{}", exec.getMessage());
        }
    }

    /**
     * Creates the core title component.
     *
     * @param innerContainer the inner container
     * @param plainText      the plain text
     * @param nodeName       the node name
     */
    private void createCoreTitleComponent(Node innerContainer, String plainText, final String nodeName) {
        try {
            Node titleCompNode = innerContainer.addNode(nodeName);
            titleCompNode.setProperty(MigrationConstants.AEM_SLING_RESOURCE_TYPE_PROP,
                    MigrationConstants.TITLE_COMP_SLING_RESOURCE);
            titleCompNode.setProperty(JCR_TITLE, plainText);
            titleCompNode.setProperty(TYPE, "h2");
        } catch (Exception exec) {
            logger.error("Exception in createCoreTitleComponent method::{}", exec.getMessage());
        }
    }

    /**
     * Creates the event description.
     *
     * @param innerContainer the inner container
     * @param data           the data
     */
    private void createEventDescription(Node innerContainer, EventPageData data) {
        final String descText = data.getDescription();
        if (StringUtils.isNotBlank(descText)) {
            List<Integer> indicesList = findAllIndicesOfGivenString(descText, "<h2 id=");
            if (!indicesList.isEmpty()) {
                for (int index = 0; index < indicesList.size(); index++) {
                    if (index == indicesList.size() - 1) {
                        findCompType(innerContainer, descText.substring(indicesList.get(index)));
                    } else {
                        findCompType(innerContainer,
                                descText.substring(indicesList.get(index), indicesList.get(index + 1)));
                    }
                }
            } else {
                /**
                 * It executes, if not find event desc or registration, agenda or pre reading
                 * id's on xml description node data
                 */
                createCoreTitleComponent(innerContainer, EVENT_DESCRIPTION, TITLE_DESC);
                createCoreTextComponent(innerContainer, descText, TEXT_DESC);
            }

        }
    }

    /**
     * Find all indices of given string.
     *
     * @param sourceTextString the source text string
     * @param searchWord       the search word
     * @return                 the index list for the search word
     */
    private List<Integer> findAllIndicesOfGivenString(String sourceTextString, String searchWord) {
        List<Integer> indexes = new ArrayList<>();
        int wordLength = 0;
        int index = 0;
        while (index != -1) {
            index = sourceTextString.indexOf(searchWord, index + wordLength); // Slight improvement
            if (index != -1) {
                indexes.add(index);
            }
            wordLength = searchWord.length();
        }
        return indexes;
    }

    /**
     * Creates the event registration core button component.
     *
     * @param innerContainer the inner container
     * @param data           the data
     */
    private void createRegisterForEventCoreButton(Node innerContainer, EventPageData data) {
        try {
            Node registerForEventButtonNode;
            if (StringUtils.isNotBlank(data.getRegistrationUrl())) {
                if (innerContainer.hasNode(MigrationConstants.BUTTON_COMP_NODE_NAME)) {
                    registerForEventButtonNode = innerContainer.getNode(MigrationConstants.BUTTON_COMP_NODE_NAME);
                } else {
                    registerForEventButtonNode = innerContainer.addNode(MigrationConstants.BUTTON_COMP_NODE_NAME);
                    registerForEventButtonNode.setProperty(MigrationConstants.AEM_SLING_RESOURCE_TYPE_PROP,
                            MigrationConstants.BUTTON_COMP_SLING_RESOURCE);
                }
                registerForEventButtonNode.setProperty(MigrationConstants.JCR_TITLE_PROP,
                        MigrationConstants.EventsPageConstants.TEXT_REGISTER_FOR_EVENT);
                registerForEventButtonNode.setProperty(MigrationConstants.LINK_TARGET_PROP,
                        MigrationConstants.TEXT_UNDERSCORE_SELF);
                registerForEventButtonNode.setProperty(MigrationConstants.LINK_URL_PROP, data.getRegistrationUrl());
                // TODO Find if default accessibilityLabel needed.

            } else {
                logger.info("Event Registration URL not found for nid : {}", data.getNid());
                if (innerContainer.hasNode(MigrationConstants.BUTTON_COMP_NODE_NAME))
                    innerContainer.getNode(MigrationConstants.BUTTON_COMP_NODE_NAME).remove();
            }

        } catch (Exception exec) {
            logger.error("Exception in createRegisterForEventCoreButton method::{}", exec.getMessage());
        }
    }

    /**
     * Creates the event details component.
     *
     * @param innerContainer the inner container
     * @param data           the data
     */
    private void createEventDetailsComponent(Node innerContainer, EventPageData data) {
        try {
            Node eventDetailsNode = innerContainer
                    .hasNode(MigrationConstants.EventsPageConstants.EVENT_DETAILS_COMP_NODE_NAME)
                            ? innerContainer.getNode(MigrationConstants.EventsPageConstants.EVENT_DETAILS_COMP_NODE_NAME)
                            : innerContainer.addNode(MigrationConstants.EventsPageConstants.EVENT_DETAILS_COMP_NODE_NAME);

            eventDetailsNode.setProperty(MigrationConstants.AEM_SLING_RESOURCE_TYPE_PROP,
                    MigrationConstants.EventsPageConstants.EVENT_DETAILS_SLING_RESOURCE);
            eventDetailsNode.setProperty(EVENT_TYPE, data.getCalendarEventType());

            eventDetailsNode.setProperty(LOCATION, DEFAULTEVENTLOCATION);
            eventDetailsNode.setProperty(HOST, DEFAULTEVENTHOST);
        } catch (Exception exec) {
            logger.error("Exception in createEventDetailsComponent method::{}", exec.getMessage());
        }
    }

    /**
     * Gets the AEM page name for given drupal node id.
     * 
     * @param list   the page name bean list
     * @param nodeId the node id
     * @return       the aem page name for given node id
     */
    private String getAemPageName(List<PageNameBean> list, final String nodeId) {
        list.stream().forEach((item) -> {
            if (item.getNodeId().equalsIgnoreCase(nodeId)) {
                String[] pathArray = item.getTitle().split("/");
                if (pathArray.length > 1) {
                    aemPageName = pathArray[pathArray.length - 1].trim().replace(".html", StringUtils.EMPTY);
                }
                if (pathArray.length == 1) {
                    aemPageName = item.getTitle().trim();
                }
                aemPageName = aemPageName.replaceAll("\\s+", "-");
            }
        });
        return aemPageName;
    }

    /**
     * Remove all style tags that has CDATA from the text content.
     * 
     * @param textContent the text content
     * @return            the cleaned up text content
     */
    private String removeStyleElementsWithCDATA(String textContent) {
        int styleStartIndex = textContent.indexOf(STYLE_TAG_START);
        int styleEndIndex = textContent.indexOf(STYLE_TAG_END);
        if (styleStartIndex >= 0 && styleEndIndex >= 0 && textContent.contains("[CDATA")) {
            String styleCdataBlock = textContent.substring(styleStartIndex, styleEndIndex + 8);
            textContent = textContent.replace(styleCdataBlock, StringUtils.EMPTY);
            removeStyleElementsWithCDATA(textContent);
        }
        return textContent;
    }

}
