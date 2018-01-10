package com.biit.liferay.access;

import java.io.IOException;
import java.util.List;
import java.util.Set;

import org.apache.http.client.ClientProtocolException;

import com.biit.liferay.access.exceptions.DuplicatedLiferayElement;
import com.biit.liferay.access.exceptions.NotConnectedToWebServiceException;
import com.biit.liferay.access.exceptions.UserGroupDoesNotExistException;
import com.biit.liferay.access.exceptions.WebServiceAccessError;
import com.biit.usermanager.entity.IGroup;
import com.biit.usermanager.entity.IUser;
import com.biit.usermanager.security.exceptions.AuthenticationRequired;

public interface IUserGroupService extends IServiceAccess {

	IGroup<Long> addUserGroup(String name, String description)
			throws NotConnectedToWebServiceException, ClientProtocolException, IOException, AuthenticationRequired,
			WebServiceAccessError, DuplicatedLiferayElement;

	void addUsersToGroup(List<IUser<Long>> users, IGroup<Long> group)
			throws ClientProtocolException, IOException, NotConnectedToWebServiceException, AuthenticationRequired;

	void addUserToGroup(IUser<Long> user, IGroup<Long> group)
			throws NotConnectedToWebServiceException, ClientProtocolException, IOException, AuthenticationRequired;

	void deleteUserFromUserGroup(IUser<Long> user, IGroup<Long> userGroup)
			throws NotConnectedToWebServiceException, ClientProtocolException, IOException, AuthenticationRequired;

	void deleteUserGroup(IGroup<Long> userGroup)
			throws NotConnectedToWebServiceException, ClientProtocolException, IOException, AuthenticationRequired;

	void deleteUserGroup(long userGroupId)
			throws NotConnectedToWebServiceException, ClientProtocolException, IOException, AuthenticationRequired;

	IGroup<Long> getUserGroup(long userGroupId)
			throws NotConnectedToWebServiceException, UserGroupDoesNotExistException, ClientProtocolException,
			IOException, AuthenticationRequired, WebServiceAccessError;

	IGroup<Long> getUserGroup(String name) throws NotConnectedToWebServiceException, ClientProtocolException,
			IOException, UserGroupDoesNotExistException, AuthenticationRequired, WebServiceAccessError;

	Set<IGroup<Long>> getUserUserGroups(IUser<Long> user)
			throws NotConnectedToWebServiceException, ClientProtocolException, IOException, AuthenticationRequired;

	void reset();

}
