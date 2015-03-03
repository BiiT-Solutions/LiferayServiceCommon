package com.biit.liferay.access;

import java.io.IOException;
import java.util.List;

import org.apache.http.client.ClientProtocolException;
import org.junit.After;
import org.junit.Assert;
import org.testng.annotations.Test;

import com.biit.liferay.access.exceptions.AuthenticationRequired;
import com.biit.liferay.access.exceptions.DuplicatedLiferayElement;
import com.biit.liferay.access.exceptions.NotConnectedToWebServiceException;
import com.biit.liferay.access.exceptions.OrganizationNotDeletedException;
import com.biit.liferay.access.exceptions.PortletNotInstalledException;
import com.biit.liferay.access.exceptions.UserDoesNotExistException;
import com.biit.liferay.access.exceptions.WebServiceAccessError;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.liferay.portal.model.Company;
import com.liferay.portal.model.Contact;
import com.liferay.portal.model.Organization;
import com.liferay.portal.model.Site;
import com.liferay.portal.model.User;
import com.liferay.portal.model.UserGroup;

public class AccessTest {
	private final static String LOGIN_USER = "test@liferay.com";
	private final static String LOGIN_PASSWORD = "extintor";
	private final static String HOST = "liferay.biit-solutions.com";

	private final String LIFERAY_PROTOCOL = "https";
	private final int PORT = 9443;
	private final String WEBSERVICES_PATH = "api/jsonws/";
	private final String AUTHENTICATION_TOKEN = "11111111";
	private final String COMPANY_VIRTUALHOST = "liferay.biit-solutions.com";

	private final static String TEST_USER = "newTestUser";
	private final static String TEST_USER_MAIL = TEST_USER + "@dummyemail.com";
	private final static String TEST_USER_PASSWORD = "asd123";
	private final static int TEST_USER_BIRTHDAY_DAY = 10;
	private final static int TEST_USER_BIRTHDAY_MONTH = 10;
	private final static int TEST_USER_BIRTHDAY_YEAR = 1975;

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

	@Test(groups = { "connection" }, expectedExceptions = AuthenticationRequired.class)
	public void notAuthorized() throws NotConnectedToWebServiceException, JsonParseException, JsonMappingException,
			IOException, AuthenticationRequired, WebServiceAccessError {
		companyService.authorizedServerConnection(HOST, LIFERAY_PROTOCOL, PORT, WEBSERVICES_PATH, AUTHENTICATION_TOKEN,
				LOGIN_USER + "failure", LOGIN_PASSWORD + "failure");
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
	public void authorized() throws NotConnectedToWebServiceException, JsonParseException, JsonMappingException,
			IOException, AuthenticationRequired, WebServiceAccessError {
		// companyService.connectToWebService(LOGIN_USER,
		// LOGIN_PASSWORD);
		companyService.authorizedServerConnection(HOST, LIFERAY_PROTOCOL, PORT, WEBSERVICES_PATH, AUTHENTICATION_TOKEN,
				LOGIN_USER, LOGIN_PASSWORD);
		companyService.getCompanyByVirtualHost(COMPANY_VIRTUALHOST);
	}

	@Test(groups = { "companyAccess" }, dependsOnGroups = { "connection" }, expectedExceptions = WebServiceAccessError.class)
	public void companyAccessFailure() throws NotConnectedToWebServiceException, JsonParseException,
			JsonMappingException, IOException, AuthenticationRequired, WebServiceAccessError {
		companyService.getCompanyByVirtualHost(COMPANY_VIRTUALHOST + "_failure");
	}

	@Test(groups = { "companyAccess" }, dependsOnGroups = { "connection" })
	public void companyAccess() throws NotConnectedToWebServiceException, JsonParseException, JsonMappingException,
			IOException, AuthenticationRequired, WebServiceAccessError {
		company = companyService.getCompanyByVirtualHost(COMPANY_VIRTUALHOST);
		Assert.assertNotNull(company);
	}

	@Test(groups = { "siteAccess" }, dependsOnGroups = { "connection" })
	public void siteAccess() throws NotConnectedToWebServiceException, JsonParseException, JsonMappingException,
			IOException, AuthenticationRequired, WebServiceAccessError {
		siteService.authorizedServerConnection(HOST, LIFERAY_PROTOCOL, PORT, WEBSERVICES_PATH, AUTHENTICATION_TOKEN,
				LOGIN_USER, LOGIN_PASSWORD);
		Assert.assertFalse(siteService.isNotConnected());
	}

	@Test(groups = { "siteAccess" }, dependsOnMethods = { "siteAccess" })
	public void addSite() throws NotConnectedToWebServiceException, ClientProtocolException, IOException,
			AuthenticationRequired, WebServiceAccessError {
		siteService.addSite(SITE_NAME, SITE_DESCRIPTION, 0, SITE_URL);
	}

	@Test(groups = { "siteAccess" }, dependsOnMethods = { "addSite" })
	public void getSite() throws NotConnectedToWebServiceException, ClientProtocolException, IOException,
			AuthenticationRequired, WebServiceAccessError {
		site = siteService.getSiteByFriendlyUrl(company, SITE_URL);
		Assert.assertNotNull(site);
	}

	@Test(groups = { "userAccess" }, dependsOnGroups = { "companyAccess" }, expectedExceptions = NotConnectedToWebServiceException.class)
	public void notConnectedToUserWebService() throws NotConnectedToWebServiceException, ClientProtocolException,
			IOException, AuthenticationRequired, WebServiceAccessError {
		userService.addUser(new Company(), "", TEST_USER, TEST_USER, 0, "", "", "", "", "", 0, 0, true, 1, 1, 1900, "Miner",
				new long[0], new long[0], new long[0], new long[0], false);
	}

	@Test(groups = { "userAccess" }, dependsOnMethods = { "notConnectedToUserWebService" })
	public void connectToUserWebService() {
		// userService.connectToWebService(LOGIN_USER,
		// LOGIN_PASSWORD);
		userService.authorizedServerConnection(HOST, LIFERAY_PROTOCOL, PORT, WEBSERVICES_PATH, AUTHENTICATION_TOKEN,
				LOGIN_USER, LOGIN_PASSWORD);
		Assert.assertFalse(userService.isNotConnected());
	}

	@Test(groups = { "userAccess" }, dependsOnMethods = { "connectToUserWebService" })
	public void userAdd() throws NotConnectedToWebServiceException, ClientProtocolException, IOException,
			AuthenticationRequired, WebServiceAccessError {
		userService.addUser(company, TEST_USER_PASSWORD, TEST_USER, TEST_USER_MAIL, 0l, "", "es_ES", TEST_USER,
				TEST_USER, TEST_USER, 0, 0, true, TEST_USER_BIRTHDAY_DAY, TEST_USER_BIRTHDAY_MONTH,
				TEST_USER_BIRTHDAY_YEAR, "Miner", new long[0], new long[0], new long[0], new long[0], false);
	}

	@Test(groups = { "userAccess" }, dependsOnMethods = { "userAdd" }, dependsOnGroups = { "companyAccess" })
	public void userAccess() throws NotConnectedToWebServiceException, ClientProtocolException, IOException,
			AuthenticationRequired, WebServiceAccessError, UserDoesNotExistException {
		try {
			user = userService.getUserByEmailAddress(company, TEST_USER_MAIL);
			Assert.assertNotNull(user);
			User user2 = userService.getUserById(user.getUserId());
			Assert.assertEquals(user.getUserId(), user2.getUserId());
		} catch (WebServiceAccessError e) {
			throw new UserDoesNotExistException("User for testing does not exists. Create user for testing: "
					+ TEST_USER_MAIL);
		}
	}

	@Test(groups = { "contactAccess" }, dependsOnMethods = { "notConnectedToUserWebService" })
	public void connectToContactWebService() {
		contactService.authorizedServerConnection(HOST, LIFERAY_PROTOCOL, PORT, WEBSERVICES_PATH, AUTHENTICATION_TOKEN,
				LOGIN_USER, LOGIN_PASSWORD);
		Assert.assertFalse(contactService.isNotConnected());
	}

	@Test(groups = { "contactAccess" }, dependsOnMethods = { "userAccess", "connectToContactWebService" })
	public void getUserBirthday() throws ClientProtocolException, NotConnectedToWebServiceException, IOException,
			AuthenticationRequired, WebServiceAccessError {
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
		userGroupService.authorizedServerConnection(HOST, LIFERAY_PROTOCOL, PORT, WEBSERVICES_PATH,
				AUTHENTICATION_TOKEN, LOGIN_USER, LOGIN_PASSWORD);
		Assert.assertFalse(userGroupService.isNotConnected());
	}

	@Test(groups = { "groupAccess" }, dependsOnMethods = { "connectToGroupWebService" })
	public void groupAdd() throws NotConnectedToWebServiceException, ClientProtocolException, IOException,
			AuthenticationRequired, WebServiceAccessError, DuplicatedLiferayElement {
		group = userGroupService.addUserGroup(TEST_GROUP, "");
		Assert.assertNotNull(group);
	}

	@Test(groups = { "groupAccess" }, dependsOnMethods = { "groupAdd" }, expectedExceptions = { DuplicatedLiferayElement.class })
	public void groupDuplicated() throws NotConnectedToWebServiceException, ClientProtocolException, IOException,
			AuthenticationRequired, WebServiceAccessError, DuplicatedLiferayElement {
		group = userGroupService.addUserGroup(TEST_GROUP, "");
	}

	@Test(groups = { "groupAccess" }, dependsOnMethods = { "userAccess", "groupAdd" })
	public void groupUserAdd() throws NotConnectedToWebServiceException, ClientProtocolException, IOException,
			AuthenticationRequired {
		int prevGroups = userGroupService.getUserUserGroups(user).size();
		userGroupService.addUserToGroup(user, group);
		Assert.assertTrue(userGroupService.getUserUserGroups(user).size() == prevGroups + 1);
	}

	@Test(groups = { "organizationAccess" }, dependsOnMethods = { "notConnectedToUserWebService" })
	public void connectToOrganizationWebService() {
		organizationService.authorizedServerConnection(HOST, LIFERAY_PROTOCOL, PORT, WEBSERVICES_PATH,
				AUTHENTICATION_TOKEN, LOGIN_USER, LOGIN_PASSWORD);
		Assert.assertFalse(organizationService.isNotConnected());
	}

	@Test(groups = { "organizationAccess" }, dependsOnGroups = { "companyAccess" }, dependsOnMethods = { "connectToOrganizationWebService" })
	public void addOrganization() throws ClientProtocolException, NotConnectedToWebServiceException, IOException,
			AuthenticationRequired, WebServiceAccessError, DuplicatedLiferayElement {
		// Check previous organization.
		int previousOrganizations = organizationService.getOrganizations(company).size();
		// Create two organizations.
		organization1 = organizationService.addOrganization(company, TEST_ORGANIZATION_1);
		organization2 = organizationService.addOrganization(company, TEST_ORGANIZATION_2);
		Assert.assertNotNull(organization1);
		Assert.assertNotNull(organization2);
		// Test the number has increased.
		Assert.assertTrue(organizationService.getOrganizations(company).size() == previousOrganizations + 2);
	}

	@Test(groups = { "groupAccess" }, dependsOnMethods = { "addOrganization" }, expectedExceptions = { DuplicatedLiferayElement.class })
	public void organizationDuplicated() throws NotConnectedToWebServiceException, ClientProtocolException,
			IOException, AuthenticationRequired, WebServiceAccessError, DuplicatedLiferayElement {
		organization1 = organizationService.addOrganization(company, TEST_ORGANIZATION_1);
	}

	@Test(groups = { "organizationAccess" }, dependsOnMethods = { "addOrganization" })
	public void getOrganizationById() throws ClientProtocolException, NotConnectedToWebServiceException, IOException,
			AuthenticationRequired, WebServiceAccessError {
		Organization organization = organizationService.getOrganization(organization1.getOrganizationId());
		Assert.assertEquals(organization1.getName(), organization.getName());
	}

	@Test(groups = { "organizationAccess" }, dependsOnMethods = { "addOrganization", "userAccess" })
	public void assignUsersToOrganization1() throws ClientProtocolException, NotConnectedToWebServiceException,
			IOException, AuthenticationRequired, WebServiceAccessError {
		organizationService.addUserToOrganization(user, organization1);
		Assert.assertEquals(1, organizationService.getUserOrganizationGroups(user.getUserId()).size());
		Assert.assertEquals(1, organizationService.getUserOrganizations(user).size());
		organizationService.addUserToOrganization(user, organization2);
		Assert.assertEquals(2, organizationService.getUserOrganizationGroups(user.getUserId()).size());
		Assert.assertEquals(2, organizationService.getUserOrganizations(user).size());
	}

	@Test(groups = { "organizationAccess" }, dependsOnMethods = { "addOrganization", "userAccess" })
	public void checkUsersOfOrganization() throws ClientProtocolException, NotConnectedToWebServiceException,
			IOException, AuthenticationRequired, WebServiceAccessError {
		List<User> organization1Users = organizationService.getOrganizationUsers(organization1);

		User organizationUser = null;
		for (User tempUser : organization1Users) {
			if (tempUser.getUserId() == user.getUserId()) {
				organizationUser = tempUser;
			}
		}
		Assert.assertEquals(1, organization1Users.size());
		Assert.assertNotNull(organizationUser);

	}

	@Test(groups = { "organizationAccess" }, dependsOnMethods = { "addOrganization", "userAccess" })
	public void checkOrganizationsOfUser() throws ClientProtocolException, NotConnectedToWebServiceException,
			IOException, AuthenticationRequired, WebServiceAccessError {
		List<Organization> organizationsOfUser = organizationService.getUserOrganizations(user);
		Assert.assertTrue(organizationsOfUser.contains(organization1));
		Assert.assertTrue(organizationsOfUser.contains(organization2));
		Assert.assertEquals(2, organizationsOfUser.size());
	}

	@Test(groups = { "pool" }, dependsOnGroups = { "userAccess" })
	public void userAccessPool() throws NotConnectedToWebServiceException, ClientProtocolException, IOException,
			AuthenticationRequired, WebServiceAccessError {
		// Make a connection to populate the pool.
		user = userService.getUserByEmailAddress(company, TEST_USER_MAIL);
		// Checks the use of the pool. Disconnect the web service.
		userService.disconnect();
		// I can still get the UserSoap (is stored previously in the pool)
		user = userService.getUserByEmailAddress(company, TEST_USER_MAIL);
		Assert.assertNotNull(user);
		// Connect again.
		connectToUserWebService();
	}

	@Test(groups = { "pool" }, dependsOnGroups = { "userAccess", "groupAccess" })
	public void groupAccessPool() throws NotConnectedToWebServiceException, ClientProtocolException, IOException,
			AuthenticationRequired {
		// Make a connection to populate the pool.
		List<UserGroup> groups = userGroupService.getUserUserGroups(user);
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
	public void organizationsAccessPool() throws NotConnectedToWebServiceException, ClientProtocolException,
			IOException, AuthenticationRequired, WebServiceAccessError {
		// Make a connection for populating the pool.
		List<Organization> organizations = organizationService.getOrganizations(company);
		// Checks the use of the pool. Disconnect the web service.
		organizationService.disconnect();
		// I can still get the organization (is stored previously in the pool)
		organizations = organizationService.getOrganizations(company);
		Assert.assertNotNull(organizations);
		Assert.assertFalse(organizations.isEmpty());
		// Get organization byid
		Organization organization = organizationService.getOrganization(organization1.getOrganizationId());
		Assert.assertEquals(organization1.getName(), organization.getName());
		// Connect again.
		connectToOrganizationWebService();
	}

	@Test(groups = { "organizationAccess" }, dependsOnGroups = { "userAccess", "siteAccess" }, dependsOnMethods = { "addOrganization" })
	public void setOrganizationsBySiteAndUser() throws NotConnectedToWebServiceException, ClientProtocolException,
			IOException, AuthenticationRequired {
		// Make a connection for populating the pool.
		Assert.assertTrue(organizationService.addOrganization(site, user, organization1));
		Assert.assertTrue(organizationService.addOrganization(site, user, organization2));
	}

	@Test(groups = { "pool" }, dependsOnMethods = { "setOrganizationsBySiteAndUser" })
	public void getOrganizationsBySiteAndUserPool() throws NotConnectedToWebServiceException, ClientProtocolException,
			IOException, AuthenticationRequired, PortletNotInstalledException {
		// Make a connection for populating the pool.
		List<Organization> organizations = organizationService.getOrganizations(site, user);
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
	public void groupDelete() throws NotConnectedToWebServiceException, ClientProtocolException, IOException,
			AuthenticationRequired {
		int prevGroups = userGroupService.getUserUserGroups(user).size();
		userGroupService.deleteUserFromUserGroup(user, group);
		userGroupService.deleteUserGroup(group);
		Assert.assertEquals(userGroupService.getUserUserGroups(user).size(), prevGroups - 1);
	}

	@Test(alwaysRun = true, groups = { "clearData" }, dependsOnGroups = { "groupAccess", "userAccess" }, dependsOnMethods = { "addOrganization" })
	public void unsetUserFromOrganization() throws NotConnectedToWebServiceException, ClientProtocolException,
			IOException, AuthenticationRequired {
		int usersInOrg1 = organizationService.getOrganizationUsers(organization1).size();
		int usersInOrg2 = organizationService.getOrganizationUsers(organization2).size();
		organizationService.removeUserFromOrganization(user, organization1);
		organizationService.removeUserFromOrganization(user, organization2);
		Assert.assertEquals(usersInOrg1 - 1, organizationService.getOrganizationUsers(organization1).size());
		Assert.assertEquals(usersInOrg2 - 1, organizationService.getOrganizationUsers(organization2).size());
	}

	@Test(alwaysRun = true, groups = { "clearData" }, dependsOnMethods = { "unsetUserFromOrganization" })
	public void organizationDelete() throws NotConnectedToWebServiceException, ClientProtocolException, IOException,
			AuthenticationRequired, OrganizationNotDeletedException {
		int prevOrgs = organizationService.getOrganizations(company).size();
		organizationService.deleteOrganization(company, organization1);
		organizationService.deleteOrganization(company, organization2);
		Assert.assertEquals(prevOrgs - 2, organizationService.getOrganizations(company).size());
	}

	@Test(alwaysRun = true, groups = { "clearData" }, dependsOnGroups = { "userAccess", "groupAccess", "contactAccess",
			"pool" }, dependsOnMethods = { "userAccess", "groupDelete" }, expectedExceptions = WebServiceAccessError.class)
	public void userDelete() throws NotConnectedToWebServiceException, UserDoesNotExistException,
			ClientProtocolException, IOException, AuthenticationRequired, WebServiceAccessError {
		userService.deleteUser(user);
		userService.getUserById(user.getUserId());
	}

	@Test(alwaysRun = true, groups = { "clearData" }, dependsOnGroups = { "userAccess", "groupAccess", "contactAccess",
			"pool" })
	public void siteDelete() throws NotConnectedToWebServiceException, ClientProtocolException, IOException,
			AuthenticationRequired, WebServiceAccessError {
		Assert.assertTrue(siteService.deleteSite(site));
	}

	@After
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
