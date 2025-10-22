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
