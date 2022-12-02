package com.workday.community.aem.core.services.impl;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

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
public class EventsPageCreationServiceImpl implements PageCreationService {

	/** The logger. */
	private final Logger LOGGER = LoggerFactory.getLogger(this.getClass().getName());
	
	private static final String default_event_location ="Virtual";
	
	private static final String CONTAINER = "container";

	private static final String CENTERCONTAINER2 = "centercontainer";

	private static final String TOPRIGHTCONTAINER2 = "toprightcontainer";

	private static final String HOST = "host";

	private static final String LOCATION = "location";

	private static final String REGISTER_LINK = "registerLink";

	private static final String NEW_WINDOW = "newWindow";

	private static final String FALSE = "false";

	private static final String TRUE = "true";

	private static final String SHOW_REGISTER_LINK = "showRegisterLink";

	private static final String JCR_TITLE = "jcr:title";

	private static final String TYPE = "type";

	private static final String TEXT_IS_RICH_PROP = "textIsRich";

	private static final String TEXT = "text";

	private static final String JCR_SQL2 = "JCR-SQL2";

	private static final String EVENT_TAGS = "eventTags";

	private static final String RETIREMENT_DATE = "retirementDate";

	private static final String READ_COUNT = "readCount";

	private static final String UPDATED_DATE = "updatedDate";

	private static final String EVENT_TYPE = "eventType";

	private static final String START_DATE = "startDate";

	private static final String END_DATE = "endDate";

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
			LOGGER.debug("resourceResolver::{}", resourceResolver);
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
					LOGGER.error("page name not provided in source file");
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
				
				// Creation of breadcrumb component
				createBreadcrumbComp(containerNode);
				
				// Creation of title component
				createTitleComp(containerNode, data);
				
				// Creation of event registration component
				createEventRegistrationComponent(containerNode, data);
				
				// Creation of event metadata component
				createEventMetaDataComponent(containerNode, data);
				
			    //TODO top right container image.
				
				// Creation of event details component
				Node toprightContainer = containerNode.hasNode(TOPRIGHTCONTAINER2)
						? containerNode.getNode(TOPRIGHTCONTAINER2)
						: containerNode.addNode(TOPRIGHTCONTAINER2);

				createEventDetailsComponent(toprightContainer, data);

				// Creation of event description component
				Node centerContainer = containerNode.hasNode(CENTERCONTAINER2)
						? containerNode.getNode(CENTERCONTAINER2)
						: containerNode.addNode(CENTERCONTAINER2);

				createEventDescription(centerContainer, data);
				
		       //TODO top bottom left container.
			   //TODO top bottom right image.
				session.save();
				session.refresh(true);
			}
		} catch(Exception exec) {
			LOGGER.error("Exception::{}", exec.getMessage());
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
			if(StringUtils.isNotBlank(data.getRetirement_date())) {
				Calendar retirementDate = WokdayUtils.convertStrToAemCalInstance(data.getRetirement_date(),GlobalConstants.EventsPageConstants.YYYY_MM_DD_FORMAT);
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
			if(StringUtils.isNotBlank(data.getStart_date())) {
				Calendar startDateCal = WokdayUtils.convertStrToAemCalInstance(data.getStart_date(),GlobalConstants.AEM_CAL_INSTANCE_FORMAT);
				startDateCal.add(Calendar.HOUR_OF_DAY, 1); // add one hour
				jcrNode.setProperty(START_DATE, startDateCal);
			}
			if(StringUtils.isNotBlank(data.getEnd_date())) {
				Calendar endDateCal = WokdayUtils.convertStrToAemCalInstance(data.getEnd_date(), GlobalConstants.AEM_CAL_INSTANCE_FORMAT);
				endDateCal.add(Calendar.HOUR_OF_DAY, 1);      // adds one hour
				jcrNode.setProperty(END_DATE, endDateCal);
			}
			
			if(StringUtils.isNotBlank(data.getCalendareventtype())) {
				jcrNode.setProperty(EVENT_TYPE, data.getCalendareventtype());
			}
			
			ArrayList<String> allPageTags = collectAllTagsForGivenPage(resourceResolver, data);
			if(allPageTags.size() > 0) {
				jcrNode.setProperty(EVENT_TAGS, allPageTags.stream().toArray(String[]::new));
			}
		} catch (Exception exec) {
			LOGGER.error("Exception occured in setPageProps::{}", exec.getMessage());
		} 
	}
	
	private ArrayList<String> collectAllTagsForGivenPage(ResourceResolver resourceResolver, final EventPageData data) {
		ArrayList<String> allPageTags = new ArrayList<String>();

		// Add Calendar eventtype tags.
		if (StringUtils.isNotBlank(data.getCalendareventtype())) {
			List<String> eventTypeTags = getTagsForGivenInputs(resourceResolver, TagFinderEnum.Calendareventtype, data.getCalendareventtype());
			if (null != eventTypeTags) {
				allPageTags.addAll(eventTypeTags);
			}
		}

		// TODO To add release tags
		if (StringUtils.isNotBlank(data.getRelease_tag())) {
			List<String> releaseTags = getTagsForGivenInputs(resourceResolver, TagFinderEnum.release_tag, data.getRelease_tag());
			if (null != releaseTags) {
				allPageTags.addAll(releaseTags);
			}
		}

		// TODO To add product tags
		if (StringUtils.isNotBlank(data.getProduct())) {
			List<String> productTags = getTagsForGivenInputs(resourceResolver, TagFinderEnum.Product, data.getProduct());
			if (null != productTags) {
				allPageTags.addAll(productTags);
			}
		}

		// TODO To add using workday tags
		if (StringUtils.isNotBlank(data.getUsing_worday())) {
			List<String> usingWorkdayTags = getTagsForGivenInputs(resourceResolver, TagFinderEnum.using_worday, data.getUsing_worday());
			if (null != usingWorkdayTags) {
				allPageTags.addAll(usingWorkdayTags);
			}
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
		List<String> tags= tagFinderUtil(resourceResolver, tagFinderEnum.getValue(), tagTypeValue);
		if(null != tags && tags.size() > 0) {
			return tags;
		}
		return null;
	}
	
	/**
	 * Creates the breadcrumb comp.
	 *
	 * @param containerNode the container node
	 */
	private void createBreadcrumbComp(final Node containerNode) {
		try {
			if(!containerNode.hasNode(GlobalConstants.BREADCRUMB_COMP_NODE_NAME)) {
				Node breadcrumbNode = containerNode.addNode(GlobalConstants.BREADCRUMB_COMP_NODE_NAME);
				breadcrumbNode.setProperty(GlobalConstants.AEM_SLING_RESOURCE_TYPE_PROP,GlobalConstants.BREADCRUMB_COMP_SLING_RESOURCE);
			}
		} catch (Exception exec) {
			LOGGER.error("Exception occured in createBreadcrumbComp::{}", exec.getMessage());
		}
	}
	
	/**
	 * Creates the title comp.
	 *
	 * @param containerNode the container node
	 * @param data the data
	 */
	private void createTitleComp(final Node containerNode, final EventPageData data) {
		try {
			if(containerNode.hasNode(GlobalConstants.TITLE_COMP_NODE_NAME)) {
				Node titleNode = containerNode.getNode(GlobalConstants.TITLE_COMP_NODE_NAME);
				titleNode.setProperty(GlobalConstants.JCR_TITLE_PROP, data.getTitle());
			} else {
				Node titleNode = containerNode.addNode(GlobalConstants.TITLE_COMP_NODE_NAME);
				titleNode.setProperty(GlobalConstants.AEM_SLING_RESOURCE_TYPE_PROP, GlobalConstants.TITLE_COMP_SLING_RESOURCE);
				titleNode.setProperty(GlobalConstants.JCR_TITLE_PROP,  data.getTitle());
			}
		} catch (Exception exec) {
			LOGGER.error("Exception::{}", exec.getMessage());
		}
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
		String partial_sql_stmt = "SELECT * FROM [cq:Tag] AS tag WHERE ISDESCENDANTNODE(tag, \""+searchPath+"\") AND [sling:resourceType] = 'cq/tagging/components/tag' AND ";
		String[] diffTagsList = tagTitle.split(",");
		StringBuilder sbr = new StringBuilder();
		for(int index=0; index<diffTagsList.length; index++) {
			if(index == 0 && StringUtils.isNotBlank(diffTagsList[index])) {
				sbr.append("[jcr:title] = '"+diffTagsList[index].trim()+"'");
			} else if(StringUtils.isNotBlank(diffTagsList[index])) {
				sbr.append(" OR [jcr:title] = '"+diffTagsList[index].trim()+"'");
			}
		}
		
		String sql_stmt = String.format("%s%s", partial_sql_stmt, sbr.toString());
		LOGGER.info("Query sql_stmt: {}",  sql_stmt);
		return resourceResolver.findResources(sql_stmt, JCR_SQL2);
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
		if(parseString.startsWith(eventDescTitle)) {
			createCoreTitleComponent(innerContainer, "Event Description","title_desc");
			createCoreTextComponent(innerContainer, parseString.replaceAll(eventDescTitle, StringUtils.EMPTY), "text_desc");
		} else if(parseString.startsWith(eventRegistrationTitle)) {
			createCoreTitleComponent(innerContainer, "Registration Information","title_reg" );
			createCoreTextComponent(innerContainer, parseString.replaceAll(eventRegistrationTitle, StringUtils.EMPTY), "text_reg");
		} else if(parseString.startsWith(eventPreReadTitle)) {
			createCoreTitleComponent(innerContainer, "Pre Reading", "title_preread");
			createCoreTextComponent(innerContainer, parseString.replaceAll(eventPreReadTitle, StringUtils.EMPTY), "text_preread");
		} else if(parseString.startsWith(eventAgendaTitle)) {
			createCoreTitleComponent(innerContainer, "Agenda", "title_agenda");
			createCoreTextComponent(innerContainer,parseString.replaceAll(eventAgendaTitle, StringUtils.EMPTY),"text_agenda");
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
			LOGGER.error("Exception in createCoreTextComponent method::{}", exec.getMessage());
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
			LOGGER.error("Exception in createCoreTitleComponent method::{}", exec.getMessage());
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
			if(indicesList.size() > 0) {
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
				createCoreTitleComponent(innerContainer, "Event Description","title_desc");
				createCoreTextComponent(innerContainer, descText, "text_desc");
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
	 * Creates the event meta data component.
	 *
	 * @param innerContainer the inner container
	 * @param data the data
	 */
	private void createEventMetaDataComponent(Node innerContainer, EventPageData data) {
		try {
			if(!innerContainer.hasNode(GlobalConstants.EventsPageConstants.EVENT_META_DATA_COMP_NODE_NAME)) {
				Node eventMetaDataNode = innerContainer.addNode(GlobalConstants.EventsPageConstants.EVENT_META_DATA_COMP_NODE_NAME);
				eventMetaDataNode.setProperty(GlobalConstants.AEM_SLING_RESOURCE_TYPE_PROP, GlobalConstants.EventsPageConstants.EVENT_MATA_DATA_SLING_RESOURCE);
			} 
		}  catch (Exception exec) {
			LOGGER.error("Exception in createEventMetaDataComponent method::{}", exec.getMessage());
		}
	}
	
	/**
	 * Creates the event registration component.
	 *
	 * @param innerContainer the inner container
	 * @param data the data
	 */
	private void createEventRegistrationComponent(Node innerContainer, EventPageData data) {
		try {
			Node eventRegistrationNode;
			if(innerContainer.hasNode(GlobalConstants.EventsPageConstants.EVENT_REGISTRATION_COMP_NODE_NAME)) {
				eventRegistrationNode = innerContainer.getNode(GlobalConstants.EventsPageConstants.EVENT_REGISTRATION_COMP_NODE_NAME);
			} else {
				eventRegistrationNode = innerContainer.addNode(GlobalConstants.EventsPageConstants.EVENT_REGISTRATION_COMP_NODE_NAME);
				eventRegistrationNode.setProperty(GlobalConstants.AEM_SLING_RESOURCE_TYPE_PROP, GlobalConstants.EventsPageConstants.EVENT_REGISTRATION_SLING_RESOURCE);
			}
			
			if(StringUtils.isNotBlank(data.getShow_ask_related_question()) ) {
				if(data.getShow_ask_related_question().equalsIgnoreCase("1")) {
					eventRegistrationNode.setProperty(SHOW_REGISTER_LINK, TRUE);
				} else {
					eventRegistrationNode.setProperty(SHOW_REGISTER_LINK, FALSE);
				}
			}
			eventRegistrationNode.setProperty(NEW_WINDOW, FALSE);
			eventRegistrationNode.setProperty(REGISTER_LINK, data.getRegistration_url());
		}  catch (Exception exec) {
			LOGGER.error("Exception in createEventRegistrationComponent method::{}", exec.getMessage());
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
			eventDetailsNode.setProperty(EVENT_TYPE, data.getCalendareventtype());
			
			//TODO - Need to set location value from xml
			eventDetailsNode.setProperty(LOCATION, default_event_location);
			//TODO - Need to set host value from xml
			eventDetailsNode.setProperty(HOST, "Workday");
		}  catch (Exception exec) {
			LOGGER.error("Exception in createEventDetailsComponent method::{}", exec.getMessage());
		}
	}
}
