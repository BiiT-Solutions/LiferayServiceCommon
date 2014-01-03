package com.biit.liferay.configuration;

import java.io.IOException;
import java.util.Properties;

import com.biit.liferay.security.PasswordEncryptationAlgorithmType;
import com.biit.utils.file.PropertiesFile;

public class ConfigurationReader {
	private final String LIFERAY_CONFIG_FILE = "liferay.conf";

	private static final String USER_TAG = "user";
	private static final String PASSWORD_TAG = "password";
	private static final String VIRTUAL_HOST_TAG = "virtualhost";
	private static final String WEBAPP_TAG = "webapp";
	private static final String PORT_TAG = "port";
	// Password algorithm
	private final String PASSWORD_ALGORITHM_TAG = "passwordEncriptationAlgorithm";
	private final String WEBSERVICES_PATH_TAG = "webservices";
	private final String LIFERAY_PROTOCOL_TAG = "liferayProtocol";
	private final String AUTH_TOKEN_TAG = "p_auth";

	private final String DEFAULT_USER = "user";
	private final String DEFAULT_PASSWORD = "pass";
	private final String DEFAULT_VIRTUAL_HOST = "localhost";
	private final String DEFAULT_LIFERAY_WEBAPP = "lportal-6.1.1";
	private final String DEFAULT_PORT = "8080";
	private final String DEFAULT_PASSWORD_ALGORITHM = "SHA";
	private final String DEFAULT_WEBSERVICES_PATH = "api/axis/";
	private final String DEFAULT_LIFERAY_PROTOCOL_PATH = "http";
	private final String DEFAULT_AUTH_TOKEN = "";

	private String user;
	private String password;
	private String virtualhost;
	private String webappName;
	private String connectionport;
	private String passwordEncryptationAlgorithm;
	private String webServicesPath;
	private String liferayProtocol;
	private String authToken;

	private static ConfigurationReader instance;

	private ConfigurationReader() {
		readConfig();
	}

	public static ConfigurationReader getInstance() {
		if (instance == null) {
			synchronized (ConfigurationReader.class) {
				if (instance == null) {
					instance = new ConfigurationReader();
				}
			}
		}
		return instance;
	}

	/**
	 * Read database config from resource and update default connection
	 * parameters.
	 */
	private void readConfig() {
		Properties prop = new Properties();
		try {
			prop = PropertiesFile.load(LIFERAY_CONFIG_FILE);
			user = prop.getProperty(USER_TAG);
			password = prop.getProperty(PASSWORD_TAG);
			virtualhost = prop.getProperty(VIRTUAL_HOST_TAG);
			webappName = prop.getProperty(WEBAPP_TAG);
			connectionport = prop.getProperty(PORT_TAG);
			passwordEncryptationAlgorithm = prop.getProperty(PASSWORD_ALGORITHM_TAG);
			webServicesPath = prop.getProperty(WEBSERVICES_PATH_TAG);
			liferayProtocol = prop.getProperty(LIFERAY_PROTOCOL_TAG);
			authToken = prop.getProperty(AUTH_TOKEN_TAG);
		} catch (IOException e) {

		} catch (NullPointerException e) {

		}

		if (user == null) {
			user = DEFAULT_USER;
		}
		if (password == null) {
			password = DEFAULT_PASSWORD;
		}

		if (virtualhost == null) {
			virtualhost = DEFAULT_VIRTUAL_HOST;
		}

		if (connectionport == null) {
			connectionport = DEFAULT_PORT;
		}

		if (webappName == null) {
			// Webapp can be null if liferay is installed in ROOT directory of
			// Apache.
			webappName = "";
			// webappName = DEFAULT_LIFERAY_WEBAPP;
		}
		if (passwordEncryptationAlgorithm == null) {
			passwordEncryptationAlgorithm = DEFAULT_PASSWORD_ALGORITHM;
		}

		if (webServicesPath == null) {
			webServicesPath = DEFAULT_WEBSERVICES_PATH;
		}

		if (liferayProtocol == null) {
			liferayProtocol = DEFAULT_LIFERAY_PROTOCOL_PATH;
		}
		
		if (authToken == null) {
			authToken = DEFAULT_AUTH_TOKEN;
		}
	}

	public String getUser() {
		return user;
	}

	public String getPassword() {
		return password;
	}

	public String getVirtualHost() {
		return virtualhost;
	}

	public String getConnectionPort() {
		return connectionport;
	}

	public String getWebAppName() {
		if (webappName != null && webappName.length() > 0) {
			return webappName + "/";
		}
		return "";
	}

	public PasswordEncryptationAlgorithmType getPasswordEncryptationAlgorithm() {
		return PasswordEncryptationAlgorithmType.getPasswordEncryptationAlgorithms(passwordEncryptationAlgorithm);
	}

	public String getWebServicesPath() {
		return webServicesPath;
	}

	public String getLiferayProtocol() {
		return liferayProtocol;
	}

	public String getAuthToken() {
		return authToken;
	}
}
