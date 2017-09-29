package com.biit.liferay.access;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.codec.binary.Base64;
import org.apache.http.Header;
import org.apache.http.HttpHeaders;
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
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.simple.JSONValue;

import com.biit.liferay.access.exceptions.NotConnectedToWebServiceException;
import com.biit.liferay.access.exceptions.WebServiceAccessError;
import com.biit.liferay.configuration.LiferayConfigurationReader;
import com.biit.liferay.log.LiferayClientLogger;
import com.biit.usermanager.security.exceptions.AuthenticationRequired;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException;

/**
 * Common methods for accessing to a Liferay web service.
 */
public abstract class ServiceAccess<Type, LiferayType extends Type> implements LiferayService {
	public static final String LIFERAY_ORGANIZATION_GROUP_SUFIX = " LFR_ORGANIZATION";
	private final static String NOT_AUTHORIZED_ERROR = "{\"exception\":\"Authenticated access required\"}";
	private final static String UNRECOGNIZED_FIELD_ERROR = "Unrecognized field \"exception\"";
	private HttpClient httpClientWithCredentials = null;
	private HttpClient httpClientWithoutCredentials = null;
	private HttpHost targetHost;
	private HttpClientContext httpContext = null;
	private String webservicesPath;
	private String connectionUser;
	private String connectionPassword;
	private String authToken;
	private CredentialsProvider credentialsProvider;

	@Override
	public void authorizedServerConnection(String host, String protocol, int port, String webservicesPath, String authenticationToken, String loginUser,
			String password) {

		LiferayClientLogger.debug(this.getClass().getName(), "Accessing using protocol '" + protocol + "', host '" + host + "', port '" + port + "', path '"
				+ webservicesPath + "', user '" + loginUser + "', password '" + password + "'.");

		this.webservicesPath = webservicesPath;
		// Host definition
		targetHost = new HttpHost(host, port, protocol);

		// Credentials
		credentialsProvider = new BasicCredentialsProvider();
		credentialsProvider.setCredentials(new AuthScope(targetHost.getHostName(), targetHost.getPort()), new UsernamePasswordCredentials(loginUser, password));

		createAuthCache(credentialsProvider);

		// Client
		SocketConfig defaultSocketConfig = SocketConfig.custom().setTcpNoDelay(true).build();
		SocketConfig socketConfig = SocketConfig.custom().setTcpNoDelay(true).setSoKeepAlive(true).setSoReuseAddress(true).build();

		// Creates a client with credentials.
		PoolingHttpClientConnectionManager connManager = new PoolingHttpClientConnectionManager();
		connManager.setDefaultSocketConfig(defaultSocketConfig);
		connManager.setSocketConfig(new HttpHost(host, port), socketConfig);

		// Add preemtive authentication as a header.
		String auth = connectionUser + ":" + connectionPassword;
		byte[] encodedAuth = Base64.encodeBase64(auth.getBytes(Charset.forName("US-ASCII")));
		String authHeader = "Basic " + new String(encodedAuth);
		Header header = new BasicHeader(HttpHeaders.AUTHORIZATION, authHeader);

		Set<Header> headers = new HashSet<Header>();
		headers.add(header);

		httpClientWithCredentials = HttpClients.custom().setDefaultHeaders(headers).setDefaultCredentialsProvider(credentialsProvider)
				.setConnectionManager(connManager).build();

		// Creates a client without credentials.
		SocketConfig defaultSocketConfig2 = SocketConfig.custom().setTcpNoDelay(true).build();
		SocketConfig socketConfig2 = SocketConfig.custom().setTcpNoDelay(true).setSoKeepAlive(true).setSoReuseAddress(true).build();
		PoolingHttpClientConnectionManager connManager2 = new PoolingHttpClientConnectionManager();
		connManager2.setDefaultSocketConfig(defaultSocketConfig2);
		connManager2.setSocketConfig(new HttpHost(host, port), socketConfig2);
		httpClientWithoutCredentials = HttpClients.custom().setConnectionManager(connManager2).build();

		connectionUser = loginUser;
		connectionPassword = password;

		authToken = authenticationToken;
	}

	private void createAuthCache(CredentialsProvider credentialsProvider) {
		// Create AuthCache instance
		AuthCache authCache = new BasicAuthCache();
		// Generate BASIC scheme object and add it to the local auth cache
		authCache.put(targetHost, new BasicScheme());
		// Add AuthCache to the execution context
		httpContext = HttpClientContext.create();
		httpContext.setCredentialsProvider(credentialsProvider);
		httpContext.setAuthCache(authCache);
	}

	@Override
	public void checkConnection() throws NotConnectedToWebServiceException {
		if (isNotConnected()) {
			throw new NotConnectedToWebServiceException("User credentials are needed to use Liferay webservice. Use the 'connectToWebService' method for this.");
		}
	}

	public Type decodeFromJson(String json, Class<LiferayType> objectClass) throws JsonParseException, JsonMappingException, IOException,
			NotConnectedToWebServiceException, WebServiceAccessError {
		LiferayClientLogger.debug(ServiceAccess.class.getName(), "Decoding JSON object: " + json);
		try {
			ObjectMapper jsonMapper = new ObjectMapper();
			jsonMapper.configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true);
			if (json.contains("Authenticated access required")) {
				LiferayClientLogger.debug(this.getClass().getName(), "User '" + connectionUser + "' with password '" + connectionPassword
						+ "' not authorized in Liferay.");
				throw new WebServiceAccessError("User not authorized: " + json);
			}
			LiferayType object = new ObjectMapper().readValue(json, objectClass);
			return object;
		} catch (UnrecognizedPropertyException e) {
			if (e.getMessage().startsWith(UNRECOGNIZED_FIELD_ERROR)) {
				throw new WebServiceAccessError("Error accessing to the webservices:" + json);
			} else {
				throw e;
			}
		} catch (JsonMappingException jme) {
			LiferayClientLogger.errorMessage(this.getClass().getName(), jme);
			throw new WebServiceAccessError("Invalid retrieved json '" + json + "'.");
		}
	}

	public abstract Set<Type> decodeListFromJson(String json, Class<LiferayType> objectClass) throws JsonParseException, JsonMappingException, IOException;

	@Override
	public void disconnect() {
		if (httpClientWithCredentials instanceof CloseableHttpClient) {
			try {
				((CloseableHttpClient) httpClientWithCredentials).close();
			} catch (IOException e) {
				// e.printStackTrace();
			}
		}
		httpClientWithCredentials = null;
		if (httpClientWithoutCredentials instanceof CloseableHttpClient) {
			try {
				((CloseableHttpClient) httpClientWithoutCredentials).close();
			} catch (IOException e) {
				// e.printStackTrace();
			}
		}
		httpClientWithoutCredentials = null;
	}

	@SuppressWarnings("rawtypes")
	public String encodeMapToJson(Map map) {
		return JSONValue.toJSONString(map);
	}

	private HttpClient getAuthorizedHttpClient() throws NotConnectedToWebServiceException {
		checkConnection();
		return httpClientWithCredentials;
	}

	public String getConnectionPassword() {
		return connectionPassword;
	}

	public String getConnectionUser() {
		return connectionUser;
	}

	private HttpClient getHttpClientWithoutCredentials() throws NotConnectedToWebServiceException {
		checkConnection();
		return httpClientWithoutCredentials;
	}

	/**
	 * Gets a response for a webservice. If it is not authorized to use the web
	 * service, try to authorize first.
	 * 
	 * @param webService
	 * @param params
	 * @return
	 * @throws ClientProtocolException
	 * @throws IOException
	 * @throws NotConnectedToWebServiceException
	 * @throws AuthenticationRequired
	 */
	public String getHttpResponse(String webService, List<NameValuePair> params) throws ClientProtocolException, IOException,
			NotConnectedToWebServiceException, AuthenticationRequired {
		return getHttpResponse(webService, params, false);
	}

	public String getHttpResponse(String webService, List<NameValuePair> params, boolean useAuthorization) throws ClientProtocolException, IOException,
			NotConnectedToWebServiceException, AuthenticationRequired {
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
			LiferayClientLogger.debug(ServiceAccess.class.getName(), "Accessing with credentials to '" + targetHost + "' service '" + post + "'!");
			response = getAuthorizedHttpClient().execute(targetHost, post, httpContext);
		} else {
			LiferayClientLogger.debug(ServiceAccess.class.getName(), "Accessing without credentials!");
			response = getHttpClientWithoutCredentials().execute(targetHost, post, httpContext);
		}
		if (response.getEntity() != null) {
			// A Simple JSON Response Read
			String result = EntityUtils.toString(response.getEntity());

			if (result.contains(NOT_AUTHORIZED_ERROR)) {
				if (!useAuthorization) {
					LiferayClientLogger.debug(ServiceAccess.class.getName(), "Accessing to '" + webService
							+ "' without authorization. Retry with authorization (" + (System.currentTimeMillis() - startTime) + " ms).");
					// Redo authorization cache for invalid or expired.
					createAuthCache(credentialsProvider);
					return getHttpResponse(webService, params, true);
				} else {
					throw new AuthenticationRequired("Authenticated access required.");
				}
			}

			// Measure response time.
			LiferayClientLogger
					.debug(ServiceAccess.class.getName(), "Accessing to '" + webService + "' (" + (System.currentTimeMillis() - startTime) + " ms).");
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
			LiferayClientLogger.debug(ServiceAccess.class.getName(), "Obtained result: " + result);
			return result;
		}
		return null;
	}

	protected HttpHost getTargetHost() {
		return targetHost;
	}

	@Override
	public boolean isNotConnected() {
		return httpClientWithCredentials == null && httpClientWithoutCredentials == null;
	}

	@Override
	public void serverConnection() {
		// Read user and password.
		String loginUser = LiferayConfigurationReader.getInstance().getUser();
		String password = LiferayConfigurationReader.getInstance().getPassword();
		String protocol = LiferayConfigurationReader.getInstance().getLiferayProtocol();
		Integer port = Integer.parseInt(LiferayConfigurationReader.getInstance().getConnectionPort());
		String host = LiferayConfigurationReader.getInstance().getHost();
		String webservicesPath = LiferayConfigurationReader.getInstance().getWebServicesPath();
		String authenticationToken = LiferayConfigurationReader.getInstance().getAuthToken();

		authorizedServerConnection(host, protocol, port, webservicesPath, authenticationToken, loginUser, password);
	}

	@Override
	public void serverConnection(String user, String password) {
		// Read user and password.
		String protocol = LiferayConfigurationReader.getInstance().getLiferayProtocol();
		Integer port = Integer.parseInt(LiferayConfigurationReader.getInstance().getConnectionPort());
		String host = LiferayConfigurationReader.getInstance().getHost();
		String webservicesPath = LiferayConfigurationReader.getInstance().getWebServicesPath();
		String authenticationToken = LiferayConfigurationReader.getInstance().getAuthToken();

		authorizedServerConnection(host, protocol, port, webservicesPath, authenticationToken, user, password);
	}

	public void setAuthParam(List<NameValuePair> params) {
		if (authToken != null && authToken.length() > 0) {
			params.add(new BasicNameValuePair("p_auth", authToken));
		}
	}

	protected String convertListToJsonString(List<String> list) {
		String listAsString = "";
		if (list != null) {
			if (!list.isEmpty()) {
				listAsString = "[";
			}
			for (String section : list) {
				if (listAsString.length() > 1) {
					listAsString += ",";
				}
				listAsString += section;
			}
			if (listAsString.length() > 0) {
				listAsString += "]";
			}
		}
		return listAsString;
	}

}
