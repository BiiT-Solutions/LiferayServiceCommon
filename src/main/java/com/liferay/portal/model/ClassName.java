package com.liferay.portal.model;

import com.biit.usermanager.entity.IElement;

public class ClassName implements java.io.Serializable, IElement<Long> {
	private static final long serialVersionUID = -2286528085111137398L;
	private long classNameId;
	private String value;

	public ClassName() {

	}

	public ClassName(long classNameId, String value) {
		this.classNameId = classNameId;
		this.value = value;
	}

	public long getClassNameId() {
		return classNameId;
	}

	public void setClassNameId(long classNameId) {
		this.classNameId = classNameId;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	@Override
	public String toString() {
		return value;
	}

	@Override
	public Long getId() {
		return getClassNameId();
	}
}
