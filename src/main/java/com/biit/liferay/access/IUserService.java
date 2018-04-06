package com.biit.liferay.access;

import java.io.IOException;
import java.util.List;
import java.util.Set;

import org.apache.http.client.ClientProtocolException;

import com.biit.liferay.access.exceptions.DuplicatedLiferayElement;
import com.biit.liferay.access.exceptions.NotConnectedToWebServiceException;
import com.biit.liferay.access.exceptions.WebServiceAccessError;
import com.biit.usermanager.entity.IGroup;
import com.biit.usermanager.entity.IUser;
import com.biit.usermanager.security.exceptions.AuthenticationRequired;
import com.biit.usermanager.security.exceptions.UserDoesNotExistException;
import com.liferay.portal.model.Company;
import com.liferay.portal.model.Status;
import com.liferay.portal.model.User;

public interface IUserService extends IServiceAccess {

	List<IUser<Long>> getUsers(Long roleId) throws ClientProtocolException, IOException, NotConnectedToWebServiceException, AuthenticationRequired,
			WebServiceAccessError;

	IUser<Long> getUserByScreenName(Company company, String screenName) throws NotConnectedToWebServiceException, ClientProtocolException, IOException,
			AuthenticationRequired, WebServiceAccessError, UserDoesNotExistException;

	IUser<Long> getUserById(long userId) throws NotConnectedToWebServiceException, UserDoesNotExistException, ClientProtocolException, IOException,
			AuthenticationRequired, WebServiceAccessError;

	IUser<Long> getUserByEmailAddress(IGroup<Long> company, String emailAddress) throws NotConnectedToWebServiceException, ClientProtocolException,
			IOException, AuthenticationRequired, WebServiceAccessError, UserDoesNotExistException;

	void deleteUser(IUser<Long> user) throws NotConnectedToWebServiceException, ClientProtocolException, IOException, AuthenticationRequired;

	IUser<Long> addUser(IGroup<Long> company, String password, String screenName, String emailAddress, long facebookId, String openId, String locale,
			String firstName, String middleName, String lastName, int prefixId, int suffixId, boolean male, int birthdayDay, int birthdayMonth,
			int birthdayYear, String jobTitle, long[] groupIds, long[] organizationIds, long[] roleIds, long[] userGroupIds, boolean sendEmail)
			throws NotConnectedToWebServiceException, ClientProtocolException, IOException, AuthenticationRequired, WebServiceAccessError,
			DuplicatedLiferayElement;

	IUser<Long> addUser(IGroup<Long> company, User user) throws NotConnectedToWebServiceException, ClientProtocolException, IOException,
			AuthenticationRequired, WebServiceAccessError, DuplicatedLiferayElement;

	Set<IUser<Long>> getCompanyUsers(IGroup<Long> company) throws ClientProtocolException, IOException, NotConnectedToWebServiceException,
			AuthenticationRequired;

	IUser<Long> updateUser(User user) throws NotConnectedToWebServiceException, ClientProtocolException, IOException, AuthenticationRequired,
			WebServiceAccessError;

	IUser<Long> updateStatus(IUser<Long> user, Status status) throws NotConnectedToWebServiceException, ClientProtocolException, IOException,
			AuthenticationRequired, WebServiceAccessError;

}
