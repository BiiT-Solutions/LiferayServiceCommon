package com.biit.liferay.access;

import com.liferay.portal.model.ClassName;

public class ClassNamePool {
	private static ClassNamePool instance = new ClassNamePool();

	private ClassNamePool() {

	}

	public static ClassNamePool getInstance() {
		return instance;
	}

	public void addClassName(String value, ClassName className) {
		// TODO Auto-generated method stub

	}

	public ClassName getClassName(String value) {
		// TODO Auto-generated method stub
		return null;
	}

}
