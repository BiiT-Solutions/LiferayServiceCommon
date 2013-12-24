package com.biit.liferay.access;

import java.io.IOException;
import java.util.List;

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
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.util.EntityUtils;

import com.biit.liferay.access.exceptions.NotConnectedToWebServiceException;
import com.biit.liferay.configuration.ConfigurationReader;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Common methods for accessing to a Liferay web service.
 */
public abstract class ServiceAccess<T> implements LiferayService {
	private CloseableHttpClient httpClient = null;
	private HttpHost targetHost;
	private BasicHttpContext httpContext;
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
	public void serverConnection(String address, String protocol, int port, String webservicesPath, String loginUser,
			String password) {
		this.webservicesPath = webservicesPath;
		// Host definition
		targetHost = new HttpHost(address, port, protocol);
		// Credentials
		CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
		credentialsProvider.setCredentials(new AuthScope(targetHost), new UsernamePasswordCredentials(loginUser,
				password));
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

		// Locate the Role service.
		serverConnection(address, protocol, port, webservicesPath, loginUser, password);
	}

	@Override
	public void checkConnection() throws NotConnectedToWebServiceException {
		if (isNotConnected()) {
			throw new NotConnectedToWebServiceException(
					"User credentials are needed to use Liferay webservice. Use the 'connectToWebService' method for this.");
		}
	}

	public String getHttpResponse(String webService, List<NameValuePair> params) throws ClientProtocolException,
			IOException, NotConnectedToWebServiceException {
		HttpPost post = new HttpPost("/" + webservicesPath + webService);
		UrlEncodedFormEntity entity = new UrlEncodedFormEntity(params, "UTF-8");
		post.setEntity(entity);
		HttpResponse response = getHttpClient().execute(targetHost, post, httpContext);
		if (response.getEntity() != null) {
			// A Simple JSON Response Read
			String result = EntityUtils.toString(response.getEntity());
			return result;
		}
		return null;
	}

	public T decodeFromJason(String json, Class<T> objectClass) throws JsonParseException, JsonMappingException, IOException {
		T company = new ObjectMapper().readValue(json, objectClass);
		return company;
	}

}
