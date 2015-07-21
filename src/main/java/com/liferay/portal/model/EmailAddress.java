/**
 * EmailAddressSoap.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package com.liferay.portal.model;

public class EmailAddress implements java.io.Serializable {
	private static final long serialVersionUID = -2734153079494119731L;

	private java.lang.String address;

	private long classNameId;

	private long classPK;

	private long companyId;

	private java.util.Calendar createDate;

	private long emailAddressId;

	private java.util.Calendar modifiedDate;

	private boolean primary;

	private long primaryKey;

	private int typeId;

	private long userId;

	private java.lang.String userName;

	private java.lang.String uuid;

	public EmailAddress() {
	}

	public EmailAddress(java.lang.String address, long classNameId, long classPK, long companyId,
			java.util.Calendar createDate, long emailAddressId, java.util.Calendar modifiedDate, boolean primary,
			long primaryKey, int typeId, long userId, java.lang.String userName, java.lang.String uuid) {
		this.address = address;
		this.classNameId = classNameId;
		this.classPK = classPK;
		this.companyId = companyId;
		this.createDate = createDate;
		this.emailAddressId = emailAddressId;
		this.modifiedDate = modifiedDate;
		this.primary = primary;
		this.primaryKey = primaryKey;
		this.typeId = typeId;
		this.userId = userId;
		this.userName = userName;
		this.uuid = uuid;
	}

	/**
	 * Gets the address value for this EmailAddressSoap.
	 * 
	 * @return address
	 */
	public java.lang.String getAddress() {
		return address;
	}

	/**
	 * Gets the classNameId value for this EmailAddressSoap.
	 * 
	 * @return classNameId
	 */
	public long getClassNameId() {
		return classNameId;
	}

	/**
	 * Gets the classPK value for this EmailAddressSoap.
	 * 
	 * @return classPK
	 */
	public long getClassPK() {
		return classPK;
	}

	/**
	 * Gets the companyId value for this EmailAddressSoap.
	 * 
	 * @return companyId
	 */
	public long getCompanyId() {
		return companyId;
	}

	/**
	 * Gets the createDate value for this EmailAddressSoap.
	 * 
	 * @return createDate
	 */
	public java.util.Calendar getCreateDate() {
		return createDate;
	}

	/**
	 * Gets the emailAddressId value for this EmailAddressSoap.
	 * 
	 * @return emailAddressId
	 */
	public long getEmailAddressId() {
		return emailAddressId;
	}

	/**
	 * Gets the modifiedDate value for this EmailAddressSoap.
	 * 
	 * @return modifiedDate
	 */
	public java.util.Calendar getModifiedDate() {
		return modifiedDate;
	}

	/**
	 * Gets the primaryKey value for this EmailAddressSoap.
	 * 
	 * @return primaryKey
	 */
	public long getPrimaryKey() {
		return primaryKey;
	}

	/**
	 * Gets the typeId value for this EmailAddressSoap.
	 * 
	 * @return typeId
	 */
	public int getTypeId() {
		return typeId;
	}

	/**
	 * Gets the userId value for this EmailAddressSoap.
	 * 
	 * @return userId
	 */
	public long getUserId() {
		return userId;
	}

	/**
	 * Gets the userName value for this EmailAddressSoap.
	 * 
	 * @return userName
	 */
	public java.lang.String getUserName() {
		return userName;
	}

	/**
	 * Gets the uuid value for this EmailAddressSoap.
	 * 
	 * @return uuid
	 */
	public java.lang.String getUuid() {
		return uuid;
	}

	/**
	 * Gets the primary value for this EmailAddressSoap.
	 * 
	 * @return primary
	 */
	public boolean isPrimary() {
		return primary;
	}

	/**
	 * Sets the address value for this EmailAddressSoap.
	 * 
	 * @param address
	 */
	public void setAddress(java.lang.String address) {
		this.address = address;
	}

	/**
	 * Sets the classNameId value for this EmailAddressSoap.
	 * 
	 * @param classNameId
	 */
	public void setClassNameId(long classNameId) {
		this.classNameId = classNameId;
	}

	/**
	 * Sets the classPK value for this EmailAddressSoap.
	 * 
	 * @param classPK
	 */
	public void setClassPK(long classPK) {
		this.classPK = classPK;
	}

	/**
	 * Sets the companyId value for this EmailAddressSoap.
	 * 
	 * @param companyId
	 */
	public void setCompanyId(long companyId) {
		this.companyId = companyId;
	}

	/**
	 * Sets the createDate value for this EmailAddressSoap.
	 * 
	 * @param createDate
	 */
	public void setCreateDate(java.util.Calendar createDate) {
		this.createDate = createDate;
	}

	/**
	 * Sets the emailAddressId value for this EmailAddressSoap.
	 * 
	 * @param emailAddressId
	 */
	public void setEmailAddressId(long emailAddressId) {
		this.emailAddressId = emailAddressId;
	}

	/**
	 * Sets the modifiedDate value for this EmailAddressSoap.
	 * 
	 * @param modifiedDate
	 */
	public void setModifiedDate(java.util.Calendar modifiedDate) {
		this.modifiedDate = modifiedDate;
	}

	/**
	 * Sets the primary value for this EmailAddressSoap.
	 * 
	 * @param primary
	 */
	public void setPrimary(boolean primary) {
		this.primary = primary;
	}

	/**
	 * Sets the primaryKey value for this EmailAddressSoap.
	 * 
	 * @param primaryKey
	 */
	public void setPrimaryKey(long primaryKey) {
		this.primaryKey = primaryKey;
	}

	/**
	 * Sets the typeId value for this EmailAddressSoap.
	 * 
	 * @param typeId
	 */
	public void setTypeId(int typeId) {
		this.typeId = typeId;
	}

	/**
	 * Sets the userId value for this EmailAddressSoap.
	 * 
	 * @param userId
	 */
	public void setUserId(long userId) {
		this.userId = userId;
	}

	/**
	 * Sets the userName value for this EmailAddressSoap.
	 * 
	 * @param userName
	 */
	public void setUserName(java.lang.String userName) {
		this.userName = userName;
	}

	/**
	 * Sets the uuid value for this EmailAddressSoap.
	 * 
	 * @param uuid
	 */
	public void setUuid(java.lang.String uuid) {
		this.uuid = uuid;
	}

	@Override
	public String toString() {
		return address;
	}
}
