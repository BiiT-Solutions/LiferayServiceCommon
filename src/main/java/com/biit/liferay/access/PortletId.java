package com.biit.liferay.access;

public enum PortletId {
	DOCUMENT_AND_MEDIA("20"),

	KNOWLEDGEBASE_PORTLET("3_WAR_knowledgebaseportlet");

	private String id;

	private PortletId(String id) {
		this.id = id;
	}

	public String getId() {
		return id;
	}

	@Override
	public String toString() {
		return getId();
	}

}
