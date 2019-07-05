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

package org.wso2.internal.apps.license.manager.connector;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.internal.apps.license.manager.exception.LicenseManagerConfigurationException;
import org.wso2.internal.apps.license.manager.util.Constants;
import org.wso2.msf4j.util.SystemVariableUtil;

import java.util.ArrayList;
import java.util.Vector;

/**
 * Create a connection with the FTP server and execute functions.
 */
public class FtpConnectionManager {

    private static final Logger log = LoggerFactory.getLogger(FtpConnectionManager.class);
    private static FtpConnectionManager ftpConnectionManager = null;
    private static Session session;
    private static ChannelSftp sftpChannel;

    private FtpConnectionManager() throws LicenseManagerConfigurationException {

        String ftpHost = SystemVariableUtil.getValue(Constants.FTP_HOST, null);
        int ftpPort = Integer.parseInt(SystemVariableUtil.getValue(Constants.FTP_PORT, null));
        String ftpUsername = SystemVariableUtil.getValue(Constants.FTP_USERNAME, null);
        String ftpPassword = SystemVariableUtil.getValue(Constants.FTP_PASSWORD, null);
        JSch jsch = new JSch();

        // Initiate SFTP connection.
        try {
            session = jsch.getSession(ftpUsername, ftpHost, ftpPort);
            session.setPassword(ftpPassword);
            java.util.Properties config = new java.util.Properties();
            config.put("StrictHostKeyChecking", "no");
            session.setConfig(config);
            session.connect();

            sftpChannel = (ChannelSftp) session.openChannel("sftp");
            sftpChannel.connect(600000);
        } catch (JSchException e) {
            throw new LicenseManagerConfigurationException("Failed to initiate a connection with FTP server", e);
        }
    }

    public static synchronized FtpConnectionManager getFtpConnectionManager()
            throws LicenseManagerConfigurationException {

        if (ftpConnectionManager == null || !session.isConnected()) {
            ftpConnectionManager = new FtpConnectionManager();
        } else {
            closeSftpChannel();
            try {
                sftpChannel = (ChannelSftp) session.openChannel("sftp");
                sftpChannel.connect();
            } catch (JSchException e) {
                throw new LicenseManagerConfigurationException("Failed to initiate a connection with FTP server", e);
            }
        }
        return ftpConnectionManager;

    }

    public static void closeSftpChannel() {

        if (sftpChannel != null) {
            sftpChannel.exit();
        }
    }

    public void downloadFileFromFtpServer(String packName) throws LicenseManagerConfigurationException {

        String ftpFilePath = SystemVariableUtil.getValue(Constants.FTP_FILE_LOCATION, null);
        String pathToStorage = SystemVariableUtil.getValue(Constants.FILE_DOWNLOAD_PATH, null);
        try {
            sftpChannel.get(ftpFilePath + packName, pathToStorage);
            if (log.isDebugEnabled()) {
                log.debug("The file " + packName + " is successfully downloaded to location " + pathToStorage);
            }
        } catch (SftpException e) {
            throw new LicenseManagerConfigurationException("Failed to download file from FTP server", e);
        }
    }

    public ArrayList<String> listFilesInFtpServer() throws LicenseManagerConfigurationException {

        String ftpFilePath = SystemVariableUtil.getValue(Constants.FTP_FILE_LOCATION, null);
        ArrayList<String> listOfPacks = new ArrayList<>();

        // Obtain the list of the available zip files.
        Vector fileList;
        try {
            fileList = sftpChannel.ls(ftpFilePath);
            for (Object aFileListVector : fileList) {
                ChannelSftp.LsEntry entry = (ChannelSftp.LsEntry) aFileListVector;
                if (entry.getFilename().endsWith(".zip")) {
                    listOfPacks.add(entry.getFilename());
                }
            }
            return listOfPacks;
        } catch (SftpException e) {
            sftpChannel.exit();
            throw new LicenseManagerConfigurationException("Failed to get the list of files from FTP server", e);
        }
    }

    public void deleteFileFromFtpServer(String fileName) throws LicenseManagerConfigurationException {

        String ftpFilePath = SystemVariableUtil.getValue(Constants.FTP_FILE_LOCATION, null);

        // Remove the pack from
        try {
            sftpChannel.rm(ftpFilePath + fileName + ".zip");
            if (log.isDebugEnabled()) {
                log.debug("The file " + fileName + ".zip" + " is removed from the FTP server");
            }
        } catch (SftpException e) {
            sftpChannel.exit();
            throw new LicenseManagerConfigurationException("Failed to delete file from FTP server", e);
        }
    }
}
