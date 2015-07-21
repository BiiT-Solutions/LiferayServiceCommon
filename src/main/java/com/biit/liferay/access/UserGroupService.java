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
import com.biit.liferay.access.exceptions.UserGroupDoesNotExistException;
import com.biit.liferay.access.exceptions.WebServiceAccessError;
import com.biit.liferay.log.LiferayClientLogger;
import com.biit.usermanager.entity.IGroup;
import com.biit.usermanager.entity.IUser;
import com.biit.usermanager.entity.pool.GroupPool;
import com.biit.usermanager.security.exceptions.AuthenticationRequired;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.liferay.portal.model.UserGroup;

/**
 * This class allows to manage group from Liferay portal.
 */
public class UserGroupService extends ServiceAccess<IGroup<Long>, UserGroup> {
	private GroupPool<Long, Long> groupPool;

	public UserGroupService() {
		groupPool = new GroupPool<Long, Long>();
	}

	/**
	 * Creates a new group on Liferay. For testing use only.
	 * 
	 * @param name
	 *            name of the new group.
	 * @param description
	 *            description of the new group.
	 * @return
	 * @throws NotConnectedToWebServiceException
	 * @throws IOException
	 * @throws ClientProtocolException
	 * @throws AuthenticationRequired
	 * @throws WebServiceAccessError
	 * @throws DuplicatedLiferayElement
	 */
	public IGroup<Long> addUserGroup(String name, String description) throws NotConnectedToWebServiceException,
			ClientProtocolException, IOException, AuthenticationRequired, WebServiceAccessError,
			DuplicatedLiferayElement {
		if (name != null && name.length() > 0) {
			checkConnection();

			List<NameValuePair> params = new ArrayList<NameValuePair>();
			params.add(new BasicNameValuePair("name", name));
			params.add(new BasicNameValuePair("description", description));

			String result = getHttpResponse("usergroup/add-user-group", params);
			IGroup<Long> userGroup = null;
			if (result != null) {
				// Check some errors
				if (result.contains("DuplicateUserGroupException")) {
					throw new DuplicatedLiferayElement("Already exists a group with this name");
				}
				// A Simple JSON Response Read
				userGroup = decodeFromJson(result, UserGroup.class);
				groupPool.addGroup(userGroup);
				return userGroup;
			}

		}
		return null;
	}

	/**
	 * Add a list of users to a group. For testing use only.
	 * 
	 * @param users
	 * @param group
	 * @throws NotConnectedToWebServiceException
	 * @throws IOException
	 * @throws ClientProtocolException
	 * @throws AuthenticationRequired
	 */
	public void addUsersToGroup(List<IUser<Long>> users, IGroup<Long> group) throws ClientProtocolException,
			IOException, NotConnectedToWebServiceException, AuthenticationRequired {
		if (users != null && users.size() > 0 && group != null) {
			checkConnection();
			List<NameValuePair> params = new ArrayList<NameValuePair>();

			String usersId = "";
			if (users.size() > 0) {
				usersId = "[";
			}
			for (int i = 0; i < users.size(); i++) {
				usersId += users.get(i).getId();
				if (i < users.size() - 1) {
					usersId += ",";
				}
			}
			if (usersId.length() > 0) {
				usersId += "]";
			}
			params.add(new BasicNameValuePair("userIds", usersId));
			params.add(new BasicNameValuePair("userGroupId", group.getId() + ""));

			getHttpResponse("user/add-user-group-users", params);
			LiferayClientLogger.info(this.getClass().getName(),
					"Users ids " + usersId + " added to group '" + group.getUniqueName() + "'");

			for (IUser<Long> user : users) {
				groupPool.addUserToGroup(user, group);
			}
		}
	}

	/**
	 * Add a user to a group. For testing use only.
	 * 
	 * @param users
	 * @param group
	 * @throws NotConnectedToWebServiceException
	 * @throws IOException
	 * @throws ClientProtocolException
	 * @throws AuthenticationRequired
	 */
	public void addUserToGroup(IUser<Long> user, IGroup<Long> group) throws NotConnectedToWebServiceException,
			ClientProtocolException, IOException, AuthenticationRequired {
		List<IUser<Long>> users = new ArrayList<IUser<Long>>();
		users.add(user);
		addUsersToGroup(users, group);
	}

	@Override
	public Set<IGroup<Long>> decodeListFromJson(String json, Class<UserGroup> objectClass) throws JsonParseException,
			JsonMappingException, IOException {
		Set<IGroup<Long>> myObjects = new ObjectMapper().readValue(json, new TypeReference<Set<UserGroup>>() {
		});

		return myObjects;
	}

	public void deleteUserFromUserGroup(IUser<Long> user, IGroup<Long> userGroup)
			throws NotConnectedToWebServiceException, ClientProtocolException, IOException, AuthenticationRequired {
		if (user != null && userGroup != null) {
			checkConnection();

			List<NameValuePair> params = new ArrayList<NameValuePair>();
			params.add(new BasicNameValuePair("userGroupId", Long.toString(userGroup.getId())));
			params.add(new BasicNameValuePair("userIds", Long.toString(user.getId())));

			getHttpResponse("user/unset-user-group-users", params);
			groupPool.removeUserFromGroups(user.getId(), userGroup.getId());

			LiferayClientLogger.info(this.getClass().getName(), "User '" + user.getUniqueName() + "' unset from '"
					+ userGroup.getUniqueName() + "'.");

		}
	}

	/**
	 * Removes a group from Liferay portal. For testing use only.
	 * 
	 * @param userGroup
	 * @throws NotConnectedToWebServiceException
	 * @throws IOException
	 * @throws ClientProtocolException
	 * @throws AuthenticationRequired
	 */
	public void deleteUserGroup(IGroup<Long> userGroup) throws NotConnectedToWebServiceException,
			ClientProtocolException, IOException, AuthenticationRequired {
		if (userGroup != null) {
			checkConnection();

			List<NameValuePair> params = new ArrayList<NameValuePair>();
			params.add(new BasicNameValuePair("userGroupId", Long.toString(userGroup.getId())));

			getHttpResponse("usergroup/delete-user-group", params);
			groupPool.removeGroupsById(userGroup.getId());

			LiferayClientLogger.info(this.getClass().getName(), "Group '" + userGroup.getUniqueName() + "' deleted.");

		}
	}

	/**
	 * Removes a group from Liferay portal. For testing use only.
	 * 
	 * @param userGroupId
	 * @throws NotConnectedToWebServiceException
	 * @throws IOException
	 * @throws ClientProtocolException
	 * @throws AuthenticationRequired
	 */
	public void deleteUserGroup(long userGroupId) throws NotConnectedToWebServiceException, ClientProtocolException,
			IOException, AuthenticationRequired {
		if (userGroupId > 0) {
			checkConnection();

			List<NameValuePair> params = new ArrayList<NameValuePair>();
			params.add(new BasicNameValuePair("userGroupId", Long.toString(userGroupId)));

			getHttpResponse("usergroup/delete-user-group", params);
			groupPool.removeGroupsById(userGroupId);

			LiferayClientLogger.info(this.getClass().getName(), "Group with id '" + userGroupId + "' deleted.");

		}
	}

	/**
	 * Get group information using the group's primary key.
	 * 
	 * @param userGroupId
	 *            group's primary key.
	 * @return a group.
	 * @throws NotConnectedToWebServiceException
	 * @throws IOException
	 * @throws ClientProtocolException
	 * @throws AuthenticationRequired
	 * @throws WebServiceAccessError
	 */
	public IGroup<Long> getUserGroup(long userGroupId) throws NotConnectedToWebServiceException,
			UserGroupDoesNotExistException, ClientProtocolException, IOException, AuthenticationRequired,
			WebServiceAccessError {
		if (userGroupId >= 0) {
			// Look up UserSoap in the pool.
			IGroup<Long> group = groupPool.getGroupById(userGroupId);
			if (group != null) {
				return group;
			}

			// Read from Liferay.
			checkConnection();
			List<NameValuePair> params = new ArrayList<NameValuePair>();
			params.add(new BasicNameValuePair("userGroupId", Long.toString(userGroupId)));

			String result = getHttpResponse("usergroup/get-user-group", params);
			if (result != null) {
				// A Simple JSON Response Read
				IGroup<Long> userGroup = decodeFromJson(result, UserGroup.class);
				groupPool.addGroup(userGroup);
				return userGroup;
			}

			throw new UserGroupDoesNotExistException("Group with id '" + userGroupId + "' does not exists.");
		}
		return null;
	}

	/**
	 * Get group information using the group's name.
	 * 
	 * @param name
	 *            name of the group
	 * @return group information
	 * @throws NotConnectedToWebServiceException
	 * @throws IOException
	 * @throws ClientProtocolException
	 * @throws UserGroupDoesNotExistException
	 * @throws AuthenticationRequired
	 * @throws WebServiceAccessError
	 */
	public IGroup<Long> getUserGroup(String name) throws NotConnectedToWebServiceException, ClientProtocolException,
			IOException, UserGroupDoesNotExistException, AuthenticationRequired, WebServiceAccessError {
		if (name != null && name.length() > 0) {
			IGroup<Long> group;
			// Look up in the pool.
			if (groupPool.getElementsByTag(name) != null && !groupPool.getElementsByTag(name).isEmpty()) {
				group = groupPool.getElementsByTag(name).iterator().next();
				if (group != null) {
					return group;
				}
			}

			// Read from Liferay.
			checkConnection();
			List<NameValuePair> params = new ArrayList<NameValuePair>();
			params.add(new BasicNameValuePair("name", name));

			String result = getHttpResponse("usergroup/get-user-group", params);
			if (result != null) {
				// A Simple JSON Response Read
				IGroup<Long> userGroup = decodeFromJson(result, UserGroup.class);
				groupPool.addGroupByTag(userGroup, userGroup.getUniqueName());
				return userGroup;
			}

			throw new UserGroupDoesNotExistException("Group '" + name + "' does not exists.");
		}
		return null;
	}

	/**
	 * Get a list of groups where the User belongs to.
	 * 
	 * @param user
	 * @return group information
	 * @throws NotConnectedToWebServiceException
	 * @throws IOException
	 * @throws ClientProtocolException
	 * @throws AuthenticationRequired
	 */
	public Set<IGroup<Long>> getUserUserGroups(IUser<Long> user) throws NotConnectedToWebServiceException,
			ClientProtocolException, IOException, AuthenticationRequired {
		Set<IGroup<Long>> groups = new HashSet<IGroup<Long>>();

		// Look up UserSoap in the pool.
		if (user != null) {
			Set<IGroup<Long>> usergroups = groupPool.getGroups(user.getId());
			if (usergroups != null) {
				return usergroups;
			}
			checkConnection();

			List<NameValuePair> params = new ArrayList<NameValuePair>();
			params.add(new BasicNameValuePair("userId", Long.toString(user.getId())));

			String result = getHttpResponse("usergroup/get-user-user-groups", params);

			if (result != null) {
				// A Simple JSON Response Read
				groups = decodeListFromJson(result, UserGroup.class);
				groupPool.addUserToGroups(user, groups);
				return groups;
			}
		}
		return groups;
	}

	public void reset() {
		groupPool.reset();
	}

}
