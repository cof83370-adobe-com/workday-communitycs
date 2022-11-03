package com.workday.community.aem.core.services.impl;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.util.Map;

import javax.jcr.Node;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;

import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.day.cq.dam.api.Asset;
import com.day.cq.dam.api.Rendition;
import com.workday.community.aem.core.constants.GlobalConstants;
import com.workday.community.aem.core.services.ParseXMLDataService;

/**
 * The Class ParseXMLDataServiceImpl.
 * 
 * @author pepalla
 */
@Component(immediate = true, service = ParseXMLDataService.class, name = ParseXMLDataServiceImpl.NAME, configurationPid = ParseXMLDataServiceImpl.CONFIG_PID)
public class ParseXMLDataServiceImpl implements ParseXMLDataService {

	/** The Constant NAME. */
	public static final String NAME = "Workday - XML Parser Provider";

	/** The Constant CONFIG_PID. */
	public static final String CONFIG_PID = "com.workday.community.aem.core.servlets.ParseXMLDataImpl";

	/** The Constant log. */
	private static final Logger log = LoggerFactory.getLogger(ParseXMLDataServiceImpl.class);

	/** JAXB jaxb instance . */
	private JAXBContext jaxbContext;

	/** JAXB unmarshaller. */
	private Unmarshaller unmarshaller;

	/**
	 * Read XML from jcr source.
	 *
	 * @param <T> the generic type
	 * @param resolver the resolver
	 * @param paramsMap the params map
	 * @param clazz the clazz
	 * @return the event pages list
	 */
	@Override
	public  <T> T  readXMLFromJcrSource(final ResourceResolver resolver, Map<String, String> paramsMap, Class<T> clazz) {
		StringBuilder builder = new StringBuilder();
		try {
			Resource assetResource = resolver.getResource(paramsMap.get(GlobalConstants.SOURC_FILE_PARAM));
			Asset asset = assetResource.adaptTo(Asset.class);
			Rendition rnd = asset.getOriginal();
			log.debug("rend path::{}", rnd.getPath());
			Node node = resolver.getResource(rnd.getPath() + String.format("%s%s", "/", GlobalConstants.JCR_CONTENT_NODE)).adaptTo(Node.class);
			InputStream inputStreamReader = node.getProperty(GlobalConstants.JCR_DATA_NODE).getBinary().getStream();
			// String result = IOUtils.toString(inputStreamReader, StandardCharsets.UTF_8);
			BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStreamReader));
			if (bufferedReader != null) {
				int eof;
				while ((eof = bufferedReader.read()) != -1) {
					builder.append((char) eof);
				}
				bufferedReader.close();
			}
			inputStreamReader.close();

		} catch (Exception e) {
			log.error("Exception occured at readXML method :{}", e.getMessage());
		}

		String xmlResponse = builder.toString();
		xmlResponse = xmlResponse.substring(xmlResponse.indexOf("\n") + 1);

		try {
			jaxbContext = JAXBContext.newInstance(clazz);
			unmarshaller = jaxbContext.createUnmarshaller();
			T obj = clazz.cast(unmarshaller.unmarshal(new StringReader(xmlResponse)));
			return   obj;
		} catch (Exception e) {
			log.error("Exception occured at readXML method :{}", e.getMessage());
		}
		return null;
	}
}
