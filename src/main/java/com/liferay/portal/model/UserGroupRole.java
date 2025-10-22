/**
 * UserGroupRoleSoap.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package com.liferay.portal.model;

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

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class UserGroupRole implements java.io.Serializable {
	private static final long serialVersionUID = 7587586600127968854L;

	private long groupId;

	private long roleId;

	private long userId;

	public UserGroupRole() {
	}

	public UserGroupRole(long groupId, long roleId, long userId) {
		this.groupId = groupId;
		this.roleId = roleId;
		this.userId = userId;
	}

	/**
	 * Gets the groupId value for this UserGroupRoleSoap.
	 *
	 * @return groupId
	 */
	public long getGroupId() {
		return groupId;
	}

	/**
	 * Gets the roleId value for this UserGroupRoleSoap.
	 *
	 * @return roleId
	 */
	public long getRoleId() {
		return roleId;
	}

	/**
	 * Gets the userId value for this UserGroupRoleSoap.
	 *
	 * @return userId
	 */
	public long getUserId() {
		return userId;
	}

	/**
	 * Sets the groupId value for this UserGroupRoleSoap.
	 *
	 * @param groupId
	 */
	public void setGroupId(long groupId) {
		this.groupId = groupId;
	}

	/**
	 * Sets the roleId value for this UserGroupRoleSoap.
	 *
	 * @param roleId
	 */
	public void setRoleId(long roleId) {
		this.roleId = roleId;
	}

	/**
	 * Sets the userId value for this UserGroupRoleSoap.
	 *
	 * @param userId
	 */
	public void setUserId(long userId) {
		this.userId = userId;
	}

}
