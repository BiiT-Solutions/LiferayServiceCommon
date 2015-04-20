package com.biit.liferay.access;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.liferay.portal.model.Company;

public class CompanyPool {

	private final static long EXPIRATION_TIME = 300000;// 5 minutes

	private Map<Long, Long> byIdTime;
	private Map<Long, Company> companyPoolById;

	private Map<String, Long> byVirtualHostTime;
	private Map<String, Company> companyPoolByVirtualHost;

	private Map<String, Long> byWebIdTime;
	private Map<String, Company> companyPoolByWebId;

	private static CompanyPool instance = new CompanyPool();

	public static CompanyPool getInstance() {
		return instance;
	}

	private CompanyPool() {
		reset();
	}

	public void reset() {
		byIdTime = new HashMap<Long, Long>();
		companyPoolById = new HashMap<Long, Company>();
		byVirtualHostTime = new HashMap<String, Long>();
		companyPoolByVirtualHost = new HashMap<String, Company>();
		byWebIdTime = new HashMap<String, Long>();
		companyPoolByWebId = new HashMap<String, Company>();
	}

	public void addCompany(Company company) {
		if (company != null) {
			long now = System.currentTimeMillis();

			companyPoolById.put(company.getCompanyId(), company);
			byIdTime.put(company.getCompanyId(), now);

			companyPoolByWebId.put(company.getWebId(), company);
			byWebIdTime.put(company.getWebId(), now);
		}
	}

	public Company getCompanyById(long companyId) {
		long now = System.currentTimeMillis();
		Long nextCompanyId = null;
		if (byIdTime.size() > 0) {
			Iterator<Long> e = new HashMap<Long, Long>(byIdTime).keySet().iterator();
			while (e.hasNext()) {
				nextCompanyId = e.next();
				if ((now - byIdTime.get(nextCompanyId)) > EXPIRATION_TIME) {
					// object has expired
					removeCompanyById(nextCompanyId);
					nextCompanyId = null;
				} else {
					if (companyId == nextCompanyId) {
						return companyPoolById.get(nextCompanyId);
					}
				}
			}
		}
		return null;
	}

	private void removeCompanyById(Long companyId) {
		byIdTime.remove(companyId);
		companyPoolById.remove(companyId);
	}

	public Company getCompanyByWebId(String webId) {
		long now = System.currentTimeMillis();
		String nextCompanyWebId = null;
		if (byWebIdTime.size() > 0) {
			Iterator<String> e = new HashMap<String, Long>(byWebIdTime).keySet().iterator();
			while (e.hasNext()) {
				nextCompanyWebId = e.next();
				if ((now - byWebIdTime.get(nextCompanyWebId)) > EXPIRATION_TIME) {
					// object has expired
					removeCompanyByWebId(nextCompanyWebId);
					nextCompanyWebId = null;
				} else {
					if (webId.equals(nextCompanyWebId)) {
						return companyPoolByWebId.get(nextCompanyWebId);
					}
				}
			}
		}
		return null;
	}

	private void removeCompanyByWebId(String webId) {
		byWebIdTime.remove(webId);
		companyPoolByWebId.remove(webId);
	}

	public void addCompany(Company company, String virtualHost) {
		if (company != null) {
			addCompany(company);
			long now = System.currentTimeMillis();

			companyPoolByVirtualHost.put(virtualHost, company);
			byVirtualHostTime.put(virtualHost, now);
		}
	}

	public Company getCompanyByVirtualHostId(String virtualHost) {
		long now = System.currentTimeMillis();
		String nextCompanyVirtualHost = null;
		if (byVirtualHostTime.size() > 0) {
			Iterator<String> e = new HashMap<String, Long>(byVirtualHostTime).keySet().iterator();
			while (e.hasNext()) {
				nextCompanyVirtualHost = e.next();
				if ((now - byVirtualHostTime.get(nextCompanyVirtualHost)) > EXPIRATION_TIME) {
					// object has expired
					removeCompanyByVirtualHost(nextCompanyVirtualHost);
					nextCompanyVirtualHost = null;
				} else {
					if (virtualHost.equals(nextCompanyVirtualHost)) {
						return companyPoolByVirtualHost.get(nextCompanyVirtualHost);
					}
				}
			}
		}
		return null;
	}

	private void removeCompanyByVirtualHost(String webId) {
		byVirtualHostTime.remove(webId);
		companyPoolByVirtualHost.remove(webId);
	}

}
