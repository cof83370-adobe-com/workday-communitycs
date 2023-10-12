package com.workday.community.aem.core.workflows;

import static com.workday.community.aem.core.constants.WorkflowConstants.JCR_PATH;

import com.adobe.granite.workflow.WorkflowException;
import com.adobe.granite.workflow.WorkflowSession;
import com.adobe.granite.workflow.exec.WorkItem;
import com.adobe.granite.workflow.exec.WorkflowProcess;
import com.adobe.granite.workflow.metadata.MetaDataMap;
import com.workday.community.aem.core.constants.WorkflowConstants;

import org.apache.commons.lang3.StringUtils;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Class TerminateRetirementWorkflowProcess.
 */
@Component(service = WorkflowProcess.class, property = { "process.label = Terminate Active Retirement Workflows of Current Page" })
public class TerminateRetirementWorkflowProcess implements WorkflowProcess {
    
    /** The Constant log. */
    private static final Logger log = LoggerFactory.getLogger(TerminateRetirementWorkflowProcess.class);

    /**
     * Execute.
     *
     * @param workItem the work item
     * @param workflowSession the workflow session
     * @param metaDataMap the meta data map
     */
    @Override
    public void execute(WorkItem workItem, WorkflowSession workflowSession, MetaDataMap metaDataMap) {
        String payloadType = workItem.getWorkflowData().getPayloadType();
        String path = "";
        
        log.debug("Payload type: {}", payloadType);
        if (StringUtils.equals(payloadType, JCR_PATH)) {
            path = workItem.getWorkflowData().getPayload().toString();
            log.info("Payload path: {}", path);
            
            try {
            	if(StringUtils.isNotBlank(path)){
	                WorkItem[] workitems = workflowSession.getActiveWorkItems();
	                for (int i = 0; i < workitems.length; i++) {
	                	if(workitems[i].getWorkflowData().getPayload().equals(path) && 
	                			workitems[i].getWorkflow().getWorkflowModel().getId().equalsIgnoreCase(WorkflowConstants.RETIREMENT_WORKFLOW) && 
	                			workitems[i].getWorkflow().getState().equals("RUNNING")) {
	                		
	                	   workflowSession.terminateWorkflow(workitems[i].getWorkflow());
	                	}
	                }
                }
            } catch (WorkflowException e) {
            	log.error("WorkflowException in TerminateRetirementWorkflowProcess: {}", e.getMessage());
			}
        }
    }
}
