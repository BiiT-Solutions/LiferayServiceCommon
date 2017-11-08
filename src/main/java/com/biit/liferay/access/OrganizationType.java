package com.biit.liferay.access;

import java.util.Objects;

public enum OrganizationType {
	REGULAR_ORGANIZATION("regular-organization"),

	LOCATION("location");

	private String name;

	private OrganizationType(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public static OrganizationType getOrganizationType(String name) {
		for (OrganizationType organizationType : OrganizationType.values()) {
			if (Objects.equals(name, organizationType.getName())) {
				return organizationType;
			}
		}
		return null;
	}
}
