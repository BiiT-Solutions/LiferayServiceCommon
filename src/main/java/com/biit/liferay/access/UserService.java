package com.biit.liferay.access;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.message.BasicNameValuePair;

import com.biit.liferay.access.exceptions.NotConnectedToWebServiceException;
import com.biit.liferay.access.exceptions.UserDoesNotExistException;
import com.biit.liferay.access.exceptions.WebServiceAccessError;
import com.biit.liferay.log.LiferayClientLogger;
import com.biit.usermanager.entity.IGroup;
import com.biit.usermanager.entity.IUser;
import com.biit.usermanager.entity.pool.GroupPool;
import com.biit.usermanager.entity.pool.UserPool;
import com.biit.usermanager.security.exceptions.AuthenticationRequired;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.liferay.portal.model.Company;
import com.liferay.portal.model.Contact;
import com.liferay.portal.model.Status;
import com.liferay.portal.model.User;

/**
 * This class allows to manage users from Liferay portal.
 */
public class UserService extends ServiceAccess<IUser<Long>, User> {
	private ContactService contactService;
	private OrganizationService organizationService;

	private final UserPool<Long, Long> userPool;
	private final GroupPool<Long, Long> groupPool;

	public UserService() {
		userPool = new UserPool<Long, Long>();
		groupPool = new GroupPool<Long, Long>();
	}

	/**
	 * Adds an user to Liferay portal.
	 * 
	 * @param company
	 * @param user
	 * @return a user.
	 * @throws NotConnectedToWebServiceException
	 * @throws IOException
	 * @throws ClientProtocolException
	 * @throws AuthenticationRequired
	 * @throws WebServiceAccessError
	 */
	public IUser<Long> addUser(Company company, User user) throws NotConnectedToWebServiceException, ClientProtocolException, IOException,
			AuthenticationRequired, WebServiceAccessError {
		if (user != null) {
			return addUser(company, user.getPassword(), user.getScreenName(), user.getEmailAddress(), user.getFacebookId(), user.getOpenId(),
					user.getTimeZoneId(), user.getFirstName(), user.getMiddleName(), user.getLastName(), 0, 0, true, 1, 1, 1900, user.getJobTitle(),
					new long[0], new long[0], new long[0], new long[0], false);
		}
		return null;
	}

	/**
	 * Creates an user into liferay portal. If password and/or screenname are
	 * not set, they will be auto-generated.
	 * 
	 * @param companySoap
	 * @param password
	 * @param screenName
	 * @param emailAddress
	 * @param facebookId
	 * @param openId
	 * @param locale
	 * @param firstName
	 * @param middleName
	 * @param lastName
	 * @return a user.
	 * @throws NotConnectedToWebServiceException
	 * @throws IOException
	 * @throws ClientProtocolException
	 * @throws AuthenticationRequired
	 * @throws WebServiceAccessError
	 */
	public IUser<Long> addUser(IGroup<Long> company, String password, String screenName, String emailAddress, long facebookId, String openId, String locale,
			String firstName, String middleName, String lastName, int prefixId, int suffixId, boolean male, int birthdayDay, int birthdayMonth,
			int birthdayYear, String jobTitle, long[] groupIds, long[] organizationIds, long[] roleIds, long[] userGroupIds, boolean sendEmail)
			throws NotConnectedToWebServiceException, ClientProtocolException, IOException, AuthenticationRequired, WebServiceAccessError {
		checkConnection();
		boolean autoPassword = false;
		boolean autoScreenName = false;
		if (password == null || password.length() == 0) {
			autoPassword = true;
		}
		if (screenName == null || screenName.length() == 0) {
			autoScreenName = true;
		}

		List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("companyId", Long.toString(company.getId())));
		params.add(new BasicNameValuePair("autoPassword", Boolean.toString(autoPassword)));
		params.add(new BasicNameValuePair("password1", password));
		params.add(new BasicNameValuePair("password2", password));
		params.add(new BasicNameValuePair("autoScreenName", Boolean.toString(autoScreenName)));
		params.add(new BasicNameValuePair("screenName", screenName));
		params.add(new BasicNameValuePair("emailAddress", emailAddress));
		params.add(new BasicNameValuePair("facebookId", Long.toString(facebookId)));
		params.add(new BasicNameValuePair("openId", openId));
		params.add(new BasicNameValuePair("locale", locale));
		params.add(new BasicNameValuePair("firstName", firstName));
		params.add(new BasicNameValuePair("middleName", middleName));
		params.add(new BasicNameValuePair("lastName", lastName));
		params.add(new BasicNameValuePair("prefixId", Integer.toString(prefixId)));
		params.add(new BasicNameValuePair("suffixId", Integer.toString(suffixId)));
		params.add(new BasicNameValuePair("male", Boolean.toString(male)));
		params.add(new BasicNameValuePair("birthdayMonth", Integer.toString(birthdayMonth)));
		params.add(new BasicNameValuePair("birthdayDay", Integer.toString(birthdayDay)));
		params.add(new BasicNameValuePair("birthdayYear", Integer.toString(birthdayYear)));
		params.add(new BasicNameValuePair("jobTitle", jobTitle));
		params.add(new BasicNameValuePair("groupIds", Arrays.toString(groupIds)));
		params.add(new BasicNameValuePair("organizationIds", Arrays.toString(organizationIds)));
		params.add(new BasicNameValuePair("roleIds", Arrays.toString(roleIds)));
		params.add(new BasicNameValuePair("userGroupIds", Arrays.toString(userGroupIds)));
		params.add(new BasicNameValuePair("sendEmail", Boolean.toString(sendEmail)));

		String result = getHttpResponse("user/add-user", params);
		IUser<Long> user = null;
		if (result != null) {
			// A Simple JSON Response Read
			user = decodeFromJson(result, User.class);
			userPool.addUser(user);
			LiferayClientLogger.info(this.getClass().getName(), "IUser<Long> '" + user.getUniqueName() + "' added.");
			return user;
		}

		return user;
	}

	@Override
	public void authorizedServerConnection(String address, String protocol, int port, String webservicesPath, String authenticationToken, String loginUser,
			String password) {
		// Standard behavior.
		super.authorizedServerConnection(address, protocol, port, webservicesPath, authenticationToken, loginUser, password);
		// Disconnect previous connections.
		try {
			contactService.disconnect();
			organizationService.disconnect();
		} catch (Exception e) {

		}
		// Some user information is in the contact object.
		contactService = new ContactService();
		contactService.authorizedServerConnection(address, protocol, port, webservicesPath, authenticationToken, loginUser, password);
		// A service is a mix of the organization service and the user service.
		organizationService = new OrganizationService();
		organizationService.authorizedServerConnection(address, protocol, port, webservicesPath, authenticationToken, loginUser, password);
	}

	@Override
	public Set<IUser<Long>> decodeListFromJson(String json, Class<User> objectClass) throws JsonParseException, JsonMappingException, IOException {
		Set<IUser<Long>> myObjects = new ObjectMapper().readValue(json, new TypeReference<Set<User>>() {
		});

		return myObjects;
	}

	public List<Long> decodeLongListFromJson(String json, Class<Long> objectClass) throws JsonParseException, JsonMappingException, IOException {
		List<Long> myObjects = new ObjectMapper().readValue(json, new TypeReference<List<Long>>() {
		});

		return myObjects;
	}

	/**
	 * Deletes an user from the database.
	 * 
	 * @param user
	 * @throws NotConnectedToWebServiceException
	 * @throws IOException
	 * @throws ClientProtocolException
	 * @throws AuthenticationRequired
	 */
	public void deleteUser(IUser<Long> user) throws NotConnectedToWebServiceException, ClientProtocolException, IOException, AuthenticationRequired {
		if (user != null) {
			checkConnection();

			List<NameValuePair> params = new ArrayList<NameValuePair>();
			params.add(new BasicNameValuePair("userId", user.getId() + ""));

			getHttpResponse("user/delete-user", params);

			userPool.removeUser(user);
			LiferayClientLogger.info(this.getClass().getName(), "IUser<Long> '" + user.getUniqueName() + "' deleted.");
		}
	}

	@Override
	public void disconnect() {
		super.disconnect();
		contactService.disconnect();
		organizationService.disconnect();
	}

	public Set<IUser<Long>> getCompanyUsers(IGroup<Long> company) throws ClientProtocolException, IOException, NotConnectedToWebServiceException,
			AuthenticationRequired {
		Set<IUser<Long>> users = new HashSet<IUser<Long>>();
		if (company != null) {

			Set<IUser<Long>> usersOfCompany = groupPool.getGroupUsers(company.getId());
			if (usersOfCompany != null) {
				return usersOfCompany;
			}

			checkConnection();

			List<NameValuePair> params = new ArrayList<NameValuePair>();
			params.add(new BasicNameValuePair("companyId", company.getId() + ""));
			params.add(new BasicNameValuePair("start", 0 + ""));
			params.add(new BasicNameValuePair("end", Integer.MAX_VALUE + ""));
			String result = getHttpResponse("user/get-company-users", params);
			if (result != null) {
				// A Simple JSON Response Read
				users = decodeListFromJson(result, User.class);
				groupPool.addGroupUsers(company.getId(), users);
				return users;
			}
		}
		return users;
	}

	/**
	 * Get user information using the email as identificator.
	 * 
	 * @param company
	 *            liferay portal where look up for.
	 * @param emailAddress
	 *            the email of the user.
	 * @return a user.
	 * @throws NotConnectedToWebServiceException
	 * @throws IOException
	 * @throws ClientProtocolException
	 * @throws AuthenticationRequired
	 * @throws WebServiceAccessError
	 */
	public IUser<Long> getUserByEmailAddress(IGroup<Long> company, String emailAddress) throws NotConnectedToWebServiceException, ClientProtocolException,
			IOException, AuthenticationRequired, WebServiceAccessError {
		if (company != null && emailAddress != null) {
			emailAddress = emailAddress.toLowerCase();
			// Look up user in the pool.
			IUser<Long> user = userPool.getUserByEmailAddress(emailAddress);
			if (user != null) {
				return user;
			}

			// Look up user in the liferay.
			checkConnection();

			List<NameValuePair> params = new ArrayList<NameValuePair>();
			params.add(new BasicNameValuePair("companyId", company.getId() + ""));
			params.add(new BasicNameValuePair("emailAddress", emailAddress));

			String result = getHttpResponse("user/get-user-by-email-address", params);
			if (result != null) {
				// A Simple JSON Response Read
				user = decodeFromJson(result, User.class);
				userPool.addUser(user);
				updateContactInformation(user);
				return user;
			}
		}
		return null;

	}

	/**
	 * Get user information using the user's primary key.
	 * 
	 * @param userId
	 *            user's primary key.
	 * @return a user.
	 * @throws NotConnectedToWebServiceException
	 * @throws IOException
	 * @throws ClientProtocolException
	 * @throws AuthenticationRequired
	 * @throws WebServiceAccessError
	 */
	public IUser<Long> getUserById(long userId) throws NotConnectedToWebServiceException, UserDoesNotExistException, ClientProtocolException, IOException,
			AuthenticationRequired, WebServiceAccessError {
		if (userId >= 0) {
			// Look up user in the pool.
			IUser<Long> user = userPool.getUserById(userId);
			if (user != null) {
				return user;
			}

			// Read from Liferay.
			checkConnection();

			List<NameValuePair> params = new ArrayList<NameValuePair>();
			params.add(new BasicNameValuePair("userId", Long.toString(userId)));

			String result = getHttpResponse("user/get-user-by-id", params);
			if (result != null) {
				// A Simple JSON Response Read
				user = decodeFromJson(result, User.class);
				userPool.addUser(user);
				updateContactInformation(user);
				return user;
			}
		}
		return null;
	}

	/**
	 * Get user information using the user's primary key.
	 * 
	 * @param company
	 *            liferay portal where look up for.
	 * @param screenName
	 *            is a unique token that identifies a liferay user from another,
	 *            so two users cannot use the same screenname.
	 * @return a user.
	 * @throws NotConnectedToWebServiceException
	 * @throws IOException
	 * @throws ClientProtocolException
	 * @throws AuthenticationRequired
	 * @throws WebServiceAccessError
	 */
	public IUser<Long> getUserByScreenName(Company company, String screenName) throws NotConnectedToWebServiceException, ClientProtocolException, IOException,
			AuthenticationRequired, WebServiceAccessError {
		screenName = screenName.toLowerCase();

		// Look up user in the pool.
		IUser<Long> user = userPool.getUserByScreenName(screenName);
		if (user != null) {
			return user;
		}

		// Read from liferay.
		checkConnection();

		List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("companyId", company.getCompanyId() + ""));
		params.add(new BasicNameValuePair("screenName", screenName));

		String result = getHttpResponse("user/get-user-by-screen-name", params);
		if (result != null) {
			// A Simple JSON Response Read
			user = decodeFromJson(result, User.class);
			userPool.addUser(user);
			updateContactInformation(user);
			return user;
		}

		return null;
	}

	/**
	 * Gets all users that have a specific standard role. Not valid for
	 * organization roles.
	 * 
	 * @param roleId
	 * @return
	 * @throws ClientProtocolException
	 * @throws IOException
	 * @throws NotConnectedToWebServiceException
	 * @throws AuthenticationRequired
	 * @throws WebServiceAccessError
	 */
	public List<IUser<Long>> getUsers(Long roleId) throws ClientProtocolException, IOException, NotConnectedToWebServiceException, AuthenticationRequired,
			WebServiceAccessError {
		List<IUser<Long>> users = new ArrayList<IUser<Long>>();
		List<IUser<Long>> usersOfRoles = userPool.getUsersOfRole(roleId);
		if (usersOfRoles != null) {
			return usersOfRoles;
		}
		checkConnection();

		List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("roleId", roleId + ""));
		String result = getHttpResponse("user/get-role-user-ids", params);
		if (result != null) {
			// A Simple JSON Response Read
			List<Long> usersIds = decodeLongListFromJson(result, Long.class);
			usersOfRoles = new ArrayList<IUser<Long>>();

			for (Long id : usersIds) {
				try {
					usersOfRoles.add(getUserById(id));
				} catch (UserDoesNotExistException e) {
					// Impossible. Comes from Liferay.
					LiferayClientLogger.errorMessage(this.getClass().getName(), e);
				}
			}

			userPool.addUsersOfRole(roleId, usersOfRoles);
			return usersOfRoles;
		}
		return users;
	}

	public void reset() {
		userPool.reset();
		groupPool.reset();
		contactService.reset();
		organizationService.reset();
	}

	/**
	 * Some user's information in Liferay is in the contact object. We copy it
	 * to the user object.
	 * 
	 * @param user
	 * @throws WebServiceAccessError
	 * @throws AuthenticationRequired
	 * @throws IOException
	 * @throws NotConnectedToWebServiceException
	 * @throws ClientProtocolException
	 */
	private void updateContactInformation(IUser<Long> user) throws ClientProtocolException, NotConnectedToWebServiceException, IOException,
			AuthenticationRequired, WebServiceAccessError {
		Contact contact = contactService.getContact((User) user);
		((User) user).setBirthday(contact.getBirthday());
		((User) user).setMale(contact.isMale());
	}

	public IUser<Long> updateUser(User user) throws NotConnectedToWebServiceException, ClientProtocolException, IOException, AuthenticationRequired,
			WebServiceAccessError {
		return updateUser(user, contactService.getContact((User) user));
	}

	public IUser<Long> updateStatus(IUser<Long> user, Status status) throws NotConnectedToWebServiceException, ClientProtocolException, IOException,
			AuthenticationRequired, WebServiceAccessError {
		return updateStatus(user, status.getValue());
	}

	public IUser<Long> updateStatus(IUser<Long> user, int status) throws NotConnectedToWebServiceException, ClientProtocolException, IOException,
			AuthenticationRequired, WebServiceAccessError {
		checkConnection();

		List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("userId", Long.toString(user.getId())));
		params.add(new BasicNameValuePair("status", Integer.toString(status)));

		String result = getHttpResponse("user/update-status", params);
		IUser<Long> returnedUser = null;
		if (result != null) {
			// A Simple JSON Response Read
			returnedUser = decodeFromJson(result, User.class);
			userPool.addUser(returnedUser);
			LiferayClientLogger.info(this.getClass().getName(), "IUser<Long> '" + returnedUser.getUniqueName() + "' status updated to '"
					+ ((User) returnedUser).getStatus() + "'.");
			return returnedUser;
		}

		return returnedUser;
	}

	private IUser<Long> updateUser(User user, Contact contact) throws NotConnectedToWebServiceException, ClientProtocolException, IOException,
			AuthenticationRequired, WebServiceAccessError {
		checkConnection();

		int previousStatus = user.getStatus();

		// For updating, the status must be INCOMPLETE
		updateStatus(user, Status.INCOMPLETE);

		boolean autoPassword = false;
		boolean autoScreenName = false;
		if (user.getPassword() == null || user.getPassword().length() == 0) {
			autoPassword = true;
		}
		if (user.getScreenName() == null || user.getScreenName().length() == 0) {
			autoScreenName = true;
		}

		List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("companyId", Long.toString(user.getCompanyId())));
		params.add(new BasicNameValuePair("autoPassword", Boolean.toString(autoPassword)));
		params.add(new BasicNameValuePair("password1", user.getPassword()));
		params.add(new BasicNameValuePair("password2", user.getPassword()));
		params.add(new BasicNameValuePair("autoScreenName", Boolean.toString(autoScreenName)));
		params.add(new BasicNameValuePair("screenName", user.getScreenName()));
		params.add(new BasicNameValuePair("emailAddress", user.getEmailAddress()));
		params.add(new BasicNameValuePair("facebookId", Long.toString(((User) user).getFacebookId())));
		params.add(new BasicNameValuePair("openId", ((User) user).getOpenId()));
		params.add(new BasicNameValuePair("locale", user.getLocale().toString()));
		params.add(new BasicNameValuePair("firstName", user.getFirstName()));
		params.add(new BasicNameValuePair("middleName", ((User) user).getMiddleName()));
		params.add(new BasicNameValuePair("lastName", user.getLastName()));
		params.add(new BasicNameValuePair("prefixId", Integer.toString(contact.getPrefixId())));
		params.add(new BasicNameValuePair("suffixId", Integer.toString(contact.getSuffixId())));
		params.add(new BasicNameValuePair("male", Boolean.toString(((User) user).isMale())));
		params.add(new BasicNameValuePair("jobTitle", ((User) user).getJobTitle()));
		params.add(new BasicNameValuePair("updateUserInformation", Boolean.toString(true)));
		params.add(new BasicNameValuePair("sendEmail", Boolean.toString(false)));

		Calendar cal = Calendar.getInstance();
		if (((User) user).getBirthday() != null) {
			cal.setTime(((User) user).getBirthday());
		}
		int month = cal.get(Calendar.MONTH);
		int day = cal.get(Calendar.DAY_OF_MONTH);
		int year = cal.get(Calendar.YEAR);
		params.add(new BasicNameValuePair("birthdayMonth", Integer.toString(month)));
		params.add(new BasicNameValuePair("birthdayDay", Integer.toString(day)));
		params.add(new BasicNameValuePair("birthdayYear", Integer.toString(year)));

		String result = getHttpResponse("user/update-incomplete-user", params);
		User returnedUser = null;
		if (result != null) {
			// A Simple JSON Response Read
			returnedUser = (User) decodeFromJson(result, User.class);
			userPool.addUser(returnedUser);
			LiferayClientLogger.info(this.getClass().getName(), "IUser<Long> '" + returnedUser.getUniqueName() + "' updated.");
			updateStatus(returnedUser, previousStatus);
			return returnedUser;
		}

		return returnedUser;
	}
}
