package com.biit.liferay.access;

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
