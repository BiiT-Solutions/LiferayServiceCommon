package com.biit.liferay.access;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.message.BasicNameValuePair;

import com.biit.liferay.access.exceptions.AuthenticationRequired;
import com.biit.liferay.access.exceptions.NotConnectedToWebServiceException;
import com.biit.liferay.access.exceptions.UserDoesNotExistException;
import com.biit.liferay.access.exceptions.WebServiceAccessError;
import com.biit.liferay.log.LiferayClientLogger;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.liferay.portal.model.Company;
import com.liferay.portal.model.Contact;
import com.liferay.portal.model.User;

/**
 * This class allows to manage users from Liferay portal.
 */
public class UserService extends ServiceAccess<User> {
	private ContactService contactService;
	private OrganizationService organizationService;

	public UserService() {
	}

	@Override
	public void authorizedServerConnection(String address, String protocol, int port, String webservicesPath,
			String authenticationToken, String loginUser, String password) {
		// Standard behavior.
		super.authorizedServerConnection(address, protocol, port, webservicesPath, authenticationToken, loginUser,
				password);
		// Disconnect previous connections.
		try {
			contactService.disconnect();
			organizationService.disconnect();
		} catch (Exception e) {

		}
		// Some user information is in the contact object.
		contactService = new ContactService();
		contactService.authorizedServerConnection(address, protocol, port, webservicesPath, authenticationToken,
				loginUser, password);
		// A service is a mix of the organization service and the user service.
		organizationService = new OrganizationService();
		organizationService.authorizedServerConnection(address, protocol, port, webservicesPath, authenticationToken,
				loginUser, password);
	}

	@Override
	public void disconnect() {
		super.disconnect();
		contactService.disconnect();
		organizationService.disconnect();
	}

	/**
	 * Creates an user into liferay portal. If password and/or screenname are not set, they will be auto-generated.
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
	public User addUser(Company company, String password, String screenName, String emailAddress, long facebookId,
			String openId, String locale, String firstName, String middleName, String lastName, int prefixId,
			int suffixId, boolean male, int birthdayDay, int birthdayMonth, int birthdayYear, String jobTitle,
			long[] groupIds, long[] organizationIds, long[] roleIds, long[] userGroupIds, boolean sendEmail)
			throws NotConnectedToWebServiceException, ClientProtocolException, IOException, AuthenticationRequired,
			WebServiceAccessError {
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
		params.add(new BasicNameValuePair("companyId", Long.toString(company.getCompanyId())));
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
		User user = null;
		if (result != null) {
			// A Simple JSON Response Read
			user = decodeFromJson(result, User.class);
			UserPool.getInstance().addUser(user);
			LiferayClientLogger.info(this.getClass().getName(), "User '" + user.getScreenName() + "' added.");
			return user;
		}

		return user;
	}

	/**
	 * Adds an user to Liferay portal.
	 * 
	 * @param companySoap
	 * @param user
	 * @return a user.
	 * @throws NotConnectedToWebServiceException
	 * @throws IOException
	 * @throws ClientProtocolException
	 * @throws AuthenticationRequired
	 * @throws WebServiceAccessError
	 */
	public User addUser(Company companySoap, User user) throws NotConnectedToWebServiceException,
			ClientProtocolException, IOException, AuthenticationRequired, WebServiceAccessError {
		if (user != null) {
			return addUser(companySoap, user.getPassword(), user.getScreenName(), user.getEmailAddress(),
					user.getFacebookId(), user.getOpenId(), user.getTimeZoneId(), user.getFirstName(),
					user.getMiddleName(), user.getLastName(), 0, 0, true, 1, 1, 1900, user.getJobTitle(), new long[0],
					new long[0], new long[0], new long[0], false);
		}
		return null;
	}

	@Override
	public List<User> decodeListFromJson(String json, Class<User> objectClass) throws JsonParseException,
			JsonMappingException, IOException {
		List<User> myObjects = new ObjectMapper().readValue(json, new TypeReference<List<User>>() {
		});

		return myObjects;
	}

	public List<Long> decodeLongListFromJson(String json, Class<Long> objectClass) throws JsonParseException,
			JsonMappingException, IOException {
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
	public void deleteUser(User user) throws NotConnectedToWebServiceException, ClientProtocolException, IOException,
			AuthenticationRequired {
		if (user != null) {
			checkConnection();

			List<NameValuePair> params = new ArrayList<NameValuePair>();
			params.add(new BasicNameValuePair("userId", user.getUserId() + ""));

			getHttpResponse("user/delete-user", params);

			UserPool.getInstance().removeUser(user);
			LiferayClientLogger.info(this.getClass().getName(), "User '" + user.getScreenName() + "' deleted.");
		}
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
	public User getUserByEmailAddress(Company company, String emailAddress) throws NotConnectedToWebServiceException,
			ClientProtocolException, IOException, AuthenticationRequired, WebServiceAccessError {
		if (company != null && emailAddress != null) {
			emailAddress = emailAddress.toLowerCase();
			// Look up user in the pool.
			User user = UserPool.getInstance().getUserByEmailAddress(emailAddress);
			if (user != null) {
				return user;
			}

			// Look up user in the liferay.
			checkConnection();

			List<NameValuePair> params = new ArrayList<NameValuePair>();
			params.add(new BasicNameValuePair("companyId", company.getCompanyId() + ""));
			params.add(new BasicNameValuePair("emailAddress", emailAddress));

			String result = getHttpResponse("user/get-user-by-email-address", params);
			if (result != null) {
				// A Simple JSON Response Read
				user = decodeFromJson(result, User.class);
				UserPool.getInstance().addUser(user);
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
	public User getUserById(long userId) throws NotConnectedToWebServiceException, UserDoesNotExistException,
			ClientProtocolException, IOException, AuthenticationRequired, WebServiceAccessError {
		if (userId >= 0) {
			// Look up user in the pool.
			User user = UserPool.getInstance().getUserById(userId);
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
				UserPool.getInstance().addUser(user);
				updateContactInformation(user);
				return user;
			}
		}
		return null;
	}

	/**
	 * Get user information using the user's primary key.
	 * 
	 * @param companySoap
	 *            liferay portal where look up for.
	 * @param screenName
	 *            is a unique token that identifies a liferay user from another, so two users cannot use the same
	 *            screenname.
	 * @return a user.
	 * @throws NotConnectedToWebServiceException
	 * @throws IOException
	 * @throws ClientProtocolException
	 * @throws AuthenticationRequired
	 * @throws WebServiceAccessError
	 */
	public User getUserByScreenName(Company companySoap, String screenName) throws NotConnectedToWebServiceException,
			ClientProtocolException, IOException, AuthenticationRequired, WebServiceAccessError {
		screenName = screenName.toLowerCase();

		// Look up user in the pool.
		User user = UserPool.getInstance().getUserByScreenName(screenName);
		if (user != null) {
			return user;
		}

		// Read from liferay.
		checkConnection();

		List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("companyId", companySoap.getCompanyId() + ""));
		params.add(new BasicNameValuePair("screenName", screenName));

		String result = getHttpResponse("user/get-user-by-screen-name", params);
		if (result != null) {
			// A Simple JSON Response Read
			user = decodeFromJson(result, User.class);
			UserPool.getInstance().addUser(user);
			updateContactInformation(user);
			return user;
		}

		return null;
	}

	/**
	 * Gets all users that have a specific role.
	 * 
	 * @param roleId
	 * @return
	 * @throws ClientProtocolException
	 * @throws IOException
	 * @throws NotConnectedToWebServiceException
	 * @throws AuthenticationRequired
	 * @throws WebServiceAccessError
	 */
	public List<User> getUsers(long roleId) throws ClientProtocolException, IOException,
			NotConnectedToWebServiceException, AuthenticationRequired, WebServiceAccessError {
		List<User> users = new ArrayList<User>();
		List<User> usersOfRoles = UserPool.getInstance().getUsersOfRole(roleId);
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
			usersOfRoles = new ArrayList<User>();

			for (Long id : usersIds) {
				try {
					usersOfRoles.add(getUserById(id));
				} catch (UserDoesNotExistException e) {
					// Impossible. Comes from Liferay.
					LiferayClientLogger.errorMessage(this.getClass().getName(), e);
				}
			}

			UserPool.getInstance().addUsersOfRole(roleId, usersOfRoles);
			return usersOfRoles;
		}
		return users;
	}

	/**
	 * Gets all users that have a specific role in an organization.
	 * 
	 * @throws WebServiceAccessError
	 * @throws AuthenticationRequired
	 * @throws NotConnectedToWebServiceException
	 * @throws IOException
	 * @throws ClientProtocolException
	 */
	public List<User> getUsers(long roleId, long organizationId) throws ClientProtocolException, IOException,
			NotConnectedToWebServiceException, AuthenticationRequired, WebServiceAccessError {
		List<User> usersOfRole = getUsers(roleId);
		List<User> usersOfOrganization = organizationService.getOrganizationUsers(organizationId);

		// Intersection of lists.
		usersOfOrganization.retainAll(usersOfRole);

		return usersOfOrganization;
	}

	/**
	 * Some user's information in Liferay is in the contact object. We copy it to the user object.
	 * 
	 * @param user
	 * @throws WebServiceAccessError
	 * @throws AuthenticationRequired
	 * @throws IOException
	 * @throws NotConnectedToWebServiceException
	 * @throws ClientProtocolException
	 */
	private void updateContactInformation(User user) throws ClientProtocolException, NotConnectedToWebServiceException,
			IOException, AuthenticationRequired, WebServiceAccessError {
		Contact contact = contactService.getContact(user);
		user.setBirthday(contact.getBirthday());
		user.setMale(contact.isMale());
	}
}
