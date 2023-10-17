package com.workday.community.aem.core.constants;

/**
 * Interface for defining workflow constants.
 */
public interface WorkflowConstants {

  /**
   * The Constant RETIREMENT_STATUS_PROP.
   */
  String RETIREMENT_STATUS_PROP = "retirementStatus";

  /**
   * The retirement status val.
   */
  String RETIREMENT_STATUS_VAL = "retired";

  /**
   * The Constant RETIRED_BADGE_TITLE.
   */
  String RETIRED_BADGE_TITLE = "RETIRED";

  /**
   * The retirement workflow immediate model name.
   */
  String RETIREMENT_WORKFLOW_IMMEDIATE_MODEL_NAME = "retirement_workflow_immediate";

  /**
   * The retirement workflow 30 days model name.
   */
  String RETIREMENT_WORKFLOW_30_DAYS_MODEL_NAME = "retirement_workflow_30_days";

  /**
   * The actual retirement date.
   */
  String ACTUAL_RETIREMENT_DATE = "actualRetirementDate";

  /**
   * The last retirement action.
   */
  String LAST_RETIREMENT_ACTION = "lastRetirementAction";

  /**
   * The immediate retirement.
   */
  String IMMEDIATE_RETIREMENT = "Immediate Retirement";

  /**
   * The scheduled retirement.
   */
  String SCHEDULED_RETIREMENT = "Scheduled Retirement";

  /**
   * The process args.
   */
  String PROCESS_ARGS = "PROCESS_ARGS";

  /**
   * The default fallback group.
   */
  String DEFAULT_FALL_BACK_GROUP = "administrators";

  /**
   * The Constant REVIEW_REMINDER_DATE.
   */
  String REVIEW_REMINDER_DATE = "reviewReminderDate";

  /**
   * The Constant RETIREMENT_NOTIFICATION_DATE.
   */
  String RETIREMENT_NOTIFICATION_DATE = "retirementNotificationDate";

  /**
   * The Constant SCHEDULED_RETIREMENT_DATE.
   */
  String SCHEDULED_RETIREMENT_DATE = "scheduledRetirementDate";

  /**
   * The Constant EVENT_TEMPLATE_PATH.
   */
  String EVENT_TEMPLATE_PATH = "/conf/workday-community/settings/wcm/templates/events";

  /**
   * The retirement workflow.
   */
  String RETIREMENT_WORKFLOW = "/var/workflow/models/workday-community/retirement_workflow_30_days";

  /**
   * The jcr path.
   */
  String JCR_PATH = "JCR_PATH";

}
