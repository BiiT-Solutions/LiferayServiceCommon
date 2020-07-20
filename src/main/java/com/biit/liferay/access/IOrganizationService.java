package com.biit.liferay.access;

import java.io.IOException;
import java.util.List;
import java.util.Set;

import org.apache.http.client.ClientProtocolException;

import com.biit.liferay.access.exceptions.DuplicatedLiferayElement;
import com.biit.liferay.access.exceptions.InvalidParsedElement;
import com.biit.liferay.access.exceptions.NotConnectedToWebServiceException;
import com.biit.liferay.access.exceptions.OrganizationNotDeletedException;
import com.biit.liferay.access.exceptions.PortletNotInstalledException;
import com.biit.liferay.access.exceptions.WebServiceAccessError;
import com.biit.usermanager.entity.IGroup;
import com.biit.usermanager.entity.IUser;
import com.biit.usermanager.security.exceptions.AuthenticationRequired;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.liferay.portal.model.Organization;

public interface IOrganizationService extends LiferayService, IServiceAccess {

	/**
	 * Creates a new organization.
	 * 
	 * @param company
	 *            the company where add the organization.
	 * @param parentOrganizationId
	 *            if it is a suborganization, we need the parent id.
	 * @param name
	 *            the name of the new organization. Must be unique.
	 * @param type
	 *            the type of the organization.
	 * @param regionId
	 *            the region
	 * @param countryId
	 *            the country
	 * @param statusId
	 *            the status
	 * @param comments
	 *            the comments
	 * @param site
	 *            if it is a site.
	 * @return the new organization added.
	 * @throws NotConnectedToWebServiceException
	 * @throws ClientProtocolException
	 * @throws IOException
	 * @throws AuthenticationRequired
	 * @throws WebServiceAccessError
	 * @throws DuplicatedLiferayElement
	 */
	IGroup<Long> addOrganization(IGroup<Long> company, Long parentOrganizationId, String name, OrganizationType type,
			Long regionId, Long countryId, int statusId, String comments, boolean site)
			throws NotConnectedToWebServiceException, ClientProtocolException, IOException, AuthenticationRequired,
			WebServiceAccessError, DuplicatedLiferayElement;

	/**
	 * Creates a new organization.
	 * 
	 * @param name
	 *            the name of the new organization.
	 * @return
	 * @throws ClientProtocolException
	 * @throws IOException
	 * @throws NotConnectedToWebServiceException
	 * @throws AuthenticationRequired
	 * @throws WebServiceAccessError
	 * @throws DuplicatedLiferayElement
	 */
	IGroup<Long> addOrganization(IGroup<Long> company, String name) throws ClientProtocolException, IOException,
			NotConnectedToWebServiceException, AuthenticationRequired, WebServiceAccessError, DuplicatedLiferayElement;

	boolean addOrganization(IGroup<Long> site, IUser<Long> user, IGroup<Long> organization)
			throws NotConnectedToWebServiceException, ClientProtocolException, IOException, AuthenticationRequired;

	/**
	 * Assign a list of users to an organization.
	 * 
	 * @param users
	 *            the users to assign
	 * @param organization
	 *            the destination organization.
	 * @throws ClientProtocolException
	 * @throws IOException
	 * @throws NotConnectedToWebServiceException
	 * @throws AuthenticationRequired
	 */
	void addUsersToOrganization(List<IUser<Long>> users, IGroup<Long> organization)
			throws ClientProtocolException, IOException, NotConnectedToWebServiceException, AuthenticationRequired;

	/**
	 * Assign a user to an organization.
	 * 
	 * @param user
	 *            the user to be assigned.
	 * @param organization
	 *            the organization where he will be added.
	 * @throws ClientProtocolException
	 * @throws IOException
	 * @throws NotConnectedToWebServiceException
	 * @throws AuthenticationRequired
	 */
	void addUserToOrganization(IUser<Long> user, IGroup<Long> organization)
			throws ClientProtocolException, IOException, NotConnectedToWebServiceException, AuthenticationRequired;

	/**
	 * Deletes an organization in Liferay database.
	 * 
	 * @param company
	 *            the company where the organization belongs to.
	 * @param organization
	 *            the organization to delete.
	 * @return true if has been deleted.
	 * @throws NotConnectedToWebServiceException
	 * @throws ClientProtocolException
	 * @throws IOException
	 * @throws AuthenticationRequired
	 * @throws OrganizationNotDeletedException
	 */
	boolean deleteOrganization(IGroup<Long> company, IGroup<Long> organization)
			throws NotConnectedToWebServiceException, ClientProtocolException, IOException, AuthenticationRequired,
			OrganizationNotDeletedException;

	/**
	 * Gets an organization by its id.
	 * 
	 * @param organizationId
	 *            the id of the organization.
	 * @return an organization or null value if does not exists.
	 * @throws WebServiceAccessError
	 * @throws NotConnectedToWebServiceException
	 * @throws IOException
	 * @throws JsonMappingException
	 * @throws JsonParseException
	 * @throws AuthenticationRequired
	 */
	IGroup<Long> getOrganization(Long organizationId) throws JsonParseException, JsonMappingException, IOException,
			NotConnectedToWebServiceException, WebServiceAccessError, AuthenticationRequired;

	/**
	 * Gets all organizations of a company.
	 * 
	 * @param company
	 *            the company where searach.
	 * @return a set of organizations.
	 * @throws NotConnectedToWebServiceException
	 * @throws ClientProtocolException
	 * @throws IOException
	 * @throws AuthenticationRequired
	 */
	Set<IGroup<Long>> getOrganizations(IGroup<Long> company)
			throws NotConnectedToWebServiceException, ClientProtocolException, IOException, AuthenticationRequired;

	/**
	 * Gets the organizations of a user in a site.
	 * 
	 * @param site
	 *            the site where organizations belong to.
	 * @param user
	 *            the user that belongs to the organizations.
	 * @return a set of organizations.
	 * @throws NotConnectedToWebServiceException
	 * @throws ClientProtocolException
	 * @throws IOException
	 * @throws AuthenticationRequired
	 */
	Set<IGroup<Long>> getOrganizations(IGroup<Long> site, IUser<Long> user) throws NotConnectedToWebServiceException,
			ClientProtocolException, IOException, AuthenticationRequired, PortletNotInstalledException;

	Set<IUser<Long>> getOrganizationUsers(IGroup<Long> organization)
			throws NotConnectedToWebServiceException, ClientProtocolException, IOException, AuthenticationRequired;

	/**
	 * Gets all organizations of a user.
	 * 
	 * @param user
	 *            the user for selecting
	 * @return a set of organizations
	 * @throws NotConnectedToWebServiceException
	 * @throws AuthenticationRequired
	 * @throws IOException
	 * @throws ClientProtocolException
	 */
	Set<IGroup<Long>> getUserOrganizationGroups(IUser<Long> user)
			throws NotConnectedToWebServiceException, ClientProtocolException, IOException, AuthenticationRequired;

	/**
	 * Gets all organizations where the user pertains to.
	 * 
	 * @param user
	 *            user that belongs to the organizations to search.
	 * @return a set of ogranizations.
	 * @throws ClientProtocolException
	 * @throws NotConnectedToWebServiceException
	 * @throws IOException
	 * @throws AuthenticationRequired
	 * @throws WebServiceAccessError
	 */
	Set<IGroup<Long>> getUserOrganizations(IUser<Long> user) throws ClientProtocolException,
			NotConnectedToWebServiceException, IOException, AuthenticationRequired, WebServiceAccessError;

	void removeUserFromOrganization(IUser<Long> user, IGroup<Long> organization)
			throws ClientProtocolException, IOException, NotConnectedToWebServiceException, AuthenticationRequired;

	void removeUsersFromOrganization(List<IUser<Long>> users, IGroup<Long> organization)
			throws ClientProtocolException, IOException, NotConnectedToWebServiceException, AuthenticationRequired;

	/**
	 * Obtains the default status from the database using a webservice.
	 * 
	 * @return an integer representing the status.
	 * @throws ClientProtocolException
	 * @throws NotConnectedToWebServiceException
	 * @throws IOException
	 * @throws AuthenticationRequired
	 * @throws WebServiceAccessError
	 */
	int getOrganizationStatus() throws ClientProtocolException, NotConnectedToWebServiceException, IOException,
			AuthenticationRequired, WebServiceAccessError;

	Long getOrganizationId(IGroup<Long> company, String name) throws NotConnectedToWebServiceException,
			ClientProtocolException, IOException, AuthenticationRequired, InvalidParsedElement;

	IGroup<Long> updateOrganization(IGroup<Long> company, Long organizationId, Long parentOrganizationId, String name,
			OrganizationType type, Long regionId, Long countryId, int statusId, String comments, boolean site)
			throws NotConnectedToWebServiceException, ClientProtocolException, IOException, AuthenticationRequired,
			WebServiceAccessError, DuplicatedLiferayElement;

	IGroup<Long> updateOrganization(IGroup<Long> company, Organization organization)
			throws ClientProtocolException, NotConnectedToWebServiceException, IOException, AuthenticationRequired,
			WebServiceAccessError, DuplicatedLiferayElement;

	boolean deleteOrganization(IGroup<Long> company, long organizationId) throws NotConnectedToWebServiceException,
			ClientProtocolException, IOException, AuthenticationRequired, OrganizationNotDeletedException;

	/**
	 * Gets the suborganizations/organizations for a user depending on the value of
	 * {@code parentOrganizationId}
	 * 
	 * @param company
	 *            Company where search the organizations
	 * @param user
	 *            user that belongs to the organizations to search
	 * @param parentOrganizationId
	 *            if null, search only parent organizations, else search a children
	 *            organizations.
	 * @return a set of organizations.
	 * @throws ClientProtocolException
	 * @throws NotConnectedToWebServiceException
	 * @throws IOException
	 * @throws AuthenticationRequired
	 * @throws WebServiceAccessError
	 */
	Set<IGroup<Long>> getOrganizations(IGroup<Long> company, IUser<Long> user, Long parentOrganizationId)
			throws ClientProtocolException, NotConnectedToWebServiceException, IOException, AuthenticationRequired,
			WebServiceAccessError;

	Set<IGroup<Long>> getOrganizations(IGroup<Long> company, Long parentId)
			throws NotConnectedToWebServiceException, ClientProtocolException, IOException, AuthenticationRequired;

}
