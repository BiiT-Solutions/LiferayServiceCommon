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
import com.biit.usermanager.entity.IElement;
import com.biit.usermanager.entity.pool.ElementPool;
import com.biit.usermanager.security.exceptions.AuthenticationRequired;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.liferay.portal.model.ClassName;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ClassNameService extends ServiceAccess<IElement<Long>, ClassName> {

    private ElementPool<Long> classNamePool;

    public ClassNameService() {
        classNamePool = new ElementPool<Long>();
    }

    @Override
    public Set<IElement<Long>> decodeListFromJson(String json, Class<ClassName> objectClass)
            throws IOException {
        Set<ClassName> myObjects = new ObjectMapper().readValue(json, new TypeReference<Set<ClassName>>() {
        });
        return new HashSet<>(myObjects);
    }

    public IElement<Long> getClassName(String value) throws NotConnectedToWebServiceException, IOException, AuthenticationRequired,
            WebServiceAccessError {
        IElement<Long> className;
        Set<IElement<Long>> classNames = classNamePool.getElementsByTag(value);
        if (classNames != null && !classNames.isEmpty()) {
            className = classNames.iterator().next();
            if (className != null) {
                return className;
            }
        }

        checkConnection();

        List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("value", value));

        String result = getHttpPostResponse("classname/fetch-class-name", params);
        if (result != null) {
            // A Simple JSON Response Read
            className = decodeFromJson(result, ClassName.class);
            classNamePool.addElementByTag(className, value);
            return className;
        }
        return null;
    }

    @Override
    public void reset() {

    }
}
