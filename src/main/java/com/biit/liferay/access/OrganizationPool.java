package com.biit.liferay.access;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import com.biit.usermanager.entity.IGroup;
import com.biit.usermanager.entity.IUser;
import com.biit.usermanager.entity.pool.GroupPool;

public class OrganizationPool extends GroupPool<Long, Long> {

	private final static long EXPIRATION_TIME = 300000;// 5 minutes

	private Map<Long, Map<Long, Long>> organizationSiteAndUsersTime; // group id -> time.
	// Site -> User -> Organizations
	private Map<Long, Map<Long, Set<IGroup<Long>>>> organizationSiteAndUsers;

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
			organizationSiteAndUsers.put(siteId, new Hashtable<Long, Set<IGroup<Long>>>());
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

	/**
	 * Gets all previously stored organizations of a user in a site.
	 * 
	 * @param siteId
	 * @param userId
	 * @return
	 */
	public Set<IGroup<Long>> getOrganizationBySiteAndUser(long siteId, long userId) {
		long now = System.currentTimeMillis();
		Long nextSiteId = null;
		Long nextUserId = null;
		if (organizationSiteAndUsersTime.size() > 0) {
			Iterator<Long> siteEnum = new HashMap<Long, Map<Long, Long>>(organizationSiteAndUsersTime).keySet()
					.iterator();
			while (siteEnum.hasNext()) {
				nextSiteId = siteEnum.next();
				Iterator<Long> userEnum = new HashMap<Long, Map<Long, Long>>(organizationSiteAndUsersTime)
						.get(nextSiteId).keySet().iterator();
				while (userEnum.hasNext()) {
					nextUserId = userEnum.next();
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

	@Override
	public void reset() {
		super.reset();
		organizationSiteAndUsersTime = new HashMap<Long, Map<Long, Long>>();
		organizationSiteAndUsers = new HashMap<Long, Map<Long, Set<IGroup<Long>>>>();
	}

}
