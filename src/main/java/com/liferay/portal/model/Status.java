package com.liferay.portal.model;

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
