package com.workday.community.aem.core.services.impl;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;

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
import com.workday.community.aem.core.constants.GlobalConstants;
import com.workday.community.aem.core.models.EventPageData;
import com.workday.community.aem.core.services.PageCreationService;
import com.workday.community.aem.core.utils.TagFinderEnum;
import com.workday.community.aem.core.utils.WokdayUtils;


/**
 * The Class PageCreationServiceImpl.
 * 
 * @author pepalla
 */
@Component(immediate = true, service = PageCreationService.class, property={"type=events-page"})
@ServiceDescription("Workday - Events Page Creation Service")
public class EventsPageCreationService implements PageCreationService {

    /** The logger. */
    private final Logger logger = LoggerFactory.getLogger(this.getClass().getName());
    
    /** The Constant default_event_location. */
	private static final String DEFAULTEVENTLOCATION = "Virtual";
    
    /** The Constant default_event_host. */
	private static final String DEFAULTEVENTHOST = "workday";
    
    /** The Constant CONTAINER. */
    private static final String CONTAINER = "container";

    /** The Constant CENTERCONTAINER2. */
    private static final String CENTERCONTAINER = "centercontainer";

    /** The Constant TOPRIGHTCONTAINER2. */
    private static final String TOPRIGHTCONTAINER = "toprightcontainer";

    /** The Constant TOPRIGHTCONTAINER2. */
    private static final String EVENTREGISTRATIONCONTAINER = "eventregistration";

    /** The Constant HOST. */
    private static final String HOST = "host";

    /** The Constant LOCATION. */
    private static final String LOCATION = "location";

    /** The Constant REGISTER_LINK. */
    private static final String REGISTER_LINK = "registerLink";

    /** The Constant NEW_WINDOW. */
    private static final String NEW_WINDOW = "newWindow";

    /** The Constant FALSE. */
    private static final String FALSE = "false";

    /** The Constant TRUE. */
    private static final String TRUE = "true";

    /** The Constant SHOW_REGISTER_LINK. */
    private static final String SHOW_REGISTER_LINK = "showRegisterLink";

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

    /** The Constant EVENT_TAGS. */
    private static final String EVENT_TAGS = "eventTags";

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
    
    private static final String TEXT_DESC = "text_desc";

    private static final String TITLE_DESC = "title_desc";
    
    private static final String EVENT_DESCRIPTION = "Event Description";

    /** The resolver factory. */
    @Reference
    private ResourceResolverFactory resolverFactory;
    
    /** The wg service param. */
    Map<String, Object> wdServiceParam = Collections.singletonMap(ResourceResolverFactory.SUBSERVICE,
            "workday-community-administrative-service");
    /**
     * Do create page.
     *
     * @param paramsMap the params map
     * @param data the data
     */
    @Override
    public void doCreatePage(final Map<String, String> paramsMap, EventPageData data) {
        try (ResourceResolver resourceResolver = resolverFactory.getServiceResourceResolver(wdServiceParam)) {
            logger.debug("resourceResolver::{}", resourceResolver);
            Session session = resourceResolver.adaptTo(Session.class);
            if (session != null) {
                //Derive the page title and page name.
                String pageNameAttr = data.getTitle();
                String pageTitle = StringUtils.EMPTY;
                if (StringUtils.isNotBlank(pageNameAttr) && pageNameAttr.length() > 12) {
                    pageTitle = pageNameAttr.substring(0, 1).toUpperCase() + pageNameAttr.substring(1);
                    pageNameAttr = pageNameAttr.substring(0, 11).toLowerCase().replaceAll("\\s+", "_");
                } else if (StringUtils.isNotBlank(pageNameAttr) && pageNameAttr.length() <= 12) {
                    pageTitle = pageNameAttr.substring(0, 1).toUpperCase() + pageNameAttr.substring(1);
                    pageNameAttr = pageNameAttr.toLowerCase().replaceAll("\\s+", "_");
                } else {
                    logger.error("page name not provided in source file");
                    return;
                }
                
                // Create Page
                PageManager pageManager = resourceResolver.adaptTo(PageManager.class);
                Page prodPage = pageManager.create(paramsMap.get(GlobalConstants.PARENT_PAGE_PATH_PARAM), pageNameAttr, paramsMap.get(GlobalConstants.TEMPLATE_PARAM), pageTitle);
                
                Node jcrNode = prodPage.hasContent() ? prodPage.getContentResource().adaptTo(Node.class) :  null;
                if (null == jcrNode) {
                    return;
                }

                //set Page properties.
                setPageProps(jcrNode, data, resourceResolver);
                Node rootNode = jcrNode.hasNode("root") ? jcrNode.getNode("root"): jcrNode.addNode("root");
                
                Node containerNode = rootNode.hasNode(CONTAINER) ? rootNode.getNode(CONTAINER): rootNode.addNode(CONTAINER);
                
                // Creation of Register for Event Core Button component
                Node eventRegistrationContainer = containerNode.hasNode(EVENTREGISTRATIONCONTAINER)
                ? containerNode.getNode(EVENTREGISTRATIONCONTAINER)
                : containerNode.addNode(EVENTREGISTRATIONCONTAINER);
                createRegisterForEventCoreButton(eventRegistrationContainer, data);
                
                //TODO top right container image.
                
                // Creation of event details component
                Node toprightContainer = containerNode.hasNode(TOPRIGHTCONTAINER)
                        ? containerNode.getNode(TOPRIGHTCONTAINER)
                        : containerNode.addNode(TOPRIGHTCONTAINER);

                createEventDetailsComponent(toprightContainer, data);

                // Creation of event description component
                Node centerContainer = containerNode.hasNode(CENTERCONTAINER)
                        ? containerNode.getNode(CENTERCONTAINER)
                        : containerNode.addNode(CENTERCONTAINER);

                createEventDescription(centerContainer, data);
                
               //TODO top bottom left container.
               //TODO top bottom right image.
                session.save();
                session.refresh(true);
            }
        } catch(Exception exec) {
            logger.error("Exception::{}", exec.getMessage());
        }
    }   
    
    /**
     * Sets the page props.
     *
     * @param jcrNode the jcr node
     * @param data the data
     * @param resourceResolver the resource resolver
     */
    private void setPageProps(final Node jcrNode, final EventPageData data, ResourceResolver resourceResolver) {
        try {
            if(StringUtils.isNotBlank(data.getFieldRetirementDateValue())) {
                Calendar retirementDate = WokdayUtils.convertStrToAemCalInstance(data.getFieldRetirementDateValue(),GlobalConstants.EventsPageConstants.YYYY_MM_DD_FORMAT);
                retirementDate.add(Calendar.DATE, 1); // add one day
                jcrNode.setProperty(RETIREMENT_DATE, retirementDate);
            }
            if(StringUtils.isNotBlank(data.getReadcount())) {
                jcrNode.setProperty(READ_COUNT, Long.parseLong(data.getReadcount()));               
            }
            if(StringUtils.isNotBlank(data.getChanged())) {
                String dateStr = WokdayUtils.getDateStringFromEpoch(Long.parseLong(data.getChanged()));
                Calendar updatedDate = WokdayUtils.convertStrToAemCalInstance(dateStr,GlobalConstants.EventsPageConstants.MMM_DD_COMMA_YYYY_FORMAT);
                updatedDate.add(Calendar.DATE, 1); //add one day
                jcrNode.setProperty(UPDATED_DATE, updatedDate);
            }
            if(StringUtils.isNotBlank(data.getStartDate())) {
                Calendar startDateCal = WokdayUtils.convertStrToAemCalInstance(data.getStartDate(),GlobalConstants.AEM_CAL_INSTANCE_FORMAT);
                startDateCal.add(Calendar.HOUR_OF_DAY, 1); // add one hour
                jcrNode.setProperty(START_DATE, startDateCal);
            }
            if(StringUtils.isNotBlank(data.getEndDate())) {
                Calendar endDateCal = WokdayUtils.convertStrToAemCalInstance(data.getEndDate(), GlobalConstants.AEM_CAL_INSTANCE_FORMAT);
                endDateCal.add(Calendar.HOUR_OF_DAY, 1);      // adds one hour
                jcrNode.setProperty(END_DATE, endDateCal);
            }
            
            if(StringUtils.isNotBlank(data.getCalendarEventType())) {
                jcrNode.setProperty(EVENT_TYPE, data.getCalendarEventType());
            }
            
            ArrayList<String> allPageTags = collectAllTagsForGivenPage(resourceResolver, data);
            if(!allPageTags.isEmpty()) {
                jcrNode.setProperty(EVENT_TAGS, allPageTags.stream().toArray(String[]::new));
            }
        } catch (Exception exec) {
            logger.error("Exception occured in setPageProps::{}", exec.getMessage());
        } 
    }
    
    /**
     * Collect all tags for given page.
     *
     * @param resourceResolver the resource resolver
     * @param data the data
     * @return the array list
     */
    private ArrayList<String> collectAllTagsForGivenPage(ResourceResolver resourceResolver, final EventPageData data) {
        ArrayList<String> allPageTags = new ArrayList<String>();

        // Add Calendar eventtype tags.
        if (StringUtils.isNotBlank(data.getCalendarEventType())) {
            List<String> eventTypeTags = Optional.ofNullable(getTagsForGivenInputs(resourceResolver, TagFinderEnum.CALENDAREVENTTYPE, data.getCalendarEventType())).orElse(new ArrayList<>());
            Optional.ofNullable(eventTypeTags).ifPresent(allPageTags::addAll);
        }

        // To add release tags
        if (StringUtils.isNotBlank(data.getReleaseTag())) {
            List<String> releaseTags = Optional.ofNullable(getTagsForGivenInputs(resourceResolver, TagFinderEnum.RELEASE_TAG, data.getReleaseTag())).orElse(new ArrayList<>());
            Optional.ofNullable(releaseTags).ifPresent(allPageTags::addAll);
        }

        // To add product tags
        if (StringUtils.isNotBlank(data.getProduct())) {
            List<String> productTags = Optional.ofNullable(getTagsForGivenInputs(resourceResolver, TagFinderEnum.PRODUCT, data.getProduct())).orElse(new ArrayList<>());
            Optional.ofNullable(productTags).ifPresent(allPageTags::addAll);
        }

        // To add using workday tags
        if (StringUtils.isNotBlank(data.getUsingWorday())) {
            List<String> usingWorkdayTags = Optional.ofNullable(getTagsForGivenInputs(resourceResolver, TagFinderEnum.USING_WORDAY, data.getUsingWorday())).orElse(new ArrayList<>());
            Optional.ofNullable(usingWorkdayTags).ifPresent(allPageTags::addAll);
        }
        return allPageTags;
    }

    /**
     * Gets the tags for given inputs.
     *
     * @param resourceResolver the resource resolver
     * @param tagFinderEnum the tag finder enum
     * @param tagTypeValue the tag type value
     * @return the tags for given inputs
     */
    private List<String> getTagsForGivenInputs(ResourceResolver resourceResolver, TagFinderEnum tagFinderEnum, final String tagTypeValue) {
        return Optional.ofNullable(tagFinderUtil(resourceResolver, tagFinderEnum.getValue(), tagTypeValue)).orElse(new ArrayList<>());
    }
    
    /**
     * Collect page tags.
     *
     * @param resourceResolver the resource resolver
     * @param tagRootPath the tag root path
     * @param tagTitle the tag title
     * @return the list
     */
    private List<String> tagFinderUtil(ResourceResolver resourceResolver, final String tagRootPath, final String tagTitle ) {
        Iterator<Resource> tagResources = doQueryForTag(resourceResolver, tagRootPath, tagTitle);
        List<String> tagsList = new ArrayList<>();
        if (null != tagResources) {
            while (tagResources.hasNext()) {
                Resource artcileResource = tagResources.next();
                if (null != artcileResource) {
                    Tag tag = artcileResource.adaptTo(Tag.class);
                    tagsList.add(tag.getTagID());
                }
            }
        }
        return tagsList;
    }
    
    /**
     * SELECT * FROM [cq:Tag] AS tag
     * WHERE ISDESCENDANTNODE(tag, "/content/cq:tags/event") AND [sling:resourceType] = 'cq/tagging/components/tag' AND [jcr:title] = 'Rising' OR [jcr:title] = 'Webinar'.
     *
     * @param resourceResolver the resource resolver
     * @param searchPath the search path
     * @param tagTitle the tag title
     * @return the iterator
     */
    private Iterator<Resource> doQueryForTag(ResourceResolver resourceResolver,  String searchPath, String tagTitle) {
        String partialSqlStmt = "SELECT * FROM [cq:Tag] AS tag WHERE ISDESCENDANTNODE(tag, \""+searchPath+"\") AND [sling:resourceType] = 'cq/tagging/components/tag' AND ";
        String[] diffTagsList = tagTitle.split(",");
        StringBuilder sbr = new StringBuilder();
        for(int index=0; index<diffTagsList.length; index++) {
            if(index == 0 && StringUtils.isNotBlank(diffTagsList[index])) {
                sbr.append("[jcr:title] = '"+diffTagsList[index].trim()+"'");
            } else if(StringUtils.isNotBlank(diffTagsList[index])) {
                sbr.append(" OR [jcr:title] = '"+diffTagsList[index].trim()+"'");
            }
        }
        
        String sqlStmt = String.format("%s%s", partialSqlStmt, sbr.toString());
        logger.info("Query sql_stmt: {}",  sqlStmt);
        return resourceResolver.findResources(sqlStmt, JCR_SQL2);
    }
    
    /**
     * Find comp type.
     *
     * @param innerContainer the inner container
     * @param parseString the parse string
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

        final String TEXT_DESC = "text_desc";

        final String TITLE_DESC = "title_desc";

        final String AGENDA = "Agenda";

        final String PRE_READING = "Pre Reading";

        final String REGISTRATION_INFORMATION = "Registration Information";

        
        if(parseString.startsWith(eventDescTitle)) {
            createCoreTitleComponent(innerContainer, EVENT_DESCRIPTION,TITLE_DESC);
            createCoreTextComponent(innerContainer, parseString.replace(eventDescTitle, StringUtils.EMPTY), TEXT_DESC);
        } else if(parseString.startsWith(eventRegistrationTitle)) {
            createCoreTitleComponent(innerContainer, REGISTRATION_INFORMATION,TITLE_REG );
            createCoreTextComponent(innerContainer, parseString.replace(eventRegistrationTitle, StringUtils.EMPTY), TEXT_REG);
        } else if(parseString.startsWith(eventPreReadTitle)) {
            createCoreTitleComponent(innerContainer, PRE_READING, TITLE_PREREAD);
            createCoreTextComponent(innerContainer, parseString.replace(eventPreReadTitle, StringUtils.EMPTY), TEXT_PREREAD);
        } else if(parseString.startsWith(eventAgendaTitle)) {
            createCoreTitleComponent(innerContainer, AGENDA, TITLE_AGENDA);
            createCoreTextComponent(innerContainer,parseString.replace(eventAgendaTitle, StringUtils.EMPTY),TEXT_AGENDA);
        }
    }
    
    /**
     * Creates the core text component.
     *
     * @param innerContainer the inner container
     * @param richText the rich text
     * @param nodeName the node name
     */
    private void createCoreTextComponent(Node innerContainer, final String richText, final String nodeName){
        try {
            Node textCompNode = innerContainer.addNode(nodeName);
            textCompNode.setProperty(GlobalConstants.AEM_SLING_RESOURCE_TYPE_PROP, GlobalConstants.TEXT_COMP_SLING_RESOURCE);
            textCompNode.setProperty(TEXT, richText);
            textCompNode.setProperty(TEXT_IS_RICH_PROP, TRUE);
        } catch (Exception exec) {
            logger.error("Exception in createCoreTextComponent method::{}", exec.getMessage());
        } 
    }
    
    /**
     * Creates the core title component.
     *
     * @param innerContainer the inner container
     * @param plainText the plain text
     * @param nodeName the node name
     */
    private void createCoreTitleComponent(Node innerContainer, String plainText, final String nodeName){
        try {
            Node titleCompNode = innerContainer.addNode(nodeName);
            titleCompNode.setProperty(GlobalConstants.AEM_SLING_RESOURCE_TYPE_PROP, GlobalConstants.TITLE_COMP_SLING_RESOURCE);
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
     * @param data the data
     */
    private void createEventDescription(Node innerContainer, EventPageData data) {
        final String descText = data.getDescription();
        if(StringUtils.isNotBlank(descText)) {
            List<Integer> indicesList = findAllIndicesOfGivenString(descText,"<h2 id=");
            if(!indicesList.isEmpty()) {
                for( int index = 0; index < indicesList.size(); index++) {
                    if(index == indicesList.size() - 1) {
                        findCompType(innerContainer, descText.substring(indicesList.get(index)));
                    } else {
                        findCompType(innerContainer, descText.substring(indicesList.get(index), indicesList.get(index+1)));
                    }
                }
            } else {
                /**
                 * It executes, if not find event desc or registration, agenda or pre reading
                 * id's on xml description node data
                 */
                createCoreTitleComponent(innerContainer, EVENT_DESCRIPTION,TITLE_DESC);
                createCoreTextComponent(innerContainer, descText, TEXT_DESC);
            }
            
        }
    }

    /**
     * Find all indices of given string.
     *
     * @param sourceTextString the source text string
     * @param searchWord the search word
     * @return the list
     */
    private  List<Integer> findAllIndicesOfGivenString(String sourceTextString, String searchWord) {
        List<Integer> indexes = new ArrayList<Integer>();
        int wordLength = 0;
        int index = 0;
        while(index != -1){
            index = sourceTextString.indexOf(searchWord, index + wordLength);  // Slight improvement
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
                if (innerContainer.hasNode(GlobalConstants.BUTTON_COMP_NODE_NAME)) {
                    registerForEventButtonNode = innerContainer.getNode(GlobalConstants.BUTTON_COMP_NODE_NAME);
                } else {
                    registerForEventButtonNode = innerContainer.addNode(GlobalConstants.BUTTON_COMP_NODE_NAME);
                    registerForEventButtonNode.setProperty(GlobalConstants.AEM_SLING_RESOURCE_TYPE_PROP,
                            GlobalConstants.BUTTON_COMP_SLING_RESOURCE);
                }
                registerForEventButtonNode.setProperty(GlobalConstants.JCR_TITLE_PROP,
                        GlobalConstants.EventsPageConstants.TEXT_REGISTER_FOR_EVENT);
                registerForEventButtonNode.setProperty(GlobalConstants.LINK_TARGET_PROP,
                        GlobalConstants.TEXT_UNDERSCORE_SELF);
                registerForEventButtonNode.setProperty(GlobalConstants.LINK_URL_PROP, data.getRegistrationUrl());
                // TODO Find if default accessibilityLabel needed

            } else {
                logger.info("Event Registration URL not found for nid : {}", data.getNid());
                if (innerContainer.hasNode(GlobalConstants.BUTTON_COMP_NODE_NAME))
                    innerContainer.getNode(GlobalConstants.BUTTON_COMP_NODE_NAME).remove();
            }

        } catch (Exception exec) {
            logger.error("Exception in createRegisterForEventCoreButton method::{}", exec.getMessage());
        }
    }
    
    /**
     * Creates the event details component.
     *
     * @param innerContainer the inner container
     * @param data the data
     */
    private void createEventDetailsComponent(Node innerContainer, EventPageData data) {
        try {
            Node eventDetailsNode = innerContainer.hasNode(GlobalConstants.EventsPageConstants.EVENT_DETAILS_COMP_NODE_NAME)
                    ? innerContainer.getNode(GlobalConstants.EventsPageConstants.EVENT_DETAILS_COMP_NODE_NAME)
                    : innerContainer.addNode(GlobalConstants.EventsPageConstants.EVENT_DETAILS_COMP_NODE_NAME);

            eventDetailsNode.setProperty(GlobalConstants.AEM_SLING_RESOURCE_TYPE_PROP, GlobalConstants.EventsPageConstants.EVENT_DETAILS_SLING_RESOURCE);
            eventDetailsNode.setProperty(EVENT_TYPE, data.getCalendarEventType());
            
            eventDetailsNode.setProperty(LOCATION, DEFAULTEVENTLOCATION);
            eventDetailsNode.setProperty(HOST, DEFAULTEVENTHOST);
        }  catch (Exception exec) {
            logger.error("Exception in createEventDetailsComponent method::{}", exec.getMessage());
        }
    }
}

