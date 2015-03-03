package com.biit.liferay.access;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;

import com.liferay.portal.model.Company;
import com.liferay.portal.model.Group;
import com.liferay.portal.model.Organization;
import com.liferay.portal.model.Site;
import com.liferay.portal.model.User;

public class OrganizationPool {

	private final static long EXPIRATION_TIME = 300000;// 5 minutes

	private Hashtable<Long, Long> time; // Company id -> time.
	// Company id -> Organizations.
	private Hashtable<Long, List<Organization>> organizationsByCompany;

	// Organizations by id;
	private Hashtable<Long, Long> organizationTime; // user id -> time.
	private Hashtable<Long, Organization> organizationsById;

	private Hashtable<Long, Long> userTime; // user id -> time.
	// Group by user.
	private Hashtable<Long, List<Group>> organizationsGroupByUser;

	private Hashtable<Long, Long> organizationUsersTime; // group id -> time.
	private Hashtable<Long, List<User>> organizationUsers; // Users by organization.

	private Hashtable<Long, Hashtable<Long, Long>> organizationSiteAndUsersTime; // group id -> time.
	// Site -> User -> Organizations
	private Hashtable<Long, Hashtable<Long, List<Organization>>> organizationSiteAndUsers;

	private static OrganizationPool instance = new OrganizationPool();

	// User --> List<Organization>
	private Hashtable<Long, Long> organizationsByUserTime;
	private Hashtable<Long, List<Organization>> organizationsByUser;

	public static OrganizationPool getInstance() {
		return instance;
	}

	private OrganizationPool() {
		reset();
	}

	public void reset() {
		time = new Hashtable<Long, Long>();
		organizationsByCompany = new Hashtable<Long, List<Organization>>();
		userTime = new Hashtable<Long, Long>();
		organizationsGroupByUser = new Hashtable<Long, List<Group>>();
		organizationUsersTime = new Hashtable<Long, Long>();
		organizationUsers = new Hashtable<Long, List<User>>();
		organizationSiteAndUsersTime = new Hashtable<Long, Hashtable<Long, Long>>();
		organizationSiteAndUsers = new Hashtable<Long, Hashtable<Long, List<Organization>>>();
		organizationsById = new Hashtable<Long, Organization>();
		organizationTime = new Hashtable<Long, Long>();
		organizationsByUser = new Hashtable<Long, List<Organization>>();
		organizationsByUserTime = new Hashtable<Long, Long>();
	}

	public void addOrganization(Company company, Organization organization) {
		if (company != null && organization != null) {
			List<Organization> organizations = new ArrayList<Organization>();
			organizations.add(organization);
			addOrganizations(company, organizations);
		}
	}

	public void addOrganizationGroup(User user, Group group) {
		if (user != null && group != null) {
			List<Group> groups = new ArrayList<Group>();
			groups.add(group);
			addOrganizationGroups(user, groups);
		}
	}

	public void addOrganizationGroups(Long userId, List<Group> groups) {
		if (userId != null && groups != null) {
			userTime.put(userId, System.currentTimeMillis());
			List<Group> organizationGroups = organizationsGroupByUser.get(userId);
			if (organizationGroups == null) {
				organizationGroups = new ArrayList<Group>();
				organizationsGroupByUser.put(userId, organizationGroups);
			}

			for (Group group : groups) {
				if (!organizationGroups.contains(group)) {
					organizationGroups.add(group);
				}
			}
		}
	}

	public void addOrganizationGroups(User user, List<Group> groups) {
		if (user != null && groups != null && !groups.isEmpty()) {
			addOrganizationGroups(user.getUserId(), groups);
		}
	}

	public void addOrganizations(Company company, List<Organization> organizationsToAdd) {
		if (company != null && organizationsToAdd != null) {
			time.put(company.getCompanyId(), System.currentTimeMillis());
			List<Organization> organizationsOfCompany = organizationsByCompany.get(company.getCompanyId());
			if (organizationsOfCompany == null) {
				organizationsOfCompany = new ArrayList<Organization>();
				organizationsByCompany.put(company.getCompanyId(), organizationsOfCompany);
			}

			for (Organization organization : organizationsToAdd) {
				if (!organizationsOfCompany.contains(organization)) {
					organizationsOfCompany.add(organization);
					// Update Ids pool also
					addOrganization(organization);
				}
			}
		}
	}

	/**
	 * Adds organizations for a user in a site to the pool.
	 * 
	 * @param siteId
	 * @param userId
	 * @param organizations
	 */
	public void addOrganizationsBySiteAndUser(long siteId, long userId, List<Organization> organizations) {
		// Update data
		Hashtable<Long, List<Organization>> organizationsByUser = organizationSiteAndUsers.get(siteId);
		if (organizationsByUser == null) {
			organizationSiteAndUsers.put(siteId, new Hashtable<Long, List<Organization>>());
		}
		organizationSiteAndUsers.get(siteId).put(userId, organizations);

		// Update time.
		if (organizationSiteAndUsersTime.get(siteId) == null) {
			organizationSiteAndUsersTime.put(siteId, new Hashtable<Long, Long>());
		}
		organizationSiteAndUsersTime.get(siteId).put(userId, System.currentTimeMillis());

		// Update Ids pool also
		for (Organization organization : organizations) {
			addOrganization(organization);
		}
	}

	/**
	 * Adds organizations for a user in a site to the pool.
	 * 
	 * @param site
	 * @param user
	 * @param organizations
	 */
	public void addOrganizationsBySiteAndUser(Site site, User user, List<Organization> organizations) {
		if (site != null && user != null) {
			addOrganizationsBySiteAndUser(site.getSiteId(), user.getUserId(), organizations);
		}
	}

	public void addOrganizationUsers(Long organizationId, List<User> users) {
		if (organizationId != null && users != null) {
			organizationUsersTime.put(organizationId, System.currentTimeMillis());
			List<User> tempUsers = new ArrayList<User>(users);
			organizationUsers.put(organizationId, tempUsers);
		}
	}

	public void addOrganization(Organization organization) {
		organizationTime.put(organization.getOrganizationId(), System.currentTimeMillis());
		organizationsById.put(organization.getOrganizationId(), organization);
	}

	/**
	 * Gets all previously stored organizations of a user in a site.
	 * 
	 * @param siteId
	 * @param userId
	 * @return
	 */
	public Organization getOrganizationById(long organizationId) {
		long now = System.currentTimeMillis();
		Long storedObjectId = null;
		if (organizationTime.size() > 0) {
			Enumeration<Long> organizationsIds = organizationTime.keys();
			while (organizationsIds.hasMoreElements()) {
				storedObjectId = organizationsIds.nextElement();
				if ((now - organizationTime.get(storedObjectId)) > EXPIRATION_TIME) {
					// object has expired
					removeOrganizationsById(organizationId);
					storedObjectId = null;
				} else {
					if (organizationsById.get(storedObjectId) != null && storedObjectId == organizationId) {
						return organizationsById.get(storedObjectId);
					}
				}
			}
		}
		return null;
	}

	/**
	 * Gets all previously stored organizations of a user in a site.
	 * 
	 * @param siteId
	 * @param userId
	 * @return
	 */
	public List<Organization> getOrganizationBySiteAndUser(long siteId, long userId) {
		long now = System.currentTimeMillis();
		Long nextSiteId = null;
		Long nextUserId = null;
		if (organizationSiteAndUsersTime.size() > 0) {
			Enumeration<Long> siteEnum = organizationSiteAndUsersTime.keys();
			while (siteEnum.hasMoreElements()) {
				nextSiteId = siteEnum.nextElement();
				Enumeration<Long> userEnum = organizationSiteAndUsersTime.get(nextSiteId).keys();
				while (userEnum.hasMoreElements()) {
					nextUserId = userEnum.nextElement();
					if ((now - organizationSiteAndUsersTime.get(nextSiteId).get(nextUserId)) > EXPIRATION_TIME) {
						// object has expired
						removeOrganizations(nextSiteId, nextUserId);
					} else {
						if ((nextSiteId == siteId) && (nextUserId == userId)) {
							return organizationSiteAndUsers.get(siteId).get(userId);
						}
					}
				}
			}
		}
		return null;
	}

	/**
	 * Gets all previously stored organizations of a user in a site.
	 * 
	 * @param siteId
	 * @param userId
	 * @return
	 */
	public List<Organization> getOrganizationBySiteAndUser(Site site, User user) {
		if (site != null && user != null) {
			return getOrganizationBySiteAndUser(site.getSiteId(), user.getUserId());
		}
		return null;
	}

	public List<Group> getOrganizationGroups(long userId) {
		long now = System.currentTimeMillis();
		Long nextUserId = null;
		if (userTime.size() > 0) {
			Enumeration<Long> e = userTime.keys();
			while (e.hasMoreElements()) {
				nextUserId = e.nextElement();
				if ((now - userTime.get(nextUserId)) > EXPIRATION_TIME) {
					// object has expired
					removeOrganizationGroupsOfUser(nextUserId);
					nextUserId = null;
				} else {
					if (userId == nextUserId) {
						return organizationsGroupByUser.get(nextUserId);
					}
				}
			}
		}
		return null;
	}

	public List<Group> getOrganizationGroups(User user) {
		if (user != null) {
			return getOrganizationGroups(user.getUserId());
		}
		return null;
	}

	public List<Organization> getOrganizations(Company company) {
		long now = System.currentTimeMillis();
		Long companyId = null;
		if (time.size() > 0) {
			Enumeration<Long> e = time.keys();
			while (e.hasMoreElements()) {
				companyId = e.nextElement();
				if ((now - time.get(companyId)) > EXPIRATION_TIME) {
					// object has expired
					removeOrganizationsOfCompany(companyId);
					companyId = null;
				} else {
					if (company.getCompanyId() == companyId) {
						return organizationsByCompany.get(companyId);
					}
				}
			}
		}
		return null;
	}

	public List<User> getOrganizationUsers(long organizationId) {
		long now = System.currentTimeMillis();
		Long nextOrganizationId = null;
		if (organizationUsersTime.size() > 0) {
			Enumeration<Long> e = organizationUsersTime.keys();
			while (e.hasMoreElements()) {
				nextOrganizationId = e.nextElement();
				if ((now - organizationUsersTime.get(nextOrganizationId)) > EXPIRATION_TIME) {
					// Object has expired.
					removeOrganizationUsers(nextOrganizationId);
					nextOrganizationId = null;
				} else {
					if (organizationId == nextOrganizationId) {
						return organizationUsers.get(nextOrganizationId);
					}
				}
			}
		}
		return null;
	}

	public void removeOrganization(Company company, Organization organization) {
		List<Organization> organizationsOfCompany = new ArrayList<Organization>(organizationsByCompany.get(company
				.getCompanyId()));
		if (organization != null) {
			for (Organization organizationOfCompany : organizationsOfCompany) {
				if (organizationOfCompany.getOrganizationId() == organization.getOrganizationId()) {
					organizationsByCompany.get(company.getCompanyId()).remove(organizationOfCompany);

					// Also remove Ids pool.
					removeOrganizationsById(organization.getOrganizationId());
				}
			}
		}
	}

	public void removeOrganizationGroups(User user) {
		if (user != null) {
			removeOrganizationGroupsOfUser(user.getUserId());
		}
	}

	private void removeOrganizationGroupsOfUser(Long userId) {
		organizationsGroupByUser.remove(userId);
		userTime.remove(userId);
	}

	public void removeOrganizations(Company company) {
		if (company != null) {
			removeOrganizationsOfCompany(company.getCompanyId());
		}
	}

	/**
	 * Remove organizations for a user in a site from the pool.
	 * 
	 * @param site
	 * @param user
	 */
	public void removeOrganizations(long siteId, long userId) {
		Hashtable<Long, List<Organization>> organizationsByUser = organizationSiteAndUsers.get(siteId);
		if (organizationsByUser != null) {
			organizationsByUser.remove(userId);
			// Remove time mark.
			organizationSiteAndUsersTime.get(siteId).remove(userId);
		}
	}

	/**
	 * Remove organizations for a user in a site from the pool.
	 * 
	 * @param site
	 * @param user
	 */
	public void removeOrganizations(Site site, User user) {
		if (site != null && user != null) {
			removeOrganizations(site.getSiteId(), user.getUserId());
		}
	}

	public void removeOrganizationsOfCompany(Long companyId) {
		time.remove(companyId);
		organizationsByCompany.remove(companyId);
	}

	public void removeOrganizationUsers(Long organizationId) {
		organizationUsersTime.remove(organizationId);
		organizationUsers.remove(organizationId);
	}

	public void removeOrganizationByUsers(Long userId) {
		organizationsByUserTime.remove(userId);
		organizationsByUser.remove(userId);
	}

	public void removeOrganizationsById(Long organizationId) {
		organizationTime.remove(organizationId);
		organizationsById.remove(organizationId);
	}

	public void removeUserFromOrganizations(Long userId, Long organizationId) {
		if (userId != null && organizationId != null) {
			List<User> tempUsers = new ArrayList<User>(organizationUsers.get(organizationId));
			for (User user : tempUsers) {
				if (user.getUserId() == userId) {
					organizationUsers.get(organizationId).remove(user);
				}
			}
		}
	}

	public void removeUserFromOrganizations(User user, Organization organization) {
		if (user != null && organization != null) {
			removeUserFromOrganizations(user.getUserId(), organization.getOrganizationId());
		}
	}

	public List<Organization> getOrganizations(Long userId) {
		long now = System.currentTimeMillis();
		Long nextOrganizationId = null;
		if (organizationsByUserTime.size() > 0) {
			Enumeration<Long> e = organizationsByUserTime.keys();
			while (e.hasMoreElements()) {
				nextOrganizationId = e.nextElement();
				if ((now - organizationsByUserTime.get(nextOrganizationId)) > EXPIRATION_TIME) {
					// Object has expired.
					removeOrganizationByUsers(nextOrganizationId);
					nextOrganizationId = null;
				} else {
					if (userId == nextOrganizationId) {
						return organizationsByUser.get(userId);
					}
				}
			}
		}
		return null;
	}

	public void addOrganizations(Long userId, List<Organization> organizations) {
		if (userId != null && organizations != null) {
			organizationsByUserTime.put(userId, System.currentTimeMillis());
			List<Organization> tempOrganizations = new ArrayList<Organization>(organizations);
			organizationsByUser.put(userId, tempOrganizations);
		}
	}
}
