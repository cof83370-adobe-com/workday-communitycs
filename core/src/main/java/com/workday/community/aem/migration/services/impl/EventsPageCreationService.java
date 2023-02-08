package com.workday.community.aem.migration.services.impl;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
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
import com.workday.community.aem.migration.models.CompAttributes;
import com.workday.community.aem.migration.models.EventPageData;
import com.workday.community.aem.migration.models.PageNameBean;
import com.workday.community.aem.migration.services.PageCreationService;
import com.workday.community.aem.migration.utils.MigrationUtils;
import com.workday.community.aem.migration.utils.TagFinderEnum;

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

    /** The registration information. */
    private static final String REGISTRATION_INFORMATION = "Registration Information";

    /** The title reg. */
    private static final String TITLE_REG = "title_reg";

    /** The text reg. */
    private static final String TEXT_REG = "text_reg";

    /** The pre reading. */
    private static final String PRE_READING = "Pre Reading";

    /** The text preread. */
    private static final String TEXT_PREREAD = "text_preread";

    /** The title preread. */
    private static final String TITLE_PREREAD = "title_preread";

    /** The agenda. */
    private static final String AGENDA = "Agenda";

    /** The text agenda. */
    private static final String TEXT_AGENDA = "text_agenda";

    /** The title agenda. */
    private static final String TITLE_AGENDA = "title_agenda";

    /** The event desc title. */
    private static final String EVENT_DESC_REGEX = "(?s)^<h2 class=(.*)title__h2(.*)>(.*)Event Description(.*)<\\/h2>(.*)$";

    /** The event registration title. */
    private static final String EVENT_REG_REGEX = "(?s)^<h2 class=(.*)title__h2(.*)>(.*)Registration Information(.*)<\\/h2>(.*)$";

    /** The event pre read title. */
    private static final String EVENT_PRE_READ_REGEX = "(?s)^<h2 class=(.*)title__h2(.*)>(.*)Pre Reading(.*)<\\/h2>(.*)$";

    /** The event agenda title. */
    private static final String EVENT_AGENDA_REGEX = "(?s)^<h2 class=(.*)title__h2(.*)>(.*)Agenda(.*)<\\/h2>(.*)$";

    /** The h2 element end tag. */
    private static final String H2_ELE_END_TAG = "</h2>";

    /** The Constant EVENT_DESC_COMP_ID. */
    private static final String EVENT_DESC_COMP_ID = "event-description";

    /** The Constant COUNTER. */
    private static final String COUNTER = "counter";

    /** The resolver factory. */
    @Reference
    private ResourceResolverFactory resolverFactory;

    /** The wd service param. */
    Map<String, Object> wdServiceParam = Collections.singletonMap(ResourceResolverFactory.SUBSERVICE,
            "workday-community-administrative-service");

    /** The jcr node. */
    Node jcrNode = null;

    /** The aem page name. */
    String aemPageName = StringUtils.EMPTY;

    /**
     * Gets the jcr node.
     *
     * @return the jcr node
     */
    public Node getJcrNode() {
        return jcrNode;
    }

    /**
     * Creates page in JCR with with given inputs.
     *
     * @param paramsMap  the params map
     * @param objectData the object data
     * @param list       the page name bean list
     */
    @Override
    public void doCreatePage(final Map<String, String> paramsMap, Object objectData, List<PageNameBean> list) {
        try (ResourceResolver resourceResolver = resolverFactory.getServiceResourceResolver(wdServiceParam)) {
            Session session = resourceResolver.adaptTo(Session.class);
            if (session != null) {
                // Derive the page title and page name.
                EventPageData data = (EventPageData) objectData;
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

                // set Page properties.
                setPageProps(jcrNode, data, resourceResolver);
                Node rootNode = jcrNode.hasNode("root") ? jcrNode.getNode("root") : jcrNode.addNode("root");
                createEventPageComponents(rootNode, data);
                MigrationUtils.saveToRepo(session);
            }
        } catch (Exception exec) {
            logger.error("Exception occurred while creating page in doCreatePage::{}", exec.getMessage());
        }
    }

    /**
     * Creates the event page components.
     *
     * @param rootNode the root node
     * @param data the data
     */
    private void createEventPageComponents(Node rootNode, EventPageData data) {
        try {
            Node containerNode = rootNode.hasNode(CONTAINER) ? rootNode.getNode(CONTAINER)
                    : rootNode.addNode(CONTAINER);

            // Creation of Register for Event Core Button component.
            Node eventRegistrationContainer = containerNode.hasNode(EVENTREGISTRATIONCONTAINER)
                    ? containerNode.getNode(EVENTREGISTRATIONCONTAINER)
                    : containerNode.addNode(EVENTREGISTRATIONCONTAINER);
            createRegisterForEventCoreButton(eventRegistrationContainer, data);

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
        } catch (Exception exec) {
            logger.error("Exception occurred in createEventPageComponents::{}", exec.getMessage());
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
                // Add one day.
                postedDate.add(Calendar.DATE, 1);
                jcrNode.setProperty(MigrationConstants.POSTED_DATE, postedDate);
            }

            if (StringUtils.isNoneBlank(data.getAccessControl())) {
                jcrNode.setProperty(MigrationConstants.DRUPAL_ACEESS_CONTROL, data.getAccessControl());
            }

            if (StringUtils.isNotBlank(data.getRetirementDate())) {
                Calendar retirementDate = MigrationUtils.convertStrToAemCalInstance(data.getRetirementDate(),
                        MigrationConstants.EventsPageConstants.YYYY_MM_DD_FORMAT);
                // Add one day.
                retirementDate.add(Calendar.DATE, 1);
                jcrNode.setProperty(MigrationConstants.RETIREMENT_DATE, retirementDate);
            }
            if (StringUtils.isNotBlank(data.getReadcount())) {
                jcrNode.setProperty(MigrationConstants.READ_COUNT, Long.parseLong(data.getReadcount()));
            }
            if (StringUtils.isNotBlank(data.getUpdatedDate())) {
                String dateStr = MigrationUtils.getDateStringFromEpoch(Long.parseLong(data.getUpdatedDate()));
                Calendar updatedDate = MigrationUtils.convertStrToAemCalInstance(dateStr,
                        MigrationConstants.EventsPageConstants.MMM_DD_COMMA_YYYY_FORMAT);
                // Add one day.
                updatedDate.add(Calendar.DATE, 1);
                jcrNode.setProperty(MigrationConstants.UPDATED_DATE, updatedDate);
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
    }

    /**
     * Creates the event type page tags.
     * To add event format and event audience tags
     * 
     * @param resourceResolver the resource resolver
     * @param data             the event page data
     */
    private void createEventTypePageTags(ResourceResolver resourceResolver, final EventPageData data) {
        // To add event type and format tags.
        if (StringUtils.isNotBlank(data.getCalendarEventType())) {
            List<String> eventTypeTags = Optional.ofNullable(MigrationUtils.getTagsForGivenInputs(resourceResolver,
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
            MigrationUtils.mountTagPageProps(getJcrNode(), MigrationConstants.TagPropertyName.EVENT_FORMAT,
                    eventFormatTags);
        }
        if (!eventAudienceTags.isEmpty()) {
            MigrationUtils.mountTagPageProps(getJcrNode(), MigrationConstants.TagPropertyName.EVENT_AUDIENCE,
                    eventAudienceTags);
        }
    }

    /**
     * Find comp type.
     *
     * @param innerContainer    the inner container
     * @param parseString       the parse string
     * @param resultMap         the result map
     * @param compAttributeList the comp attribute list
     * @return the map
     */
    public Map<String, Object> findCompType(Node innerContainer, String parseString, Map<String, Object> resultMap,
            List<CompAttributes> compAttributeList) {
        int count = (Integer) resultMap.get(COUNTER);
        if (MigrationUtils.isMatchedRegex(EVENT_DESC_REGEX, parseString)) {
            CompAttributes compAtr = new CompAttributes(1, EVENT_DESCRIPTION,
                    parseString.substring(parseString.indexOf(H2_ELE_END_TAG), parseString.length()));
            compAttributeList.add(compAtr);
            count++;
            resultMap.put(COUNTER, count);
        } else if (MigrationUtils.isMatchedRegex(EVENT_REG_REGEX, parseString)) {
            CompAttributes compAtr = new CompAttributes(2, REGISTRATION_INFORMATION,
                    parseString.substring(parseString.indexOf(H2_ELE_END_TAG), parseString.length()));
            compAttributeList.add(compAtr);
            count++;
            resultMap.put(COUNTER, count);
        } else if (MigrationUtils.isMatchedRegex(EVENT_PRE_READ_REGEX, parseString)) {
            CompAttributes compAtr = new CompAttributes(3, PRE_READING,
                    parseString.substring(parseString.indexOf(H2_ELE_END_TAG), parseString.length()));
            compAttributeList.add(compAtr);
            count++;
            resultMap.put(COUNTER, count);
        } else if (MigrationUtils.isMatchedRegex(EVENT_AGENDA_REGEX, parseString)) {
            CompAttributes compAtr = new CompAttributes(4, AGENDA,
                    parseString.substring(parseString.indexOf(H2_ELE_END_TAG), parseString.length()));
            compAttributeList.add(compAtr);
            count++;
            resultMap.put(COUNTER, count);
        }
        resultMap.put("compList", compAttributeList);
        return resultMap;
    }

    /**
     * Creates the components in order fashion.
     *
     * @param innerContainer    the inner container
     * @param compAttributeList the comp attribute list
     */
    private void createComponentsInOrderFashion(Node innerContainer,
            List<CompAttributes> compAttributeList) {
        if (!compAttributeList.isEmpty()) {
            Collections.sort(compAttributeList, CompAttributes.idComparator);
            for (int index = 0; index < compAttributeList.size(); index++) {
                if (compAttributeList.get(index).getId() == 1) {
                    createCoreTitleComponent(innerContainer, compAttributeList.get(index).getTitleVal(), TITLE_DESC);
                    MigrationUtils.createCoreTextComponent(innerContainer, compAttributeList.get(index).getTextVal(),
                            TEXT_DESC,
                            EVENT_DESC_COMP_ID);
                } else if (compAttributeList.get(index).getId() == 2) {
                    createCoreTitleComponent(innerContainer, compAttributeList.get(index).getTitleVal(), TITLE_REG);
                    MigrationUtils.createCoreTextComponent(innerContainer, compAttributeList.get(index).getTextVal(),
                            TEXT_REG,
                            "registration-information");
                } else if (compAttributeList.get(index).getId() == 3) {
                    createCoreTitleComponent(innerContainer, compAttributeList.get(index).getTitleVal(), TITLE_PREREAD);
                    MigrationUtils.createCoreTextComponent(innerContainer, compAttributeList.get(index).getTextVal(),
                            TEXT_PREREAD,
                            "pre-reading");
                } else if (compAttributeList.get(index).getId() == 4) {
                    createCoreTitleComponent(innerContainer, compAttributeList.get(index).getTitleVal(), TITLE_AGENDA);
                    MigrationUtils.createCoreTextComponent(innerContainer, compAttributeList.get(index).getTextVal(),
                            TEXT_AGENDA,
                            "agenda");
                }
            }
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
            titleCompNode.setProperty(MigrationConstants.JCR_TITLE_PROP, plainText);
            titleCompNode.setProperty(MigrationConstants.TYPE, "h2");
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
            List<Integer> indicesList = MigrationUtils.findAllIndicesOfGivenString(descText, "<h2 class=");
            if (!indicesList.isEmpty()) {
                int counter = 0;
                List<CompAttributes> compAttrList = new ArrayList<>();
                Map<String, Object> resultMap = new HashMap<>();
                resultMap.put(COUNTER, counter);
                String parseStr = "";
                for (int index = 0; index < indicesList.size(); index++) {
                    if (index == indicesList.size() - 1) {
                        parseStr = descText.substring(indicesList.get(index));
                        resultMap = findCompType(innerContainer, parseStr, resultMap, compAttrList);
                    } else {
                        parseStr = descText.substring(indicesList.get(index), indicesList.get(index + 1));
                        resultMap = findCompType(innerContainer, parseStr, resultMap, compAttrList);
                    }
                }
                processResultMap(innerContainer, resultMap, descText);
            } else {
                // It executes, if not find event desc or registration, agenda or pre reading
                // id's on xml description node data.
                createCoreTitleComponent(innerContainer, EVENT_DESCRIPTION, TITLE_DESC);
                MigrationUtils.createCoreTextComponent(innerContainer, descText, TEXT_DESC, EVENT_DESC_COMP_ID);
            }

        }
    }

    /**
     * Process result map.
     *
     * @param innerContainer the inner container
     * @param resultMap      the result map
     * @param descText       the desc text
     */
    private void processResultMap(Node innerContainer, Map<String, Object> resultMap, String descText) {
        if (null != resultMap && (Integer) resultMap.get(COUNTER) == 0) {
            createOnlyEventDescSection(innerContainer, descText);
        }
        if (null != resultMap) {
            @SuppressWarnings (value="unchecked")
            List<CompAttributes> compAttributeList = (List<CompAttributes>) resultMap.get("compList");
            if (!compAttributeList.isEmpty()) {
                createComponentsInOrderFashion(innerContainer, compAttributeList);
            }
        }
    }

    /**
     * Creates the only event desc section.
     *
     * @param innerContainer the inner container
     * @param descText       the desc text
     */
    private void createOnlyEventDescSection(Node innerContainer, final String descText) {
        createCoreTitleComponent(innerContainer, EVENT_DESCRIPTION, TITLE_DESC);
        MigrationUtils.createCoreTextComponent(innerContainer, descText, TEXT_DESC, EVENT_DESC_COMP_ID);
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
            } else {
                logger.info("Event Registration URL not found for drupalNodeId : {}", data.getDrupalNodeId());
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
                            ? innerContainer
                                    .getNode(MigrationConstants.EventsPageConstants.EVENT_DETAILS_COMP_NODE_NAME)
                            : innerContainer
                                    .addNode(MigrationConstants.EventsPageConstants.EVENT_DETAILS_COMP_NODE_NAME);

            eventDetailsNode.setProperty(MigrationConstants.AEM_SLING_RESOURCE_TYPE_PROP,
                    MigrationConstants.EventsPageConstants.EVENT_DETAILS_SLING_RESOURCE);
            eventDetailsNode.setProperty(EVENT_TYPE, data.getCalendarEventType());

            eventDetailsNode.setProperty(LOCATION, DEFAULTEVENTLOCATION);
            eventDetailsNode.setProperty(HOST, DEFAULTEVENTHOST);
        } catch (Exception exec) {
            logger.error("Exception in createEventDetailsComponent method::{}", exec.getMessage());
        }
    }
}