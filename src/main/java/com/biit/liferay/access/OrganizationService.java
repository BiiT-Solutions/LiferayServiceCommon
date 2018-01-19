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
import com.biit.liferay.access.exceptions.InvalidParsedElement;
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
import com.liferay.portal.model.User;

/**
 * Manage all Organization Services. As some organization's properties are
 * defined as a group, also manage some group services.
 */
public class OrganizationService extends ServiceAccess<IGroup<Long>, Organization> implements IOrganizationService {
	private final static long DEFAULT_PARENT_ORGANIZATION_ID = 0;
	private final static long DEFAULT_REGION_ID = 0;
	private final static long DEFAULT_COUNTRY_ID = 0;
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

	@Override
	public IGroup<Long> addOrganization(IGroup<Long> company, Long parentOrganizationId, String name,
			OrganizationType type, Long regionId, Long countryId, int statusId, String comments, boolean site)
			throws NotConnectedToWebServiceException, ClientProtocolException, IOException, AuthenticationRequired,
			WebServiceAccessError, DuplicatedLiferayElement {
		if (company == null || parentOrganizationId == null || name == null || type == null || regionId == null
				|| countryId == null || comments == null) {
			return null;
		}
		// Look up user in the liferay.
		checkConnection();

		List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("parentOrganizationId", parentOrganizationId + ""));
		params.add(new BasicNameValuePair("name", name));
		params.add(new BasicNameValuePair("type", type.getName()));
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
			if (parentOrganizationId.intValue() != 0) {
				organizationPool.removeSuborganizations(company.getId(), parentOrganizationId);
			}
			LiferayClientLogger.info(this.getClass().getName(),
					"Organization '" + organization.getUniqueName() + "' added.");
			return organization;
		}

		return organization;
	}

	public IGroup<Long> addOrganization(IGroup<Long> company, Organization organization)
			throws ClientProtocolException, NotConnectedToWebServiceException, IOException, AuthenticationRequired,
			WebServiceAccessError, DuplicatedLiferayElement {
		return addOrganization(company, organization.getParentOrganizationId(), organization.getName(),
				OrganizationType.getOrganizationType(organization.getType()), organization.getRegionId(),
				organization.getCountryId(), getOrganizationStatus(), organization.getComments(), DEFAULT_CREATE_SITE);
	}

	@Override
	public IGroup<Long> updateOrganization(IGroup<Long> company, Long organizationId, Long parentOrganizationId,
			String name, OrganizationType type, Long regionId, Long countryId, int statusId, String comments,
			boolean site) throws NotConnectedToWebServiceException, ClientProtocolException, IOException,
			AuthenticationRequired, WebServiceAccessError, DuplicatedLiferayElement {
		if (company == null || parentOrganizationId == null || name == null || type == null || regionId == null
				|| countryId == null || comments == null) {
			return null;
		}
		// Look up user in the liferay.
		checkConnection();

		List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("organizationId", organizationId + ""));
		params.add(new BasicNameValuePair("parentOrganizationId", parentOrganizationId + ""));
		params.add(new BasicNameValuePair("name", name));
		params.add(new BasicNameValuePair("type", type.getName()));
		params.add(new BasicNameValuePair("regionId", regionId + ""));
		params.add(new BasicNameValuePair("countryId", countryId + ""));
		params.add(new BasicNameValuePair("statusId", statusId + ""));
		params.add(new BasicNameValuePair("comments", comments));
		params.add(new BasicNameValuePair("site", Boolean.toString(site)));
		params.add(new BasicNameValuePair("-serviceContext", null));

		String result = getHttpResponse("organization/update-organization", params);
		IGroup<Long> organization = null;
		if (result != null) {
			// Check some errors
			if (result.contains("There is another organization named")) {
				throw new DuplicatedLiferayElement("Already exists an organization with name '" + name + "'.");
			}
			// A Simple JSON Response Read
			organization = decodeFromJson(result, Organization.class);
			// Refresh content of the pool.
			organizationPool.addGroupByTag(organization, company.getUniqueName());
			LiferayClientLogger.info(this.getClass().getName(),
					"Organization '" + organization.getUniqueName() + "' updated.");
			return organization;
		}

		return organization;
	}

	@Override
	public IGroup<Long> updateOrganization(IGroup<Long> company, Organization organization)
			throws ClientProtocolException, NotConnectedToWebServiceException, IOException, AuthenticationRequired,
			WebServiceAccessError, DuplicatedLiferayElement {
		return updateOrganization(company, organization.getId(), organization.getParentOrganizationId(),
				organization.getName(), OrganizationType.getOrganizationType(organization.getType()),
				organization.getRegionId(), organization.getCountryId(), getOrganizationStatus(),
				organization.getComments(), DEFAULT_CREATE_SITE);
	}

	@Override
	public IGroup<Long> addOrganization(IGroup<Long> company, String name) throws ClientProtocolException, IOException,
			NotConnectedToWebServiceException, AuthenticationRequired, WebServiceAccessError, DuplicatedLiferayElement {
		return addOrganization(company, DEFAULT_PARENT_ORGANIZATION_ID, name, OrganizationType.REGULAR_ORGANIZATION,
				DEFAULT_REGION_ID, DEFAULT_COUNTRY_ID, getOrganizationStatus(), "", DEFAULT_CREATE_SITE);
	}

	@Override
	public boolean addOrganization(IGroup<Long> site, IUser<Long> user, IGroup<Long> organization)
			throws NotConnectedToWebServiceException, ClientProtocolException, IOException, AuthenticationRequired {
		if (site != null && user != null && organization != null) {
			// Look up user in the liferay.
			checkConnection();

			List<NameValuePair> params = new ArrayList<NameValuePair>();
			params.add(new BasicNameValuePair("siteId", site.getId() + ""));
			params.add(new BasicNameValuePair("userId", user.getId() + ""));
			params.add(new BasicNameValuePair("organizationId", organization.getId() + ""));

			String result = getHttpResponse("liferay-service-common-portlet.site/add-organization", params);
			LiferayClientLogger.info(this.getClass().getName(), "Organization '" + organization.getId() + "' added.");

			return Boolean.parseBoolean(result);
		}
		return false;
	}

	@Override
	public void addUsersToOrganization(List<IUser<Long>> users, IGroup<Long> organization)
			throws ClientProtocolException, IOException, NotConnectedToWebServiceException, AuthenticationRequired {
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

			LiferayClientLogger.info(this.getClass().getName(),
					"Users " + users + " added to organization '" + organization + "'.");
		}
	}

	@Override
	public void addUserToOrganization(IUser<Long> user, IGroup<Long> organization)
			throws ClientProtocolException, IOException, NotConnectedToWebServiceException, AuthenticationRequired {
		List<IUser<Long>> users = new ArrayList<IUser<Long>>();
		users.add(user);
		addUsersToOrganization(users, organization);
	}

	@Override
	public void authorizedServerConnection(String address, String protocol, int port, String webservicesPath,
			String authenticationToken, String loginUser, String password) {
		// Standard behavior.
		super.authorizedServerConnection(address, protocol, port, webservicesPath, authenticationToken, loginUser,
				password);
		// Disconnect previous connection
		try {
			listTypeService.disconnect();
		} catch (Exception e) {

		}
		// Some user information is in the contact object.
		listTypeService = new ListTypeService();
		listTypeService.authorizedServerConnection(address, protocol, port, webservicesPath, authenticationToken,
				loginUser, password);

		try {
			companyService.disconnect();
		} catch (Exception e) {

		}
		companyService = new CompanyService();
		companyService.authorizedServerConnection(address, protocol, port, webservicesPath, authenticationToken,
				loginUser, password);
	}

	public Set<IGroup<Long>> decodeGroupListFromJson(String json, Class<Group> objectClass)
			throws JsonParseException, JsonMappingException, IOException {
		Set<IGroup<Long>> myObjects = new ObjectMapper().readValue(json, new TypeReference<Set<Group>>() {
		});
		return myObjects;
	}

	@Override
	public Set<IGroup<Long>> decodeListFromJson(String json, Class<Organization> objectClass)
			throws JsonParseException, JsonMappingException, IOException {
		Set<IGroup<Long>> myObjects = new ObjectMapper().readValue(json, new TypeReference<Set<Organization>>() {
		});

		return myObjects;
	}

	@Override
	public boolean deleteOrganization(IGroup<Long> company, IGroup<Long> organization)
			throws NotConnectedToWebServiceException, ClientProtocolException, IOException, AuthenticationRequired,
			OrganizationNotDeletedException {
		if (company != null && organization != null) {
			try {
				if (deleteOrganization(company, organization.getId())) {
					LiferayClientLogger.info(this.getClass().getName(),
							"Organization '" + organization.getUniqueName() + "' deleted.");
					return true;
				}
			} catch (OrganizationNotDeletedException ond) {
				throw new OrganizationNotDeletedException("Organization '" + organization.getUniqueName() + "' (id '"
						+ organization.getId() + "') not deleted correctly. ");
			}
		}
		return false;
	}

	@Override
	public boolean deleteOrganization(IGroup<Long> company, long organizationId)
			throws NotConnectedToWebServiceException, ClientProtocolException, IOException, AuthenticationRequired,
			OrganizationNotDeletedException {
		if (company != null) {
			checkConnection();

			List<NameValuePair> params = new ArrayList<NameValuePair>();
			params.add(new BasicNameValuePair("organizationId", organizationId + ""));

			String result = getHttpResponse("organization/delete-organization", params);

			if (result == null || result.length() < 3) {
				// Cannot remove by tag correctly. Remove everything from the
				// pool.
				organizationPool.reset();
				LiferayClientLogger.info(this.getClass().getName(),
						"Organization with Id '" + organizationId + "' deleted.");
				return true;
			} else {
				throw new OrganizationNotDeletedException(
						"Organization  with id '" + organizationId + "' not deleted correctly. ");
			}
		}
		return false;
	}

	@Override
	public void disconnect() {
		super.disconnect();
		listTypeService.disconnect();
		companyService.disconnect();
	}

	@Override
	public IGroup<Long> getOrganization(Long organizationId) throws JsonParseException, JsonMappingException,
			IOException, NotConnectedToWebServiceException, WebServiceAccessError, AuthenticationRequired {
		if (organizationId != null && organizationId >= 0) {
			// Look up organization in the pool.
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

	@Override
	public Set<IGroup<Long>> getOrganizations(IGroup<Long> company)
			throws NotConnectedToWebServiceException, ClientProtocolException, IOException, AuthenticationRequired {
		// Look up organization in the pool.
		Set<IGroup<Long>> organizations = new HashSet<IGroup<Long>>();
		if (company != null) {
			organizations = organizationPool.getElementsByTag(company.getUniqueName());
			if (organizations != null) {
				return organizations;
			}

			// Look up organizations in the liferay.
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

	@Override
	public Set<IGroup<Long>> getOrganizations(IGroup<Long> site, IUser<Long> user)
			throws NotConnectedToWebServiceException, ClientProtocolException, IOException, AuthenticationRequired,
			PortletNotInstalledException {
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
					throw new PortletNotInstalledException(
							"Portlet 'form-reader-portlet' is not installed on Liferay. Please, install it.");
				}
				// A Simple JSON Response Read
				organizations = decodeListFromJson(result, Organization.class);
				organizationPool.addOrganizationsBySiteAndUser(site, user, organizations);
			}
		}
		return organizations;
	}

	@Override
	public Long getOrganizationId(IGroup<Long> company, String name) throws NotConnectedToWebServiceException,
			ClientProtocolException, IOException, AuthenticationRequired, InvalidParsedElement {
		if (company != null && name != null) {
			// Read from Liferay.
			checkConnection();

			List<NameValuePair> params = new ArrayList<NameValuePair>();
			params.add(new BasicNameValuePair("companyId", Long.toString(company.getId())));
			params.add(new BasicNameValuePair("name", name));

			String result = getHttpResponse("organization/get-organization-id", params);
			if (result != null) {
				try {
					return Long.parseLong(result);
				} catch (Exception e) {
					throw new InvalidParsedElement("The response '" + result
							+ "' of the webservice 'organization/get-organization-id'  is invalid.");
				}
			}
		}
		return null;
	}

	@Override
	public int getOrganizationStatus() throws ClientProtocolException, NotConnectedToWebServiceException, IOException,
			AuthenticationRequired, WebServiceAccessError {
		if (organizationStatus == null) {
			try {
				organizationStatus = listTypeService.getFullMemberStatus();
			} catch (AuthenticationRequired e) {
				throw new AuthenticationRequired(
						"Cannot connect to inner service 'ListTypeService'. Authentication Required. ");
			}
		}
		return organizationStatus;
	}

	@Override
	public Set<IUser<Long>> getOrganizationUsers(IGroup<Long> organization)
			throws NotConnectedToWebServiceException, ClientProtocolException, IOException, AuthenticationRequired {
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

	@Override
	public Set<IGroup<Long>> getUserOrganizationGroups(IUser<Long> user)
			throws NotConnectedToWebServiceException, ClientProtocolException, IOException, AuthenticationRequired {
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
				LiferayClientLogger.debug(this.getClass().getName(), "Obtained '" + groups + "'.");
				return groups;
			}
		}
		return null;
	}

	@Override
	public Set<IGroup<Long>> getUserOrganizations(IUser<Long> user) throws ClientProtocolException,
			NotConnectedToWebServiceException, IOException, AuthenticationRequired, WebServiceAccessError {
		Set<IGroup<Long>> organizations = new HashSet<IGroup<Long>>();
		if (user != null) {

			// Look up group in the pool.
			organizations = organizationPool.getGroups(user.getId());
			if (organizations != null) {
				return organizations;
			}

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

	@Override
	public Set<IGroup<Long>> getOrganizations(IGroup<Long> company, IUser<Long> user, Long parentOrganizationId)
			throws ClientProtocolException, NotConnectedToWebServiceException, IOException, AuthenticationRequired,
			WebServiceAccessError {
		Set<IGroup<Long>> organizations = new HashSet<IGroup<Long>>();
		if (user != null) {
			if (parentOrganizationId == null) {
				parentOrganizationId = new Long(0);
			}

			// Look up group in the pool.
			organizations = organizationPool.getSuborganizations(company, user, parentOrganizationId);
			if (organizations != null) {
				return organizations;
			}

			checkConnection();

			List<NameValuePair> params = new ArrayList<NameValuePair>();
			params.add(new BasicNameValuePair("companyId", company.getId() + ""));
			params.add(new BasicNameValuePair("parentOrganizationId", parentOrganizationId.intValue() + ""));
			String result = getHttpResponse("organization/get-organizations", params);
			if (result != null) {
				// Get organizations with selected parent.
				organizations = decodeListFromJson(result, Organization.class);
				// Obtain organizations for the user.
				Set<IGroup<Long>> userOrganizations = getUserOrganizations(user);
				organizations.retainAll(userOrganizations);

				// Add to pool.
				organizationPool.addOrganizations(company.getId(), user.getId(), parentOrganizationId, organizations);

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
	@Override
	public void removeUserFromOrganization(IUser<Long> user, IGroup<Long> organization)
			throws ClientProtocolException, IOException, NotConnectedToWebServiceException, AuthenticationRequired {
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
	@Override
	public void removeUsersFromOrganization(List<IUser<Long>> users, IGroup<Long> organization)
			throws ClientProtocolException, IOException, NotConnectedToWebServiceException, AuthenticationRequired {
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

			LiferayClientLogger.info(this.getClass().getName(),
					"Users " + usersIds + " removed from organization '" + organization.getUniqueName() + "'.");
		}
	}

	public void reset() {
		organizationPool.reset();
	}
}
