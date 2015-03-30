/*
*  Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.wso2.carbon.analytics.dataservice.servlet.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.analytics.dataservice.servlet.exception.AnalyticsAPIAuthenticationException;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class AnalyticsAPIAuthenticator {
    private static Log log = LogFactory.getLog(AnalyticsAPIAuthenticator.class);
    //TODO: change to hazelcast
    private List<String> sessionIds;

    public AnalyticsAPIAuthenticator() {
        sessionIds = new ArrayList<>();
    }

    public String authenticate(String username, String password) throws AnalyticsAPIAuthenticationException {
        if (username == null || username.trim().isEmpty()) {
            logAndThrowAuthException("Username is not provided!");
        }
        if (password == null || password.trim().isEmpty()) {
            logAndThrowAuthException("Password is not provided!");
        }
        String userName = MultitenantUtils.getTenantAwareUsername(username);
        if (MultitenantUtils.getTenantDomain(username).equals(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME)) {
            boolean authenticated = ServiceHolder.getAuthenticationService().authenticate(userName, password);
            if (authenticated) {
                String sessionId = UUID.randomUUID().toString();
                sessionIds.add(sessionId);
                return sessionId;
            } else {
                logAndThrowAuthException("Login failed for user :" + userName);
            }
        } else {
            logAndThrowAuthException("Only super tenant users is authenticated to use the service!");
        }
        return null;
    }

    private void logAndThrowAuthException(String message) throws AnalyticsAPIAuthenticationException {
        log.error(message);
        throw new AnalyticsAPIAuthenticationException(message);
    }

    public void validateSessionId(String sessionId) throws AnalyticsAPIAuthenticationException {
        if (!sessionIds.contains(sessionId)) {
            logAndThrowAuthException("Unauthenticated session Id : " + sessionId);
        }
    }

    public void logout(String sessionId){
        sessionIds.remove(sessionId);
    }

}
