package com.biit.liferay.access;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.biit.usermanager.entity.IGroup;
import com.biit.usermanager.entity.IUser;
import com.biit.usermanager.entity.pool.GroupPool;

public class OrganizationPool extends GroupPool<Long, Long> {
	// group id -> time.
	private Map<Long, Map<Long, Long>> organizationSiteAndUsersTime;
	// Site -> User -> Organizations
	private Map<Long, Map<Long, Set<IGroup<Long>>>> organizationSiteAndUsers;
	// Company -> User -> parentOrganizations
	private Map<Long, Map<Long, Map<Long, Set<IGroup<Long>>>>> suborganizationsByUser;
	// Company id -> User Id -> parentOrganizations -> time.
	private Map<Long, Map<Long, Map<Long, Long>>> suborganizationsByUserTime;

	/**
	 * Adds organizations for a user in a site to the pool.
	 * 
	 * @param site
	 * @param user
	 * @param organizations
	 */
	public void addOrganizationsBySiteAndUser(IGroup<Long> site, IUser<Long> user, Set<IGroup<Long>> organizations) {
		if (site != null && user != null) {
			addOrganizationsBySiteAndUser(site.getId(), user.getId(), organizations);
		}
	}

	/**
	 * Adds organizations for a user in a site to the pool.
	 * 
	 * @param siteId
	 * @param userId
	 * @param organizations
	 */
	public void addOrganizationsBySiteAndUser(long siteId, long userId, Set<IGroup<Long>> organizations) {
		// Update data
		Map<Long, Set<IGroup<Long>>> organizationsByUser = organizationSiteAndUsers.get(siteId);
		if (organizationsByUser == null) {
			organizationSiteAndUsers.put(siteId, new HashMap<Long, Set<IGroup<Long>>>());
		}
		organizationSiteAndUsers.get(siteId).put(userId, organizations);

		// Update time.
		if (organizationSiteAndUsersTime.get(siteId) == null) {
			organizationSiteAndUsersTime.put(siteId, new Hashtable<Long, Long>());
		}
		organizationSiteAndUsersTime.get(siteId).put(userId, System.currentTimeMillis());

		// Update Ids pool also
		for (IGroup<Long> organization : organizations) {
			super.addGroup(organization);
		}
	}

	public void addOrganizations(long companyId, long userId, long parentOrganizationId,
			Set<IGroup<Long>> organizations) {
		// Update data.
		Map<Long, Map<Long, Set<IGroup<Long>>>> organizationsByCompany = suborganizationsByUser.get(companyId);
		if (organizationsByCompany == null) {
			suborganizationsByUser.put(companyId, new HashMap<Long, Map<Long, Set<IGroup<Long>>>>());
		}

		Map<Long, Set<IGroup<Long>>> organizationsByUser = suborganizationsByUser.get(companyId).get(userId);
		if (organizationsByUser == null) {
			suborganizationsByUser.get(companyId).put(userId, new HashMap<Long, Set<IGroup<Long>>>());
		}
		suborganizationsByUser.get(companyId).get(userId).put(parentOrganizationId, organizations);

		// Update time.
		Map<Long, Map<Long, Long>> organizationsByCompanyTime = suborganizationsByUserTime.get(companyId);
		if (organizationsByCompanyTime == null) {
			suborganizationsByUserTime.put(companyId, new HashMap<Long, Map<Long, Long>>());
		}

		Map<Long, Long> organizationsByUserTime = suborganizationsByUserTime.get(companyId).get(userId);
		if (organizationsByUserTime == null) {
			suborganizationsByUserTime.get(companyId).put(userId, new HashMap<Long, Long>());
		}
		suborganizationsByUserTime.get(companyId).get(userId).put(parentOrganizationId, System.currentTimeMillis());

		// Update Ids pool also
		for (IGroup<Long> organization : organizations) {
			super.addGroup(organization);
		}
	}

	/**
	 * Gets all previously stored organizations of a user in a site.
	 * 
	 * @param siteId
	 * @param userId
	 * @return
	 */
	public Set<IGroup<Long>> getOrganizationBySiteAndUser(IGroup<Long> site, IUser<Long> user) {
		if (site != null && user != null) {
			return getOrganizationBySiteAndUser(site.getId(), user.getId());
		}
		return null;
	}

	public Set<IGroup<Long>> getSuborganizations(IGroup<Long> company, IUser<Long> user, Long parentOrganizationId) {
		if (company != null && user != null) {
			if (parentOrganizationId == null) {
				parentOrganizationId = new Long(0);
			}
			return getSuborganizations(company.getId(), user.getId(), parentOrganizationId);
		}
		return null;
	}

	public synchronized Set<IGroup<Long>> getSuborganizations(long compantId, long userId, long parentOrganizationId) {
		long now = System.currentTimeMillis();
		Long nextCompanyId = null;
		Long nextUserId = null;
		Long nextOrganizationId = null;
		if (suborganizationsByUserTime.size() > 0) {
			Iterator<Long> companyIterator = new HashMap<Long, Map<Long, Map<Long, Long>>>(suborganizationsByUserTime)
					.keySet().iterator();
			while (companyIterator.hasNext()) {
				nextCompanyId = companyIterator.next();
				Iterator<Long> userIterator = new HashMap<Long, Map<Long, Long>>(
						suborganizationsByUserTime.get(nextCompanyId)).keySet().iterator();
				while (userIterator.hasNext()) {
					nextUserId = userIterator.next();
					Iterator<Long> organizationsIterator = new HashMap<Long, Long>(
							suborganizationsByUserTime.get(nextCompanyId).get(nextUserId)).keySet().iterator();
					while (organizationsIterator.hasNext()) {
						nextOrganizationId = organizationsIterator.next();
						if (suborganizationsByUserTime.get(nextCompanyId) != null
								&& suborganizationsByUserTime.get(nextCompanyId).get(nextUserId) != null
								&& suborganizationsByUserTime.get(nextCompanyId).get(nextUserId)
										.get(nextOrganizationId) != null) {
							if ((now - suborganizationsByUserTime.get(nextCompanyId).get(nextUserId)
									.get(nextOrganizationId)) > getExpirationTime()) {
								// object has expired
								removeOrganizations(nextCompanyId, nextUserId, nextOrganizationId);
							} else {
								if ((nextCompanyId == compantId) && (nextUserId == userId)
										&& (nextOrganizationId == parentOrganizationId)) {
									return suborganizationsByUser.get(nextCompanyId).get(nextUserId)
											.get(nextOrganizationId);
								}
							}
						}
					}
				}
			}
		}
		return null;
	}

	public synchronized Set<IGroup<Long>> getOrganizationBySiteAndUser(long siteId, long userId) {
		long now = System.currentTimeMillis();
		Long nextSiteId = null;
		Long nextUserId = null;
		if (organizationSiteAndUsersTime.size() > 0) {
			Iterator<Long> siteIterator = new HashMap<Long, Map<Long, Long>>(organizationSiteAndUsersTime).keySet()
					.iterator();
			while (siteIterator.hasNext()) {
				nextSiteId = siteIterator.next();
				Iterator<Long> userEnum = new HashMap<Long, Long>(organizationSiteAndUsersTime.get(nextSiteId)).keySet()
						.iterator();
				while (userEnum.hasNext()) {
					nextUserId = userEnum.next();
					if (organizationSiteAndUsersTime.get(nextSiteId) != null
							&& organizationSiteAndUsersTime.get(nextSiteId).get(nextUserId) != null) {
						if ((now - organizationSiteAndUsersTime.get(nextSiteId)
								.get(nextUserId)) > getExpirationTime()) {
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
		}
		return null;
	}

	/**
	 * Remove organizations for a user in a site from the pool.
	 * 
	 * @param site
	 * @param user
	 */
	public void removeOrganizations(long siteId, long userId) {
		Map<Long, Set<IGroup<Long>>> organizationsByUser = organizationSiteAndUsers.get(siteId);
		if (organizationsByUser != null) {
			organizationsByUser.remove(userId);
			// Remove time mark.
			organizationSiteAndUsersTime.get(siteId).remove(userId);
		}
	}

	public void removeOrganizations(long companyId, long userId, long parentOrganizationId) {
		if (suborganizationsByUserTime.get(companyId) != null
				&& suborganizationsByUserTime.get(companyId).get(userId) != null
				&& suborganizationsByUserTime.get(companyId).get(userId).get(parentOrganizationId) != null) {
			suborganizationsByUser.get(companyId).get(userId).remove(parentOrganizationId);
			// Remove time mark.
			suborganizationsByUserTime.get(companyId).get(userId).remove(parentOrganizationId);
		}
	}

	public void removeSuborganizations(long companyId, long parentOrganizationId) {
		if (suborganizationsByUserTime.get(companyId) != null) {
			for (Entry<Long, Map<Long, Set<IGroup<Long>>>> userOrganizationsEntries : suborganizationsByUser
					.get(companyId).entrySet()) {
				if (userOrganizationsEntries.getValue().get(parentOrganizationId) != null) {
					userOrganizationsEntries.getValue().remove(parentOrganizationId);
				}
			}
			for (Entry<Long, Map<Long, Long>> userOrganizationsEntries : suborganizationsByUserTime.get(companyId)
					.entrySet()) {
				if (userOrganizationsEntries.getValue().get(parentOrganizationId) != null) {
					userOrganizationsEntries.getValue().remove(parentOrganizationId);
				}
			}
		}
	}

	@Override
	public void reset() {
		super.reset();
		organizationSiteAndUsersTime = new HashMap<Long, Map<Long, Long>>();
		organizationSiteAndUsers = new HashMap<Long, Map<Long, Set<IGroup<Long>>>>();
		suborganizationsByUser = new HashMap<Long, Map<Long, Map<Long, Set<IGroup<Long>>>>>();
		suborganizationsByUserTime = new HashMap<Long, Map<Long, Map<Long, Long>>>();
	}

	@Override
	public void removeUser(IUser<Long> user) {
		super.removeUser(user);
		for (Long siteId : new HashSet<Long>(organizationSiteAndUsers.keySet())) {
			for (Long userId : new HashSet<Long>(organizationSiteAndUsers.get(siteId).keySet())) {
				if (userId.equals(user.getId())) {
					removeOrganizations(siteId, userId);
				}
			}
		}
		System.out.println("#### REMOVING USER " + user.getId());
		for (Long companyId : new HashSet<Long>(suborganizationsByUser.keySet())) {
			System.out.println("\tCompany " + companyId);
			for (Long userId : new HashSet<Long>(suborganizationsByUser.get(companyId).keySet())) {
				System.out.println("\tUser " + userId + " <-> " + user.getId());
				if (userId.equals(user.getId())) {
					System.out.println("Equals!!");
					System.out.println(suborganizationsByUser.get(companyId).get(userId));
					suborganizationsByUser.get(companyId).remove(userId);
					suborganizationsByUserTime.get(companyId).remove(userId);
					System.out.println(suborganizationsByUser.get(companyId).get(userId));
				}
			}
		}
	}
}
