package com.biit.liferay.access;

/*-
 * #%L
 * Liferay Client Common Utils
 * %%
 * Copyright (C) 2013 - 2025 BiiT Sourcing Solutions S.L.
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

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
