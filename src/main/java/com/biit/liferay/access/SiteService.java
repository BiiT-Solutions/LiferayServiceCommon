package com.biit.liferay.access;

import com.biit.liferay.access.exceptions.DuplicatedLiferayElement;
import com.biit.liferay.access.exceptions.NotConnectedToWebServiceException;
import com.biit.liferay.access.exceptions.WebServiceAccessError;
import com.biit.liferay.log.LiferayClientLogger;
import com.biit.usermanager.entity.IGroup;
import com.biit.usermanager.entity.pool.GroupPool;
import com.biit.usermanager.security.exceptions.AuthenticationRequired;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.liferay.portal.model.Site;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.message.BasicNameValuePair;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Site service is almost the same that Group Service. Liferay stores Sites as
 * Groups.
 */
public class SiteService extends ServiceAccess<IGroup<Long>, Site> {

	private GroupPool<Long, Long> groupPool;

	public SiteService() {
		groupPool = new GroupPool<Long, Long>();
	}

	/**
	 * Add a site to the database. The companyId is set by the serviceContext. If
	 * the service context is null, probably will be used the service context of the
	 * user used for connecting to the webservices?
	 * 
	 * @param name
	 * @param description
	 * @param type
	 * @param friendlyURL
	 * @return
	 * @throws NotConnectedToWebServiceException
	 * @throws ClientProtocolException
	 * @throws IOException
	 * @throws AuthenticationRequired
	 * @throws WebServiceAccessError
	 * @throws DuplicatedLiferayElement
	 */
	public IGroup<Long> addSite(String name, String description, SiteType type, String friendlyURL)
			throws NotConnectedToWebServiceException, ClientProtocolException, IOException, AuthenticationRequired,
			WebServiceAccessError, DuplicatedLiferayElement {
		if (name != null) {
			checkConnection();

			List<NameValuePair> params = new ArrayList<NameValuePair>();
			params.add(new BasicNameValuePair("name", name));
			params.add(new BasicNameValuePair("description", description));
			params.add(new BasicNameValuePair("type", type.getType() + ""));
			params.add(new BasicNameValuePair("friendlyURL", friendlyURL));
			params.add(new BasicNameValuePair("site", "1"));
			params.add(new BasicNameValuePair("active", "1"));
			params.add(new BasicNameValuePair("-serviceContext", null));

			String result = getHttpPostResponse("group/add-group", params);
			IGroup<Long> site = null;
			if (result != null) {
				// A Simple JSON Response Read
				try {
					site = decodeFromJson(result, Site.class);
				} catch (WebServiceAccessError wsa) {
					if (wsa.getMessage().contains("DuplicateGroupException")) {
						throw new DuplicatedLiferayElement("Already exists a site with name '" + name + "'.");
					} else {
						throw wsa;
					}
				}

				groupPool.addGroup(site);
				LiferayClientLogger.info(this.getClass().getName(), "Site '" + site.getUniqueName() + "' added.");
				return site;
			}
		}
		return null;
	}

	@Override
	public Set<IGroup<Long>> decodeListFromJson(String json, Class<Site> objectClass)
			throws IOException {
		Set<Site> myObjects = new ObjectMapper().readValue(json, new TypeReference<Set<Site>>() {
		});
		return new HashSet<>(myObjects);
	}

	public boolean deleteSite(IGroup<Long> site)
			throws NotConnectedToWebServiceException, ClientProtocolException, IOException, AuthenticationRequired {
		if (site != null) {
			checkConnection();

			List<NameValuePair> params = new ArrayList<NameValuePair>();
			params.add(new BasicNameValuePair("groupId", Long.toString(site.getUniqueId())));

			getHttpPostResponse("group/delete-group", params);
			groupPool.removeGroupsById(site.getUniqueId());
			LiferayClientLogger.info(this.getClass().getName(), "Site '" + site.getUniqueName() + "' deleted.");
			return true;
		}
		return false;
	}

	public IGroup<Long> getSite(IGroup<Long> company, String siteName) throws NotConnectedToWebServiceException,
			ClientProtocolException, IOException, AuthenticationRequired, WebServiceAccessError {
		if (company != null) {
			return getSite(company.getUniqueId(), siteName);
		}
		return null;
	}

	public IGroup<Long> getSite(Long companyId, String siteName) throws NotConnectedToWebServiceException,
			ClientProtocolException, IOException, AuthenticationRequired, WebServiceAccessError {
		if (companyId != null && siteName != null) {
			// Look up group in the pool.
			IGroup<Long> site = null;
			// Look up user in the pool.
			if (groupPool.getElementsByTag(siteName) != null && !groupPool.getElementsByTag(siteName).isEmpty()) {
				site = groupPool.getElementsByTag(siteName).iterator().next();
				if (site != null) {
					return site;
				}
			}

			checkConnection();

			List<NameValuePair> params = new ArrayList<NameValuePair>();
			params.add(new BasicNameValuePair("companyId", companyId + ""));
			params.add(new BasicNameValuePair("name", siteName));

			String result = getHttpPostResponse("group/get-group", params);
			if (result != null) {
				// A Simple JSON Response Read
				site = decodeFromJson(result, Site.class);
				groupPool.addGroupByTag(site, siteName);
				return site;
			}
		}
		return null;
	}

	public IGroup<Long> getSiteByFriendlyUrl(IGroup<Long> company, String friendlyUrl)
			throws NotConnectedToWebServiceException, ClientProtocolException, IOException, AuthenticationRequired,
			WebServiceAccessError {
		if (company != null) {
			return getSiteByFriendlyUrl(company.getUniqueId(), friendlyUrl);
		}
		return null;
	}

	public IGroup<Long> getSiteByFriendlyUrl(Long companyId, String friendlyUrl)
			throws NotConnectedToWebServiceException, ClientProtocolException, IOException, AuthenticationRequired,
			WebServiceAccessError {
		if (companyId != null && friendlyUrl != null) {
			IGroup<Long> site = null;
			// Look up user in the pool.
			if (groupPool.getElementsByTag(friendlyUrl) != null && !groupPool.getElementsByTag(friendlyUrl).isEmpty()) {
				site = groupPool.getElementsByTag(friendlyUrl).iterator().next();
				if (site != null) {
					return site;
				}
			}

			checkConnection();

			List<NameValuePair> params = new ArrayList<NameValuePair>();
			params.add(new BasicNameValuePair("companyId", companyId + ""));
			params.add(new BasicNameValuePair("parentGroupId", "0"));
			params.add(new BasicNameValuePair("site", "1"));

			String result = getHttpPostResponse("group/get-groups", params);
			if (result != null) {
				// A Simple JSON Response Read
				Set<IGroup<Long>> sites = decodeListFromJson(result, Site.class);
				for (IGroup<Long> siteSearch : sites) {
					if (((Site) siteSearch).getFriendlyURL().equals(friendlyUrl)) {
						groupPool.addGroupByTag(site, friendlyUrl);
						return siteSearch;
					}
				}
			}
		}
		return null;
	}

	@Override
	public void reset() {
		groupPool.reset();
	}

}
