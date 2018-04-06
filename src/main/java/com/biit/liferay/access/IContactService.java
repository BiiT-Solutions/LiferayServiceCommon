package com.biit.liferay.access;

import java.io.IOException;

import org.apache.http.client.ClientProtocolException;

import com.biit.liferay.access.exceptions.NotConnectedToWebServiceException;
import com.biit.liferay.access.exceptions.WebServiceAccessError;
import com.biit.usermanager.security.exceptions.AuthenticationRequired;
import com.liferay.portal.model.Contact;
import com.liferay.portal.model.User;

public interface IContactService extends IServiceAccess {

	Contact getContact(User user) throws NotConnectedToWebServiceException, ClientProtocolException, IOException, AuthenticationRequired, WebServiceAccessError;

	Contact getContact(Long contactId) throws NotConnectedToWebServiceException, ClientProtocolException, IOException, AuthenticationRequired,
			WebServiceAccessError;

	void reset();

}
