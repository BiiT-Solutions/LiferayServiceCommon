package com.biit.liferay.access;

import java.io.IOException;
import java.util.List;
import java.util.Set;

import org.apache.http.client.ClientProtocolException;

import com.biit.liferay.access.exceptions.DuplicatedLiferayElement;
import com.biit.liferay.access.exceptions.NotConnectedToWebServiceException;
import com.biit.liferay.access.exceptions.OrganizationNotDeletedException;
import com.biit.liferay.access.exceptions.PortletNotInstalledException;
import com.biit.liferay.access.exceptions.WebServiceAccessError;
import com.biit.usermanager.entity.IGroup;
import com.biit.usermanager.entity.IUser;
import com.biit.usermanager.security.exceptions.AuthenticationRequired;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;

public interface IOrganizationService {

	IGroup<Long> addOrganization(IGroup<Long> company, Long parentOrganizationId, String name, OrganizationType type, Long regionId, Long countryId, int statusId,
			String comments, boolean site) throws NotConnectedToWebServiceException, ClientProtocolException, IOException, AuthenticationRequired,
			WebServiceAccessError, DuplicatedLiferayElement;

	IGroup<Long> addOrganization(IGroup<Long> company, String name) throws ClientProtocolException, IOException, NotConnectedToWebServiceException,
			AuthenticationRequired, WebServiceAccessError, DuplicatedLiferayElement;

	boolean addOrganization(IGroup<Long> site, IUser<Long> user, IGroup<Long> organization) throws NotConnectedToWebServiceException, ClientProtocolException,
			IOException, AuthenticationRequired;

	void addUsersToOrganization(List<IUser<Long>> users, IGroup<Long> organization) throws ClientProtocolException, IOException,
			NotConnectedToWebServiceException, AuthenticationRequired;

	void addUserToOrganization(IUser<Long> user, IGroup<Long> organization) throws ClientProtocolException, IOException, NotConnectedToWebServiceException,
			AuthenticationRequired;

	void deleteOrganization(IGroup<Long> company, IGroup<Long> organization) throws NotConnectedToWebServiceException, ClientProtocolException, IOException,
			AuthenticationRequired, OrganizationNotDeletedException;

	IGroup<Long> getOrganization(Long organizationId) throws JsonParseException, JsonMappingException, IOException, NotConnectedToWebServiceException,
			WebServiceAccessError, AuthenticationRequired;

	Set<IGroup<Long>> getOrganizations(IGroup<Long> company) throws NotConnectedToWebServiceException, ClientProtocolException, IOException,
			AuthenticationRequired;

	Set<IGroup<Long>> getOrganizations(IGroup<Long> site, IUser<Long> user) throws NotConnectedToWebServiceException, ClientProtocolException, IOException,
			AuthenticationRequired, PortletNotInstalledException;

	Set<IUser<Long>> getOrganizationUsers(IGroup<Long> organization) throws NotConnectedToWebServiceException, ClientProtocolException, IOException,
			AuthenticationRequired;

	Set<IGroup<Long>> getUserOrganizationGroups(IUser<Long> user) throws NotConnectedToWebServiceException, ClientProtocolException, IOException,
			AuthenticationRequired;

	Set<IGroup<Long>> getUserOrganizations(IUser<Long> user) throws ClientProtocolException, NotConnectedToWebServiceException, IOException,
			AuthenticationRequired, WebServiceAccessError;

	void removeUserFromOrganization(IUser<Long> user, IGroup<Long> organization) throws ClientProtocolException, IOException,
			NotConnectedToWebServiceException, AuthenticationRequired;

	void removeUsersFromOrganization(List<IUser<Long>> users, IGroup<Long> organization) throws ClientProtocolException, IOException,
			NotConnectedToWebServiceException, AuthenticationRequired;

	int getOrganizationStatus() throws ClientProtocolException, NotConnectedToWebServiceException, IOException, AuthenticationRequired, WebServiceAccessError;

}
