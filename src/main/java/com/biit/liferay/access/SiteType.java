package com.biit.liferay.access;

public enum SiteType {
	TYPE_COMMUNITY_OPEN(1),

	TYPE_COMMUNITY_RESTRICTED(2),

	TYPE_COMMUNITY_PRIVATE(3),

	DEFAULT_PARENT_GROUP_ID(0);

	private int type;

	private SiteType(int type) {
		this.type = type;
	}

	public int getType() {
		return type;
	}
}
