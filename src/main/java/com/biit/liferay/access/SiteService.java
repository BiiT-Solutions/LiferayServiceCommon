package com.biit.liferay.access;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.message.BasicNameValuePair;

import com.biit.liferay.access.exceptions.AuthenticationRequired;
import com.biit.liferay.access.exceptions.NotConnectedToWebServiceException;
import com.biit.liferay.access.exceptions.WebServiceAccessError;
import com.biit.liferay.log.LiferayClientLogger;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.liferay.portal.model.Site;

/**
 * Site service is almost the same that Group Service. Liferay stores Sites as Groups.
 * 
 */
public class SiteService extends ServiceAccess<Site> {

	public SiteService() {
	}

	@Override
	public List<Site> decodeListFromJson(String json, Class<Site> objectClass) throws JsonParseException,
			JsonMappingException, IOException {
		List<Site> myObjects = new ObjectMapper().readValue(json, new TypeReference<List<Site>>() {
		});

		return myObjects;
	}

	public Site getSite(Long companyId, String siteName) throws NotConnectedToWebServiceException,
			ClientProtocolException, IOException, AuthenticationRequired, WebServiceAccessError {
		if (companyId != null && siteName != null) {
			// Look up user in the pool.
			Site site = SitePool.getInstance().getSiteByName(siteName);
			if (site != null) {
				return site;
			}

			checkConnection();

			List<NameValuePair> params = new ArrayList<NameValuePair>();
			params.add(new BasicNameValuePair("companyId", companyId + ""));
			params.add(new BasicNameValuePair("name", siteName));

			String result = getHttpResponse("group/get-group", params);
			if (result != null) {
				// A Simple JSON Response Read
				site = decodeFromJson(result, Site.class);
				return site;
			}
		}
		return null;
	}

	public Site getSiteByFriendlyUrl(Long companyId, String friendlyUrl) throws NotConnectedToWebServiceException,
			ClientProtocolException, IOException, AuthenticationRequired, WebServiceAccessError {
		if (companyId != null && friendlyUrl != null) {
			// Look up user in the pool.
			Site site = SitePool.getInstance().getSiteByFriendlyUrl(friendlyUrl);
			if (site != null) {
				return site;
			}

			checkConnection();

			List<NameValuePair> params = new ArrayList<NameValuePair>();
			params.add(new BasicNameValuePair("companyId", companyId + ""));
			params.add(new BasicNameValuePair("parentGroupId", "0"));
			params.add(new BasicNameValuePair("site", "1"));

			String result = getHttpResponse("group/get-groups", params);
			if (result != null) {
				// A Simple JSON Response Read
				List<Site> sites = decodeListFromJson(result, Site.class);
				for (Site siteSearch : sites) {
					if (siteSearch.getFriendlyURL().equals(friendlyUrl)) {
						return siteSearch;
					}
				}
			}
		}
		return null;
	}

	/**
	 * Add a site to the database. The companyId is set by the serviceContext. If the service context is null, probably
	 * will be used the service context of the user used for connecting to the webservices?
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
	 */
	public Site addSite(String name, String description, int type, String friendlyURL)
			throws NotConnectedToWebServiceException, ClientProtocolException, IOException, AuthenticationRequired,
			WebServiceAccessError {
		if (name != null) {
			checkConnection();

			List<NameValuePair> params = new ArrayList<NameValuePair>();
			params.add(new BasicNameValuePair("name", name));
			params.add(new BasicNameValuePair("description", description));
			params.add(new BasicNameValuePair("type", type + ""));
			params.add(new BasicNameValuePair("friendlyURL", friendlyURL));
			params.add(new BasicNameValuePair("site", "1"));
			params.add(new BasicNameValuePair("active", "1"));
			params.add(new BasicNameValuePair("-serviceContext", null));

			String result = getHttpResponse("group/add-group", params);
			Site site = null;
			if (result != null) {
				// A Simple JSON Response Read
				site = decodeFromJson(result, Site.class);
				SitePool.getInstance().addSite(site);
				LiferayClientLogger.info(this.getClass().getName(), "Site '" + site.getName() + "' added.");
				return site;
			}
		}
		return null;
	}

	public boolean deleteSite(Site site) throws NotConnectedToWebServiceException, ClientProtocolException,
			IOException, AuthenticationRequired {
		if (site != null) {
			checkConnection();

			List<NameValuePair> params = new ArrayList<NameValuePair>();
			params.add(new BasicNameValuePair("groupId", Long.toString(site.getGroupId())));

			getHttpResponse("group/delete-group", params);
			SitePool.getInstance().removeSite(site);
			LiferayClientLogger.info(this.getClass().getName(), "Site '" + site.getName() + "' deleted.");
			return true;
		}
		return false;
	}

}
