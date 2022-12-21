package com.workday.community.aem.migration.services.impl;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import javax.jcr.Node;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;

import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.propertytypes.ServiceDescription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.day.cq.dam.api.Asset;
import com.day.cq.dam.api.Rendition;
import com.workday.community.aem.migration.constants.MigrationConstants;
import com.workday.community.aem.migration.models.EventPagesList;
import com.workday.community.aem.migration.models.PageNameBean;
import com.workday.community.aem.migration.services.PageCreationService;
import com.workday.community.aem.migration.services.PageNameFinderService;
import com.workday.community.aem.migration.services.ParseXMLDataService;

/**
 * The Class ParseXMLDataServiceImpl.
 * 
 * @author pepalla
 */
@Component(immediate = true, service = ParseXMLDataService.class)
@ServiceDescription("Workday - XML Parser Provider")
public class ParseXMLDataServiceImpl implements ParseXMLDataService {

	/** The Constant log. */
	private static final Logger log = LoggerFactory.getLogger(ParseXMLDataServiceImpl.class);

	/** JAXB jaxb instance . */
	private JAXBContext jaxbContext;

	/** JAXB unmarshaller. */
	private Unmarshaller unmarshaller;

	/**
	 * https://experienceleaguecommunities.adobe.com/t5/adobe-experience-manager/how
	 * -do-i-specify-an-implementation-using-osgiservice/m-p/285357
	 */
	// @OSGiService(filter="(component.name=events-page)")
	@Reference(target = "(type=events-page)")
	PageCreationService eventsPageCreationService;

	@Reference
	PageNameFinderService pageNameFinderService;

	/**
	 * Read XML from jcr source.
	 *
	 * @param <T>       the generic type
	 * @param resolver  the resolver
	 * @param paramsMap the params map
	 * @param clazz     the clazz
	 * @return the event pages list
	 */
	public <T> T readXMLFromJcrSource(final ResourceResolver resolver, Map<String, String> paramsMap, Class<T> clazz) {
		String xmlResponse = readInputStreamFromAsset(resolver, paramsMap);
		if (StringUtils.isNotBlank(xmlResponse)) {
			xmlResponse = xmlResponse.substring(xmlResponse.indexOf("\n") + 1);
			try {
				jaxbContext = JAXBContext.newInstance(clazz);
				unmarshaller = jaxbContext.createUnmarshaller();
				return clazz.cast(unmarshaller.unmarshal(new StringReader(xmlResponse)));
			} catch (Exception e) {
				log.error("Exception occurred at readXML method :{}", e.getMessage());
			}
		}
		return null;
	}

	private String readInputStreamFromAsset(final ResourceResolver resolver, final Map<String, String> paramsMap) {
		StringBuilder builder = new StringBuilder();
		try {
			Resource assetResource = resolver.getResource(paramsMap.get(MigrationConstants.SOURC_FILE_PARAM));
			Asset asset = assetResource.adaptTo(Asset.class);
			Rendition rnd = asset.getOriginal();
			log.debug("rend path::{}", rnd.getPath());
			Node node = resolver
					.getResource(rnd.getPath() + String.format("%s%s", "/", MigrationConstants.JCR_CONTENT_NODE))
					.adaptTo(Node.class);
			InputStream inputStreamReader = node.getProperty(MigrationConstants.JCR_DATA_NODE).getBinary().getStream();
			/**  String result = IOUtils.toString(inputStreamReader, StandardCharsets.UTF_8); */
			BufferedReader bufferedReader = new BufferedReader(
					new InputStreamReader(inputStreamReader, StandardCharsets.UTF_8));
			int eof;
			while ((eof = bufferedReader.read()) != -1) {
				builder.append((char) eof);
			}
			bufferedReader.close();
			inputStreamReader.close();
		} catch (Exception e) {
			log.error("Exception occurred at readInputStreamFromAsset method :{}", e.getMessage());
		}
		return builder.toString();
	}

	/**
	 * Read xml from jcr and delegate to page creation service.
	 *
	 * @param resolver     the resolver
	 * @param paramsMap    the params map
	 * @param templatePath the template path
	 * @return the string
	 */
	@Override
	public void readXmlFromJcrAndDelegateToPageCreationService(ResourceResolver resolver, Map<String, String> paramsMap,
			final String templatePath) {
		try {
			String templateName = findTemplateName(templatePath);
			if (StringUtils.isNotBlank(templateName)) {
				switch (templateName) {
					case "event-page-template":
						EventPagesList listOfPageData = readXMLFromJcrSource(resolver, paramsMap, EventPagesList.class);
						if (null != listOfPageData) {
							doCreateEventsPageCreationService(resolver, paramsMap, listOfPageData);
						}
						break;
					case "kits-page-template":
						break;
					default:
				}
			}
		} catch (Exception exec) {
			log.error("Exception occurred at readXmlFromJcrAndDelegateToPageCreationService method :{}",
					exec.getMessage());
		}
	}

	/**
	 * Find template name.
	 *
	 * @param templatePath the template path
	 * @return the string
	 */
	private String findTemplateName(final String templatePath) {
		String[] arr = templatePath.split("\\/");
		String templateName = StringUtils.EMPTY;
		if (null != arr && arr.length > 0)
			templateName = arr[arr.length - 1];
		return templateName;
	}

	/**
	 * Do create events page creation service.
	 *
	 * @param paramsMap      the params map
	 * @param listOfPageData the list of page data
	 * @return the string
	 */
	private void doCreateEventsPageCreationService(ResourceResolver resolver, Map<String, String> paramsMap,
			EventPagesList listOfPageData) {
		List<PageNameBean> list = pageNameFinderService.getPageName(resolver,
				MigrationConstants.EVENT_PAGE_NAMES_FINDER_JSON);
		listOfPageData.getRoot().forEach(item -> eventsPageCreationService.doCreatePage(paramsMap, item, list));
	}
}
