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
import com.workday.community.aem.core.models.EventPagesList;
import com.workday.community.aem.core.services.PageCreationService;
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

	/** The page creation service. */
	@Reference
	PageCreationService pageCreationService;

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
			String status = "failure";
			final String sourceFile = req.getParameter(GlobalConstants.SOURC_FILE_PARAM);
			final String template = req.getParameter(GlobalConstants.TEMPLATE_PARAM);
			final String pagePath = req.getParameter(GlobalConstants.PARENT_PAGE_PATH_PARAM);
			if (StringUtils.isNotBlank(sourceFile) && StringUtils.isNotBlank(template)
					&& StringUtils.isNotBlank(pagePath)) {
				Map<String, String> paramsMap = new HashMap<String, String>();
				paramsMap.put(GlobalConstants.SOURC_FILE_PARAM, sourceFile);
				paramsMap.put(GlobalConstants.TEMPLATE_PARAM, template);
				paramsMap.put(GlobalConstants.PARENT_PAGE_PATH_PARAM, pagePath);

				if (null != parseXMLDataService && null != getModelBasedOnTemplateName(template)) {
					EventPagesList listOfPageData = parseXMLDataService.readXMLFromJcrSource(resolver, paramsMap,
							getModelBasedOnTemplateName(template));
					if (null != listOfPageData) {
						log.debug("listOfPageData:::{}", listOfPageData);
						listOfPageData.getRoot().forEach((item) -> {
							pageCreationService.doCreatePage(req, paramsMap, item);
						});
						status = "success";
					}
				}
			}
			resp.getWriter().write(status);
		} catch (Exception e) {
			log.error("Exception occured at doGet method of XMLDataImporterServlet" + e.getMessage());
		}
	}
	
	@SuppressWarnings("unchecked")
	private <T> T getModelBasedOnTemplateName(String template) {
		// template path null check already happening above.
		String[] arr = template.split("\\/");
		String templateName = StringUtils.EMPTY;
		if (null != arr && arr.length > 0)
			templateName = arr[arr.length - 1];

		switch (templateName) {
		case "events-page":
			return (T) EventPagesList.class;
		case "kits-page":
			return null;
		default:
			return null;
		}
	}
}
