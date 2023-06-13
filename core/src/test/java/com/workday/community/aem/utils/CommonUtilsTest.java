package com.workday.community.aem.utils;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.workday.community.aem.core.constants.WccConstants;
import com.workday.community.aem.core.utils.CommonUtils;
import org.apache.jackrabbit.api.security.user.User;
import org.apache.jackrabbit.api.security.user.UserManager;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.Value;

import static junit.framework.Assert.assertNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;

/**
 * The Class CommonUtilsTest.
 */
@ExtendWith({MockitoExtension.class})
public class CommonUtilsTest {
    
    /** The ResourceResolver. */
    ResourceResolver resourceResolver;

    /** The user. */
    private User user;
    
    @BeforeEach
    public void setup() throws RepositoryException {
        resourceResolver = mock(ResourceResolver.class);
        Session session = mock(Session.class);
        // The service UserManager.
        UserManager userManager = mock(UserManager.class);
        user = mock(User.class);
        lenient().when(resourceResolver.adaptTo(Session.class)).thenReturn(session);
        lenient().when(session.getUserID()).thenReturn("testUser");
        lenient().when(resourceResolver.adaptTo(UserManager.class)).thenReturn(userManager);
        lenient().when(userManager.getAuthorizable(anyString())).thenReturn(user);
    }

    /**
     * Test getLoggedInUserSourceId.
     */
    @Test
    public void testGetLoggedInUserSourceId() throws RepositoryException {
        Value value = mock(Value.class);
        Value[] values = { value };
        String expectedSfId = "test sfid";
        lenient().when(value.getString()).thenReturn(expectedSfId);
        lenient().when(user.getProperty(WccConstants.PROFILE_SOURCE_ID)).thenReturn(values);
        String sfId = CommonUtils.getLoggedInUserSourceId(resourceResolver);
        assertEquals(expectedSfId, sfId);
    }

    @Test
    public void testGetLoggedInUserSourceIdWithException() throws RepositoryException {
        Value value = mock(Value.class);
        String expectedSfId = "test sfid";
        lenient().when(value.getString()).thenReturn(expectedSfId);
        lenient().when(user.getProperty(WccConstants.PROFILE_SOURCE_ID)).thenThrow(new RepositoryException());
        String sfId = CommonUtils.getLoggedInUserSourceId(resourceResolver);
        assertNull(sfId);
    }

    /**
     * Test getLoggedInUserId.
     */
    @Test 
    public void testGetLoggedInUserId() throws RepositoryException {
        Value value = mock(Value.class);
        String expectedUserId = "test user id";
        Value[] values = {value};
        lenient().when(value.getString()).thenReturn(expectedUserId);
        lenient().when(user.getProperty(WccConstants.PROFILE_OKTA_ID)).thenReturn(values);
        String userId = CommonUtils.getLoggedInUserId(resourceResolver);
        assertEquals(expectedUserId, userId);
    }

    /**
     * Test getLoggedInUserId.
     */
    @Test
    public void testGetLoggedInUserIdWithException() throws RepositoryException {
        Value value = mock(Value.class);
        String expectedUserId = "test user id";
        lenient().when(value.getString()).thenReturn(expectedUserId);
        lenient().when(user.getProperty(WccConstants.PROFILE_OKTA_ID)).thenThrow(new RepositoryException());
        String userId = CommonUtils.getLoggedInUserId(resourceResolver);
        assertNull(userId);
    }

    /**
     * Test getLoggedInCustomerType.
     */
    @Test 
    public void testGetLoggedInCustomerType() throws RepositoryException {
        Value value = mock(Value.class);
        String expectedCustomerType = "test customer type";
        Value[] values = {value};
        lenient().when(value.getString()).thenReturn(expectedCustomerType);
        lenient().when(user.getProperty(WccConstants.CC_TYPE)).thenReturn(values);
        String customerType = CommonUtils.getLoggedInCustomerType(resourceResolver);
        assertEquals(expectedCustomerType, customerType);
    }

    /**
     * Test testGetLoggedInCustomerTypeWithException
     * @throws RepositoryException RepositoryException object.
     */
    @Test
    public void testGetLoggedInCustomerTypeWithException() throws RepositoryException {
        Value value = mock(Value.class);
        Value[] values = {value};
        lenient().when(value.getString()).thenThrow(new RepositoryException());
        lenient().when(user.getProperty(WccConstants.CC_TYPE)).thenReturn(values);
        String customerType = CommonUtils.getLoggedInCustomerType(resourceResolver);
        assertNull(customerType);
    }

    /**
     * Test getLoggedInUser.
     */
    @Test
    public void testGetLoggedInUser() {
        User testUser = CommonUtils.getLoggedInUser(resourceResolver);
        assertEquals(testUser, user);

    }

    @Test
    public void testGetLoggedInUserWithException() throws RepositoryException {
        Session session = mock(Session.class);
        UserManager userManager = mock(UserManager.class);
        lenient().when(resourceResolver.adaptTo(Session.class)).thenReturn(session);
        lenient().when(resourceResolver.adaptTo(UserManager.class)).thenReturn(userManager);
        lenient().when(userManager.getAuthorizable(anyString())).thenThrow(new RepositoryException());
        User testUser = CommonUtils.getLoggedInUser(resourceResolver);
        assertNull(testUser);
    }

    /**
     * Test getLoggedInUserAsNode.
     */
    @Test
    public void testGetLoggedInUserAsNode() throws RepositoryException {
        User testUser = CommonUtils.getLoggedInUser(resourceResolver);
        lenient().when(testUser.getPath()).thenReturn("user path");
        
        Resource resource = mock(Resource.class);
        Node expectedUserNode = mock(Node.class);
        lenient().when(resourceResolver.getResource("user path")).thenReturn(resource);
        lenient().when(resource.adaptTo(Node.class)).thenReturn(expectedUserNode);
        Node userNode = CommonUtils.getLoggedInUserAsNode(resourceResolver);
        assertEquals(userNode, expectedUserNode);
    }

    @Test
    public void testGetLoggedInUserAsNodeWithException() throws RepositoryException {
        Session session = mock(Session.class);
        UserManager userManager = mock(UserManager.class);
        lenient().when(resourceResolver.adaptTo(Session.class)).thenReturn(session);
        lenient().when(resourceResolver.adaptTo(UserManager.class)).thenReturn(userManager);
        lenient().when(userManager.getAuthorizable(anyString())).thenThrow(new RepositoryException());
        Node userNode = CommonUtils.getLoggedInUserAsNode(resourceResolver);
        assertNull(userNode);
    }

    /**
     * Test testUpdateTargetToSource.
     */
    @Test
    public void testUpdateTargetToSource() {
        String sourceString = "{\"primary\":{\"menu\":[{\"type\":\"Primary\",\"tooltip\":\"Learnmenu\",\"title\":\"Learn\",\"parent\":null,\"order\":10,\"level\":\"1\",\"id\":\"aTD520000008ONsGAM\",\"icon\":null,\"href\":null,\"description\":\"Gettingstartedandlearning-focusedresources\",\"children\":[{\"type\":\"Primary\",\"tooltip\":\"GetStarted\",\"title\":\"GetStarted\",\"parent\":\"aTD520000008ONsGAM\",\"order\":10,\"level\":\"2\",\"id\":\"aTD520000008ONtGAM\",\"icon\":null,\"href\":\"https://preprod.community-workday.com/learn/get-started\",\"description\":\"Organizedgettingstartedinfoforcustomers\",\"children\":[]},{\"type\":\"Primary\",\"tooltip\":\"Documentation\",\"title\":\"Documentation\",\"parent\":\"aTD520000008ONsGAM\",\"order\":20,\"level\":\"2\",\"id\":\"aTD520000008ONuGAM\",\"icon\":null,\"href\":\"https://preprod.community-workday.com/learn/documentation\",\"description\":\"Organizedlinkstoofficialdocumentationpages\",\"children\":[]},{\"type\":\"Primary\",\"tooltip\":\"TrainingInformation\",\"title\":\"TrainingInformation\",\"parent\":\"aTD520000008ONsGAM\",\"order\":30,\"level\":\"2\",\"id\":\"aTD520000008ONvGAM\",\"icon\":null,\"href\":\"https://preprod.community-workday.com/learn/training\",\"description\":\"Trainingofferingsandquicklinks\",\"children\":[]},{\"type\":\"Primary\",\"tooltip\":\"Solutions\",\"title\":\"Solutions\",\"parent\":\"aTD520000008ONsGAM\",\"order\":40,\"level\":\"2\",\"id\":\"aTD520000008ONwGAM\",\"icon\":null,\"href\":\"https://preprod.community-workday.com/learn/solutions\",\"description\":\"SolutionsandtoolkitsforcustomersandpartnersforimplementingintheirWorkdaytenants\",\"children\":[]},{\"type\":\"Primary\",\"tooltip\":\"StrategyRoadmaps\",\"title\":\"StrategyRoadmaps\",\"parent\":\"aTD520000008ONsGAM\",\"order\":50,\"level\":\"2\",\"id\":\"aTD520000008ONxGAM\",\"icon\":null,\"href\":\"https://preprod.community-workday.com/learn/strategy-roadmaps\",\"description\":\"Productspecificroadmaps\",\"children\":[]},{\"type\":\"Primary\",\"tooltip\":\"SharedDemonstrationTenants\",\"title\":\"SharedDemonstrationTenants\",\"parent\":\"aTD520000008ONsGAM\",\"order\":60,\"level\":\"2\",\"id\":\"aTD520000008ONyGAM\",\"icon\":null,\"href\":\"https://preprod.community-workday.com/learn/demonstration-tenants\",\"description\":\"Overviewandaccesstoshareddemonstrationtenants\",\"children\":[]},{\"type\":\"Primary\",\"tooltip\":\"ServicesBasecamp\",\"title\":\"ServicesBasecamp\",\"parent\":\"aTD520000008ONsGAM\",\"order\":70,\"level\":\"2\",\"id\":\"aTD520000008ONzGAM\",\"icon\":null,\"href\":\"https://preprod.community-workday.com/learn/services-basecamp\",\"description\":\"Servicesandpartnerspecificresources\",\"children\":[]}]},{\"type\":\"Primary\",\"tooltip\":\"ProductHubMenu\",\"title\":\"Products\",\"parent\":null,\"order\":20,\"level\":\"1\",\"id\":\"aTD520000008ONfGAM\",\"icon\":null,\"href\":null,\"description\":\"Producthubsorganizecontentanditeractionopportunitiesbyproduct\",\"children\":[{\"type\":\"Primary\",\"tooltip\":\"HumanCapitalManagementhub\",\"title\":\"HumanCapitalManagement\",\"parent\":\"aTD520000008ONfGAM\",\"order\":10,\"level\":\"2\",\"id\":\"aTD520000008ONgGAM\",\"icon\":null,\"href\":\"https://preprod.community-workday.com/products/human-capital-management\",\"description\":\"Specificproducthubnavigationlink\",\"children\":[]},{\"type\":\"Primary\",\"tooltip\":\"FinancialManagementhub\",\"title\":\"FinancialManagement\",\"parent\":\"aTD520000008ONfGAM\",\"order\":20,\"level\":\"2\",\"id\":\"aTD520000008ONhGAM\",\"icon\":null,\"href\":\"https://preprod.community-workday.com/products/financial-management\",\"description\":\"Specificproducthubnavigationlink\",\"children\":[]},{\"type\":\"Primary\",\"tooltip\":\"WorkdayPayrollhub\",\"title\":\"WorkdayPayroll\",\"parent\":\"aTD520000008ONfGAM\",\"order\":30,\"level\":\"2\",\"id\":\"aTD520000008ONiGAM\",\"icon\":null,\"href\":\"https://preprod.community-workday.com/products/workday-payroll\",\"description\":\"Specificproducthubnavigationlink\",\"children\":[]},{\"type\":\"Primary\",\"tooltip\":\"Platform&ProductExtensions\",\"title\":\"Platform&ProductExtensions\",\"parent\":\"aTD520000008ONfGAM\",\"order\":40,\"level\":\"2\",\"id\":\"aTD520000008ONjGAM\",\"icon\":null,\"href\":\"https://preprod.community-workday.com/products/platform-and-product-extensions\",\"description\":\"Specificproducthubnavigationlink\",\"children\":[]},{\"type\":\"Primary\",\"tooltip\":\"EmergingProductshub\",\"title\":\"EmergingProducts\",\"parent\":\"aTD520000008ONfGAM\",\"order\":50,\"level\":\"2\",\"id\":\"aTD520000008ONkGAM\",\"icon\":null,\"href\":\"https://preprod.community-workday.com/products/emerging-products\",\"description\":\"Specificproducthubnavigationlink\",\"children\":[]},{\"type\":\"Primary\",\"tooltip\":\"WorkforceManagementhub\",\"title\":\"WorkforceManagement\",\"parent\":\"aTD520000008ONfGAM\",\"order\":60,\"level\":\"2\",\"id\":\"aTD520000008ONlGAM\",\"icon\":null,\"href\":\"https://preprod.community-workday.com/products/workforce-management\",\"description\":\"Specificproducthubnavigationlink\",\"children\":[]},{\"type\":\"Primary\",\"tooltip\":\"Viewallproducthubs\",\"title\":\"AllProducts\",\"parent\":\"aTD520000008ONfGAM\",\"order\":70,\"level\":\"2\",\"id\":\"aTD520000008ONmGAM\",\"icon\":null,\"href\":\"https://preprod.community-workday.com/all-products\",\"description\":\"Linkstoallavailableproducthubs\",\"children\":[]}]},{\"type\":\"Primary\",\"tooltip\":\"Collaboratemenu\",\"title\":\"Collaborate\",\"parent\":null,\"order\":30,\"level\":\"1\",\"id\":\"aTD520000008OO8GAM\",\"icon\":null,\"href\":null,\"description\":\"Interactiveusergeneratedcontentopportunities\",\"children\":[{\"type\":\"Primary\",\"tooltip\":\"Forums\",\"title\":\"Discuss\",\"parent\":\"aTD520000008OO8GAM\",\"order\":10,\"level\":\"2\",\"id\":\"aTD520000008OO9GAM\",\"icon\":null,\"href\":\"https://stage-kr.community-workday.com/t5/Discuss/ct-p/Category_Discuss\",\"description\":\"Landingpageforforumsavailabletouser\",\"children\":[]},{\"type\":\"Primary\",\"tooltip\":\"Brainstorms\",\"title\":\"Innovate\",\"parent\":\"aTD520000008OO8GAM\",\"order\":20,\"level\":\"2\",\"id\":\"aTD520000008OOAGA2\",\"icon\":null,\"href\":\"https://stage-kr.community-workday.com/t5/Innovate/ct-p/Category_Innovate\",\"description\":\"LandingpageforviewingandcreatingbrainstormsforimprovingWorkday\",\"children\":[]},{\"type\":\"Primary\",\"tooltip\":\"Events\",\"title\":\"Connect\",\"parent\":\"aTD520000008OO8GAM\",\"order\":30,\"level\":\"2\",\"id\":\"aTD520000008OOBGA2\",\"icon\":null,\"href\":\"https://stage-kr.community-workday.com/t5/Connect/ct-p/Category_Connect\",\"description\":\"Upcomingeventsausermayattend\",\"children\":[]},{\"type\":\"Primary\",\"tooltip\":\"UserGroups\",\"title\":\"Partners\",\"parent\":\"aTD520000008OO8GAM\",\"order\":40,\"level\":\"2\",\"id\":\"aTD520000008OOCGA2\",\"icon\":null,\"href\":\"https://stage-kr.community-workday.com/t5/Partners/ct-p/Category_Partners\",\"description\":\"Organizepartner-specificresourcesandparticipationopportunities\",\"children\":[]},{\"type\":\"Primary\",\"tooltip\":\"DesignPartnerOpportunities\",\"title\":\"Discover\",\"parent\":\"aTD520000008OO8GAM\",\"order\":50,\"level\":\"2\",\"id\":\"aTD520000008OODGA2\",\"icon\":null,\"href\":\"https://stage-kr.community-workday.com/t5/Discover/ct-p/Category_Discover\",\"description\":\"Membernewsandupdatesandcollaborateresources\",\"children\":[]}]},{\"type\":\"Primary\",\"tooltip\":\"FeatureReleasemenu\",\"title\":\"FeatureRelease\",\"parent\":null,\"order\":40,\"level\":\"1\",\"id\":\"aTD520000008ONnGAM\",\"icon\":null,\"href\":null,\"description\":\"NavtositestohelpuserswithWorkdayFeatureReleases\",\"children\":[{\"type\":\"Primary\",\"tooltip\":\"FeatureReleaseGuidebook\",\"title\":\"FeatureReleaseGuidebook\",\"parent\":\"aTD520000008ONnGAM\",\"order\":10,\"level\":\"2\",\"id\":\"aTD520000008ONoGAM\",\"icon\":null,\"href\":\"https://preprod.community-workday.com/feature-release/guidebook\",\"description\":\"Frameworkforcustomerstouseforourreleasecycle\",\"children\":[]},{\"type\":\"Primary\",\"tooltip\":\"ScheduledMaintenance\",\"title\":\"ScheduledMaintenance\",\"parent\":\"aTD520000008ONnGAM\",\"order\":20,\"level\":\"2\",\"id\":\"aTD520000008ONpGAM\",\"icon\":null,\"href\":\"https://preprod.community-workday.com/feature-release/maintenance\",\"description\":\"PlannedMaintenancewindows\",\"children\":[]},{\"type\":\"Primary\",\"tooltip\":\"ReleaseCenter\",\"title\":\"ReleaseCenter\",\"parent\":\"aTD520000008ONnGAM\",\"order\":30,\"level\":\"2\",\"id\":\"aTD520000008ONqGAM\",\"icon\":null,\"href\":\"https://preprod.community-workday.com/feature-release/release-center\",\"description\":\"TheReleaseCenterorganizesreleasedetailsforthemostrecentfeaturerelease.TheReleasePrepCenterreplacesitforthe6weeksapproachinganewrelease,andfeaturesthenextrelease.\",\"children\":[]},{\"type\":\"Primary\",\"tooltip\":\"ReleaseNotes\",\"title\":\"ReleaseNotes\",\"parent\":\"aTD520000008ONnGAM\",\"order\":40,\"level\":\"2\",\"id\":\"aTD520000008ONrGAM\",\"icon\":null,\"href\":\"https://preprod.community-workday.com/feature-release/release-notes\",\"description\":\"Comprehensivereleasenotes\",\"children\":[]}]},{\"type\":\"Primary\",\"tooltip\":\"GetHelpmenu\",\"title\":\"GetHelp\",\"parent\":null,\"order\":50,\"level\":\"1\",\"id\":\"aTD520000008OOEGA2\",\"icon\":null,\"href\":null,\"description\":\"Self-serviceandsolutionfindingresources\",\"children\":[{\"type\":\"Primary\",\"tooltip\":\"DatacenterStatus\",\"title\":\"DatacenterStatus\",\"parent\":\"aTD520000008OOEGA2\",\"order\":10,\"level\":\"2\",\"id\":\"aTD520000008OOFGA2\",\"icon\":null,\"href\":\"https://preprod.community-workday.com/get-help/datacenter-status\",\"description\":\"SummaryonthecurrenthealthstatusofWorkdaydatacenters\",\"children\":[]},{\"type\":\"Primary\",\"tooltip\":\"TroubleshootingResources\",\"title\":\"TroubleshootingResources\",\"parent\":\"aTD520000008OOEGA2\",\"order\":20,\"level\":\"2\",\"id\":\"aTD520000008OOGGA2\",\"icon\":null,\"href\":\"https://preprod.community-workday.com/get-help/troubleshooting-resources\",\"description\":\"Organizedlistofresourcesandlinkstohelptroubleshooting\",\"children\":[]},{\"type\":\"Primary\",\"tooltip\":\"CustomerAlerts\",\"title\":\"CustomerAlerts\",\"parent\":\"aTD520000008OOEGA2\",\"order\":30,\"level\":\"2\",\"id\":\"aTD520000008OOHGA2\",\"icon\":null,\"href\":\"https://preprod.community-workday.com/get-help/customer-alerts\",\"description\":\"Supportalertstocustomersrelatedtooperations,defectsandotherproduct-specificinformation\",\"children\":[]},{\"type\":\"Primary\",\"tooltip\":\"ServicesAlerts\",\"title\":\"ServicesAlerts\",\"parent\":\"aTD520000008OOEGA2\",\"order\":40,\"level\":\"2\",\"id\":\"aTD520000008OOIGA2\",\"icon\":null,\"href\":\"https://preprod.community-workday.com/get-help/services-alerts\",\"description\":\"Supportalertstostaffandpartnersrelatedtooperations,defectsandotherproduct-specificinformation\",\"children\":[]},{\"type\":\"Primary\",\"tooltip\":\"TrainingCoordinatorHub\",\"title\":\"TrainingCoordinatorHub\",\"parent\":\"aTD520000008OOEGA2\",\"order\":60,\"level\":\"2\",\"id\":\"aTD520000008OOJGA2\",\"icon\":null,\"href\":\"https://preprod.community-workday.com/get-help/training-coordinator-hub\",\"description\":\"TrainingandresourcesforusersintheTrainingCoordinatorWorkdaySupportRole\",\"children\":[]},{\"type\":\"Primary\",\"tooltip\":\"NSCHub\",\"title\":\"NSCHub\",\"parent\":\"aTD520000008OOEGA2\",\"order\":70,\"level\":\"2\",\"id\":\"aTD520000008OOKGA2\",\"icon\":null,\"href\":\"https://preprod.community-workday.com/get-help/nsc-hub\",\"description\":\"TrainingandresourcesforusersintheNamedSupportContactWorkdaySupportrole\",\"children\":[]}]},{\"type\":\"Primary\",\"tooltip\":\"Managemenu\",\"title\":\"Manage\",\"parent\":null,\"order\":60,\"level\":\"1\",\"id\":\"aTD520000008OO0GAM\",\"icon\":null,\"href\":null,\"description\":\"SupportandWorkdayRoleAdministrativeFunctions\",\"children\":[{\"type\":\"Primary\",\"tooltip\":\"Cases\",\"title\":\"Cases\",\"parent\":\"aTD520000008OO0GAM\",\"order\":10,\"level\":\"2\",\"id\":\"aTD520000008OO1GAM\",\"icon\":null,\"href\":\"https://workday--uat.sandbox.my.site.com/workdaycustomercenter/apex/CC_CommunityCases\",\"description\":\"Customercaseportal\",\"children\":[]},{\"type\":\"Primary\",\"tooltip\":\"Users\",\"title\":\"Users\",\"parent\":\"aTD520000008OO0GAM\",\"order\":20,\"level\":\"2\",\"id\":\"aTD520000008OO2GAM\",\"icon\":null,\"href\":\"https://workday--uat.sandbox.my.site.com/CommunityAccess/s/user-management\",\"description\":\"OrgAdministrationandCustomerCenterContactmanagement\",\"children\":[]},{\"type\":\"Primary\",\"tooltip\":null,\"title\":\"ContactsManagement\",\"parent\":\"aTD520000008OO0GAM\",\"order\":30,\"level\":\"2\",\"id\":\"aTD520000008OO3GAM\",\"icon\":null,\"href\":\"https://workday--uat.sandbox.my.site.com/workdaycustomercenter/apex/CC_NSCAdministration\",\"description\":\"Supportcontactmangement\",\"children\":[]},{\"type\":\"Primary\",\"tooltip\":\"Training\",\"title\":\"Training\",\"parent\":\"aTD520000008OO0GAM\",\"order\":40,\"level\":\"2\",\"id\":\"aTD520000008OO4GAM\",\"icon\":null,\"href\":\"https://workday--uat.sandbox.my.site.com/workdaycustomercenter/apex/LMS_TrainingUnits\",\"description\":\"Trainingadministrationtasks\",\"children\":[]},{\"type\":\"Primary\",\"tooltip\":\"Tenants\",\"title\":\"Tenants\",\"parent\":\"aTD520000008OO0GAM\",\"order\":50,\"level\":\"2\",\"id\":\"aTD520000008OO5GAM\",\"icon\":null,\"href\":\"https://workday--uat.sandbox.my.site.com/workdaycustomercenter/apex/CC_CommunityTenants\",\"description\":\"Tenantrequestandmanagement\",\"children\":[]},{\"type\":\"Primary\",\"tooltip\":\"MyOrganization\",\"title\":\"MyOrganization\",\"parent\":\"aTD520000008OO0GAM\",\"order\":60,\"level\":\"2\",\"id\":\"aTD520000008OO6GAM\",\"icon\":null,\"href\":\"https://workday--uat.sandbox.my.site.com/CommunityAccess/s/\",\"description\":\"Organizationprofilemanagement\",\"children\":[]},{\"type\":\"Primary\",\"tooltip\":\"Reports\",\"title\":\"Reports\",\"parent\":\"aTD520000008OO0GAM\",\"order\":70,\"level\":\"2\",\"id\":\"aTD520000008OOYGA2\",\"icon\":null,\"href\":\"https://workday--uat.sandbox.my.site.com/workdaycustomercenter/00O/o\",\"description\":\"Supportorientedreports\",\"children\":[]},{\"type\":\"Primary\",\"tooltip\":\"Reports\",\"title\":\"ContentLibrary\",\"parent\":\"aTD520000008OO0GAM\",\"order\":80,\"level\":\"2\",\"id\":\"aTD520000008OO7GAM\",\"icon\":null,\"href\":\"https://workday--uat.sandbox.my.site.com/workdaycustomercenter/apex/CC_ListContent\",\"description\":\"Supportorientedreportsincludingauditandcompliance\",\"children\":[]}]}]}}";
        String targetString = "{\"primary\":{\"menu\":[{\"type\":\"Primary\",\"tooltip\":\"Learnmenu\",\"title\":\"Learn\",\"parent\":null,\"order\":10,\"level\":\"1\",\"id\":\"aTD520000008ONsGAM\",\"icon\":null,\"href\":null,\"description\":\"Gettingstartedandlearning-focusedresources\",\"children\":[{\"type\":\"Primary\",\"tooltip\":\"GetStarted\",\"title\":\"GetStarted\",\"parent\":\"aTD520000008ONsGAM\",\"order\":10,\"level\":\"2\",\"id\":\"aTD520000008ONtGAM\",\"icon\":null,\"href\":\"https://beta-content.workday.com/en-us/learn/get-started.html\",\"description\":\"Organizedgettingstartedinfoforcustomers\",\"children\":[]},{\"type\":\"Primary\",\"tooltip\":\"Resources\",\"title\":\"Resources\",\"parent\":\"aTI4X0000008OIyWAM\",\"order\":20,\"level\":\"2\",\"id\":\"aTI4X0000008OJ0WAM\",\"href\":\"https://beta-content.workday.com/en-us/learn/resources.html\",\"description\":\"Organizedlinkstoofficialdocumentationpages\",\"children\":[]},{\"type\":\"Primary\",\"tooltip\":\"Documentation\",\"title\":\"Documentation\",\"parent\":\"aTD520000008ONsGAM\",\"order\":20,\"level\":\"2\",\"id\":\"aTD520000008ONuGAM\",\"icon\":null,\"href\":\"https://preprod.community-workday.com/learn/documentation\",\"description\":\"Organizedlinkstoofficialdocumentationpages\",\"children\":[]},{\"type\":\"Primary\",\"tooltip\":\"TrainingInformation\",\"title\":\"TrainingInformation\",\"parent\":\"aTD520000008ONsGAM\",\"order\":30,\"level\":\"2\",\"id\":\"aTD520000008ONvGAM\",\"icon\":null,\"href\":\"https://beta-content.workday.com/en-us/learn/training-information.html\",\"description\":\"Trainingofferingsandquicklinks\",\"children\":[]},{\"type\":\"Primary\",\"tooltip\":\"Solutions\",\"title\":\"Solutions\",\"parent\":\"aTD520000008ONsGAM\",\"order\":40,\"level\":\"2\",\"id\":\"aTD520000008ONwGAM\",\"icon\":null,\"href\":\"https://beta-content.workday.com/en-us/learn/solutions.html\",\"description\":\"SolutionsandtoolkitsforcustomersandpartnersforimplementingintheirWorkdaytenants\",\"children\":[]},{\"type\":\"Primary\",\"tooltip\":\"StrategyRoadmaps\",\"title\":\"StrategyRoadmaps\",\"parent\":\"aTD520000008ONsGAM\",\"order\":50,\"level\":\"2\",\"id\":\"aTD520000008ONxGAM\",\"icon\":null,\"href\":\"https://beta-content.workday.com/en-us/learn/strategy-roadmaps.html\",\"description\":\"Productspecificroadmaps\",\"children\":[]},{\"type\":\"Primary\",\"tooltip\":\"SharedDemonstrationTenants\",\"title\":\"SharedDemonstrationTenants\",\"parent\":\"aTD520000008ONsGAM\",\"order\":60,\"level\":\"2\",\"id\":\"aTD520000008ONyGAM\",\"icon\":null,\"href\":\"https://beta-content.workday.com/en-us/learn/shared-demonstration-tenants.html\",\"description\":\"Overviewandaccesstoshareddemonstrationtenants\",\"children\":[]},{\"type\":\"Primary\",\"tooltip\":\"ServicesBasecamp\",\"title\":\"ServicesBasecamp\",\"parent\":\"aTD520000008ONsGAM\",\"order\":70,\"level\":\"2\",\"id\":\"aTD520000008ONzGAM\",\"icon\":null,\"href\":\"https://beta-content.workday.com/en-us/learn/services-basecamp.html\",\"description\":\"Servicesandpartnerspecificresources\",\"children\":[]}]},{\"type\":\"Primary\",\"tooltip\":\"ProductHubMenu\",\"title\":\"Products\",\"parent\":null,\"order\":20,\"level\":\"1\",\"id\":\"aTD520000008ONfGAM\",\"icon\":null,\"href\":null,\"description\":\"Producthubsorganizecontentanditeractionopportunitiesbyproduct\",\"children\":[{\"type\":\"Primary\",\"tooltip\":\"HumanCapitalManagementhub\",\"title\":\"HumanCapitalManagement\",\"parent\":\"aTD520000008ONfGAM\",\"order\":10,\"level\":\"2\",\"id\":\"aTD520000008ONgGAM\",\"icon\":null,\"href\":\"https://beta-content.workday.com/en-us/products/human-capital-management.html\",\"description\":\"Specificproducthubnavigationlink\",\"children\":[]},{\"type\":\"Primary\",\"tooltip\":\"FinancialManagementhub\",\"title\":\"FinancialManagement\",\"parent\":\"aTD520000008ONfGAM\",\"order\":20,\"level\":\"2\",\"id\":\"aTD520000008ONhGAM\",\"icon\":null,\"href\":\"https://beta-content.workday.com/en-us/products/financial-management.html\",\"description\":\"Specificproducthubnavigationlink\",\"children\":[]},{\"type\":\"Primary\",\"tooltip\":\"WorkdayPayrollhub\",\"title\":\"WorkdayPayroll\",\"parent\":\"aTD520000008ONfGAM\",\"order\":30,\"level\":\"2\",\"id\":\"aTD520000008ONiGAM\",\"icon\":null,\"href\":\"https://beta-content.workday.com/en-us/products/payroll.html\",\"description\":\"Specificproducthubnavigationlink\",\"children\":[]},{\"type\":\"Primary\",\"tooltip\":\"Platform&ProductExtensions\",\"title\":\"Platform&ProductExtensions\",\"parent\":\"aTD520000008ONfGAM\",\"order\":40,\"level\":\"2\",\"id\":\"aTD520000008ONjGAM\",\"icon\":null,\"href\":\"https://beta-content.workday.com/en-us/products/platform-and-product-extensions.html\",\"description\":\"Specificproducthubnavigationlink\",\"children\":[]},{\"type\":\"Primary\",\"tooltip\":\"EmergingProductshub\",\"title\":\"EmergingProducts\",\"parent\":\"aTD520000008ONfGAM\",\"order\":50,\"level\":\"2\",\"id\":\"aTD520000008ONkGAM\",\"icon\":null,\"href\":\"https://preprod.community-workday.com/products/emerging-products\",\"description\":\"Specificproducthubnavigationlink\",\"children\":[]},{\"type\":\"Primary\",\"tooltip\":\"WorkforceManagementhub\",\"title\":\"WorkforceManagement\",\"parent\":\"aTD520000008ONfGAM\",\"order\":60,\"level\":\"2\",\"id\":\"aTD520000008ONlGAM\",\"icon\":null,\"href\":\"https://beta-content.workday.com/en-us/products/workforce-management.html\",\"description\":\"Specificproducthubnavigationlink\",\"children\":[]},{\"type\":\"Primary\",\"tooltip\":\"Viewallproducthubs\",\"title\":\"AllProducts\",\"parent\":\"aTD520000008ONfGAM\",\"order\":70,\"level\":\"2\",\"id\":\"aTD520000008ONmGAM\",\"icon\":null,\"href\":\"https://beta-content.workday.com/en-us/products.html\",\"description\":\"Linkstoallavailableproducthubs\",\"children\":[]}]},{\"type\":\"Primary\",\"tooltip\":\"Collaboratemenu\",\"title\":\"Collaborate\",\"parent\":null,\"order\":30,\"level\":\"1\",\"id\":\"aTD520000008OO8GAM\",\"icon\":null,\"href\":null,\"description\":\"Interactiveusergeneratedcontentopportunities\",\"children\":[{\"type\":\"Primary\",\"tooltip\":\"Forums\",\"title\":\"Discuss\",\"parent\":\"aTD520000008OO8GAM\",\"order\":10,\"level\":\"2\",\"id\":\"aTD520000008OO9GAM\",\"icon\":null,\"href\":\"https://stage-kr.community-workday.com/t5/Discuss/ct-p/Category_Discuss\",\"description\":\"Landingpageforforumsavailabletouser\",\"children\":[]},{\"type\":\"Primary\",\"tooltip\":\"Brainstorms\",\"title\":\"Innovate\",\"parent\":\"aTD520000008OO8GAM\",\"order\":20,\"level\":\"2\",\"id\":\"aTD520000008OOAGA2\",\"icon\":null,\"href\":\"https://stage-kr.community-workday.com/t5/Innovate/ct-p/Category_Innovate\",\"description\":\"LandingpageforviewingandcreatingbrainstormsforimprovingWorkday\",\"children\":[]},{\"type\":\"Primary\",\"tooltip\":\"Events\",\"title\":\"Connect\",\"parent\":\"aTD520000008OO8GAM\",\"order\":30,\"level\":\"2\",\"id\":\"aTD520000008OOBGA2\",\"icon\":null,\"href\":\"https://stage-kr.community-workday.com/t5/Connect/ct-p/Category_Connect\",\"description\":\"Upcomingeventsausermayattend\",\"children\":[]},{\"type\":\"Primary\",\"tooltip\":\"UserGroups\",\"title\":\"Partners\",\"parent\":\"aTD520000008OO8GAM\",\"order\":40,\"level\":\"2\",\"id\":\"aTD520000008OOCGA2\",\"icon\":null,\"href\":\"https://stage-kr.community-workday.com/t5/Partners/ct-p/Category_Partners\",\"description\":\"Organizepartner-specificresourcesandparticipationopportunities\",\"children\":[]},{\"type\":\"Primary\",\"tooltip\":\"DesignPartnerOpportunities\",\"title\":\"Discover\",\"parent\":\"aTD520000008OO8GAM\",\"order\":50,\"level\":\"2\",\"id\":\"aTD520000008OODGA2\",\"icon\":null,\"href\":\"https://stage-kr.community-workday.com/t5/Discover/ct-p/Category_Discover\",\"description\":\"Membernewsandupdatesandcollaborateresources\",\"children\":[]}]},{\"type\":\"Primary\",\"tooltip\":\"FeatureReleasemenu\",\"title\":\"FeatureRelease\",\"parent\":null,\"order\":40,\"level\":\"1\",\"id\":\"aTD520000008ONnGAM\",\"icon\":null,\"href\":null,\"description\":\"NavtositestohelpuserswithWorkdayFeatureReleases\",\"children\":[{\"type\":\"Primary\",\"tooltip\":\"FeatureReleaseGuidebook\",\"title\":\"FeatureReleaseGuidebook\",\"parent\":\"aTD520000008ONnGAM\",\"order\":10,\"level\":\"2\",\"id\":\"aTD520000008ONoGAM\",\"icon\":null,\"href\":\"https://beta-content.workday.com/en-us/feature-release/feature-release-guidebook.html\",\"description\":\"Frameworkforcustomerstouseforourreleasecycle\",\"children\":[]},{\"type\":\"Primary\",\"tooltip\":\"ScheduledMaintenance\",\"title\":\"ScheduledMaintenance\",\"parent\":\"aTD520000008ONnGAM\",\"order\":20,\"level\":\"2\",\"id\":\"aTD520000008ONpGAM\",\"icon\":null,\"href\":\"https://beta-content.workday.com/en-us/feature-release/scheduled-maintenance.html\",\"description\":\"PlannedMaintenancewindows\",\"children\":[]},{\"type\":\"Primary\",\"tooltip\":\"ReleaseCenter\",\"title\":\"ReleaseCenter\",\"parent\":\"aTD520000008ONnGAM\",\"order\":30,\"level\":\"2\",\"id\":\"aTD520000008ONqGAM\",\"icon\":null,\"href\":\"https://beta-content.workday.com/en-us/feature-release/release-center.html\",\"description\":\"TheReleaseCenterorganizesreleasedetailsforthemostrecentfeaturerelease.TheReleasePrepCenterreplacesitforthe6weeksapproachinganewrelease,andfeaturesthenextrelease.\",\"children\":[]},{\"type\":\"Primary\",\"tooltip\":\"ReleaseNotes\",\"title\":\"ReleaseNotes\",\"parent\":\"aTD520000008ONnGAM\",\"order\":40,\"level\":\"2\",\"id\":\"aTD520000008ONrGAM\",\"icon\":null,\"href\":\"https://beta-content.workday.com/en-us/feature-release/release-center/release-notes.html\",\"description\":\"Comprehensivereleasenotes\",\"children\":[]}]},{\"type\":\"Primary\",\"tooltip\":\"GetHelpmenu\",\"title\":\"GetHelp\",\"parent\":null,\"order\":50,\"level\":\"1\",\"id\":\"aTD520000008OOEGA2\",\"icon\":null,\"href\":null,\"description\":\"Self-serviceandsolutionfindingresources\",\"children\":[{\"type\":\"Primary\",\"tooltip\":\"DatacenterStatus\",\"title\":\"DatacenterStatus\",\"parent\":\"aTD520000008OOEGA2\",\"order\":10,\"level\":\"2\",\"id\":\"aTD520000008OOFGA2\",\"icon\":null,\"href\":\"https://preprod.community-workday.com/get-help/datacenter-status\",\"description\":\"SummaryonthecurrenthealthstatusofWorkdaydatacenters\",\"children\":[]},{\"type\":\"Primary\",\"tooltip\":\"TroubleshootingResources\",\"title\":\"TroubleshootingResources\",\"parent\":\"aTD520000008OOEGA2\",\"order\":20,\"level\":\"2\",\"id\":\"aTD520000008OOGGA2\",\"icon\":null,\"href\":\"https://beta-content.workday.com/en-us/get-help/troubleshooting.html\",\"description\":\"Organizedlistofresourcesandlinkstohelptroubleshooting\",\"children\":[]},{\"type\":\"Primary\",\"tooltip\":\"CustomerAlerts\",\"title\":\"CustomerAlerts\",\"parent\":\"aTD520000008OOEGA2\",\"order\":30,\"level\":\"2\",\"id\":\"aTD520000008OOHGA2\",\"icon\":null,\"href\":\"https://preprod.community-workday.com/get-help/customer-alerts\",\"description\":\"Supportalertstocustomersrelatedtooperations,defectsandotherproduct-specificinformation\",\"children\":[]},{\"type\":\"Primary\",\"tooltip\":\"ServicesAlerts\",\"title\":\"ServicesAlerts\",\"parent\":\"aTD520000008OOEGA2\",\"order\":40,\"level\":\"2\",\"id\":\"aTD520000008OOIGA2\",\"icon\":null,\"href\":\"https://preprod.community-workday.com/get-help/services-alerts\",\"description\":\"Supportalertstostaffandpartnersrelatedtooperations,defectsandotherproduct-specificinformation\",\"children\":[]},{\"type\":\"Primary\",\"tooltip\":\"TrainingCoordinatorHub\",\"title\":\"TrainingCoordinatorHub\",\"parent\":\"aTD520000008OOEGA2\",\"order\":60,\"level\":\"2\",\"id\":\"aTD520000008OOJGA2\",\"icon\":null,\"href\":\"https://beta-content.workday.com/en-us/get-help/training-coordinators.html\",\"description\":\"TrainingandresourcesforusersintheTrainingCoordinatorWorkdaySupportRole\",\"children\":[]},{\"type\":\"Primary\",\"tooltip\":\"NSCHub\",\"title\":\"NSCHub\",\"parent\":\"aTD520000008OOEGA2\",\"order\":70,\"level\":\"2\",\"id\":\"aTD520000008OOKGA2\",\"icon\":null,\"href\":\"https://beta-content.workday.com/en-us/get-help/support/nscs.html\",\"description\":\"TrainingandresourcesforusersintheNamedSupportContactWorkdaySupportrole\",\"children\":[]}]},{\"type\":\"Primary\",\"tooltip\":\"Managemenu\",\"title\":\"Manage\",\"parent\":null,\"order\":60,\"level\":\"1\",\"id\":\"aTD520000008OO0GAM\",\"icon\":null,\"href\":null,\"description\":\"SupportandWorkdayRoleAdministrativeFunctions\",\"children\":[{\"type\":\"Primary\",\"tooltip\":\"Cases\",\"title\":\"Cases\",\"parent\":\"aTD520000008OO0GAM\",\"order\":10,\"level\":\"2\",\"id\":\"aTD520000008OO1GAM\",\"icon\":null,\"href\":\"https://workday--uat.sandbox.my.site.com/workdaycustomercenter/apex/CC_CommunityCases\",\"description\":\"Customercaseportal\",\"children\":[]},{\"type\":\"Primary\",\"tooltip\":\"Users\",\"title\":\"Users\",\"parent\":\"aTD520000008OO0GAM\",\"order\":20,\"level\":\"2\",\"id\":\"aTD520000008OO2GAM\",\"icon\":null,\"href\":\"https://workday--uat.sandbox.my.site.com/CommunityAccess/s/user-management\",\"description\":\"OrgAdministrationandCustomerCenterContactmanagement\",\"children\":[]},{\"type\":\"Primary\",\"tooltip\":null,\"title\":\"ContactsManagement\",\"parent\":\"aTD520000008OO0GAM\",\"order\":30,\"level\":\"2\",\"id\":\"aTD520000008OO3GAM\",\"icon\":null,\"href\":\"https://workday--uat.sandbox.my.site.com/workdaycustomercenter/apex/CC_NSCAdministration\",\"description\":\"Supportcontactmangement\",\"children\":[]},{\"type\":\"Primary\",\"tooltip\":\"Training\",\"title\":\"Training\",\"parent\":\"aTD520000008OO0GAM\",\"order\":40,\"level\":\"2\",\"id\":\"aTD520000008OO4GAM\",\"icon\":null,\"href\":\"https://workday--uat.sandbox.my.site.com/workdaycustomercenter/apex/LMS_TrainingUnits\",\"description\":\"Trainingadministrationtasks\",\"children\":[]},{\"type\":\"Primary\",\"tooltip\":\"Tenants\",\"title\":\"Tenants\",\"parent\":\"aTD520000008OO0GAM\",\"order\":50,\"level\":\"2\",\"id\":\"aTD520000008OO5GAM\",\"icon\":null,\"href\":\"https://workday--uat.sandbox.my.site.com/workdaycustomercenter/apex/CC_CommunityTenants\",\"description\":\"Tenantrequestandmanagement\",\"children\":[]},{\"type\":\"Primary\",\"tooltip\":\"MyOrganization\",\"title\":\"MyOrganization\",\"parent\":\"aTD520000008OO0GAM\",\"order\":60,\"level\":\"2\",\"id\":\"aTD520000008OO6GAM\",\"icon\":null,\"href\":\"https://workday--uat.sandbox.my.site.com/CommunityAccess/s/\",\"description\":\"Organizationprofilemanagement\",\"children\":[]},{\"type\":\"Primary\",\"tooltip\":\"Reports\",\"title\":\"Reports\",\"parent\":\"aTD520000008OO0GAM\",\"order\":70,\"level\":\"2\",\"id\":\"aTD520000008OOYGA2\",\"icon\":null,\"href\":\"https://workday--uat.sandbox.my.site.com/workdaycustomercenter/00O/o\",\"description\":\"Supportorientedreports\",\"children\":[]},{\"type\":\"Primary\",\"tooltip\":\"Reports\",\"title\":\"ContentLibrary\",\"parent\":\"aTD520000008OO0GAM\",\"order\":80,\"level\":\"2\",\"id\":\"aTD520000008OO7GAM\",\"icon\":null,\"href\":\"https://workday--uat.sandbox.my.site.com/workdaycustomercenter/apex/CC_ListContent\",\"description\":\"Supportorientedreportsincludingauditandcompliance\",\"children\":[]}]}]}}";
        Gson gson = new Gson();
        JsonObject source = gson.fromJson(sourceString, JsonObject.class);
        JsonObject target = gson.fromJson(targetString, JsonObject.class);
        assertEquals(-1, gson.toJson(source).indexOf("beta-content"));
        CommonUtils.updateSourceFromTarget(source, target, "id", "beta");
        assertEquals(353, gson.toJson(source).indexOf("beta-content"));

        JsonObject source1 = gson.fromJson(sourceString, JsonObject.class);
        JsonObject target1 = gson.fromJson(targetString, JsonObject.class);
        CommonUtils.updateSourceFromTarget(source1, target1, "id", "dev");
        assertEquals(353, gson.toJson(source1).indexOf("dev-content"));

        JsonObject source2 = gson.fromJson(sourceString, JsonObject.class);
        JsonObject target2 = gson.fromJson(targetString, JsonObject.class);
        CommonUtils.updateSourceFromTarget(source2, target2, "id", "prod");
        assertEquals(-1, gson.toJson(source2).indexOf("community-content"));
    }
}
