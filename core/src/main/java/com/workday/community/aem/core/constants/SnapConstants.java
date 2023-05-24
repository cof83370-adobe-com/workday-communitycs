package com.workday.community.aem.core.constants;

import java.util.Map;

public interface SnapConstants {
  /**
   * The profile source id.
   */
  String PROFILE_SOURCE_ID = "./profile/sourceId";

  /**
   * The defeault salesforce id for menu api.
   */
  String DEFAULT_SFID_MASTER = "masterdata";

  /**
   * The user contenxt info key.
   */
  String USER_CONTEXT_INFO_KEY = "contextInfo";

  /**
   * The user role key.
   */
  String USER_CONTACT_ROLE_KEY = "contactRole";

  /**
   * The type key.
   */
  String USER_TYPE_KEY = "type";

  /**
   * The user contact info key.
   */
  String USER_CONTACT_INFORMATION_KEY = "contactInformation";

  /**
   * The isWorkmaye key.
   */
  String IS_WORKMATE_KEY = "isWorkmate";

  /**
   * The property access key.
   */
  String PROPERTY_ACCESS_KEY = "propertyAccess";

  /**
   * The property access community.
   */
  String PROPERTY_ACCESS_COMMUNITY = "Community";

  /**
   * The nsc supporting key.
   */
  String NSC_SUPPORTING_KEY = "nscSupporting";

  /** 
   * The mapping of custom role and access control tag. 
   */
 Map<String, String> CUSTOMER_ROLES_MAPPING = Map.of("Named Support Contact", "customer_name_support_contact", "Training Coordinator", "customer_training_coordinator");

 /** 
   * The mapping of ncs supporting and access control tag. 
   */
  Map<String, String> NSC_SUPPORTING_MAPPING = Map.of("Adaptive Planning", "customer_adaptive_only", "Scout", "customer_scount_only", "Peakon", "customer_peakon_only", "VNDLY", "customer_vndly_only");
 
}
