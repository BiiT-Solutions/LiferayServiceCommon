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
import com.liferay.portal.model.ListType;

public class ListTypeService extends ServiceAccess<ListType, ListType> {
	private final static String FULL_MEMBER_TAG = "full-member";
	private final static String FULL_MEMBER_TYPE = "com.liferay.portal.model.Organization.status";

	public ListTypeService() {
	}

	@Override
	public Set<ListType> decodeListFromJson(String json, Class<ListType> objectClass)
			throws JsonParseException, JsonMappingException, IOException {
		Set<ListType> myObjects = new ObjectMapper().readValue(json, new TypeReference<Set<ListType>>() {
		});
		return myObjects;
	}

	public int getFullMemberStatus() throws NotConnectedToWebServiceException, ClientProtocolException, IOException,
			AuthenticationRequired, WebServiceAccessError {
		// Look up user in the liferay.
		checkConnection();

		List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("type", FULL_MEMBER_TYPE));

		String result = getHttpPostResponse("listtype/get-list-types", params);
		if (result != null) {
			// A Simple JSON Response Read
			Set<ListType> listTypes = decodeListFromJson(result, ListType.class);

			for (ListType listType : listTypes) {
				if (listType.getName().equals(FULL_MEMBER_TAG)) {
					return listType.getListTypeId().intValue();
				}
			}
		}
		return -1;
	}

	@Override
	public void reset() {
	}

}
