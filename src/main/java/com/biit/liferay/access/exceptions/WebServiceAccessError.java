package com.biit.liferay.access.exceptions;

public class WebServiceAccessError extends Exception {
	private static final long serialVersionUID = 90258666939113356L;

	public WebServiceAccessError(String info) {
		super(info);
	}
}
