/**
 * PortletPreferencesIds.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package com.liferay.portal.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class PortletPreferencesIds implements java.io.Serializable {
	private static final long serialVersionUID = 7652885934006438491L;

	private long companyId;

	private long ownerId;

	private int ownerType;

	private long plid;

	private java.lang.String portletId;

	private java.lang.Object __equalsCalc = null;

	private boolean __hashCodeCalc = false;

	public PortletPreferencesIds() {
	}

	public PortletPreferencesIds(long companyId, long ownerId, int ownerType, long plid, java.lang.String portletId) {
		this.companyId = companyId;
		this.ownerId = ownerId;
		this.ownerType = ownerType;
		this.plid = plid;
		this.portletId = portletId;
	}

	public synchronized boolean equals(java.lang.Object obj) {
		if (obj == null)
			return false;
		if (!(obj instanceof PortletPreferencesIds))
			return false;
		if (this == obj)
			return true;
		if (__equalsCalc != null) {
			return (__equalsCalc == obj);
		}
		__equalsCalc = obj;
		boolean _equals;
		PortletPreferencesIds other = (PortletPreferencesIds) obj;
		_equals = true && this.companyId == other.getCompanyId() && this.ownerId == other.getOwnerId() && this.ownerType == other.getOwnerType()
				&& this.plid == other.getPlid()
				&& ((this.portletId == null && other.getPortletId() == null) || (this.portletId != null && this.portletId.equals(other.getPortletId())));
		__equalsCalc = null;
		return _equals;
	}

	/**
	 * Gets the companyId value for this PortletPreferencesIds.
	 * 
	 * @return companyId
	 */
	public long getCompanyId() {
		return companyId;
	}

	/**
	 * Gets the ownerId value for this PortletPreferencesIds.
	 * 
	 * @return ownerId
	 */
	public long getOwnerId() {
		return ownerId;
	}

	/**
	 * Gets the ownerType value for this PortletPreferencesIds.
	 * 
	 * @return ownerType
	 */
	public int getOwnerType() {
		return ownerType;
	}

	/**
	 * Gets the plid value for this PortletPreferencesIds.
	 * 
	 * @return plid
	 */
	public long getPlid() {
		return plid;
	}

	/**
	 * Gets the portletId value for this PortletPreferencesIds.
	 * 
	 * @return portletId
	 */
	public java.lang.String getPortletId() {
		return portletId;
	}

	public synchronized int hashCode() {
		if (__hashCodeCalc) {
			return 0;
		}
		__hashCodeCalc = true;
		int _hashCode = 1;
		_hashCode += new Long(getCompanyId()).hashCode();
		_hashCode += new Long(getOwnerId()).hashCode();
		_hashCode += getOwnerType();
		_hashCode += new Long(getPlid()).hashCode();
		if (getPortletId() != null) {
			_hashCode += getPortletId().hashCode();
		}
		__hashCodeCalc = false;
		return _hashCode;
	}

	/**
	 * Sets the companyId value for this PortletPreferencesIds.
	 * 
	 * @param companyId
	 */
	public void setCompanyId(long companyId) {
		this.companyId = companyId;
	}

	/**
	 * Sets the ownerId value for this PortletPreferencesIds.
	 * 
	 * @param ownerId
	 */
	public void setOwnerId(long ownerId) {
		this.ownerId = ownerId;
	}

	/**
	 * Sets the ownerType value for this PortletPreferencesIds.
	 * 
	 * @param ownerType
	 */
	public void setOwnerType(int ownerType) {
		this.ownerType = ownerType;
	}

	/**
	 * Sets the plid value for this PortletPreferencesIds.
	 * 
	 * @param plid
	 */
	public void setPlid(long plid) {
		this.plid = plid;
	}

	/**
	 * Sets the portletId value for this PortletPreferencesIds.
	 * 
	 * @param portletId
	 */
	public void setPortletId(java.lang.String portletId) {
		this.portletId = portletId;
	}

}
