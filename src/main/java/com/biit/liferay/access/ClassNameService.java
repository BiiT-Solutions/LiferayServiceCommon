package com.biit.liferay.access;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.message.BasicNameValuePair;

import com.biit.liferay.access.exceptions.AuthenticationRequired;
import com.biit.liferay.access.exceptions.NotConnectedToWebServiceException;
import com.biit.liferay.access.exceptions.WebServiceAccessError;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.liferay.portal.model.ClassName;

public class ClassNameService extends ServiceAccess<ClassName> {

	public ClassNameService() {
	}

	@Override
	public List<ClassName> decodeListFromJson(String json, Class<ClassName> objectClass) throws JsonParseException,
			JsonMappingException, IOException {
		List<ClassName> myObjects = new ObjectMapper().readValue(json, new TypeReference<List<ClassName>>() {
		});

		return myObjects;
	}

	public ClassName getClassName(String value) throws NotConnectedToWebServiceException, ClientProtocolException,
			IOException, AuthenticationRequired, WebServiceAccessError {
		ClassName className = ClassNamePool.getInstance().getClassName(value);
		if (className != null) {
			return className;
		}

		checkConnection();

		List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("value", value));

		String result = getHttpResponse("classname/fetch-class-name", params);
		if (result != null) {
			// A Simple JSON Response Read
			className = decodeFromJson(result, ClassName.class);
			ClassNamePool.getInstance().addClassName(value, className);
			return className;
		}
		return null;
	}

}
