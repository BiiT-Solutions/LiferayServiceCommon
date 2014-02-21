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
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.config.SocketConfig;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.BasicAuthCache;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.util.EntityUtils;

import com.biit.liferay.access.exceptions.AuthenticationRequired;
import com.biit.liferay.access.exceptions.NotConnectedToWebServiceException;
import com.biit.liferay.access.exceptions.WebServiceAccessError;
import com.biit.liferay.configuration.ConfigurationReader;
import com.biit.liferay.log.LiferayClientLogger;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException;

/**
 * Common methods for accessing to a Liferay web service.
 */
public abstract class ServiceAccess<T> implements LiferayService {
	private final static String NOT_AUTHORIZED_ERROR = "{\"exception\":\"Authenticated access required\"}";
	private final static String UNRECOGNIZED_FIELD_ERROR = "Unrecognized field \"exception\"";
	private HttpClient httpClientWithCredentials = null;
	private HttpClient httpClientWithoutCredentials = null;
	private HttpHost targetHost;
	private BasicHttpContext httpContext = null;
	private String webservicesPath;
	private String authenticatedWithUser;
	private String authToken;

	@Override
	public boolean isNotConnected() {
		return httpClientWithCredentials == null;
	}

	@Override
	public void disconnect() {
		if (httpClientWithCredentials instanceof CloseableHttpClient) {
			try {
				((CloseableHttpClient) httpClientWithCredentials).close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		httpClientWithCredentials = null;
		if (httpClientWithoutCredentials instanceof CloseableHttpClient) {
			try {
				((CloseableHttpClient) httpClientWithoutCredentials).close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		httpClientWithoutCredentials = null;
	}

	private HttpClient getAuthorizedHttpClient() throws NotConnectedToWebServiceException {
		checkConnection();
		return httpClientWithCredentials;
	}

	private HttpClient getHttpClientWithoutCredentials() throws NotConnectedToWebServiceException {
		checkConnection();
		return httpClientWithoutCredentials;
	}

	@Override
	public void authorizedServerConnection(String address, String protocol, int port, String webservicesPath,
			String authenticationToken, String loginUser, String password) {
		this.webservicesPath = webservicesPath;
		// Host definition
		targetHost = new HttpHost(address, port, protocol);
		// Credentials
		CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
		credentialsProvider.setCredentials(new AuthScope(targetHost.getHostName(), targetHost.getPort()),
				new UsernamePasswordCredentials(loginUser, password));

		// Client
		SocketConfig defaultSocketConfig = SocketConfig.custom().setTcpNoDelay(true).build();
		SocketConfig socketConfig = SocketConfig.custom().setTcpNoDelay(true).setSoKeepAlive(true)
				.setSoReuseAddress(true).build();

		// Creates a client with credentials.
		PoolingHttpClientConnectionManager connManager = new PoolingHttpClientConnectionManager();
		connManager.setDefaultSocketConfig(defaultSocketConfig);
		connManager.setSocketConfig(new HttpHost(address, port), socketConfig);
		httpClientWithCredentials = HttpClients.custom().setDefaultCredentialsProvider(credentialsProvider)
				.setConnectionManager(connManager).build();

		// Creates a client without credentials.
		SocketConfig defaultSocketConfig2 = SocketConfig.custom().setTcpNoDelay(true).build();
		SocketConfig socketConfig2 = SocketConfig.custom().setTcpNoDelay(true).setSoKeepAlive(true)
				.setSoReuseAddress(true).build();
		PoolingHttpClientConnectionManager connManager2 = new PoolingHttpClientConnectionManager();
		connManager2.setDefaultSocketConfig(defaultSocketConfig2);
		connManager2.setSocketConfig(new HttpHost(address, port), socketConfig2);
		httpClientWithoutCredentials = HttpClients.custom().setConnectionManager(connManager2).build();

		createAuthCache();

		authenticatedWithUser = loginUser;

		authToken = authenticationToken;
	}

	/**
	 * Stores authentication data.
	 */
	private void createAuthCache() {
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

		authorizedServerConnection(address, protocol, port, webservicesPath, authenticationToken, loginUser, password);
	}

	@Override
	public void checkConnection() throws NotConnectedToWebServiceException {
		if (isNotConnected()) {
			throw new NotConnectedToWebServiceException(
					"User credentials are needed to use Liferay webservice. Use the 'connectToWebService' method for this.");
		}
	}

	public String getHttpResponse(String webService, List<NameValuePair> params, boolean useAuthorization)
			throws ClientProtocolException, IOException, NotConnectedToWebServiceException, AuthenticationRequired {
		// Set authentication param if defined.
		long startTime = System.currentTimeMillis();

		// Add p_auth token to the parameters
		List<NameValuePair> authParams = new ArrayList<NameValuePair>(params);
		setAuthParam(authParams);

		HttpPost post = new HttpPost("/" + webservicesPath + webService);
		UrlEncodedFormEntity entity = new UrlEncodedFormEntity(authParams, "UTF-8");
		post.setEntity(entity);
		HttpResponse response;
		if (useAuthorization) {
			response = getAuthorizedHttpClient().execute(targetHost, post, httpContext);
		} else {
			response = getHttpClientWithoutCredentials().execute(targetHost, post, httpContext);
		}
		if (response.getEntity() != null) {
			// A Simple JSON Response Read
			String result = EntityUtils.toString(response.getEntity());

			if (result.contains(NOT_AUTHORIZED_ERROR)) {
				if (!useAuthorization) {
					LiferayClientLogger.debug(ServiceAccess.class.getName(), "Accessed to '" + webService
							+ "' without authorization. Retry with authorization ("
							+ (System.currentTimeMillis() - startTime) + " ms).");
					// Redo authorization cache for invalid or expired.
					createAuthCache();
					return getHttpResponse(webService, params, true);
				} else {
					throw new AuthenticationRequired("Authenticated access required.");
				}
			}

			// Measure response time.
			LiferayClientLogger.info(ServiceAccess.class.getName(),
					"Accessed to '" + webService + "' (" + (System.currentTimeMillis() - startTime) + " ms).");
			if (LiferayClientLogger.isDebugEnabled()) {
				String paramsText = "";
				for (NameValuePair param : authParams) {
					if (paramsText.length() > 0) {
						paramsText += ", ";
					}
					paramsText += "(" + param.getName() + ", " + param.getValue() + ")";
				}
				LiferayClientLogger.debug(ServiceAccess.class.getName(), "Using parameters: [" + paramsText + "]");
			}
			return result;
		}
		return null;
	}

	/**
	 * Gets a response for a webservice. If it is not authorized to use the web service, try to authorize first.
	 * 
	 * @param webService
	 * @param params
	 * @return
	 * @throws ClientProtocolException
	 * @throws IOException
	 * @throws NotConnectedToWebServiceException
	 * @throws AuthenticationRequired
	 */
	public String getHttpResponse(String webService, List<NameValuePair> params) throws ClientProtocolException,
			IOException, NotConnectedToWebServiceException, AuthenticationRequired {
		return getHttpResponse(webService, params, false);
	}

	public T decodeFromJson(String json, Class<T> objectClass) throws JsonParseException, JsonMappingException,
			IOException, NotConnectedToWebServiceException, WebServiceAccessError {
		LiferayClientLogger.info(ServiceAccess.class.getName(), "Decoding JSON object: " + json);
		try {
			T object = new ObjectMapper().readValue(json, objectClass);
			return object;
		} catch (UnrecognizedPropertyException e) {
			if (e.getMessage().startsWith(UNRECOGNIZED_FIELD_ERROR)) {
				throw new WebServiceAccessError("Error accessing to the webservices:" + json);
			} else {
				throw e;
			}
		}
	}

	public abstract List<T> decodeListFromJson(String json, Class<T> objectClass) throws JsonParseException,
			JsonMappingException, IOException;

	public void setAuthParam(List<NameValuePair> params) {
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

	protected HttpHost getTargetHost() {
		return targetHost;
	}

	public String getAuthenticatedWithUser() {
		return authenticatedWithUser;
	}

}
