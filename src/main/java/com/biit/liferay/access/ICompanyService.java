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

import java.io.IOException;

import org.apache.http.client.ClientProtocolException;

import com.biit.liferay.access.exceptions.NotConnectedToWebServiceException;
import com.biit.liferay.access.exceptions.WebServiceAccessError;
import com.biit.usermanager.entity.IGroup;
import com.biit.usermanager.security.exceptions.AuthenticationRequired;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;

public interface ICompanyService extends LiferayService, IServiceAccess {

	IGroup<Long> getCompanyById(long companyId) throws NotConnectedToWebServiceException, ClientProtocolException,
			IOException, AuthenticationRequired, WebServiceAccessError;

	IGroup<Long> getCompanyByVirtualHost(String virtualHost) throws NotConnectedToWebServiceException,
			JsonParseException, JsonMappingException, IOException, AuthenticationRequired, WebServiceAccessError;

	IGroup<Long> getDefaultCompany() throws NotConnectedToWebServiceException, JsonParseException, JsonMappingException,
			IOException, AuthenticationRequired, WebServiceAccessError;

	IGroup<Long> getCompanyByWebId(String webId) throws NotConnectedToWebServiceException, ClientProtocolException,
			IOException, AuthenticationRequired, WebServiceAccessError;

}
