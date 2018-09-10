/*
 *
 *  * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *  *
 *  * WSO2 Inc. licenses this file to you under the Apache License,
 *  * Version 2.0 (the "License"); you may not use this file except
 *  * in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  *      http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing,
 *  * software distributed under the License is distributed on an
 *  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  * KIND, either express or implied. See the License for the
 *  * specific language governing permissions and limitations
 *  * under the License.
 *
 */

package org.wso2.internal.apps.license.manager.impl;

import com.google.gson.JsonArray;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.internal.apps.license.manager.connector.FtpConnectionManager;
import org.wso2.internal.apps.license.manager.exception.LicenseManagerConfigurationException;
import org.wso2.internal.apps.license.manager.service.LicenseManagerServiceEndpoint;
import org.wso2.internal.apps.license.manager.util.JsonUtils;

import java.util.ArrayList;

/**
 * Implementation of the API service to list out all the packs uploaded to the FTP server.
 */
public class GetUploadedPacksApiServiceImpl {

    private static final Logger log = LoggerFactory.getLogger(GetUploadedPacksApiServiceImpl.class);


    /**
     * Get the list of zip packs uploaded to the FTP server.
     *
     * @return list of names of the uploaded packs
     * @throws LicenseManagerConfigurationException if the SFTP connection fails
     */
    public JsonArray getListOfPacksName() throws LicenseManagerConfigurationException {

        log.info("getListofPackname");
        ArrayList<String> listOfPacks;
        JsonArray listOfPacksAsJson;
        FtpConnectionManager ftpConnectionManager = FtpConnectionManager.getFtpConnectionManager();
        listOfPacks = ftpConnectionManager.listFilesInFtpServer();
        ftpConnectionManager.closeSftpChannel();
        listOfPacksAsJson = JsonUtils.getListOfPacksUploadedAsJson(listOfPacks);
        log.info("returning listofpack");
        return listOfPacksAsJson;
    }
}
