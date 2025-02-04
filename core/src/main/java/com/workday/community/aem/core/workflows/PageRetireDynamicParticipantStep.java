package com.workday.community.aem.core.workflows;

import static com.workday.community.aem.core.constants.WorkflowConstants.DEFAULT_FALL_BACK_GROUP;
import static com.workday.community.aem.core.constants.WorkflowConstants.PROCESS_ARGS;

import com.adobe.granite.workflow.WorkflowException;
import com.adobe.granite.workflow.WorkflowSession;
import com.adobe.granite.workflow.exec.ParticipantStepChooser;
import com.adobe.granite.workflow.exec.WorkItem;
import com.adobe.granite.workflow.metadata.MetaDataMap;
import com.workday.community.aem.core.services.RunModeConfigService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.osgi.framework.Constants;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * The Class PageRetireDynamicParticipantStep.
 */
@Slf4j
@Component(service = ParticipantStepChooser.class, property = {
    Constants.SERVICE_DESCRIPTION
        + "=Participant step to choose the assignee group based on environment/workflow",
    Constants.SERVICE_VENDOR + "=Workday Community",
    "chooser.label=" + "Env Specific Dynamic Participant"
})
public class PageRetireDynamicParticipantStep implements ParticipantStepChooser {

  /**
   * The Constant ENV_VAR.
   */
  private static final String ENV_VAR = "#ENV#";

  /**
   * The run mode config service.
   */
  @Reference
  private RunModeConfigService runModeConfigService;

  /**
   * Gets the participant.
   *
   * @param workItem        the work item
   * @param workflowSession the workflow session
   * @param metaDataMap     the meta data map
   * @return the participant
   * @throws WorkflowException the workflow exception
   */
  @Override
  public String getParticipant(WorkItem workItem, WorkflowSession workflowSession,
                               MetaDataMap metaDataMap) throws WorkflowException {
    log.info("Entering PageRetireDynamicParticipantStep >>>>>> ");
    String commonName = metaDataMap.get(PROCESS_ARGS, String.class);
    String workflowTitle = workItem.getWorkflow().getWorkflowModel().getTitle();
    String env = runModeConfigService.getEnv();
    return (StringUtils.isNotBlank(env) && StringUtils.isNotBlank(commonName))
        ? getDynamicParticipant(commonName, env, workflowTitle)
        : DEFAULT_FALL_BACK_GROUP;
  }

  /**
   * Gets the dynamic participant.
   *
   * @param commonNameOfApprover the common name of approver
   * @param environment          the environment
   * @param workflowTitle        the workflow title
   * @return the dynamic participant
   */
  public String getDynamicParticipant(final String commonNameOfApprover, final String environment,
                                      final String workflowTitle) {
    final String dynamicParticipant = commonNameOfApprover
        .trim()
        .replace(ENV_VAR, environment.trim());
    log.debug("Dynamic participant for {} >>>>>> {}", workflowTitle, dynamicParticipant);
    return dynamicParticipant;
  }

}
