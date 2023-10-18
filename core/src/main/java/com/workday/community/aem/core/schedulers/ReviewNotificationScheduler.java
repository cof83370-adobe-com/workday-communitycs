package com.workday.community.aem.core.schedulers;

import static com.workday.community.aem.core.constants.GlobalConstants.ADMIN_SERVICE_USER;
import static org.apache.sling.jcr.resource.api.JcrResourceConstants.SLING_RESOURCE_TYPE_PROPERTY;
import static com.day.cq.commons.jcr.JcrConstants.JCR_TITLE;

import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;

import org.apache.commons.mail.Email;
import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.SimpleEmail;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.apache.sling.commons.scheduler.ScheduleOptions;
import org.apache.sling.commons.scheduler.Scheduler;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.metatype.annotations.Designate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.day.cq.mailer.MessageGateway;
import com.day.cq.mailer.MessageGatewayService;
import com.workday.community.aem.core.config.ReviewNotificationSchedulerConfig;
import com.workday.community.aem.core.constants.GlobalConstants;
import com.workday.community.aem.core.services.CacheManagerService;
import com.workday.community.aem.core.services.QueryService;
import com.workday.community.aem.core.services.RunModeConfigService;

@Component(service= ReviewNotificationScheduler.class, immediate = true)
@Designate(ocd=ReviewNotificationSchedulerConfig.class)
public class ReviewNotificationScheduler implements Runnable {
	
    private static final Logger LOGGER = LoggerFactory.getLogger(ReviewNotificationScheduler.class);
    
    @Reference
    private Scheduler scheduler;
    
    /** The resolver factory. */
    @Reference
    private ResourceResolverFactory resolverFactory;
    
    /** The cache manager. */
    @Reference
    CacheManagerService cacheManager;
    
    /** The query service. */
    @Reference
    QueryService queryService;
    
    String authorDomain = "";
    
    @Reference
    MessageGatewayService messageGatewayService;
    
    @Reference
    RunModeConfigService runModeConfigService;
    
    @Activate
    protected void activate(final ReviewNotificationSchedulerConfig config) {
    	if(isAuthorInstance()) {
			authorDomain = config.authorDomain();
			LOGGER.debug(" ReviewNotificationScheduler activate method called and authorInstUrl : " + authorDomain);
	
			// Execute this method to add scheduler.
			addScheduler(config);
    	}
    }

    // Add all configurations to Schedule a scheduler depending on name and expression.
    public void addScheduler(ReviewNotificationSchedulerConfig config) {
		LOGGER.debug("Scheduler added successfully >>>>>>>   ");
		if (config.workflowNotificationReview10Months()) {
			ScheduleOptions options = scheduler.EXPR(config.workflowNotificationCron());
			options.name(config.schedulerName());

			// Add scheduler to call depending on option passed.
			scheduler.schedule(this, options);
			LOGGER.debug("Scheduler added successfully name='{}'", config.schedulerName());
		} else {
			LOGGER.debug("ReviewNotificationScheduler disabled");
		}
    }

    // Custom method to deactivate or unschedule scheduler
    public void removeScheduler(ReviewNotificationSchedulerConfig config) {
        scheduler.unschedule(config.schedulerName());
    }

    // On deactivate component it will unschedule scheduler
    @Deactivate
    protected void deactivate(ReviewNotificationSchedulerConfig config) {
    	if(isAuthorInstance()) {
    		removeScheduler(config);
    	}
    }

    // On component modification change status will remove and add scheduler
    @Modified
    protected void modified(ReviewNotificationSchedulerConfig config) {
    	if(isAuthorInstance()) {
	        removeScheduler(config);
	        addScheduler(config);
    	}
    }

    // run() method will get call every day
    @Override
    public void run() {
		LOGGER.debug("ReviewNotificationScheduler run >>>>>>>>>>>");
		try (ResourceResolver resResolver = cacheManager.getServiceResolver(ADMIN_SERVICE_USER)) {
			List<String> paths = queryService.getReviewReminderPages();
			paths.stream().filter(item -> resResolver.getResource(item) != null).forEach(path -> {
				try {
					Node node = Objects.requireNonNull(resResolver.getResource(path + GlobalConstants.JCR_CONTENT_PATH))
							.adaptTo(Node.class);

					if (node != null) {
						if (node.hasProperty(GlobalConstants.PROP_JCR_CREATED_BY)) {
							// logic to send mail to author
							String author = node.getProperty(GlobalConstants.PROP_JCR_CREATED_BY).getString();
							LOGGER.debug("author is: {}", author);

							// Regular Expression
							String regex = "^(.+)@(.+)$";
							// Compile regular expression to get the pattern
							Pattern pattern = Pattern.compile(regex);

							// Create instance of matcher
							Matcher matcher = pattern.matcher(author);

							if (author != null && matcher.matches()) {
								Node emailTemplateParentNode = Objects.requireNonNull(resResolver
										.getResource(GlobalConstants.COMMUNITY_CONTENT_NOTIFICATIONS_ROOT_PATH
												+ "/workflows/review-reminder-10-months/jcr:content/root/container/container"))
										.adaptTo(Node.class);
								
								Node emailTemplateTextNode = Objects.requireNonNull(resResolver
										.getResource(GlobalConstants.COMMUNITY_CONTENT_NOTIFICATIONS_ROOT_PATH
												+ "/workflows/review-reminder-10-months/jcr:content/root/container/container/text"))
										.adaptTo(Node.class);
								
								String msg = "";
								
								if (emailTemplateTextNode != null) {
									msg = processTextComponentFromEmailTemplate(emailTemplateTextNode, node, path);
									
									//send email once Day CQ Mail Configuration is ready
									LOGGER.debug("Sending email to author: {}", author);
									//sendEmail(author, msg, node);
								} else if (emailTemplateParentNode != null) {
									NodeIterator nodeItr = emailTemplateParentNode.getNodes();
									
									while (nodeItr.hasNext()) {
										Node childNode = nodeItr.nextNode();
										msg = processTextComponentFromEmailTemplate(childNode, node, path);
									}
									
									//send email once Day CQ Mail Configuration is ready
									LOGGER.debug("Sending email to author: {}", author);
									//sendEmail(author, msg, node);
								}	
								LOGGER.debug("Mail content is: {}", msg);
							}
						}			
					}

				} catch (Exception e) {
					LOGGER.error("Exception occured while sending the mail: {}", e.getMessage());
				}
			});
		} catch (Exception e) {
			LOGGER.error("Exception in run >>>>>>>" + e.getMessage());
		}
    }
    
    /**
     * Read text component value of email template content
     * 
     * @param textNode
     * @param node
     * @param path
     * @return
     */
    public String processTextComponentFromEmailTemplate(Node textNode, Node node, String path) {
		LOGGER.debug("processTextComponentFromEmailTemplate >>>>>>>   ");
		String text = "";
		
		try {
			if (textNode.hasProperty(SLING_RESOURCE_TYPE_PROPERTY)) {
				String resourceType = textNode.getProperty(SLING_RESOURCE_TYPE_PROPERTY).getValue().getString();
				if (resourceType.equals(GlobalConstants.TEXT_COMPONENT)) {
					text = textNode.getProperty("text").getValue().getString();
					
					String pageURL = authorDomain.concat("/editor.html").concat(path).concat(".html");
					String pageTitle = node.getProperty(JCR_TITLE).getString();
					String pageTitleLink = "<a href='".concat(pageURL).concat("' target='_blank'>").concat(pageTitle).concat("</a>");
					text = text.trim().replace("{pageTitle}", pageTitleLink.toString());
				}
			}
		} catch (RepositoryException e) {
			LOGGER.error("Iterator page jcr:content failed: {}", e.getMessage());
		}

		return text;
    }
    
    /**
     * It is use to check whether AEM is running in Author mode or not.
     * @return Returns true is AEM is in author mode, false otherwise
     */
    public boolean isAuthorInstance() {
        return (runModeConfigService.getInstance() != null && runModeConfigService.getInstance().equals(GlobalConstants.PROP_AUTHOR));
    }
    
    
    /**
     * Send mail to author
     * 
     * @param authorMail
     * @param message
     * @param node
     * @throws RepositoryException
     * @throws EmailException
     */
    public void sendEmail(String authorMail, String message, Node node) throws RepositoryException, EmailException {
    	MessageGateway<Email> messageGateway;

		Email email = new SimpleEmail();
		String subject = node.getProperty(JCR_TITLE).getString() + " to retire in 60 days";

		email.addTo(authorMail);
		email.setSubject(subject);
		email.setMsg(message);

		// Inject a Messagegateway Service and send the message
		messageGateway = messageGatewayService.getGateway(Email.class);

		// check the logs to see that messageGateway is not null
		messageGateway.send((Email) email);
    }
}
