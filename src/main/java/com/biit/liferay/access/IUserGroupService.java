package com.biit.liferay.access;

/*-
 * #%L
 * Liferay Client Common Utils
 * %%
 * Copyright (C) 2013 - 2025 BiiT Sourcing Solutions S.L.
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

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
