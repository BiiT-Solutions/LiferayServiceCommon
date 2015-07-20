package com.biit.liferay.access;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.message.BasicNameValuePair;

import com.biit.liferay.access.exceptions.AuthenticationRequired;
import com.biit.liferay.access.exceptions.NotConnectedToWebServiceException;
import com.biit.liferay.access.exceptions.WebServiceAccessError;
import com.biit.usermanager.entity.IGroup;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.liferay.portal.model.Group;

public class GroupService extends ServiceAccess<IGroup<Long>, Group> {

	public GroupService() {
	}

	@Override
	public Set<IGroup<Long>> decodeListFromJson(String json, Class<Group> objectClass) throws JsonParseException,
			JsonMappingException, IOException {
		Set<IGroup<Long>> myObjects = new ObjectMapper().readValue(json, new TypeReference<Set<Group>>() {
		});
		return myObjects;
	}

	public IGroup<Long> getGroup(Long companyId, String groupName) throws NotConnectedToWebServiceException,
			ClientProtocolException, IOException, AuthenticationRequired, WebServiceAccessError {
		if (companyId != null && groupName != null) {
			// Look up user in the liferay.
			checkConnection();

			List<NameValuePair> params = new ArrayList<NameValuePair>();
			params.add(new BasicNameValuePair("companyId", companyId + ""));
			params.add(new BasicNameValuePair("name", groupName));

			String result = getHttpResponse("group/get-group", params);
			if (result != null) {
				// A Simple JSON Response Read
				IGroup<Long> group = decodeFromJson(result, Group.class);
				return group;
			}
		}
		return null;
	}

}
