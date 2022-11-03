package com.workday.community.aem.core.services.impl;

import java.util.Calendar;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.jcr.Node;
import javax.jcr.Session;

import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.day.cq.wcm.api.Page;
import com.day.cq.wcm.api.PageManager;
import com.workday.community.aem.core.constants.GlobalConstants;
import com.workday.community.aem.core.models.EventPageData;
import com.workday.community.aem.core.services.PageCreationService;
import com.workday.community.aem.core.utils.WokdayUtils;


/**
 * The Class PageCreationServiceImpl.
 * 
 * @author pepalla
 */
@Component(immediate = true, service = PageCreationService.class, name = PageCreationServiceImpl.NAME,
configurationPid = PageCreationServiceImpl.CONFIG_PID)
public class PageCreationServiceImpl implements PageCreationService {

	/** The logger. */
	private static Logger LOGGER = LoggerFactory.getLogger(PageCreationServiceImpl.class);

	/** The Constant NAME. */
	public static final String NAME = "Workday - Sample Page Creation Service";
	
	/** The Constant CONFIG_PID. */
	public static final String CONFIG_PID = "com.workday.community.aem.core.services.PageCreationServiceImpl";
	
	/** The resolver factory. */
	@Reference
	private ResourceResolverFactory resolverFactory;
	
	
	/** The wg service param. */
	Map<String, Object> wdServiceParam = Collections.singletonMap(ResourceResolverFactory.SUBSERVICE,
			"workday-community-administrative-service");
	
	
	/**
	 * Do create page.
	 *
	 * @param req the req
	 * @param paramsMap the params map
	 * @param data the data
	 */
	@Override
	public void doCreatePage(SlingHttpServletRequest req, final Map<String, String> paramsMap, EventPageData data) {
		try (ResourceResolver resourceResolver = resolverFactory.getServiceResourceResolver(wdServiceParam)) {
			LOGGER.info("resourceResolver::"+resourceResolver);
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

				Node jcrNode = null;
				if (prodPage.hasContent()) {
					jcrNode = prodPage.getContentResource().adaptTo(Node.class);
				} else {
					return;
				}

				Node containerNode = jcrNode.getNode("root/container");
				
				// Creation of title component
				if(containerNode.hasNode(GlobalConstants.TITLE_COMP_NODE_NAME)) {
					Node titleNode = containerNode.getNode(GlobalConstants.TITLE_COMP_NODE_NAME);
					titleNode.setProperty("jcr:title", data.getTitle());
				} else {
					Node titleNode = containerNode.addNode(GlobalConstants.TITLE_COMP_NODE_NAME);
					titleNode.setProperty(GlobalConstants.AEM_SLING_RESOURCE_TYPE_PROP, GlobalConstants.TITLE_COMP_SLING_RESOURCE);
					titleNode.setProperty("jcr:title",  data.getTitle());
				}
				Node innerContainer = null;
				
				if(containerNode.hasNode("container")) 
					innerContainer = containerNode.getNode("container");
				 else 
					innerContainer = containerNode.addNode("container");
				
				// Creation of event validity component
				createEventValidityComp(innerContainer, data);
				
				// Creation of event details component
				// eventNode.setProperty("description", data.getField_description_value());
				List<Document> htmlDocs = parseEventDetailsContent(data);
				prepareEventDetailsNodes(htmlDocs, innerContainer);
				
				// Creation of event date component
				createEventDateComp(innerContainer, data);
				
				//creation of moredetails component
				createMoreDetailsComp(containerNode,data);
			
				session.save();
				session.refresh(true);
			}
		} catch(Exception exec) {
			LOGGER.error("Exception::{}", exec.getMessage());
		}
	}   
	
	/**
	 * Parses the event details content.
	 *
	 * @param data the data
	 * @return the list
	 */
	private List<Document> parseEventDetailsContent(EventPageData data) {
		String splitterString="<h3 class=\"title__h3\">";
		String[] richTextArry = data.getField_description_value().split(splitterString);
		List<Document> htmlDocs = new LinkedList<Document>();
		if(null != richTextArry && richTextArry.length > 0) {
			for(int i = 0; i < richTextArry.length; i++) {
				if(i==0) {
					continue; // avoiding first element
				}
				String eachCompleteCompDetails = splitterString+richTextArry[i];
				Document document = Jsoup.parse(eachCompleteCompDetails);
				htmlDocs.add(document);
			}
		}
		return htmlDocs;
	}
	
	/**
	 * Creates the event date comp.
	 *
	 * @param innerContainer the inner container
	 * @param data the data
	 */
	private void createEventDateComp(Node innerContainer, EventPageData data) {
		try {
			Node eventDateNode = innerContainer.addNode(GlobalConstants.EventsPageConstants.EVENT_DATE_COMP_NODE_NAME);
			eventDateNode.setProperty(GlobalConstants.AEM_SLING_RESOURCE_TYPE_PROP, GlobalConstants.EventsPageConstants.EVENT_DATE_SLING_RESOURCE);
			Calendar startDateCal = WokdayUtils.convertStrToAemCalInstance(data.getStart_date(),GlobalConstants.AEM_CAL_INSTANCE_FORMAT);
			startDateCal.add(Calendar.HOUR_OF_DAY, 1); // add one hour
			eventDateNode.setProperty("startDate", startDateCal);
			Calendar endDateCal = WokdayUtils.convertStrToAemCalInstance(data.getEnd_date(), GlobalConstants.AEM_CAL_INSTANCE_FORMAT);
			endDateCal.add(Calendar.HOUR_OF_DAY, 1);      // adds one hour
			eventDateNode.setProperty("endDate", endDateCal);
			if(StringUtils.isNotBlank(data.getShow_ask_related_question()) ) {
				if(data.getShow_ask_related_question().equalsIgnoreCase("1")) {
					eventDateNode.setProperty("showQuestionLink", "true");
				} else {
					eventDateNode.setProperty("showQuestionLink", "false");
				}
			}
			eventDateNode.setProperty("timeZone", "PDT"); //TO DO
		} catch (Exception exec) {
			LOGGER.error("Exception occured at createEventDateComp method:{}", exec.getMessage());
		}
	}
	
	/**
	 * Creates the more details comp.
	 *
	 * @param containerNode the container node
	 * @param data the data
	 */
	private void createMoreDetailsComp(Node containerNode, EventPageData data) {
		Node rightContainer = null;
		try {
			if(containerNode.hasNode("rightcontainer")) 
				rightContainer = containerNode.getNode("rightcontainer");
			 else 
				 rightContainer = containerNode.addNode("rightcontainer");
			Node moreDetailsNode = rightContainer.addNode(GlobalConstants.EventsPageConstants.MORE_DETAILS_COMP_NODE_NAME);
			moreDetailsNode.setProperty(GlobalConstants.AEM_SLING_RESOURCE_TYPE_PROP, GlobalConstants.EventsPageConstants.MORE_DETAILS_SLING_RESOURCE);
			moreDetailsNode.setProperty("contentType", data.getContenttype());
			moreDetailsNode.setProperty("contentTypeLabel", data.getContentTypeLabel());
			moreDetailsNode.setProperty("calEventType", data.getCalendareventtype());
			moreDetailsNode.setProperty("calEventTypeLabel", data.getCalendarEventTypeLabel());
			moreDetailsNode.setProperty("product", data.getProduct());
			moreDetailsNode.setProperty("productLabel", data.getProductLabel());
		} catch (Exception exec) {
			LOGGER.error("Exception occured while creating moredetails comp node:{}", exec.getMessage());
		} 
	}
	
	/**
	 * Creates the event validity comp.
	 *
	 * @param innerContainer the inner container
	 * @param data the data
	 */
	private void createEventValidityComp(Node innerContainer, EventPageData data) {
		try {
			Node eventValidityNode = innerContainer.addNode(GlobalConstants.EventsPageConstants.EVENT_VALIDITY_COMP_NODE_NAME);
			eventValidityNode.setProperty(GlobalConstants.AEM_SLING_RESOURCE_TYPE_PROP, GlobalConstants.EventsPageConstants.EVENT_VALIDITY_SLING_RESOURCE);
			String dateStr = WokdayUtils.getDateStringFromEpoch(Long.parseLong(data.getChanged()));
			Calendar updatedDate = WokdayUtils.convertStrToAemCalInstance(dateStr,GlobalConstants.EventsPageConstants.MMM_DD_COMMA_YYYY_FORMAT);
			updatedDate.add(Calendar.DATE, 1); //add one day
			eventValidityNode.setProperty("updatedDate", updatedDate);
			Calendar retirementDate = WokdayUtils.convertStrToAemCalInstance(data.getField_retirement_date_value(),GlobalConstants.EventsPageConstants.YYYY_MM_DD_FORMAT);
			retirementDate.add(Calendar.DATE, 1); // add one day
			eventValidityNode.setProperty("retirementDate", retirementDate);
			eventValidityNode.setProperty("readCount", data.getReadcount());
		} catch (Exception exec) {
			LOGGER.error("Exception occured while creating event validity comp node:{}", exec.getMessage());
		} 
	}
	
	/**
	 * Prepare event details nodes.
	 *
	 * @param htmlDocs the html docs
	 * @param innerContainer the inner container
	 */
	private void prepareEventDetailsNodes(List<Document> htmlDocs, Node innerContainer) {
		 try {
			for(int index=0; index<htmlDocs.size(); index++) {
				Document document = htmlDocs.get(index);
				 Node eventDetailsNode = innerContainer.addNode(String.format("%s%s", GlobalConstants.EventsPageConstants.EVENT_DETAILS_COMP_NODE_NAME, index));
				 eventDetailsNode.setProperty(GlobalConstants.AEM_SLING_RESOURCE_TYPE_PROP, GlobalConstants.EventsPageConstants.EVENT_DETAILS_SLING_RESOURCE);
				 List<String> h3List = new LinkedList<String>();
				 Elements h3Elements = document.getElementsByTag("h3");
			      for (Element eachEle : h3Elements) {
			    	  h3List.add(eachEle.text());
			      }
			      eventDetailsNode.setProperty("heading", String.join(",", h3List));
			      sanitizeDocForEasyParsing(document);
			      eachEventDetailParsing(document, eventDetailsNode);
			      List<String> paraGraphList = new LinkedList<String>();
			      Elements paragrapthElements = document.getElementsByTag("p");
			      for (Element eachEle : paragrapthElements) {
			    	  paraGraphList.add(eachEle.text());
			      }
			      if(null != paraGraphList && paraGraphList.size() > 0) {
			    	  eventDetailsNode.setProperty("notes", String.join(",", paraGraphList));
			      }
			 }
		} catch (Exception exec) {
			LOGGER.error("Exception occured at prepareEventDetailsNodes method::{}", exec.getMessage());
		} 
	}
	
	/**
	 * Each event detail parsing.
	 *
	 * @param document the document
	 * @param eventDetailsNode the event details node
	 */
	private void eachEventDetailParsing(Document document, Node eventDetailsNode) {
		   Elements allLevelLiItems = document.select("li.first-level");
		      Node eventAccordion = null;
		      Node listNode=null;
				try {
					for (int ind = 0; ind < allLevelLiItems.size(); ind++) {
						Elements secondLevel = allLevelLiItems.get(ind).select("li.second-level");
						if (!secondLevel.isEmpty() && secondLevel.size() > 0) {
							Node itemNode=null;
							for (int innderInd = 0; innderInd < secondLevel.size(); innderInd++ ) { //
								if(innderInd==0){
									 if(eventDetailsNode.hasNode("eventAccordion")) {
										eventAccordion = eventDetailsNode.getNode("eventAccordion");
									 } else {
										eventAccordion = eventDetailsNode.addNode("eventAccordion");
									 }
									 itemNode = eventAccordion.addNode(String.format("%s%s", "item", ind));
						    		 listNode=itemNode.addNode("list");
						    		 Node innerItemNode=listNode.addNode(String.format("%s%s", "item", innderInd));
						    		 innerItemNode.setProperty("eachInnerBullet", secondLevel.get(innderInd).text());
								} else {
									Node innerItemNode=listNode.addNode(String.format("%s%s", "item", innderInd));
									innerItemNode.setProperty("eachInnerBullet", secondLevel.get(innderInd).text());
								}
								secondLevel.get(innderInd).remove();
							}
							itemNode.setProperty("eachOuterBullet", allLevelLiItems.get(ind).text());
						} else {
							eventAccordion = addOuterBullets(ind, eventAccordion, eventDetailsNode, allLevelLiItems.get(ind));
						}
					}
				} catch (Exception exec) {
					LOGGER.error("Exception occured at each event detail parsing method::{}", exec.getMessage());
				} 
	}
	
	/**
	 * Sanitize doc for easy parsing.
	 *
	 * @param document the document
	 */
	private void sanitizeDocForEasyParsing(Document document) {
		document.select("ul > li").addClass("first-level");
	    document.select("ul > li > ul > li").removeClass("first-level").addClass("second-level");
	}
	
	/**
	 * Adds the outer bullets.
	 *
	 * @param ind the ind
	 * @param eventAccordion the event accordion
	 * @param eventDetailsNode the event details node
	 * @param ele the ele
	 * @return the node
	 * @throws Exception the exception
	 */
	private Node addOuterBullets(int ind, Node eventAccordion, Node eventDetailsNode, Element ele) throws  Exception {
		if(ind == 0) {
 		   eventAccordion = eventDetailsNode.addNode("eventAccordion");
 	  } else {
 		 eventAccordion = eventDetailsNode.getNode("eventAccordion");
 	  }
 	  Node itemNode = eventAccordion.addNode(String.format("%s%s", "item", ind));
 	  itemNode.setProperty("eachOuterBullet", ele.text());
	  return eventAccordion;
	}
}
