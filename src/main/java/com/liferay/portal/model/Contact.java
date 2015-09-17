package com.liferay.portal.model;

import java.util.Calendar;
import java.util.Date;

/**
 * Stores extra information of a user.
 */
public class Contact {
	private Long accountId;
	private String aimSn;
	private Date birthday;
	private long classNameId;
	private long classPK;
	private long companyId;
	private long contactId;
	private Date createDate;
	private String emailAddress;
	private String employeeNumber;
	private String employeeStatusId;
	private String facebookSn;
	private String firstName;
	private String hoursOfOperation;
	private String icqSn;
	private String jabberSn;
	private String jobClass;
	private String jobTitle;
	private String lastName;
	private boolean male;
	private String middleName;
	private Date modifiedDate;
	private String msnSn;
	private String mySpaceSn;
	private long parentContactId;
	private int prefixId;
	private String skypeSn;
	private String smsSn;
	private int suffixId;
	private String twitterSn;
	private long userId;
	private String userName;
	private String ymSn;

	public Long getAccountId() {
		return accountId;
	}

	public String getAimSn() {
		return aimSn;
	}

	public Date getBirthday() {
		return birthday;
	}

	/**
	 * Get the day of the birthday.
	 * 
	 * @return day [1, 365].
	 */
	public int getBirthdayDay() {
		Calendar c = Calendar.getInstance();
		c.setTime(birthday);
		return c.get(Calendar.DAY_OF_MONTH);
	}

	/**
	 * Get the month of the birthday.
	 * 
	 * @return months [0, 11].
	 */
	public int getBirthdayMonth() {
		Calendar c = Calendar.getInstance();
		c.setTime(birthday);
		return c.get(Calendar.MONTH);
	}

	/**
	 * Get the year of the birthday.
	 * 
	 * @return year (yyyy).
	 */
	public int getBirthdayYear() {
		Calendar c = Calendar.getInstance();
		c.setTime(birthday);
		return c.get(Calendar.YEAR);
	}

	public long getClassNameId() {
		return classNameId;
	}

	public long getClassPK() {
		return classPK;
	}

	public long getCompanyId() {
		return companyId;
	}

	public long getContactId() {
		return contactId;
	}

	public Date getCreateDate() {
		return createDate;
	}

	public String getEmailAddress() {
		return emailAddress;
	}

	public String getEmployeeNumber() {
		return employeeNumber;
	}

	public String getEmployeeStatusId() {
		return employeeStatusId;
	}

	public String getFacebookSn() {
		return facebookSn;
	}

	public String getFirstName() {
		return firstName;
	}

	public String getHoursOfOperation() {
		return hoursOfOperation;
	}

	public String getIcqSn() {
		return icqSn;
	}

	public String getJabberSn() {
		return jabberSn;
	}

	public String getJobClass() {
		return jobClass;
	}

	public String getJobTitle() {
		return jobTitle;
	}

	public String getLastName() {
		return lastName;
	}

	public String getMiddleName() {
		return middleName;
	}

	public Date getModifiedDate() {
		return modifiedDate;
	}

	public String getMsnSn() {
		return msnSn;
	}

	public String getMySpaceSn() {
		return mySpaceSn;
	}

	public long getParentContactId() {
		return parentContactId;
	}

	public int getPrefixId() {
		return prefixId;
	}

	public String getSkypeSn() {
		return skypeSn;
	}

	public String getSmsSn() {
		return smsSn;
	}

	public int getSuffixId() {
		return suffixId;
	}

	public String getTwitterSn() {
		return twitterSn;
	}

	public long getUserId() {
		return userId;
	}

	public String getUserName() {
		return userName;
	}

	public String getYmSn() {
		return ymSn;
	}

	public boolean isMale() {
		return male;
	}

	public void setAccountId(Long accountId) {
		this.accountId = accountId;
	}

	public void setAimSn(String aimSn) {
		this.aimSn = aimSn;
	}

	public void setBirthday(Date birthday) {
		this.birthday = birthday;
	}

	public void setClassNameId(long classNameId) {
		this.classNameId = classNameId;
	}

	public void setClassPK(long classPK) {
		this.classPK = classPK;
	}

	public void setCompanyId(long companyId) {
		this.companyId = companyId;
	}

	public void setContactId(long contactId) {
		this.contactId = contactId;
	}

	public void setCreateDate(Date createDate) {
		this.createDate = createDate;
	}

	public void setEmailAddress(String emailAddress) {
		this.emailAddress = emailAddress;
	}

	public void setEmployeeNumber(String employeeNumber) {
		this.employeeNumber = employeeNumber;
	}

	public void setEmployeeStatusId(String employeeStatusId) {
		this.employeeStatusId = employeeStatusId;
	}

	public void setFacebookSn(String facebookSn) {
		this.facebookSn = facebookSn;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public void setHoursOfOperation(String hoursOfOperation) {
		this.hoursOfOperation = hoursOfOperation;
	}

	public void setIcqSn(String icqSn) {
		this.icqSn = icqSn;
	}

	public void setJabberSn(String jabberSn) {
		this.jabberSn = jabberSn;
	}

	public void setJobClass(String jobClass) {
		this.jobClass = jobClass;
	}

	public void setJobTitle(String jobTitle) {
		this.jobTitle = jobTitle;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	public void setMale(boolean male) {
		this.male = male;
	}

	public void setMiddleName(String middleName) {
		this.middleName = middleName;
	}

	public void setModifiedDate(Date modifiedDate) {
		this.modifiedDate = modifiedDate;
	}

	public void setMsnSn(String msnSn) {
		this.msnSn = msnSn;
	}

	public void setMySpaceSn(String mySpaceSn) {
		this.mySpaceSn = mySpaceSn;
	}

	public void setParentContactId(long parentContactId) {
		this.parentContactId = parentContactId;
	}

	public void setPrefixId(int prefixId) {
		this.prefixId = prefixId;
	}

	public void setSkypeSn(String skypeSn) {
		this.skypeSn = skypeSn;
	}

	public void setSmsSn(String smsSn) {
		this.smsSn = smsSn;
	}

	public void setSuffixId(int suffixId) {
		this.suffixId = suffixId;
	}

	public void setTwitterSn(String twitterSn) {
		this.twitterSn = twitterSn;
	}

	public void setUserId(long userId) {
		this.userId = userId;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public void setYmSn(String ymSn) {
		this.ymSn = ymSn;
	}

}
