package com.liferay.portal.model;

public enum Status {

	ANY(-1),

	APPROVED(0),

	PENDING(1),

	DRAFT(2),

	EXPIRED(3),

	DENIED(4),

	INACTIVE(5),

	INCOMPLETE(6),

	SCHEDULED(7),

	TRASH(8),

	DRAFT_FROM_APPROVED(8);

	private int value;

	private Status(int value) {
		this.value = value;
	}

	public int getValue() {
		return value;
	}

}
