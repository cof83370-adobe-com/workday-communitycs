package com.workday.community.aem.core.servlets;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.Servlet;
import javax.servlet.ServletException;

import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.servlets.SlingSafeMethodsServlet;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.workday.community.aem.core.constants.GlobalConstants;

import com.workday.community.aem.core.services.ParseXMLDataService;

/**
 * The Class XMLDataImporterServlet.
 * 
 * @author pepalla
 */
@Component(service = { Servlet.class }, property = { "sling.servlet.paths=" + XMLDataImporterServlet.RESOURCE_PATH_1, "sling.servlet.methods=GET" })
public class XMLDataImporterServlet extends SlingSafeMethodsServlet {

	/** The Constant log. */
	private static final Logger log = LoggerFactory.getLogger(XMLDataImporterServlet.class);

	/** The Constant RESOURCE_PATH. */
	public static final String RESOURCE_PATH_1 = "/bin/dataimporter";

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1L;

	/** The Constant DEFAULT_CHARSET. */
	private static final String DEFAULT_CHARSET = "UTF-8";

	/** The parse XML data service. */
	@Reference
	ParseXMLDataService parseXMLDataService;
	
	

	/**
	 * Do get.
	 *
	 * @param req the req
	 * @param resp the resp
	 * @throws ServletException the servlet exception
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	@Override
	protected void doGet(final SlingHttpServletRequest req, final SlingHttpServletResponse resp)
			throws ServletException, IOException {
		
		resp.setContentType("text/html");
		resp.setCharacterEncoding(DEFAULT_CHARSET);
		try(ResourceResolver resolver = req.getResourceResolver()) {
			final String sourceFile = req.getParameter(GlobalConstants.SOURC_FILE_PARAM);
			final String template = req.getParameter(GlobalConstants.TEMPLATE_PARAM);
			final String pagePath = req.getParameter(GlobalConstants.PARENT_PAGE_PATH_PARAM);
			if (StringUtils.isNotBlank(sourceFile) && StringUtils.isNotBlank(template)
					&& StringUtils.isNotBlank(pagePath)) {
				Map<String, String> paramsMap = new HashMap<String, String>();
				paramsMap.put(GlobalConstants.SOURC_FILE_PARAM, sourceFile);
				paramsMap.put(GlobalConstants.TEMPLATE_PARAM, template);
				paramsMap.put(GlobalConstants.PARENT_PAGE_PATH_PARAM, pagePath);
				if (null != parseXMLDataService) {
					 parseXMLDataService.readXmlFromJcrAndDelegateToPageCreationService(resolver, paramsMap,template);
				}
			}
		} catch (Exception e) {
			resp.sendError(400, e.getMessage());
			log.error("Exception occured at doGet method of XMLDataImporterServlet" + e.getMessage());
		}
	}
}
