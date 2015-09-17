package com.biit.liferay.access;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.message.BasicNameValuePair;

import com.biit.liferay.access.exceptions.NotConnectedToWebServiceException;
import com.biit.liferay.access.exceptions.WebServiceAccessError;
import com.biit.usermanager.entity.IGroup;
import com.biit.usermanager.entity.pool.GroupPool;
import com.biit.usermanager.security.exceptions.AuthenticationRequired;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.liferay.portal.model.Company;

/**
 * This class allows to obtain a liferay portal instance.
 */
public class CompanyService extends ServiceAccess<IGroup<Long>, Company> {

	private GroupPool<Long, Long> groupPool;

	public CompanyService() {
		groupPool = new GroupPool<Long, Long>();
	}

	@Override
	public Set<IGroup<Long>> decodeListFromJson(String json, Class<Company> objectClass) throws JsonParseException,
			JsonMappingException, IOException {
		Set<IGroup<Long>> myObjects = new ObjectMapper().readValue(json, new TypeReference<Set<Company>>() {
		});

		return myObjects;
	}

	/**
	 * Returns the CompanySoap with the virtual host name.
	 * 
	 * @param companyId
	 *            the primary key of the CompanySoap
	 * @return Returns the CompanySoap with the virtual host name.
	 * @throws NotConnectedToWebServiceException
	 * @throws IOException
	 * @throws ClientProtocolException
	 * @throws AuthenticationRequired
	 * @throws WebServiceAccessError
	 */
	public IGroup<Long> getCompanyById(long companyId) throws NotConnectedToWebServiceException,
			ClientProtocolException, IOException, AuthenticationRequired, WebServiceAccessError {

		IGroup<Long> company = groupPool.getGroupById(companyId);
		if (company != null) {
			return company;
		}

		checkConnection();

		List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("companyId", companyId + ""));

		String result = getHttpResponse("company/get-company-by-id", params);
		if (result != null) {
			// A Simple JSON Response Read
			company = decodeFromJson(result, Company.class);
			groupPool.addGroup(company);
			return company;
		}

		return null;
	}

	/**
	 * Returns the CompanySoap with the virtual host name.
	 * 
	 * @param virtualHost
	 *            the CompanySoap's virtual host name.
	 * @return Returns the CompanySoap with the virtual host name.
	 * @throws NotConnectedToWebServiceException
	 * @throws IOException
	 * @throws JsonMappingException
	 * @throws JsonParseException
	 * @throws AuthenticationRequired
	 * @throws WebServiceAccessError
	 * 
	 */
	public IGroup<Long> getCompanyByVirtualHost(String virtualHost) throws NotConnectedToWebServiceException,
			JsonParseException, JsonMappingException, IOException, AuthenticationRequired, WebServiceAccessError {

		IGroup<Long> company = null;
		// Look up user in the pool.
		if (groupPool.getElementsByTag(virtualHost) != null && !groupPool.getElementsByTag(virtualHost).isEmpty()) {
			company = groupPool.getElementsByTag(virtualHost).iterator().next();
			if (company != null) {
				return company;
			}
		}

		checkConnection();

		List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("virtualHost", virtualHost));

		String result = getHttpResponse("company/get-company-by-virtual-host", params);
		if (result != null) {
			// A Simple JSON Response Read
			company = decodeFromJson(result, Company.class);
			groupPool.addGroupByTag(company, virtualHost);
			return company;
		}

		return null;
	}

	/**
	 * Returns the CompanySoap with the web domain.
	 * 
	 * @param webId
	 *            The CompanySoap's web domain
	 * @return Returns the CompanySoap with the virtual host name.
	 * @throws NotConnectedToWebServiceException
	 * @throws IOException
	 * @throws ClientProtocolException
	 * @throws AuthenticationRequired
	 * @throws WebServiceAccessError
	 */
	public IGroup<Long> getCompanyByWebId(String webId) throws NotConnectedToWebServiceException,
			ClientProtocolException, IOException, AuthenticationRequired, WebServiceAccessError {

		IGroup<Long> company = null;
		// Look up user in the pool.
		if (groupPool.getElementsByTag(webId) != null && !groupPool.getElementsByTag(webId).isEmpty()) {
			company = groupPool.getElementsByTag(webId).iterator().next();
			if (company != null) {
				return company;
			}
		}

		checkConnection();

		List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("webId ", webId));

		String result = getHttpResponse("company/get-company-by-web-id", params);
		if (result != null) {
			// A Simple JSON Response Read
			company = decodeFromJson(result, Company.class);
			groupPool.addGroupByTag(company, webId);
			return company;
		}

		return null;
	}

	public void reset() {
		groupPool.reset();
	}

}
