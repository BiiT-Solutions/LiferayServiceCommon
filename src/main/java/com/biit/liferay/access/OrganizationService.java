package com.biit.liferay.access;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.message.BasicNameValuePair;

import com.biit.liferay.access.exceptions.DuplicatedLiferayElement;
import com.biit.liferay.access.exceptions.NotConnectedToWebServiceException;
import com.biit.liferay.access.exceptions.OrganizationNotDeletedException;
import com.biit.liferay.access.exceptions.PortletNotInstalledException;
import com.biit.liferay.access.exceptions.WebServiceAccessError;
import com.biit.liferay.log.LiferayClientLogger;
import com.biit.usermanager.entity.IGroup;
import com.biit.usermanager.entity.IUser;
import com.biit.usermanager.security.exceptions.AuthenticationRequired;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.liferay.portal.model.Group;
import com.liferay.portal.model.Organization;
import com.liferay.portal.model.Site;
import com.liferay.portal.model.User;

/**
 * Manage all Organization Services. As some organization's properties are
 * defined as a group, also manage some group services.
 * 
 */
public class OrganizationService extends ServiceAccess<IGroup<Long>, Organization> {
	private final static long DEFAULT_PARENT_ORGANIZATION_ID = 0;
	private final static long DEFAULT_REGION_ID = 0;
	private final static long DEFAULT_COUNTRY_ID = 0;
	private final static String DEFAULT_TYPE = "regular-organization";
	private final static boolean DEFAULT_CREATE_SITE = false;
	private final static int DEFAUL_START_GROUP = -1;
	private final static int DEFAUL_END_GROUP = -1;
	private Integer organizationStatus = null;
	private ListTypeService listTypeService;
	private CompanyService companyService;

	private OrganizationPool organizationPool;

	public OrganizationService() {
		organizationPool = new OrganizationPool();
	}

	/**
	 * Creates a new organization.
	 * 
	 * @param parentOrganizationId
	 * @param name
	 * @param type
	 * @param regionId
	 * @param countryId
	 * @param statusId
	 * @param comments
	 * @param site
	 * @return
	 * @throws NotConnectedToWebServiceException
	 * @throws ClientProtocolException
	 * @throws IOException
	 * @throws AuthenticationRequired
	 * @throws WebServiceAccessError
	 * @throws DuplicatedLiferayElement
	 */
	public IGroup<Long> addOrganization(IGroup<Long> company, Long parentOrganizationId, String name, String type, Long regionId, Long countryId, int statusId,
			String comments, boolean site) throws NotConnectedToWebServiceException, ClientProtocolException, IOException, AuthenticationRequired,
			WebServiceAccessError, DuplicatedLiferayElement {
		if (company == null || parentOrganizationId == null || name == null || type == null || regionId == null || countryId == null || comments == null) {
			return null;
		}
		// Look up user in the liferay.
		checkConnection();

		List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("parentOrganizationId", parentOrganizationId + ""));
		params.add(new BasicNameValuePair("name", name));
		params.add(new BasicNameValuePair("type", type));
		params.add(new BasicNameValuePair("regionId", regionId + ""));
		params.add(new BasicNameValuePair("countryId", countryId + ""));
		params.add(new BasicNameValuePair("statusId", statusId + ""));
		params.add(new BasicNameValuePair("comments", comments));
		params.add(new BasicNameValuePair("site", Boolean.toString(site)));

		String result = getHttpResponse("organization/add-organization", params);
		IGroup<Long> organization = null;
		if (result != null) {
			// Check some errors
			if (result.contains("There is another organization named")) {
				throw new DuplicatedLiferayElement("Already exists an organization with name '" + name + "'.");
			}
			// A Simple JSON Response Read
			organization = decodeFromJson(result, Organization.class);
			organizationPool.addGroupByTag(organization, company.getUniqueName());
			LiferayClientLogger.info(this.getClass().getName(), "Organization '" + organization.getUniqueName() + "' added.");
			return organization;
		}

		return organization;
	}

	/**
	 * Creates a new organization. Requires the use of ListTypeService.
	 * 
	 * @param name
	 * @return
	 * @throws ClientProtocolException
	 * @throws IOException
	 * @throws NotConnectedToWebServiceException
	 * @throws AuthenticationRequired
	 * @throws WebServiceAccessError
	 * @throws DuplicatedLiferayElement
	 */
	public IGroup<Long> addOrganization(IGroup<Long> company, String name) throws ClientProtocolException, IOException, NotConnectedToWebServiceException,
			AuthenticationRequired, WebServiceAccessError, DuplicatedLiferayElement {
		return addOrganization(company, DEFAULT_PARENT_ORGANIZATION_ID, name, DEFAULT_TYPE, DEFAULT_REGION_ID, DEFAULT_COUNTRY_ID, getOrganizationStatus(), "",
				DEFAULT_CREATE_SITE);
	}

	public boolean addOrganization(Site site, IUser<Long> user, IGroup<Long> organization) throws NotConnectedToWebServiceException, ClientProtocolException,
			IOException, AuthenticationRequired {
		if (site != null && user != null && organization != null) {
			// Look up user in the liferay.
			checkConnection();

			List<NameValuePair> params = new ArrayList<NameValuePair>();
			params.add(new BasicNameValuePair("siteId", site.getSiteId() + ""));
			params.add(new BasicNameValuePair("userId", user.getId() + ""));
			params.add(new BasicNameValuePair("organizationId", organization.getId() + ""));

			String result = getHttpResponse("liferay-service-common-portlet.site/add-organization", params);
			LiferayClientLogger.info(this.getClass().getName(), "Organization '" + organization.getId() + "' added.");

			return Boolean.parseBoolean(result);
		}
		return false;
	}

	/**
	 * Assign a list of users to an organization.
	 * 
	 * @param users
	 * @param organization
	 * @throws ClientProtocolException
	 * @throws IOException
	 * @throws NotConnectedToWebServiceException
	 * @throws AuthenticationRequired
	 */
	public void addUsersToOrganization(List<IUser<Long>> users, IGroup<Long> organization) throws ClientProtocolException, IOException,
			NotConnectedToWebServiceException, AuthenticationRequired {
		if (users != null && organization != null && !users.isEmpty()) {
			// Look up user in the liferay.
			checkConnection();

			String usersIds = "";
			if (users.size() > 0) {
				usersIds = "[";
			}
			for (int i = 0; i < users.size(); i++) {
				usersIds += users.get(i).getId();
				if (i < users.size() - 1) {
					usersIds += ",";
				}
			}
			if (usersIds.length() > 0) {
				usersIds += "]";
			}

			List<NameValuePair> params = new ArrayList<NameValuePair>();
			params.add(new BasicNameValuePair("organizationId", organization.getId() + ""));
			params.add(new BasicNameValuePair("userIds", usersIds));

			getHttpResponse("user/add-organization-users", params);

			// Reset the pool of groups to calculate again the user's
			// organization groups.
			for (IUser<Long> user : users) {
				organizationPool.addUserToGroup(user, organization);
			}

			LiferayClientLogger.info(this.getClass().getName(), "Users " + usersIds + " added to organization '" + organization.getUniqueName() + "'.");
		}
	}

	/**
	 * Assign a user to an organization.
	 * 
	 * @param user
	 * @param organization
	 * @throws ClientProtocolException
	 * @throws IOException
	 * @throws NotConnectedToWebServiceException
	 * @throws AuthenticationRequired
	 */
	public void addUserToOrganization(IUser<Long> user, IGroup<Long> organization) throws ClientProtocolException, IOException,
			NotConnectedToWebServiceException, AuthenticationRequired {
		List<IUser<Long>> users = new ArrayList<IUser<Long>>();
		users.add(user);
		addUsersToOrganization(users, organization);
	}

	@Override
	public void authorizedServerConnection(String address, String protocol, int port, String webservicesPath, String authenticationToken, String loginUser,
			String password) {
		// Standard behavior.
		super.authorizedServerConnection(address, protocol, port, webservicesPath, authenticationToken, loginUser, password);
		// Disconnect previous connection
		try {
			listTypeService.disconnect();
		} catch (Exception e) {

		}
		// Some user information is in the contact object.
		listTypeService = new ListTypeService();
		listTypeService.authorizedServerConnection(address, protocol, port, webservicesPath, authenticationToken, loginUser, password);

		try {
			companyService.disconnect();
		} catch (Exception e) {

		}
		companyService = new CompanyService();
		companyService.authorizedServerConnection(address, protocol, port, webservicesPath, authenticationToken, loginUser, password);
	}

	public Set<IGroup<Long>> decodeGroupListFromJson(String json, Class<Group> objectClass) throws JsonParseException, JsonMappingException, IOException {
		Set<IGroup<Long>> myObjects = new ObjectMapper().readValue(json, new TypeReference<Set<Group>>() {
		});
		return myObjects;
	}

	@Override
	public Set<IGroup<Long>> decodeListFromJson(String json, Class<Organization> objectClass) throws JsonParseException, JsonMappingException, IOException {
		Set<IGroup<Long>> myObjects = new ObjectMapper().readValue(json, new TypeReference<Set<Organization>>() {
		});

		return myObjects;
	}

	/**
	 * Deletes an organization in Liferay database.
	 * 
	 * @param organization
	 * @throws NotConnectedToWebServiceException
	 * @throws ClientProtocolException
	 * @throws IOException
	 * @throws AuthenticationRequired
	 * @throws OrganizationNotDeletedException
	 */
	public void deleteOrganization(IGroup<Long> company, IGroup<Long> organization) throws NotConnectedToWebServiceException, ClientProtocolException,
			IOException, AuthenticationRequired, OrganizationNotDeletedException {
		if (company != null && organization != null) {
			checkConnection();

			List<NameValuePair> params = new ArrayList<NameValuePair>();
			params.add(new BasicNameValuePair("organizationId", organization.getId() + ""));

			String result = getHttpResponse("organization/delete-organization", params);

			if (result == null || result.length() < 3) {
				organizationPool.removeGroupByTag(company.getUniqueName(), organization);
				LiferayClientLogger.info(this.getClass().getName(), "Organization '" + organization.getUniqueName() + "' deleted.");
			} else {
				throw new OrganizationNotDeletedException("Organization '" + organization.getUniqueName() + "' (id:" + organization.getId()
						+ ") not deleted correctly. ");
			}
		}
	}

	@Override
	public void disconnect() {
		super.disconnect();
		listTypeService.disconnect();
		companyService.disconnect();
	}

	/**
	 * Gets an organization by its ID.
	 * 
	 * @param organizationId
	 * @return
	 * @throws WebServiceAccessError
	 * @throws NotConnectedToWebServiceException
	 * @throws IOException
	 * @throws JsonMappingException
	 * @throws JsonParseException
	 * @throws AuthenticationRequired
	 */
	public IGroup<Long> getOrganization(Long organizationId) throws JsonParseException, JsonMappingException, IOException, NotConnectedToWebServiceException,
			WebServiceAccessError, AuthenticationRequired {
		if (organizationId != null && organizationId >= 0) {
			// Look up user in the pool.
			IGroup<Long> organization = organizationPool.getGroupById(organizationId);
			if (organization != null) {
				return organization;
			}

			// Read from Liferay.
			checkConnection();

			List<NameValuePair> params = new ArrayList<NameValuePair>();
			params.add(new BasicNameValuePair("organizationId", Long.toString(organizationId)));

			String result = getHttpResponse("organization/get-organization", params);
			if (result != null) {
				// A Simple JSON Response Read
				organization = decodeFromJson(result, Organization.class);
				organizationPool.addGroup(organization);
				return organization;
			}
		}
		return null;
	}

	/**
	 * Gets all organizations of a company.
	 * 
	 * @param company
	 * @return
	 * @throws NotConnectedToWebServiceException
	 * @throws ClientProtocolException
	 * @throws IOException
	 * @throws AuthenticationRequired
	 */
	public Set<IGroup<Long>> getOrganizations(IGroup<Long> company) throws NotConnectedToWebServiceException, ClientProtocolException, IOException,
			AuthenticationRequired {
		// Look up user in the pool.
		Set<IGroup<Long>> organizations = new HashSet<IGroup<Long>>();
		if (company != null) {
			organizations = organizationPool.getElementsByTag(company.getUniqueName());
			if (organizations != null) {
				return organizations;
			}

			// Look up user in the liferay.
			checkConnection();

			List<NameValuePair> params = new ArrayList<NameValuePair>();
			params.add(new BasicNameValuePair("companyId", company.getId() + ""));
			params.add(new BasicNameValuePair("parentOrganizationId", DEFAULT_PARENT_ORGANIZATION_ID + ""));

			String result = getHttpResponse("organization/get-organizations", params);
			if (result != null) {
				// A Simple JSON Response Read
				organizations = decodeListFromJson(result, Organization.class);
				organizationPool.addGroupByTag(organizations, company.getUniqueName());
			}
		}

		return organizations;
	}

	/**
	 * Gets the organizations of a user in a site.
	 * 
	 * @param site
	 * @param user
	 * @return
	 * @throws NotConnectedToWebServiceException
	 * @throws ClientProtocolException
	 * @throws IOException
	 * @throws AuthenticationRequired
	 */
	public Set<IGroup<Long>> getOrganizations(IGroup<Long> site, IUser<Long> user) throws NotConnectedToWebServiceException, ClientProtocolException,
			IOException, AuthenticationRequired, PortletNotInstalledException {
		Set<IGroup<Long>> organizations = new HashSet<IGroup<Long>>();

		if (site != null && user != null) {
			organizations = organizationPool.getOrganizationBySiteAndUser(site, user);
			if (organizations != null) {
				return organizations;
			}

			// Look up user in the liferay.
			checkConnection();

			List<NameValuePair> params = new ArrayList<NameValuePair>();
			params.add(new BasicNameValuePair("siteId", site.getId() + ""));
			params.add(new BasicNameValuePair("userId", user.getId() + ""));

			String result = getHttpResponse("liferay-service-common-portlet.site/get-organizations", params);
			if (result != null) {
				// Check if Portlet not installed
				if (result.contains("and method POST for")) {
					throw new PortletNotInstalledException("Portlet 'form-reader-portlet' is not installed on Liferay. Please, install it.");
				}
				// A Simple JSON Response Read
				organizations = decodeListFromJson(result, Organization.class);
				organizationPool.addOrganizationsBySiteAndUser(site, user, organizations);
			}
		}
		return organizations;
	}

	/**
	 * Obtains the default status from the database using a webservice. Requires
	 * the use of ListTypeService.
	 * 
	 * @return
	 * @throws ClientProtocolException
	 * @throws NotConnectedToWebServiceException
	 * @throws IOException
	 * @throws AuthenticationRequired
	 * @throws WebServiceAccessError
	 */
	private int getOrganizationStatus() throws ClientProtocolException, NotConnectedToWebServiceException, IOException, AuthenticationRequired,
			WebServiceAccessError {
		if (organizationStatus == null) {
			try {
				organizationStatus = listTypeService.getFullMemberStatus();
			} catch (AuthenticationRequired e) {
				throw new AuthenticationRequired("Cannot connect to inner service 'ListTypeService'. Authentication Required. ");
			}
		}
		return organizationStatus;
	}

	public Set<IUser<Long>> getOrganizationUsers(IGroup<Long> organization) throws NotConnectedToWebServiceException, ClientProtocolException, IOException,
			AuthenticationRequired {
		Set<IUser<Long>> users = new HashSet<IUser<Long>>();
		// Look up users in the pool.
		users = organizationPool.getGroupUsers(organization.getId());
		if (users != null) {
			return users;
		}

		// Look up user in the liferay.
		checkConnection();

		List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("organizationId", organization.getId() + ""));

		String result = getHttpResponse("/user/get-organization-users", params);
		if (result != null) {
			// A Simple JSON Response Read
			users = (new UserService()).decodeListFromJson(result, User.class);
			organizationPool.addGroupUsers(organization.getId(), users);
			return users;
		}
		return null;
	}

	/**
	 * Gets all organizations of a user.
	 * 
	 * @param user
	 * @return
	 * @throws NotConnectedToWebServiceException
	 * @throws AuthenticationRequired
	 * @throws IOException
	 * @throws ClientProtocolException
	 */
	public Set<IGroup<Long>> getUserOrganizationGroups(IUser<Long> user) throws NotConnectedToWebServiceException, ClientProtocolException, IOException,
			AuthenticationRequired {
		if (user != null) {
			Set<IGroup<Long>> groups = new HashSet<IGroup<Long>>();
			// Look up group in the pool.
			groups = organizationPool.getGroups(user.getId());
			if (groups != null) {
				return groups;
			}

			// Look up user in the liferay.
			checkConnection();

			List<NameValuePair> params = new ArrayList<NameValuePair>();
			params.add(new BasicNameValuePair("userId", user.getId() + ""));
			params.add(new BasicNameValuePair("start", DEFAUL_START_GROUP + ""));
			params.add(new BasicNameValuePair("end", DEFAUL_END_GROUP + ""));

			String result = getHttpResponse("group/get-user-organizations-groups", params);
			if (result != null) {
				// A Simple JSON Response Read
				groups = decodeGroupListFromJson(result, Group.class);
				organizationPool.addUserToGroups(user, groups);
				return groups;
			}
		}
		return null;
	}

	/**
	 * Gets all organizations where the user pertains to.
	 * 
	 * @param company
	 * @param user
	 * @return
	 * @throws ClientProtocolException
	 * @throws NotConnectedToWebServiceException
	 * @throws IOException
	 * @throws AuthenticationRequired
	 * @throws WebServiceAccessError
	 */
	public Set<IGroup<Long>> getUserOrganizations(IUser<Long> user) throws ClientProtocolException, NotConnectedToWebServiceException, IOException,
			AuthenticationRequired, WebServiceAccessError {
		Set<IGroup<Long>> organizations = new HashSet<IGroup<Long>>();
		if (user != null) {

			// Look up group in the pool.
			organizations = organizationPool.getGroups(user.getId());
			if (organizations != null) {
				return organizations;
			}

			// Look up user in the liferay.
			checkConnection();

			List<NameValuePair> params = new ArrayList<NameValuePair>();
			params.add(new BasicNameValuePair("userId", user.getId() + ""));
			String result = getHttpResponse("organization/get-user-organizations", params);
			if (result != null) {
				// A Simple JSON Response Read
				organizations = decodeListFromJson(result, Organization.class);
				organizationPool.addUserToGroups(user, organizations);
				return organizations;
			}
		}

		return organizations;
	}

	/**
	 * Remove a list of users to an organization.
	 * 
	 * @param users
	 * @param organization
	 * @throws ClientProtocolException
	 * @throws IOException
	 * @throws NotConnectedToWebServiceException
	 * @throws AuthenticationRequired
	 */
	public void removeUserFromOrganization(IUser<Long> user, IGroup<Long> organization) throws ClientProtocolException, IOException,
			NotConnectedToWebServiceException, AuthenticationRequired {
		if (user != null && organization != null) {
			// Look up user in the liferay.
			List<IUser<Long>> users = new ArrayList<IUser<Long>>();
			users.add(user);
			removeUsersFromOrganization(users, organization);
		}
	}

	/**
	 * Remove a list of users to an organization.
	 * 
	 * @param users
	 * @param organization
	 * @throws ClientProtocolException
	 * @throws IOException
	 * @throws NotConnectedToWebServiceException
	 * @throws AuthenticationRequired
	 */
	public void removeUsersFromOrganization(List<IUser<Long>> users, IGroup<Long> organization) throws ClientProtocolException, IOException,
			NotConnectedToWebServiceException, AuthenticationRequired {
		if (users != null && organization != null && !users.isEmpty()) {
			// Look up user in the liferay.
			checkConnection();

			String usersIds = "";
			if (users.size() > 0) {
				usersIds = "[";
			}
			for (int i = 0; i < users.size(); i++) {
				usersIds += users.get(i).getId();
				if (i < users.size() - 1) {
					usersIds += ",";
				}
			}
			if (usersIds.length() > 0) {
				usersIds += "]";
			}

			List<NameValuePair> params = new ArrayList<NameValuePair>();
			params.add(new BasicNameValuePair("organizationId", organization.getId() + ""));
			params.add(new BasicNameValuePair("userIds", usersIds));

			getHttpResponse("user/unset-organization-users", params);

			// Reset the pool of groups to calculate again the user's
			// organization groups.
			for (IUser<Long> user : users) {
				organizationPool.removeUser(user);
			}

			LiferayClientLogger.info(this.getClass().getName(), "Users " + usersIds + " removed from organization '" + organization.getUniqueName() + "'.");
		}
	}

	public void reset() {
		organizationPool.reset();
	}
}
