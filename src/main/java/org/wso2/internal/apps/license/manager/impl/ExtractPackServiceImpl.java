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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.internal.apps.license.manager.connector.FtpConnectionManager;
import org.wso2.internal.apps.license.manager.exception.LicenseManagerConfigurationException;
import org.wso2.internal.apps.license.manager.exception.LicenseManagerRuntimeException;
import org.wso2.internal.apps.license.manager.model.PackDetails;
import org.wso2.internal.apps.license.manager.model.TaskProgress;
import org.wso2.internal.apps.license.manager.util.Constants;
import org.wso2.internal.apps.license.manager.util.JarFileHandler;
import org.wso2.internal.apps.license.manager.util.TaskHandler;
import org.wso2.internal.apps.license.manager.util.ZipHandler;
import org.wso2.msf4j.util.SystemVariableUtil;

import java.io.File;

/**
 * Operations related to extracting a particular jar
 */

public class ExtractPackServiceImpl {

    private static final Logger log = LoggerFactory.getLogger(ExtractPackServiceImpl.class);

    /**
     *
     * @param username Logged in user
     * @param packName Name of the uploaded pack
     * @return
     */
    public TaskProgress startPackExtractionProcess(String username, String packName) {

        //creating new TaskProgress obj to keep track of the process
        TaskProgress taskProgress = TaskHandler.createNewTaskProgress(username, packName);
        taskProgress.setStepNumber(Constants.PACK_EXTRACTION_STEP_ID);
        taskProgress.setMessage("Pack extraction has been started");

        new Thread(() -> {
            taskProgress.setExecutingThreadId(Thread.currentThread().getId());
            String pathToStorage = SystemVariableUtil.getValue(Constants.FILE_DOWNLOAD_PATH, null);
            JarFileHandler jarFileHandler = new JarFileHandler();

            taskProgress.setMessage("Downloading the pack");

            try {
                // Initiate SFTP connection and download file
                FtpConnectionManager ftpConnectionManager = FtpConnectionManager.getFtpConnectionManager();
                ftpConnectionManager.downloadFileFromFtpServer(packName);
                ftpConnectionManager.closeSftpChannel();

                // Unzip the downloaded file.
                String zipFilePath = pathToStorage + packName;
                String filePath = zipFilePath.substring(0, zipFilePath.lastIndexOf('.'));
                File zipFile = new File(zipFilePath);
                File dir = new File(filePath);
                taskProgress.setMessage("Unzipping the pack");
                ZipHandler.unzip(zipFile.getAbsolutePath(), dir.getAbsolutePath());

                // Extract jars from the pack.
                taskProgress.setMessage("Extracting jars");
                PackDetails packDetails = jarFileHandler.extractJarsRecursively(filePath);
                taskProgress.setMessage("JarFile.java extraction complete");
                log.info("JarFile.java extraction complete");
                taskProgress.setStatus(Constants.COMPLETE);
                taskProgress.setData(packDetails);

            } catch (LicenseManagerConfigurationException e) {
                taskProgress.setStatus(Constants.FAILED);
                taskProgress.setMessage("Failed to connect to FTP server");
                log.info("FTP server error", e);
            } catch (LicenseManagerRuntimeException e) {
                taskProgress.setStatus(Constants.FAILED);
                taskProgress.setMessage("Pack contains corrupted Files, Please re-upload a different pack");
                log.info("Extraction Process Failed", e);
            }
        }).start();

        return taskProgress;
    }

}
