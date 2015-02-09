package com.biit.liferay.access;

import java.util.Enumeration;
import java.util.Hashtable;

import com.liferay.portal.model.Site;

public class SitePool {
	private final static long EXPIRATION_TIME = 300000;// 5 minutes

	private Hashtable<Long, Long> time; // Site id -> time.
	private Hashtable<Long, Site> sites; // Site id -> Site.

	private static SitePool instance = new SitePool();

	public static SitePool getInstance() {
		return instance;
	}

	private SitePool() {
		reset();
	}
	
	public void reset(){
		time = new Hashtable<Long, Long>();
		sites = new Hashtable<Long, Site>();
	}

	public void addSite(Site site) {
		if (site != null) {
			time.put(site.getGroupId(), System.currentTimeMillis());
			sites.put(site.getGroupId(), site);
		}
	}

	public Site getSiteByName(String siteName) {
		long now = System.currentTimeMillis();
		Long storedObject = null;
		if (time.size() > 0) {
			Enumeration<Long> e = time.keys();
			while (e.hasMoreElements()) {
				storedObject = e.nextElement();
				if ((now - time.get(storedObject)) > EXPIRATION_TIME) {
					// object has expired
					removeSite(storedObject);
					storedObject = null;
				} else {
					if (sites.get(storedObject) != null && sites.get(storedObject).getName().equals(siteName)) {
						return sites.get(storedObject);
					}
				}
			}
		}
		return null;
	}

	public void removeSite(Site site) {
		if (site != null) {
			removeSite(site.getGroupId());
		}
	}

	public Site getSiteByFriendlyUrl(String friendlyUrl) {
		long now = System.currentTimeMillis();
		Long storedObject = null;
		if (time.size() > 0) {
			Enumeration<Long> e = time.keys();
			while (e.hasMoreElements()) {
				storedObject = e.nextElement();
				if ((now - time.get(storedObject)) > EXPIRATION_TIME) {
					// object has expired
					removeSite(storedObject);
					storedObject = null;
				} else {
					if (sites.get(storedObject) != null && sites.get(storedObject).getFriendlyURL().equals(friendlyUrl)) {
						return sites.get(storedObject);
					}
				}
			}
		}
		return null;
	}

	private void removeSite(long siteId) {
		time.remove(siteId);
		sites.remove(siteId);
	}

}
