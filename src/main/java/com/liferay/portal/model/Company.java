/**
 * CompanySoap.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package com.liferay.portal.model;

import com.biit.usermanager.entity.IGroup;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Company implements java.io.Serializable, IGroup<Long> {
	private static final long serialVersionUID = -5076489772058450143L;

	private long accountId;

	private boolean active;

	private long companyId;

	private String homeURL;

	private String key;

	private long logoId;

	private int maxUsers;

	private String mx;

	private long primaryKey;

	private boolean system;

	private String webId;

	public Company() {
	}

	public Company(long accountId, boolean active, long companyId, String homeURL, String key,
			long logoId, int maxUsers, String mx, long primaryKey, boolean system, String webId) {
		this.accountId = accountId;
		this.active = active;
		this.companyId = companyId;
		this.homeURL = homeURL;
		this.key = key;
		this.logoId = logoId;
		this.maxUsers = maxUsers;
		this.mx = mx;
		this.primaryKey = primaryKey;
		this.system = system;
		this.webId = webId;
	}

	/**
	 * Gets the accountId value for this CompanySoap.
	 * 
	 * @return accountId
	 */
	public long getAccountId() {
		return accountId;
	}

	/**
	 * Gets the companyId value for this CompanySoap.
	 * 
	 * @return companyId
	 */
	public long getCompanyId() {
		return companyId;
	}

	/**
	 * Gets the homeURL value for this CompanySoap.
	 * 
	 * @return homeURL
	 */
	public String getHomeURL() {
		return homeURL;
	}

	@Override
	public Long getId() {
		return getCompanyId();
	}

	/**
	 * Gets the key value for this CompanySoap.
	 * 
	 * @return key
	 */
	public String getKey() {
		return key;
	}

	/**
	 * Gets the logoId value for this CompanySoap.
	 * 
	 * @return logoId
	 */
	public long getLogoId() {
		return logoId;
	}

	/**
	 * Gets the maxUsers value for this CompanySoap.
	 * 
	 * @return maxUsers
	 */
	public int getMaxUsers() {
		return maxUsers;
	}

	/**
	 * Gets the mx value for this CompanySoap.
	 * 
	 * @return mx
	 */
	public String getMx() {
		return mx;
	}

	/**
	 * Gets the primaryKey value for this CompanySoap.
	 * 
	 * @return primaryKey
	 */
	public long getPrimaryKey() {
		return primaryKey;
	}

	@Override
	public String getUniqueName() {
		return getWebId();
	}

	/**
	 * Gets the webId value for this CompanySoap.
	 * 
	 * @return webId
	 */
	public String getWebId() {
		return webId;
	}

	/**
	 * Gets the active value for this CompanySoap.
	 * 
	 * @return active
	 */
	public boolean isActive() {
		return active;
	}

	/**
	 * Gets the system value for this CompanySoap.
	 * 
	 * @return system
	 */
	public boolean isSystem() {
		return system;
	}

	/**
	 * Sets the accountId value for this CompanySoap.
	 * 
	 * @param accountId
	 */
	public void setAccountId(long accountId) {
		this.accountId = accountId;
	}

	/**
	 * Sets the active value for this CompanySoap.
	 * 
	 * @param active
	 */
	public void setActive(boolean active) {
		this.active = active;
	}

	/**
	 * Sets the companyId value for this CompanySoap.
	 * 
	 * @param companyId
	 */
	public void setCompanyId(long companyId) {
		this.companyId = companyId;
	}

	/**
	 * Sets the homeURL value for this CompanySoap.
	 * 
	 * @param homeURL
	 */
	public void setHomeURL(String homeURL) {
		this.homeURL = homeURL;
	}

	/**
	 * Sets the key value for this CompanySoap.
	 * 
	 * @param key
	 */
	public void setKey(String key) {
		this.key = key;
	}

	/**
	 * Sets the logoId value for this CompanySoap.
	 * 
	 * @param logoId
	 */
	public void setLogoId(long logoId) {
		this.logoId = logoId;
	}

	/**
	 * Sets the maxUsers value for this CompanySoap.
	 * 
	 * @param maxUsers
	 */
	public void setMaxUsers(int maxUsers) {
		this.maxUsers = maxUsers;
	}

	/**
	 * Sets the mx value for this CompanySoap.
	 * 
	 * @param mx
	 */
	public void setMx(String mx) {
		this.mx = mx;
	}

	/**
	 * Sets the primaryKey value for this CompanySoap.
	 * 
	 * @param primaryKey
	 */
	public void setPrimaryKey(long primaryKey) {
		this.primaryKey = primaryKey;
	}

	/**
	 * Sets the system value for this CompanySoap.
	 * 
	 * @param system
	 */
	public void setSystem(boolean system) {
		this.system = system;
	}

	/**
	 * Sets the webId value for this CompanySoap.
	 * 
	 * @param webId
	 */
	public void setWebId(String webId) {
		this.webId = webId;
	}

}
