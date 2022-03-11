package com.biit.liferay.access;

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
