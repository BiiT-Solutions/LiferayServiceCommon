package com.biit.liferay.access;

/*-
 * #%L
 * Liferay Client Common Utils
 * %%
 * Copyright (C) 2013 - 2025 BiiT Sourcing Solutions S.L.
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

import com.biit.liferay.access.exceptions.NotConnectedToWebServiceException;
import com.biit.liferay.access.exceptions.WebServiceAccessError;
import com.biit.liferay.configuration.LiferayConfigurationReader;
import com.biit.usermanager.entity.IGroup;
import com.biit.usermanager.entity.pool.GroupPool;
import com.biit.usermanager.security.exceptions.AuthenticationRequired;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.liferay.portal.model.Company;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.message.BasicNameValuePair;

import javax.inject.Named;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * This class allows to obtain a liferay portal instance.
 */
@Named
public class CompanyService extends ServiceAccess<IGroup<Long>, Company> implements ICompanyService {

    private GroupPool<Long, Long> groupPool;

    public CompanyService() {
        groupPool = new GroupPool<>();
    }

    @Override
    public Set<IGroup<Long>> decodeListFromJson(String json, Class<Company> objectClass) throws IOException {
        Set<Company> myObjects = new ObjectMapper().readValue(json, new TypeReference<Set<Company>>() {
        });

        return new HashSet<>(myObjects);
    }

    /**
     * Returns the Company with the virtual host name.
     *
     * @param companyId the primary key of the Company
     * @return Returns the Company with the virtual host name.
     * @throws NotConnectedToWebServiceException
     * @throws IOException
     * @throws ClientProtocolException
     * @throws AuthenticationRequired
     * @throws WebServiceAccessError
     */
    @Override
    public IGroup<Long> getCompanyById(long companyId) throws NotConnectedToWebServiceException, ClientProtocolException, IOException, AuthenticationRequired,
            WebServiceAccessError {

        IGroup<Long> company = groupPool.getGroupById(companyId);
        if (company != null) {
            return company;
        }

        checkConnection();

        List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("companyId", companyId + ""));

        String result = getHttpPostResponse("company/get-company-by-id", params);
        if (result != null) {
            // A Simple JSON Response Read
            company = decodeFromJson(result, Company.class);
            groupPool.addGroup(company);
            return company;
        }

        return null;
    }

    /**
     * Returns the Company with the virtual host name.
     *
     * @param virtualHost the Company's virtual host name.
     * @return Returns the Company with the virtual host name.
     * @throws NotConnectedToWebServiceException
     * @throws IOException
     * @throws JsonMappingException
     * @throws JsonParseException
     * @throws AuthenticationRequired
     * @throws WebServiceAccessError
     */
    @Override
    public IGroup<Long> getCompanyByVirtualHost(String virtualHost) throws NotConnectedToWebServiceException, JsonParseException, JsonMappingException,
            IOException, AuthenticationRequired, WebServiceAccessError {

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

        String result = getHttpPostResponse("company/get-company-by-virtual-host", params);
        if (result != null) {
            // A Simple JSON Response Read
            company = decodeFromJson(result, Company.class);
            groupPool.addGroupByTag(company, virtualHost);
            return company;
        }

        return null;
    }

    @Override
    public IGroup<Long> getDefaultCompany() throws NotConnectedToWebServiceException, JsonParseException, JsonMappingException, IOException,
            AuthenticationRequired, WebServiceAccessError {
        return getCompanyByVirtualHost(LiferayConfigurationReader.getInstance().getVirtualHost());
    }

    /**
     * Returns the Company with the web domain.
     *
     * @param webId The Company's web domain
     * @return Returns the Company with the virtual host name.
     * @throws NotConnectedToWebServiceException
     * @throws IOException
     * @throws ClientProtocolException
     * @throws AuthenticationRequired
     * @throws WebServiceAccessError
     */
    @Override
    public IGroup<Long> getCompanyByWebId(String webId) throws NotConnectedToWebServiceException, ClientProtocolException, IOException, AuthenticationRequired,
            WebServiceAccessError {

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

        String result = getHttpPostResponse("company/get-company-by-web-id", params);
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
