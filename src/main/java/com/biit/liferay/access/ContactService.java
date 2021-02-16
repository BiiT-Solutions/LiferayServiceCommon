package com.biit.liferay.access;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.inject.Named;

import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.message.BasicNameValuePair;

import com.biit.liferay.access.exceptions.NotConnectedToWebServiceException;
import com.biit.liferay.access.exceptions.WebServiceAccessError;
import com.biit.usermanager.security.exceptions.AuthenticationRequired;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.liferay.portal.model.Contact;
import com.liferay.portal.model.User;

@Named
public class ContactService extends ServiceAccess<Contact, Contact> implements IContactService {

	private final ContactPool contactPool;

	public ContactService() {
		contactPool = new ContactPool();
	}

	@Override
	public Set<Contact> decodeListFromJson(String json, Class<Contact> objectClass) throws JsonParseException, JsonMappingException, IOException {
		Set<Contact> myObjects = new ObjectMapper().readValue(json, new TypeReference<Set<Contact>>() {
		});
		return myObjects;
	}

	/**
	 * Gets the user's contact by id.
	 * 
	 * @param contactId
	 * @return
	 * @throws NotConnectedToWebServiceException
	 * @throws ClientProtocolException
	 * @throws IOException
	 * @throws AuthenticationRequired
	 * @throws WebServiceAccessError
	 */
	@Override
	public Contact getContact(Long contactId) throws NotConnectedToWebServiceException, ClientProtocolException, IOException, AuthenticationRequired,
			WebServiceAccessError {
		if (contactId != null) {
			// Look up user in the liferay.
			Contact contact = contactPool.getElement(contactId);
			if (contact != null) {
				return contact;
			}

			checkConnection();

			List<NameValuePair> params = new ArrayList<NameValuePair>();
			params.add(new BasicNameValuePair("contactId", contactId + ""));

			String result = getHttpPostResponse("contact/get-contact", params);
			if (result != null) {
				// A Simple JSON Response Read
				contact = decodeFromJson(result, Contact.class);
				contactPool.addElement(contact);
				return contact;
			}
		}
		return null;
	}

	@Override
	public void reset() {
		contactPool.reset();
	}

	/**
	 * Gets the user contact.
	 * 
	 * @param user
	 * @return
	 * @throws NotConnectedToWebServiceException
	 * @throws ClientProtocolException
	 * @throws IOException
	 * @throws AuthenticationRequired
	 * @throws WebServiceAccessError
	 */
	@Override
	public Contact getContact(User user) throws NotConnectedToWebServiceException, ClientProtocolException, IOException, AuthenticationRequired,
			WebServiceAccessError {
		if (user != null) {
			return getContact(user.getContactId());
		}
		return null;
	}
}
