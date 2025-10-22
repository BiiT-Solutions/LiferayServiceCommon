package com.biit.liferay.access;

/*-
 * #%L
 * Liferay Client Common Utils
 * %%
 * Copyright (C) 2013 - 2025 BiiT Sourcing Solutions S.L.
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

import com.biit.liferay.access.exceptions.NotConnectedToWebServiceException;

public interface LiferayService {

    /**
     * Starts the secure communication with the server to obtain the JSON services.
     *
     * @param address
     * @param port
     * @param protocol
     * @param loginUser
     * @param password
     */
    void authorizedServerConnection(String address, String protocol, int port, String proxyPrefix, String webservicesPath,
                                    String authenticationToken, String loginUser, String password);

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
     * Check if the user and password has been successfully inserted before.
     *
     * @return
     */
    boolean isNotConnected();

    /**
     * Connects to Liferay webservice using the liferay.conf file.
     */
    void serverConnection();

    /**
     * Starts the secure communication with the server to obtain the JSON services.
     *
     * @param user
     * @param password
     */
    void serverConnection(String user, String password);

}
