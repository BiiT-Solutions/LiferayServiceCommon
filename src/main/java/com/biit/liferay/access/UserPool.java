package com.biit.liferay.access;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.liferay.portal.model.User;

public class UserPool {

	private final static long EXPIRATION_TIME = 300000;// 5 minutes

	private Map<Long, Long> time; // User id -> time.
	private Map<Long, User> users; // User id -> User.

	private Map<Long, List<User>> usersOfRole;
	private Map<Long, Long> usersOfRoleTime; // User id -> time.

	private Map<Long, List<User>> usersOfCompany;
	private Map<Long, Long> usersOfCompanyTime; // User id -> time.

	private static UserPool instance = new UserPool();

	public static UserPool getInstance() {
		return instance;
	}

	private UserPool() {
		reset();
	}

	public void reset() {
		time = new HashMap<Long, Long>();
		users = new HashMap<Long, User>();
		usersOfRole = new HashMap<Long, List<User>>();
		usersOfRoleTime = new HashMap<Long, Long>();
		usersOfCompany = new HashMap<Long, List<User>>();
		usersOfCompanyTime = new HashMap<Long, Long>();
	}

	public void addUser(User user) {
		if (user != null) {
			time.put(user.getUserId(), System.currentTimeMillis());
			users.put(user.getUserId(), user);
		}
	}

	public User getUserByEmailAddress(String emailAddress) {
		long now = System.currentTimeMillis();
		Long userId = null;
		if (time.size() > 0) {
			Iterator<Long> e = new HashMap<Long, Long>(time).keySet().iterator();
			while (e.hasNext()) {
				userId = e.next();
				if ((now - time.get(userId)) > EXPIRATION_TIME) {
					// object has expired
					removeUser(userId);
					userId = null;
				} else {
					if (users.get(userId) != null && users.get(userId).getEmailAddress().equals(emailAddress)) {
						return users.get(userId);
					}
				}
			}
		}
		return null;
	}

	public User getUserById(long userId) {
		long now = System.currentTimeMillis();
		Long storedObject = null;
		if (time.size() > 0) {
			Iterator<Long> e = new HashMap<Long, Long>(time).keySet().iterator();
			while (e.hasNext()) {
				storedObject = e.next();
				if ((now - time.get(storedObject)) > EXPIRATION_TIME) {
					// object has expired
					removeUser(storedObject);
					storedObject = null;
				} else {
					if (users.get(storedObject) != null && users.get(storedObject).getUserId() == userId) {
						return users.get(userId);
					}
				}
			}
		}
		return null;
	}

	public User getUserByScreenName(String screenName) {
		long now = System.currentTimeMillis();
		Long userId = null;
		if (time.size() > 0) {
			Iterator<Long> e = new HashMap<Long, Long>(time).keySet().iterator();
			while (e.hasNext()) {
				userId = e.next();
				if ((now - time.get(userId)) > EXPIRATION_TIME) {
					// object has expired
					removeUser(userId);
					userId = null;
				} else {
					if (users.get(userId) != null && users.get(userId).getScreenName().equals(screenName)) {
						return users.get(userId);
					}
				}
			}
		}
		return null;
	}

	public void removeUser(long userId) {
		time.remove(userId);
		users.remove(userId);
	}

	public void removeUser(User user) {
		if (user != null) {
			removeUser(user.getUserId());
		}
	}

	public List<User> getUsersOfRole(Long roleId) {
		long now = System.currentTimeMillis();
		Long storedObject = null;
		if (usersOfRoleTime.size() > 0) {
			Iterator<Long> e = new HashMap<Long, Long>(usersOfRoleTime).keySet().iterator();
			while (e.hasNext()) {
				storedObject = e.next();
				if ((now - usersOfRoleTime.get(storedObject)) > EXPIRATION_TIME) {
					// object has expired
					removeUsersOfRole(storedObject);
					storedObject = null;
				} else {
					if (storedObject.equals(roleId)) {
						return usersOfRole.get(storedObject);
					}
				}
			}
		}
		return null;
	}

	public void addUsersOfRole(long roleId, List<User> usersOfRoles) {
		if (usersOfRoles != null) {
			usersOfRole.put(roleId, usersOfRoles);
			usersOfRoleTime.put(roleId, System.currentTimeMillis());
		}
	}

	public void removeUsersOfRole(long roleId) {
		usersOfRole.remove(roleId);
		usersOfRoleTime.remove(roleId);
	}

	public List<User> getUsersOfCompany(Long companyId) {
		long now = System.currentTimeMillis();
		Long storedObject = null;
		if (usersOfCompanyTime.size() > 0) {
			Iterator<Long> e = new HashMap<Long, Long>(usersOfCompanyTime).keySet().iterator();
			while (e.hasNext()) {
				storedObject = e.next();
				if ((now - usersOfCompanyTime.get(storedObject)) > EXPIRATION_TIME) {
					// object has expired
					removeUsersOfCompany(storedObject);
					storedObject = null;
				} else {
					if (storedObject.equals(companyId)) {
						return usersOfCompany.get(storedObject);
					}
				}
			}
		}
		return null;
	}

	public void addUsersOfCompany(long companyId, List<User> users) {
		if (users != null) {
			usersOfCompany.put(companyId, users);
			usersOfCompanyTime.put(companyId, System.currentTimeMillis());
		}
	}

	public void removeUsersOfCompany(long companyId) {
		usersOfCompany.remove(companyId);
		usersOfCompanyTime.remove(companyId);
	}
}
