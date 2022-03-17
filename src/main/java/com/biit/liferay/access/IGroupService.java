package com.biit.liferay.access;

import com.biit.liferay.access.exceptions.NotConnectedToWebServiceException;
import com.biit.liferay.access.exceptions.WebServiceAccessError;
import com.biit.usermanager.entity.IGroup;
import com.biit.usermanager.security.exceptions.AuthenticationRequired;
import org.apache.http.client.ClientProtocolException;

import java.io.IOException;

public interface IGroupService extends IServiceAccess, LiferayService {

    IGroup<Long> getGroup(Long companyId, String groupName) throws NotConnectedToWebServiceException,
            ClientProtocolException, IOException, AuthenticationRequired, WebServiceAccessError;

    void reset();

}
