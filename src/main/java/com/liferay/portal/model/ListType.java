package com.liferay.portal.model;

/**
 * Liferay storage of different types.
 * 
 */
public class ListType {
	private Long listTypeId;
	private String name;
	private String type;

	public Long getListTypeId() {
		return listTypeId;
	}

	public String getName() {
		return name;
	}

	public String getType() {
		return type;
	}

	public void setListTypeId(Long listTypeId) {
		this.listTypeId = listTypeId;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setType(String type) {
		this.type = type;
	}
}
