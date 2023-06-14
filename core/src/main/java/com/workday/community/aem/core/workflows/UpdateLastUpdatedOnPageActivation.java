package com.workday.community.aem.core.workflows;

import com.adobe.granite.workflow.WorkflowSession;
import com.adobe.granite.workflow.exec.WorkItem;
import com.adobe.granite.workflow.exec.WorkflowProcess;
import com.adobe.granite.workflow.metadata.MetaDataMap;
import com.workday.community.aem.core.constants.GlobalConstants;
import com.workday.community.aem.core.utils.ResolverUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.resource.*;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Calendar;
import java.util.Objects;
import java.util.TimeZone;


import static com.workday.community.aem.core.constants.GlobalConstants.EQUALS;
import static com.workday.community.aem.core.constants.GlobalConstants.PROCESS_LABEL;
import static com.workday.community.aem.core.workflows.UpdateLastUpdatedOnPageActivation.PROCESS_LABEL_VALUE;


@Component(
        service = WorkflowProcess.class,
        property = {
                PROCESS_LABEL + EQUALS + PROCESS_LABEL_VALUE
        }
)
public class UpdateLastUpdatedOnPageActivation implements WorkflowProcess {

    protected static final String PROCESS_LABEL_VALUE = "Update Last Updated Page Activation";
    private static final String TAG = UpdateLastUpdatedOnPageActivation.class.getSimpleName();
    private static final Logger LOGGER = LoggerFactory.getLogger(UpdateLastUpdatedOnPageActivation.class);

    /**
     * The resolver factory.
     */
    @Reference
    private transient ResourceResolverFactory resolverFactory;

    @Override
    public void execute(WorkItem workItem, WorkflowSession workflowSession, MetaDataMap metaDataMap) {
        // Getting payload from Workflow - workItem -> workflowData -> payload
        String payloadType = workItem.getWorkflowData().getPayloadType();
        LOGGER.error("{}: Payload type: {}", TAG, payloadType);
        // Check type of payload; there are two - JCR_PATH and JCR_UUID
        if (StringUtils.equals(payloadType, "JCR_PATH")) {
            // Get the JCR path from the payload
            String path = workItem.getWorkflowData().getPayload().toString();
            LOGGER.error("{}: Payload path: {}", TAG, path);
            // Get process arguments which will contain the properties to update
            String[] processArguments = metaDataMap.get("PROCESS_ARGS", "default").split("=");
            // Get resource resolver
            try (ResourceResolver resourceResolver = ResolverUtil.newResolver(resolverFactory, "workday-community-administrative-service");) {
                // Get Resource from path
                Resource  res = resourceResolver.getResource(path + GlobalConstants.JCR_CONTENT_PATH);
                ModifiableValueMap modifiableValueMap = Objects.requireNonNull(res).adaptTo(ModifiableValueMap.class);

                String overrideDate = modifiableValueMap.get("overrideDate", String.class);
                if(StringUtils.isBlank(overrideDate) ||  !overrideDate.equals("true")){
                    Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
                    modifiableValueMap.put("updatedDate", calendar);
                } else {
                    modifiableValueMap.remove("overrideDate");
                }

                if (resourceResolver.hasChanges()) {
                    resourceResolver.commit();
                }
            } catch (PersistenceException  | LoginException e) {
                LOGGER.error("Exception occurred when running query to get total number of pages {} ", e.getMessage());
            }
        } else {
            LOGGER.error("{}: payload type - {} is not valid", TAG, payloadType);
        }
    }
}
