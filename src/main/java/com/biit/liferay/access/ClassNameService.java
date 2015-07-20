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
import com.biit.usermanager.entity.IElement;
import com.biit.usermanager.entity.pool.ElementPool;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.liferay.portal.model.ClassName;

public class ClassNameService extends ServiceAccess<IElement<Long>, ClassName> {

	private ElementPool<Long> classNamePool;

	public ClassNameService() {
		classNamePool = new ElementPool<Long>();
	}

	@Override
	public Set<IElement<Long>> decodeListFromJson(String json, Class<ClassName> objectClass) throws JsonParseException,
			JsonMappingException, IOException {
		Set<IElement<Long>> myObjects = new ObjectMapper().readValue(json, new TypeReference<Set<ClassName>>() {
		});

		return myObjects;
	}

	public IElement<Long> getClassName(String value) throws NotConnectedToWebServiceException, ClientProtocolException,
			IOException, AuthenticationRequired, WebServiceAccessError {
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

		String result = getHttpResponse("classname/fetch-class-name", params);
		if (result != null) {
			// A Simple JSON Response Read
			className = decodeFromJson(result, ClassName.class);
			classNamePool.addElementByTag(className, value);
			return className;
		}
		return null;
	}
}
