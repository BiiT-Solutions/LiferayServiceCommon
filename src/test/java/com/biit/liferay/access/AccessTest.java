package com.biit.liferay.access;

import java.io.IOException;
import java.util.Set;

import org.apache.http.client.ClientProtocolException;
import org.junit.Assert;
import org.testng.annotations.AfterTest;
import org.testng.annotations.Test;

import com.biit.liferay.access.exceptions.DuplicatedLiferayElement;
import com.biit.liferay.access.exceptions.NotConnectedToWebServiceException;
import com.biit.liferay.access.exceptions.OrganizationNotDeletedException;
import com.biit.liferay.access.exceptions.PortletNotInstalledException;
import com.biit.liferay.access.exceptions.UserDoesNotExistException;
import com.biit.liferay.access.exceptions.WebServiceAccessError;
import com.biit.usermanager.entity.IElement;
import com.biit.usermanager.entity.IGroup;
import com.biit.usermanager.entity.IUser;
import com.biit.usermanager.security.exceptions.AuthenticationRequired;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.liferay.portal.model.Company;
import com.liferay.portal.model.Contact;
import com.liferay.portal.model.Organization;
import com.liferay.portal.model.Site;
import com.liferay.portal.model.User;
import com.liferay.portal.model.UserGroup;

public class AccessTest {
	private final static String LOGIN_USER = "devel@biit-solutions.com";
	private final static String LOGIN_PASSWORD = "^KOEySk!fRJ45";
	private final static String HOST = "liferay.biit-solutions.com";

	private final static String LIFERAY_PROTOCOL = "https";
	private final static int PORT = 9443;
	private final static String WEBSERVICES_PATH = "api/jsonws/";
	private final static String AUTHENTICATION_TOKEN = "11111111";
	private final static String COMPANY_VIRTUALHOST = "liferay.biit-solutions.com";

	private final static String TEST_USER = "newTestUser";
	private final static String TEST_USER_MAIL = TEST_USER + "@dummyemail.com";
	private final static String TEST_USER_PASSWORD = "asd123";
	private final static int TEST_USER_BIRTHDAY_DAY = 10;
	private final static int TEST_USER_BIRTHDAY_MONTH = 10;
	private final static int TEST_USER_BIRTHDAY_YEAR = 1975;

	private final static String TEST_USER_EDIT_NAME = "editedTestUser";
	private final static String TEST_USER_EDIT_SURNAME = "Surname";
	private final static String TEST_USER_EDIT_LANGUAGE = "nl_NL";

	private final static String SITE_NAME = "testSite";
	private final static String SITE_DESCRIPTION = "This site is created with the automated Shap testing.";
	private final static String SITE_URL = "/test-site";

	private final static String TEST_GROUP = "TestGroup1";

	private final static String TEST_ORGANIZATION_1 = "TestOrganization1";
	private final static String TEST_ORGANIZATION_2 = "TestOrganization2";

	private Company company;
	private User user;
	private UserGroup group;
	private Organization organization1, organization2;
	private Site site;

	private CompanyService companyService = new CompanyService();
	private SiteService siteService = new SiteService();
	private UserService userService = new UserService();
	private ContactService contactService = new ContactService();
	private UserGroupService userGroupService = new UserGroupService();
	private GroupService groupService = new GroupService();
	private OrganizationService organizationService = new OrganizationService();
	private ClassNameService classNameService = new ClassNameService();

	@Test(groups = { "connection" }, expectedExceptions = AuthenticationRequired.class)
	public void notAuthorized() throws NotConnectedToWebServiceException, JsonParseException, JsonMappingException, IOException, AuthenticationRequired,
			WebServiceAccessError {
		companyService.authorizedServerConnection(HOST, LIFERAY_PROTOCOL, PORT, WEBSERVICES_PATH, AUTHENTICATION_TOKEN, LOGIN_USER + "failure", LOGIN_PASSWORD
				+ "failure");
		try {
			companyService.getCompanyByVirtualHost(COMPANY_VIRTUALHOST + "_failure");
		} catch (NotConnectedToWebServiceException ncws) {
			// From Liferay 6.1
			// Assert.assertTrue(af.getFaultReason().contains("(401)"));
			// From Liferay 6.2
			// Assert.assertTrue(af.getFaultReason().contains("Authenticated access required"));
			// JSON
			Assert.assertTrue(ncws.getMessage().contains("Incorrect credentials for accessing to the webservices"));
			throw ncws;
		}
	}

	@Test(groups = { "connection" }, dependsOnMethods = { "notAuthorized" })
	public void authorized() throws NotConnectedToWebServiceException, JsonParseException, JsonMappingException, IOException, AuthenticationRequired,
			WebServiceAccessError {
		// companyService.connectToWebService(LOGIN_USER,
		// LOGIN_PASSWORD);
		companyService.authorizedServerConnection(HOST, LIFERAY_PROTOCOL, PORT, WEBSERVICES_PATH, AUTHENTICATION_TOKEN, LOGIN_USER, LOGIN_PASSWORD);
		companyService.getCompanyByVirtualHost(COMPANY_VIRTUALHOST);
	}

	@Test(groups = { "className" }, dependsOnGroups = { "connection" })
	public void classNameAccess() throws NotConnectedToWebServiceException, JsonParseException, JsonMappingException, IOException, AuthenticationRequired,
			WebServiceAccessError {
		classNameService.authorizedServerConnection(HOST, LIFERAY_PROTOCOL, PORT, WEBSERVICES_PATH, AUTHENTICATION_TOKEN, LOGIN_USER, LOGIN_PASSWORD);
		IElement<Long> className = classNameService.getClassName("com.liferay.knowledgebase.model.KBFolder");
		Assert.assertNotNull(className);
		Assert.assertNotNull(className.getId());
	}

	@Test(groups = { "companyAccess" }, dependsOnGroups = { "connection" }, expectedExceptions = WebServiceAccessError.class)
	public void companyAccessFailure() throws NotConnectedToWebServiceException, JsonParseException, JsonMappingException, IOException, AuthenticationRequired,
			WebServiceAccessError {
		companyService.getCompanyByVirtualHost(COMPANY_VIRTUALHOST + "_failure");
	}

	@Test(groups = { "companyAccess" }, dependsOnGroups = { "connection" })
	public void companyAccess() throws NotConnectedToWebServiceException, JsonParseException, JsonMappingException, IOException, AuthenticationRequired,
			WebServiceAccessError {
		company = (Company) companyService.getCompanyByVirtualHost(COMPANY_VIRTUALHOST);
		Assert.assertNotNull(company);
	}

	@Test(groups = { "siteAccess" }, dependsOnGroups = { "connection" })
	public void siteAccess() throws NotConnectedToWebServiceException, JsonParseException, JsonMappingException, IOException, AuthenticationRequired,
			WebServiceAccessError {
		siteService.authorizedServerConnection(HOST, LIFERAY_PROTOCOL, PORT, WEBSERVICES_PATH, AUTHENTICATION_TOKEN, LOGIN_USER, LOGIN_PASSWORD);
		Assert.assertFalse(siteService.isNotConnected());
	}

	@Test(groups = { "siteAccess" }, dependsOnMethods = { "siteAccess" })
	public void addSite() throws NotConnectedToWebServiceException, ClientProtocolException, IOException, AuthenticationRequired, WebServiceAccessError {
		siteService.addSite(SITE_NAME, SITE_DESCRIPTION, SiteType.DEFAULT_PARENT_GROUP_ID, SITE_URL);
	}

	@Test(groups = { "siteAccess" }, dependsOnMethods = { "addSite" })
	public void getSite() throws NotConnectedToWebServiceException, ClientProtocolException, IOException, AuthenticationRequired, WebServiceAccessError {
		site = (Site) siteService.getSiteByFriendlyUrl(company, SITE_URL);
		Assert.assertNotNull(site);
	}

	@Test(groups = { "userAccess" }, dependsOnGroups = { "companyAccess" }, expectedExceptions = NotConnectedToWebServiceException.class)
	public void notConnectedToUserWebService() throws NotConnectedToWebServiceException, ClientProtocolException, IOException, AuthenticationRequired,
			WebServiceAccessError {
		userService.addUser(new Company(), "", TEST_USER, TEST_USER, 0, "", "", "", "", "", 0, 0, true, 1, 1, 1900, "Miner", new long[0], new long[0],
				new long[0], new long[0], false);
	}

	@Test(groups = { "userAccess" }, dependsOnMethods = { "notConnectedToUserWebService" })
	public void connectToUserWebService() {
		// userService.connectToWebService(LOGIN_USER,
		// LOGIN_PASSWORD);
		userService.authorizedServerConnection(HOST, LIFERAY_PROTOCOL, PORT, WEBSERVICES_PATH, AUTHENTICATION_TOKEN, LOGIN_USER, LOGIN_PASSWORD);
		Assert.assertFalse(userService.isNotConnected());
	}

	@Test(groups = { "userAccess" }, dependsOnMethods = { "connectToUserWebService" })
	public void userAdd() throws NotConnectedToWebServiceException, ClientProtocolException, IOException, AuthenticationRequired, WebServiceAccessError {
		userService.addUser(company, TEST_USER_PASSWORD, TEST_USER, TEST_USER_MAIL, 0l, "", "es_ES", TEST_USER, TEST_USER, TEST_USER, 0, 0, true,
				TEST_USER_BIRTHDAY_DAY, TEST_USER_BIRTHDAY_MONTH, TEST_USER_BIRTHDAY_YEAR, "Miner", new long[0], new long[0], new long[0], new long[0], false);
	}

	@Test(groups = { "userAccess" }, dependsOnMethods = { "userAdd" }, dependsOnGroups = { "companyAccess" })
	public void userAccess() throws NotConnectedToWebServiceException, ClientProtocolException, IOException, AuthenticationRequired, WebServiceAccessError,
			UserDoesNotExistException {
		try {
			user = (User) userService.getUserByEmailAddress(company, TEST_USER_MAIL);
			Assert.assertNotNull(user);
			User user2 = (User) userService.getUserById(user.getId());
			Assert.assertEquals(user.getId(), user2.getId());
		} catch (WebServiceAccessError e) {
			throw new UserDoesNotExistException("User for testing does not exists. Create user for testing: " + TEST_USER_MAIL);
		}
	}

	@Test(groups = { "userAccess" }, dependsOnMethods = { "userAccess" })
	public void userEdit() throws NotConnectedToWebServiceException, ClientProtocolException, IOException, AuthenticationRequired, WebServiceAccessError {
		// Change some fields.
		String language = user.getLanguageId();

		user.setFirstName(TEST_USER_EDIT_NAME);
		user.setLastName(TEST_USER_EDIT_SURNAME);
		user.setLanguageId(TEST_USER_EDIT_LANGUAGE);
		user = (User) userService.updateUser(user);

		// Retrieve again from database to check it.
		IUser<Long> updatedUser = userService.getUserByEmailAddress(company, TEST_USER_MAIL);
		Assert.assertEquals(TEST_USER_EDIT_NAME, updatedUser.getFirstName());
		Assert.assertEquals(TEST_USER_EDIT_SURNAME, updatedUser.getLastName());
		Assert.assertEquals(TEST_USER_EDIT_LANGUAGE, updatedUser.getLanguageId());

		// Let data as it is at the start.
		user.setFirstName(TEST_USER);
		user.setLanguageId(language);
		user = (User) userService.updateUser(user);
		updatedUser = (User) userService.getUserByEmailAddress(company, TEST_USER_MAIL);
		Assert.assertEquals(TEST_USER, updatedUser.getFirstName());
	}

	@Test(groups = { "contactAccess" }, dependsOnMethods = { "notConnectedToUserWebService" })
	public void connectToContactWebService() {
		contactService.authorizedServerConnection(HOST, LIFERAY_PROTOCOL, PORT, WEBSERVICES_PATH, AUTHENTICATION_TOKEN, LOGIN_USER, LOGIN_PASSWORD);
		Assert.assertFalse(contactService.isNotConnected());
	}

	@Test(groups = { "contactAccess" }, dependsOnMethods = { "userAccess", "connectToContactWebService" })
	public void getUserBirthday() throws ClientProtocolException, NotConnectedToWebServiceException, IOException, AuthenticationRequired, WebServiceAccessError {
		Contact contact = contactService.getContact(user);
		Assert.assertNotNull(contact);
		Assert.assertEquals(TEST_USER_BIRTHDAY_YEAR, contact.getBirthdayYear());
		Assert.assertEquals(TEST_USER_BIRTHDAY_MONTH, contact.getBirthdayMonth());
		Assert.assertEquals(TEST_USER_BIRTHDAY_DAY, contact.getBirthdayDay());
	}

	@Test(groups = { "groupAccess" })
	public void connectToGroupWebService() {
		// userGroupService.connectToWebService(LOGIN_USER,
		// LOGIN_PASSWORD);
		userGroupService.authorizedServerConnection(HOST, LIFERAY_PROTOCOL, PORT, WEBSERVICES_PATH, AUTHENTICATION_TOKEN, LOGIN_USER, LOGIN_PASSWORD);
		Assert.assertFalse(userGroupService.isNotConnected());
	}

	@Test(groups = { "groupAccess" }, dependsOnMethods = { "connectToGroupWebService" })
	public void groupAdd() throws NotConnectedToWebServiceException, ClientProtocolException, IOException, AuthenticationRequired, WebServiceAccessError,
			DuplicatedLiferayElement {
		group = (UserGroup) userGroupService.addUserGroup(TEST_GROUP, "");
		Assert.assertNotNull(group);
	}

	@Test(groups = { "groupAccess" }, dependsOnMethods = { "groupAdd" }, expectedExceptions = { DuplicatedLiferayElement.class })
	public void groupDuplicated() throws NotConnectedToWebServiceException, ClientProtocolException, IOException, AuthenticationRequired,
			WebServiceAccessError, DuplicatedLiferayElement {
		group = (UserGroup) userGroupService.addUserGroup(TEST_GROUP, "");
	}

	@Test(groups = { "groupAccess" }, dependsOnMethods = { "userEdit", "groupAdd" })
	public void groupUserAdd() throws NotConnectedToWebServiceException, ClientProtocolException, IOException, AuthenticationRequired {
		int prevGroups = userGroupService.getUserUserGroups(user).size();
		userGroupService.addUserToGroup(user, group);
		Assert.assertEquals(prevGroups + 1, userGroupService.getUserUserGroups(user).size());
	}

	@Test(groups = { "organizationAccess" }, dependsOnMethods = { "notConnectedToUserWebService" })
	public void connectToOrganizationWebService() {
		organizationService.authorizedServerConnection(HOST, LIFERAY_PROTOCOL, PORT, WEBSERVICES_PATH, AUTHENTICATION_TOKEN, LOGIN_USER, LOGIN_PASSWORD);
		Assert.assertFalse(organizationService.isNotConnected());
	}

	@Test(groups = { "organizationAccess" }, dependsOnGroups = { "companyAccess" }, dependsOnMethods = { "connectToOrganizationWebService" })
	public void addOrganization() throws ClientProtocolException, NotConnectedToWebServiceException, IOException, AuthenticationRequired,
			WebServiceAccessError, DuplicatedLiferayElement {
		// Check previous organization.
		int previousOrganizations = organizationService.getOrganizations(company).size();
		// Create two organizations.
		organization1 = (Organization) organizationService.addOrganization(company, TEST_ORGANIZATION_1);
		organization2 = (Organization) organizationService.addOrganization(company, TEST_ORGANIZATION_2);
		Assert.assertNotNull(organization1);
		Assert.assertNotNull(organization2);
		// Test the number has increased.
		Assert.assertTrue(organizationService.getOrganizations(company).size() == previousOrganizations + 2);
	}

	@Test(groups = { "groupAccess" }, dependsOnMethods = { "addOrganization" }, expectedExceptions = { DuplicatedLiferayElement.class })
	public void organizationDuplicated() throws NotConnectedToWebServiceException, ClientProtocolException, IOException, AuthenticationRequired,
			WebServiceAccessError, DuplicatedLiferayElement {
		organization1 = (Organization) organizationService.addOrganization(company, TEST_ORGANIZATION_1);
	}

	@Test(groups = { "organizationAccess" }, dependsOnMethods = { "addOrganization" })
	public void getOrganizationById() throws ClientProtocolException, NotConnectedToWebServiceException, IOException, AuthenticationRequired,
			WebServiceAccessError {
		Organization organization = (Organization) organizationService.getOrganization(organization1.getOrganizationId());
		Assert.assertEquals(organization1.getName(), organization.getName());
	}

	@Test(groups = { "organizationAccess" }, dependsOnMethods = { "addOrganization", "userAccess" })
	public void assignUsersToOrganizations() throws ClientProtocolException, NotConnectedToWebServiceException, IOException, AuthenticationRequired,
			WebServiceAccessError {
		organizationService.addUserToOrganization(user, organization1);
		Assert.assertEquals(1, organizationService.getUserOrganizationGroups(user).size());
		Assert.assertEquals(1, organizationService.getUserOrganizations(user).size());
		organizationService.addUserToOrganization(user, organization2);
		Assert.assertEquals(2, organizationService.getUserOrganizationGroups(user).size());
		Assert.assertEquals(2, organizationService.getUserOrganizations(user).size());
	}

	@Test(groups = { "organizationAccess" }, dependsOnMethods = { "assignUsersToOrganizations", "userAccess" })
	public void checkUsersOfOrganization() throws ClientProtocolException, NotConnectedToWebServiceException, IOException, AuthenticationRequired,
			WebServiceAccessError {
		Set<IUser<Long>> organization1Users = organizationService.getOrganizationUsers(organization1);

		IUser<Long> organizationUser = null;
		for (IUser<Long> tempUser : organization1Users) {
			if (tempUser.getId().equals(user.getId())) {
				organizationUser = tempUser;
				break;
			}
		}
		Assert.assertEquals(1, organization1Users.size());
		Assert.assertNotNull(organizationUser);

	}

	@Test(groups = { "organizationAccess" }, dependsOnMethods = { "assignUsersToOrganizations", "userAccess" })
	public void checkOrganizationsOfUser() throws ClientProtocolException, NotConnectedToWebServiceException, IOException, AuthenticationRequired,
			WebServiceAccessError {
		Set<IGroup<Long>> organizationsOfUser = organizationService.getUserOrganizations(user);
		Assert.assertTrue(organizationsOfUser.contains(organization1));
		Assert.assertTrue(organizationsOfUser.contains(organization2));
		Assert.assertEquals(2, organizationsOfUser.size());
	}

	@Test(groups = { "pool" }, dependsOnGroups = { "userAccess" })
	public void userAccessPool() throws NotConnectedToWebServiceException, ClientProtocolException, IOException, AuthenticationRequired, WebServiceAccessError {
		// Make a connection to populate the pool.
		user = (User) userService.getUserByEmailAddress(company, TEST_USER_MAIL);
		// Checks the use of the pool. Disconnect the web service.
		userService.disconnect();
		// I can still get the UserSoap (is stored previously in the pool)
		user = (User) userService.getUserByEmailAddress(company, TEST_USER_MAIL);
		Assert.assertNotNull(user);
		// Connect again.
		connectToUserWebService();
	}

	@Test(groups = { "pool" }, dependsOnGroups = { "userAccess", "groupAccess" })
	public void groupAccessPool() throws NotConnectedToWebServiceException, ClientProtocolException, IOException, AuthenticationRequired {
		// Make a connection to populate the pool.
		Set<IGroup<Long>> groups = userGroupService.getUserUserGroups(user);
		// Checks the use of the pool. Disconnect the web service.
		userGroupService.disconnect();
		// I can still get the group (is stored previously in the pool)
		groups = userGroupService.getUserUserGroups(user);
		Assert.assertNotNull(groups);
		Assert.assertFalse(groups.isEmpty());
		// Connect again.
		connectToGroupWebService();
	}

	@Test(groups = { "pool" }, dependsOnGroups = { "organizationAccess" })
	public void organizationsAccessPool() throws NotConnectedToWebServiceException, ClientProtocolException, IOException, AuthenticationRequired,
			WebServiceAccessError {
		// Make a connection for populating the pool.
		Set<IGroup<Long>> organizations = organizationService.getOrganizations(company);
		// Checks the use of the pool. Disconnect the web service.
		organizationService.disconnect();
		// I can still get the organization (is stored previously in the pool)
		organizations = organizationService.getOrganizations(company);
		Assert.assertNotNull(organizations);
		Assert.assertFalse(organizations.isEmpty());
		// Get organization byid
		IGroup<Long> organization = organizationService.getOrganization(organization1.getOrganizationId());
		Assert.assertEquals(organization1.getName(), organization.getUniqueName());
		// Connect again.
		connectToOrganizationWebService();
	}

	@Test(groups = { "organizationAccess" }, dependsOnGroups = { "userAccess", "siteAccess" }, dependsOnMethods = { "addOrganization" })
	public void setOrganizationsBySiteAndUser() throws NotConnectedToWebServiceException, ClientProtocolException, IOException, AuthenticationRequired {
		// Make a connection for populating the pool.
		Assert.assertTrue(organizationService.addOrganization(site, user, organization1));
		Assert.assertTrue(organizationService.addOrganization(site, user, organization2));
	}

	@Test(groups = { "pool" }, dependsOnMethods = { "setOrganizationsBySiteAndUser" })
	public void getOrganizationsByUserPool() throws NotConnectedToWebServiceException, ClientProtocolException, IOException, AuthenticationRequired,
			PortletNotInstalledException, WebServiceAccessError {
		// Make a connection for populating the pool.
		Set<IGroup<Long>> organizations = organizationService.getUserOrganizations(user);
		Assert.assertNotNull(organizations);
		Assert.assertFalse(organizations.isEmpty());
		// Checks the use of the pool. Disconnect the web service.
		organizationService.disconnect();
		// I can still get the organization (is stored previously in the pool)
		organizations = organizationService.getUserOrganizations(user);
		Assert.assertNotNull(organizations);
		Assert.assertFalse(organizations.isEmpty());
		// Connect again.
		connectToOrganizationWebService();
	}

	@Test(groups = { "pool" }, dependsOnMethods = { "setOrganizationsBySiteAndUser" })
	public void getOrganizationsBySiteAndUserPool() throws NotConnectedToWebServiceException, ClientProtocolException, IOException, AuthenticationRequired,
			PortletNotInstalledException {
		// Make a connection for populating the pool.
		Set<IGroup<Long>> organizations = organizationService.getOrganizations(site, user);
		Assert.assertNotNull(organizations);
		Assert.assertFalse(organizations.isEmpty());
		// Checks the use of the pool. Disconnect the web service.
		organizationService.disconnect();
		// I can still get the organization (is stored previously in the pool)
		organizations = organizationService.getOrganizations(site, user);
		Assert.assertNotNull(organizations);
		Assert.assertFalse(organizations.isEmpty());
		// Connect again.
		connectToOrganizationWebService();
	}

	@Test(alwaysRun = true, groups = { "clearData" }, dependsOnGroups = { "groupAccess", "userAccess", "pool" }, dependsOnMethods = { "groupAdd" })
	public void groupDelete() throws NotConnectedToWebServiceException, ClientProtocolException, IOException, AuthenticationRequired {
		int prevGroups = userGroupService.getUserUserGroups(user).size();
		userGroupService.deleteUserFromUserGroup(user, group);
		userGroupService.deleteUserGroup(group);
		Assert.assertEquals(prevGroups - 1, userGroupService.getUserUserGroups(user).size());
	}

	@Test(alwaysRun = true, groups = { "clearData" }, dependsOnGroups = { "groupAccess", "userAccess" }, dependsOnMethods = { "userEdit", "addOrganization" })
	public void unsetUserFromOrganization() throws NotConnectedToWebServiceException, ClientProtocolException, IOException, AuthenticationRequired {
		int usersInOrg1 = organizationService.getOrganizationUsers(organization1).size();
		int usersInOrg2 = organizationService.getOrganizationUsers(organization2).size();
		organizationService.removeUserFromOrganization(user, organization1);
		organizationService.removeUserFromOrganization(user, organization2);
		Assert.assertEquals(usersInOrg1 - 1, organizationService.getOrganizationUsers(organization1).size());
		Assert.assertEquals(usersInOrg2 - 1, organizationService.getOrganizationUsers(organization2).size());
	}

	@Test(alwaysRun = true, groups = { "clearData" }, dependsOnMethods = { "unsetUserFromOrganization" })
	public void organizationDelete() throws NotConnectedToWebServiceException, ClientProtocolException, IOException, AuthenticationRequired,
			OrganizationNotDeletedException {
		int prevOrgs = organizationService.getOrganizations(company).size();
		organizationService.deleteOrganization(company, organization1);
		organizationService.deleteOrganization(company, organization2);
		Assert.assertEquals(prevOrgs - 2, organizationService.getOrganizations(company).size());
	}

	@Test(alwaysRun = true, groups = { "clearData" }, dependsOnGroups = { "userAccess", "groupAccess", "contactAccess", "pool" }, dependsOnMethods = {
			"userEdit", "userAccess", "groupDelete" }, expectedExceptions = WebServiceAccessError.class)
	public void userDelete() throws NotConnectedToWebServiceException, UserDoesNotExistException, ClientProtocolException, IOException, AuthenticationRequired,
			WebServiceAccessError {
		userService.deleteUser(user);
		userService.getUserById(user.getId());
	}

	@Test(alwaysRun = true, groups = { "clearData" }, dependsOnGroups = { "userAccess", "groupAccess", "contactAccess", "pool" })
	public void siteDelete() throws NotConnectedToWebServiceException, ClientProtocolException, IOException, AuthenticationRequired, WebServiceAccessError {
		Assert.assertTrue(siteService.deleteSite(site));
	}

	@AfterTest(groups = { "pool", "clearData", "organizationAccess", "connection", "companyAccess", "userAccess", "contactAccess", "groupAccess" })
	public void closeConnections() {
		organizationService.disconnect();
		userService.disconnect();
		userGroupService.disconnect();
		groupService.disconnect();
		contactService.disconnect();
		companyService.disconnect();
		siteService.disconnect();
	}
}
