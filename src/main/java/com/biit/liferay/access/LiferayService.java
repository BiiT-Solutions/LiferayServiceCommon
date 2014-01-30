package com.biit.liferay.access;

import com.biit.liferay.access.exceptions.NotConnectedToWebServiceException;

public interface LiferayService {

	/**
	 * Check if the user and password has been successfully inserted before.
	 * 
	 * @return
	 */
	boolean isNotConnected();

	/**
	 * Connects to Liferay webservice using the liferay.conf file.
	 * 
	 * @throws ServiceException
	 */
	void serverConnection();

	/**
	 * Checks if a connection has been established.
	 * 
	 * @throws NotConnectedToWebServiceException
	 */
	void checkConnection() throws NotConnectedToWebServiceException;

	/**
	 * Finish the connection.
	 */
	void disconnect();

	/**
	 * Starts the secure communication with the server to obtain the JSON services.
	 * 
	 * @param address
	 * @param port
	 * @param protocol
	 * @param loginUser
	 * @param password
	 */
	void authorizedServerConnection(String address, String protocol, int port, String webservicesPath,
			String authenticationToken, String loginUser, String password);

}
