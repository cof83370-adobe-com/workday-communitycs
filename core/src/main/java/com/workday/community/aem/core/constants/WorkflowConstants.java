package com.workday.community.aem.core.constants;

/**
 * The Interface WorkflowConstants.
 */
public interface WorkflowConstants {

    /** The Constant RETIREMENT_STATUS_PROP. */
    String RETIREMENT_STATUS_PROP = "retirementStatus";

    /** The retirement status val. */
    String RETIREMENT_STATUS_VAL = "retired";

    /** The Constant RETIRED_BADGE_TITLE. */
    String RETIRED_BADGE_TITLE = "RETIRED";

    /** The retirement workflow immediate model name. */
    String RETIREMENT_WORKFLOW_IMMEDIATE_MODEL_NAME = "retirement_workflow_immediate";

    /** The retirement workflow 30 days model name. */
    String RETIREMENT_WORKFLOW_30_DAYS_MODEL_NAME = "retirement_workflow_30_days";

    /** The actual retirement date. */
    String ACTUAL_RETIREMENT_DATE = "actualRetirementDate";

    /** The last retirement action. */
    String LAST_RETIREMENT_ACTION = "lastRetirementAction";

    /** The immediate retirement. */
    String IMMEDIATE_RETIREMENT = "Immediate Retirement";

    /** The scheduled retirement. */
    String SCHEDULED_RETIREMENT = "Scheduled Retirement";

    /** The iso 8601 format. */
    String ISO_8601_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX";

    /** The process args. */
    String PROCESS_ARGS = "PROCESS_ARGS";

    /** The default fallback group. */
    String DEFAULT_FALL_BACK_GROUP = "administrators";
}
