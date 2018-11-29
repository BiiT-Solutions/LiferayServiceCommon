package com.biit.liferay.access;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.PostConstruct;

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
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.BasicAuthCache;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.simple.JSONValue;

import com.biit.liferay.access.exceptions.NotConnectedToWebServiceException;
import com.biit.liferay.access.exceptions.WebServiceAccessError;
import com.biit.liferay.configuration.LiferayConfigurationReader;
import com.biit.liferay.log.LiferayClientLogger;
import com.biit.usermanager.security.exceptions.AuthenticationRequired;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException;

/**
 * Common methods for accessing to a Liferay web service.
 */
public abstract class ServiceAccess<Type, LiferayType extends Type> implements LiferayService, IServiceAccess {
	public static final String LIFERAY_ORGANIZATION_GROUP_SUFIX = " LFR_ORGANIZATION";
	private final static String NOT_AUTHORIZED_ERROR_LIFERAY_621 = "{\"exception\":\"Authenticated access required\"}";
	private final static String NOT_AUTHORIZED_ERROR_LIFERAY_625 = "{\"exception\":\"java.lang.SecurityException\",\"message\":\"Authenticated access required\"}";
	private final static String UNRECOGNIZED_FIELD_ERROR = "Unrecognized field \"exception\"";
	private final static String EXCEPTION_PREFIX = "{\"exception\":";
	private HttpClient httpClientWithCredentials = null;
	private HttpClient httpClientWithoutCredentials = null;
	private HttpHost targetHost;
	private HttpClientContext httpContext = null;
	private String webservicesPath;
	private String connectionUser;
	private String connectionPassword;
	private String authToken;
	private CredentialsProvider credentialsProvider;
	private String host;
	private String protocol;
	private int port;

	@Override
	public void authorizedServerConnection(String host, String protocol, int port, String webservicesPath, String authenticationToken, String loginUser,
			String password) {
		this.host = host;
		this.protocol = protocol;
		this.port = port;

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

		httpClientWithCredentials = HttpClients.custom().setDefaultCredentialsProvider(credentialsProvider).setConnectionManager(connManager).build();
		// httpClientWithCredentials.getParams().setAuthenticationPreemptive(true);

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

	protected HttpClientContext createAuthCache(CredentialsProvider credentialsProvider) {
		// Create AuthCache instance
		AuthCache authCache = new BasicAuthCache();
		// Generate BASIC scheme object and add it to the local auth cache
		authCache.put(targetHost, new BasicScheme());
		// Add AuthCache to the execution context
		httpContext = HttpClientContext.create();
		httpContext.setCredentialsProvider(credentialsProvider);
		httpContext.setAuthCache(authCache);

		return httpContext;
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
				LiferayClientLogger.error(this.getClass().getName(), "User not authorized: " + json);
				throw new WebServiceAccessError("User not authorized: " + json);
			}
			if (json.startsWith(EXCEPTION_PREFIX)) {
				// If @JsonIgnoreProperties(ignoreUnknown = true) is on
				// entities, the JsonMappingException exception is
				// never launch. We need to check it here.
				LiferayClientLogger.error(this.getClass().getName(), "Invalid retrieved json '" + json + "'.");
				throw new WebServiceAccessError("Invalid retrieved json '" + json + "'.");
			}
			LiferayType object = new ObjectMapper().readValue(json, objectClass);
			return object;
		} catch (UnrecognizedPropertyException e) {
			if (e.getMessage().startsWith(UNRECOGNIZED_FIELD_ERROR)) {
				LiferayClientLogger.errorMessage(this.getClass().getName(), e);
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

	public String getHttpResponse(String webService, List<NameValuePair> params) throws ClientProtocolException, IOException,
			NotConnectedToWebServiceException, AuthenticationRequired {
		return getHttpResponse(LiferayConfigurationReader.getInstance().getProxyPrefix(), webService, params, true);
	}

	/**
	 * Gets a response for a webservice. If it is not authorized to use the web
	 * service, try to authorize first.
	 *
	 * @param proxyPrefix
	 * @param webService
	 * @param params
	 * @return
	 * @throws ClientProtocolException
	 * @throws IOException
	 * @throws NotConnectedToWebServiceException
	 * @throws AuthenticationRequired
	 */
	public String getHttpResponse(String proxyPrefix, String webService, List<NameValuePair> params) throws ClientProtocolException, IOException,
			NotConnectedToWebServiceException, AuthenticationRequired {
		return getHttpResponse(proxyPrefix, webService, params, true);
	}

	private String getHttpResponse(String proxyPrefix, String webService, List<NameValuePair> params, boolean useAuthorization) throws ClientProtocolException,
			IOException, NotConnectedToWebServiceException, AuthenticationRequired {
		// Set authentication param if defined.
		long startTime = System.currentTimeMillis();

		// Add p_auth token to the parameters
		List<NameValuePair> authParams = new ArrayList<NameValuePair>(params);
		setAuthParam(authParams);

		// Add URL separators.
		if (proxyPrefix == null) {
			proxyPrefix = "";
		}
		proxyPrefix = proxyPrefix + (proxyPrefix.endsWith("/") || proxyPrefix.length() == 0 ? "" : "/");
		String webservicesPath = this.webservicesPath + (this.webservicesPath.endsWith("/") || this.webservicesPath.length() == 0 ? "" : "/");

		HttpPost post = new HttpPost("/" + proxyPrefix + webservicesPath + webService);
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

			if (result.contains(NOT_AUTHORIZED_ERROR_LIFERAY_621) || result.contains(NOT_AUTHORIZED_ERROR_LIFERAY_625)) {
				if (!useAuthorization) {
					LiferayClientLogger.debug(ServiceAccess.class.getName(), "Accessing to '" + webService
							+ "' without authorization. Retry with authorization (" + (System.currentTimeMillis() - startTime) + " ms).");
					// Redo authorization cache for invalid or expired.
					createAuthCache(credentialsProvider);
					return getHttpResponse(proxyPrefix, webService, params, true);
				} else {
					LiferayClientLogger.debug(this.getClass().getName(), "Accessing using protocol '" + protocol + "', host '" + host + "', port '" + port
							+ "', proxyPrefix '" + proxyPrefix + "', path '" + webservicesPath + "', user '" + connectionUser + "', password '"
							+ connectionPassword + "'.");
					throw new AuthenticationRequired("Authenticated access failed!");
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

	/**
	 * Used for sending files to a webservice.
	 * 
	 * @param webService
	 * @param builder
	 * @param useAuthorization
	 * @return
	 * @throws ClientProtocolException
	 * @throws IOException
	 * @throws NotConnectedToWebServiceException
	 * @throws AuthenticationRequired
	 */
	public String getHttpResponse(String webService, MultipartEntityBuilder builder) throws ClientProtocolException, IOException,
			NotConnectedToWebServiceException, AuthenticationRequired {
		return getHttpResponse(webService, builder, true);
	}

	/**
	 * Used for sending files to a webservice.
	 * 
	 * @param webService
	 * @param builder
	 * @param useAuthorization
	 * @return
	 * @throws ClientProtocolException
	 * @throws IOException
	 * @throws NotConnectedToWebServiceException
	 * @throws AuthenticationRequired
	 */
	public String getHttpResponse(String webService, MultipartEntityBuilder builder, boolean useAuthorization) throws ClientProtocolException, IOException,
			NotConnectedToWebServiceException, AuthenticationRequired {
		long startTime = System.currentTimeMillis();

		builder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
		builder.setCharset(Charset.forName("UTF-8"));

		HttpPost httpPost = new HttpPost("/" + webservicesPath + webService);
		httpPost.setEntity(builder.build());

		HttpResponse response;
		if (useAuthorization) {
			LiferayClientLogger.debug(ServiceAccess.class.getName(), "Accessing with credentials to '" + targetHost + "' service '" + httpPost + "'!");
			response = getAuthorizedHttpClient().execute(targetHost, httpPost, httpContext);
		} else {
			LiferayClientLogger.debug(ServiceAccess.class.getName(), "Accessing without credentials!");
			response = getHttpClientWithoutCredentials().execute(targetHost, httpPost, httpContext);
		}
		if (response.getEntity() != null) {
			// A Simple JSON Response Read
			String result = EntityUtils.toString(response.getEntity());

			if (result.contains(NOT_AUTHORIZED_ERROR_LIFERAY_621) || result.contains(NOT_AUTHORIZED_ERROR_LIFERAY_625)) {
				if (!useAuthorization) {
					LiferayClientLogger.debug(ServiceAccess.class.getName(), "Accessing to '" + webService
							+ "' without authorization. Retry with authorization (" + (System.currentTimeMillis() - startTime) + " ms).");
					// Redo authorization cache for invalid or expired.
					createAuthCache(credentialsProvider);
					return getHttpResponse(webService, builder, true);
				} else {
					throw new AuthenticationRequired("Authenticated access failed.");
				}
			}

			// Measure response time.
			LiferayClientLogger
					.debug(ServiceAccess.class.getName(), "Accessing to '" + webService + "' (" + (System.currentTimeMillis() - startTime) + " ms).");
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

	@PostConstruct
	@Override
	public void serverConnection() {
		// Read user and password.
		if (isNotConnected()) {
			String loginUser = LiferayConfigurationReader.getInstance().getUser();
			String password = LiferayConfigurationReader.getInstance().getPassword();
			String protocol = LiferayConfigurationReader.getInstance().getLiferayProtocol();
			Integer port = Integer.parseInt(LiferayConfigurationReader.getInstance().getConnectionPort());
			String host = LiferayConfigurationReader.getInstance().getHost();
			String webservicesPath = LiferayConfigurationReader.getInstance().getWebServicesPath();
			String authenticationToken = LiferayConfigurationReader.getInstance().getAuthToken();

			authorizedServerConnection(host, protocol, port, webservicesPath, authenticationToken, loginUser, password);
		}
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

	protected String convertToJson(List<String> listToConvert) throws WebServiceAccessError {
		if (listToConvert == null) {
			return "";
		}
		try {
			return new ObjectMapper().writeValueAsString(listToConvert);
		} catch (JsonProcessingException jpe) {
			LiferayClientLogger.error(this.getClass().getName(), "Error converting to json '" + listToConvert + "'.");
			throw new WebServiceAccessError("Invalid parameter passed to webservice");
		}
	}

	protected String convertToJson(Map<Long, String[]> mapToConvert) throws WebServiceAccessError {
		if (mapToConvert == null) {
			return "";
		}
		try {
			return new ObjectMapper().writeValueAsString(mapToConvert).replaceAll("\\{", "").replace("\\}", "");
		} catch (JsonProcessingException jpe) {
			LiferayClientLogger.error(this.getClass().getName(), "Error converting to json '" + mapToConvert + "'.");
			throw new WebServiceAccessError("Invalid parameter passed to webservice");
		}
	}

	public String getHost() {
		return host;
	}

	public String getProtocol() {
		return protocol;
	}

	public int getPort() {
		return port;
	}

}
