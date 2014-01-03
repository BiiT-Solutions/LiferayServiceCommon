package com.biit.liferay.access;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.AuthCache;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.BasicAuthCache;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.util.EntityUtils;

import com.biit.liferay.access.exceptions.AuthenticationRequired;
import com.biit.liferay.access.exceptions.NotConnectedToWebServiceException;
import com.biit.liferay.access.exceptions.WebServiceAccessError;
import com.biit.liferay.configuration.ConfigurationReader;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException;

/**
 * Common methods for accessing to a Liferay web service.
 */
public abstract class ServiceAccess<T> implements LiferayService {
	private CloseableHttpClient httpClient = null;
	private HttpHost targetHost;
	private BasicHttpContext httpContext = null;
	private String webservicesPath;

	@Override
	public boolean isNotConnected() {
		return httpClient == null;
	}

	@Override
	public void disconnect() {
		try {
			httpClient.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		httpClient = null;
	}

	@Override
	public CloseableHttpClient getHttpClient() throws NotConnectedToWebServiceException {
		checkConnection();
		return httpClient;
	}

	@Override
	public void serverConnection(String address, String protocol, int port, String webservicesPath,
			String authenticationToken, String loginUser, String password) {
		this.webservicesPath = webservicesPath;
		// Host definition
		targetHost = new HttpHost(address, port, protocol);
		// Credentials
		CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
		credentialsProvider.setCredentials(new AuthScope(targetHost.getHostName(), targetHost.getPort()),
				new UsernamePasswordCredentials(loginUser, password));

		// Client
		httpClient = HttpClients.custom().setDefaultCredentialsProvider(credentialsProvider).build();

		// Create AuthCache instance
		AuthCache authCache = new BasicAuthCache();
		// Generate BASIC scheme object and add it to the local auth cache
		BasicScheme basicScheme = new BasicScheme();
		authCache.put(targetHost, basicScheme);
		// Add AuthCache to the execution context
		httpContext = new BasicHttpContext();
		httpContext.setAttribute(HttpClientContext.AUTH_CACHE, authCache);
	}

	@Override
	public void serverConnection() {
		// Read user and password.
		String loginUser = ConfigurationReader.getInstance().getUser();
		String password = ConfigurationReader.getInstance().getPassword();
		String protocol = ConfigurationReader.getInstance().getLiferayProtocol();
		Integer port = Integer.parseInt(ConfigurationReader.getInstance().getConnectionPort());
		String address = ConfigurationReader.getInstance().getVirtualHost();
		String webservicesPath = ConfigurationReader.getInstance().getWebServicesPath();
		String authenticationToken = ConfigurationReader.getInstance().getAuthToken();

		serverConnection(address, protocol, port, webservicesPath, authenticationToken, loginUser, password);
	}

	@Override
	public void checkConnection() throws NotConnectedToWebServiceException {
		if (isNotConnected()) {
			throw new NotConnectedToWebServiceException(
					"User credentials are needed to use Liferay webservice. Use the 'connectToWebService' method for this.");
		}
	}

	public String getHttpResponse(String webService, List<NameValuePair> params) throws ClientProtocolException,
			IOException, NotConnectedToWebServiceException, AuthenticationRequired {
		// Set authentication param if defined.
		setAuthParam(params);

		HttpPost post = new HttpPost("/" + webservicesPath + webService);
		UrlEncodedFormEntity entity = new UrlEncodedFormEntity(params, "UTF-8");
		post.setEntity(entity);
		HttpResponse response = getHttpClient().execute(targetHost, post, httpContext);
		if (response.getEntity() != null) {
			// A Simple JSON Response Read
			String result = EntityUtils.toString(response.getEntity());
			if (result.contains("{\"exception\":\"Authenticated access required\"}")) {
				throw new AuthenticationRequired("Authenticated access required.");
			}
			return result;
		}
		return null;
	}

	public T decodeFromJson(String json, Class<T> objectClass) throws JsonParseException, JsonMappingException,
			IOException, NotConnectedToWebServiceException, WebServiceAccessError {
		try {
			T object = new ObjectMapper().readValue(json, objectClass);
			return object;
		} catch (UnrecognizedPropertyException e) {
			if (e.getMessage().startsWith("Unrecognized field \"exception\"")) {
				throw new WebServiceAccessError("Error accessing to the webservices:" + json);
			}
		}
		return null;
	}

	public abstract List<T> decodeListFromJson(String json, Class<T> objectClass) throws JsonParseException,
			JsonMappingException, IOException;

	public void setAuthParam(List<NameValuePair> params) {
		String authToken = ConfigurationReader.getInstance().getAuthToken();
		if (authToken != null && authToken.length() > 0) {
			params.add(new BasicNameValuePair("p_auth", authToken));
		}
	}

	public String convertMapToString(Map<String, String> map) {
		String result = "";
		for (String key : map.keySet()) {
			if (result.length() > 0) {
				result += ",";
			}
			result += key + ":" + map.get(key);
		}
		if (result.length() > 0) {
			result = "{" + result + "}";
		}
		return result;
	}

}
