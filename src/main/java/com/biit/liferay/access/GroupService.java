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
import com.biit.usermanager.entity.IGroup;
import com.biit.usermanager.entity.pool.GroupPool;
import com.biit.usermanager.security.exceptions.AuthenticationRequired;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.liferay.portal.model.Group;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.message.BasicNameValuePair;

import javax.inject.Named;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Named
public class GroupService extends ServiceAccess<IGroup<Long>, Group> implements IGroupService {

	private GroupPool<Long, Long> groupPool;

	public GroupService() {
		groupPool = new GroupPool<Long, Long>();
	}

	@Override
	public Set<IGroup<Long>> decodeListFromJson(String json, Class<Group> objectClass) throws JsonParseException, JsonMappingException, IOException {
		Set<Group> myObjects = new ObjectMapper().readValue(json, new TypeReference<Set<Group>>() {
		});
		return new HashSet<>(myObjects);
	}

	@Override
	public IGroup<Long> getGroup(Long companyId, String groupName) throws NotConnectedToWebServiceException, ClientProtocolException, IOException,
			AuthenticationRequired, WebServiceAccessError {
		if (companyId != null && groupName != null) {

			Set<IGroup<Long>> groups = groupPool.getElementsByTag(groupName);
			// Only one group by name.
			if (groups != null && groups.size() > 0) {
				return groups.iterator().next();
			}

			// Look up user in the liferay.
			checkConnection();

			List<NameValuePair> params = new ArrayList<NameValuePair>();
			params.add(new BasicNameValuePair("companyId", companyId + ""));
			params.add(new BasicNameValuePair("name", groupName));

			String result = getHttpPostResponse("group/get-group", params);
			if (result != null) {
				// A Simple JSON Response Read
				IGroup<Long> group = decodeFromJson(result, Group.class);
				groupPool.addElementByTag(group, groupName);
				return group;
			}
		}
		return null;
	}

	@Override
	public void reset() {
		groupPool.reset();
	}

}
