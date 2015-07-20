package com.biit.liferay.access;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.liferay.portal.model.Contact;

public class ContactPool {
	private final static long EXPIRATION_TIME = 300000;// 5 minutes

	private Map<Long, Long> time; // Contact id -> time.
	private Map<Long, Contact> contacts; // Contact id -> User.

	public ContactPool() {
		reset();
	}

	public void reset() {
		time = new HashMap<Long, Long>();
		contacts = new HashMap<Long, Contact>();
	}

	public Contact getContact(long contactId) {
		long now = System.currentTimeMillis();
		Long storedObject = null;
		if (time.size() > 0) {
			Iterator<Long> e = new HashMap<Long, Long>(time).keySet().iterator();
			while (e.hasNext()) {
				storedObject = e.next();
				if ((now - time.get(storedObject)) > EXPIRATION_TIME) {
					// object has expired
					removeContact(storedObject);
					storedObject = null;
				} else {
					if (contacts.get(storedObject) != null && contacts.get(storedObject).getContactId() == contactId) {
						return contacts.get(contactId);
					}
				}
			}
		}
		return null;
	}

	public void addContact(Contact contact) {
		if (contact != null) {
			time.put(contact.getContactId(), System.currentTimeMillis());
			contacts.put(contact.getUserId(), contact);
		}
	}

	private void removeContact(long contactId) {
		time.remove(contactId);
		contacts.remove(contactId);
	}

}
