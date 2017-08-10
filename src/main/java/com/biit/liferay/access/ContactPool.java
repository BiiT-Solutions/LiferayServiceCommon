package com.biit.liferay.access;

import com.biit.utils.pool.SimplePool;
import com.liferay.portal.model.Contact;

public class ContactPool extends SimplePool<Long, Contact> {
	private final static long EXPIRATION_TIME = 3600000; // 60 minutes

	private static ContactPool instance = new ContactPool();

	public static ContactPool getInstance() {
		return instance;
	}

	@Override
	public boolean isDirty(Contact element) {
		return false;
	}

	@Override
	public long getExpirationTime() {
		return EXPIRATION_TIME;
	}

}
